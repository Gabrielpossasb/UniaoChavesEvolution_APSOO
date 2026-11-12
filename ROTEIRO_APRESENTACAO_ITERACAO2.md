# Roteiro da apresentação: 2ª iteração (Solicitar Orçamento, RF-5)

Duração sugerida: 10 a 12 minutos, cerca de 3 minutos por pessoa. A ordem segue o caminho da
modelagem: requisitos, análise, design e implementação. Cada parte mostra um artefato e diz o que ele
decidiu para a parte seguinte.

**Antes de começar:** com o MySQL ligado, rode `mysql -u root < sql/schema.sql` dentro de
`Implementacao/registrar-ordem-servico` e abra o `MenuPrincipal`. O banco volta aos dados de
exemplo e o próximo orçamento sai como ORC-2026-0234.

---

## 1. Luiz Phellipe: o caso de uso e a Descrição Expandida

**Mostrar:** diagrama de casos de uso, Descrição Expandida e mockup.

**Explicar:**
- **O que é o caso de uso.** O cliente chega na oficina (ou entra em contato) pedindo um orçamento. O
  técnico registra quem é o cliente, qual o veículo, o tipo de serviço e os valores. O orçamento é a
  porta de entrada do negócio: sem ele não existe Ordem de Serviço (1ª iteração).
- **Quem usa.** Só o Técnico de Módulos e o Técnico de Chaves (PRE-01). O Administrador não é ator
  deste caso de uso.
- **Fluxo principal, em uma frase:** o técnico identifica o cliente, escolhe o veículo, informa o
  segmento, preenche os dados técnicos, inclui os serviços com valor e registra. O sistema gera o
  número ORC-AAAA-NNNN e grava com situação "Em análise".
- **Variantes:** o segmento muda o formulário.
  - **V1, Módulos:** tipo de módulo, modelo da ECU, identificação e sintoma.
  - **V2, Chaves:** tipo de chave, quantidade, transponder e situação do imobilizador.
- **Regras mais importantes:**
  - RN-01: precisa de ao menos um serviço;
  - RN-03: só serviços do segmento escolhido;
  - RN-05: o valor estimado é a soma dos itens;
  - RN-07: a validade não pode ser anterior à data da solicitação;
  - RN-08: numeração sequencial que reinicia a cada ano.
- **Fluxos alternativos (citar dois):**
  - FA-02: o cliente não tem cadastro, então é cadastrado na hora com nome e telefone;
  - FA-12D: o registro não pode ser concluído, então o orçamento é salvo como rascunho.
- **Mockup:** uma tela só, dividida em blocos, e o bloco de dados técnicos troca conforme o segmento.

**Pergunta provável:** por que dois segmentos? Porque a empresa tem dois serviços com dados técnicos
diferentes: reparo de centrais (ECU) e confecção de chaves.

---

## 2. Eduardo José: análise (DSS, contrato e modelo conceitual)

**Mostrar:** DSS, Contrato de Operação e refinamento do modelo conceitual.

**Explicar:**
- **DSS.** Trata o sistema como uma caixa preta e mostra só as mensagens que a tela manda para ele e
  o que volta. São 3 diagramas: fluxo normal, alternativos e alternativos das variantes. Cada
  mensagem virou um método da controladora, por exemplo `informarCliente`, `selecionarVeiculo`,
  `informarSegmento`, `incluirItem` e `registrarOrcamento`.
- **Por que só uma operação tem contrato.** As outras mensagens apenas consultam ou guardam o que o
  técnico vai informando. Quem efetiva tudo é `registrarOrcamento(dataValidade, observacoes)`.
- **O contrato:**
  - pré-condições: cliente e veículo identificados, segmento e dados técnicos informados, ao menos
    um item e validade correta;
  - pós-condições: orçamento criado na subclasse do segmento, com número, data, situação
    "Em análise" e valor estimado, associado a cliente, veículo, técnico e itens, e gravado.
- **Modelo conceitual refinado.** Mudanças em relação à 1ª iteração:
  - Orçamento ganhou duas especializações, **OrcamentoModulos** e **OrcamentoChaves**, porque cada
    segmento guarda dados diferentes;
  - surgiram **ModuloECU** (a central do veículo, que pode aparecer em vários atendimentos) e
    **InformacoesSegurancaVeiculo** (senhas e código mecânico).

