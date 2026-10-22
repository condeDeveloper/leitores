package br.com.conde.leitores;

import java.util.ArrayList;
import java.util.List;

/** A configuracao medida: quantos leitores, quantos escritores e qual politica. */
public record Sistema(int leitores, int escritores, Politica politica) {

    public Sistema {
        if (leitores < 1 || escritores < 1) {
            throw new IllegalArgumentException("precisa de pelo menos um de cada lado");
        }
    }

    public static Sistema de(int leitores, int escritores, Politica politica) {
        return new Sistema(leitores, escritores, politica);
    }

    public Sistema com(Politica outra) {
        return new Sistema(leitores, escritores, outra);
    }

    public int processos() {
        return leitores + escritores;
    }

    public boolean ehLeitor(int processo) {
        return processo < leitores;
    }

    public Estado inicial() {
        return Estado.inicial(leitores, processos());
    }

    public List<String> nomes() {
        List<String> nomes = new ArrayList<>();
        for (int i = 0; i < leitores; i++) {
            nomes.add("leitor" + (i + 1));
        }
        for (int i = 0; i < escritores; i++) {
            nomes.add("escritor" + (i + 1));
        }
        return nomes;
    }

    @Override
    public String toString() {
        return leitores + "L x " + escritores + "E, " + politica.rotulo();
    }
}
