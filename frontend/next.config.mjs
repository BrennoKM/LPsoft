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

// O corte físico de features no frontend (remover o código do bundle de um
// cliente) é responsabilidade do build.sh, que substitui src/features/<não
// contratada> antes do `npm build` — espelhando o backend (Maven não compila
// o módulo) e o WS-GiftCards (build copia só o contratado).
//
// Em dev / build sem cliente, todas as features estão presentes; hasFeature()
// (NEXT_PUBLIC_FEATURES) controla rota/menu em runtime. Alias de bundler NÃO é
// usado: Turbopack resolve os path-aliases do tsconfig (@/*) com precedência,
// tornando resolveAlias para @/features/* não confiável.

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
