package br.com.conde.leitores;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Varre todos os estados alcancaveis e mede tres coisas: se a lei fisica se
 * cumpre, quanto de leitura simultanea a politica permite e qual o caminho ate o
 * primeiro defeito.
 *
 * <p>A lei fisica do problema e uma so: escritor fica sozinho. Leitor com leitor
 * pode, leitor com escritor nao, escritor com escritor muito menos.
 */
public final class Explorador {

    private final Sistema sistema;
    private final Maquina maquina;
    private final int teto;

    public Explorador(Sistema sistema) {
        this(sistema, 300_000);
    }

    public Explorador(Sistema sistema, int teto) {
        this.sistema = sistema;
        this.maquina = new Maquina(sistema);
        this.teto = teto;
    }

    public record Resultado(Sistema sistema, int estados, int transicoes,
                            int maximoDeLeitores, int entradasDeLeitor,
                            int entradasEmCima, List<String> leiQuebrada,
                            Map<Estado, List<Maquina.Movimento>> grafo) {

        public boolean segura() {
            return leiQuebrada == null;
        }

        /**
         * Das vezes em que um leitor entra, em quantas ele entra <b>em cima</b> de
         * outro que ja esta lendo.
         *
         * <p>Esta e a medida de paralelismo que separa as politicas, e nao a
         * contagem de estados: os estados alcancaveis sao os mesmos nas tres
         * politicas corretas. O que muda sao os caminhos permitidos entre eles.
         */
        public double leituraJunta() {
            return entradasDeLeitor == 0 ? 0 : (double) entradasEmCima / entradasDeLeitor;
        }
    }

    public Resultado explorar() {
        Estado inicial = sistema.inicial();
        Map<Estado, List<Maquina.Movimento>> grafo = new HashMap<>();
        Map<Estado, Estado> pai = new HashMap<>();
        Map<Estado, String> comoCheguei = new HashMap<>();
        Deque<Estado> aFazer = new ArrayDeque<>();
        grafo.put(inicial, null);
        pai.put(inicial, null);
        aFazer.add(inicial);

        int transicoes = 0;
        int maximoDeLeitores = 0;
        int entradasDeLeitor = 0;
        int entradasEmCima = 0;
        List<String> quebrada = null;

        while (!aFazer.isEmpty()) {
            Estado atual = aFazer.poll();
            List<Maquina.Movimento> daqui = maquina.movimentos(atual);
            grafo.put(atual, daqui);
            transicoes += daqui.size();

            int lendo = atual.lendo();
            int escrevendo = atual.escrevendo();
            maximoDeLeitores = Math.max(maximoDeLeitores, lendo);
            for (Maquina.Movimento movimento : daqui) {
                if (movimento.acao().equals("entra e le")) {
                    entradasDeLeitor++;
                    if (lendo > 0) {
                        entradasEmCima++;
                    }
                }
            }
            if (quebrada == null && (escrevendo > 1 || (escrevendo > 0 && lendo > 0))) {
                quebrada = traco(atual, pai, comoCheguei, escrevendo > 1
                        ? escrevendo + " escritores dentro ao mesmo tempo"
                        : "escritor e leitor dentro ao mesmo tempo");
            }

            for (Maquina.Movimento movimento : daqui) {
                Estado destino = movimento.destino();
                if (!grafo.containsKey(destino)) {
                    if (grafo.size() >= teto) {
                        throw new IllegalStateException("passou do teto de " + teto + " estados");
                    }
                    grafo.put(destino, null);
                    pai.put(destino, atual);
                    comoCheguei.put(destino, sistema.nomes().get(movimento.processo())
                            + ": " + movimento.acao());
                    aFazer.add(destino);
                }
            }
        }

        return new Resultado(sistema, grafo.size(), transicoes, maximoDeLeitores,
                entradasDeLeitor, entradasEmCima, quebrada, grafo);
    }

    private List<String> traco(Estado alvo, Map<Estado, Estado> pai,
                               Map<Estado, String> comoCheguei, String veredito) {
        List<String> caminho = new ArrayList<>();
        Estado passo = alvo;
        while (pai.get(passo) != null) {
            caminho.add(0, comoCheguei.get(passo));
            passo = pai.get(passo);
        }
        caminho.add("=> " + veredito);
        return caminho;
    }
}
