/**
 * Lê flags expostas pelo build (next.config.mjs → env.NEXT_PUBLIC_FEATURES).
 */
export type FeatureFlags = Record<string, boolean>;

const raw = process.env.NEXT_PUBLIC_FEATURES ?? '{}';

let parsed: FeatureFlags = {};
try {
  parsed = JSON.parse(raw);
} catch {
  parsed = {};
}

export const features: FeatureFlags = parsed;

export function hasFeature(name: string): boolean {
  return features[name] === true;
}

export const clientSlug = process.env.NEXT_PUBLIC_CLIENT ?? 'unknown';
export const clientDisplayName = process.env.NEXT_PUBLIC_DISPLAY_NAME ?? clientSlug;
