import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import yaml from 'yaml';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const client = process.env.CLIENT ?? process.env.NEXT_PUBLIC_CLIENT ?? 'enterprise';
const manifestPath = path.resolve(__dirname, `../clients/${client}.yml`);

// O manifesto é input INTERNO da tooling de composição (build.sh). Em dev e no
// modo binary o build roda no repo e o lê diretamente. No modo source o
// artefato é self-contained e NÃO leva nenhum manifesto — as decisões já vêm
// projetadas em env (CLIENT/DISPLAY_NAME/NEXT_PUBLIC_FEATURES), então aqui
// caímos no fallback por env. Nunca lançar erro por ausência de manifesto.
let manifest;
try {
  manifest = yaml.parse(fs.readFileSync(manifestPath, 'utf8'));
} catch {
  let features = {};
  try {
    features = JSON.parse(process.env.NEXT_PUBLIC_FEATURES ?? '{}');
  } catch {
    features = {};
  }
  manifest = {
    client,
    displayName: process.env.DISPLAY_NAME ?? process.env.NEXT_PUBLIC_DISPLAY_NAME ?? client,
    features,
  };
}

const featureFlags = manifest.features ?? {};

/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  env: {
    NEXT_PUBLIC_CLIENT: manifest.client ?? client,
    NEXT_PUBLIC_DISPLAY_NAME: manifest.displayName ?? client,
    NEXT_PUBLIC_FEATURES: JSON.stringify(featureFlags),
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8130/api/v1',
  },
};

export default nextConfig;
