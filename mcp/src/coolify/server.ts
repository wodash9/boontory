import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { z } from 'zod';

import { CoolifyClient } from './client.js';
import { mapError } from '../shared/errors.js';
import { formatCollection, formatDetails } from '../shared/format.js';
import { defineTool, textResponse } from '../shared/mcp.js';
import type { TextToolResult, ToolDefinition } from '../shared/mcp.js';

const emptyInputSchema = z.object({});
const uuidSchema = z.object({
  uuid: z.string().trim().min(1),
});

export const COOLIFY_TOOL_NAMES = [
  'coolify_list_applications',
  'coolify_get_application',
  'coolify_start_application',
  'coolify_stop_application',
  'coolify_restart_application',
  'coolify_list_services',
  'coolify_get_service',
  'coolify_start_service',
  'coolify_stop_service',
  'coolify_restart_service',
] as const;

type CoolifyToolName = typeof COOLIFY_TOOL_NAMES[number];

async function runTool(action: () => Promise<string>): Promise<TextToolResult> {
  try {
    return textResponse(await action());
  } catch (error) {
    return textResponse(mapError(error), true);
  }
}

function formatActionResult(label: string, uuid: string, response: unknown): string {
  return formatDetails(`${label} ${uuid}`, response);
}

export function createCoolifyToolDefinitions(client: CoolifyClient): ToolDefinition[] {
  const definitions: ToolDefinition[] = [];

  definitions.push(defineTool(
    'coolify_list_applications',
    {
      title: 'List Coolify applications',
      description: 'List applications from Coolify using GET /applications.',
      inputSchema: emptyInputSchema,
      annotations: { readOnlyHint: true },
    },
    async () => runTool(async () => formatCollection('Coolify applications', await client.listApplications())),
  ));

  definitions.push(defineTool(
    'coolify_get_application',
    {
      title: 'Get Coolify application',
      description: 'Fetch one Coolify application by UUID.',
      inputSchema: uuidSchema,
      annotations: { readOnlyHint: true },
    },
    async ({ uuid }) => runTool(async () => formatDetails(`Coolify application ${uuid}`, await client.getApplication(uuid))),
  ));

  const applicationActions: ReadonlyArray<[CoolifyToolName, string, (uuid: string) => Promise<unknown>]> = [
    ['coolify_start_application', 'Started application', (uuid) => client.startApplication(uuid)],
    ['coolify_stop_application', 'Stopped application', (uuid) => client.stopApplication(uuid)],
    ['coolify_restart_application', 'Restarted application', (uuid) => client.restartApplication(uuid)],
  ];

  for (const [name, label, action] of applicationActions) {
    definitions.push(defineTool(
      name,
      {
        title: label,
        description: `${label} through the official Coolify API route.`,
        inputSchema: uuidSchema,
      },
      async ({ uuid }) => runTool(async () => formatActionResult(label, uuid, await action(uuid))),
    ));
  }

  definitions.push(defineTool(
    'coolify_list_services',
    {
      title: 'List Coolify services',
      description: 'List services from Coolify using GET /services.',
      inputSchema: emptyInputSchema,
      annotations: { readOnlyHint: true },
    },
    async () => runTool(async () => formatCollection('Coolify services', await client.listServices())),
  ));

  definitions.push(defineTool(
    'coolify_get_service',
    {
      title: 'Get Coolify service',
      description: 'Fetch one Coolify service by UUID.',
      inputSchema: uuidSchema,
      annotations: { readOnlyHint: true },
    },
    async ({ uuid }) => runTool(async () => formatDetails(`Coolify service ${uuid}`, await client.getService(uuid))),
  ));

  const serviceActions: ReadonlyArray<[CoolifyToolName, string, (uuid: string) => Promise<unknown>]> = [
    ['coolify_start_service', 'Started service', (uuid) => client.startService(uuid)],
    ['coolify_stop_service', 'Stopped service', (uuid) => client.stopService(uuid)],
    ['coolify_restart_service', 'Restarted service', (uuid) => client.restartService(uuid)],
  ];

  for (const [name, label, action] of serviceActions) {
    definitions.push(defineTool(
      name,
      {
        title: label,
        description: `${label} through the official Coolify API route.`,
        inputSchema: uuidSchema,
      },
      async ({ uuid }) => runTool(async () => formatActionResult(label, uuid, await action(uuid))),
    ));
  }

  return definitions;
}

export function createCoolifyServer(client: CoolifyClient): McpServer {
  const server = new McpServer(
    {
      name: 'boontory-coolify-mcp',
      version: '0.1.0',
    },
    {
      instructions: 'Use these tools to inspect and control Coolify applications and services.',
    },
  );

  for (const tool of createCoolifyToolDefinitions(client)) {
    server.registerTool(tool.name, tool.config, tool.execute);
  }

  return server;
}
