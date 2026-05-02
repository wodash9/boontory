import KcAdminClient from '@keycloak/keycloak-admin-client';
import { z } from 'zod';

import { parseEnv, nonEmptyString, urlString } from '../shared/env.js';

const keycloakEnvSchema = z.object({
  KEYCLOAK_BASE_URL: urlString,
  KEYCLOAK_AUTH_REALM: nonEmptyString,
  KEYCLOAK_CLIENT_ID: nonEmptyString,
  KEYCLOAK_CLIENT_SECRET: nonEmptyString,
  KEYCLOAK_DEFAULT_REALM: nonEmptyString.optional(),
});

export type KeycloakEnv = z.infer<typeof keycloakEnvSchema>;

export function loadKeycloakEnv(source: NodeJS.ProcessEnv = process.env): KeycloakEnv {
  return parseEnv(keycloakEnvSchema, source);
}

export interface RealmSummary {
  id?: string;
  realm?: string;
  displayName?: string;
}

export interface UserSummary {
  id?: string;
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  enabled?: boolean;
  emailVerified?: boolean;
}

export interface GroupSummary {
  id?: string;
  name?: string;
  path?: string;
}

export interface RoleSummary {
  id?: string;
  name?: string;
  description?: string;
}

export interface RoleMapping {
  id: string;
  name: string;
  description?: string | undefined;
}

export interface CreateUserInput {
  realm?: string | undefined;
  username: string;
  email?: string | undefined;
  firstName?: string | undefined;
  lastName?: string | undefined;
  enabled?: boolean | undefined;
  emailVerified?: boolean | undefined;
}

export interface CreateUserPayload {
  realm?: string | undefined;
  username?: string | undefined;
  email?: string | undefined;
  firstName?: string | undefined;
  lastName?: string | undefined;
  enabled?: boolean | undefined;
  emailVerified?: boolean | undefined;
}

export interface KeycloakAdminLike {
  auth(credentials: {
    grantType: 'client_credentials';
    clientId: string;
    clientSecret: string;
  }): Promise<void>;
  setConfig(config: { realmName: string }): void;
  realms: {
    find(): Promise<RealmSummary[]>;
  };
  users: {
    find(query?: {
      first?: number | undefined;
      max?: number | undefined;
      search?: string | undefined;
    }): Promise<UserSummary[]>;
    create(payload?: CreateUserPayload): Promise<{ id: string }>;
    addToGroup(params: { id: string; groupId: string }): Promise<unknown>;
    addRealmRoleMappings(params?: { id: string; roles: RoleMapping[]; realm?: string | undefined }): Promise<unknown>;
  };
  groups: {
    find(query?: {
      first?: number | undefined;
      max?: number | undefined;
      search?: string | undefined;
    }): Promise<GroupSummary[]>;
  };
  roles: {
    find(query?: {
      first?: number | undefined;
      max?: number | undefined;
      search?: string | undefined;
    }): Promise<RoleSummary[]>;
    findOneByName(params: { name: string }): Promise<RoleSummary | undefined>;
  };
}

export function createKeycloakAdminClient(env: KeycloakEnv): KeycloakAdminLike {
  return new KcAdminClient({
    baseUrl: env.KEYCLOAK_BASE_URL,
    realmName: env.KEYCLOAK_AUTH_REALM,
  });
}

export class KeycloakService {
  private readonly adminClient: KeycloakAdminLike;
  private readonly env: KeycloakEnv;

  constructor(adminClient: KeycloakAdminLike, env: KeycloakEnv) {
    this.adminClient = adminClient;
    this.env = env;
  }

  private async authenticate(): Promise<void> {
    await this.adminClient.auth({
      grantType: 'client_credentials',
      clientId: this.env.KEYCLOAK_CLIENT_ID,
      clientSecret: this.env.KEYCLOAK_CLIENT_SECRET,
    });
  }

  private resolveRealm(realm?: string): string {
    return realm?.trim() || this.env.KEYCLOAK_DEFAULT_REALM || this.env.KEYCLOAK_AUTH_REALM;
  }

