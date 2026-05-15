import axios, { AxiosError } from 'axios';
import { clearSession, getToken } from '@/core/auth/storage';

const baseURL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8130/api/v1';

export const api = axios.create({ baseURL });

api.interceptors.request.use((config) => {
  const token = typeof window !== 'undefined' ? getToken() : null;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      clearSession();
    }
    return Promise.reject(error);
  },
);

export function extractMessage(error: unknown, fallback: string): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as { erro?: string } | undefined;
    if (data?.erro) return data.erro;
    return error.message ?? fallback;
  }
  return fallback;
}
