'use client';

import { notFound } from 'next/navigation';
import { hasFeature } from '@/lib/feature-flags';
import { RecorrenciaPage } from '@/features/recorrencia';

export default function Page() {
  // Em builds de cliente que não contrataram 'recorrencia', o build.sh remove
  // src/features/recorrencia fisicamente. Em dev/build completo o código existe;
  // este guard esconde a rota (404) quando a feature não está no manifesto.
  if (!hasFeature('recorrencia')) {
    notFound();
  }
  return <RecorrenciaPage />;
}
