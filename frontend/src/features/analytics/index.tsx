'use client';

import { format } from 'date-fns';
import { extractMessage } from '@/lib/http';
import { useResumo } from './hooks';

export function AnalyticsPage() {
  const { data, isLoading, error } = useResumo();

  const maxDia = data ? Math.max(1, ...data.porDia.map((d) => d.total)) : 1;

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold">Analytics</h1>

      {isLoading && <p className="text-muted-foreground">Carregando…</p>}
      {error && (
        <p className="text-destructive">{extractMessage(error, 'Falha ao carregar resumo')}</p>
      )}

      {data && (
        <>
          <div className="rounded-md border bg-card p-6">
            <p className="text-sm text-muted-foreground">Eventos contabilizados</p>
            <p className="mt-1 text-4xl font-semibold tabular-nums">{data.total}</p>
          </div>

          <section className="flex flex-col gap-2">
            <h2 className="text-sm font-medium text-muted-foreground">Por dia</h2>
            {data.porDia.length === 0 ? (
              <p className="rounded-md border border-dashed p-6 text-center text-muted-foreground">
                Sem dados ainda.
              </p>
            ) : (
              <ul className="flex flex-col gap-1.5 rounded-md border bg-card p-4">
                {data.porDia.map((d) => (
                  <li key={d.dia} className="flex items-center gap-3">
                    <span className="w-24 shrink-0 text-sm tabular-nums">
                      {format(new Date(`${d.dia}T00:00:00`), 'dd/MM/yyyy')}
                    </span>
                    <span
                      className="h-4 rounded bg-primary"
                      style={{ width: `${(d.total / maxDia) * 100}%`, minWidth: '0.5rem' }}
                    />
                    <span className="text-sm tabular-nums text-muted-foreground">{d.total}</span>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="flex flex-col gap-2">
            <h2 className="text-sm font-medium text-muted-foreground">Por usuário</h2>
            {data.porUsuario.length === 0 ? (
              <p className="rounded-md border border-dashed p-6 text-center text-muted-foreground">
                Sem dados ainda.
              </p>
            ) : (
              <ul className="divide-y rounded-md border bg-card">
                {data.porUsuario.map((u) => (
                  <li key={u.criadoPor} className="flex items-center justify-between p-3">
                    <span className="font-mono text-xs text-muted-foreground">{u.criadoPor}</span>
                    <span className="text-sm font-medium tabular-nums">{u.total}</span>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </div>
  );
}
