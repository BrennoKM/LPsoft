'use client';

import { extractMessage } from '@/lib/http';
import { useResumoPorCategoria } from './hooks';

/**
 * Painel injetado na página de Categorias via CategoriaPanelSlot do core.
 * Esta feature depende ESTRITAMENTE de 'categorias' no backend; no front ela
 * apenas se registra no slot — categorias não a conhece.
 */
export function ResumoPorCategoriaPanel() {
  const { data, isLoading, error } = useResumoPorCategoria();

  return (
    <section className="flex flex-col gap-3 rounded-md border bg-card p-4">
      <div>
        <h2 className="text-sm font-semibold">Resumo por categoria</h2>
        <p className="mt-0.5 text-xs text-muted-foreground">
          Quantos eventos estão associados a cada categoria.
        </p>
      </div>

      {isLoading && <p className="text-sm text-muted-foreground">Carregando…</p>}
      {error && (
        <p className="text-sm text-destructive">
          {extractMessage(error, 'Falha ao carregar o resumo')}
        </p>
      )}
      {data && data.length === 0 && (
        <p className="text-sm text-muted-foreground">Nenhuma categoria ainda.</p>
      )}

      {data && data.length > 0 && (
        <ul className="divide-y rounded-md border">
          {data.map((r) => (
            <li key={r.categoriaId} className="flex items-center justify-between px-3 py-2">
              <span className="flex items-center gap-2 text-sm">
                {r.cor && (
                  <span
                    className="inline-block h-3 w-3 rounded-full border"
                    style={{ backgroundColor: r.cor }}
                  />
                )}
                {r.nome}
              </span>
              <span className="rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">
                {r.totalEventos} {r.totalEventos === 1 ? 'evento' : 'eventos'}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
