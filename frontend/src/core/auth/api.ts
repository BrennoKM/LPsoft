import { api } from '@/lib/http';
import type { LoginInput, RegistrarInput } from './schemas';

export type TokenResponse = { token: string; usuarioId: string; email: string };

export async function registrar(input: RegistrarInput): Promise<{ usuarioId: string }> {
  const { data } = await api.post<{ usuarioId: string }>('/auth/registrar', input);
  return data;
}

export async function login(input: LoginInput): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/auth/login', input);
  return data;
}
