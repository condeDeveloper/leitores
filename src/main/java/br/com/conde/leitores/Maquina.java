package br.com.conde.leitores;

import java.util.ArrayList;
import java.util.List;

/**
 * A transicao: cada processo roda o laco chega, entra, usa, sai.
 *
 * <p>"Entrar" so acontece se a politica deixar. Quando ela nao deixa, nao existe
 * transicao: o processo fica na fila sem progredir, que e exatamente o que o
 * explorador precisa para perguntar se ele pode ficar assim para sempre.
 */
public final class Maquina {

    private final Sistema sistema;

    public Maquina(Sistema sistema) {
        this.sistema = sistema;
    }

    public record Movimento(int processo, String acao, Estado destino) {
    }

    public List<Movimento> movimentos(Estado estado) {
        List<Movimento> saida = new ArrayList<>();
        for (int processo = 0; processo < sistema.processos(); processo++) {
            switch (estado.fase(processo)) {
                case Estado.FORA -> saida.add(new Movimento(processo, "chega", estado.chegar(processo)));
                case Estado.QUERENDO -> {
                    if (sistema.politica().podeEntrar(estado, processo)) {
                        saida.add(new Movimento(processo,
                                sistema.ehLeitor(processo) ? "entra e le" : "entra e escreve",
                                estado.entrar(processo)));
                    }
                }
                case Estado.DENTRO -> saida.add(new Movimento(processo, "sai", estado.sair(processo)));
                default -> throw new IllegalStateException("fase desconhecida");
            }
        }
        return saida;
    }

    public Estado aplicar(Estado estado, int processo) {
        for (Movimento movimento : movimentos(estado)) {
            if (movimento.processo() == processo) {
                return movimento.destino();
            }
        }
        throw new IllegalStateException("processo " + processo + " nao pode andar em " + estado);
    }
}
