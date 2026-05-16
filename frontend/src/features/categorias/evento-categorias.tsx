'use client';

import { X } from 'lucide-react';
import { useCategorias } from './hooks';
import { useAtribuirCategoria, useCategoriasDoEvento, useRemoverCategoria } from './hooks';

const selectClass =
  'h-7 rounded-md border border-input bg-background px-2 text-xs ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50';

/**
 * Slot injetado na linha de evento via registry do core (slots.ts).
 * O core não conhece este componente — só renderiza o que foi registrado.
 */
export function EventoCategorias({ eventoId }: { eventoId: string }) {
  const { data: todas } = useCategorias();
  const { data: doEvento } = useCategoriasDoEvento(eventoId);
  const atribuir = useAtribuirCategoria(eventoId);
  const remover = useRemoverCategoria(eventoId);

  const atribuidasIds = new Set((doEvento ?? []).map((c) => c.id));
  const disponiveis = (todas ?? []).filter((c) => !atribuidasIds.has(c.id));

  return (
    <div className="mt-2 flex flex-wrap items-center gap-1.5">
      {(doEvento ?? []).map((c) => (
        <span
          key={c.id}
          className="inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs"
        >
          {c.cor && (
            <span
              className="inline-block h-2.5 w-2.5 rounded-full"
              style={{ backgroundColor: c.cor }}
            />
          )}
          {c.nome}
          <button
            type="button"
            onClick={() => remover.mutate(c.id)}
            aria-label={`Remover categoria ${c.nome}`}
            className="text-muted-foreground hover:text-destructive"
          >
            <X className="h-3 w-3" />
          </button>
        </span>
      ))}

      {disponiveis.length > 0 && (
        <select
          className={selectClass}
          value=""
          onChange={(e) => e.target.value && atribuir.mutate(e.target.value)}
          aria-label="Adicionar categoria"
        >
          <option value="">+ categoria</option>
          {disponiveis.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome}
            </option>
          ))}
        </select>
      )}
    </div>
  );
}
