export function logToStderr(message: string, details?: unknown): void {
  const prefix = `[boontory-mcp] ${message}`;

  if (details === undefined) {
    console.error(prefix);
    return;
  }

  console.error(prefix, details);
}
