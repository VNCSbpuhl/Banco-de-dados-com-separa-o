# Replicação MySQL com separação de leitura e escrita

Projeto em **Java 8 + Spring Boot 2.7** para a disciplina de Computação em
Nuvem 2.

Identificação usada no campo `criado_por`: **Grupo 8**.

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

## Preparação local

O projeto inclui scripts para instalar o MySQL Community Server 8.4.9 portátil. Os binários e arquivos de dados não ficam no Git: o instalador baixa o servidor oficial e recria o ambiente.

```powershell
.\scripts\instalar-mysql-local.ps1
.\scripts\iniciar-projeto-local.ps1
```

Pressione `Ctrl+C` para encerrar. A aplicação usa a porta `8080`.

## No dia da apresentação

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

## Endpoints opcionais

- `GET http://localhost:8080/health`
- `GET http://localhost:8080/pedidos/{id}`
- `GET http://localhost:8080/clientes/{id}/pedidos`
- `GET http://localhost:8080/produtos/baixo-estoque`
- `GET http://localhost:8080/relatorios/vendas`

## Separação de leitura e escrita

- `DatabaseService` usa `writeJdbcTemplate` para INSERT e UPDATE.
- `ReadRouter` distribui SELECTs entre N réplicas em round-robin.
- Pedidos, estoque e itens usam uma transação no banco primário.
- Todos os registros usam `Grupo 8` no campo `criado_por`.
