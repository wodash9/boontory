import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { z } from 'zod';

import { KeycloakService } from './client.js';
import { mapError } from '../shared/errors.js';
import { formatCollection, formatDetails } from '../shared/format.js';
import { defineTool, requireConfirm, textResponse } from '../shared/mcp.js';
import type { TextToolResult, ToolDefinition } from '../shared/mcp.js';

const emptyInputSchema = z.object({});
const listSchema = z.object({
  realm: z.string().trim().min(1).optional(),
  search: z.string().trim().min(1).optional(),
  first: z.number().int().nonnegative().optional(),
  max: z.number().int().positive().optional(),
});
const createUserSchema = z.object({
  realm: z.string().trim().min(1).optional(),
  username: z.string().trim().min(1),
  email: z.string().trim().email().optional(),
  firstName: z.string().trim().min(1).optional(),
  lastName: z.string().trim().min(1).optional(),
  enabled: z.boolean().optional(),
  emailVerified: z.boolean().optional(),
  confirm: z.boolean(),
});
const addUserToGroupSchema = z.object({
  realm: z.string().trim().min(1).optional(),
  userId: z.string().trim().min(1),
  groupId: z.string().trim().min(1),
  confirm: z.boolean(),
});
const assignRealmRolesSchema = z.object({
  realm: z.string().trim().min(1).optional(),
  userId: z.string().trim().min(1),
  roleNames: z.array(z.string().trim().min(1)).min(1),
  confirm: z.boolean(),
});

export const KEYCLOAK_TOOL_NAMES = [
  'keycloak_list_realms',
  'keycloak_list_users',
  'keycloak_list_groups',
  'keycloak_list_realm_roles',
  'keycloak_create_user',
  'keycloak_add_user_to_group',
  'keycloak_assign_realm_roles',
] as const;

async function runTool(action: () => Promise<string>): Promise<TextToolResult> {
  try {
    return textResponse(await action());
  } catch (error) {
    return textResponse(mapError(error), true);
  }
}

export function createKeycloakToolDefinitions(service: KeycloakService): ToolDefinition[] {
  const definitions: ToolDefinition[] = [];

  definitions.push(defineTool(
    'keycloak_list_realms',
    {
      title: 'List Keycloak realms',
      description: 'List realms available through the configured Keycloak admin client.',
      inputSchema: emptyInputSchema,
      annotations: { readOnlyHint: true },
    },
    async () => runTool(async () => formatCollection('Keycloak realms', await service.listRealms())),
  ));

  definitions.push(defineTool(
    'keycloak_list_users',
    {
      title: 'List Keycloak users',
      description: 'List users in a target Keycloak realm.',
      inputSchema: listSchema,
      annotations: { readOnlyHint: true },
    },
    async (args) => runTool(async () => formatCollection('Keycloak users', await service.listUsers(args))),
  ));

  definitions.push(defineTool(
    'keycloak_list_groups',
    {
      title: 'List Keycloak groups',
      description: 'List groups in a target Keycloak realm.',
      inputSchema: listSchema,
      annotations: { readOnlyHint: true },
    },
    async (args) => runTool(async () => formatCollection('Keycloak groups', await service.listGroups(args))),
  ));

  definitions.push(defineTool(
    'keycloak_list_realm_roles',
    {
      title: 'List Keycloak realm roles',
      description: 'List realm roles in a target Keycloak realm.',
      inputSchema: listSchema,
      annotations: { readOnlyHint: true },
    },
    async (args) => runTool(async () => formatCollection('Keycloak realm roles', await service.listRealmRoles(args))),
  ));

  definitions.push(defineTool(
    'keycloak_create_user',
    {
      title: 'Create Keycloak user',
      description: 'Create a user in the target Keycloak realm. Requires confirm:true.',
      inputSchema: createUserSchema,
    },
    async ({ confirm, ...args }) => runTool(async () => {
      requireConfirm(confirm, 'create a Keycloak user');
      return formatDetails('Created Keycloak user', await service.createUser(args));
    }),
  ));

  definitions.push(defineTool(
    'keycloak_add_user_to_group',
    {
      title: 'Add Keycloak user to group',
      description: 'Add a user to a Keycloak group. Requires confirm:true.',
      inputSchema: addUserToGroupSchema,
    },
    async ({ confirm, ...args }) => runTool(async () => {
      requireConfirm(confirm, 'add a Keycloak user to a group');
      return formatDetails('Added Keycloak user to group', await service.addUserToGroup(args));
    }),
  ));

  definitions.push(defineTool(
    'keycloak_assign_realm_roles',
    {
      title: 'Assign Keycloak realm roles',
      description: 'Assign realm roles to a Keycloak user. Requires confirm:true.',
      inputSchema: assignRealmRolesSchema,
    },
    async ({ confirm, ...args }) => runTool(async () => {
      requireConfirm(confirm, 'assign Keycloak realm roles');
      return formatDetails('Assigned Keycloak realm roles', await service.assignRealmRoles(args));
    }),
  ));

  return definitions;
}

export function createKeycloakServer(service: KeycloakService): McpServer {
  const server = new McpServer(
    {
      name: 'boontory-keycloak-mcp',
      version: '0.1.0',
    },
    {
      instructions: 'Use these tools to inspect Keycloak and perform confirmed user-management actions.',
    },
  );

  for (const tool of createKeycloakToolDefinitions(service)) {
    server.registerTool(tool.name, tool.config, tool.execute);
  }

  return server;
}
