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

export async function criarEvento(input: NovoEventoInput): Promise<EventoResponse> {
  const payload = {
    titulo: input.titulo,
    descricao: input.descricao || undefined,
    inicio: new Date(input.inicio).toISOString(),
    fim: new Date(input.fim).toISOString(),
  };
  const { data } = await api.post<EventoResponse>('/eventos', payload);
  return data;
}