**Pergunta provável:** por que herança e não um campo "segmento"? Com herança, cada tipo só tem os
atributos que usa, e as regras ficam em quem conhece os dados.

---

## 3. Caetano Raimundo: design (classes, comunicação e banco)

**Mostrar:** Diagrama de Classes v2, Diagrama de Comunicação e Mapeamento Objeto-Relacional v2.

**Explicar:**
- **Diagrama de Classes v2.** Junta as duas iterações e separa as camadas:
  - **boundary:** `TelaSolicitarOrcamento`;
  - **control:** `ControladoraSolicitarOrcamento`, que guarda os dados da sessão de atendimento;
  - **entidades:** `Orcamento` e subclasses, `ItemOrcamento`, `Cliente`, `Veiculo`, `ModuloECU`;
  - **DAOs:** uma por entidade gravada.
- **Diagrama de Comunicação de `registrarOrcamento`.** Mostra a ordem interna, mensagens 1.1 a 1.15:
  1. busca o técnico (1.1) e gera o número (1.2);
  2. cria o orçamento da subclasse certa (1.3 ou 1.4);
  3. no segmento Módulos, procura o módulo do veículo e o cria se não existir (1.7 a 1.10);
  4. adiciona os itens (1.11) e associa cliente, veículo e técnico (1.12 a 1.14);
  5. com `sinalizarRegistro` (1.15), marca a data, põe "Em análise" e calcula o valor.
- **Destaque:** no código, essas mensagens estão na mesma ordem e numeradas nos comentários. Dá para
  ler o diagrama e o método lado a lado.
- **Mapeamento OR v2.** A herança virou "uma tabela por subclasse":
  - `TB_Orcamento` guarda o que é comum;
  - `TB_OrcamentoModulos` e `TB_OrcamentoChaves` guardam o específico, com a mesma chave;
  - gravar um orçamento são vários INSERTs numa transação: ou grava tudo, ou nada.

**Pergunta provável:** o que acontece se o banco cair no meio? A transação é desfeita, nada fica
pela metade e a tela mostra "Não foi possível registrar o orçamento" (FA-13), com os dados mantidos.

---

## 4. Gabriel Abraão: implementação e demonstração

**Mostrar:** o sistema rodando.

**Explicar rapidamente:**
- Java com Swing (desktop), MySQL e JDBC, em 4 camadas: tela, controladora, negócio e persistência.
- A tela não tem SQL e não conhece as DAOs. Erros de regra sobem como mensagem para a tela.
- Cada campo é validado ao sair dele e de novo ao registrar.

**Demonstração (passo a passo):**
1. No menu, escolher o usuário **Gabriel Abraão (Técnico de Módulos)** e abrir *Solicitar Orçamento*.
   O formulário já mostra o número provisório e a data (passo 1).
2. Origem **Presencial**; no cliente, digitar **024.577.310-03**. Aparece Marcos Antônio Ribeiro e
   os veículos dele (passos 2 e 3).
3. Escolher o veículo **QAF-7C21**. Aparece o histórico técnico (passos 4 e 5).
4. Segmento **Módulos**. Aparecem o bloco técnico e os serviços de Módulos (passos 6 e 7).
5. Em identificação, escolher **MM-88214-C**: tipo e modelo vêm preenchidos e aparecem os
   atendimentos anteriores desse módulo (variante V1, 8.1 e 8.2). Digitar um sintoma.
6. Informar a validade e clicar em **Registrar** sem incluir serviço: aparece "O orçamento deve
   conter ao menos um serviço" (FA-12A, RN-01).
7. Incluir um serviço com valor e registrar: "Orçamento **ORC-2026-0234** registrado com sucesso",
   com situação EM ANÁLISE (passo 13).
8. Se sobrar tempo: mostrar no Registrar OS que a 1ª iteração continua funcionando.

**Fechamento:** as diferenças encontradas entre os documentos (por exemplo, nomes que mudaram entre
diagramas) e a decisão tomada em cada caso estão registradas na seção 7 do `RELATORIO_ITERACAO2.md`.

---

**Observação:** a divisão por pessoa segue as etapas da modelagem. Troquem os nomes se a participação
real em cada artefato foi diferente.
