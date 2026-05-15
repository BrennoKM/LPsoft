import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { criarCategoria, deletarCategoria, listarCategorias, type Categoria } from './api';

const KEY = ['categorias'] as const;

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
