/**
 * Efeito-colateral de registro (o "@Component" desta feature). Importado pelo
 * composition root do core (features.ts) só quando 'recorrencia' é contratada.
 */
import { registerEventoCreateSlot } from '@/core/shared/slots';
import { EventoCreateRecorrencia } from './evento-create';

registerEventoCreateSlot(EventoCreateRecorrencia);
