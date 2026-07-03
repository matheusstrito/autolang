# AutoLang

DSL declarativa para **definir Autômatos Finitos Determinísticos (AFD)** e **simular a aceitação de cadeias**, desenvolvida como trabalho final (T6) da disciplina **Construção de Compiladores** — DC/UFSCar.

## Integrantes

- Matheus Marangoni Salomão
- Leonardo Poloni Berti Moríkio
- João Lucas Gomes Pelegrino

## Vídeo demonstrativo

Uma demonstração curta da linguagem e do compilador em funcionamento está disponível em:

**https://youtu.be/dns92Mo7wn8**

---

## Sobre a linguagem

AutoLang é uma linguagem **declarativa de domínio específico**: em vez de descrever *como* computar algo passo a passo, o usuário **declara** um autômato finito (seu alfabeto, estados, estado inicial, estados finais e transições) e, opcionalmente, um conjunto de cadeias a serem testadas. O compilador valida o autômato e, em seguida, **simula** cada cadeia, dizendo se é **aceita** ou **rejeitada** e mostrando o caminho percorrido entre os estados.

Esse é um domínio clássico da teoria da computação e se encaixa na proposta do T6 (linguagem pequena, declarativa, para uma aplicação específica), em vez de uma linguagem de programação de propósito geral.

### Por que isso é um "compilador"

O processamento de um arquivo `.auto` percorre as mesmas etapas de um compilador tradicional:

1. **Análise léxica** — quebra o texto em tokens (palavras-chave, identificadores, símbolos `--`, `-->`, chaves, cadeias). Gerada pelo ANTLR a partir da gramática.
2. **Análise sintática** — verifica se os blocos (`alfabeto`, `estados`, `inicial`, `finais`, `transicoes`, `testar`) estão na forma correta, produzindo a árvore sintática. Gerada pelo ANTLR.
3. **Análise semântica** — verificações que a gramática sozinha não garante (ver lista abaixo). Implementada percorrendo a árvore com o *Visitor* do ANTLR.
4. **Interpretação** — simula as cadeias de teste sobre o autômato já validado.

### Exemplo de programa

```
// AFD que aceita cadeias com numero PAR de 'a' (b's livres)
automato ParidadeA {
    alfabeto { a, b }
    estados  { q0, q1 }
    inicial  q0
    finais   { q0 }
    transicoes {
        q0 -- a --> q1
        q1 -- a --> q0
        q0 -- b --> q0
        q1 -- b --> q1
    }
    testar {
        "aa"    aceita
        "ab"    rejeita
        "abba"  aceita
    }
}
```

Saída (resumida):

```
Automato: ParidadeA
...
Cadeia "aa": obtido=aceita, esperado=aceita -> OK
   caminho: (q0) --a--> (q1) --a--> (q0)  [final: ACEITA]
Cadeia "ab": obtido=rejeita, esperado=rejeita -> OK
   caminho: (q0) --a--> (q1) --b--> (q1)  [não-final: REJEITA]
...
Resultado: 3/3 testes corretos
```

---

## Estrutura da linguagem

| Bloco | Obrigatório | Descrição |
|-------|-------------|-----------|
| `automato NOME { ... }` | sim | Envolve toda a definição. |
| `alfabeto { ... }` | sim | Símbolos válidos (um caractere cada). |
| `estados { ... }` | sim | Conjunto de estados. |
| `inicial Q` | sim | Estado inicial (deve estar em `estados`). |
| `finais { ... }` | sim | Estados de aceitação (pode ser vazio: `{ }`). |
| `transicoes { ... }` | sim | Lista de `origem -- simbolo --> destino`. |
| `testar { ... }` | não | Cadeias a simular, com resultado esperado (`aceita`/`rejeita`). |

Comentários de linha usam `//`.

---

## Análise semântica (verificações de conformidade)

São **6 verificações** além da gramática:

1. **VS1 — Estados duplicados:** nenhum estado pode ser declarado duas vezes.
2. **VS2 — Símbolos duplicados:** nenhum símbolo pode repetir no alfabeto.
3. **VS3 — Estado inicial válido:** o estado inicial deve constar em `estados`.
4. **VS4 — Estados finais válidos:** todo estado final deve constar em `estados`.
5. **VS5 — Transições consistentes:** em cada transição, origem e destino devem ser estados declarados e o símbolo deve pertencer ao alfabeto.
6. **VS6 — Determinismo:** não pode haver duas transições com o mesmo par (origem, símbolo) — isso violaria a definição de AFD.

