'use client';

import { notFound } from 'next/navigation';
import { hasFeature } from '@/lib/feature-flags';
import { NotificacoesPage } from '@/features/push-notif';

export default function Page() {
  // build.sh remove src/features/push-notif fisicamente em clientes sem ela;
  // este guard esconde a rota (404) quando a feature não está no manifesto.
  if (!hasFeature('push-notif')) {
    notFound();
  }
  return <NotificacoesPage />;
}
