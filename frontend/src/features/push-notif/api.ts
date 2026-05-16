import { api } from '@/lib/http';

export type PushEnviado = {
  eventoId: string;
  titulo: string;
  agendadoPara: string;
  enviadoEm: string;
};

export async function listarPushEnviados(): Promise<PushEnviado[]> {
  const { data } = await api.get<PushEnviado[]>('/push-notif/enviados');
  return data;
}
