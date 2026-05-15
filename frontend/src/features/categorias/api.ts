import { api } from '@/lib/http';

export type Categoria = {
  id: string;
  nome: string;
  cor: string | null;
  criadoEm: string;
};

export type CriarCategoriaInput = { nome: string; cor?: string };

export async function listarCategorias(): Promise<Categoria[]> {
  const { data } = await api.get<Categoria[]>('/categorias');
  return data;
}

export async function criarCategoria(input: CriarCategoriaInput): Promise<Categoria> {
  const { data } = await api.post<Categoria>('/categorias', input);
  return data;
}

export async function deletarCategoria(id: string): Promise<void> {
  await api.delete(`/categorias/${id}`);
}
