import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';

import { createKeycloakAdminClient, KeycloakService, loadKeycloakEnv } from './client.js';
import { createKeycloakServer } from './server.js';
import { logToStderr } from '../shared/logging.js';

async function main(): Promise<void> {
  const env = loadKeycloakEnv();
  const adminClient = createKeycloakAdminClient(env);
  const service = new KeycloakService(adminClient, env);
  const server = createKeycloakServer(service);
  const transport = new StdioServerTransport();

  await server.connect(transport);
  logToStderr('Keycloak MCP server running on stdio.');
}

main().catch((error: unknown) => {
  logToStderr('Fatal error in Keycloak MCP server.', error);
  process.exit(1);
});
