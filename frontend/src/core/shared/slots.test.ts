import { describe, it, expect, beforeEach, vi } from 'vitest';

// O registry é um singleton de módulo. resetModules + import dinâmico isola
// cada teste, tornando as asserções absolutas e determinísticas.
type Slots = typeof import('./slots');
let s: Slots;

beforeEach(async () => {
  vi.resetModules();
  s = await import('./slots');
});

const A = () => null;
const B = () => null;

describe('registry de slots (DI análogo ao SPI)', () => {
  it('registries começam vazios', () => {
    expect(s.getEventoRowSlots()).toHaveLength(0);
    expect(s.getEventoBadgeSlots()).toHaveLength(0);
    expect(s.getEventoCreateSlots()).toHaveLength(0);
    expect(s.getCategoriaPanelSlots()).toHaveLength(0);
  });

  it('register é idempotente: mesma ref não duplica', () => {
    s.registerEventoRowSlot(A);
    s.registerEventoRowSlot(A);
    expect(s.getEventoRowSlots()).toEqual([A]);
  });

  it('get reflete o registro de refs distintas, na ordem', () => {
    s.registerEventoRowSlot(A);
    s.registerEventoRowSlot(B);
    expect(s.getEventoRowSlots()).toEqual([A, B]);
  });

  it('isolamento: registrar Row não vaza para Badge/Create/CategoriaPanel', () => {
    s.registerEventoRowSlot(A);
    expect(s.getEventoBadgeSlots()).toHaveLength(0);
    expect(s.getEventoCreateSlots()).toHaveLength(0);
    expect(s.getCategoriaPanelSlots()).toHaveLength(0);
  });

  it('Badge/Create/CategoriaPanel também idempotentes e isolados entre si', () => {
    s.registerEventoBadgeSlot(A);
    s.registerEventoBadgeSlot(A);
    s.registerEventoCreateSlot(A);
    s.registerCategoriaPanelSlot(A);
    s.registerCategoriaPanelSlot(A);
    expect(s.getEventoBadgeSlots()).toEqual([A]);
    expect(s.getEventoCreateSlots()).toEqual([A]);
    expect(s.getCategoriaPanelSlots()).toEqual([A]);
    expect(s.getEventoRowSlots()).toHaveLength(0);
  });
});
