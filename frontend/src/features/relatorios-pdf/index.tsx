'use client';

import { FileDown } from 'lucide-react';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { hasFeature } from '@/lib/feature-flags';
import { extractMessage } from '@/lib/http';
import { baixarRelatorioEventos } from './api';
import { usePreviaRelatorio } from './hooks';

export function RelatoriosPage() {
  const [baixando, setBaixando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const comAnalytics = hasFeature('analytics');
  const { data: previa, isLoading, error } = usePreviaRelatorio();

  async function baixar() {
    setErro(null);
    setBaixando(true);
    try {
      const blob = await baixarRelatorioEventos();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'eventos.pdf';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      setErro(extractMessage(err, 'Falha ao gerar o relatório'));
    } finally {
      setBaixando(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-2xl font-semibold">Relatórios</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          PDF dos eventos, gerado sem bibliotecas externas.
        </p>
      </div>

      <div className="flex flex-col gap-3 rounded-md border bg-card p-4">
        <Button onClick={baixar} disabled={baixando} className="self-start">
          <FileDown className="h-4 w-4" />
          {baixando ? 'Gerando…' : 'Baixar PDF'}
        </Button>

        <p className="text-sm text-muted-foreground">
          {comAnalytics ? (
            <>
              Este pacote contratou <strong>analytics</strong>: o PDF inclui a
              seção de agregados (dependência <em>opcional</em> resolvida via SPI
              do core).
            </>
          ) : (
            <>
              Este pacote <strong>não</strong> contratou <strong>analytics</strong>:
              o PDF sai sem a seção de agregados — degrada graciosamente, sem
              erro (dependência <em>opcional</em>).
            </>
          )}
        </p>

        {erro && <p className="text-sm text-destructive">{erro}</p>}
      </div>

      <div className="flex flex-col gap-2">
        <h2 className="text-sm font-medium text-muted-foreground">
          Prévia do conteúdo
        </h2>
        {isLoading && <p className="text-sm text-muted-foreground">Carregando…</p>}
        {error && (
          <p className="text-sm text-destructive">
            {extractMessage(error, 'Falha ao carregar a prévia')}
          </p>
        )}
        {previa && (
          <pre className="overflow-x-auto rounded-md border bg-muted/40 p-4 text-xs leading-relaxed text-foreground">
            {previa.join('\n')}
          </pre>
        )}
      </div>
    </div>
  );
}
