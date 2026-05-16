const KEY_TOKEN = 'lpsoft.token';
const KEY_USUARIO = 'lpsoft.usuario';

export type UsuarioAtual = { id: string; email: string };

export function setSession(token: string, usuario: UsuarioAtual): void {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(KEY_TOKEN, token);
  window.localStorage.setItem(KEY_USUARIO, JSON.stringify(usuario));
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return window.localStorage.getItem(KEY_TOKEN);
}

export function getUsuario(): UsuarioAtual | null {
  if (typeof window === 'undefined') return null;
  const raw = window.localStorage.getItem(KEY_USUARIO);
  if (!raw) return null;
  try { return JSON.parse(raw) as UsuarioAtual; } catch { return null; }
}

export function clearSession(): void {
  if (typeof window === 'undefined') return;
  window.localStorage.removeItem(KEY_TOKEN);
  window.localStorage.removeItem(KEY_USUARIO);
}
