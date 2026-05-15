'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { getToken } from './storage';

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [pronto, setPronto] = useState(false);

  useEffect(() => {
    if (!getToken()) {
      router.replace('/login');
    } else {
      setPronto(true);
    }
  }, [router]);

  if (!pronto) {
    return <div className="p-8 text-slate-500">Verificando sessão…</div>;
  }
  return <>{children}</>;
}
