import { useQuery } from '@tanstack/react-query';
import { listarPushEnviados, type PushEnviado } from './api';

export function usePushEnviados() {
  return useQuery<PushEnviado[]>({
    queryKey: ['push-notif', 'enviados'],
    queryFn: listarPushEnviados,
  });
}
