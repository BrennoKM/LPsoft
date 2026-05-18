import { describe, it, expect, afterEach, vi } from 'vitest';

// feature-flags lê process.env.NEXT_PUBLIC_FEATURES no import (const top-level).
// Cada caso: stubEnv + resetModules + import dinâmico.
async function loadWith(value: string | undefined) {
  vi.resetModules();
  if (value === undefined) {
    vi.stubEnv('NEXT_PUBLIC_FEATURES', '');
  } else {
    vi.stubEnv('NEXT_PUBLIC_FEATURES', value);
  }
  return import('./feature-flags');
}

afterEach(() => {
  vi.unstubAllEnvs();
});

describe('feature-flags (o corte por cliente)', () => {
  it('JSON válido expõe as flags', async () => {
    const { hasFeature } = await loadWith('{"categorias":true,"analytics":false}');
    expect(hasFeature('categorias')).toBe(true);
    expect(hasFeature('analytics')).toBe(false);
  });

  it('NEXT_PUBLIC_FEATURES ausente/vazio → nenhuma feature', async () => {
    const { hasFeature } = await loadWith(undefined);
    expect(hasFeature('categorias')).toBe(false);
  });

  it('JSON inválido → cai em {} (catch), sem lançar', async () => {
    const { hasFeature } = await loadWith('{nao-json');
    expect(hasFeature('categorias')).toBe(false);
  });

  it('hasFeature exige === true ("true" string não conta)', async () => {
    const { hasFeature } = await loadWith('{"categorias":"true"}');
    expect(hasFeature('categorias')).toBe(false);
  });

  it('feature ausente do mapa → false', async () => {
    const { hasFeature } = await loadWith('{"categorias":true}');
    expect(hasFeature('inexistente')).toBe(false);
  });
});
