import { describe, it, expect } from 'vitest';
import { AxiosError } from 'axios';
import { extractMessage } from './http';

describe('extractMessage', () => {
  it('AxiosError com response.data.erro → mensagem do backend', () => {
    const e = new AxiosError('falha http');
    e.response = { data: { erro: 'antecedência inválida' } } as never;
    expect(extractMessage(e, 'fallback')).toBe('antecedência inválida');
  });

  it('AxiosError sem data.erro → error.message', () => {
    const e = new AxiosError('timeout de rede');
    expect(extractMessage(e, 'fallback')).toBe('timeout de rede');
  });

  it('erro não-Axios (Error comum) → fallback', () => {
    expect(extractMessage(new Error('qualquer'), 'fallback')).toBe('fallback');
  });

  it('valor arbitrário (string) → fallback', () => {
    expect(extractMessage('xyz', 'fallback')).toBe('fallback');
  });
});
