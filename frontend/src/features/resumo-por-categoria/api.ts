import { api } from '@/lib/http';

export type ResumoItem = {
  categoriaId: string;
  nome: string;
  cor: string | null;
  totalEventos: number;
};

export async function obterResumoPorCategoria(): Promise<ResumoItem[]> {
  const { data } = await api.get<ResumoItem[]>('/resumo-categoria');
  return data;
}
