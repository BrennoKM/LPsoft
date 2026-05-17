import { api } from '@/lib/http';

export type Politica = { antecedenciaHoras: number };

export type LembreteAgendado = {
  eventoId: string;
  titulo: string;
  quando: string;
};

export async function obterPolitica(): Promise<Politica> {
  const { data } = await api.get<Politica>('/lembretes/politica');
  return data;
}

export async function atualizarPolitica(antecedenciaHoras: number): Promise<Politica> {
  const { data } = await api.put<Politica>('/lembretes/politica', { antecedenciaHoras });
  return data;
}

export async function listarAgendados(): Promise<LembreteAgendado[]> {
  const { data } = await api.get<LembreteAgendado[]>('/lembretes/agendados');
  return data;
}
