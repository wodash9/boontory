import { z } from 'zod';

import { parseEnv, nonEmptyString, urlString } from '../shared/env.js';
import { HttpError } from '../shared/errors.js';

const coolifyEnvSchema = z.object({
  COOLIFY_BASE_URL: urlString,
  COOLIFY_TOKEN: nonEmptyString,
});

export type CoolifyEnv = z.infer<typeof coolifyEnvSchema>;

export function loadCoolifyEnv(source: NodeJS.ProcessEnv = process.env): CoolifyEnv {
  return parseEnv(coolifyEnvSchema, source);
}

export type FetchLike = (input: string | URL, init?: RequestInit) => Promise<Response>;

function parseJsonOrText(body: string): unknown {
  if (body.trim().length === 0) {
    return null;
  }

  try {
    const parsed: unknown = JSON.parse(body);
    return parsed;
  } catch {
    return body;
  }
}

function normalizeBaseUrl(baseUrl: string): string {
  return baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
}

export class CoolifyClient {
  private readonly apiBaseUrl: string;
  private readonly token: string;
  private readonly fetchLike: FetchLike;

  constructor(env: CoolifyEnv, fetchLike: FetchLike = fetch) {
    this.apiBaseUrl = `${normalizeBaseUrl(env.COOLIFY_BASE_URL)}/api/v1`;
    this.token = env.COOLIFY_TOKEN;
    this.fetchLike = fetchLike;
  }

  private async request(path: string, method: 'GET' | 'POST'): Promise<unknown> {
    const response = await this.fetchLike(`${this.apiBaseUrl}/${path}`, {
      method,
      headers: {
        Accept: 'application/json',
        Authorization: `Bearer ${this.token}`,
      },
    });

    const responseBody = await response.text();

    if (!response.ok) {
      throw new HttpError(response.status, response.statusText, responseBody);
    }

    return parseJsonOrText(responseBody);
  }

  async listApplications(): Promise<unknown[]> {
    const result = await this.request('applications', 'GET');

    if (!Array.isArray(result)) {
      throw new Error('Coolify returned a non-list response for applications.');
    }

    return result;
  }

  async getApplication(uuid: string): Promise<unknown> {
    return this.request(`applications/${uuid}`, 'GET');
  }

  async startApplication(uuid: string): Promise<unknown> {
    return this.request(`applications/${uuid}/start`, 'GET');
  }

  async stopApplication(uuid: string): Promise<unknown> {
    return this.request(`applications/${uuid}/stop`, 'GET');
  }

  async restartApplication(uuid: string): Promise<unknown> {
    return this.request(`applications/${uuid}/restart`, 'GET');
  }

  async listServices(): Promise<unknown[]> {
    const result = await this.request('services', 'GET');

    if (!Array.isArray(result)) {
      throw new Error('Coolify returned a non-list response for services.');
    }

    return result;
  }

  async getService(uuid: string): Promise<unknown> {
    return this.request(`services/${uuid}`, 'GET');
  }

  async startService(uuid: string): Promise<unknown> {
    return this.request(`services/${uuid}/start`, 'POST');
  }

  async stopService(uuid: string): Promise<unknown> {
    return this.request(`services/${uuid}/stop`, 'POST');
  }

  async restartService(uuid: string): Promise<unknown> {
    return this.request(`services/${uuid}/restart`, 'POST');
  }
}
