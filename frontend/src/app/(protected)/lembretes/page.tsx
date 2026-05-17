'use client';

import { notFound } from 'next/navigation';
import { hasFeature } from '@/lib/feature-flags';
import { LembretesPage } from '@/features/lembretes';

export default function Page() {
  // build.sh remove src/features/lembretes fisicamente em clientes sem ela;
  // este guard esconde a rota (404) quando a feature não está no manifesto.
  if (!hasFeature('lembretes')) {
    notFound();
  }
  return <LembretesPage />;
}
