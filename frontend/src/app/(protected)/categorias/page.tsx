'use client';

import { notFound } from 'next/navigation';
import { hasFeature } from '@/lib/feature-flags';
import { CategoriasPage } from '@/features/categorias';

export default function Page() {
  // Em builds de cliente que não contrataram 'categorias', o build.sh remove
  // src/features/categorias fisicamente. Em dev/build completo o código existe;
  // este guard esconde a rota (404) quando a feature não está no manifesto.
  if (!hasFeature('categorias')) {
    notFound();
  }
  return <CategoriasPage />;
}
