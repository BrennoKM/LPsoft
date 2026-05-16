'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { LogOut } from 'lucide-react';
import { AuthGuard } from '@/core/auth/AuthGuard';
import { clearSession, getUsuario } from '@/core/auth/storage';
import { Button } from '@/components/ui/button';
import { clientDisplayName, hasFeature } from '@/lib/feature-flags';
// Composition root: dispara o registro das features contratadas (core→core).
import '@/core/shared/features';

export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();

  function sair() {
    clearSession();
    router.replace('/login');
  }

  return (
    <AuthGuard>
      <header className="border-b">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-6">
            <Link href="/eventos" className="text-lg font-semibold">
              {clientDisplayName}
            </Link>
            <nav className="flex items-center gap-4 text-sm">
              <Link href="/eventos" className="text-muted-foreground hover:text-foreground">
                Eventos
              </Link>
              {hasFeature('categorias') && (
                <Link href="/categorias" className="text-muted-foreground hover:text-foreground">
                  Categorias
                </Link>
              )}
              {hasFeature('recorrencia') && (
                <Link href="/recorrencia" className="text-muted-foreground hover:text-foreground">
                  Recorrência
                </Link>
              )}
              {hasFeature('analytics') && (
                <Link href="/analytics" className="text-muted-foreground hover:text-foreground">
                  Analytics
                </Link>
              )}
            </nav>
          </div>
          <UsuarioAtualLogado onSair={sair} />
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-6">{children}</main>
    </AuthGuard>
  );
}

function UsuarioAtualLogado({ onSair }: { onSair: () => void }) {
  const usuario = typeof window !== 'undefined' ? getUsuario() : null;
  return (
    <div className="flex items-center gap-3 text-sm text-muted-foreground">
      {usuario && <span>{usuario.email}</span>}
      <Button variant="ghost" size="sm" onClick={onSair}>
        <LogOut className="h-4 w-4" />
        Sair
      </Button>
    </div>
  );
}
