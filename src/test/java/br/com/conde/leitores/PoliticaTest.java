package br.com.conde.leitores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("As regras de admissao")
class PoliticaTest {

    private final Sistema sistema = Sistema.de(2, 2, Politica.PREFERE_LEITOR);

    private Estado comLeitorDentro() {
        return sistema.inicial().chegar(0).entrar(0);
    }

    @Test
    @DisplayName("prefere leitor: leitor entra em cima de leitor, escritor espera o recurso esvaziar")
    void prefereLeitor() {
        Estado estado = comLeitorDentro().chegar(1).chegar(2);

        assertThat(Politica.PREFERE_LEITOR.podeEntrar(estado, 1)).isTrue();
        assertThat(Politica.PREFERE_LEITOR.podeEntrar(estado, 2)).isFalse();
    }

    @Test
    @DisplayName("prefere leitor: escritor na fila nao segura leitor novo, e e dai que vem a fome")
    void escritorNaFilaNaoSegura() {
        Estado estado = comLeitorDentro().chegar(2).chegar(1);

        assertThat(Politica.PREFERE_LEITOR.podeEntrar(estado, 1)).isTrue();
    }

    @Test
    @DisplayName("prefere escritor: escritor na fila fecha a porta para leitor novo")
    void prefereEscritor() {
        Estado estado = comLeitorDentro().chegar(2).chegar(1);

        assertThat(Politica.PREFERE_ESCRITOR.podeEntrar(estado, 1)).isFalse();

        Estado semEscritor = comLeitorDentro().chegar(1);

        assertThat(Politica.PREFERE_ESCRITOR.podeEntrar(semEscritor, 1)).isTrue();
    }

    @Test
    @DisplayName("justa: o leitor passa se so houver leitor na frente dele")
    void justaDeixaLeitorEmBloco() {
        Estado doisLeitores = sistema.inicial().chegar(0).chegar(1);

        assertThat(Politica.JUSTA.podeEntrar(doisLeitores, 0)).isTrue();
        assertThat(Politica.JUSTA.podeEntrar(doisLeitores, 1)).isTrue();

        Estado escritorNaFrente = sistema.inicial().chegar(2).chegar(1);

        assertThat(Politica.JUSTA.podeEntrar(escritorNaFrente, 1)).isFalse();
        assertThat(Politica.JUSTA.podeEntrar(escritorNaFrente, 2)).isTrue();
    }

    @Test
    @DisplayName("justa: escritor so entra quando e o primeiro da fila e o recurso esta vazio")
    void justaOrdenaOEscritor() {
        Estado comLeitorNaFrente = sistema.inicial().chegar(1).chegar(2);

        assertThat(Politica.JUSTA.podeEntrar(comLeitorNaFrente, 2)).isFalse();

        Estado soEscritor = sistema.inicial().chegar(2);

        assertThat(Politica.JUSTA.podeEntrar(soEscritor, 2)).isTrue();
    }

    @Test
    @DisplayName("todas as politicas corretas recusam leitor enquanto alguem escreve")
    void ninguemLeDuranteEscrita() {
        Estado escrevendo = sistema.inicial().chegar(2).entrar(2).chegar(0);

        for (Politica politica : new Politica[]{Politica.PREFERE_LEITOR,
                Politica.PREFERE_ESCRITOR, Politica.JUSTA}) {
            assertThat(politica.podeEntrar(escrevendo, 0))
                    .describedAs("%s deixou ler durante escrita", politica.rotulo())
                    .isFalse();
        }
    }

    @Test
    @DisplayName("a politica livre deixa qualquer um entrar, e e para isso que ela existe")
    void livreDeixaTudo() {
        Estado escrevendo = sistema.inicial().chegar(2).entrar(2).chegar(0).chegar(3);

        assertThat(Politica.LIVRE.podeEntrar(escrevendo, 0)).isTrue();
        assertThat(Politica.LIVRE.podeEntrar(escrevendo, 3)).isTrue();
    }
}
