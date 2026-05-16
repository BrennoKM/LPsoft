/**
 * Efeito-colateral de registro (o "@Component" desta feature). Importado pelo
 * composition root do core (features.ts) só quando 'categorias' é contratada.
 */
import { registerEventoRowSlot } from '@/core/shared/slots';
import { EventoCategorias } from './evento-categorias';

registerEventoRowSlot(EventoCategorias);
