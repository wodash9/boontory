import { describe, expect, it, vi } from 'vitest';

import { CoolifyClient, type CoolifyEnv, type FetchLike } from '../src/coolify/client.js';
import { COOLIFY_TOOL_NAMES, createCoolifyToolDefinitions } from '../src/coolify/server.js';

function createEnv(): CoolifyEnv {
  return {
    COOLIFY_BASE_URL: 'https://coolify.example.com',
    COOLIFY_TOKEN: 'token-123',
  };
}

describe('Coolify client', () => {
  it('uses GET /api/v1/applications with Bearer auth', async () => {
    const fetchLike = vi.fn<FetchLike>().mockResolvedValue(
      new Response(JSON.stringify([{ uuid: 'app-1', name: 'Books' }]), { status: 200 }),
    );
    const client = new CoolifyClient(createEnv(), fetchLike);

    const applications = await client.listApplications();

    expect(applications).toHaveLength(1);
    expect(fetchLike).toHaveBeenCalledWith(
      'https://coolify.example.com/api/v1/applications',
      expect.objectContaining({
        method: 'GET',
        headers: expect.objectContaining({ Authorization: 'Bearer token-123' }),
      }),
    );
  });

  it('uses POST for service lifecycle routes', async () => {
    const fetchLike = vi.fn<FetchLike>().mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), { status: 200 }),
    );
    const client = new CoolifyClient(createEnv(), fetchLike);

    await client.restartService('svc-1');

    expect(fetchLike).toHaveBeenCalledWith(
      'https://coolify.example.com/api/v1/services/svc-1/restart',
      expect.objectContaining({ method: 'POST' }),
    );
  });
});

describe('Coolify tool registration', () => {
  it('registers the full Coolify tool surface', () => {
    const client = new CoolifyClient(
      createEnv(),
      async () => new Response(JSON.stringify([]), { status: 200 }),
    );

    const tools = createCoolifyToolDefinitions(client);

    expect(tools.map((tool) => tool.name).sort()).toEqual([...COOLIFY_TOOL_NAMES].sort());
  });

  it('returns human-readable text for list and get tools', async () => {
    const fetchLike = vi.fn<FetchLike>().mockImplementation(async (input) => {
      const url = String(input);

      if (url.endsWith('/applications')) {
        return new Response(JSON.stringify([{ uuid: 'app-1', name: 'Books', status: 'running' }]), { status: 200 });
      }

      if (url.endsWith('/applications/app-1')) {
        return new Response(JSON.stringify({ uuid: 'app-1', name: 'Books' }), { status: 200 });
      }

      return new Response(JSON.stringify([]), { status: 200 });
    });
    const client = new CoolifyClient(createEnv(), fetchLike);
    const tools = createCoolifyToolDefinitions(client);
    const listTool = tools.find((tool) => tool.name === 'coolify_list_applications');
    const getTool = tools.find((tool) => tool.name === 'coolify_get_application');

    const listResult = await listTool?.execute({});
    const getResult = await getTool?.execute({ uuid: 'app-1' });

    expect(listResult?.content[0]?.text).toContain('Coolify applications');
    expect(listResult?.content[0]?.text).toContain('Books');
    expect(getResult?.content[0]?.text).toContain('Coolify application app-1');
  });
});
