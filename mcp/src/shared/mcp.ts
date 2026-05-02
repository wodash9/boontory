import { z } from 'zod';

export type TextToolResult = {
  content: Array<{
    type: 'text';
    text: string;
  }>;
  isError?: boolean;
};

export type ToolConfig<Schema extends z.ZodTypeAny> = {
  title: string;
  description: string;
  inputSchema: Schema;
  annotations?: {
    readOnlyHint?: boolean;
    destructiveHint?: boolean;
    idempotentHint?: boolean;
  };
};

export type ToolDefinition = {
  name: string;
  config: ToolConfig<z.ZodTypeAny>;
  execute: (args: unknown) => Promise<TextToolResult>;
};

export function defineTool<Schema extends z.ZodTypeAny>(
  name: string,
  config: ToolConfig<Schema>,
  execute: (args: z.infer<Schema>) => Promise<TextToolResult>,
): ToolDefinition {
  return {
    name,
    config,
    execute: (args: unknown) => execute(config.inputSchema.parse(args)),
  };
}

export function textResponse(text: string, isError = false): TextToolResult {
  return isError
    ? { content: [{ type: 'text', text }], isError: true }
    : { content: [{ type: 'text', text }] };
}

export function requireConfirm(confirm: boolean | undefined, action: string): void {
  if (confirm !== true) {
    throw new Error(`Confirmation required to ${action}. Re-run with confirm:true.`);
  }
}
