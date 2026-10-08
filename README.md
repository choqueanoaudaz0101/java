# Game Finder

API Spring Boot para o projeto Java Advanced. Implementa os quatro endpoints do enunciado com JPA, H2, paginação e HATEOAS. O campo `inWishlist` é mantido nos dados e nas respostas, sem endpoints de alteração.

## Requisitos

- JDK 21 atualizado.
- Internet na primeira execução para baixar Maven e dependências.
- IntelliJ IDEA com suporte a Java 21. O projeto também pode ser executado pelo terminal.

Spring Boot 4.1.1 e Maven 3.9.16. O Maven Wrapper está incluído para Windows, Linux e macOS, com verificação SHA-256 da distribuição.

## Executar no IntelliJ

1. Extraia o ZIP.
2. Em **File > Open**, selecione a pasta `game-finder`, que contém `pom.xml`.
3. Aguarde a importação do Maven. Se necessário, abra o `pom.xml` como projeto Maven.
4. Em **File > Project Structure > Project SDK**, selecione um JDK 21.
5. Em **Settings > Build, Execution, Deployment > Build Tools > Maven**, use o Maven Wrapper e o JDK 21 no importer e runner.
6. Execute o método `main` de `br.com.fiap.gamefinder.GameFinderApplication`.

O backend fica em `http://localhost:8080`. A raiz `/` não possui página; consulte `/games`.

## Executar pelo terminal

Na pasta que contém `pom.xml`:

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux ou macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Para testar e gerar o JAR:

```powershell
.\mvnw.cmd clean verify
java -jar target/game-finder-1.0.0.jar
```

No Linux/macOS, substitua `.\mvnw.cmd` por `./mvnw`.

## Endpoints

| Método | Caminho | Resultado |
| --- | --- | --- |
| GET | `/games` | Jogos paginados e ordenados |
| GET | `/games/{id}` | Detalhes de um jogo |
| GET | `/games/genres/{genreId}` | Jogos do gênero |
| GET | `/games/platforms/{platformId}` | Jogos da plataforma |

As listagens aceitam `page`, `size` e `sort`. Os valores padrão são `page=0`, `size=20` e `sort=title,asc`.

- `page`: de 0 a 10000.
- `size`: de 1 a 100.
- `sort`: `id`, `title`, `releaseDate` ou `rating`, seguido de `,asc` ou `,desc`.
- Empates são ordenados por `id`, para estabilizar a paginação.

Exemplos:

```text
http://localhost:8080/games?page=0&size=6&sort=title,asc
http://localhost:8080/games/1
http://localhost:8080/games/genres/4
http://localhost:8080/games/platforms/4
```

Listagens retornam `_embedded.gameList`, `_links` e `page`. Cada jogo inclui os links `self`, `same-genre` e `same-platform`, com títulos. Listagens vazias preservam `gameList: []`.

Um jogo inexistente retorna 404. Um filtro sem resultados retorna 200 com lista vazia. Parâmetros inválidos retornam 400. Métodos de escrita são bloqueados com 403.

## Dados e frontend

O H2 funciona em memória, com 20 jogos, 10 gêneros e 6 plataformas carregados na inicialização. Os dados vêm do `data.sql` do professor e são recriados a cada execução.

Frontend de referência: https://github.com/joaocarloslima/gamefinder-web

No frontend, configure `API_URL=http://localhost:8080` em `.env.local` e siga suas instruções de execução. As chamadas à API são realizadas pelo servidor Next.js; não é necessário liberar CORS para esse fluxo.

A listagem principal é compatível com a paginação do frontend. Na versão de referência analisada, a página de filtros não encaminha os parâmetros de paginação; o tamanho padrão 20 comporta todos os resultados da base de exemplo.

As imagens são URLs externas fornecidas pelo professor. Sua disponibilidade depende dos respectivos servidores.

## Organização

- `controller`: rotas e validação de parâmetros.
- `service`: consultas em transações somente de leitura.
- `repository`: consultas JPA com gênero e plataforma carregados juntos.
- `model`: entidades e índices do banco.
- `dto`: respostas imutáveis, sem serialização direta de entidades JPA.
- `assembler`: links HATEOAS.
- `config`: regras de segurança.
- `exception`: respostas de erro.
- `src/test`: testes de integração da API.

## Segurança

A API é pública e somente de consulta, conforme o enunciado. Escuta apenas em `127.0.0.1` por padrão, bloqueia métodos de escrita e rotas não autorizadas, não cria sessões e mantém o console H2 desativado. Não contém chaves ou credenciais de serviços externos.

A ordenação aceita apenas os campos documentados, as consultas usam parâmetros JPA e a paginação é limitada. Erros não retornam stack traces. O Spring Security fornece cabeçalhos de proteção e a política de conteúdo restringe execução e incorporação das respostas.

Os fontes da aplicação não contêm comentários. Os scripts oficiais do Maven Wrapper preservam seus avisos de licença Apache.

Para disponibilizar a API fora da máquina local, configure o bind de rede explicitamente e use HTTPS, limites de tráfego e controle de acesso adequados ao ambiente. A configuração entregue é para execução local do trabalho.
