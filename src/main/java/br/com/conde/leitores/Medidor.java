package br.com.conde.leitores;

import java.util.List;
import java.util.Locale;

/**
 * As tabelas do README. Uso:
 * {@code mvn exec:java -Dexec.args="politicas"} (ou {@code inanicao}, {@code paralelismo}).
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String tabela = args.length > 0 ? args[0] : "tudo";
        boolean tudo = tabela.equals("tudo");
        if (tudo || tabela.equals("politicas")) {
            politicas();
        }
        if (tudo || tabela.equals("inanicao")) {
            inanicao();
        }
        if (tudo || tabela.equals("paralelismo")) {
            paralelismo();
        }
    }

    /** As quatro politicas, no mesmo tamanho de sistema. */
    public static void politicas() {
        System.out.println();
        System.out.println("POLITICAS  (2 leitores, 2 escritores)");
        System.out.printf(Locale.ROOT, "%-18s %8s %8s %10s %12s %10s%n",
                "politica", "estados", "arestas", "lei fisica", "inanicao", "max lendo");
        for (Politica politica : Politica.values()) {
            Sistema sistema = Sistema.de(2, 2, politica);
            Explorador.Resultado r = new Explorador(sistema).explorar();
            List<Inanicao.Achado> fome = r.segura()
                    ? new Inanicao(sistema, r).procurar() : List.of();
            System.out.printf(Locale.ROOT, "%-18s %8d %8d %10s %12s %10d%n",
                    politica.rotulo(), r.estados(), r.transicoes(),
                    r.segura() ? "ok" : "QUEBRA",
                    r.segura() ? (fome.isEmpty() ? "nao" : "SIM") : "-",
                    r.maximoDeLeitores());
        }
        Explorador.Resultado livre = new Explorador(Sistema.de(2, 2, Politica.LIVRE)).explorar();
        System.out.println();
        System.out.println("contraexemplo da politica livre:");
        for (String linha : livre.leiQuebrada()) {
            System.out.println("  " + linha);
        }
        System.out.println();
    }

    /** Quem passa fome em cada politica. */
    public static void inanicao() {
        System.out.println();
        System.out.println("INANICAO  (quem fica esperando para sempre, em cada politica)");
        System.out.printf(Locale.ROOT, "%-18s %-10s %s%n", "politica", "sistema", "passa fome");
        int[][] casos = {{1, 2}, {2, 1}, {2, 2}, {3, 1}, {1, 3}};
        for (Politica politica : List.of(Politica.PREFERE_LEITOR, Politica.PREFERE_ESCRITOR,
                Politica.JUSTA)) {
            for (int[] caso : casos) {
                Sistema sistema = Sistema.de(caso[0], caso[1], politica);
                Explorador.Resultado r = new Explorador(sistema).explorar();
                List<Inanicao.Achado> fome = new Inanicao(sistema, r).procurar();
                System.out.printf(Locale.ROOT, "%-18s %-10s %s%n",
                        politica.rotulo(), caso[0] + "L x " + caso[1] + "E",
                        fome.isEmpty() ? "-" : String.join(", ",
                                fome.stream().map(Inanicao.Achado::nome).toList()));
            }
        }
        System.out.println();
    }

    /** Quanta leitura simultanea cada politica produz de verdade. */
    public static void paralelismo() {
        System.out.println();
        System.out.println("PARALELISMO  (das vezes que um leitor entra, quantas ele entra EM CIMA de outro)");
        System.out.printf(Locale.ROOT, "%-18s %10s %10s %12s %12s%n",
                "politica", "2L x 1E", "3L x 1E", "3L x 2E", "4L x 1E");
        for (Politica politica : List.of(Politica.PREFERE_LEITOR, Politica.PREFERE_ESCRITOR,
                Politica.JUSTA)) {
            StringBuilder linha = new StringBuilder(String.format(Locale.ROOT, "%-18s", politica.rotulo()));
            for (int[] caso : new int[][]{{2, 1}, {3, 1}, {3, 2}, {4, 1}}) {
                Explorador.Resultado r = new Explorador(Sistema.de(caso[0], caso[1], politica)).explorar();
                linha.append(String.format(Locale.ROOT, " %9.1f%%", 100 * r.leituraJunta()));
            }
            System.out.println(linha);
        }
        System.out.println();
    }
}
