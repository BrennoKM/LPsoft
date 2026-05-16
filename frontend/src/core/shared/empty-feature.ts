/**
 * Stub usado pelo build.sh ao montar o pacote de um cliente: features não
 * contratadas têm sua pasta substituída por reexports deste módulo, removendo
 * o código real do bundle daquele cliente (corte físico, espelhando o backend).
 *
 * Em dev / build completo este módulo não é usado — todas as features estão
 * presentes e hasFeature() controla rota/menu em runtime.
 */
export const EmptyComponent = () => null;

const emptyFeature = { EmptyComponent };

export default emptyFeature;
