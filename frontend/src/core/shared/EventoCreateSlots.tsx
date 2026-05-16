'use client';

import { forwardRef, useImperativeHandle, useRef } from 'react';
import { getEventoCreateSlots, type EventoCreateApply } from './slots';

export type EventoCreateSlotsHandle = {
  /** Aplica todos os slots ao evento recém-criado (sequencial). */
  applyAll: (eventoId: string) => Promise<void>;
};

/**
 * Renderiza os slots de criação registrados pelas features e coleta os
 * `apply` de cada um. O core (formulário de novo evento) chama `applyAll`
 * após criar o evento. Lista vazia (ex.: lite) → nada renderiza, nada aplica.
 */
export const EventoCreateSlots = forwardRef<EventoCreateSlotsHandle>(
  function EventoCreateSlots(_props, ref) {
    const slots = getEventoCreateSlots();
    // Índice estável (o registry não muda em runtime); cada slot registra
    // seu apply uma vez no mount.
    const appliers = useRef<EventoCreateApply[]>([]);

    useImperativeHandle(
      ref,
      () => ({
        async applyAll(eventoId: string) {
          for (const apply of appliers.current) {
            if (apply) await apply(eventoId);
          }
        },
      }),
      [],
    );

    if (slots.length === 0) return null;

    return (
      <div className="flex flex-col gap-4 rounded-md border border-dashed p-4">
        {slots.map((Slot, i) => (
          <Slot
            key={i}
            onReady={(apply) => {
              appliers.current[i] = apply;
            }}
          />
        ))}
      </div>
    );
  },
);
