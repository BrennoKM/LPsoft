import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import yaml from 'yaml';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const client = process.env.CLIENT ?? 'enterprise';
const manifestPath = path.resolve(__dirname, `../clients/${client}.yml`);

let manifest;
try {
  manifest = yaml.parse(fs.readFileSync(manifestPath, 'utf8'));
} catch (err) {
  throw new Error(
    `Não foi possível carregar manifesto do cliente "${client}" em ${manifestPath}.\n` +
      `Defina CLIENT=<slug> e crie clients/<slug>.yml.\nErro original: ${err.message}`,
  );
}

const featureFlags = manifest.features ?? {};
const emptyFeaturePath = path.resolve(__dirname, './src/core/shared/empty-feature.ts');

const disabledAliases = Object.fromEntries(
  Object.entries(featureFlags)
    .filter(([, enabled]) => !enabled)
    .map(([name]) => [`@/features/${name}`, emptyFeaturePath]),
);

/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  turbopack: {
    resolveAlias: disabledAliases,
  },
  webpack: (config) => {
    config.resolve.alias = { ...config.resolve.alias, ...disabledAliases };
    return config;
  },
  env: {
    NEXT_PUBLIC_CLIENT: manifest.client ?? client,
    NEXT_PUBLIC_DISPLAY_NAME: manifest.displayName ?? client,
    NEXT_PUBLIC_FEATURES: JSON.stringify(featureFlags),
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8130/api/v1',
  },
};

export default nextConfig;
