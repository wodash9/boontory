function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function getString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim().length > 0 ? value : undefined;
}

function getPreferredLabel(record: Record<string, unknown>): string {
  const primary = getString(record.name)
    ?? getString(record.username)
    ?? getString(record.email)
    ?? getString(record.realm)
    ?? getString(record.id)
    ?? getString(record.uuid)
    ?? 'unnamed';

  const identifier = getString(record.uuid) ?? getString(record.id);
  const status = getString(record.status);

  const extras: string[] = [];
  if (identifier !== undefined && identifier !== primary) {
    extras.push(identifier);
  }
  if (status !== undefined) {
    extras.push(`status=${status}`);
  }

  return extras.length > 0 ? `${primary} (${extras.join(', ')})` : primary;
}

export function formatDetails(title: string, value: unknown): string {
  const body = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
  return `${title}\n\n${body}`;
}

export function formatCollection(title: string, values: unknown[]): string {
  if (values.length === 0) {
    return `${title}\n\nNo results.`;
  }

  const lines = values.map((value, index) => {
    if (isRecord(value)) {
      return `${index + 1}. ${getPreferredLabel(value)}`;
    }

    return `${index + 1}. ${String(value)}`;
  });

  return `${title}\n\n${lines.join('\n')}`;
}
