import { useQuery } from '@tanstack/react-query';
import { obterResumoPorCategoria, type ResumoItem } from './api';

export function useResumoPorCategoria() {
  return useQuery<ResumoItem[]>({
    queryKey: ['resumo-por-categoria'],
    queryFn: obterResumoPorCategoria,
  });
}
