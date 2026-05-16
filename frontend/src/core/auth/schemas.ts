import { z } from 'zod';

export const registrarSchema = z.object({
  nome: z.string().min(1, 'Informe seu nome').max(120),
  email: z.string().email('Email inválido').max(160),
  senha: z.string().min(8, 'Mínimo 8 caracteres').max(100),
});

export const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  senha: z.string().min(1, 'Informe a senha'),
});

export type RegistrarInput = z.infer<typeof registrarSchema>;
export type LoginInput = z.infer<typeof loginSchema>;
