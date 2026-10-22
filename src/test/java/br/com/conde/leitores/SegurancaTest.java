package br.com.conde.leitores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A lei fisica: escritor fica sozinho")
class SegurancaTest {

    @ParameterizedTest
    @EnumSource(value = Politica.class, names = {"PREFERE_LEITOR", "PREFERE_ESCRITOR", "JUSTA"})
    @DisplayName("em nenhuma ordem de passos um escritor divide o recurso")
    void asTresPoliticasSegurem(Politica politica) {
        for (int leitores = 1; leitores <= 3; leitores++) {
            for (int escritores = 1; escritores <= 2; escritores++) {
                Explorador.Resultado r = new Explorador(
                        Sistema.de(leitores, escritores, politica)).explorar();

                assertThat(r.segura())
                        .describedAs("%dL x %dE com %s", leitores, escritores, politica.rotulo())
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("sem regra de admissao a lei nao se cumpre sozinha, e bastam quatro passos")
    void semRegraQuebra() {
        Explorador.Resultado r = new Explorador(Sistema.de(2, 2, Politica.LIVRE)).explorar();

        assertThat(r.segura()).isFalse();
        assertThat(r.leiQuebrada()).hasSize(5);
        assertThat(r.leiQuebrada().get(4)).contains("escritor e leitor dentro");
    }

    @Test
    @DisplayName("leitor junto com leitor e permitido, e as politicas usam isso")
    void leitorComLeitorPode() {
        for (Politica politica : new Politica[]{Politica.PREFERE_LEITOR,
                Politica.PREFERE_ESCRITOR, Politica.JUSTA}) {
            Explorador.Resultado r = new Explorador(Sistema.de(3, 1, politica)).explorar();

            assertThat(r.maximoDeLeitores())
                    .describedAs("%s nao deixou tres lerem juntos", politica.rotulo())
                    .isEqualTo(3);
        }
    }

    @Test
    @DisplayName("as tres politicas corretas alcancam exatamente os mesmos estados")
    void mesmosEstados() {
        int comLeitor = new Explorador(Sistema.de(2, 2, Politica.PREFERE_LEITOR)).explorar().estados();
        int comEscritor = new Explorador(Sistema.de(2, 2, Politica.PREFERE_ESCRITOR)).explorar().estados();
        int justa = new Explorador(Sistema.de(2, 2, Politica.JUSTA)).explorar().estados();

        assertThat(comLeitor).isEqualTo(comEscritor).isEqualTo(justa).isEqualTo(134);
    }

    @Test
    @DisplayName("o que muda entre elas sao os caminhos, nao os estados")
    void mudamAsArestas() {
        int comLeitor = new Explorador(Sistema.de(2, 2, Politica.PREFERE_LEITOR)).explorar().transicoes();
        int comEscritor = new Explorador(Sistema.de(2, 2, Politica.PREFERE_ESCRITOR)).explorar().transicoes();
        int justa = new Explorador(Sistema.de(2, 2, Politica.JUSTA)).explorar().transicoes();

        assertThat(comLeitor).isEqualTo(420);
        assertThat(comEscritor).isEqualTo(308);
        assertThat(justa).isEqualTo(286);
        assertThat(justa).isLessThan(comEscritor).isLessThan(comLeitor);
    }

    @Test
    @DisplayName("a politica livre tem mais estados: ela alcanca os proibidos tambem")
    void livreAlcancaOProibido() {
        Explorador.Resultado livre = new Explorador(Sistema.de(2, 2, Politica.LIVRE)).explorar();

        assertThat(livre.estados()).isEqualTo(168);
        assertThat(livre.estados()).isGreaterThan(134);
    }
}
