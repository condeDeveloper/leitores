package br.com.conde.leitores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("O estado e a maquina")
class EstadoTest {

    private final Sistema sistema = Sistema.de(2, 2, Politica.JUSTA);

    @Test
    @DisplayName("comeca com todo mundo fora e a fila vazia")
    void comecaVazio() {
        Estado estado = sistema.inicial();

        assertThat(estado.processos()).isEqualTo(4);
        assertThat(estado.fila()).isEmpty();
        assertThat(estado.lendo()).isZero();
        assertThat(estado.escrevendo()).isZero();
    }

    @Test
    @DisplayName("os leitores sao os primeiros indices, e e so isso que define o papel")
    void papelPorIndice() {
        Estado estado = sistema.inicial();

        assertThat(estado.ehLeitor(0)).isTrue();
        assertThat(estado.ehLeitor(1)).isTrue();
        assertThat(estado.ehLeitor(2)).isFalse();
        assertThat(estado.ehLeitor(3)).isFalse();
    }

    @Test
    @DisplayName("chegar poe na fila na ordem, e entrar tira so quem entrou")
    void filaMantemAOrdem() {
        Estado estado = sistema.inicial().chegar(2).chegar(0).chegar(1);

        assertThat(estado.fila()).containsExactly(2, 0, 1);

        Estado depois = estado.entrar(0);

        assertThat(depois.fila()).containsExactly(2, 1);
        assertThat(depois.lendo()).isEqualTo(1);
    }

    @Test
    @DisplayName("sair nao mexe na fila de quem ainda espera")
    void sairNaoMexeNaFila() {
        Estado estado = sistema.inicial().chegar(0).entrar(0).chegar(1).chegar(2);

        Estado depois = estado.sair(0);

        assertThat(depois.fila()).containsExactly(1, 2);
        assertThat(depois.lendo()).isZero();
        assertThat(depois.fase(0)).isEqualTo(Estado.FORA);
    }

    @Test
    @DisplayName("a fila faz parte do estado: a mesma gente em outra ordem e outro estado")
    void ordemDaFilaImporta() {
        Estado um = sistema.inicial().chegar(0).chegar(2);
        Estado outro = sistema.inicial().chegar(2).chegar(0);

        assertThat(um).isNotEqualTo(outro);
    }

    @Test
    @DisplayName("no inicio todo mundo pode chegar, e so isso")
    void movimentosIniciais() {
        var movimentos = new Maquina(sistema).movimentos(sistema.inicial());

        assertThat(movimentos).hasSize(4);
        assertThat(movimentos).allMatch(movimento -> movimento.acao().equals("chega"));
    }

    @Test
    @DisplayName("quem esta na fila sem permissao nao gera transicao nenhuma")
    void bloqueadoNaoAnda() {
        Sistema prefere = Sistema.de(2, 1, Politica.PREFERE_LEITOR);
        Estado estado = prefere.inicial().chegar(0).entrar(0).chegar(2);

        var movimentos = new Maquina(prefere).movimentos(estado);

        assertThat(movimentos).extracting(Maquina.Movimento::processo).doesNotContain(2);
    }

    @Test
    @DisplayName("sistema sem um dos lados nao e o problema dos leitores e escritores")
    void recusaConfiguracaoSemSentido() {
        assertThatThrownBy(() -> Sistema.de(0, 2, Politica.JUSTA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sistema.de(2, 0, Politica.JUSTA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("o teto protege de configuracao grande demais")
    void teto() {
        Explorador apertado = new Explorador(Sistema.de(3, 3, Politica.JUSTA), 40);

        assertThatThrownBy(apertado::explorar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("teto de 40 estados");
    }
}
