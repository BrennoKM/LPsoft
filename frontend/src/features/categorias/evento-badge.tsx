'use client';

import { useCategoriasDoEvento } from './hooks';

/**
 * Badge só-leitura: mostra as categorias do evento como etiquetas compactas.
 * Usado em lugares densos (célula do calendário) via registry do core.
 */
export function EventoBadgeCategorias({ eventoId }: { eventoId: string }) {
  const { data } = useCategoriasDoEvento(eventoId);
  if (!data || data.length === 0) return null;

  return (
    <span className="flex flex-wrap gap-1">
      {data.map((c) => (
        <span
          key={c.id}
          className="inline-flex items-center gap-1 rounded px-1 text-[10px] leading-4 opacity-80"
          title={c.nome}
        >
          <span
            className="inline-block h-2 w-2 rounded-full"
            style={{ backgroundColor: c.cor ?? 'currentColor' }}
          />
          {c.nome}
        </span>
      ))}
    </span>
  );
}
