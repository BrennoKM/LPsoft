import { api } from '@/lib/http';

/** Baixa o relatório de eventos em PDF (endpoint autenticado → blob). */
export async function baixarRelatorioEventos(): Promise<Blob> {
  const { data } = await api.get('/relatorios/eventos.pdf', { responseType: 'blob' });
  return data as Blob;
}
