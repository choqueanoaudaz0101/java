# Validação da entrega

Validado com JDK 21.0.12.1 em Linux e Spring Boot 4.1.1.

- `clean verify`: 40 testes executados, nenhum erro, falha ou teste ignorado.
- JAR iniciado e consultado por HTTP real.
- Navegação das quatro páginas de jogos e verificação dos 20 IDs sem repetição.
- Filtros por todos os gêneros e plataformas presentes nos dados.
- Detalhes, datas, campos e links HATEOAS compatíveis com o frontend de referência.
- Respostas de lista vazia, 400 e 404 verificadas.
- Métodos de escrita e rotas administrativas bloqueados.
- Console H2 desativado, cabeçalhos de segurança e ausência de sessão em consultas verificados.
- Ordenação inválida, inclusive tentativa de injeção SQL, rejeitada.
- Página de seis jogos carregada em duas consultas ao banco, incluindo gênero e plataforma.
- Fontes Java e SQL da aplicação sem comentários.

O IntelliJ e o Windows não foram executados neste ambiente. A entrega usa a estrutura Maven padrão e inclui os scripts oficiais do Maven Wrapper para as plataformas suportadas.

A consulta ao serviço externo OSV recebeu HTTP 403 neste ambiente. A auditoria de vulnerabilidades conhecidas das dependências não foi concluída. Os testes de segurança listados acima não equivalem a uma garantia de ausência de toda vulnerabilidade.
