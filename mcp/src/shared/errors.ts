export class HttpError extends Error {
  readonly status: number;
  readonly responseBody: string;

  constructor(status: number, statusText: string, responseBody: string) {
    super(`HTTP ${status} ${statusText}`);
    this.name = 'HttpError';
    this.status = status;
    this.responseBody = responseBody;
  }
}

function stringifyUnknown(value: unknown): string {
  if (typeof value === 'string') {
    return value;
  }

  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return 'Unknown error';
  }
}

export function mapError(error: unknown, context?: string): string {
  let message = 'Unknown error';

  if (error instanceof HttpError) {
    const body = error.responseBody.trim();
    message = body.length > 0 ? `${error.message}: ${body}` : error.message;
  } else if (error instanceof Error) {
    message = error.message;
  } else {
    message = stringifyUnknown(error);
  }

  return context ? `${context}: ${message}` : message;
}
