# Apresentação - Replicação de Banco de Dados

## Identificação

- Disciplina: Computação em Nuvem 2
- Grupo: Grupo 8
- Integrantes: Vinicius, Bruno Santos e Nathan Bizinoto
- Campo `criado_por`: Grupo 8

## Objetivo

Desenvolver uma aplicação Java preparada para conexão com MySQL, escrita no
banco primário e leitura alternada entre uma ou mais réplicas.

## Tecnologias

- Java 8
- Spring Boot 2.7
- MySQL 8.4.9
- Maven
- API REST opcional

## Estrutura do banco

- `cliente`
- `produto`
- `pedido`
- `pedido_item`

## Como a solução funciona

1. `DatabaseConfig` cria uma conexão de escrita com o banco primário.
2. A mesma configuração cria uma conexão de leitura para cada réplica.
3. `DatabaseService` envia INSERT e UPDATE somente para o primário.
4. `ReadRouter` alterna as consultas entre as réplicas em round-robin.
5. `DataGenerator` cria clientes, produtos, pedidos e itens automaticamente.
6. Localmente, primário e réplica apontam para o mesmo MySQL em `127.0.0.1`.

## Demonstração

1. Configurar host, usuário e senha fornecidos pelo professor.
2. Iniciar a aplicação Java.
3. Abrir `http://localhost:8080/health`.
4. Mostrar no terminal as linhas `[WRITE primaria]`.
5. Mostrar no terminal as linhas `[READ host-da-replica]`.
6. Abrir `http://localhost:8080/relatorios/vendas`.
7. Abrir `http://localhost:8080/produtos/baixo-estoque`.
8. Mostrar no código `DatabaseConfig`, `ReadRouter` e `DatabaseService`.
9. Mostrar as quatro tabelas em `database/schema.sql`.

## Explicação para o professor

> A escrita sempre utiliza a conexão do banco primário. As consultas utilizam
> as réplicas configuradas e são alternadas em round-robin. No desenvolvimento
> local usamos o mesmo MySQL para os dois caminhos, pois a replicação local não
> era obrigatória. Na apresentação, os hosts são informados por variáveis de
> ambiente, sem alterar o código-fonte.

## Encerramento

A aplicação atende aos requisitos de conexão MySQL, separação de leitura e
escrita, suporte a múltiplas réplicas, geração automática de clientes e
produtos e identificação dos registros como Grupo 8.
