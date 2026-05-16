package io.lpsoft.core.shared.spi;

import java.util.List;

/**
 * Ponto de extensão (SPI) do core: features podem contribuir seções a
 * relatórios sem que core ou consumidores as conheçam em tempo de compilação.
 *
 * Quem consome injeta {@code List<SecaoRelatorio>} — Spring entrega todas as
 * implementações presentes no classpath (lista vazia se nenhuma). É o que
 * torna a integração com 'analytics' OPCIONAL: sem o módulo, nenhuma seção;
 * com ele, a seção aparece. Sem acoplamento Maven entre as features.
 */
public interface SecaoRelatorio {

    String titulo();

    List<String> linhas();
}
