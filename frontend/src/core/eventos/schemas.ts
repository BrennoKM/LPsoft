import { z } from 'zod';

export const novoEventoSchema = z
  .object({
    titulo: z.string().min(1, 'Informe o título').max(200),
    descricao: z.string().max(2000).optional().or(z.literal('')),
    inicio: z.string().min(1, 'Informe o início'),
    fim: z.string().min(1, 'Informe o fim'),
  })
  .refine((data) => new Date(data.fim) > new Date(data.inicio), {
    message: 'Fim deve ser posterior ao início',
    path: ['fim'],
  });

export type NovoEventoInput = z.infer<typeof novoEventoSchema>;
