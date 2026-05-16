'use client';

import { format } from 'date-fns';
import { Plus } from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { useEventos } from '@/core/eventos/hooks';
import { getEventoRowSlots } from '@/core/shared/slots';
import { extractMessage } from '@/lib/http';

export default function EventosPage() {
  const { data: eventos, isLoading, error } = useEventos();
  const rowSlots = getEventoRowSlots();

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Meus eventos</h1>
        <Link href="/eventos/novo">
          <Button>
            <Plus className="h-4 w-4" />
            Novo evento
          </Button>
        </Link>
      </div>

      {isLoading && <p className="text-muted-foreground">Carregando…</p>}
      {error && <p className="text-destructive">{extractMessage(error, 'Falha ao carregar')}</p>}
      {!isLoading && !error && eventos && eventos.length === 0 && (
        <p className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
          Nenhum evento ainda. Crie o primeiro!
        </p>
      )}

      {eventos && eventos.length > 0 && (
        <ul className="divide-y rounded-md border bg-card">
          {eventos.map((e) => (
            <li key={e.id} className="flex items-start justify-between p-4">
              <div>
                <p className="font-medium">{e.titulo}</p>
                {e.descricao && (
                  <p className="mt-1 text-sm text-muted-foreground">{e.descricao}</p>
                )}
                <p className="mt-2 text-xs text-muted-foreground">
                  {format(new Date(e.inicio), "dd/MM/yyyy HH:mm")} →{' '}
                  {format(new Date(e.fim), "dd/MM/yyyy HH:mm")}
                </p>
                {rowSlots.map((Slot, i) => (
                  <Slot key={i} eventoId={e.id} />
                ))}
              </div>
              <span className="rounded-full bg-secondary px-2 py-0.5 text-xs font-medium">
                {e.status}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
