'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useState } from 'react';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { extractMessage } from '@/lib/http';
import { getCategoriaPanelSlots } from '@/core/shared/slots';
import { useCategorias, useCriarCategoria, useDeletarCategoria } from './hooks';

const schema = z.object({
  nome: z.string().min(1, 'Informe o nome').max(80),
  cor: z
    .string()
    .regex(/^#[0-9a-fA-F]{6}$/, 'Use formato #RRGGBB')
    .optional()
    .or(z.literal('')),
});
type FormInput = z.infer<typeof schema>;

export function CategoriasPage() {
  const { data: categorias, isLoading } = useCategorias();
  const criar = useCriarCategoria();
  const deletar = useDeletarCategoria();
  const [erro, setErro] = useState<string | null>(null);

  const form = useForm<FormInput>({
    resolver: zodResolver(schema),
    defaultValues: { nome: '', cor: '' },
  });

  async function onSubmit(values: FormInput) {
    setErro(null);
    try {
      await criar.mutateAsync({ nome: values.nome, cor: values.cor || undefined });
      form.reset();
    } catch (err) {
      setErro(extractMessage(err, 'Falha ao criar categoria'));
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold">Categorias</h1>

      <form
        onSubmit={form.handleSubmit(onSubmit)}
        className="flex flex-wrap items-end gap-3 rounded-md border bg-card p-4"
      >
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="nome">Nome</Label>
          <Input id="nome" className="w-56" {...form.register('nome')} />
          {form.formState.errors.nome && (
            <span className="text-xs text-destructive">{form.formState.errors.nome.message}</span>
          )}
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="cor">Cor (opcional)</Label>
          <Input id="cor" placeholder="#3b82f6" className="w-32" {...form.register('cor')} />
          {form.formState.errors.cor && (
            <span className="text-xs text-destructive">{form.formState.errors.cor.message}</span>
          )}
        </div>
        <Button type="submit" disabled={form.formState.isSubmitting}>
          Adicionar
        </Button>
        {erro && <span className="w-full text-sm text-destructive">{erro}</span>}
      </form>

      {isLoading && <p className="text-muted-foreground">Carregando…</p>}
      {categorias && categorias.length === 0 && (
        <p className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
          Nenhuma categoria ainda.
        </p>
      )}
      {categorias && categorias.length > 0 && (
        <ul className="divide-y rounded-md border bg-card">
          {categorias.map((c) => (
            <li key={c.id} className="flex items-center justify-between p-3">
              <span className="flex items-center gap-2">
                {c.cor && (
                  <span
                    className="inline-block h-4 w-4 rounded-full border"
                    style={{ backgroundColor: c.cor }}
                  />
                )}
                {c.nome}
              </span>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => deletar.mutate(c.id)}
                aria-label={`Excluir ${c.nome}`}
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </li>
          ))}
        </ul>
      )}

      {getCategoriaPanelSlots().map((Panel, i) => (
        <Panel key={i} />
      ))}
    </div>
  );
}
