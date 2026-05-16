import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  criarRecorrencia,
  desativarRecorrencia,
  listarRecorrencia,
  processarRecorrencia,
  type CriarRecorrenciaInput,
  type Recorrencia,
} from './api';

const recorrenciaKey = (eventoId: string) => ['recorrencia', eventoId] as const;

export function useRecorrencia(eventoId: string) {
  return useQuery<Recorrencia[]>({
    queryKey: recorrenciaKey(eventoId),
    queryFn: () => listarRecorrencia(eventoId),
  });
}

export function useCriarRecorrencia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ eventoId, input }: { eventoId: string; input: CriarRecorrenciaInput }) =>
      criarRecorrencia(eventoId, input),
    onSuccess: (_data, { eventoId }) => {
      qc.invalidateQueries({ queryKey: recorrenciaKey(eventoId) });
      // registrar já materializa a janela → a lista de eventos muda na hora
      qc.invalidateQueries({ queryKey: ['eventos'] });
    },
  });
}

export function useDesativarRecorrencia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (eventoId: string) => desativarRecorrencia(eventoId),
    onSuccess: (_data, eventoId) =>
      qc.invalidateQueries({ queryKey: recorrenciaKey(eventoId) }),
  });
}

export function useProcessarRecorrencia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: processarRecorrencia,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['eventos'] });
      qc.invalidateQueries({ queryKey: ['recorrencia'] });
    },
  });
}
