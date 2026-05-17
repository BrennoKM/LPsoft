'use client';

import { X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import type { EventoCreateSlotProps } from '@/core/shared/slots';
import { atribuirCategoria } from './api';
import { useCategorias } from './hooks';

const selectClass =
  'h-9 w-56 rounded-md border border-input bg-background px-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50';

/**
 * Slot injetado no formulário de "Novo evento" via registry do core. Guarda
 * a seleção localmente; ao criar o evento, o core chama o `apply` e aí as
 * categorias são atribuídas ao id novo. O core não conhece este componente.
 */
export function EventoCreateCategorias({ onReady }: EventoCreateSlotProps) {
  const { data: categorias } = useCategorias();
  const [selecionadas, setSelecionadas] = useState<string[]>([]);
  const selRef = useRef<string[]>([]);
  useEffect(() => {
    selRef.current = selecionadas;
  }, [selecionadas]);

  useEffect(() => {
    onReady(async (eventoId: string) => {
      for (const categoriaId of selRef.current) {
        await atribuirCategoria(eventoId, categoriaId);
      }
    });
  }, [onReady]);

  const escolhidas = (categorias ?? []).filter((c) => selecionadas.includes(c.id));
  const disponiveis = (categorias ?? []).filter((c) => !selecionadas.includes(c.id));

  return (
    <div className="flex flex-col gap-1.5">
      <span className="text-sm font-medium">Categorias</span>
      <div className="flex flex-wrap items-center gap-1.5">
        {escolhidas.map((c) => (
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
              aria-label={`Remover ${c.nome}`}
              onClick={() => setSelecionadas((s) => s.filter((id) => id !== c.id))}
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
            onChange={(e) => {
              if (e.target.value) setSelecionadas((s) => [...s, e.target.value]);
            }}
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
    </div>
  );
}
