import { api } from '@/lib/http';

export type PorUsuario = { criadoPor: string; total: number };
export type PorDia = { dia: string; total: number };

export type Resumo = {
  total: number;
  porUsuario: PorUsuario[];
  porDia: PorDia[];
};

export async function obterResumo(): Promise<Resumo> {
  const { data } = await api.get<Resumo>('/analytics/resumo');
  return data;
}
