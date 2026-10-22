package br.com.conde.leitores;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Procura quem pode esperar para sempre.
 *
 * <p>A pergunta nao e sobre estado nenhum, e sobre <b>ciclo</b>: existe uma volta
 * que o sistema pode repetir infinitamente, com gente lendo e escrevendo, e um
 * processo especifico parado na fila o tempo todo?
 *
 * <p>A volta so conta se for <b>justa</b>: todo processo que em algum ponto dela
 * podia andar precisa ter andado. Sem essa exigencia toda politica "tem
 * inanicao", porque sempre da para imaginar um escalonador que ignora alguem de
 * proposito. O que interessa e o caso em que ninguem e ignorado e mesmo assim
 * alguem fica de fora, porque a propria regra de admissao nunca o deixa entrar.
 */
public final class Inanicao {

    private final Sistema sistema;
    private final Explorador.Resultado resultado;

    public Inanicao(Sistema sistema, Explorador.Resultado resultado) {
        this.sistema = sistema;
        this.resultado = resultado;
    }

    public record Achado(int processo, String nome, int tamanhoDoCiclo) {
    }

    public List<Achado> procurar() {
        List<Achado> achados = new ArrayList<>();
        for (int processo = 0; processo < sistema.processos(); processo++) {
            Achado achado = procurarPara(processo);
            if (achado != null) {
                achados.add(achado);
            }
        }
        return achados;
    }

    private Achado procurarPara(int faminto) {
        // So interessa o pedaco do grafo em que o faminto esta na fila E a politica
        // nao o deixa entrar. Querer nao basta: num estado em que ele PODE entrar,
        // ficar de fora seria culpa do escalonador, nao da regra de admissao.
        Set<Estado> permitidos = new HashSet<>();
        for (Estado estado : resultado.grafo().keySet()) {
            if (estado.fase(faminto) == Estado.QUERENDO
                    && !sistema.politica().podeEntrar(estado, faminto)) {
                permitidos.add(estado);
            }
        }
        if (permitidos.isEmpty()) {
            return null;
        }
        for (List<Estado> componente : componentes(permitidos)) {
            if (temCiclo(componente, permitidos) && justo(componente, faminto)) {
                return new Achado(faminto, sistema.nomes().get(faminto), componente.size());
            }
        }
        return null;
    }

    private List<List<Estado>> componentes(Set<Estado> permitidos) {
        Map<Estado, Integer> indice = new HashMap<>();
        Map<Estado, Integer> menor = new HashMap<>();
        Set<Estado> naPilha = new HashSet<>();
        Deque<Estado> pilha = new ArrayDeque<>();
        List<List<Estado>> saida = new ArrayList<>();
        int[] contador = {0};

        for (Estado raiz : permitidos) {
            if (indice.containsKey(raiz)) {
                continue;
            }
            Deque<Estado> caminho = new ArrayDeque<>();
            Deque<Integer> posicao = new ArrayDeque<>();
            caminho.push(raiz);
            posicao.push(0);
            indice.put(raiz, contador[0]);
            menor.put(raiz, contador[0]++);
            pilha.push(raiz);
            naPilha.add(raiz);

            while (!caminho.isEmpty()) {
                Estado atual = caminho.peek();
                List<Estado> vizinhos = vizinhos(atual, permitidos);
                int i = posicao.pop();
                if (i < vizinhos.size()) {
                    posicao.push(i + 1);
                    Estado proximo = vizinhos.get(i);
                    if (!indice.containsKey(proximo)) {
                        indice.put(proximo, contador[0]);
                        menor.put(proximo, contador[0]++);
                        pilha.push(proximo);
                        naPilha.add(proximo);
                        caminho.push(proximo);
                        posicao.push(0);
                    } else if (naPilha.contains(proximo)) {
                        menor.put(atual, Math.min(menor.get(atual), indice.get(proximo)));
                    }
                } else {
                    caminho.pop();
                    if (!caminho.isEmpty()) {
                        Estado anterior = caminho.peek();
                        menor.put(anterior, Math.min(menor.get(anterior), menor.get(atual)));
                    }
                    if (menor.get(atual).equals(indice.get(atual))) {
                        List<Estado> componente = new ArrayList<>();
                        Estado quem;
                        do {
                            quem = pilha.pop();
                            naPilha.remove(quem);
                            componente.add(quem);
                        } while (!quem.equals(atual));
                        saida.add(componente);
                    }
                }
            }
        }
        return saida;
    }

    private List<Estado> vizinhos(Estado estado, Set<Estado> permitidos) {
        List<Estado> saida = new ArrayList<>();
        for (Maquina.Movimento movimento : resultado.grafo().get(estado)) {
            if (permitidos.contains(movimento.destino())) {
                saida.add(movimento.destino());
            }
        }
        return saida;
    }

    private boolean temCiclo(List<Estado> componente, Set<Estado> permitidos) {
        if (componente.size() > 1) {
            return true;
        }
        Estado unico = componente.get(0);
        return vizinhos(unico, permitidos).contains(unico);
    }

    private boolean justo(List<Estado> componente, int faminto) {
        Set<Estado> dentro = new HashSet<>(componente);
        Set<Integer> podiaAndar = new HashSet<>();
        Set<Integer> andou = new HashSet<>();
        for (Estado estado : componente) {
            for (Maquina.Movimento movimento : resultado.grafo().get(estado)) {
                podiaAndar.add(movimento.processo());
                if (dentro.contains(movimento.destino())) {
                    andou.add(movimento.processo());
                }
            }
        }
        if (podiaAndar.contains(faminto)) {
            return false;
        }
        podiaAndar.removeAll(andou);
        return podiaAndar.isEmpty() && !andou.isEmpty();
    }
}
