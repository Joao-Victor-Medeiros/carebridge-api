# Especificação Completa de Regras de Negócio e Modelagem — `carebridge-api`

> **Fonte única dos contratos HTTP:** rotas, nomes de campos, corpos de requisição e resposta, códigos de status e corpo de erro seguem [`contrato-api.md`](contrato-api.md) (contrato v2, o mesmo que os apps iOS e web implementam). Onde este documento divergir dele, vale o contrato.

Este documento consolida as regras de negócio, modelos de dados (JPA/PostgreSQL), máquinas de estado, contratos HTTP e políticas de segurança de todos os módulos do backend Spring Boot (`carebridge-api`), alinhados ao Contrato v2 dos clientes Web (React/TypeScript) e Mobile (iOS/Swift).

---

## 1. Perfis de Usuário e Entidades Base

O sistema opera exclusivamente com **dois perfis de acesso** (`TipoPerfil`):
* `PACIENTE`
* `PROFISSIONAL_SAUDE`

### 1.1. Entidade `Usuario` (Credenciais e Identidade Base)
* `id`: `UUID` (chave primária gerada automaticamente).
* `email`: `String` (único no sistema, obrigatório, case-insensitive).
* `senhaHash`: `String` (hash BCrypt, nunca exposto em DTOs de resposta).
* `tipoPerfil`: `TipoPerfil` (`PACIENTE` ou `PROFISSIONAL_SAUDE`).
* `termosAceitosEm`: `Instant` (timestamp obrigatório registrado no momento do cadastro).
* `emailVerificado`: `boolean` (indica se o código de verificação foi confirmado).
* `criadoEm` / `atualizadoEm`: `Instant`.

### 1.2. Entidade `Paciente` (Relação 1:1 com `Usuario`)
* `id`: `UUID`.
* `usuario`: referência `1:1` para `Usuario`.
* `nome`: `String` (obrigatório).
* `dataNascimento`: `LocalDate` (formato `aaaa-mm-dd`, obrigatório).
* `telefone`: `String` (opcional, armazenado apenas com dígitos).
* `endereco`: `String` (opcional/nulo).

### 1.3. Entidade `ProfissionalSaude` (Relação 1:1 com `Usuario`)
* `id`: `UUID`.
* `usuario`: referência `1:1` para `Usuario`.
* `nome`: `String` (obrigatório).
* `conselho`: `String` (ex.: `CRM`, `CRP`, `CREFITO`, `COREN`, obrigatório).
* `uf`: `String` (sigla da unidade federativa do conselho, ex.: `DF`, `SP`, obrigatório).
* `numeroRegistro`: `String` (número de inscrição no conselho, obrigatório — **não deve ser descartado** no cadastro).
* `especialidade`: `String` (especialidade principal de atuação).
* `ondeAtende`: `String` (instituição, clínica ou local de atendimento).
* `biografiaOuDescricao`: `String` (opcional).
* `statusVerificacao`: Enum `StatusVerificacao` (`EM_VERIFICACAO`, `VERIFICADO`, `RECUSADO` — valor inicial padrão: `EM_VERIFICACAO`).
* `documentos`: lista de até `2` entidades `Documento` (`nomeArquivo`, `tipo`, `arquivo` em `byte[]`).

> **Regra de Operação do Profissional:** O status `statusVerificacao` tem caráter estritamente informativo (exibido como selo na vitrine pública de oportunidades). O profissional recém-cadastrado pode criar, editar, publicar e encerrar oportunidades imediatamente após o cadastro, sem depender de aprovação manual prévia.

---

## 2. Regras de Cadastro (Onboarding)

### 2.1. Cadastro de Paciente (`POST /paciente/cadastro`)
* **Acesso:** Público (`permitAll`).
* **Content-Type:** `application/json`.
* **Blindagem de Perfil:** O servidor **deve ignorar** qualquer valor enviado no campo `tipoPerfil` do JSON e forçar obrigatoriamente `TipoPerfil.PACIENTE`.
* **Validações:**
    * `email` não pode existir previamente na tabela `Usuario` (retornar `409 Conflict` com mensagem clara).
    * `acceptedTerms` deve ser `true` (gravar `termosAceitosEm = Instant.now()`).
    * `nome`, `email`, `senha` e `dataNascimento` são obrigatórios.
