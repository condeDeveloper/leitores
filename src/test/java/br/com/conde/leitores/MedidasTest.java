package br.com.conde.leitores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Os numeros publicados no README, presos em teste. */
@DisplayName("Os numeros do README")
class MedidasTest {

    private Explorador.Resultado r(int leitores, int escritores, Politica politica) {
        return new Explorador(Sistema.de(leitores, escritores, politica)).explorar();
    }

    @Test
    @DisplayName("tabela das politicas: 2 leitores e 2 escritores")
    void tabelaDasPoliticas() {
        assertThat(r(2, 2, Politica.LIVRE).estados()).isEqualTo(168);
        assertThat(r(2, 2, Politica.LIVRE).transicoes()).isEqualTo(672);
        assertThat(r(2, 2, Politica.PREFERE_LEITOR).estados()).isEqualTo(134);
        assertThat(r(2, 2, Politica.PREFERE_LEITOR).transicoes()).isEqualTo(420);
        assertThat(r(2, 2, Politica.PREFERE_ESCRITOR).transicoes()).isEqualTo(308);
        assertThat(r(2, 2, Politica.JUSTA).transicoes()).isEqualTo(286);
    }

    @Test
    @DisplayName("a justa corta um terco dos caminhos da que prefere leitor")
    void aJustaCortaCaminhos() {
        double justa = r(2, 2, Politica.JUSTA).transicoes();
        double leitor = r(2, 2, Politica.PREFERE_LEITOR).transicoes();

        assertThat(1 - justa / leitor).isBetween(0.30, 0.34);
    }

    @Test
    @DisplayName("tabela da inanicao: quem passa fome em cada tamanho")
    void tabelaDaInanicao() {
        assertThat(fome(1, 2, Politica.PREFERE_LEITOR)).isZero();
        assertThat(fome(2, 1, Politica.PREFERE_LEITOR)).isEqualTo(1);
        assertThat(fome(2, 2, Politica.PREFERE_LEITOR)).isEqualTo(2);
        assertThat(fome(3, 1, Politica.PREFERE_LEITOR)).isEqualTo(1);

        assertThat(fome(1, 2, Politica.PREFERE_ESCRITOR)).isEqualTo(1);
        assertThat(fome(2, 1, Politica.PREFERE_ESCRITOR)).isZero();
        assertThat(fome(2, 2, Politica.PREFERE_ESCRITOR)).isEqualTo(2);
        assertThat(fome(1, 3, Politica.PREFERE_ESCRITOR)).isEqualTo(1);

        for (int[] caso : new int[][]{{1, 2}, {2, 1}, {2, 2}, {3, 1}, {1, 3}}) {
            assertThat(fome(caso[0], caso[1], Politica.JUSTA)).isZero();
        }
    }

    @Test
    @DisplayName("tabela do paralelismo: as tres politicas em quatro tamanhos")
    void tabelaDoParalelismo() {
        assertThat(pct(2, 1, Politica.PREFERE_LEITOR)).isEqualTo(21.4);
        assertThat(pct(3, 1, Politica.PREFERE_LEITOR)).isEqualTo(33.8);
        assertThat(pct(3, 2, Politica.PREFERE_LEITOR)).isEqualTo(29.5);
        assertThat(pct(4, 1, Politica.PREFERE_LEITOR)).isEqualTo(41.2);

        assertThat(pct(2, 1, Politica.PREFERE_ESCRITOR)).isEqualTo(25.0);
        assertThat(pct(3, 1, Politica.PREFERE_ESCRITOR)).isEqualTo(38.9);
        assertThat(pct(3, 2, Politica.PREFERE_ESCRITOR)).isEqualTo(38.9);
        assertThat(pct(4, 1, Politica.PREFERE_ESCRITOR)).isEqualTo(46.7);

        assertThat(pct(2, 1, Politica.JUSTA)).isEqualTo(22.2);
        assertThat(pct(3, 1, Politica.JUSTA)).isEqualTo(34.8);
        assertThat(pct(3, 2, Politica.JUSTA)).isEqualTo(30.5);
        assertThat(pct(4, 1, Politica.JUSTA)).isEqualTo(42.2);
    }

    @Test
    @DisplayName("o medidor roda inteiro sem lancar")
    void medidorRoda() {
        Medidor.politicas();
        Medidor.inanicao();
        Medidor.paralelismo();
    }

    private int fome(int leitores, int escritores, Politica politica) {
        Sistema sistema = Sistema.de(leitores, escritores, politica);
        return new Inanicao(sistema, new Explorador(sistema).explorar()).procurar().size();
    }

    private double pct(int leitores, int escritores, Politica politica) {
        return Math.round(1000 * r(leitores, escritores, politica).leituraJunta()) / 10.0;
    }
}
