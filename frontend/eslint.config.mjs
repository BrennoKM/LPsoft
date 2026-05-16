import { defineConfig } from 'eslint/config';
import nextVitals from 'eslint-config-next/core-web-vitals';
import nextTs from 'eslint-config-next/typescript';
import prettier from 'eslint-config-prettier';

const guardCoreFromFeatures = {
  files: ['src/core/**/*.{ts,tsx}', 'src/lib/**/*.{ts,tsx}', 'src/components/**/*.{ts,tsx}'],
  rules: {
    'no-restricted-imports': [
      'error',
      {
        patterns: [
          {
            group: ['@/features/*', '../features/*', '../../features/*', '../../../features/*'],
            message:
              'Código base (core/lib/components) não pode importar de features. Inverta a dependência via evento ou hook compartilhado.',
          },
        ],
      },
    ],
  },
};

export default defineConfig([
  ...nextVitals,
  ...nextTs,
  guardCoreFromFeatures,
  {
    // Exceção única: o composition root É o ponto de montagem (análogo ao
    // component scan / classpath do Maven). Ele PODE referenciar features;
    // o build.sh corta as linhas das não contratadas. Nenhum outro arquivo
    // de core tem essa permissão.
    files: ['src/core/shared/features.ts'],
    rules: { 'no-restricted-imports': 'off' },
  },
  prettier,
  {
    rules: {
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-unused-vars': [
        'warn',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
      // Regras do React Compiler (Next 16) — manter como warning para não bloquear builds da PoC
      'react-hooks/set-state-in-effect': 'warn',
      'react-hooks/exhaustive-deps': 'warn',
      // falso-positivo conhecido: react-hook-form handleSubmit(onSubmit) onde
      // onSubmit só lê o ref no submit (handler), não no render
      'react-hooks/refs': 'warn',
    },
  },
  {
    ignores: ['.next/**', 'node_modules/**', 'next-env.d.ts'],
  },
]);
