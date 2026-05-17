'use client';

import {
  addMonths,
  eachDayOfInterval,
  endOfMonth,
  endOfWeek,
  format,
  isSameMonth,
  isToday,
  parseISO,
  startOfMonth,
  startOfWeek,
  subMonths,
} from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { useMemo, useState } from 'react';
import { Button } from '@/components/ui/button';
import { getEventoBadgeSlots } from '@/core/shared/slots';
import type { EventoResponse } from './api';
import { EventoDetalheModal } from './EventoDetalheModal';

const SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

/** Sugere início = fim do último evento do dia (senão 09:00) — facilita não
 *  colidir; não bloqueia se você quiser sobrepor. */
function horaSugerida(eventosDoDia: EventoResponse[]): string {
  if (eventosDoDia.length === 0) return '09:00';
  const ultimo = eventosDoDia
    .map((e) => parseISO(e.fim))
    .reduce((max, f) => (f > max ? f : max));
  return format(ultimo, 'HH:mm');
}

export function CalendarioEventos({
  eventos,
  onDiaClick,
}: {
  eventos: EventoResponse[];
  onDiaClick: (isoDate: string, horaInicio: string) => void;
}) {
  const [mes, setMes] = useState(() => startOfMonth(new Date()));
  const [selecionado, setSelecionado] = useState<EventoResponse | null>(null);
  const badgeSlots = getEventoBadgeSlots();

  const dias = useMemo(() => {
    const inicio = startOfWeek(startOfMonth(mes), { weekStartsOn: 0 });
    const fim = endOfWeek(endOfMonth(mes), { weekStartsOn: 0 });
    return eachDayOfInterval({ start: inicio, end: fim });
  }, [mes]);

  const porDia = useMemo(() => {
    const m = new Map<string, EventoResponse[]>();
    for (const e of eventos) {
      const chave = format(parseISO(e.inicio), 'yyyy-MM-dd');
      const lista = m.get(chave) ?? [];
      lista.push(e);
      m.set(chave, lista);
    }
    return m;
  }, [eventos]);

  return (
    <div className="rounded-md border bg-card">
      <div className="flex items-center justify-between border-b p-3">
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="sm" onClick={() => setMes((d) => subMonths(d, 1))}>
            ‹
          </Button>
          <span className="min-w-40 text-center text-sm font-medium capitalize">
            {format(mes, 'MMMM yyyy', { locale: ptBR })}
          </span>
          <Button variant="ghost" size="sm" onClick={() => setMes((d) => addMonths(d, 1))}>
            ›
          </Button>
        </div>
        <Button variant="secondary" size="sm" onClick={() => setMes(startOfMonth(new Date()))}>
          Hoje
        </Button>
      </div>

      <div className="grid grid-cols-7 border-b text-center text-xs font-medium text-muted-foreground">
        {SEMANA.map((d) => (
          <div key={d} className="p-2">
            {d}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-7">
        {dias.map((dia) => {
          const chave = format(dia, 'yyyy-MM-dd');
          const doDia = porDia.get(chave) ?? [];
          const foraDoMes = !isSameMonth(dia, mes);
          return (
            <button
              type="button"
              key={chave}
              onClick={() => onDiaClick(chave, horaSugerida(doDia))}
              className={`flex min-h-24 flex-col gap-1 border-b border-r p-1.5 text-left transition-colors hover:ring-1 hover:ring-inset hover:ring-primary/40 ${
                foraDoMes ? 'bg-muted/30 text-muted-foreground' : ''
              }`}
            >
              <span
                className={`self-end text-xs ${
                  isToday(dia)
                    ? 'flex h-5 w-5 items-center justify-center rounded-full bg-primary text-primary-foreground'
                    : ''
                }`}
              >
                {format(dia, 'd')}
              </span>
              {doDia.map((e) => (
                <span
                  key={e.id}
                  role="button"
                  tabIndex={0}
                  onClick={(ev) => {
                    ev.stopPropagation();
                    setSelecionado(e);
                  }}
                  className="flex flex-col gap-0.5 rounded bg-primary px-1.5 py-0.5 text-[11px] leading-tight text-primary-foreground hover:bg-primary/90"
                >
                  <span className="truncate font-medium" title={e.titulo}>
                    {e.titulo}
                  </span>
                  {badgeSlots.map((Slot, i) => (
                    <Slot key={i} eventoId={e.id} />
                  ))}
                </span>
              ))}
            </button>
          );
        })}
      </div>

      {selecionado && (
        <EventoDetalheModal evento={selecionado} onClose={() => setSelecionado(null)} />
      )}
    </div>
  );
}
