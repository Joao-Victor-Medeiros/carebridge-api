# CareBridge API

API REST da plataforma CareBridge, desenvolvida com Spring Boot, Spring Security, JWT, Spring Data JPA e PostgreSQL.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT (`java-jwt`)
- PostgreSQL
- Docker
- Springdoc OpenAPI / Swagger
- Spring Boot Actuator

## Requisitos

- Java 21
- PostgreSQL 15 ou superior, ou Docker Desktop
- Maven Wrapper incluído no projeto (`mvnw.cmd` no Windows e `./mvnw` no Linux/macOS)
- Docker Desktop, caso queira executar o PostgreSQL ou a API em containers

## Configuração das variáveis de ambiente

Copie o arquivo de exemplo:

```powershell
Copy-Item .env.exemple .env
```

Edite o arquivo `.env`:

```env
JWT_SECRET=uma-chave-jwt-local
DATABASE_URL=jdbc:postgresql://localhost:5432/carebridge-db
DATABASE_USERNAME=admin
DATABASE_PASSWORD=admin
```

O arquivo `.env` está no `.gitignore` e não deve ser commitado.

> O projeto importa opcionalmente o arquivo `.env` por meio de `application.yaml`. Nunca publique senhas, tokens JWT ou a senha do Neon no repositório.

## Banco PostgreSQL local

### Opção 1: PostgreSQL instalado localmente

Crie um banco com os mesmos dados usados na configuração padrão:

```sql
CREATE DATABASE "carebridge-db";
```

Configure no `.env`:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/carebridge-db
DATABASE_USERNAME=admin
DATABASE_PASSWORD=admin
```

### Opção 2: PostgreSQL com Docker Compose

Defina no `.env` as variáveis usadas pelo `docker-compose.yaml`:

```env
POSTGRES_DB=carebridge-db
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
```

Suba o PostgreSQL:

```powershell
docker compose up -d postgres
```

Verifique o container:

```powershell
docker compose ps
```

Para acompanhar os logs:

```powershell
docker compose logs -f postgres
```

Para parar o banco:

```powershell
docker compose down
```

Os dados ficam no volume Docker chamado `postgres`.

## Executar a aplicação localmente

Com o PostgreSQL disponível e o `.env` configurado:

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada, por padrão, na porta `8080`:

```text
http://localhost:8080
```

No ambiente de desenvolvimento, o arquivo `application.yaml` usa:

```yaml
spring.jpa.hibernate.ddl-auto: update
```

Assim, o Hibernate cria ou atualiza as tabelas com base nas entidades JPA.

## Compilar o projeto

```powershell
.\mvnw.cmd clean compile
```

Para gerar o JAR sem executar testes:

```powershell
.\mvnw.cmd clean package -DskipTests
```

O artefato será criado em:

```text
target/carebridge-api-0.0.1-SNAPSHOT.jar
```

Executar o JAR:

```powershell
java -jar target/carebridge-api-0.0.1-SNAPSHOT.jar
```

## Profile de produção

O profile de produção está em:

```text
src/main/resources/application-prod.yml
```

Para ativá-lo localmente no PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DATABASE_URL="jdbc:postgresql://SEU_HOST_NEON/neondb?sslmode=require&channel_binding=require"
$env:DATABASE_USERNAME="SEU_USUARIO_NEON"
$env:DATABASE_PASSWORD="SUA_SENHA_NEON"
$env:JWT_SECRET="uma-chave-jwt-de-teste"
.\mvnw.cmd spring-boot:run
```

O profile `prod` usa as variáveis:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
```

Atualmente, o profile de produção usa `ddl-auto: update`, portanto o Hibernate cria ou atualiza as tabelas no banco PostgreSQL configurado, como o Neon.

## Executar com Docker

### Construir a imagem

```powershell
docker build -t carebridge-api:prod-test .
```

O Dockerfile usa Java 21 e compila a aplicação dentro de uma imagem multi-stage.

### Executar com PostgreSQL local

No Windows, o container acessa um PostgreSQL da máquina por `host.docker.internal`:

```powershell
docker run -d `
  --name carebridge-api-prod-test `
  -p 8081:8081 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e PORT=8081 `
  -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/carebridge-db" `
  -e DATABASE_USERNAME="admin" `
  -e DATABASE_PASSWORD="admin" `
  -e JWT_SECRET="uma-chave-jwt-de-teste" `
  carebridge-api:prod-test
```

### Executar com Neon

Não coloque a senha real no comando ou no repositório. Configure-a diretamente no ambiente de execução:

```powershell
docker run -d `
  --name carebridge-api-neon-test `
  -p 8082:8082 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e PORT=8082 `
  -e DATABASE_URL="jdbc:postgresql://SEU_HOST_NEON/neondb?sslmode=require&channel_binding=require" `
  -e DATABASE_USERNAME="SEU_USUARIO_NEON" `
  -e DATABASE_PASSWORD="SUA_SENHA_NEON" `
  -e JWT_SECRET="uma-chave-jwt-de-teste" `
  carebridge-api:prod-test
