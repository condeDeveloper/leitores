# leitores

O problema dos leitores e escritores resolvido do zero em Java 21, com as três
políticas clássicas julgadas pela varredura de **todos os entrelaçamentos**.

Não há semáforo nenhum aqui, e isso é de propósito. O problema não é sobre
travas: é sobre **regra de admissão**. Todas as políticas respeitam a mesma lei
física (escritor fica sozinho, leitores entram juntos) e mesmo assim se comportam
de um jeito completamente diferente, porque o que muda é quem passa na frente de
quem. A regra é uma função do estado, e é ela que decide quem passa fome.

```
mvn -B -ntp verify
mvn -B -ntp exec:java -Dexec.args="politicas"   # (ou inanicao, paralelismo)
```

## As políticas

2 leitores, 2 escritores:

| política | estados | arestas | lei física | inanição | máx. lendo |
|---|---:|---:|---|---|---:|
| livre | 168 | 672 | **QUEBRA** | - | 2 |
| prefere leitor | 134 | 420 | ok | **SIM** | 2 |
| prefere escritor | 134 | 308 | ok | **SIM** | 2 |
| justa | 134 | 286 | ok | não | 2 |

Sem regra nenhuma a lei física não se cumpre sozinha, e bastam quatro passos:

```
leitor1:   chega
leitor1:   entra e le
escritor1: chega
escritor1: entra e escreve
=> escritor e leitor dentro ao mesmo tempo
```

**As três políticas corretas alcançam exatamente os mesmos 134 estados.** Estado
por estado, as mesmas configurações são possíveis. O que muda são as **arestas**:
420, 308 e 286. A política não decide onde o sistema pode estar, decide por quais
caminhos ele chega lá — e a justa corta um terço dos caminhos que a que prefere
leitor permite.

## Quem passa fome, e a simetria que eu não esperava

| política | 1L x 2E | 2L x 1E | 2L x 2E | 3L x 1E | 1L x 3E |
|---|---|---|---|---|---|
| prefere leitor | - | escritor1 | os dois escritores | escritor1 | - |
| prefere escritor | leitor1 | - | os dois leitores | - | leitor1 |
| justa | - | - | - | - | - |

**O lado preferido precisa de DOIS para matar de fome o outro.** Com um leitor
só, a política que prefere leitor não causa fome nenhuma: ele tem que soltar o
recurso entre uma volta e outra, e nessa fresta o escritor entra. São necessários
dois leitores se revezando — um entra antes do outro sair — para que o recurso
nunca fique vazio. O mesmo vale espelhado: um escritor sozinho não mata leitor
nenhum de fome.

A pergunta de inanição aqui não é sobre estado nenhum, é sobre **ciclo**: existe
uma volta que o sistema repete para sempre, com gente lendo e escrevendo, e um
processo parado na fila o tempo todo? E o ciclo só conta se for **justo**: todo
processo que em algum ponto dele podia andar precisa ter andado. Sem essa
exigência toda política "tem inanição", porque sempre dá para imaginar um
escalonador que ignora alguém de propósito.

Esse detalhe é mais importante do que parece. Num sistema de 2 leitores e 1
escritor, o escritor está na fila em **18** dos 33 estados — mas em 11 deles ele
*poderia* entrar. Ficar de fora nesses 11 seria culpa do escalonador, não da
regra. A inanição mora nos outros 7, e é só esse pedaço do grafo que o detector
olha.

## O paralelismo, que me pegou de surpresa

Das vezes em que um leitor entra, em quantas ele entra **em cima** de outro que
já estava lendo:

| política | 2L x 1E | 3L x 1E | 3L x 2E | 4L x 1E |
|---|---:|---:|---:|---:|
| prefere leitor | 21,4% | 33,8% | 29,5% | 41,2% |
| **prefere escritor** | **25,0%** | **38,9%** | **38,9%** | **46,7%** |
| justa | 22,2% | 34,8% | 30,5% | 42,2% |

**A política que prioriza o escritor produz mais leitura simultânea que a que
prioriza o leitor.** Em todos os tamanhos medidos.

O motivo, depois de olhar os caminhos: quando um escritor está na fila, a
política que o prefere **segura os leitores novos**. Eles se acumulam, e quando o
escritor termina, entram em bloco, um em cima do outro. Já a política que prefere
leitor deixa cada um entrar assim que chega, muitas vezes sozinho, e o recurso
passa boa parte do tempo com um leitor só dentro. Preferir leitor maximiza *a
chance de entrar*; preferir escritor acaba maximizando *a leitura junta*.

A justa fica entre as duas e mais perto da que prefere leitor.

## O que as medidas me corrigiram

1. **"Preferir leitor dá mais paralelismo de leitura."** Dá menos. Quem agrupa os
   leitores é justamente a política que os faz esperar.
2. **"Um leitor basta para matar o escritor de fome."** Não basta. Precisa de dois
   se revezando para o recurso nunca esvaziar. A fome não vem de alguém estar
   dentro, vem de nunca haver uma fresta.
3. **"Estar na fila querendo entrar já é inanição."** Em 11 dos 18 estados em que
   o escritor espera, ele podia ter entrado. Contar esses como fome transformaria
   em defeito da política o que é escolha do escalonador.
4. **"Políticas diferentes alcançam estados diferentes."** Alcançam exatamente os
   mesmos 134. Medir política contando estado não mede nada; é preciso contar
   transição.

## Como está organizado

- `Politica`: as quatro regras de admissão, cada uma uma função do estado.
- `Estado`: estado global imutável, com a fila de espera na ordem de chegada.
  Os processos são laços infinitos: sem laço não há ciclo, e sem ciclo não há
  como perguntar se alguém espera para sempre.
- `Maquina`: a transição. Quem está na fila sem permissão não gera transição.
- `Explorador`: a varredura, a lei física e a medida de leitura simultânea.
- `Inanicao`: componentes fortemente conexos (Tarjan iterativo) no pedaço do
  grafo em que o faminto está na fila **e bloqueado**, mais o teste de justiça.
- `Medidor`: as três tabelas, rodando no CI nos três sistemas.

41 testes.
