'use client';

import { notFound } from 'next/navigation';
import { hasFeature } from '@/lib/feature-flags';
import { NotificacoesPage } from '@/features/notificacao';

export default function Page() {
  // build.sh remove src/features/notificacao fisicamente em clientes sem ela;
  // este guard esconde a rota (404) quando a feature não está no manifesto.
  if (!hasFeature('notificacao')) {
    notFound();
  }
  return <NotificacoesPage />;
}
