import { useQuery } from '@tanstack/react-query';
import { obterResumo, type Resumo } from './api';

const KEY = ['analytics', 'resumo'] as const;

export function useResumo() {
  return useQuery<Resumo>({ queryKey: KEY, queryFn: obterResumo });
}
