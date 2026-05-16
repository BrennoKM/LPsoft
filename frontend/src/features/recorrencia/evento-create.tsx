'use client';

import { useEffect, useRef, useState } from 'react';
import type { EventoCreateSlotProps } from '@/core/shared/slots';
import { criarRecorrencia, type Frequencia } from './api';

const ctrl =
  'h-9 rounded-md border border-input bg-background px-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50';

type Estado = { ativar: boolean; freq: Frequencia; intervalo: number; ate: string };

/**
 * Slot injetado no formulário de "Novo evento". Se "repete?" estiver marcado,
 * ao criar o evento o core chama o `apply` e a recorrência é registrada para
 * o id novo (que materializa a janela imediatamente). Core não conhece isto.
 */
export function EventoCreateRecorrencia({ onReady }: EventoCreateSlotProps) {
  const [estado, setEstado] = useState<Estado>({
    ativar: false,
    freq: 'DIARIA',
    intervalo: 1,
    ate: '',
  });
  const ref = useRef<Estado>(estado);
  useEffect(() => {
    ref.current = estado;
  }, [estado]);

  useEffect(() => {
    onReady(async (eventoId: string) => {
      const e = ref.current;
      if (!e.ativar) return;
      await criarRecorrencia(eventoId, {
        freq: e.freq,
        intervalo: Math.max(1, e.intervalo),
        ate: e.ate ? new Date(e.ate).toISOString() : undefined,
      });
    });
  }, [onReady]);

  return (
    <div className="flex flex-col gap-2">
      <label className="flex items-center gap-2 text-sm font-medium">
        <input
          type="checkbox"
          checked={estado.ativar}
          onChange={(ev) => setEstado((s) => ({ ...s, ativar: ev.target.checked }))}
        />
        Repete?
      </label>

      {estado.ativar && (
        <div className="flex flex-wrap items-end gap-3 pl-6">
          <div className="flex flex-col gap-1">
            <span className="text-xs text-muted-foreground">Frequência</span>
            <select
              className={`${ctrl} w-36`}
              value={estado.freq}
              onChange={(ev) =>
                setEstado((s) => ({ ...s, freq: ev.target.value as Frequencia }))
              }
              aria-label="Frequência"
            >
              <option value="DIARIA">Diária</option>
              <option value="SEMANAL">Semanal</option>
              <option value="MENSAL">Mensal</option>
            </select>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-xs text-muted-foreground">A cada</span>
            <input
              type="number"
              min={1}
              className={`${ctrl} w-20`}
              value={estado.intervalo}
              onChange={(ev) =>
                setEstado((s) => ({ ...s, intervalo: Number(ev.target.value) }))
              }
              aria-label="Intervalo"
            />
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-xs text-muted-foreground">Até (opcional)</span>
            <input
              type="datetime-local"
              className={`${ctrl} w-56`}
              value={estado.ate}
              onChange={(ev) => setEstado((s) => ({ ...s, ate: ev.target.value }))}
              aria-label="Até"
            />
          </div>
        </div>
      )}
    </div>
  );
}
