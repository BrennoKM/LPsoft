'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { format } from 'date-fns';
import { useForm } from 'react-hook-form';
import { useEffect, useState } from 'react';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { extractMessage } from '@/lib/http';
import { usePolitica, useAtualizarPolitica, useLembretesAgendados } from './hooks';

const schema = z.object({
  antecedenciaHoras: z.coerce
    .number()
    .int('Use um número inteiro de horas')
    .min(1, 'Mínimo de 1 hora')
    .max(24 * 30, 'Máximo de 720 horas (30 dias)'),
});
type FormInput = z.input<typeof schema>;

export function LembretesPage() {
  const { data: politica, isLoading: carregandoPolitica } = usePolitica();
  const { data: agendados, isLoading: carregandoLista } = useLembretesAgendados();
  const atualizar = useAtualizarPolitica();
  const [erro, setErro] = useState<string | null>(null);
  const [salvo, setSalvo] = useState(false);

  const form = useForm<FormInput>({
    resolver: zodResolver(schema),
    defaultValues: { antecedenciaHoras: 24 },
  });
  const { reset } = form;

  useEffect(() => {
    if (politica) {
      reset({ antecedenciaHoras: politica.antecedenciaHoras });
    }
  }, [politica, reset]);

  async function onSubmit(values: FormInput) {
    setErro(null);
    setSalvo(false);
    try {
      await atualizar.mutateAsync(Number(values.antecedenciaHoras));
      setSalvo(true);
    } catch (err) {
      setErro(extractMessage(err, 'Falha ao salvar a política'));
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Lembretes</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Política de antecedência: ao criar um evento, um lembrete é programado
          para <strong>N horas antes</strong> do início. Os canais de aviso (ex.:
          Notificações) reagem a essa decisão — aqui você define o quando.
        </p>
      </div>

      <form
        onSubmit={form.handleSubmit(onSubmit)}
        className="flex flex-wrap items-end gap-3 rounded-md border bg-card p-4"
      >
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="antecedenciaHoras">Antecedência (horas)</Label>
          <Input
            id="antecedenciaHoras"
            type="number"
            min={1}
            className="w-32"
            disabled={carregandoPolitica}
            {...form.register('antecedenciaHoras')}
          />
          {form.formState.errors.antecedenciaHoras && (
            <span className="text-xs text-destructive">
              {form.formState.errors.antecedenciaHoras.message}
            </span>
          )}
        </div>
        <Button type="submit" disabled={form.formState.isSubmitting || carregandoPolitica}>
          Salvar
        </Button>
        {salvo && <span className="text-sm text-muted-foreground">Política atualizada.</span>}
        {erro && <span className="w-full text-sm text-destructive">{erro}</span>}
      </form>

      <div>
        <h2 className="mb-2 text-sm font-medium text-muted-foreground">
          Lembretes programados
        </h2>
        {carregandoLista && <p className="text-muted-foreground">Carregando…</p>}
        {agendados && agendados.length === 0 && (
          <p className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
            Nenhum lembrete programado. Crie um evento para programar um.
          </p>
        )}
        {agendados && agendados.length > 0 && (
          <ul className="divide-y rounded-md border bg-card">
            {agendados.map((l, i) => (
              <li
                key={`${l.eventoId}-${i}`}
                className="flex items-start justify-between p-4"
              >
                <p className="font-medium">{l.titulo}</p>
                <span className="text-xs text-muted-foreground">
                  avisar em {format(new Date(l.quando), 'dd/MM/yyyy HH:mm')}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
