'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useCriarEvento } from '@/core/eventos/hooks';
import { novoEventoSchema, type NovoEventoInput } from '@/core/eventos/schemas';
import { extractMessage } from '@/lib/http';

export default function NovoEventoPage() {
  const router = useRouter();
  const criar = useCriarEvento();
  const [erro, setErro] = useState<string | null>(null);

  const form = useForm<NovoEventoInput>({
    resolver: zodResolver(novoEventoSchema),
    defaultValues: { titulo: '', descricao: '', inicio: '', fim: '' },
  });

  async function onSubmit(values: NovoEventoInput) {
    setErro(null);
    try {
      await criar.mutateAsync(values);
      router.push('/eventos');
    } catch (err) {
      setErro(extractMessage(err, 'Falha ao criar evento'));
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-6 text-2xl font-semibold">Novo evento</h1>
      <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="titulo">Título</Label>
          <Input id="titulo" maxLength={200} {...form.register('titulo')} />
          {form.formState.errors.titulo && (
            <p className="text-xs text-destructive">{form.formState.errors.titulo.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="descricao">Descrição</Label>
          <Textarea id="descricao" rows={3} maxLength={2000} {...form.register('descricao')} />
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="inicio">Início</Label>
            <Input id="inicio" type="datetime-local" {...form.register('inicio')} />
            {form.formState.errors.inicio && (
              <p className="text-xs text-destructive">{form.formState.errors.inicio.message}</p>
            )}
          </div>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="fim">Fim</Label>
            <Input id="fim" type="datetime-local" {...form.register('fim')} />
            {form.formState.errors.fim && (
              <p className="text-xs text-destructive">{form.formState.errors.fim.message}</p>
            )}
          </div>
        </div>
        {erro && <p className="text-sm text-destructive">{erro}</p>}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" type="button" onClick={() => router.back()}>
            Cancelar
          </Button>
          <Button type="submit" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? 'Criando…' : 'Criar'}
          </Button>
        </div>
      </form>
    </div>
  );
}
