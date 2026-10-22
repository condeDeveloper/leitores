package br.com.conde.leitores;

import java.util.Arrays;

/**
 * O estado global: onde cada processo esta e quem esta usando o recurso.
 *
 * <p>Os processos sao lacos infinitos de proposito: chegam, entram, usam, saem e
 * voltam a chegar. Sem laco nao existiria ciclo no grafo, e sem ciclo nao daria
 * para perguntar se alguem espera <b>para sempre</b>, que e a pergunta que separa
 * as politicas.
 *
 * <p>Os processos sao numerados com os leitores primeiro, entao "ser leitor" e
 * uma comparacao de indice e nao um campo que precise ser carregado por ai.
 */
public final class Estado {

    /** Em que ponto do laco cada processo esta. */
    public static final int FORA = 0;
    public static final int QUERENDO = 1;
    public static final int DENTRO = 2;

    private final int leitores;
    private final int[] fase;
    private final int[] fila;
    private final int cacheHash;

    public Estado(int leitores, int[] fase, int[] fila) {
        this.leitores = leitores;
        this.fase = fase;
        this.fila = fila;
        this.cacheHash = 31 * Arrays.hashCode(fase) + Arrays.hashCode(fila);
    }

    public static Estado inicial(int leitores, int processos) {
        return new Estado(leitores, new int[processos], new int[0]);
    }

    public boolean ehLeitor(int processo) {
        return processo < leitores;
    }

    public int fase(int processo) {
        return fase[processo];
    }

    public int processos() {
        return fase.length;
    }

    public int[] fila() {
        return fila.clone();
    }

    public boolean dentro(int processo) {
        return fase[processo] == DENTRO;
    }

    /** Quantos estao dentro lendo. */
    public int lendo() {
        int conta = 0;
        for (int processo = 0; processo < leitores; processo++) {
            if (fase[processo] == DENTRO) {
                conta++;
            }
        }
        return conta;
    }

    /** Quantos estao dentro escrevendo. Mais de um aqui e a lei fisica quebrada. */
    public int escrevendo() {
        int conta = 0;
        for (int processo = leitores; processo < fase.length; processo++) {
            if (fase[processo] == DENTRO) {
                conta++;
            }
        }
        return conta;
    }

    public int primeiroDaFila() {
        return fila.length == 0 ? -1 : fila[0];
    }

    public boolean temEscritorNaFila() {
        for (int quem : fila) {
            if (!ehLeitor(quem)) {
                return true;
            }
        }
        return false;
    }

    /** Na frente deste processo so tem leitor esperando (ou ninguem). */
    public boolean soLeitoresNaFrente(int processo) {
        for (int quem : fila) {
            if (quem == processo) {
                return true;
            }
            if (!ehLeitor(quem)) {
                return false;
            }
        }
        return true;
    }

    public Estado chegar(int processo) {
        int[] novaFase = fase.clone();
        novaFase[processo] = QUERENDO;
        int[] novaFila = Arrays.copyOf(fila, fila.length + 1);
        novaFila[fila.length] = processo;
        return new Estado(leitores, novaFase, novaFila);
    }

    public Estado entrar(int processo) {
        int[] novaFase = fase.clone();
        novaFase[processo] = DENTRO;
        int[] novaFila = new int[fila.length - 1];
        int k = 0;
        for (int quem : fila) {
            if (quem != processo) {
                novaFila[k++] = quem;
            }
        }
        return new Estado(leitores, novaFase, novaFila);
    }

    public Estado sair(int processo) {
        int[] novaFase = fase.clone();
        novaFase[processo] = FORA;
        return new Estado(leitores, novaFase, fila);
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        return outro instanceof Estado e
                && Arrays.equals(fase, e.fase) && Arrays.equals(fila, e.fila);
    }

    @Override
    public int hashCode() {
        return cacheHash;
    }

    @Override
    public String toString() {
        return "fase=" + Arrays.toString(fase) + " fila=" + Arrays.toString(fila);
    }
}
