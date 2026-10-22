package br.com.conde.leitores;

/**
 * Quem tem direito de entrar no recurso, e quando.
 *
 * <p>O problema dos leitores e escritores nao e sobre travas: e sobre <b>regra de
 * admissao</b>. Todas as politicas aqui respeitam a mesma lei fisica (escritor
 * sozinho, leitores juntos) e mesmo assim se comportam de um jeito completamente
 * diferente, porque o que muda e quem passa na frente de quem.
 *
 * <p>Por isso este repositorio nao tem semaforo nenhum. Semaforo e uma das
 * maneiras de implementar a regra; a regra em si e uma funcao do estado, e e ela
 * que decide se alguem passa fome.
 */
public enum Politica {

    /**
     * Sem regra nenhuma: quem quiser entra. Esta aqui para o explorador mostrar
     * que a lei fisica nao se cumpre sozinha.
     */
    LIVRE("livre") {
        @Override
        public boolean podeEntrar(Estado estado, int processo) {
            return true;
        }
    },

    /**
     * Primeiro problema de Courtois: leitor entra sempre que nao houver escritor
     * escrevendo, mesmo com escritor na fila. Maximiza a leitura simultanea.
     */
    PREFERE_LEITOR("prefere leitor") {
        @Override
        public boolean podeEntrar(Estado estado, int processo) {
            if (estado.ehLeitor(processo)) {
                return estado.escrevendo() == 0;
            }
            return estado.escrevendo() == 0 && estado.lendo() == 0;
        }
    },

    /**
     * Segundo problema de Courtois: escritor na fila fecha a porta para leitor
     * novo. Leitor que ja esta dentro termina; leitor que chegou depois espera.
     */
    PREFERE_ESCRITOR("prefere escritor") {
        @Override
        public boolean podeEntrar(Estado estado, int processo) {
            if (estado.ehLeitor(processo)) {
                return estado.escrevendo() == 0 && !estado.temEscritorNaFila();
            }
            return estado.escrevendo() == 0 && estado.lendo() == 0;
        }
    },

    /**
     * Ordem de chegada. Quem chegou primeiro entra primeiro, com uma folga: um
     * leitor passa junto com o leitor da frente, senao a leitura simultanea
     * morreria e a politica viraria uma fila de exclusao mutua.
     */
    JUSTA("justa") {
        @Override
        public boolean podeEntrar(Estado estado, int processo) {
            if (estado.ehLeitor(processo)) {
                return estado.escrevendo() == 0 && estado.soLeitoresNaFrente(processo);
            }
            return estado.escrevendo() == 0 && estado.lendo() == 0
                    && estado.primeiroDaFila() == processo;
        }
    };

    private final String rotulo;

    Politica(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** O processo esta na fila querendo entrar. Ele pode? */
    public abstract boolean podeEntrar(Estado estado, int processo);
}
