'use client';

import { format } from 'date-fns';
import { extractMessage } from '@/lib/http';
import { useNotificacoesProgramadas } from './hooks';

export function NotificacoesPage() {
  const { data, isLoading, error } = useNotificacoesProgramadas();

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-2xl font-semibold">Notificações</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Canal de aviso in-app. Cada item nasce <strong>programado</strong> para
          o futuro — na antecedência definida em{' '}
          <span className="font-medium">Lembretes</span> — e passa a{' '}
          <strong>enviado</strong> quando essa hora chega (envio simulado, a cada
          ~30s).
        </p>
      </div>

      {isLoading && <p className="text-muted-foreground">Carregando…</p>}
      {error && (
        <p className="text-destructive">{extractMessage(error, 'Falha ao carregar')}</p>
      )}
      {data && data.length === 0 && (
        <p className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
          Nenhuma notificação programada. Crie um evento para programar uma.
        </p>
      )}

      {data && data.length > 0 && (
        <ul className="divide-y rounded-md border bg-card">
          {data.map((n, i) => (
            <li key={`${n.eventoId}-${i}`} className="flex items-start justify-between p-4">
              <div>
                <p className="font-medium">{n.titulo}</p>
                <p className="mt-1 text-xs text-muted-foreground">
                  programada para{' '}
                  {format(new Date(n.programadaPara), 'dd/MM/yyyy HH:mm')}
                </p>
              </div>
              {n.enviadaEm ? (
                <span className="rounded-full bg-primary px-2 py-0.5 text-xs text-primary-foreground">
                  enviada {format(new Date(n.enviadaEm), 'dd/MM HH:mm')}
                </span>
              ) : (
                <span className="rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">
                  agendada
                </span>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
