## Buscador Global - Visualizador Geodemográfico

Aplicação de console em Java que consome a API pública RestCountries v3.1 para consultar, comparar e listar dados geopolíticos de países e continentes — população, área e densidade demográfica.

---

## Funcionalidades

### Consultar / Comparar

Aceita expressões compostas com países e continentes, incluindo operações de soma, subtração e comparação bilateral.

```
Brazil                            → dados do Brasil
Africa                            → dados do continente africano
Brazil Argentina                  → soma Brasil + Argentina
Americas - USA                    → Américas excluindo os EUA
Africa India x China              → comparação: (África + Índia) vs China
Asia - China x Asia - Russia      → comparação: Ásia excluindo China vs Ásia excluindo Rússia
```

### Listagem

Lista todos os países ou aplica filtros por população mínima, área mínima e densidade mínima. O resultado pode ser ordenado por qualquer dos três critérios, em ordem crescente ou decrescente. Exibe também um painel de totais agregados por continente.

---

## Tecnologias

- Java 17+
- [Jackson Databind](https://github.com/FasterXML/jackson-databind) — desserialização JSON
- [RestCountries API v3.1](https://restcountries.com) — fonte de dados

---

## Estrutura do projeto

```
src/main/java/
├── Main.java                          # Ponto de entrada que orquestra o fluxo
└── geopolitico/
    ├── model/
    │   ├── Country.java               # Modelo de país (POJO imutável)
    │   └── GrupoResultado.java        # Resultado agregado de uma expressão
    ├── service/
    │   ├── CountryService.java        # Interface do serviço de dados
    │   └── RestCountriesClient.java   # Implementação com cache e tratamento de erro
    ├── parser/
    │   └── ExpressionParser.java      # Interpretação de expressões geodemográficas
    ├── formatter/
    │   └── Formatter.java             # Formatação e exibição no console
    ├── cache/
    │   └── InMemoryCache.java         # Cache LRU em memória
    ├── exception/
    │   └── ApiException.java          # Exceção tipada para falhas de rede
    └── util/
        └── InputUtils.java            # Leitura e validação de entrada do usuário
```

---

## Como compilar e executar

**Pré-requisitos**
- JDK 17 ou superior
- Maven 3.8+

**Compilar**
```bash
mvn compile
```

**Executar**
```bash
mvn exec:java -Dexec.mainClass="Main"
```

**Ou manualmente com javac/java:**
```bash
javac -cp ".:lib/jackson-databind-*.jar:lib/jackson-core-*.jar:lib/jackson-annotations-*.jar" \
      -d out src/main/java/**/*.java src/main/java/Main.java

java -cp "out:lib/*" Main
```

> A aplicação requer conexão com a internet para consultar a API RestCountries.

---

## Decisões de arquitetura

| Decisão | Motivo |
|---|---|
| Interface `CountryService` | Permite substituir a fonte de dados ou injetar mocks em testes sem alterar a lógica de negócio |
| Cache LRU em memória | Evita chamadas HTTP redundantes dentro da mesma sessão; `buscarTodos()` é cacheado na primeira chamada |
| `GrupoResultado` como `record` | Imutabilidade garantida em tempo de compilação; elimina campos públicos sem getters |
| `ApiException` tipada | Separa falhas de infraestrutura (rede, timeout) de erros de lógica, permitindo mensagens contextuais ao usuário |
| `ExpressionParser` isolado | Parser com responsabilidade única, testável independentemente da interface de usuário |
| Áreas geográficas reais dos continentes | A soma das áreas dos países retornados pela API subestima a área real; valores fixos corrigem isso |

---

## Licença

Projeto de portfólio acadêmico. Livre para uso e referência.
