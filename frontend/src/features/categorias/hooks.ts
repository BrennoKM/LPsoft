import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  atribuirCategoria,
  criarCategoria,
  deletarCategoria,
  listarCategorias,
  listarCategoriasDoEvento,
  removerCategoria,
  type Categoria,
} from './api';

const KEY = ['categorias'] as const;
const eventoKey = (eventoId: string) => ['categorias', 'evento', eventoId] as const;

export function useCategorias() {
  return useQuery<Categoria[]>({ queryKey: KEY, queryFn: listarCategorias });
}

export function useCriarCategoria() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: criarCategoria,
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useDeletarCategoria() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deletarCategoria,
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useCategoriasDoEvento(eventoId: string) {
  return useQuery<Categoria[]>({
    queryKey: eventoKey(eventoId),
    queryFn: () => listarCategoriasDoEvento(eventoId),
  });
}

export function useAtribuirCategoria(eventoId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (categoriaId: string) => atribuirCategoria(eventoId, categoriaId),
    onSuccess: () => qc.invalidateQueries({ queryKey: eventoKey(eventoId) }),
  });
}

export function useRemoverCategoria(eventoId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (categoriaId: string) => removerCategoria(eventoId, categoriaId),
    onSuccess: () => qc.invalidateQueries({ queryKey: eventoKey(eventoId) }),
  });
}