Além disso, é emitido um **aviso** (não-fatal) para **estados inalcançáveis** a partir do estado inicial.

---

## Como compilar o compilador

**Pré-requisitos:** Java (JDK 11+ ) e Maven.

```bash
mvn clean package
```

Isso lê a gramática `AutoLang.g4`, gera o lexer/parser com o ANTLR, compila as classes Java e produz o JAR executável em:

```
target/autolang-1.0-SNAPSHOT-jar-with-dependencies.jar
```

> Observação: o projeto usa o `antlr4-maven-plugin` (mesma configuração de um projeto ANTLR padrão). Se o ambiente não tiver acesso ao Maven Central, é possível gerar e compilar manualmente com o `antlr-4.12.0-complete.jar` (ver seção "Build manual" abaixo).

---

## Como usar

```bash
# imprime o resultado na tela
java -jar target/autolang-1.0-SNAPSHOT-jar-with-dependencies.jar entrada.auto

# grava o resultado em um arquivo
java -jar target/autolang-1.0-SNAPSHOT-jar-with-dependencies.jar entrada.auto saida.txt
```

---

## Casos de teste

A pasta `casos-de-teste/entrada/` contém 15 exemplos cobrindo todas as etapas; as saídas correspondentes estão em `casos-de-teste/saida/`.

| Arquivo | O que demonstra |
|---------|-----------------|
| `01-valido_paridade` | Programa válido completo + simulação. |
| `02-lexico_cadeia_nao_fechada` | Erro léxico: cadeia sem fechar aspas. |
| `03-lexico_simbolo_invalido` | Erro léxico: caractere inválido. |
| `04-sintatico_sem_inicial` | Erro sintático: bloco obrigatório ausente. |
| `05-sintatico_chave_aberta` | Erro sintático: chave não fechada. |
| `06-semantico_vs1_estado_duplicado` | VS1. |
| `07-semantico_vs2_simbolo_duplicado` | VS2. |
| `08-semantico_vs3_inicial_invalido` | VS3. |
| `09-semantico_vs4_final_invalido` | VS4. |
| `10-semantico_vs5_transicao_invalida` | VS5 (símbolo e estado). |
| `11-semantico_vs6_nao_deterministico` | VS6 (determinismo). |
| `12-aviso_estado_inalcancavel` | Aviso de inalcançabilidade + interpretação. |
| `13-exec_termina_ab` | Simulação: cadeias terminadas em "ab". |
| `14-exec_binario_div3` | Simulação: binários divisíveis por 3. |
| `15-exec_sem_transicao` | Simulação: rejeição por transição indefinida. |

Para rodar todos de uma vez:

```bash
for f in casos-de-teste/entrada/*.auto; do
  java -jar target/autolang-1.0-SNAPSHOT-jar-with-dependencies.jar "$f"
  echo "----"
done
```

---

## Build manual (sem Maven Central)

```bash
# 1. gerar lexer/parser/visitor
java -jar antlr-4.12.0-complete.jar -visitor -listener \
  -package br.ufscar.dc.compiladores \
  -o target/generated-sources \
  src/main/antlr4/br/ufscar/dc/compiladores/AutoLang.g4

# 2. compilar
javac -encoding UTF-8 -cp antlr-4.12.0-complete.jar -d target/classes \
  target/generated-sources/.../AutoLang*.java \
  src/main/java/br/ufscar/dc/compiladores/autolang/*.java

# 3. executar
java -cp "antlr-4.12.0-complete.jar:target/classes" \
  br.ufscar.dc.compiladores.autolang.Principal entrada.auto
```

---

## Organização do código

```
src/main/antlr4/.../AutoLang.g4          gramática (léxico + sintático)
src/main/java/.../autolang/
    Principal.java            ponto de entrada (pipeline completo)
    MeuErrorListener.java     captura erros léxicos/sintáticos
    Automato.java             modelo do AFD (AST enriquecida)
    AnalisadorSemantico.java  as 6 verificações semânticas (Visitor)
    Interpretador.java        simulação das cadeias
```
