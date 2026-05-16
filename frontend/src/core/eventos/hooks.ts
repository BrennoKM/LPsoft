import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  atualizarEvento,
  criarEvento,
  excluirEvento,
  listarEventos,
  type EventoResponse,
} from './api';
import type { NovoEventoInput } from './schemas';

const EVENTOS_KEY = ['eventos'] as const;

export function useEventos() {
  return useQuery<EventoResponse[]>({
    queryKey: EVENTOS_KEY,
    queryFn: listarEventos,
  });
}

export function useCriarEvento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: NovoEventoInput) => criarEvento(input),
    onSuccess: () => qc.invalidateQueries({ queryKey: EVENTOS_KEY }),
  });
}

export function useAtualizarEvento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: NovoEventoInput }) =>
      atualizarEvento(id, input),
    onSuccess: () => qc.invalidateQueries({ queryKey: EVENTOS_KEY }),
  });
}

export function useExcluirEvento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => excluirEvento(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: EVENTOS_KEY }),
  });
}
