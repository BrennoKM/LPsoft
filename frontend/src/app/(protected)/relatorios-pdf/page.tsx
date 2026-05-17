'use client';

import { notFound } from 'next/navigation';
import { hasFeature } from '@/lib/feature-flags';
import { RelatoriosPage } from '@/features/relatorios-pdf';

export default function Page() {
  // build.sh remove src/features/relatorios-pdf fisicamente em clientes sem
  // ela; este guard esconde a rota (404) quando não está no manifesto.
  if (!hasFeature('relatorios-pdf')) {
    notFound();
  }
  return <RelatoriosPage />;
}
