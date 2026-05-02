import { describe, expect, it, vi } from 'vitest';
import { z } from 'zod';

import { parseEnv } from '../src/shared/env.js';
import { HttpError, mapError } from '../src/shared/errors.js';
import { logToStderr } from '../src/shared/logging.js';
import { requireConfirm, textResponse } from '../src/shared/mcp.js';

describe('shared helpers', () => {
  it('parses environment values with zod schemas', () => {
    const env = parseEnv(
      z.object({
        TOKEN: z.string().min(1),
      }),
      { TOKEN: 'secret' },
    );

    expect(env.TOKEN).toBe('secret');
  });

  it('throws a readable error for invalid environment values', () => {
    expect(() =>
      parseEnv(
        z.object({
          TOKEN: z.string().min(1),
        }),
        { TOKEN: '' },
      ),
    ).toThrow(/Invalid environment configuration/);
  });

  it('creates text MCP responses', () => {
    expect(textResponse('hello world')).toEqual({
      content: [{ type: 'text', text: 'hello world' }],
    });
    expect(textResponse('broken', true)).toEqual({
      content: [{ type: 'text', text: 'broken' }],
      isError: true,
    });
  });

  it('requires confirmation for protected actions', () => {
    expect(() => requireConfirm(false, 'do a thing')).toThrow(/confirm:true/);
    expect(() => requireConfirm(true, 'do a thing')).not.toThrow();
  });

  it('maps HTTP errors to readable messages', () => {
    const error = new HttpError(401, 'Unauthorized', '{"message":"bad token"}');

    expect(mapError(error, 'Coolify request failed')).toContain('401');
    expect(mapError(error, 'Coolify request failed')).toContain('bad token');
  });

  it('logs to stderr only', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    logToStderr('hello');
    expect(spy).toHaveBeenCalledWith('[boontory-mcp] hello');
    spy.mockRestore();
  });
});
