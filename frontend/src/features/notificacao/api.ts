import { api } from '@/lib/http';

export type NotificacaoProgramada = {
  eventoId: string;
  titulo: string;
  programadaPara: string;
  enviadaEm: string | null;
};

export async function listarNotificacoesProgramadas(): Promise<NotificacaoProgramada[]> {
  const { data } = await api.get<NotificacaoProgramada[]>('/notificacao/programadas');
  return data;
}
