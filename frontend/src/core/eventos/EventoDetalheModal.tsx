'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { format, parseISO } from 'date-fns';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { getEventoRowSlots } from '@/core/shared/slots';
import { extractMessage } from '@/lib/http';
import type { EventoResponse } from './api';
import { useAtualizarEvento, useExcluirEvento } from './hooks';
import { novoEventoSchema, type NovoEventoInput } from './schemas';

const paraInputLocal = (iso: string) => format(parseISO(iso), "yyyy-MM-dd'T'HH:mm");

export function EventoDetalheModal({
  evento,
  onClose,
}: {
  evento: EventoResponse;
  onClose: () => void;
}) {
  const atualizar = useAtualizarEvento();
  const excluir = useExcluirEvento();
  const rowSlots = getEventoRowSlots();
  const [erro, setErro] = useState<string | null>(null);
  const [confirmando, setConfirmando] = useState(false);

  const form = useForm<NovoEventoInput>({
    resolver: zodResolver(novoEventoSchema),
    defaultValues: {
      titulo: evento.titulo,
      descricao: evento.descricao ?? '',
      inicio: paraInputLocal(evento.inicio),
      fim: paraInputLocal(evento.fim),
    },
  });

  async function salvar(values: NovoEventoInput) {
    setErro(null);
    try {
      await atualizar.mutateAsync({ id: evento.id, input: values });
      onClose();
    } catch (err) {
      setErro(extractMessage(err, 'Falha ao salvar'));
    }
  }

  async function remover() {
    setErro(null);
    try {
      await excluir.mutateAsync(evento.id);
      onClose();
    } catch (err) {
      setErro(extractMessage(err, 'Falha ao excluir'));
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
    >
      <div
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-md border bg-card p-6"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="mb-4 text-xl font-semibold">Evento</h2>
        <form onSubmit={form.handleSubmit(salvar)} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="d-titulo">Título</Label>
            <Input id="d-titulo" maxLength={200} {...form.register('titulo')} />
            {form.formState.errors.titulo && (
              <p className="text-xs text-destructive">{form.formState.errors.titulo.message}</p>
            )}
          </div>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="d-descricao">Descrição</Label>
            <Textarea id="d-descricao" rows={3} maxLength={2000} {...form.register('descricao')} />
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="d-inicio">Início</Label>
              <Input id="d-inicio" type="datetime-local" {...form.register('inicio')} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="d-fim">Fim</Label>
              <Input id="d-fim" type="datetime-local" {...form.register('fim')} />
              {form.formState.errors.fim && (
                <p className="text-xs text-destructive">{form.formState.errors.fim.message}</p>
              )}
            </div>
          </div>

          {rowSlots.length > 0 && (
            <div className="rounded-md border border-dashed p-3">
              {rowSlots.map((Slot, i) => (
                <Slot key={i} eventoId={evento.id} />
              ))}
            </div>
          )}

          {erro && <p className="text-sm text-destructive">{erro}</p>}

          <div className="flex items-center justify-between gap-2">
            {confirmando ? (
              <span className="flex items-center gap-2 text-sm">
                Excluir?
                <Button type="button" variant="destructive" size="sm" onClick={remover}>
                  Sim
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setConfirmando(false)}
                >
                  Não
                </Button>
              </span>
            ) : (
              <Button
                type="button"
                variant="destructive"
                size="sm"
                onClick={() => setConfirmando(true)}
              >
                Excluir
              </Button>
            )}
            <div className="flex gap-2">
              <Button type="button" variant="secondary" onClick={onClose}>
                Cancelar
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                Salvar
              </Button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
