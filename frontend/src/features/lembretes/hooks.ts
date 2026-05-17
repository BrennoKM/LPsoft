import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  atualizarPolitica,
  listarAgendados,
  obterPolitica,
  type LembreteAgendado,
  type Politica,
} from './api';

const POLITICA_KEY = ['lembretes', 'politica'] as const;
const AGENDADOS_KEY = ['lembretes', 'agendados'] as const;

export function usePolitica() {
  return useQuery<Politica>({ queryKey: POLITICA_KEY, queryFn: obterPolitica });
}

export function useAtualizarPolitica() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: atualizarPolitica,
    onSuccess: () => qc.invalidateQueries({ queryKey: POLITICA_KEY }),
  });
}

export function useLembretesAgendados() {
  return useQuery<LembreteAgendado[]>({
    queryKey: AGENDADOS_KEY,
    queryFn: listarAgendados,
  });
}
