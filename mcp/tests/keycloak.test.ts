import { describe, expect, it, vi } from 'vitest';

import {
  type KeycloakAdminLike,
  KeycloakService,
  type KeycloakEnv,
} from '../src/keycloak/client.js';
import { KEYCLOAK_TOOL_NAMES, createKeycloakToolDefinitions } from '../src/keycloak/server.js';

function createEnv(): KeycloakEnv {
  return {
    KEYCLOAK_BASE_URL: 'https://keycloak.example.com',
    KEYCLOAK_AUTH_REALM: 'master',
    KEYCLOAK_CLIENT_ID: 'svc-client',
    KEYCLOAK_CLIENT_SECRET: 'secret',
    KEYCLOAK_DEFAULT_REALM: 'books',
  };
}

function createAdminClient(): KeycloakAdminLike {
  return {
    auth: vi.fn().mockResolvedValue(undefined),
    setConfig: vi.fn(),
    realms: {
      find: vi.fn().mockResolvedValue([{ realm: 'master' }, { realm: 'books' }]),
    },
    users: {
      find: vi.fn().mockResolvedValue([{ id: 'user-1', username: 'reader' }]),
      create: vi.fn().mockResolvedValue({ id: 'user-1' }),
      addToGroup: vi.fn().mockResolvedValue(undefined),
      addRealmRoleMappings: vi.fn().mockResolvedValue(undefined),
    },
    groups: {
      find: vi.fn().mockResolvedValue([{ id: 'group-1', name: 'admins' }]),
    },
    roles: {
      find: vi.fn().mockResolvedValue([{ id: 'role-1', name: 'manage-users' }]),
      findOneByName: vi.fn().mockImplementation(async ({ name }: { name: string }) => ({ id: `${name}-id`, name })),
    },
  };
}

describe('Keycloak service', () => {
  it('authenticates with client credentials and scopes queries to a realm', async () => {
    const adminClient = createAdminClient();
    const service = new KeycloakService(adminClient, createEnv());

    const users = await service.listUsers({ realm: 'books', max: 10 });

    expect(users).toHaveLength(1);
    expect(adminClient.auth).toHaveBeenCalledWith({
      grantType: 'client_credentials',
      clientId: 'svc-client',
      clientSecret: 'secret',
    });
    expect(adminClient.setConfig).toHaveBeenCalledWith({ realmName: 'books' });
    expect(adminClient.users.find).toHaveBeenCalledWith({ first: undefined, max: 10, search: undefined });
  });

  it('resolves realm roles before assigning them', async () => {
    const adminClient = createAdminClient();
    const service = new KeycloakService(adminClient, createEnv());

    const result = await service.assignRealmRoles({
      userId: 'user-1',
      roleNames: ['manage-users', 'view-users'],
    });

    expect(result.assignedRoles).toEqual(['manage-users', 'view-users']);
    expect(adminClient.roles.findOneByName).toHaveBeenCalledTimes(2);
    expect(adminClient.users.addRealmRoleMappings).toHaveBeenCalledWith({
      id: 'user-1',
      roles: [
        { id: 'manage-users-id', name: 'manage-users', description: undefined },
        { id: 'view-users-id', name: 'view-users', description: undefined },
      ],
    });
  });
});

describe('Keycloak tool registration', () => {
  it('registers the full Keycloak tool surface', () => {
    const service = new KeycloakService(createAdminClient(), createEnv());

    const tools = createKeycloakToolDefinitions(service);

    expect(tools.map((tool) => tool.name).sort()).toEqual([...KEYCLOAK_TOOL_NAMES].sort());
  });

  it('blocks mutating tools without confirm:true', async () => {
    const service = new KeycloakService(createAdminClient(), createEnv());
    const tools = createKeycloakToolDefinitions(service);
    const createUserTool = tools.find((tool) => tool.name === 'keycloak_create_user');

    const result = await createUserTool?.execute({
      username: 'reader',
      confirm: false,
    });

    expect(result?.isError).toBe(true);
    expect(result?.content[0]?.text).toContain('confirm:true');
  });

  it('returns human-readable text for realm listing', async () => {
    const service = new KeycloakService(createAdminClient(), createEnv());
    const tools = createKeycloakToolDefinitions(service);
    const listRealmsTool = tools.find((tool) => tool.name === 'keycloak_list_realms');

    const result = await listRealmsTool?.execute({});

    expect(result?.content[0]?.text).toContain('Keycloak realms');
    expect(result?.content[0]?.text).toContain('master');
  });
});
