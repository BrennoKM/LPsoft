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