* **Resposta (`201 Created`):**
  ```json
  {
    "email": "paciente@teste.com",
    "profile": "PACIENTE",
    "codeLength": 6
  }
  ```

### 2.2. Cadastro de Profissional de Saúde (`POST /profissional-saude/cadastro`)
* **Acesso:** Público (`permitAll`).
* **Content-Type:** `multipart/form-data` contendo a parte `dados` (JSON) e até `2` arquivos na parte `documentos`.
* **Blindagem de Perfil:** Forçar obrigatoriamente `TipoPerfil.PROFISSIONAL_SAUDE`.
* **Validações:**
    * Verificar previamente se `email` já existe em `Usuario` antes de persistir (não deixar estourar `DataIntegrityViolationException` genérica; lançar exceção tratada como `409 Conflict`).
    * Persistir integralmente `conselho`, `uf`, `numeroRegistro`, `especialidade` e `ondeAtende` na entidade `ProfissionalSaude`.
    * Validar tamanho e formato dos arquivos anexados (PDF, JPG ou PNG, até 10 MB por arquivo).
* **Resposta (`201 Created`):**
  ```json
  {
    "email": "profissional@teste.com",
    "profile": "PROFISSIONAL_SAUDE",
    "codeLength": 6
  }
  ```

---

## 3. Autenticação, Sessão e LGPD

### 3.1. Login (`POST /user/login`)
* **Acesso:** Público (`permitAll`).
* **Entrada:** `{ "email": "...", "password": "..." }`.
* **Token JWT:**
    * Validade padrão de 2 horas (`7200` segundos).
    * Subject (`sub`) contendo o e-mail do usuário e claims com `id` e `tipoPerfil`.
* **Proteção contra Força Bruta:**
    * Contabilizar tentativas falhas consecutivas por e-mail/IP.
    * Após 5 tentativas incorretas, bloquear novas tentativas temporariamente e retornar `429 Too Many Requests`.
