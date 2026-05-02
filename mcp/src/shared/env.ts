import { z } from 'zod';

export const nonEmptyString = z.string().trim().min(1);
export const urlString = z.string().trim().url();

export function parseEnv<Shape extends z.ZodRawShape>(
  schema: z.ZodObject<Shape>,
  source: NodeJS.ProcessEnv = process.env,
): z.infer<z.ZodObject<Shape>> {
  const result = schema.safeParse(source);

  if (result.success) {
    return result.data;
  }

  const details = result.error.issues
    .map((issue) => `${issue.path.join('.') || 'env'}: ${issue.message}`)
    .join('; ');

  throw new Error(`Invalid environment configuration: ${details}`);
}
