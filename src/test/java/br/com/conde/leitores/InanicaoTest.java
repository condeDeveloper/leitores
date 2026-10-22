package br.com.conde.leitores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O achado simetrico: o lado preferido precisa de <b>dois</b> para matar de fome
 * o outro. Um sozinho tem que soltar o recurso entre uma volta e outra, e nessa
 * fresta o outro lado entra.
 */
@DisplayName("Quem espera para sempre")
class InanicaoTest {

    private List<String> famintos(int leitores, int escritores, Politica politica) {
        Sistema sistema = Sistema.de(leitores, escritores, politica);
        Explorador.Resultado r = new Explorador(sistema).explorar();
        return new Inanicao(sistema, r).procurar().stream().map(Inanicao.Achado::nome).toList();
    }

    @Test
    @DisplayName("prefere leitor: com DOIS leitores o escritor nunca entra")
    void dolsLeitoresMatamOEscritor() {
        assertThat(famintos(2, 1, Politica.PREFERE_LEITOR)).containsExactly("escritor1");
        assertThat(famintos(3, 1, Politica.PREFERE_LEITOR)).containsExactly("escritor1");
        assertThat(famintos(2, 2, Politica.PREFERE_LEITOR))
                .containsExactly("escritor1", "escritor2");
    }

    @Test
    @DisplayName("prefere leitor: com UM leitor so, ninguem passa fome")
    void umLeitorNaoBasta() {
        assertThat(famintos(1, 1, Politica.PREFERE_LEITOR)).isEmpty();
        assertThat(famintos(1, 2, Politica.PREFERE_LEITOR)).isEmpty();
        assertThat(famintos(1, 3, Politica.PREFERE_LEITOR)).isEmpty();
    }

    @Test
    @DisplayName("prefere escritor: e o espelho exato, precisa de DOIS escritores")
    void espelho() {
        assertThat(famintos(1, 2, Politica.PREFERE_ESCRITOR)).containsExactly("leitor1");
        assertThat(famintos(1, 3, Politica.PREFERE_ESCRITOR)).containsExactly("leitor1");
        assertThat(famintos(2, 2, Politica.PREFERE_ESCRITOR))
                .containsExactly("leitor1", "leitor2");

        assertThat(famintos(2, 1, Politica.PREFERE_ESCRITOR)).isEmpty();
        assertThat(famintos(3, 1, Politica.PREFERE_ESCRITOR)).isEmpty();
    }

    @Test
    @DisplayName("justa: ninguem passa fome, em nenhum tamanho de sistema")
    void justaNaoDeixaNinguemDeFora() {
        for (int leitores = 1; leitores <= 3; leitores++) {
            for (int escritores = 1; escritores <= 2; escritores++) {
                assertThat(famintos(leitores, escritores, Politica.JUSTA))
                        .describedAs("%dL x %dE", leitores, escritores)
                        .isEmpty();
            }
        }
    }

    @Test
    @DisplayName("quando passa fome, passa fome o lado inteiro: nao sobra um privilegiado")
    void oLadoInteiro() {
        assertThat(famintos(2, 2, Politica.PREFERE_LEITOR)).hasSize(2);
        assertThat(famintos(2, 2, Politica.PREFERE_ESCRITOR)).hasSize(2);
        assertThat(famintos(3, 2, Politica.PREFERE_LEITOR)).hasSize(2);
    }

    @Test
    @DisplayName("querer nao e passar fome: num estado em que ele pode entrar, a culpa seria do escalonador")
    void querendoNaoBasta() {
        Sistema sistema = Sistema.de(2, 1, Politica.PREFERE_LEITOR);
        Explorador.Resultado r = new Explorador(sistema).explorar();

        long querendo = r.grafo().keySet().stream()
                .filter(estado -> estado.fase(2) == Estado.QUERENDO).count();
        long bloqueado = r.grafo().keySet().stream()
                .filter(estado -> estado.fase(2) == Estado.QUERENDO
                        && !Politica.PREFERE_LEITOR.podeEntrar(estado, 2)).count();

        assertThat(querendo).isEqualTo(18);
        assertThat(bloqueado).isEqualTo(7);
        assertThat(new Inanicao(sistema, r).procurar()).hasSize(1);
    }
}
