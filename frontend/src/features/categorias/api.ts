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

export async function listarCategoriasDoEvento(eventoId: string): Promise<Categoria[]> {
  const { data } = await api.get<Categoria[]>(`/eventos/${eventoId}/categorias`);
  return data;
}

export async function atribuirCategoria(eventoId: string, categoriaId: string): Promise<void> {
  await api.post(`/eventos/${eventoId}/categorias`, { categoriaId });
}

export async function removerCategoria(eventoId: string, categoriaId: string): Promise<void> {
  await api.delete(`/eventos/${eventoId}/categorias/${categoriaId}`);
}