* **Saída (`200 OK`):** Deve incluir o token, a validade em segundos (`expiresIn`) e o objeto `user` completo:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "nome": "Ana Souza",
      "email": "paciente@teste.com",
      "tipoPerfil": "PACIENTE",
      "dataNascimento": "1988-03-12",
      "telefone": "61999990000",
      "verification": null
    }
  }
  ```

### 3.2. Perfil do Usuário Autenticado (`GET /user/me`)
* **Acesso:** Autenticado (`Authorization: Bearer <token>`).
* **Comportamento:** Resolve o usuário a partir do token no `SecurityContextHolder` e carrega os dados complementares de `Paciente` ou `ProfissionalSaude`.
* **Saída (`200 OK`):**
    * Para `PACIENTE`: `verification` retorna `null`.
    * Para `PROFISSIONAL_SAUDE`: `dataNascimento` pode retornar `null` e `verification` retorna `"EM_VERIFICACAO"`, `"VERIFICADO"` ou `"RECUSADO"`.

### 3.3. Verificação de E-mail e Recuperação de Senha
* **`POST /auth/verify` (Público):** Recebe `{ "email": "...", "code": "..." }`, valida o código temporário e marca `emailVerificado = true`.
* **`POST /auth/resend-code` (Público):** Gera e reenvia novo código de verificação para o e-mail informado.
* **`POST /auth/password-reset` (Público):** Inicia ou conclui a redefinição de senha mediante código/token enviado por e-mail.

### 3.4. Privacidade e Gestão de Conta (LGPD)
* **`POST /me/data-export` (Autenticado):** Gera pacote/relatório com os dados pessoais do usuário logado.
* **`DELETE /me/documents` (`PACIENTE`):** Remove todos os documentos vinculados às solicitações/perfil do paciente logado.
* **`DELETE /me` (Autenticado):** Exige confirmação da senha atual no corpo da requisição antes de excluir ou anonimizar a conta do usuário.

---

## 4. Matriz de Autorização por Rota (`SecurityConfigurations`)

| Método e Rota | Regra de Acesso | Validação Adicional |
| :--- | :--- | :--- |
| `OPTIONS /**` | `permitAll` | Preflight CORS liberado sem token. |
| `POST /user/login` | `permitAll` | Rate limit de 5 falhas (`429`). |
| `POST /paciente/cadastro` | `permitAll` | Força perfil `PACIENTE`. |
| `POST /profissional-saude/cadastro` | `permitAll` | Força perfil `PROFISSIONAL_SAUDE`. |
| `POST /auth/verify` | `permitAll` | — |
| `POST /auth/resend-code` | `permitAll` | — |
| `POST /auth/password-reset` | `permitAll` | — |
| `GET /oportunidades` | `permitAll` | Retorna apenas oportunidades com status `ATIVA`. |
| `GET /oportunidades/{id}` | `permitAll` | Vitrine pública de detalhe da vaga. |
| `GET /v3/api-docs/**, /swagger-ui/**` | `permitAll` | Documentação OpenAPI. |
| `GET /user/me` | `authenticated` | `PACIENTE` ou `PROFISSIONAL_SAUDE`. |
| `POST /me/data-export` | `authenticated` | Próprio usuário. |
| `DELETE /me/documents` | `hasRole("PACIENTE")` | Próprio paciente. |
| `DELETE /me` | `authenticated` | Exige senha atual válida. |
| `GET /professional/opportunities` | `hasRole("PROFISSIONAL_SAUDE")` | Lista apenas oportunidades do próprio profissional (`RASCUNHO`, `ATIVA`, `ENCERRADA`). |
| `POST /oportunidades` | `hasRole("PROFISSIONAL_SAUDE")` | Cria oportunidade vinculada ao profissional logado. |
| `PUT /oportunidades/{id}` | `hasRole("PROFISSIONAL_SAUDE")` | Apenas o dono (ofertante) da oportunidade e se `status != ENCERRADA`. |
| `POST /professional/opportunities/{id}/close` | `hasRole("PROFISSIONAL_SAUDE")` | Apenas o dono da oportunidade e se `status == ATIVA`. |
| `DELETE /oportunidades/{id}` | `hasRole("PROFISSIONAL_SAUDE")` | Apenas o dono da oportunidade e se `status == RASCUNHO`. |
| `/professional/requests/**` | `hasRole("PROFISSIONAL_SAUDE")` | Apenas solicitações vinculadas às oportunidades do profissional logado. |
| `/applications/**` | `hasRole("PACIENTE")` | Apenas solicitações criadas pelo paciente logado. |

---

## 5. Segurança HTTP, CORS e Padronização de Erros

### 5.1. Separação Estrita entre `401` e `403`
* **`401 Unauthorized` (`AuthenticationEntryPoint`):**
    * Senha ou e-mail incorretos no `POST /user/login`.
    * Requisição a rota protegida sem header `Authorization`.
    * Token JWT malformado, assinatura inválida ou token expirado.
    * **Importante:** Os clientes (especialmente iOS) usam `401` como gatilho para salvar rascunhos locais e solicitar novo login.
* **`403 Forbidden` (`AccessDeniedHandler`):**
    * Usuário está autenticado com token válido, mas tenta acessar rota exclusiva de outro perfil (ex.: `PACIENTE` chamando `POST /oportunidades`) ou manipular recurso de terceiros (ex.: editar oportunidade de outro profissional).
* **Filtro JWT (`SecurityFilter`):**
    * Quando a requisição não possuir header `Authorization`, o filtro não deve abortar a requisição; deve apenas repassar para `filterChain.doFilter(request, response)` para que o Spring Security avalie se a rota é `permitAll()`.

### 5.2. Configuração de CORS
* **Origens permitidas:** `https://luisfontinelles.github.io` e `http://localhost:5173` (e demais portas locais de desenvolvimento).
* **Métodos permitidos:** `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
* **Cabeçalhos permitidos:** `Authorization`, `Content-Type`, `Accept`, `Origin`, `X-Requested-With`.
* **Cabeçalhos expostos:** `Authorization`, `Content-Disposition`.

### 5.3. Contrato Global de Resposta de Erro (`@RestControllerAdvice`)
Toda resposta de erro HTTP (`400`, `401`, `403`, `404`, `409`, `422`, `429`, `500`) — incluindo erros disparados pelos handlers do Spring Security e métodos `void` — deve retornar JSON contendo obrigatoriamente o campo `message` legível para exibição direta ao usuário:

```json
{
  "statusCode": 401,
  "message": "Sessão expirada ou credenciais inválidas.",
  "error": "Unauthorized",
  "requestId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
}
```

---

## 6. Modelagem de Oportunidades e Triagem

### 6.1. Mapa de Relacionamentos

```text
[ProfissionalSaude] 1 ────────── * [Oportunidade]
                                      │       │
                          (1:0..1)    │       │ (1:*)
                                      ▼       ▼
                        [FormularioTriagem] [Inscricao]
```

* **`ProfissionalSaude` -> `Oportunidade` (1:N):** Toda oportunidade pertence obrigatoriamente a um profissional ofertante (`ofertante_id`). Os dados do profissional (`nome`, `conselho`, `uf`, `numeroRegistro`, `ondeAtende`, `statusVerificacao`) compõem a identificação e o selo informativo na vitrine pública.
* **`Oportunidade` -> `FormularioTriagem` (1:1):** Cada oportunidade possui uma configuração de triagem (`CascadeType.ALL`, `orphanRemoval = true`) definindo o modo (`PADRAO` ou `PERSONALIZADO`), as perguntas e os documentos exigidos.
* **`Oportunidade` -> `Inscricao` (1:N):** Uma oportunidade recebe múltiplas solicitações de pacientes. As inscrições aprovadas contabilizam `vagasPreenchidas` e o total de solicitações alimenta `applicationCount`.

### 6.2. Enums Padronizados de Oportunidade (Contrato v2)
* **`StatusOportunidade`:** `RASCUNHO`, `ATIVA`, `ENCERRADA`
* **`TipoServico`:** `consulta`, `exame`, `terapia`, `procedimento`, `grupo`
* **`ModalidadeAtendimento` (`modalidade`):** `presencial`, `online`, `hibrido`
* **`ModalidadeAcesso` (`modalidadeAcesso`):** `gratuito`, `sus`, `preco-social`, `pesquisa`
* **`PublicoAlvo` (`publicoAlvo`):** `criancas`, `adolescentes`, `adultos`, `idosos`, `gestantes`
* **`FormaContato` (`formaContato`):** `plataforma`, `telefone`, `email`
* **`ModoTriagem`:** `PADRAO`, `PERSONALIZADO`
* **`TipoPerguntaTriagem`:** `TEXTO_CURTO`, `TEXTO_LONGO`, `MULTIPLA`, `SIM_NAO`

### 6.3. Entidade `Oportunidade`

| Campo | Tipo | Obrigatoriedade e Regras |
| :--- | :--- | :--- |
| `id` | `UUID` | Chave primária gerada automaticamente. |
| `ofertante` | `ProfissionalSaude` | `@ManyToOne(optional = false)` — profissional criador da vaga. |
| `titulo` | `String` | Obrigatório. |
| `descricao` | `String (TEXT)` | Obrigatório ao publicar (`ATIVA`); deve ter entre 40 e 600 caracteres. |
| `tipoServico` | `TipoServico` | Obrigatório (`consulta`, `exame`, `terapia`, `procedimento`, `grupo`). |
| `especialidade` | `String` | Obrigatório (ex.: `"cardiologia"`, `"psicologia"`, `"fisioterapia"`). |
| `modalidade` | `ModalidadeAtendimento` | Obrigatório (`presencial`, `online`, `hibrido`). |
| `modalidadeAcesso` | `ModalidadeAcesso` | Obrigatório (`gratuito`, `sus`, `preco-social`, `pesquisa`). |
| `preco` | `BigDecimal` | Nullable. Obrigatório e `> 0` apenas se `modalidadeAcesso == preco-social`; deve ser `null` nos demais casos. |
| `publicoAlvo` | `List<PublicoAlvo>` | Lista não vazia (`@ElementCollection` ou `JSONB`) com os públicos atendidos. |
| `publicoAlvoObservacao` | `String` | Opcional (complemento textual do público-alvo). |
| `vagasTotais` | `Integer` | Obrigatório (`>= 1`). |
| `vagasPreenchidas` | `Integer` | Default `0`. Incrementado ao aprovar inscrição e decrementado em caso de cancelamento pós-aprovação. |
| `cep` | `String` | Obrigatório se `modalidade` for `presencial` ou `hibrido`. |
| `endereco` | `String` | Obrigatório se `modalidade` for `presencial` ou `hibrido`. |
| `cidade` | `String` | Obrigatório para exibição na vitrine pública. |
| `uf` | `String (2 chars)` | Sigla do estado (ex.: `"DF"`). |
| `inscricoesDe` | `LocalDate` | Data de início das inscrições (`aaaa-mm-dd`). |
| `inscricoesAte` | `LocalDate` | Data limite das inscrições (`aaaa-mm-dd`), deve ser `>= inscricoesDe`. |
| `criteriosElegibilidade` | `String (TEXT)` | Texto livre opcional com requisitos da vaga. |
| `semRestricoes` | `boolean` | Default `false`. Indica ausência de restrições específicas. |
| `formaContato` | `FormaContato` | Obrigatório (`plataforma`, `telefone`, `email`). |
| `prazoRespostaDias` | `Integer` | Default `15` (usado para calcular o prazo de resposta das solicitações). |
| `status` | `StatusOportunidade` | `RASCUNHO`, `ATIVA` ou `ENCERRADA`. |
| `ultimaAtualizacao` | `Instant` | Atualizado automaticamente (`@UpdateTimestamp`). |

### 6.4. Entidade `FormularioTriagem`
* `id`: `UUID`
* `oportunidade`: `@OneToOne` com `Oportunidade`
* `modo`: `ModoTriagem` (`PADRAO` ou `PERSONALIZADO`)
* `perguntas`: `JSONB` / Texto estruturado contendo lista de objetos `{ id, rotulo, tipo, obrigatoria, opcoes }`
    * Quando `modo == PADRAO`, utiliza o conjunto fixo de 8 perguntas padrão com IDs consistentes entre Web e Mobile.
    * Quando `modo == PERSONALIZADO`, exige ao menos 1 pergunta cadastrada; perguntas do tipo `MULTIPLA` devem conter no mínimo 2 opções em `opcoes`.
* `documentosExigidos`: `JSONB` / Texto estruturado contendo lista de objetos `{ id, rotulo }` (ex.: `{"id": "rg", "rotulo": "RG"}`).

---

## 7. Regras de Negócio e Máquina de Estados de Oportunidade

```text
[CRIAR] ──► RASCUNHO ──(Publicar/Editar)──► ATIVA ──(Encerrar)──► ENCERRADA
   │                                          ▲
   └──────────────(Publicar direto)───────────┘
```

* **Criação (`POST`):** Permite salvar como `RASCUNHO` (validação flexibilizada para salvamento parcial entre etapas) ou publicar diretamente como `ATIVA` (aplica todas as validações obrigatórias).
* **Edição (`PUT`):** Permitida apenas para o profissional dono (ofertante) quando o status for `RASCUNHO` ou `ATIVA`. Oportunidades com status `ENCERRADA` não podem ser editadas (retornar `422 Unprocessable Entity` ou `409 Conflict`).
* **Encerramento (`POST /professional/opportunities/{id}/close`):** Permitido somente quando `status == ATIVA`. Tentar encerrar um `RASCUNHO` ou uma vaga já `ENCERRADA` deve retornar erro de negócio.
* **Exclusão (`DELETE`):** Permitida somente quando `status == RASCUNHO`. Se a oportunidade estiver `ATIVA`, `ENCERRADA` ou possuir inscrições vinculadas, bloquear a exclusão (`409 Conflict`).
* **Consistência de Vagas:** Na edição de uma vaga `ATIVA`, `vagasTotais` não pode ser reduzido para um valor inferior a `vagasPreenchidas`.

---

## 8. Contratos de Endpoints de Oportunidade

### 8.1. Vitrine Pública (`permitAll` — Sem Token)
* **`GET /oportunidades`:** Retorna exclusivamente oportunidades com `status == ATIVA`, paginadas no padrão Spring (`content`, `number`, `totalPages`, `totalElements`).
    * **Filtros via Query Params (opcionais):** `q` (busca textual em título, descrição e especialidade), `serviceType` (lista separada por vírgula), `specialty`, `careMode` (lista separada por vírgula), `accessType` (lista separada por vírgula), `audience` (lista separada por vírgula), `distance` (ignorado enquanto não houver geocodificação, retornando `distanceKm: null`), `page` e `size`.
* **`GET /oportunidades/{id}`:** Retorna os detalhes completos da oportunidade, os blocos `provider` (`id`, `name`, `kind`, `verified`) e `professional` (`name`, `council`, `state`, `number`), além da estrutura `triage` (`mode`, `questions`, `documents`).

### 8.2. Painel do Profissional (`hasRole("PROFISSIONAL_SAUDE")`)
* **`GET /professional/opportunities`:** Lista todas as oportunidades do profissional logado (`RASCUNHO`, `ATIVA` e `ENCERRADA`), incluindo o contador `applicationCount`.
* **`GET /professional/opportunities/{id}`:** Detalhe completo da oportunidade do profissional logado (inclusive rascunhos e encerradas).
* **`POST /oportunidades` (ou `/professional/opportunities`):** Cria oportunidade + `FormularioTriagem` (`201 Created`).
* **`PUT /oportunidades/{id}` (ou `/professional/opportunities/{id}`):** Atualiza oportunidade + `FormularioTriagem` (`200 OK`).
* **`POST /professional/opportunities/{id}/close`:** Altera status de `ATIVA` para `ENCERRADA` (`200 OK`).
* **`DELETE /oportunidades/{id}` (ou `/professional/opportunities/{id}`):** Exclui definitivamente apenas se `status == RASCUNHO` (`204 No Content`).

---

## 9. Modelagem e Regras de Solicitações (`Inscricao`), Documentos e Mensagens

### 9.1. Estados da Solicitação (`StatusInscricao`)
* **`RECEBIDA`:** Estado inicial ao submeter a solicitação.
* **`EM_ANALISE`:** Profissional abriu ou iniciou avaliação da solicitação.
* **`AGUARDANDO_INFORMACOES`:** Profissional solicitou esclarecimento ou anexo complementar ao paciente.
* **`APROVADA`:** Solicitação aceita pelo profissional (incrementa `vagasPreenchidas` e libera chat/agendamento).
* **`RECUSADA`:** Solicitação negada pelo profissional (exige `motivoDecisao` obrigatório).
* **`CANCELADA`:** Solicitação cancelada pelo paciente.

### 9.2. Entidades do Fluxo de Solicitação

#### `Inscricao`
* `id`: `UUID`
* `protocolo`: `String` (código numérico/alfanumérico único gerado na criação, ex.: `"20377"`)
* `paciente`: `@ManyToOne(optional = false)` com `Paciente`
* `oportunidade`: `@ManyToOne(optional = false)` com `Oportunidade`
* `status`: `StatusInscricao`
* `respostasTriagem`: `JSONB` / Texto estruturado com lista `{ perguntaId, pergunta, resposta }`
* `consentimentoDadosSaudeEm`: `Instant` (obrigatório no envio da inscrição)
* `dataInscricao`: `Instant`
* `prazoResposta`: `Instant` (calculado como `dataInscricao + oportunidade.prazoRespostaDias`)
* `decididaEm`: `Instant` (nullable)
* `motivoDecisao`: `String` (obrigatório quando `status == RECUSADA`)
* `pedidoInformacao`: `JSONB` / Embedded (`question`, `dueAt`)
* `combinado`: `JSONB` / Embedded (`when`, `place`, `bring`) preenchido na aprovação

#### `DocumentoInscricao`
* `id`: `UUID`
* `inscricao`: `@ManyToOne` com `Inscricao`
* `nomeArquivo`: `String`
* `tamanhoBytes`: `long` (máximo 10 MB por arquivo)
* `tipo`: `String` (MIME type restrito a `application/pdf`, `image/jpeg`, `image/png`)
* `arquivo`: `byte[]` (ou referência de storage)
* `enviadoEm`: `Instant`

#### `Mensagem`
* `id`: `UUID`
* `inscricao`: `@ManyToOne` com `Inscricao`
* `autor`: `TipoPerfil` (`PACIENTE` ou `PROFISSIONAL_SAUDE`)
* `texto`: `String` (obrigatório)
* `enviadaEm`: `Instant`
* `lida`: `boolean` (usado para calcular `unreadMessages` e `hasUnreadMessage`)

### 9.3. Regras de Negócio de Solicitações
* **Unicidade de Inscrição Ativa:** Impedir que o mesmo paciente crie duas inscrições simultâneas ativas (`RECEBIDA`, `EM_ANALISE`, `AGUARDANDO_INFORMACOES`, `APROVADA`) para a mesma oportunidade (`409 Conflict`).
* **Disponibilidade da Vaga:** Só permitir novas inscrições se `oportunidade.status == ATIVA` e `vagasPreenchidas < vagasTotais`.
* **Consentimento LGPD:** Rejeitar criação de inscrição (`400`/`422`) caso `healthDataConsentAt` não seja informado.
* **Mensageria (REST Polling):** O envio e leitura de mensagens ocorrem via endpoints REST (`GET`/`POST` em `/messages`) após a aprovação da solicitação (`canMessage == true`).

---

## 10. Contratos de Endpoints de Solicitações e Mensagens

### 10.1. Endpoints do Paciente (`hasRole("PACIENTE")`)
* **`POST /applications`:** Cria nova inscrição com respostas da triagem, `healthDataConsentAt` e anexos (`201 Created`).
* **`GET /applications`:** Lista todas as inscrições do paciente autenticado.
* **`GET /applications/{id}`:** Retorna detalhe completo da solicitação, `infoRequest`, `appointment` (combinado), `unreadMessages` e documentos.
* **`POST /applications/{id}/cancel`:** Cancela a solicitação (`status = CANCELADA`). Se já estava `APROVADA`, decrementa `vagasPreenchidas` da oportunidade.
* **`POST /applications/{id}/reply`:** Responde a um pedido de informação (`{ "text": "..." }` + anexo opcional), retornando o status para `EM_ANALISE`.
* **`GET /applications/{id}/messages` e `POST /applications/{id}/messages`:** Lista (marcando mensagens do profissional como lidas) e envia mensagens (`{ "text": "..." }`).
* **`DELETE /applications/{id}/documents`:** Remove documentos enviados pelo paciente na solicitação.

### 10.2. Endpoints do Profissional (`hasRole("PROFISSIONAL_SAUDE")`)
* **`GET /professional/requests`:** Lista solicitações recebidas nas oportunidades do profissional logado, incluindo `hasUnreadMessage` e `canMessage`.
* **`GET /professional/requests/{id}`:** Detalhe da solicitação com respostas da triagem, lista de documentos (`id`, `name`, `sizeKb`, `sentAt`) e histórico de `messages`.
* **`POST /professional/requests/{id}/decision`:** Recebe `{ "status": "APROVADA" | "RECUSADA", "reason": "...", "appointment": { "when", "place", "bring" } }`. Exige `reason` se `RECUSADA`; incrementa `vagasPreenchidas` se `APROVADA`.
* **`POST /professional/requests/{id}/info-request`:** Recebe `{ "text": "..." }`, registra pergunta e prazo e altera o status para `AGUARDANDO_INFORMACOES`.
* **`POST /professional/requests/{id}/messages`:** Envia mensagem ao paciente (`{ "text": "..." }`).
