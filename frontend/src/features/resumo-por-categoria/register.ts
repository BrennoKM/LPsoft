/**
 * Efeito-colateral de registro (o "@Component" desta feature). Importado pelo
 * composition root do core (features.ts) só quando 'resumo-por-categoria' é
 * contratada. Injeta o painel na página de Categorias via slot do core —
 * categorias não conhece esta feature.
 */
import { registerCategoriaPanelSlot } from '@/core/shared/slots';
import { ResumoPorCategoriaPanel } from './index';

registerCategoriaPanelSlot(ResumoPorCategoriaPanel);
