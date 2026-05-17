import { api } from '@/lib/http';
import type { NovoEventoInput } from './schemas';

export type EventoStatus = 'RASCUNHO' | 'PUBLICADO' | 'CANCELADO';

export type EventoResponse = {
  id: string;
  titulo: string;
  descricao: string | null;
  inicio: string;
  fim: string;
  criadoPor: string;
  status: EventoStatus;
  criadoEm: string;
};

export async function listarEventos(): Promise<EventoResponse[]> {
  const { data } = await api.get<EventoResponse[]>('/eventos');
  return data;
}

function payloadDe(input: NovoEventoInput) {
  return {
    titulo: input.titulo,
    descricao: input.descricao || undefined,
    inicio: new Date(input.inicio).toISOString(),
    fim: new Date(input.fim).toISOString(),
  };
}

export async function criarEvento(input: NovoEventoInput): Promise<EventoResponse> {
  const { data } = await api.post<EventoResponse>('/eventos', payloadDe(input));
  return data;
}

export async function atualizarEvento(
  id: string,
  input: NovoEventoInput,
): Promise<EventoResponse> {
  const { data } = await api.patch<EventoResponse>(`/eventos/${id}`, payloadDe(input));
  return data;
}

export async function excluirEvento(id: string): Promise<void> {
  await api.delete(`/eventos/${id}`);
}
