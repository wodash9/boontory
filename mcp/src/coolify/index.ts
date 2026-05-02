import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';

import { CoolifyClient, loadCoolifyEnv } from './client.js';
import { createCoolifyServer } from './server.js';
import { logToStderr } from '../shared/logging.js';

async function main(): Promise<void> {
  const env = loadCoolifyEnv();
  const client = new CoolifyClient(env);
  const server = createCoolifyServer(client);
  const transport = new StdioServerTransport();

  await server.connect(transport);
  logToStderr('Coolify MCP server running on stdio.');
}

main().catch((error: unknown) => {
  logToStderr('Fatal error in Coolify MCP server.', error);
  process.exit(1);
});
