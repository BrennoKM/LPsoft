'use client';

import { format } from 'date-fns';
import { extractMessage } from '@/lib/http';
import { usePushEnviados } from './hooks';

export function NotificacoesPage() {
  const { data, isLoading, error } = usePushEnviados();

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-2xl font-semibold">Notificações</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Pushes enviados ao reagir aos lembretes — esta feature depende de
          “lembretes”: cada push só existe porque um lembrete foi agendado.
        </p>
      </div>

      {isLoading && <p className="text-muted-foreground">Carregando…</p>}
      {error && (
        <p className="text-destructive">{extractMessage(error, 'Falha ao carregar')}</p>
      )}
      {data && data.length === 0 && (
        <p className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
          Nenhum push ainda. Crie um evento para disparar a cadeia.
        </p>
      )}

      {data && data.length > 0 && (
        <ul className="divide-y rounded-md border bg-card">
          {data.map((p, i) => (
            <li key={`${p.eventoId}-${i}`} className="flex items-start justify-between p-4">
              <div>
                <p className="font-medium">{p.titulo}</p>
                <p className="mt-1 text-xs text-muted-foreground">
                  agendado para {format(new Date(p.agendadoPara), 'dd/MM/yyyy HH:mm')}
                </p>
              </div>
              <span className="text-xs text-muted-foreground">
                enviado {format(new Date(p.enviadoEm), 'dd/MM/yyyy HH:mm')}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