  private buildListQuery(options: {
    first?: number | undefined;
    max?: number | undefined;
    search?: string | undefined;
  }): { first?: number | undefined; max?: number | undefined; search?: string | undefined } {
    const query: { first?: number | undefined; max?: number | undefined; search?: string | undefined } = {};

    if (options.first !== undefined) {
      query.first = options.first;
    }
    if (options.max !== undefined) {
      query.max = options.max;
    }
    if (options.search !== undefined) {
      query.search = options.search;
    }

    return query;
  }

  private buildCreateUserPayload(input: CreateUserInput, realm: string): CreateUserPayload {
    const payload: CreateUserPayload = {
      realm,
      username: input.username,
      enabled: input.enabled ?? true,
      emailVerified: input.emailVerified ?? false,
    };

    if (input.email !== undefined) {
      payload.email = input.email;
    }
    if (input.firstName !== undefined) {
      payload.firstName = input.firstName;
    }
    if (input.lastName !== undefined) {
      payload.lastName = input.lastName;
    }

    return payload;
  }

  private async prepareRealm(realm?: string): Promise<string> {
    await this.authenticate();
    const targetRealm = this.resolveRealm(realm);
    this.adminClient.setConfig({ realmName: targetRealm });
    return targetRealm;
  }

  async listRealms(): Promise<RealmSummary[]> {
    await this.authenticate();
    this.adminClient.setConfig({ realmName: this.env.KEYCLOAK_AUTH_REALM });
    return this.adminClient.realms.find();
  }

  async listUsers(options: { realm?: string | undefined; first?: number | undefined; max?: number | undefined; search?: string | undefined }): Promise<UserSummary[]> {
    await this.prepareRealm(options.realm);
    return this.adminClient.users.find(this.buildListQuery(options));
  }

  async listGroups(options: { realm?: string | undefined; first?: number | undefined; max?: number | undefined; search?: string | undefined }): Promise<GroupSummary[]> {
    await this.prepareRealm(options.realm);
    return this.adminClient.groups.find(this.buildListQuery(options));
  }

  async listRealmRoles(options: { realm?: string | undefined; first?: number | undefined; max?: number | undefined; search?: string | undefined }): Promise<RoleSummary[]> {
    await this.prepareRealm(options.realm);
    return this.adminClient.roles.find(this.buildListQuery(options));
  }

  async createUser(input: CreateUserInput): Promise<{ realm: string; userId?: string }> {
    const realm = await this.prepareRealm(input.realm);
    const createdUser = await this.adminClient.users.create(this.buildCreateUserPayload(input, realm));

    return createdUser.id === undefined ? { realm } : { realm, userId: createdUser.id };
  }

  async addUserToGroup(input: { realm?: string | undefined; userId: string; groupId: string }): Promise<{ realm: string; userId: string; groupId: string }> {
    const realm = await this.prepareRealm(input.realm);
    await this.adminClient.users.addToGroup({
      id: input.userId,
      groupId: input.groupId,
    });

    return {
      realm,
      userId: input.userId,
      groupId: input.groupId,
    };
  }

  async assignRealmRoles(input: { realm?: string | undefined; userId: string; roleNames: string[] }): Promise<{ realm: string; userId: string; assignedRoles: string[] }> {
    const realm = await this.prepareRealm(input.realm);
    const resolvedRoles: RoleMapping[] = [];

    for (const roleName of input.roleNames) {
      const role = await this.adminClient.roles.findOneByName({ name: roleName });

      if (role?.id === undefined || role.name === undefined) {
        throw new Error(`Realm role not found: ${roleName}`);
      }

      const resolvedRole: RoleMapping = { id: role.id, name: role.name };
      if (role.description !== undefined) {
        resolvedRole.description = role.description;
      }

      resolvedRoles.push(resolvedRole);
    }

    await this.adminClient.users.addRealmRoleMappings({
      id: input.userId,
      roles: resolvedRoles,
    });

    return {
      realm,
      userId: input.userId,
      assignedRoles: resolvedRoles.map((role) => role.name ?? 'unknown-role'),
    };
  }
}
