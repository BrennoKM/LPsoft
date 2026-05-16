'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { login, registrar } from '@/core/auth/api';
import { registrarSchema, type RegistrarInput } from '@/core/auth/schemas';
import { setSession } from '@/core/auth/storage';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { extractMessage } from '@/lib/http';

export default function RegistrarPage() {
  const router = useRouter();
  const [erro, setErro] = useState<string | null>(null);

  const form = useForm<RegistrarInput>({
    resolver: zodResolver(registrarSchema),
    defaultValues: { nome: '', email: '', senha: '' },
  });

  async function onSubmit(values: RegistrarInput) {
    setErro(null);
    try {
      await registrar(values);
      const resp = await login({ email: values.email, senha: values.senha });
      setSession(resp.token, { id: resp.usuarioId, email: resp.email });
      router.push('/eventos');
    } catch (err) {
      setErro(extractMessage(err, 'Falha no registro'));
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-4">
      <h1 className="mb-6 text-2xl font-semibold">Criar conta</h1>
      <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="nome">Nome</Label>
          <Input id="nome" autoComplete="name" {...form.register('nome')} />
          {form.formState.errors.nome && (
            <p className="text-xs text-destructive">{form.formState.errors.nome.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" autoComplete="email" {...form.register('email')} />
          {form.formState.errors.email && (
            <p className="text-xs text-destructive">{form.formState.errors.email.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="senha">Senha</Label>
          <Input
            id="senha"
            type="password"
            autoComplete="new-password"
            {...form.register('senha')}
          />
          {form.formState.errors.senha && (
            <p className="text-xs text-destructive">{form.formState.errors.senha.message}</p>
          )}
        </div>
        {erro && <p className="text-sm text-destructive">{erro}</p>}
        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? 'Criando…' : 'Criar conta'}
        </Button>
      </form>
      <p className="mt-4 text-sm text-muted-foreground">
        Já tem conta?{' '}
        <Link href="/login" className="font-medium text-foreground underline">
          Entrar
        </Link>
      </p>
    </div>
  );
}
