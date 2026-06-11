# Replicação MySQL com separação de leitura e escrita

Projeto em **Java 8 + Spring Boot 2.7** para a disciplina de Computação em
Nuvem 2.

Identificação usada no campo `criado_por`: **Grupo 8**.

## Integrantes

- Vinicius Rodrigues Oliveira
- Bruno José dos Santos
- Nathan Bizinoto de Oliveira

## Desenvolvimento local

Durante o desenvolvimento, a aplicação usa por padrão:

- host primário: `localhost`;
- host de leitura: `localhost`;
- porta: `3306`;
- usuário: `root`;
- senha: vazia;
- database: `aula-db`.

Assim, o mesmo MySQL local recebe os dois tipos de conexão, mas o código já
mantém os caminhos de escrita e leitura separados.

Não é necessário configurar replicação no computador local.

Caso seu MySQL local tenha senha, defina `DB_PASSWORD` antes de iniciar.

## Requisitos

- Java 8 ou superior;
- JDK, e não apenas o Java de execução;
- Maven 3.6 ou superior;
- acesso aos hosts MySQL fornecidos pelo professor.

## Preparação local

O projeto inclui uma instalação portátil do MySQL 8.4.9. Ela não cria serviço
no Windows e não exige XAMPP.

Para instalar ou recriar o banco:

```powershell
.\scripts\instalar-mysql-local.ps1
```

Para iniciar somente o MySQL:

```powershell
.\scripts\iniciar-mysql-local.ps1
```

Para encerrar somente o MySQL:

```powershell
.\scripts\parar-mysql-local.ps1
```

## Execução local

O comando abaixo inicia o MySQL e a aplicação Java:

```powershell
.\scripts\iniciar-projeto-local.ps1
```

Pressione `Ctrl+C` para encerrar. Por padrão, a aplicação usa a porta `8080`.

```

Não será necessário alterar o código Java.
Use `DB_USE_SSL=false` caso o professor informe que a conexão não utiliza SSL.

## Endpoints

- `GET http://localhost:8080/health`
- `GET http://localhost:8080/pedidos/{id}`
- `GET http://localhost:8080/clientes/{id}/pedidos`
- `GET http://localhost:8080/produtos/baixo-estoque`
- `GET http://localhost:8080/relatorios/vendas`


## Separação de leitura e escrita

- `DatabaseService` usa `writeJdbcTemplate` para INSERT e UPDATE.
- `ReadRouter` distribui SELECTs entre N réplicas em round-robin.
- A criação de pedido, atualização de estoque e itens usa uma transação no
  banco primário.

## Escopo da apresentação

- A automação cria clientes e produtos continuamente.
- O projeto também cria pedidos e itens para demonstrar relacionamentos.
- A API REST foi mantida como recurso opcional.
- No campo `criado_por`, todos os registros usam `Grupo 8`.
- O professor fornecerá os hosts online em 11 de junho de 2026.
