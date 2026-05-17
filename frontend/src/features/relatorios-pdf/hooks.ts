import { useQuery } from '@tanstack/react-query';
import { obterPreviaRelatorio } from './api';

export function usePreviaRelatorio() {
  return useQuery<string[]>({
    queryKey: ['relatorios-pdf', 'previa'],
    queryFn: obterPreviaRelatorio,
  });
}
