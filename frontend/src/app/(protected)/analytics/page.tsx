'use client';

import { notFound } from 'next/navigation';
import { hasFeature } from '@/lib/feature-flags';
import { AnalyticsPage } from '@/features/analytics';

export default function Page() {
  // Em builds de cliente que não contrataram 'analytics', o build.sh remove
  // src/features/analytics fisicamente. Em dev/build completo o código existe;
  // este guard esconde a rota (404) quando a feature não está no manifesto.
  if (!hasFeature('analytics')) {
    notFound();
  }
  return <AnalyticsPage />;
}
