# API CAGED | Empregabilidade da População Negra em Viamão/RS

**Dados públicos, tecnologia e consciência racial: transformando informações em conhecimento.**

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![Power BI](https://img.shields.io/badge/Power_BI-Analytics-F2C811?logo=powerbi&logoColor=black)
![Status](https://img.shields.io/badge/Status-Em_Desenvolvimento-orange)

**Desenvolvedor:** Alex Carvalho  
**Localidade de referência:** Viamão, Rio Grande do Sul  
**Data comemorativa:** 20 de novembro — Dia Nacional de Zumbi e da Consciência Negra  
**Status:** Em desenvolvimento

---

## 1. Sobre o projeto

Qual é a realidade da população negra no mercado de trabalho formal de Viamão/RS? Quantas pessoas negras foram admitidas ou desligadas? Como essas movimentações se distribuem por sexo, idade, atividade econômica e remuneração?

Essas são algumas das perguntas que motivaram o desenvolvimento desta API.

A **API CAGED – Viamão/RS** é uma aplicação REST desenvolvida em Java e Spring Boot que utiliza microdados públicos do Novo Cadastro Geral de Empregados e Desempregados (Novo CAGED), disponibilizados pelo Ministério do Trabalho e Emprego (MTE).

Seu objetivo é transformar registros administrativos de admissões e desligamentos em informações estruturadas, permitindo analisar a movimentação do emprego formal da população negra, especialmente de pessoas pretas e pardas, no município de Viamão/RS.

A solução foi idealizada para contribuir com reflexões relacionadas ao Dia da Consciência Negra, utilizando tecnologia e dados públicos como instrumentos de análise, conscientização e produção de conhecimento.

Os dados disponibilizados pela API podem ser consumidos pelo **Microsoft Power BI**, possibilitando a construção de dashboards, relatórios e indicadores sobre a participação da população negra no mercado de trabalho formal.

### Objetivos

- Analisar admissões, desligamentos e saldo de empregos formais da população negra.
- Identificar diferenças nas movimentações de emprego por raça/cor, sexo, idade e setor econômico.
- Disponibilizar dados estruturados para visualizações e análises no Power BI.
- Facilitar o acesso e a interpretação de dados públicos do Novo CAGED.
- Incentivar discussões fundamentadas em evidências sobre desigualdades raciais no mundo do trabalho.

> **Por que esse projeto importa?**
>
> O Dia da Consciência Negra é também uma oportunidade para refletir sobre as condições de acesso, permanência e participação da população negra no mercado de trabalho. A tecnologia permite organizar dados que podem contribuir para essa discussão, tornando informações públicas mais acessíveis e compreensíveis.

---

## 2. Tecnologias utilizadas

| Tecnologia | Aplicação |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 3.4 | Desenvolvimento da API REST |
| Maven | Gerenciamento de dependências e build |
| Apache Commons Net | Comunicação com o servidor FTP do MTE |
| Apache Commons Compress | Processamento de arquivos compactados, conforme a implementação |
| SpringDoc OpenAPI | Documentação interativa com Swagger UI |
| JSON | Formato de resposta da API |
| Power BI | Consumo e visualização dos indicadores |

### Arquitetura

A aplicação está organizada em camadas, separando as responsabilidades de configuração, exposição de endpoints, processamento dos dados e aplicação das regras de consulta.

**Fluxo de processamento:**

```text
Ministério do Trabalho e Emprego
              |
              v
       FTP do Novo CAGED
              |
              v
     Download dos microdados
              |
              v
   Leitura e processamento
              |
              v
      Cache local de dados
              |
              v
   Filtros e agregações
              |
              v
       API REST / JSON
              |
              v
       Microsoft Power BI
              |
              v
  Dashboards e indicadores
```

O armazenamento em cache evita downloads e processamentos repetidos, melhorando o desempenho das consultas posteriores.

---

## 3. Pré-requisitos

Para executar a aplicação localmente, é necessário:

- JDK 21 ou superior compatível.
- Maven 3.9 ou superior.
- Acesso à internet para consultar os arquivos públicos do MTE.
- Espaço disponível em disco para armazenar os arquivos processados.

Verifique as instalações:

```bash
java -version
mvn -version
```

---

## 4. Instalação e execução

### Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd caged-api
```

### Executar com Maven

```bash
mvn spring-boot:run
```

### Executar por arquivo JAR

```bash
mvn clean package
java -jar target/caged-api-1.0.0.jar
```

*O nome do arquivo JAR pode variar conforme a versão configurada no `pom.xml`.*

### Acessar a aplicação

**API local:**

http://127.0.0.1:8000

**Swagger UI:**

http://127.0.0.1:8000/swagger-ui.html

Na primeira consulta de uma competência, a aplicação realiza o download e o processamento dos arquivos necessários. Essa operação pode levar alguns minutos, dependendo do tamanho dos dados e da conexão.

As consultas posteriores utilizam o cache local armazenado no diretório `dados_rs/`.

---

## 5. Configurações

As propriedades da aplicação estão definidas em:

`src/main/resources/application.yml`

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8000` | Porta HTTP |
| `caged.ftp-host` | `ftp.mtps.gov.br` | Servidor FTP |
| `caged.ftp-dir` | `/pdet/microdados/NOVO CAGED` | Diretório dos microdados |
| `caged.pasta-dados` | `dados_rs` | Diretório do cache |
| `caged.uf-rs` | `43` | Código da UF |
| `caged.cod-viamao` | `432300` | Código IBGE de Viamão |
| `caged.charset` | `UTF-8` | Codificação configurada |
| `caged.ftp-timeout-segundos` | `120` | Timeout da conexão |

**Observação:** a codificação deve ser compatível com os arquivos disponibilizados pelo MTE. Caso ocorram problemas com caracteres acentuados, verificar a necessidade de utilizar `ISO-8859-1`.

---

## 6. Endpoints da API

### GET `/rs/movimentacoes`

Retorna as movimentações de emprego formal de uma competência específica.

**Exemplo:**

```http
GET /rs/movimentacoes?ano=2026&mes=01&sexo=Homem&raca=Negra
```

### GET `/rs/movimentacoes-ano`

Retorna as movimentações consolidadas dos meses disponíveis de determinado ano.

**Exemplo:**

```http
GET /rs/movimentacoes-ano?ano=2026&sexo=Homem&raca=Negra
```

### Parâmetros disponíveis

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `ano` | String | Ano de referência, padrão 2026 |
| `mes` | String | Mês de referência, padrão 01 |
| `municipio` | Integer | Código IBGE, padrão 432300; 0 para RS |
| `sexo` | String | Homem, Mulher ou todos |
| `raca` | String | Branca, Preta, Parda, Amarela, Indígena, Negra ou todos |
| `idade_min` | Integer | Idade mínima |
| `idade_max` | Integer | Idade máxima |
| `salario_min` | Decimal | Limite salarial inferior |
| `salario_max` | Decimal | Limite salarial superior |
| `agregado` | Boolean | Agrupamento dos dados, padrão true |

**Atenção:** no endpoint anual, os padrões informados para `sexo` e `raca` são, respectivamente, `Homem` e `Preta`. Para analisar toda a população negra, utilizar explicitamente `raca=Negra`.

---

## 7. Exemplos de consultas

### Homens negros em Viamão — janeiro de 2026

```http
GET /rs/movimentacoes?ano=2026&mes=01&sexo=Homem&raca=Negra
```

### Mulheres negras em Viamão — ano de 2026

```http
GET /rs/movimentacoes-ano?ano=2026&sexo=Mulher&raca=Negra
```

### População negra, independentemente do sexo

```http
GET /rs/movimentacoes-ano?ano=2026&sexo=todos&raca=Negra
```

### Comparação com todas as categorias de raça/cor

```http
GET /rs/movimentacoes-ano?ano=2026&sexo=todos&raca=todos
```

### Movimentações em todo o Rio Grande do Sul

```http
GET /rs/movimentacoes-ano?ano=2026&municipio=0&sexo=todos&raca=todos
```

### Consulta sem agregação

```http
GET /rs/movimentacoes?ano=2026&mes=03&agregado=false
```

---

## 8. Estrutura dos dados retornados

Quando `agregado=true`, a API disponibiliza campos para análise das movimentações de emprego.

| Campo | Descrição |
|---|---|
| `competencia` | Ano e mês (AAAAMM) |
| `municipio` | Código IBGE |
| `secao` | Seção CNAE |
| `sexo` | Sexo informado |
| `raca` | Raça/cor informada |
| `idade` | Idade |
| `admissoes` | Quantidade de admissões |
| `desligamentos` | Quantidade de desligamentos |
| `saldo` | Admissões menos desligamentos |
| `salarioMedio` | Média salarial calculada |
| `salarioMediano` | Mediana salarial calculada |

O saldo é calculado por:

**Saldo = Admissões − Desligamentos**

- Saldo positivo: mais admissões do que desligamentos.
- Saldo negativo: mais desligamentos do que admissões.
- Saldo zero: quantidades equivalentes.

Esses indicadores representam movimentações de vínculos formais, não necessariamente pessoas distintas.

---

## 9. Integração com Power BI

A API pode ser consumida diretamente pelo Power BI Desktop, utilizando o Power Query.

### Procedimento

1. Abra o Power BI Desktop.
2. Selecione **Obter Dados**.
3. Escolha **Consulta em Branco**.
4. Abra o **Editor Avançado**.
5. Insira o código abaixo.

```powerquery
let
    Fonte = Json.Document(
        Web.Contents(
            "http://127.0.0.1:8000/rs/movimentacoes-ano",
            [
                Query = [
                    ano = "2026",
                    sexo = "Homem",
                    raca = "Negra"
                ],
                Timeout = #duration(0, 0, 15, 0)
            ]
        )
    ),
    Tabela = Table.FromList(
        Fonte,
        Splitter.SplitByNothing(),
        null,
        null,
        ExtraValues.Error
    ),
    Expandido = Table.ExpandRecordColumn(
        Tabela,
        "Column1",
        {
            "competencia",
            "municipio",
            "secao",
            "sexo",
            "raca",
            "idade",
            "admissoes",
            "desligamentos",
            "saldo",
            "salarioMedio",
            "salarioMediano"
        }
    )
in
    Expandido
```

### Recomendações

- Mantenha a API em execução durante a atualização dos dados.
- Realize a primeira consulta antecipadamente para preencher o cache.
- Para atualização no Power BI Service, considere a utilização de um gateway ou a hospedagem da API em um ambiente acessível.

---

## 10. Indicadores e análises possíveis

A partir dos dados disponibilizados, poderão ser construídos painéis com os seguintes indicadores:

**Empregabilidade**
- Total de admissões da população negra.
- Total de desligamentos.
- Saldo mensal e anual de empregos.
- Evolução das movimentações ao longo do tempo.

**Perfil dos trabalhadores**
- Distribuição por sexo.
- Faixas etárias.
- Comparação entre pessoas pretas e pardas.
- Participação por seção de atividade econômica.

**Remuneração**
- Salário médio.
- Salário mediano.
- Diferenças salariais entre grupos.
- Distribuição das remunerações por setor e perfil.

**Comparações**
- População negra e demais grupos de raça/cor.
- Homens negros e mulheres negras.
- Viamão e o conjunto do Rio Grande do Sul.

As comparações devem considerar diferenças de composição dos grupos, qualidade dos registros e limitações do Novo CAGED.

---

## 11. Estrutura do projeto

```text
caged-api/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── br/com/cagedapi/
        │       ├── CagedApiApplication.java
        │       ├── config/
        │       ├── controller/
        │       ├── exception/
        │       ├── model/
        │       └── service/
        │           ├── CagedDataService.java
        │           └── CagedQueryService.java
        └── resources/
            └── application.yml
```

**Responsabilidades principais:**

- `config`: configurações e propriedades da aplicação.
- `controller`: disponibilização dos endpoints REST.
- `exception`: tratamento de exceções.
- `model`: representação das movimentações, filtros e resumos.
- `CagedDataService`: acesso aos arquivos, leitura e cache.
- `CagedQueryService`: aplicação de filtros e agregações.

---

## 12. Fonte e metodologia

**Fonte oficial:** Ministério do Trabalho e Emprego — Novo CAGED.

Os dados são obtidos a partir dos arquivos públicos de movimentações trabalhistas.

### Critérios utilizados

**Admissões:** registros identificados com `saldomovimentação = 1`.

**Desligamentos:** registros identificados com `saldomovimentação = -1`.

**População negra:** agrupamento das categorias de raça/cor **Preta** e **Parda**, conforme a classificação utilizada pelo IBGE.

**Abrangência:** vínculos de emprego formal contemplados pelo Novo CAGED, não incluindo o trabalho informal, o trabalho autônomo ou os vínculos estatutários.

### Cuidados metodológicos

- Os indicadores refletem movimentações registradas, e não a quantidade total de trabalhadores empregados.
- Uma mesma pessoa pode aparecer em mais de uma movimentação.
- Os dados podem sofrer revisões decorrentes de declarações fora do prazo, exclusões e retificações.
- A ausência de informação de raça/cor pode afetar a representatividade das análises.
- Indicadores salariais exigem atenção à variável utilizada, à existência de valores ausentes e à composição dos grupos comparados.
- Diferenças observadas entre grupos não demonstram, isoladamente, relações de causalidade ou discriminação.

---

## 13. Limitações conhecidas

O projeto encontra-se em desenvolvimento e apresenta limitações que devem ser consideradas na interpretação dos resultados.

- A disponibilidade dos dados depende do calendário de publicação do MTE.
- Competências recentes podem ainda não estar disponíveis.
- Arquivos já armazenados em cache não são atualizados automaticamente após eventuais republicações.
- Salários ausentes podem ser representados como `null`.
- Os dados de movimentações fora do prazo e exclusões ainda não estão integrados.
- O tempo da primeira consulta depende do download e processamento dos arquivos.
- A qualidade dos indicadores depende da consistência e do preenchimento dos registros de origem.

---

## 14. Roadmap — Próximas etapas

### Qualidade e confiabilidade

- [ ] Implementar testes automatizados para leitura, filtros e agregações.
- [ ] Validar os resultados com as estatísticas oficiais publicadas pelo MTE.
- [ ] Implementar tratamento e registro de inconsistências.
- [ ] Definir regras documentadas para salários ausentes ou inválidos.

### Evolução da API

- [ ] Implementar atualização manual do cache.
- [ ] Incorporar movimentações fora do prazo (CAGEDFOR).
- [ ] Incorporar registros de exclusão (CAGEDEXC).
- [ ] Criar endpoints específicos para indicadores consolidados.
- [ ] Implementar paginação para consultas detalhadas.
- [ ] Adicionar logs estruturados e monitoramento.

### Infraestrutura

- [ ] Criar Dockerfile.
- [ ] Disponibilizar ambiente de execução por Docker Compose.
- [ ] Automatizar processos de atualização dos dados.
- [ ] Avaliar hospedagem para disponibilização pública da API.

### Visualização e análise

- [ ] Desenvolver dashboard interativo no Power BI.
- [ ] Criar indicadores comparativos por raça/cor e sexo.
- [ ] Implementar análise temporal de admissões e desligamentos.
- [ ] Produzir relatório de indicadores para o Dia da Consciência Negra.

---

## 15. Impacto social e compromisso com os dados

Este projeto parte da compreensão de que a tecnologia pode contribuir para ampliar o acesso à informação e apoiar discussões sobre questões sociais relevantes.

Ao reunir dados públicos sobre o mercado de trabalho formal, a API busca oferecer uma base técnica para estudos, análises e reflexões sobre a participação da população negra na economia local.

O objetivo não é antecipar conclusões, mas **criar condições para que os dados sejam analisados com transparência, responsabilidade e rigor metodológico**.

O acesso à informação é uma ferramenta importante para compreender realidades, identificar desafios e apoiar a construção de uma sociedade mais justa.

---

## 16. Referências

- [Ministério do Trabalho e Emprego](https://www.gov.br/trabalho-e-emprego/pt-br)
- [Programa de Disseminação das Estatísticas do Trabalho (PDET)](https://pdet.mte.gov.br/)
- [Instituto Brasileiro de Geografia e Estatística (IBGE)](https://www.ibge.gov.br/)

---

## 17. Autor

**Alex Carvalho**

Graduado em Análise e Desenvolvimento de Sistemas pelo Instituto Federal de Educação, Ciência e Tecnologia do Rio Grande do Sul (IFRS).

Projeto desenvolvido com Java, Spring Boot e integração de dados públicos, reunindo conhecimentos em desenvolvimento backend, APIs REST, processamento de dados e análise de informações.

**20 de novembro — Dia Nacional de Zumbi e da Consciência Negra**

*Tecnologia, dados e consciência racial: conhecer a realidade é um passo importante para transformá-la.*
