/**
 * Composition root — importa o efeito-colateral de registro das features
 * contratadas. É o análogo ao component scan do Spring: aqui o "classpath"
 * é o conjunto de imports abaixo, e o build.sh remove a linha de uma feature
 * NÃO contratada (espelhando o Maven profile que tira o módulo do classpath).
 *
 * Em dev / build completo todas as linhas existem; hasFeature() ainda controla
 * rota/menu em runtime. Core importa só ESTE arquivo (core→core), nunca a
 * feature direto.
 */
import '@/features/categorias/register';
import '@/features/resumo-por-categoria/register';
import '@/features/recorrencia/register';
