Instruções do projeto — Spring Boot
Objetivo
Este repositório contém uma API Spring Boot. Preserve a arquitetura existente e implemente somente o que a tarefa solicitar.
Princípios de implementação
- Antes de alterar código, inspecione apenas os arquivos diretamente relacionados à tarefa.
- Não crie novas dependências, módulos, entidades, endpoints, migrations ou alterações de infraestrutura sem solicitação explícita.
- Não refatore código não relacionado ao pedido.
- Preserve convenções existentes de pacotes, nomenclatura e tratamento de exceções.
- Controllers recebem DTOs e não devem expor entidades JPA diretamente.
- Regras de negócio pertencem à camada de serviço; persistência pertence aos repositórios.
- Use record para DTOs imutáveis, quando isso estiver alinhado ao padrão já usado no projeto.
  Documentação local
- A pasta docs/ contém contexto local do projeto, como regras de negócio, arquitetura e decisões técnicas.
- Consulte somente o documento diretamente necessário para a tarefa atual.
- Não criar, mover, versionar, excluir ou modificar arquivos em docs/ sem solicitação explícita.
- Trate a documentação como fonte de verdade quando houver conflito com suposições.
  Política de testes
- Não criar, modificar ou executar testes automaticamente.
- Só trabalhar em testes quando a solicitação mencionar explicitamente “crie testes”, “escreva testes”, “teste esta feature” ou equivalente.
- Não adicionar testes como parte implícita da implementação de uma feature.
- Não executar mvn test, ./mvnw test, mvn verify ou testes de integração sem solicitação explícita.
- Quando testes forem solicitados, criar somente os cenários diretamente relacionados à regra de negócio ou endpoint informado.
- Antes de escrever testes, resumir os cenários que serão cobertos.
  Validação padrão
- Para mudanças de código, valide somente com ./mvnw compile ou mvn compile, conforme o projeto.
- Não executar Docker, Docker Compose, migrations, chamadas HTTP externas ou a suíte completa automaticamente.
- Não repetir comandos que falharam sem antes identificar e corrigir a provável causa.
- Se a compilação falhar, corrija somente falhas relacionadas à alteração atual e informe bloqueios externos.
  Comunicação e entrega
- Antes de implementar, apresente em poucas linhas os arquivos que pretende alterar e o resultado esperado.
- Ao concluir, informe os arquivos alterados, a validação executada e limitações ou pendências encontradas.
- Quando requisitos de negócio estiverem ambíguos, pare e peça esclarecimento antes de decidir ou implementar.
- Não fazer commits, pushes, deploys ou alterações externas sem solicitação explícita.