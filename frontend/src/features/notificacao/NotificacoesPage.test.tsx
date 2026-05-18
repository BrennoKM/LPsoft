import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';

// Mock do hook react-query → controla estados sem QueryClientProvider nem o
// refetchInterval. Render exemplar (padrão replicável p/ outras páginas).
vi.mock('./hooks', () => ({ useNotificacoesProgramadas: vi.fn() }));
import { useNotificacoesProgramadas } from './hooks';
import { NotificacoesPage } from './index';

const mockHook = vi.mocked(useNotificacoesProgramadas);

function arrange(state: Partial<ReturnType<typeof useNotificacoesProgramadas>>) {
  mockHook.mockReturnValue({
    data: undefined,
    isLoading: false,
    error: null,
    ...state,
  } as ReturnType<typeof useNotificacoesProgramadas>);
}

beforeEach(() => mockHook.mockReset());

describe('NotificacoesPage', () => {
  it('loading mostra "Carregando…"', () => {
    arrange({ isLoading: true });
    render(<NotificacoesPage />);
    expect(screen.getByText('Carregando…')).toBeInTheDocument();
  });

  it('erro mostra a mensagem (fallback p/ erro não-Axios)', () => {
    arrange({ error: new Error('x') });
    render(<NotificacoesPage />);
    expect(screen.getByText('Falha ao carregar')).toBeInTheDocument();
  });

  it('lista vazia mostra o placeholder', () => {
    arrange({ data: [] });
    render(<NotificacoesPage />);
    expect(
      screen.getByText(/Nenhuma notificação programada/i),
    ).toBeInTheDocument();
  });

  it('lista renderiza titulo e badge agendada vs enviada', () => {
    arrange({
      data: [
        { eventoId: 'e1', titulo: 'Reunião', programadaPara: '2026-06-01T10:00:00Z', enviadaEm: null },
        { eventoId: 'e2', titulo: 'Consulta', programadaPara: '2026-06-02T09:00:00Z', enviadaEm: '2026-06-01T09:00:00Z' },
      ],
    });
    render(<NotificacoesPage />);
    expect(screen.getByText('Reunião')).toBeInTheDocument();
    expect(screen.getByText('Consulta')).toBeInTheDocument();
    expect(screen.getByText('agendada')).toBeInTheDocument();
    expect(screen.getByText(/^enviada /)).toBeInTheDocument();
  });
});
