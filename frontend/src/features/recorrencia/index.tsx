'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { format } from 'date-fns';
import { RefreshCw, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useEventos } from '@/core/eventos/hooks';
import { extractMessage } from '@/lib/http';
import type { Frequencia } from './api';
import {
  useCriarRecorrencia,
  useDesativarRecorrencia,
  useProcessarRecorrencia,
  useRecorrencia,
} from './hooks';

const FREQ_LABEL: Record<Frequencia, string> = {
  DIARIA: 'dia(s)',
  SEMANAL: 'semana(s)',
  MENSAL: 'mês(es)',
};

const selectClass =
  'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50';

const schema = z.object({
  eventoId: z.string().uuid('Selecione um evento'),
  freq: z.enum(['DIARIA', 'SEMANAL', 'MENSAL']),
  intervalo: z.coerce.number().int().min(1, 'Mínimo 1'),
  ate: z.string().optional().or(z.literal('')),
});
type FormInput = z.infer<typeof schema>;

export function RecorrenciaPage() {
  const { data: eventos, isLoading } = useEventos();
  const criar = useCriarRecorrencia();
  const processar = useProcessarRecorrencia();
  const [erro, setErro] = useState<string | null>(null);
  const [ultimoProcessamento, setUltimoProcessamento] = useState<number | null>(null);

  const form = useForm<FormInput>({
    resolver: zodResolver(schema),
    defaultValues: { eventoId: '', freq: 'DIARIA', intervalo: 1, ate: '' },
  });

  async function onSubmit(values: FormInput) {
    setErro(null);
    try {
      await criar.mutateAsync({
        eventoId: values.eventoId,
        input: {
          freq: values.freq,
          intervalo: values.intervalo,
          ate: values.ate ? new Date(values.ate).toISOString() : undefined,
        },
      });
      form.reset({ eventoId: '', freq: 'DIARIA', intervalo: 1, ate: '' });
    } catch (err) {
      setErro(extractMessage(err, 'Falha ao registrar recorrência'));
    }
  }

  async function processarAgora() {
    setUltimoProcessamento(null);
    const r = await processar.mutateAsync();
    setUltimoProcessamento(r.ocorrenciasCriadas);
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Recorrência</h1>
        <div className="flex items-center gap-3">
          {ultimoProcessamento !== null && (
            <span className="text-sm text-muted-foreground">
              {ultimoProcessamento} ocorrência(s) gerada(s)
            </span>
          )}
          <Button
            variant="ghost"
            size="sm"
            onClick={processarAgora}
            disabled={processar.isPending}
          >
            <RefreshCw className="h-4 w-4" />
            Processar pendentes
          </Button>
        </div>
      </div>

      <form
        onSubmit={form.handleSubmit(onSubmit)}
        className="flex flex-wrap items-end gap-3 rounded-md border bg-card p-4"
      >
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="eventoId">Evento modelo</Label>
          <select id="eventoId" className={`${selectClass} w-64`} {...form.register('eventoId')}>
            <option value="">Selecione…</option>
            {eventos?.map((e) => (
              <option key={e.id} value={e.id}>
                {e.titulo}
              </option>
            ))}
          </select>
          {form.formState.errors.eventoId && (
            <span className="text-xs text-destructive">
              {form.formState.errors.eventoId.message}
            </span>
          )}
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="freq">Frequência</Label>
          <select id="freq" className={`${selectClass} w-40`} {...form.register('freq')}>
            <option value="DIARIA">Diária</option>
            <option value="SEMANAL">Semanal</option>
            <option value="MENSAL">Mensal</option>
          </select>
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="intervalo">A cada</Label>
          <Input
            id="intervalo"
            type="number"
            min={1}
            className="w-24"
            {...form.register('intervalo')}
          />
          {form.formState.errors.intervalo && (
            <span className="text-xs text-destructive">
              {form.formState.errors.intervalo.message}
            </span>
          )}
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="ate">Até (opcional)</Label>
          <Input id="ate" type="datetime-local" className="w-56" {...form.register('ate')} />
        </div>
        <Button type="submit" disabled={form.formState.isSubmitting}>
          Registrar
        </Button>
        {erro && <span className="w-full text-sm text-destructive">{erro}</span>}
      </form>

      {isLoading && <p className="text-muted-foreground">Carregando…</p>}
      {eventos && eventos.length === 0 && (
        <p className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
          Nenhum evento para tornar recorrente.
        </p>
      )}
      {eventos && eventos.length > 0 && (
        <ul className="divide-y rounded-md border bg-card">
          {eventos.map((e) => (
            <EventoRecorrenciaRow key={e.id} eventoId={e.id} titulo={e.titulo} />
          ))}
        </ul>
      )}
    </div>
  );
}

function EventoRecorrenciaRow({ eventoId, titulo }: { eventoId: string; titulo: string }) {
  const { data: regras } = useRecorrencia(eventoId);
  const desativar = useDesativarRecorrencia();
  const ativa = regras?.find((r) => r.ativo);

  return (
    <li className="flex items-center justify-between p-3">
      <div>
        <p className="font-medium">{titulo}</p>
        {ativa ? (
          <p className="mt-1 text-sm text-muted-foreground">
            A cada {ativa.intervalo} {FREQ_LABEL[ativa.freq]} · próximo{' '}
            {format(new Date(ativa.proximoDisparo), 'dd/MM/yyyy HH:mm')}
            {ativa.ate && ` · até ${format(new Date(ativa.ate), 'dd/MM/yyyy')}`}
          </p>
        ) : (
          <p className="mt-1 text-sm text-muted-foreground">Sem recorrência</p>
        )}
      </div>
      {ativa && (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => desativar.mutate(eventoId)}
          aria-label={`Desativar recorrência de ${titulo}`}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      )}
    </li>
  );
}
