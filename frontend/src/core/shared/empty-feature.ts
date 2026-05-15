/**
 * Módulo substituído pelo bundler quando uma feature está desativada no manifesto.
 * Permite que imports `@/features/<nome>` compilem mesmo sem a feature presente.
 */
const emptyFeature = null;

export const EmptyComponent = () => null;

export default emptyFeature;
