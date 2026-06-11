# Replicação MySQL com separação de leitura e escrita

Projeto em **Java 8 + Spring Boot 2.7** para a disciplina de Computação em
Nuvem 2.

Identificação usada no campo `criado_por`: **Grupo 8**.

## Integrantes

- Vinicius
- Bruno Santos
- Nathan Bizinoto

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

## No dia da apresentação

Antes de iniciar, preencha no PowerShell os valores informados pelo professor:

```powershell
$env:DB_PRIMARY_HOST="HOST_PRIMARIO_DO_PROFESSOR"
$env:DB_REPLICA_HOSTS="HOST_REPLICA_1,HOST_REPLICA_2"
$env:DB_USER="USUARIO_FORNECIDO"
$env:DB_PASSWORD="SENHA_FORNECIDA"
$env:DB_NAME="aula-db"
$env:DB_PORT="3306"
$env:DB_USE_SSL="true"
mvn spring-boot:run
```

Não será necessário alterar o código Java.
Use `DB_USE_SSL=false` caso o professor informe que a conexão não utiliza SSL.

## Endpoints

- `GET http://localhost:8080/health`
- `GET http://localhost:8080/pedidos/{id}`
- `GET http://localhost:8080/clientes/{id}/pedidos`
- `GET http://localhost:8080/produtos/baixo-estoque`
- `GET http://localhost:8080/relatorios/vendas`

## Roteiro de apresentação

1. No VS Code, abra a pasta do projeto e selecione `Terminal > Novo Terminal`.
2. Para a demonstração local, execute `.\scripts\iniciar-projeto-local.ps1`.
3. Aguarde aparecer `Started Application` e as linhas `[WRITE primaria]` e
   `[READ 127.0.0.1]`.
4. No navegador, abra `http://localhost:8080/health` e mostre o status, o
   nome `Grupo 8` e a quantidade de réplicas.
5. Abra `http://localhost:8080/relatorios/vendas` para demonstrar uma leitura
   com agregações.
6. Abra `http://localhost:8080/produtos/baixo-estoque` para demonstrar uma
   consulta realizada pelo caminho de leitura.
7. No VS Code, abra
   `src/main/java/br/edu/fatec/bd/config/DatabaseConfig.java` para mostrar as
   conexões separadas do primário e das réplicas.
8. Abra `src/main/java/br/edu/fatec/bd/database/ReadRouter.java` para mostrar
   a alternância round-robin entre as réplicas.
9. Abra `src/main/java/br/edu/fatec/bd/service/DatabaseService.java` para
   mostrar que INSERT e UPDATE usam `writeJdbcTemplate`.
10. Abra `database/schema.sql` para mostrar as tabelas `cliente`, `produto`,
    `pedido` e `pedido_item`.
11. Pressione `Ctrl+C` no terminal para encerrar a aplicação.

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
