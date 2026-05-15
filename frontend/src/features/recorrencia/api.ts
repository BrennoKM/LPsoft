import { api } from '@/lib/http';

export type Frequencia = 'DIARIA' | 'SEMANAL' | 'MENSAL';

export type Recorrencia = {
  id: string;
  eventoModeloId: string;
  freq: Frequencia;
  intervalo: number;
  proximoDisparo: string;
  ate: string | null;
  ativo: boolean;
};

export type CriarRecorrenciaInput = {
  freq: Frequencia;
  intervalo: number;
  ate?: string;
};

export async function listarRecorrencia(eventoId: string): Promise<Recorrencia[]> {
  const { data } = await api.get<Recorrencia[]>(`/eventos/${eventoId}/recorrencia`);
  return data;
}

export async function criarRecorrencia(
  eventoId: string,
  input: CriarRecorrenciaInput,
): Promise<Recorrencia> {
  const { data } = await api.post<Recorrencia>(`/eventos/${eventoId}/recorrencia`, input);
  return data;
}

export async function desativarRecorrencia(eventoId: string): Promise<void> {
  await api.delete(`/eventos/${eventoId}/recorrencia`);
}

export async function processarRecorrencia(): Promise<{ ocorrenciasCriadas: number }> {
  const { data } = await api.post<{ ocorrenciasCriadas: number }>('/recorrencia/processar');
  return data;
}
