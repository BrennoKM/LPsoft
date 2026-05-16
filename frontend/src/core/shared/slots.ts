/**
 * Registry de slots — o equivalente no frontend ao SPI do backend
 * (core define a "tomada"; features se registram; o core consome a lista,
 * vazia se nenhuma feature presente). Quem decide o que entra é o
 * composition root (features.ts), cortado pelo build.sh a partir do
 * manifesto do cliente — espelhando o classpath decidido pelo Maven profile.
 *
 * O core NUNCA importa uma feature; só lê este registry.
 */
import type { ComponentType } from 'react';

export type EventoRowSlot = ComponentType<{ eventoId: string }>;

const eventoRowSlots: EventoRowSlot[] = [];

export function registerEventoRowSlot(component: EventoRowSlot): void {
  if (!eventoRowSlots.includes(component)) {
    eventoRowSlots.push(component);
  }
}

export function getEventoRowSlots(): readonly EventoRowSlot[] {
  return eventoRowSlots;
}

// ── Slot do formulário de criação ─────────────────────────────────────────
// Read-write: o slot renderiza campos próprios, guarda o próprio estado e,
// DEPOIS que o core cria o evento, recebe o id novo para aplicar-se (ex.:
// atribuir categoria, registrar recorrência). O core nunca conhece a feature.

/** Aplica o efeito da feature ao evento recém-criado. */
export type EventoCreateApply = (eventoId: string) => Promise<void>;

export type EventoCreateSlotProps = {
  /** Chamado UMA vez (no mount): registra como o slot se aplica ao evento. */
  onReady: (apply: EventoCreateApply) => void;
};

export type EventoCreateSlot = ComponentType<EventoCreateSlotProps>;

const eventoCreateSlots: EventoCreateSlot[] = [];

export function registerEventoCreateSlot(component: EventoCreateSlot): void {
  if (!eventoCreateSlots.includes(component)) {
    eventoCreateSlots.push(component);
  }
}

export function getEventoCreateSlots(): readonly EventoCreateSlot[] {
  return eventoCreateSlots;
}
