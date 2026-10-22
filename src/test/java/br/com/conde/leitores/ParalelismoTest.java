package br.com.conde.leitores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Quanta leitura simultanea cada politica produz de verdade.
 *
 * <p>Contar estados nao serve: as tres politicas corretas alcancam exatamente os
 * mesmos estados. O que muda sao os caminhos, entao a medida tem que ser sobre
 * transicao: das vezes em que um leitor entra, em quantas ele entra em cima de
 * outro que ja estava lendo.
 */
@DisplayName("O paralelismo de leitura")
class ParalelismoTest {

    private double junta(int leitores, int escritores, Politica politica) {
        return new Explorador(Sistema.de(leitores, escritores, politica)).explorar().leituraJunta();
    }

    @Test
    @DisplayName("prefere ESCRITOR produz mais leitura simultanea que prefere LEITOR")
    void oContraintuitivo() {
        assertThat(junta(2, 1, Politica.PREFERE_ESCRITOR))
                .isGreaterThan(junta(2, 1, Politica.PREFERE_LEITOR));
        assertThat(junta(3, 1, Politica.PREFERE_ESCRITOR))
                .isGreaterThan(junta(3, 1, Politica.PREFERE_LEITOR));
        assertThat(junta(4, 1, Politica.PREFERE_ESCRITOR))
                .isGreaterThan(junta(4, 1, Politica.PREFERE_LEITOR));
    }

    @Test
    @DisplayName("a justa fica no meio, e mais perto da que prefere leitor")
    void aJustaFicaNoMeio() {
        double leitor = junta(3, 1, Politica.PREFERE_LEITOR);
        double escritor = junta(3, 1, Politica.PREFERE_ESCRITOR);
        double justa = junta(3, 1, Politica.JUSTA);

        assertThat(justa).isBetween(leitor, escritor);
        assertThat(justa - leitor).isLessThan(escritor - justa);
    }

    @Test
    @DisplayName("quanto mais leitor, mais leitura junta, em todas as politicas")
    void maisLeitorMaisJunta() {
        for (Politica politica : new Politica[]{Politica.PREFERE_LEITOR,
                Politica.PREFERE_ESCRITOR, Politica.JUSTA}) {
            assertThat(junta(2, 1, politica)).isLessThan(junta(3, 1, politica));
            assertThat(junta(3, 1, politica)).isLessThan(junta(4, 1, politica));
        }
    }

    @Test
    @DisplayName("acrescentar escritor derruba a leitura junta das tres politicas")
    void escritorAtrapalha() {
        for (Politica politica : new Politica[]{Politica.PREFERE_LEITOR,
                Politica.PREFERE_ESCRITOR, Politica.JUSTA}) {
            assertThat(junta(3, 2, politica)).isLessThanOrEqualTo(junta(3, 1, politica));
        }
    }

    @Test
    @DisplayName("os numeros publicados: 21,4%, 25,0% e 22,2% com dois leitores e um escritor")
    void numerosPublicados() {
        assertThat(junta(2, 1, Politica.PREFERE_LEITOR)).isCloseTo(0.214, within(0.001));
        assertThat(junta(2, 1, Politica.PREFERE_ESCRITOR)).isCloseTo(0.250, within(0.001));
        assertThat(junta(2, 1, Politica.JUSTA)).isCloseTo(0.222, within(0.001));
    }

    @Test
    @DisplayName("com um leitor so nao existe leitura junta em politica nenhuma")
    void umLeitorNuncaLeJunto() {
        for (Politica politica : Politica.values()) {
            assertThat(junta(1, 1, politica)).isZero();
        }
    }
}
