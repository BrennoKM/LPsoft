import { describe, it, expect, beforeEach } from 'vitest';
import { setSession, getToken, getUsuario, clearSession } from './storage';

beforeEach(() => {
  window.localStorage.clear();
});

describe('auth/storage', () => {
  it('setSession grava; getToken/getUsuario lêem de volta', () => {
    setSession('tok-123', { id: 'u1', email: 'a@b.com' });
    expect(getToken()).toBe('tok-123');
    expect(getUsuario()).toEqual({ id: 'u1', email: 'a@b.com' });
  });

  it('sem sessão → getToken/getUsuario retornam null', () => {
    expect(getToken()).toBeNull();
    expect(getUsuario()).toBeNull();
  });

  it('usuario com JSON corrompido → null (catch)', () => {
    window.localStorage.setItem('lpsoft.usuario', '{nao-json');
    expect(getUsuario()).toBeNull();
  });

  it('clearSession remove token e usuario', () => {
    setSession('tok', { id: 'u', email: 'e' });
    clearSession();
    expect(getToken()).toBeNull();
    expect(getUsuario()).toBeNull();
  });
});