```

Ver logs:

```powershell
docker logs -f carebridge-api-prod-test
```

Parar e remover:

```powershell
docker stop carebridge-api-prod-test
docker rm carebridge-api-prod-test
```

## Deploy no Render

No serviço Web Service do Render, configure o deploy usando o `Dockerfile` e adicione as variáveis:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://SEU_HOST_NEON/neondb?sslmode=require&channel_binding=require
DATABASE_USERNAME=SEU_USUARIO_NEON
DATABASE_PASSWORD=SUA_SENHA_NEON
JWT_SECRET=UMA_CHAVE_FORTE
```

O Dockerfile usa a porta fornecida pelo Render:

```text
PORT
```

Localmente, a porta padrão é `8080`.

O Render pode verificar a aplicação por:

```text
/actuator/health
```

## Health check e documentação

Com a aplicação na porta `8080`:

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

Com o container de teste na porta `8081`:

```text
http://localhost:8081/actuator/health
http://localhost:8081/swagger-ui/index.html
```

Com o container conectado ao Neon na porta `8082`:

```text
http://localhost:8082/actuator/health
http://localhost:8082/swagger-ui/index.html
```

## Principais endpoints

### Autenticação

Login:

```http
POST /user/login
```

Body:

```json
{
  "email": "usuario@example.com",
  "password": "Senha123!"
}
```

Use o token retornado nas rotas protegidas:

```http
Authorization: Bearer SEU_TOKEN
```

Cadastro genérico:

```http
POST /user/register
```

Body:

```json
{
  "nome": "Paulo Ricardo",
  "email": "paulo.ricardo@example.com",
  "senha": "Senha123!",
  "tipoPerfil": "PACIENTE"
}
```

O cadastro público de `ADMIN` é bloqueado.

### Paciente

```http
POST /paciente/cadastro
```

Body:

```json
{
  "usuarioDto": {
    "nome": "Paulo Ricardo",
    "email": "paulo.ricardo@example.com",
    "senha": "Senha123!",
    "tipoPerfil": "PACIENTE"
  },
  "nome": "Paulo Ricardo",
  "dataNascimento": "1990-05-21",
  "telefone": "+55 11 99999-9999",
  "endereco": "Rua das Flores, 123"
}
```

### Profissional de saúde

```http
POST /profissional-saude/cadastro
```

Essa rota usa `multipart/form-data`. Envie a parte `dados` como JSON:

```json
{
  "usuarioDto": {
    "nome": "Dra. Ana Souza",
    "email": "ana.souza@example.com",
    "senha": "Senha123!",
    "tipoPerfil": "PROFISSIONAL_SAUDE"
  },
  "nome": "Dra. Ana Souza",
  "documentosRegistro": [
    "CRM-SP 123456"
  ],
  "biografiaOuDescricao": "Médica clínica geral."
}
```

Novos profissionais começam com `validado_institucionalmente = false`.

### Administração

Listar profissionais pendentes:

```http
GET /user/profissionais/pendentes
```

Aprovar profissional:

```http
PATCH /user/profissionais/{profissionalId}/aprovar
```

Essas rotas exigem token de um usuário com perfil `ADMIN`.

### Oportunidades

Criar:

```http
POST /oportunidades
```

Exige profissional de saúde validado institucionalmente.

Body:

```json
{
  "titulo": "Atendimento psicológico gratuito",
  "descricao": "Atendimento psicológico individual.",
  "tipoServico": "Psicologia",
  "publicoAlvo": "Adultos de baixa renda",
  "modalidade": "ONLINE",
  "condicoesAcesso": "Agendamento prévio obrigatório.",
  "statusAtiva": true
}
```

Listar oportunidades ativas:

```http
GET /oportunidades
```

Consultar por ID:

```http
GET /oportunidades/{id}
```

Atualizar:

```http
PUT /oportunidades/{id}
```

Excluir:

```http
DELETE /oportunidades/{id}
```

O ofertante é obtido automaticamente do usuário autenticado. Não envie `ofertanteId` no body.

## Banco e perfil de produção

O profile `prod` pode criar/atualizar o schema com base nas entidades JPA. Para um ambiente de produção definitivo, recomenda-se substituir `ddl-auto: update` por migrations versionadas e `ddl-auto: validate`.

O schema inclui entidades como:

```text
usuarios
pacientes
profissional_saude
documentos_registro
oportunidades
formulario_triagem
inscricoes
```

## Diagnóstico rápido

Verifique o status do container:

```powershell
docker ps -a
```

Veja os logs:

```powershell
docker logs --tail 200 NOME_DO_CONTAINER
```

Teste a API:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
```

Se houver erro de banco, confira:

- URL com o prefixo `jdbc:postgresql://`;
- usuário e senha do PostgreSQL;
- `sslmode=require` para Neon;
- banco acessível pelo container;
- `SPRING_PROFILES_ACTIVE=prod`.
