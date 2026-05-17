import { useQuery } from '@tanstack/react-query';
import { listarNotificacoesProgramadas, type NotificacaoProgramada } from './api';

export function useNotificacoesProgramadas() {
  return useQuery<NotificacaoProgramada[]>({
    queryKey: ['notificacao', 'programadas'],
    queryFn: listarNotificacoesProgramadas,
    // Acompanha a transição programada → enviada feita pelo dispatcher.
    refetchInterval: 15000,
  });
}
