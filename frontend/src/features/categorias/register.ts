/**
 * Efeito-colateral de registro (o "@Component" desta feature). Importado pelo
 * composition root do core (features.ts) só quando 'categorias' é contratada.
 */
import {
  registerEventoBadgeSlot,
  registerEventoCreateSlot,
  registerEventoRowSlot,
} from '@/core/shared/slots';
import { EventoBadgeCategorias } from './evento-badge';
import { EventoCategorias } from './evento-categorias';
import { EventoCreateCategorias } from './evento-create';

registerEventoRowSlot(EventoCategorias);
registerEventoCreateSlot(EventoCreateCategorias);
registerEventoBadgeSlot(EventoBadgeCategorias);
