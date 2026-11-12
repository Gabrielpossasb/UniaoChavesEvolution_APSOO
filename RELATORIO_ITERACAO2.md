# Relatório da 2ª iteração: Solicitar Orçamento (RF-5)

Projeto União Evolutions · APSOO 2026.2 · Grupo G5 · relatório de 07/11/2026.

O caso de uso **Solicitar Orçamento** foi implementado dentro do projeto da 1ª iteração
(`Implementacao/registrar-ordem-servico/`), em Java + Swing + JDBC + MySQL, nas mesmas 4 camadas.
O **Registrar Ordem de Serviço** continua funcionando. Ele foi adaptado ao Mapeamento OR v2 e
conferido contra uma linha de base gravada antes de qualquer mudança. Nada foi commitado: tudo está
no working tree.

**Verificação:**

- o projeto compila sem erros;
- **167 verificações de interface** passaram (10 cenários exercitando a tela real, um deles
  conferindo a ordem do DSS com uma controladora espiã);
- **69 verificações sem interface** passaram (ordem das mensagens, regras, validadores e regressão
  do Registrar OS);
- nenhuma falha.

As correções feitas depois da primeira entrega estão na **seção 10, Rodadas de correção**.

---

## 1. Inventário: documentos lidos

Os `.docx` foram lidos como texto e os PDFs como texto e como imagem. Os `.asta` (Astah, Java
serializado dentro de um ZIP) tiveram suas strings extraídas, para confirmar a grafia exata de
classes, atributos, operações e mensagens que a imagem rasterizada não deixava ler.

| Caminho | O que forneceu |
|---|---|
| `2° Iteração Solicitar Orçamento/Requisitos/Descricao_Expandida_Criar_Orcamento.docx` | **Especificação principal**: PRE-01..04, POS-01..07, RN-01..09, fluxo principal (13 passos), FA-00, FA-02, FA-04, FA-09A/B, FA-11A/B, FA-12A/B/C/D, FA-13, variantes V1/V2, FAV-8.1, FAV-8.2, FAV-8.4A, FAV-8.4B e o texto das mensagens |
| `2° Iteração Solicitar Orçamento/Requisitos/MOCKUP-solicitar-orcamento (1).pdf` | Tela 1 (V1 e V2), Tela 2 (8 janelas) e Tela 3 (validações): layout, rótulos, dicas e botões |
| `2° Iteração Solicitar Orçamento/Analise/Contrato_Operacao_Solicitar_Orcamento.docx` | Pré e pós-condições de `registrarOrcamento(dataValidade, observacoes)` |
| `2° Iteração Solicitar Orçamento/Analise/DSS - Solicitar Orçamento.pdf` + `DSS_Solicitar_Orcamento (1).asta` | DSS em 3 diagramas: fluxo normal (mensagens 1-15), fluxos alternativos (1-21) e alternativos das variantes (1-15) |
| `2° Iteração Solicitar Orçamento/Analise/Refinamento_do_modelo_conceitual*.{pdf,docx,asta}` | Modelo conceitual v2 (Orcamento com subclasses, ModuloECU, InformacoesSegurancaVeiculo) |
| `2° Iteração Solicitar Orçamento/Design/diagrama_classes_v2.pdf` + `diagrama_de_classes_v2.asta` | **Diagrama de classes v2**: nomes, atributos, tipos, visibilidade e assinaturas |
| `2° Iteração Solicitar Orçamento/Design/Diagrama de comunicação.docx` + `Diagrama de Comunicacao 2°.asta` | **Ordem das mensagens** 1 → 1.15.3 de `registrarOrcamento` (o projeto não tem diagrama de sequência de design; este é o equivalente) |
| `2° Iteração Solicitar Orçamento/Design/Mapeamento_Objeto_Relacional_v2.docx` | Tabelas e colunas do `schema.sql` |
| `1° interação Registrar Ordem de serviço/Requisitos/Documento Requisitos União Evolutions {1.0,2.0,3.0}.docx` | RF-5 e a regra "Em análise"; RNF-4 (rede local) e RNF-7 (desktop) |
| `1° interação …/Requisitos/Casos de uso 01 {PDF,Atualizado PDF}.pdf` + `CasosDeUso01 {Atualizado,Desatualizado}.asta` | Atores do UC 1 "Solicitar Orçamento": Técnico de Módulos e Técnico de Chaves |
| `1° interação …/Requisitos/Descricao_Expandida_Registrar_Ordem_de_Servico.docx`, `MOCKUP-registrar-ordem-de-servico.pdf`, `Questionario_Requisitos_UniaoEvolutions-v2.pdf`, `respostas_doc_requisitos.pdf` | Contexto da 1ª iteração e do domínio |
| `1° interação …/Analise/*` (DSS, contrato, modelo e mapa conceitual, `FluxoNormal.asta`, `fluxos alternativos Corrijido.asta`) | Contexto da 1ª iteração |
| `1° interação …/Design/*` (diagrama de classes, comunicação, Mapeamento OR) | Contexto da 1ª iteração (substituído pelo v2 onde há conflito) |
| `1° interação …/Modelagem_Negocio-CORRIGIDO!…/*` | Diagrama de atividades do negócio |
| `Gerencia_Projeto/Plano de iterações.docx` | Plano de iterações (ver contradição 7.5) |
| `membros_do_grupo.docx`, `Participacao no Trabalho Prático.docx` | Equipe |
| `Implementacao/registrar-ordem-servico/{README,DOCUMENTACAO,RASTREABILIDADE}.md` + `src/`, `sql/` | Arquitetura, padrões e estilo da 1ª iteração |
| `Implementacao/cadastrar-cliente/*`, `Implementacao/*.rar` | Ver seção 2 |

Não havia diagrama de atividade nem de estado da 2ª iteração, nem glossário separado. Os conceitos
estão no Documento de Requisitos 3.0.

---

## 2. O que existia em `cadastrar-cliente` e o que foi aproveitado

**Ninguém tinha começado o Solicitar Orçamento ali.** A pasta (de 28/09/2026, anterior à 1ª iteração
corrigida) contém um CRUD isolado de **Cadastrar Cliente**:

- `Cliente(id, nome, cpf, telefone, endereco)`;
- tabela `cliente` (minúscula);
- `ClienteDAO` com `incluir/alterar/excluir/buscar/listar`;
- `ClienteView` e um `README`/`DOCUMENTACAO`.

Esse caso de uso não existe no diagrama de casos de uso nem no plano de iterações.

**Aproveitado no código: nada**, porque nenhum nome bate com o diagrama de classes v2:

- o v2 tem `cpfCnpj`, não `cpf`;
- o v2 não tem `endereco`;
- o v2 tem `TB_Cliente`, não `cliente`;
- a `ClienteDAO` do v2 tem `buscarPorCpfCnpj` e `salvar`.

Aproveitada só a ideia de aceitar CPF **ou** CNPJ, que o próprio v2 confirma. **A pasta não foi
alterada.**

Os `.rar` em `Implementacao/` são cópias antigas das duas pastas. O de `registrar-ordem-servico` é
anterior ao conteúdo atual da pasta. Também não foram alterados.

---

## 3. Arquivos criados e modificados

Todos em `Implementacao/registrar-ordem-servico/`, exceto os dois da raiz.

### Criados

| Arquivo | Conteúdo |
|---|---|
| `src/uniaoevolutions/MainSolicitarOrcamento.java` | Ponto de entrada da 2ª iteração; argumento opcional = id do usuário logado |
| `src/uniaoevolutions/MenuPrincipal.java` | Rodada 4 (ACRÉSCIMO): menu com botões para abrir as duas telas e escolher o usuário logado, sem argumento na linha de comando |
| `src/uniaoevolutions/controle/ControladoraSolicitarOrcamento.java` | `<<control>>`: 6 atributos e 19 operações do diagrama; `registrarOrcamento` = mensagens 1.1-1.15.3 |
| `src/uniaoevolutions/view/TelaSolicitarOrcamento.java` | `<<boundary>>`: Telas 1, 2 e 3 do mockup, com validação ao perder o foco e no envio |
| `src/uniaoevolutions/view/Validacoes.java` | Apoio da interface: formato de CPF/CNPJ (DV), data, valor, telefone, placa, ano e chassi |
| `src/uniaoevolutions/negocio/ModuloECU.java` | Entidade do v2, com `<<create>>` |
| `src/uniaoevolutions/negocio/OrcamentoModulos.java` | Subclasse V1: `<<create>>`, `setModuloIlegivel`, `associarModulo` |
| `src/uniaoevolutions/negocio/OrcamentoChaves.java` | Subclasse V2: `<<create>>`, `setComprovacaoPropriedade` |
| `src/uniaoevolutions/negocio/InformacoesSegurancaVeiculo.java` | Entidade do diagrama v2 (rodada 3): `codigoMecanico`, `senhaImobilizador`, `senhaAlarme`, `senhaRadio`; associação "Veiculo 1 possui 0..1" |
| `src/uniaoevolutions/persistencia/ClienteDAO.java` | `buscarPorCpfCnpj`, `salvar` (+ acréscimos `buscarPorNomeOuTelefone` e `buscarClientes`) |
| `src/uniaoevolutions/persistencia/VeiculoDAO.java` | `buscarPorPlaca`, `salvar` (+ acréscimo `buscarPorCliente`) |
| `src/uniaoevolutions/persistencia/ServicoBaseDAO.java` | `buscarPorCategoria`, `buscarServico` |
| `src/uniaoevolutions/persistencia/ModuloECUDAO.java` | `buscarModulo(vei, identificacaoModulo)` (+ acréscimo `buscarModulosDoVeiculo`) |
| `test/uniaoevolutions/BancoDeTeste.java` | Recria o banco pelo `schema.sql` e faz consultas de conferência |
| `test/uniaoevolutions/controle/TesteControladoras.java` | 69 verificações: ordem das mensagens (DAOs espiãs), regras, validadores e regressão do Registrar OS |
| `test/uniaoevolutions/view/Robo.java` | Apoio aos testes de interface (componentes por nome, foco, janelas modais e capturas) |
| `test/uniaoevolutions/view/TesteTelaSolicitarOrcamento.java` | 10 cenários, 167 verificações, sobre a tela real (inclui digitação real de teclado e a ordem do DSS) |
| `test/uniaoevolutions/view/ControladoraEspia.java` | Controladora real que registra as mensagens do DSS aceitas, na ordem em que chegam (cenário `ordemDoDss`) |
| `docs/telas-iteracao2/*.png` | Capturas geradas pelos testes, para comparar com o mockup |
| `/RELATORIO_ITERACAO2.md` | Este relatório |
| `/RESUMO_ITERACAO2.md` | Versão curta deste relatório, com uma linha sobre cada arquivo |
| `/CLAUDE.md` | Arquitetura, como rodar e armadilhas |

### Modificados

| Arquivo | Mudança |
|---|---|
| `sql/schema.sql` | Reescrito pelo Mapeamento OR v2: `TB_Cliente.cpfCnpj`; colunas de módulo e senhas saem de `TB_Veiculo` para `TB_ModuloECU` e `TB_InformacoesSegurancaVeiculo`; `TB_Orcamento` ganha `numero/dataValidade/observacoes/motivoCancelamento/idUsuario` e perde `segmentoPretendido`; novas `TB_OrcamentoModulos/Chaves`. Dados de exemplo para testar só selecionando: 6 clientes, 6 veículos, módulos, senhas, 10 orçamentos e 3 OS anteriores (ver 10.1) |
| `src/uniaoevolutions/negocio/Orcamento.java` | Vira superclasse com os atributos e operações do v2 (`setDataSolicitacao` e `setStatus` privados, como no diagrama). Mantém a API da 1ª iteração |
| `src/uniaoevolutions/negocio/Cliente.java` | `cpfCnpj` e `<<create>> Cliente(nome, telefone)` |
| `src/uniaoevolutions/negocio/Veiculo.java` | `<<create>> Veiculo(placa, …)` e `associarModulo(mod)`; os campos da 1ª iteração ficam |
| `src/uniaoevolutions/negocio/ItemOrcamento.java` | `<<create>> ItemOrcamento(servico, valorProposto)` |
| `src/uniaoevolutions/persistencia/OrcamentoDAO.java` | SELECT adaptado às tabelas v2 (segmento pela subclasse; módulo e senhas pelas tabelas novas). Novos: `gerarNumeroOrcamento`, `salvar`, `salvarRascunho`, `buscarAtendimentosModulo`, `listarOrcamentos` |
| `bin/` | Recompilado |
| `README.md` | Seção curta da 2ª iteração |

Modificados na rodada 2 (seção 10.2):

| Arquivo | Mudança |
|---|---|
| `src/uniaoevolutions/controle/ControladoraRegistrarOS.java` | Mensagens na ordem do diagrama de comunicação (1.7 antes de 1.8-1.10; 1.5 antes de 1.6); acréscimos movidos para depois dos métodos do diagrama. Fins de linha CRLF preservados |
| `src/uniaoevolutions/negocio/OrdemDeServico.java` | Rodada 3: parâmetro `associarOrcamento(ocr)` renomeado para `orc`, como no diagrama v2 |
| `DOCUMENTACAO.md`, `RASTREABILIDADE.md` (1ª iteração) | Trechos que descreviam a inversão de 1.7 atualizados para a ordem atual. As mudanças antigas não commitadas desses arquivos foram preservadas |

**Não alterados:**

- `TelaRegistrarOrdemServico`, `Main`, `OrdemServicoDAO`, `UsuarioDAO`, `OrdemDeServicoModulos`,
  `OrdemDeServicoChaves`.

---

## 4. Rastreabilidade

Arquivo da controladora: `src/uniaoevolutions/controle/ControladoraSolicitarOrcamento.java` (abaixo,
"Ctrl"). Arquivo da tela: `src/uniaoevolutions/view/TelaSolicitarOrcamento.java` ("Tela"). Números
de linha da versão entregue.

### 4.1 Diagrama de comunicação: `registrarOrcamento`, na ordem do diagrama

| Mensagem | Código | Ctrl, linha |
|---|---|---|
| (pré-condições do contrato: RN-01, RN-04, RN-07) | `verificarPreCondicoes(dataValidade)` | 406 |
| 1 `registrarOrcamento(dataValidade, observacoes)` | método | 405 |
| 1.1 `tec = buscarUsuario(idUsuarioLogado)` | `usuarioDAO.buscarUsuario(idUsuarioLogado)` | 409 |
| 1.2 `numero = gerarNumeroOrcamento()` | `orcamentoDAO.gerarNumeroOrcamento()` | 412 |
| 1.3 `[seg="Modulos"] <<create>> OrcamentoModulos(…)` | `new OrcamentoModulos(numero, dataValidade, origemAtendimento, observacoes, sintomaRelatado, diagnosticoPendenteBancada)` | 418 |
| 1.4 `[seg="Chaves"] <<create>> OrcamentoChaves(…)` | `new OrcamentoChaves(…10 parâmetros…)` | 424 |
| 1.5 `[identificacaoIlegivel] setModuloIlegivel(…)` | `((OrcamentoModulos) orc).setModuloIlegivel(…)` | 431 |
| 1.6 `[perdaTotal] setComprovacaoPropriedade(…)` | `((OrcamentoChaves) orc).setComprovacaoPropriedade(…)` | 437 |
| 1.7 `[seg="Modulos"] mod = buscarModulo(vei, identificacaoModulo)` | `moduloECUDAO.buscarModulo(vei, identificacaoModulo)` | 443 |
| 1.8 `[mod == null] <<create>> ModuloECU(…)` | `new ModuloECU(tipoModulo, modeloECU, identificacaoModulo)` | 448 |
| 1.9 `[mod criado] associarModulo(mod)` → vei | `vei.associarModulo(mod)` | 452 |
| 1.10 `[seg="Modulos"] associarModulo(mod)` → orc | `((OrcamentoModulos) orc).associarModulo(mod)` | 458 |
| 1.11 `[para cada item] adicionarItem(item)` | `orc.adicionarItem(item)` | 463 |
| 1.12 `associarCliente(cli)` | `orc.associarCliente(cli)` | 467 |
| 1.13 `associarVeiculo(vei)` | `orc.associarVeiculo(vei)` | 470 |
| 1.14 `associarTecnico(tec)` | `orc.associarTecnico(tec)` | 473 |
| 1.15 `sinalizarRegistro()` | `orc.sinalizarRegistro()` | 477 |
| 1.15.1 `setDataSolicitacao(dataAtual)` | `negocio/Orcamento.java` | 116 |
| 1.15.2 `setStatus("Em análise")` | `negocio/Orcamento.java` | 117 |
| 1.15.3 `calcularValorEstimado()` | `negocio/Orcamento.java` | 118 |
| (pós-condição "orc foi persistido") | `orcamentoDAO.salvar(orc)` | 480 |

A ordem 1.1 → 1.2 → 1.7 → salvar é verificada em tempo de execução por
`TesteControladoras.ordemDasMensagens` (DAOs espiãs).

### 4.2 DSS (Interface → Sistema): operação e onde a tela a envia

| Mensagem do DSS | Operação | Ctrl | Tela (chamada) |
|---|---|---|---|
| Normal 1 | `iniciarOrcamento(): Orcamento` | 154 | 1023 |
| Normal 3 / Alt. 2 | `informarCliente(origemAtendimento, cpfCnpjCliente): Cliente` | 179 | 1062 |
| Alt. 4 (FA-02) | `cadastrarCliente(nome, telefone): Cliente` | 218 | 2138 |
| Normal 5 | `selecionarVeiculo(placa): Veiculo` | 230 | 1305 |
| Alt. 6 (FA-04) | `cadastrarVeiculo(placa, marca, modelo, anoModelo, motorizacao, numeroChassi): Veiculo` | 243 | 2218 |
| Normal 7 | `informarSegmento(segmento): List<ServicoBase>` | 263 | 1422, 1682 |
| Normal 9 (V1) | `informarDadosModulo(tipoModulo, modeloECU, identificacaoModulo, sintomaRelatado): List<Orcamento>` | 280 | 1486 |
| Variantes 2 (FAV-8.1) | `informarIdentificacaoIlegivel(tipoModulo, sintomaRelatado)` | 297 | 1476 |
| Variantes 4 (FAV-8.1) | `informarDescricaoModulo(registroFotografico, descricaoFisicaPlaca): List<Orcamento>` | 307 | 1479 |
| Variantes 6 (FAV-8.2) | `informarDiagnosticoPendente()` | 316 | 1492, 1548 |
| Normal 11 / Var. 13, 15 (V2, FAV-8.4B) | `informarDadosChaves(…6 parâmetros…): String` | 325 | 1603 |
| Variantes 9 (FAV-8.4A) | `informarComprovacaoPropriedade(documentoPropriedade, nomeSolicitante, documentoSolicitante): ServicoBase` | 348 | 1656 |
| Normal 13 / Alt. 8 / Var. 11 | `incluirItem(idServicoBase, valorProposto): double` | 365 | 1724 |
| Alt. 10 (FA-11A) | `removerItem(idItem): double` | 386 | 1776 |
| Alt. 12 (FA-11B) | `alterarValorItem(idItem, novoValorProposto): double` | 392 | 1813 |
| Normal 15 / Alt. 14 | `registrarOrcamento(dataValidade, observacoes): Orcamento` | 405 | 1851 |
| Alt. 16 (FA-12D) | `salvarRascunhoOrcamento(observacoes)` | 489 | 2011 |
| Alt. 18 (FA-00) | `solicitarConfirmacaoDesistencia(): String` | 553 | 2034 |
| Alt. 20 (FA-00) | `descartarOrcamento(): List<Orcamento>` | 558 | 2040 (e 2020, FA-12D) |
| Alt. 21 `continuarEditando()` | Técnico → Interface, sem mensagem ao Sistema | - | 2037 |
| (acréscimo) "listaVeiculos" de Normal 3 | `consultarVeiculosVinculados()` | 568 | 1279 |
| (acréscimo) lista de clientes da busca do mockup | `consultarClientes(trecho)` | 576 | 1182 |
| (acréscimo) módulos já registrados do veículo | `consultarModulosDoVeiculo()` | 581 | 1220 |
| (acréscimo) "historicoTecnico" de Normal 5 | `consultarHistoricoTecnico()` | 589 | 1340 |

### 4.3 Diagrama de comunicação da 1ª iteração: `registrarOrdemServico`

Fonte: `1° interação…/Design/Diagrama de Comunicação.corrigido.docx` (29/09/2026). O `.asta` da
pasta está desatualizado (ainda chama a operação de `abrirOS`). Arquivo:
`src/uniaoevolutions/controle/ControladoraRegistrarOS.java`.

| Mensagem do diagrama | Código | Linha |
|---|---|---|
| 1.1 `orc = buscarOrcamento(orcamentoSelecionado)` | `orcamentoDAO.buscarOrcamento(orcamentoSelecionado)` | 120 |
| 1.2 `vei = buscarVeiculo(orc)` (`:VeiculoDAO`) | `orc.getVeiculo()`, do diagrama de classes (ver 7.15) | 131 |
| 1.3 `tec = buscarTecnico(tecnicoSelecionado)` (`:TecnicoDAO`) | `usuarioDAO.buscarUsuario(tecnicoSelecionado)` (ver 7.15) | 134 |
| 1.4 `seg = getSegmentoPretendido()` | `orc.getSegmentoPretendido()` | 140 |
| 1.5 `[seg="Modulos"] <<create>> OrdemDeServicoModulos(…)` / 1.6 `[seg="Chaves"] <<create>> OrdemDeServicoChaves(…)` | `criarOrdemDeServico(…)`, com o ramo de Módulos primeiro | 144 |
| 1.7 `sinalizarAbertura()` → 1.7.1 `setDataAbertura()`, 1.7.2 `setStatusOs("Aberta")` | `os.sinalizarAbertura()` | 148 |
| 1.8 `associaOS(os)` → orc | `os.associarOrcamento(orc)` (ver 7.15) | 151 |
| 1.9 `associaOS(os)` → vei | `os.associarVeiculo(vei)` | 154 |
| 1.10 `associaOS(os)` → tec | `os.associarTecnico(tec)` | 157 |
| (RN-03, RN-04: não é mensagem do diagrama) | `os.validar()` | 160 |
| 1.11 `salvar(os)` | `ordemServicoDAO.salvar(os)` | 166 |

---

## 5. Validações

Cada campo digitável valida **(a)** ao perder o foco e **(b)** de novo em "Registrar orçamento",
com o formulário inteiro: `validarFormulario()` revalida todos os campos aplicáveis ao segmento e às
opções marcadas. Os testes conferem os dois momentos, incluindo a perda de foco real do Swing (cenário
`focoReal`) e um campo inválido que não chegou a perder o foco antes do envio.

Origem das mensagens: **Doc** = Descrição Expandida, **Mockup** = texto da Tela 1/2/3, **Eu** =
escrita por mim.

| Campo | Regra | Mensagem | Origem |
|---|---|---|---|
| **Todos os campos numéricos** | a digitação descarta o que não é permitido, inclusive ao colar (ver 10.1): Válido até só dígitos e "/" (máx. 10); Qtd. de cópias só dígitos (máx. 3); Valor proposto e Novo valor proposto só dígitos, "," e "." (máx. 15), então o sinal de menos nem entra; Ano-modelo só dígitos (máx. 4); Telefone só dígitos, "(", ")", "-" e espaço; Placa só letras, dígitos e "-", em maiúsculas (máx. 8); Nº do chassi só letras e dígitos, em maiúsculas (máx. 17) | sem mensagem: o caractere simplesmente não entra (bipe) | Eu |
| Válido até * | obrigatório; dd/mm/aaaa, ou ddmmaaaa sem barras (as barras são postas ao perder o foco; ver 10.6); data que existe; ≥ data da solicitação (RN-07) | "Informe um prazo de validade igual ou posterior à data de solicitação." (todos os casos, pois o FA-12C cobre "não informado ou inválido") | Doc (FA-12C) |
| Origem do atendimento * | uma das duas opções (no envio e ao buscar o cliente) | "Selecione a origem do atendimento." | Eu |
| Cliente * | livre (aceita nome); obrigatório; se só dígitos/pontuação: CPF 11 dígitos + DV (ou, se o DV não fecha e são 11 dígitos de celular, telefone), CNPJ 14 dígitos + DV, ou fixo de 10 dígitos; se tiver parênteses/espaço: telefone; com letras: nome (≥ 2 caracteres); no envio, cliente identificado | "Campo obrigatório." / "CPF inválido: confira os 11 dígitos." / "CNPJ inválido: confira os 14 dígitos." / "Telefone inválido. Use o formato (DD) 99999-9999." / "Informe o nome, o CPF (11 dígitos), o CNPJ (14 dígitos) ou o telefone." / "Cliente não encontrado. Use “+ Cliente não cadastrado”." / "Mais de um cliente encontrado. Informe o CPF/CNPJ ou o telefone." | Eu |
| Veículo (placa) * | obrigatório; AAA-9999 ou AAA-9A99 (com ou sem hífen); no envio, veículo identificado | "Campo obrigatório." / "Placa inválida. Use o formato AAA-9999 ou AAA-9A99." / "Veículo não encontrado. Use “+ Veículo não listado”." | Eu |
| Segmento pretendido * | uma das duas opções | "Selecione o segmento pretendido." | Eu |
| Tipo de módulo *, Modelo da ECU *, Sintoma relatado * (V1, RN-04) | obrigatórios; Modelo dispensado no FAV-8.1 | inline: "Campo obrigatório."; caixa: "<Campo>: campo obrigatório para o segmento Módulos." | caixa: Mockup (Tela 3); inline: Eu |
| Identificação do módulo | opcional; ≤ 255 | "Máximo de 255 caracteres." | Eu |
| Registro fotográfico *, Descrição física da placa * (FAV-8.1) | obrigatórios com a etiqueta ilegível; foto jpg/jpeg/png | "Campo obrigatório." / "Anexe uma imagem jpg, jpeg ou png." | Eu (caixa: Mockup) |
| Tipo de chave *, Situação do imobilizador * (V2, RN-04) | obrigatórios | idem V1, "…para o segmento Chaves." | Mockup / Eu |
| Qtd. de cópias * | inteiro > 0; ≤ 4 salvo "Apagar chaves existentes" (FAV-8.4B) | "Informe uma quantidade de cópias maior que zero." / "Limite de chaves do imobilizador atingido. Máximo permitido: 4." | Eu / Doc + Mockup |
| Código do transponder | opcional; ≤ 255 | "Máximo de 255 caracteres." | Eu |
| Documento de propriedade (CRLV) *, Solicitante – nome *, Solicitante – documento * (FAV-8.4A) | obrigatórios com perda total | "Campo obrigatório." | Eu |
| Serviço base * | escolhido ao incluir; da categoria do segmento (RN-03) | "Selecione um serviço base." / "Serviço não disponível para o segmento informado." + "Os itens já incluídos são mantidos." | Eu / Doc (FA-09B) + Mockup |
| Valor proposto (R$) * | formato brasileiro (`150`, `150,5`, `1.234,56`, `R$ 1.234,56`); > 0; obrigatório se há serviço escolhido | "Informe um valor proposto válido para o serviço selecionado." | Doc (FA-09A) |
| Novo valor proposto * (FA-11B) | mesmo formato; > 0 | idem | Doc (FA-09A) |
| Observações | opcional; ≤ 255 | "Máximo de 255 caracteres." | Eu |
| Itens (FA-12A, RN-01) | ao menos um, no envio | "O orçamento deve conter ao menos um serviço." | Doc |
| Caixa de pendências (FA-12B) | lista todo campo inválido, menos validade e itens | "Corrija N pendências antes de registrar:" + "• Campo: mensagem" | Mockup |
| Cadastro rápido: Nome *, Telefone / WhatsApp * (FA-02) | obrigatório; telefone com DDD, celular começando com 9 | "Campo obrigatório." / "Telefone inválido. Use o formato (DD) 99999-9999." | Eu |
| Cadastro rápido: Placa *, Marca *, Modelo *, Ano-modelo *, Motorização *, Nº do chassi * (FA-04, RF-1) | obrigatórios; placa como acima; ano 1950…ano+1; chassi VIN 17 caracteres sem I/O/Q; placa e chassi únicos | "Campo obrigatório." / "Ano-modelo inválido: informe um ano entre 1950 e 2027." / "Nº do chassi inválido: 17 letras e números, sem I, O e Q." / "Já existe veículo cadastrado com a placa … ." / "…com o número de chassi informado." | Eu |

**Outras mensagens da Descrição Expandida usadas literalmente:**

- "Orçamento ORC-AAAA-NNNN registrado com sucesso" (passo 13);
- "Orçamento salvo como rascunho" (FA-12D);
- "Não foi possível registrar o orçamento. Tente novamente." (FA-13).

**Usadas do mockup:**

- "Desistir deste orçamento?" (FA-00);
- os textos dos alertas FAV-8.1, FAV-8.2, FAV-8.4A e "comprovação de propriedade";
- "Remover serviço do orçamento?";
- "Os dados informados foram mantidos no formulário.".

**Também escritas por mim:**

- "Usuário logado não encontrado.";
- "Somente Técnico de Módulos ou Técnico de Chaves pode registrar orçamentos." (PRE-01);
- "Identifique o cliente antes de cadastrar o veículo.";
- "Remova os serviços incluídos antes de trocar o segmento.";
- "Para salvar o rascunho, identifique o cliente, o veículo e o segmento pretendido.";
- "Não foi possível acessar a base de dados. Tente novamente." (falha de banco fora do registro);
- "Nenhum atendimento anterior registrado para este módulo neste veículo." (variante sem
  identificação da frase do mockup).

---

## 6. Decisões tomadas por omissão ou ambiguidade

| # | Decisão | Alternativa descartada |
|---|---|---|
| D1 | Implementar no projeto `registrar-ordem-servico`, com um segundo ponto de entrada (`MainSolicitarOrcamento`); o `Main` da 1ª iteração ficou intocado | Projeto novo ou a pasta `cadastrar-cliente`: duplicaria as classes compartilhadas (`Orcamento`, `Cliente`, `Veiculo`…) |
| D2 | Banco no Mapeamento OR v2, com a `OrcamentoDAO` da 1ª iteração adaptada (segmento lido pela subclasse; módulo e senhas pelas tabelas novas) | Manter o schema da 1ª iteração e só acrescentar tabelas: contrariaria o MOR v2 |
| D3 | `Veiculo` mantém os campos de módulo e senhas da 1ª iteração, preenchidos a partir das tabelas novas, para a tela do Registrar OS não mudar | Mudar a tela do Registrar OS para ler `ModuloECU`: mexeria na 1ª iteração |
| D4 | `Orcamento` concreto (o nome não está em itálico no diagrama, ao contrário de `OrdemDeServico`), com `getSegmentoPretendido()` sobrescrito nas subclasses | Abstrato: quebraria a leitura da 1ª iteração |
| D5 | `status` em VARCHAR, aceitando também as situações da 1ª iteração | ENUM do MOR v2: invalidaria "Aprovado pelo cliente" e "Convertido em OS" |
| D6 | A controladora guarda, em atributos privados extras, os dados técnicos informados em cada mensagem (o contrato fala em "sessão de registro"; o diagrama só lista 6 atributos) | Criar o orçamento antes de `registrarOrcamento`: contrariaria o contrato, que o cria nessa operação |
| D7 | Regras violadas sobem como `IllegalArgumentException` com o texto da mensagem, e falhas de banco como `SQLException` (padrão da 1ª iteração) | Devolver os códigos `"valorInvalido"`, `"semItens"`… do DSS alternativo: conflitaria com os tipos do diagrama de classes (ver 7.2) |
| D8 | Pré-condições do contrato conferidas **antes** da mensagem 1.1, e o corpo segue 1.1 → 1.15.3 sem reordenar (a 1ª iteração tinha invertido 1.7 e 1.8-1.10) | Validar no meio da sequência |
| D9 | O `ModuloECU` novo é gravado dentro da transação de `OrcamentoDAO.salvar`, porque `ModuloECUDAO` não tem `salvar` no diagrama; se a gravação falha, o id volta a 0 para a nova tentativa | Acrescentar `ModuloECUDAO.salvar`: operação fora do diagrama |
| D10 | Busca do cliente por nome, CPF/CNPJ ou telefone (texto do mockup), com o acréscimo `ClienteDAO.buscarPorNomeOuTelefone`. É necessária também porque o cliente do FA-02 nasce sem CPF/CNPJ | Só CPF/CNPJ: a dica do mockup mentiria e o cliente do FA-02 nunca seria encontrado de novo |
| D11 | "Veículos vinculados ao cliente" = veículos dos orçamentos do cliente (acréscimo `VeiculoDAO.buscarPorCliente`); o vínculo do veículo novo do FA-04 se forma no registro (RN-02). A placa digitada também é buscada (`selecionarVeiculo(placa)`) | Coluna `idCliente` em `TB_Veiculo`: não existe no MOR v2 nem no diagrama de classes |
| D12 | Histórico técnico (passo 5) = orçamentos do veículo (sem rascunhos), pela `listarOrcamentos` do diagrama, com as Ordens de Serviço do veículo listadas abaixo (`OrdemServicoDAO.buscarHistoricoTecnico` da 1ª iteração). **Revisada na rodada 7 (10.7)**: antes eram só as OS, e não batia com o passo 8.2 | Só as OS (versão anterior): os atendimentos do módulo (orçamentos) não apareciam no histórico do veículo, contra o texto do passo 8.2 |
| D13 | Limite do imobilizador = 4, constante (valor do mockup). "Apagar chaves existentes" (FAV-8.4B.2) libera o limite e não é gravado (não há coluna) | Inventar atributo e coluna de "máximo de chaves" |
| D14 | Desmarcar "ilegível", "bancada" ou "perda total" reenvia `informarSegmento` e os dados do bloco, porque o DSS não tem mensagem para desfazer | Acrescentar operações de "desmarcar" à controladora |
| D15 | Não se troca o segmento com serviços já incluídos | Apagar os itens silenciosamente |
| D16 | PRE-01: usuário logado pelo argumento da linha de comando (padrão: 1, Gabriel Abraão, o do mockup). Só Técnico de Módulos e Técnico de Chaves abrem o caso de uso: no diagrama de casos de uso o Administrador não está ligado ao "Solicitar Orçamento" e não há generalização dele para os técnicos (corrigido na rodada 3; antes o Administrador era aceito) | Tela de login: não especificada. Aceitar o Administrador por analogia com a 1ª iteração: contraria o diagrama de casos de uso e a PRE-01 |
| D17 | Rascunho exige cliente, veículo e segmento (FKs e tabela da subclasse do MOR); não consome número; a listagem do FA-12D usa `descartarOrcamento()`, que encerra a sessão | Gravar rascunho sem cliente: o MOR não permite |
| D18 | Depois de registrar ou salvar rascunho, o formulário reabre para um novo orçamento. Desistir (FA-00) fecha a tela, porque o FA-00.5 encerra o caso de uso. O "X" da janela não faz nada: o único jeito de sair é "Voltar / Desistir" (rodada 8; antes o "X" pedia a confirmação do FA-00 e desistir reabria o formulário) | Fechar a aplicação em todos os casos |
| D19 | Look and feel Metal (multiplataforma) só na tela nova, para as cores e bordas do mockup saírem iguais em Windows e Linux. A 1ª iteração continua com o do sistema | System LAF: no Windows ignora fundos e bordas customizados de botões |
| D20 | Os selos "PASSOS 1 · 12", "V1 · PASSOS 7.1-8.3"… e os cabeçalhos "Tela 2: …" do mockup **não** aparecem: são anotações do documento, não elementos da tela | Desenhá-los na interface |
| D21 | Listas das combos (tipos de módulo, modelos de ECU, tipos de chave, situações do imobilizador, marcas) inventadas como sugestão; as combos são **editáveis** e aceitam qualquer texto | Listas fechadas ou catálogo novo |
| D22 | Registro fotográfico guarda o caminho do arquivo (`String`, como no diagrama); o arquivo não é copiado | Copiar ou embutir a imagem no banco |
| D23 | Valor proposto: ao perder o foco, a obrigatoriedade só é cobrada se há serviço escolhido (o campo é de inclusão, não do orçamento) | Cobrar sempre: daria erro a quem só passa pelo campo |
| D24 | Telecomando começa em "Não"; "Válido até" começa vazio; "Situação" mostra EM ANÁLISE desde a abertura, como no mockup | Pré-preencher a validade com um prazo inventado |
| D25 | Dados de exemplo: CPFs do mockup com os dígitos verificadores corrigidos; CNPJ da Oficina Center Car inventado; três serviços de Chaves do mockup acrescentados ao catálogo; chassi e motorização do Gol iguais ao mockup v2 | Usar os CPFs inválidos do mockup (a validação os recusa) |
| D27 | Campo "Cliente": ao digitar ou clicar na lupa, abre a lista dos clientes cadastrados para **selecionar** (acréscimos `ControladoraSolicitarOrcamento.consultarClientes` e `ClienteDAO.buscarClientes`); o escolhido é identificado pela mensagem do DSS `informarCliente`, com o CPF/CNPJ (ou o telefone) | Uma combo com todos os clientes: o mockup mostra um campo de busca |
| D28 | "Identificação do módulo" virou combo editável com os módulos já registrados do veículo; escolher um preenche Tipo de módulo e Modelo da ECU (acréscimos `consultarModulosDoVeiculo` e `ModuloECUDAO.buscarModulosDoVeiculo`). Os dados seguem por `informarDadosModulo`, que só é enviado com a RN-04 completa (o sintoma continua sendo digitado) | Enviar `informarDadosModulo` sem o sintoma: contrariaria a RN-04 / DSS 8-9 |
| D29 | Campos numéricos bloqueiam letras na digitação (`DocumentFilter`), em vez de só acusar o erro depois | Só validar ao perder o foco (como estava): o usuário pediu para não poder digitar letras |
| D30 | "+ Cliente não cadastrado" exige antes a origem e a busca do cliente, porque no DSS `informarCliente` (que guarda a origem) vem antes de `cadastrarCliente` | Abrir o cadastro direto: deixava a controladora sem origem (bug 10.2.1) |
| D31 | Dados de exemplo ampliados para testar só selecionando (10.1); os orçamentos novos têm números abaixo de 0231 e situações que não aparecem no Registrar OS, para não mudar a 1ª iteração | Mudar os orçamentos 231-233 da 1ª iteração |
| D32 | A ordem do fluxo normal do DSS, `informarCliente` (3) → `selecionarVeiculo` (5) → `informarSegmento` (7), é exigida pela controladora: veículo sem cliente e segmento sem veículo são recusados ("Identifique o cliente antes de selecionar o veículo." / "Selecione o veículo antes de informar o segmento."). Os dados técnicos e os serviços **não** são forçados a vir antes do registro, porque o FA-12B prevê registrar com campos da RN-04 pendentes | Deixar a tela aceitar qualquer ordem (como estava) |
| D33 | Nas variantes, a tela guarda a marcação e só envia a mensagem quando a anterior já foi aceita: `informarDiagnosticoPendente` só depois de `informarDadosModulo` (FAV-8.2 é o passo 8.2, depois do 8.1); `informarComprovacaoPropriedade` só depois de `informarDadosChaves` com a perda total (FAV-8.4A parte do passo 8.4) | Enviar no instante do clique, fora da ordem |
| D34 | Registrar OS (1ª iteração): `sinalizarAbertura` (1.7) passou para antes de 1.8-1.10, como no diagrama; o `validar()` ficou entre 1.10 e 1.11. O comportamento não muda: 1.7 só marca o objeto em memória, e se a validação recusar, nada é gravado | Manter a inversão justificada na documentação da 1ª iteração |
| D35 | Em toda classe ligada ao diagrama de classes, os membros do diagrama ficam juntos e na ordem do diagrama; os acréscimos (métodos de apoio, getters, consultas extras) vêm depois do último membro do diagrama | Deixar os acréscimos intercalados |
| D26 | Testes em Java puro (`test/`), sem JUnit, porque o projeto não tem gerenciador de dependências e opera offline (RNF-4) | Adicionar Maven/JUnit |

---

## 7. Contradições entre documentos

| # | Contradição | O que foi seguido |
|---|---|---|
| 7.1 | O **diagrama de comunicação** termina em 1.15.3 sem nenhuma mensagem de gravação; o **contrato de operação** exige "O objeto orc foi persistido", e o diagrama de classes tem `OrcamentoDAO.salvar(orc)` | Contrato (documento de requisitos): `orcamentoDAO.salvar(orc)` depois de 1.15 |
| 7.2 | Retorno de `registrarOrcamento`: `Orcamento` (diagrama de classes e DSS normal), `String` (DSS alternativo, "resultado") e `void` (diagrama de comunicação). Retorno de `incluirItem`: `double` (classes, DSS normal) e `String` (DSS alternativo) | Diagrama de classes (`Orcamento`, `double`); os "resultados" do DSS alternativo viraram exceções com a mensagem do documento |
| 7.3 | Numeração do diagrama de comunicação: o `.docx` (22:12) mostra 1.15.1-1.15.3 dentro de `sinalizarRegistro`; o `.asta` (20:35) numera 1.16-1.18 | Mesma ordem nos dois; usada a versão mais recente (`.docx`) |
| 7.4 | O contrato fala em "objeto tec de **Tecnico**" e o modelo conceitual v2 tem `Tecnico`/`Administrador`; o diagrama de classes v2 tem só `Usuario` com `tipoUsuario` | Diagrama de classes (`Usuario`), como na 1ª iteração |
| 7.5 | O **Plano de iterações** põe "Solicitar Orçamento" na 1ª iteração e reserva a 2ª para CRUDs; a pasta e a Descrição Expandida dizem "2ª iteração" | Descrição Expandida |
| 7.6 | Situações do orçamento: RN-06 e MOR v2 listam Rascunho, Em análise, **Aprovado**, Recusado, Expirado, Cancelado; a 1ª iteração usa "Aprovado pelo cliente", "Convertido em OS" e "Aguardando aprovação do cliente" | Novos orçamentos usam as do v2; as da 1ª iteração foram mantidas para não quebrá-la. **Precisa ser unificado pelo grupo** |
| 7.7 | O mockup diz "Busque por nome, CPF/CNPJ ou telefone"; a operação é `informarCliente(origemAtendimento, cpfCnpjCliente)` e a DAO só tem `buscarPorCpfCnpj` | Mockup (D10), mantendo o nome da operação |
| 7.8 | FA-02.3 cadastra o cliente só com nome e telefone (`<<create>> Cliente(nome, telefone)`), mas `TB_Cliente.cpfCnpj` é `UNIQUE` e o mockup mostra CPF/CNPJ | Cliente do FA-02 nasce sem CPF/CNPJ (coluna aceita NULL) |
| 7.9 | FA-04.4 diz "veículo cadastrado e **vinculado ao cliente**", mas não há associação Cliente-Veiculo no diagrama de classes nem FK no MOR | Vínculo pelo orçamento (D11) |
| 7.10 | DSS e mockup dão textos diferentes: "Confirmar a desistência do registro?" × "Desistir deste orçamento?"; "Comprovação de propriedade do veículo obrigatória" × texto longo do mockup; "Campos obrigatórios não preenchidos" × "Corrija N pendências antes de registrar"; FAV-8.1 e FAV-8.2 também diferem | A Descrição Expandida não dá esses textos; seguido o mockup (regra "a tela é o mockup") |
| 7.11 | Os CPFs do mockup (024.577.310-45, 311.904.227-80) têm dígitos verificadores **inválidos** | Corrigidos nos dados de exemplo (D25) |
| 7.12 | O mockup mostra "Eduardo José – Técnico de Chaves"; os dados da 1ª iteração cadastram Eduardo como Técnico de Módulos (a Técnica de Chaves é Jamily Sales) | Mantidos os dados da 1ª iteração; a V2 é demonstrada com o usuário 3 |
| 7.13 | Passo 8.5 diz que a comprovação de propriedade é "obrigatória para serviços de chaves"; o contrato, o MOR e o mockup só pedem os documentos na perda total (FAV-8.4A) | Exibida a exigência em todo orçamento de Chaves (passo 8.5) e os documentos cobrados só na perda total, como especificam o contrato e o FAV-8.4A |
| 7.15 | Diagrama de comunicação da 1ª iteração (29/09) × diagrama de classes corrigido (01/10): o de comunicação usa `:VeiculoDAO.buscarVeiculo(orc)` (1.2), `:TecnicoDAO.buscarTecnico` (1.3) e `associaOS(os)` enviado a orc/vei/tec (1.8-1.10); o de classes não tem `VeiculoDAO`/`TecnicoDAO` (v1) nem `associaOS`, e tem `Orcamento.getVeiculo()`, `UsuarioDAO.buscarUsuario()` e `OrdemDeServico.associarOrcamento/Veiculo/Tecnico()` | **Nomes** do diagrama de classes (mais recente); **ordem** do diagrama de comunicação. Os comentários do código citam o texto do diagrama e o nome usado |
| 7.16 | DSS dos fluxos alternativos das variantes desenha o bloco FAV-8.4A (`informarComprovacaoPropriedade`, 8-11) **antes** de `informarDadosChaves` (12-13); a Descrição Expandida diz que a perda total é informada no passo 8.4 (o próprio `informarDadosChaves`, que tem `perdaTotal` como parâmetro) e só depois vêm os documentos (FAV-8.4A.1-3) | Descrição Expandida: `informarDadosChaves` → `informarComprovacaoPropriedade` → `incluirItem`. Os fragmentos do DSS das variantes foram lidos como cenários independentes |
| 7.17 | O `.asta` do diagrama de comunicação da 1ª iteração está desatualizado (mensagem 1 = `abrirOS`, só cinco mensagens) em relação à imagem corrigida do `.docx` | Imagem corrigida do `.docx` |
| 7.18 | DSS da 1ª iteração × diagrama de classes v2: o DSS dos fluxos alternativos devolve `resultado : String` de `registrarOrdemServico` e `listaOS = descartarRegistro(): List`; o diagrama de classes tem `registrarOrdemServico(...): OrdemDeServico` e `descartarRegistro(): void`. A ordem das mensagens é a mesma | Diagrama de classes (como a 1ª iteração já fazia). Sem mudança de código |
| 7.14 | MOR v2 não tem `TB_ItemOrcamento.quantidade` nem `TB_OrdemDeServico.numeroOS`, que a 1ª iteração acrescentou | Mantidos como `[ACRESCIMO]`, senão a 1ª iteração quebraria |

---

## 8. O que ficou de fora e por quê

- **POS-07, registro de auditoria**: nenhuma classe, tabela ou operação o modela (mesma lacuna
  registrada na 1ª iteração).
- **RN-07, expiração automática**, e **RN-09, cancelamento com motivo**: não há operação nem ator
  disparando-as neste caso de uso; `motivoCancelamento` existe na tabela, mas nada a preenche.
  Aprovação e recusa pelo cliente são de outro fluxo.
- **Retomar um rascunho** (FA-12D.3 "pode ser retomado posteriormente"): nenhuma operação do DSS ou
  do diagrama de classes carrega um rascunho de volta. O rascunho é gravado e aparece na lista.
- **Autenticação**: não há caso de uso de login; o usuário vem do argumento (D16).
- **FA-09B pela tela**: a combo só oferece serviços do segmento (passo 7.2), então o fluxo não é
  alcançável pela interface. A regra está na controladora e foi testada nela e com um serviço de
  outro segmento injetado na combo.
- **Cópia do arquivo da foto** (D22) e **máximo de chaves por veículo** (D13): sem atributo no
  modelo.

---

## 9. Como executar

Pré-requisitos (os mesmos da 1ª iteração): JDK 17+ e MySQL/MariaDB (XAMPP) na porta 3306 com
`root` sem senha.

1. **Recrie o banco** importando `Implementacao/registrar-ordem-servico/sql/schema.sql` no
   phpMyAdmin (aba Importar) ou com `mysql -u root < sql/schema.sql`. **Reimporte mesmo que já tenha
   o banco da 1ª iteração**: o schema mudou (13 tabelas).
2. **VS Code**: abra a pasta `Implementacao/registrar-ordem-servico` (não a raiz do repositório; ver
   `CLAUDE.md`) e clique em ▶ Run em
   `src/uniaoevolutions/MenuPrincipal.java` (menu com as duas telas; ver 10.4), em
   `src/uniaoevolutions/MainSolicitarOrcamento.java` (Solicitar Orçamento) ou em
   `src/uniaoevolutions/MainRegistrarOrdemServico.java` (Registrar OS).
3. **Terminal** (dentro de `registrar-ordem-servico`; no Windows troque `/` por `\` e `:` por `;`):

   ```bash
   javac -encoding UTF-8 -d bin -cp lib/mysql-connector-j-8.4.0.jar $(find src -name '*.java')
   java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MenuPrincipal             # menu com as duas telas
   java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MainSolicitarOrcamento     # usuário 1, Módulos
   java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MainSolicitarOrcamento 3   # Jamily, Chaves
   java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MainRegistrarOrdemServico                       # Registrar OS
   ```

4. **Só selecionando** (dados de exemplo): marque a origem, clique na lupa do campo Cliente e
   escolha na lista; escolha a placa; marque o segmento; em Módulos escolha a Identificação do
   módulo na lista (tipo e modelo vêm junto) e digite só o sintoma; escolha os serviços e digite os
   valores. Clientes prontos: Marcos (QAF-7C21, Módulos, 3 atendimentos anteriores), Fernanda
   (RCD-2H48, Chaves), Ana Paula (HSK-4B19), Auto Elétrica Pantanal (NRX-3E77 e PQT-8D12), Oficina
   Center Car (RTB-2J45) e João Victor (sem veículo, para testar o FA-04).

   **Roteiro do mockup (V1)**:
   - "Válido até" daqui a 15 dias, origem Presencial;
   - Cliente `024.577.310-03`, placa `QAF-7C21`;
   - segmento Módulos: ECU – Injeção Eletrônica / Magneti Marelli IAW 4GV.G1 / MM-88214-C / sintoma;
   - os três serviços (150, 380, 220) → **R$ 750,00** → Registrar orçamento → **ORC-2026-0234**.

   Para a **V2 com FA-02/FA-04**:
   - execute com o argumento `3`;
   - para os cadastros rápidos, busque um nome que não existe (ex.: "Ricardo Teixeira Lima") → + Cliente
     não cadastrado → cadastre um veículo novo (ex.: `MTZ-5F31`); a Fernanda e o `RCD-2H48` do mockup
     já vêm cadastrados;
   - segmento Chaves, qtd. 5 (limite) e depois 2, Perda total com os documentos → o apagamento é
     acrescentado.
5. **Testes** (recriam o banco a cada cenário). Os de interface precisam de um display; em servidor,
   use `Xvfb :99` e `DISPLAY=:99`:

   ```bash
   javac -encoding UTF-8 -d bin-test -cp "bin:lib/mysql-connector-j-8.4.0.jar" $(find test -name '*.java')
   java -cp "bin-test:bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.controle.TesteControladoras
   java -Dimagens=docs/telas-iteracao2 -cp "bin-test:bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.view.TesteTelaSolicitarOrcamento
   ```

   O cenário `falhaDeGravacao` para e religa o MariaDB com `sudo service mariadb stop/start`, só
   funciona em Linux. Para pulá-lo, passe o nome de outro cenário como argumento.

---

## 10. Rodadas de correção

Cada rodada lista o que foi pedido, os bugs e desvios encontrados na revisão e o que mudou. Os
números de linha das seções 4.1 e 4.2 já estão atualizados.

### 10.1 Rodada 1 (07/11/2026): campos numéricos, dados para testar só selecionando

**Pedidos do usuário**

1. *"Não quero poder digitar letras; campos que só podem ter números."* Implementado com um filtro
   de digitação (`TelaSolicitarOrcamento.restringir`) nos campos da tabela da seção 5. Letras e
   símbolos não entram, inclusive colando. Placa e chassi viram maiúsculas. A validação de formato
   continua (ex.: `31/02/2027` só tem caracteres permitidos, mas é recusada).
2. *"Já ter tudo para testar, como o cliente cadastrado para selecionar, o veículo e todas as infos,
   mas que eu apenas selecione."*
   - O campo Cliente mostra a lista dos clientes cadastrados ao digitar ou ao clicar na lupa (D27).
   - A Identificação do módulo lista os módulos do veículo e preenche tipo e modelo (D28).
   - Os dados de exemplo foram ampliados em `sql/schema.sql`:
     - 6 clientes com CPF/CNPJ válidos: os dos dois mockups e mais três;
     - 6 veículos, cada um com módulo e informações de segurança;
     - 10 orçamentos em situações variadas;
     - as 3 OS de 2025 do QAF-7C21 ("Ver 3 atendimentos anteriores", como no mockup).

     Os orçamentos 231-233 da 1ª iteração não mudaram, o próximo número continua ORC-2026-0234 e a
     regressão do Registrar OS segue passando.

**Bugs encontrados na revisão e corrigidos**

| # | Bug | Correção |
|---|---|---|
| 10.2.1 | "+ Cliente não cadastrado" podia ser usado sem a busca: a controladora ficava sem `origemAtendimento` (só `informarCliente` a guarda), a tela aceitava o formulário e o registro voltava com a pendência "Origem do atendimento" | O botão exige origem e busca antes; segue a ordem do DSS (D30) |
| 10.2.2 | Celular digitado sem pontuação (`67991804402`, 11 dígitos) era tratado como CPF e recusado com "CPF inválido" | `Validacoes.validarBuscaCliente` aceita 11 dígitos que formam um celular válido; `informarCliente` tenta o telefone quando o CPF não é encontrado |
| 10.2.3 | `ClienteDAO` usava `REGEXP_REPLACE`, que não existe no MySQL 5.7 | Trocado por `REPLACE` encadeado, que funciona em qualquer versão |
| 10.2.4 | Campos somente leitura estreitos mostravam o fim do texto ("…Técnic", "45U1ET043219") e combos editáveis mostravam o fim do valor | Cursor volta ao início ao preencher e ao sair do campo |
| 10.2.5 | A borda vermelha do "Segmento pretendido" não saía depois de escolhido o segmento | Restauração da aparência não dependia mais de a borda original ser não nula |
| 10.2.6 | Com a tabela de serviços vazia, o cabeçalho encolhia e "Nenhum serviço incluído." ficava desalinhado | Tabela fica sempre visível, com o aviso na largura toda |
| 10.2.7 | Comentários no fim de linhas do `schema.sql` atrapalhavam leitores de script que separam instruções por `;` | Comentários movidos para linhas próprias |

**Conferência com o diagrama nesta rodada:**

- os nomes e assinaturas das 19 operações da controladora, das DAOs do v2 e das entidades continuam
  idênticos ao diagrama de classes v2;
- a ordem 1.1-1.15.3 não mudou.

Os únicos métodos públicos que não estão no diagrama são os acréscimos de consulta listados na tabela
4.2 (`consultarVeiculosVinculados`, `consultarClientes`, `consultarModulosDoVeiculo`,
`consultarHistoricoTecnico`), mais os de apoio de `Orcamento` (`marcarComoProvisorio`,
`marcarComoRascunho`, `restaurarEstadoPersistido`, getters e setters).

**Testes:**

- novo cenário `selecaoSemDigitar`;
- a perda de foco real agora inclui a digitação de teclado de verdade, com `java.awt.Robot`, na
  lista de clientes;
- cenários atualizados para os filtros e os dados novos.

Resultado: 147 verificações de interface e 63 sem interface, sem falhas.

### 10.2 Rodada 2 (07/11/2026): auditoria de fidelidade à ordem dos diagramas

**Pedido:** *"verificar que todo código relacionado com algum diagrama esteja escrito na ordem que
aparece nos diagramas; fiel ao diagrama."* Foram auditados os três tipos de ordem:

| Diagrama | O que impõe | Como foi verificado | Achado | Correção |
|---|---|---|---|---|
| Comunicação, 2ª iteração | ordem 1.1-1.15.3 em `registrarOrcamento` | leitura linha a linha + script que confere os marcadores `// 1.x` em ordem crescente + DAOs espiãs (`TesteControladoras.ordemDasMensagens`) | fiel | `salvarRascunhoOrcamento` (sem diagrama próprio, monta o mesmo objeto) buscava o técnico por último: alinhada à mesma sequência (1.1, 1.3/1.4, 1.5, 1.6, 1.7-1.10, 1.11-1.14) |
| Comunicação, 1ª iteração | ordem 1.1-1.11 em `registrarOrdemServico` | leitura da imagem corrigida (o `.asta` está desatualizado, 7.17) | **1.7 vinha depois de 1.8-1.10**; o `<<create>>` testava Chaves (1.6) antes de Módulos (1.5); comentários de 1.2/1.3 citavam texto que não é o do diagrama | ordem corrigida (D34), ramo de Módulos primeiro, comentários com o texto exato do diagrama e o nome adotado (7.15). `DOCUMENTACAO.md` e `RASTREABILIDADE.md` da 1ª iteração atualizados |
| DSS, 2ª iteração | ordem das mensagens Interface → Sistema em cada fluxo | controladora espiã na tela real (`ControladoraEspia`, cenário `ordemDoDss`), incluindo preenchimento fora de ordem pelo técnico | a tela permitia: segmento antes do veículo, veículo antes do cliente, `informarDiagnosticoPendente` antes de `informarDadosModulo`, `informarComprovacaoPropriedade` antes de `informarDadosChaves`. Além disso, um segmento recusado fazia o rádio pular para o segmento **oposto** | D32 e D33; o rádio volta exatamente à seleção anterior (ou a nenhuma) |
| Classes v2 (todas as classes ligadas a ele) | ordem de declaração de atributos e métodos | script que extrai as declarações e compara com a ordem do diagrama | `OrcamentoDAO`: `atualizarStatus`/`incluirItem` intercalados; `ItemOrcamento`: `getSubtotal`, `getDescricao` e id intercalados; `ControladoraRegistrarOS`: `consultarHistoricoTecnico`, `criarOrdemDeServico`, `numeroReservado` intercalados | acréscimos movidos para depois do último membro do diagrama, sem mudar nenhum corpo de método (D35). Resultado do script: 0 problemas em 21 classes |

As sequências conferidas pelo cenário `ordemDoDss` (mensagens aceitas pela controladora, na ordem
em que chegaram):

- **V1 com a bancada marcada antes dos dados do módulo:** `iniciarOrcamento` → `informarCliente` →
  `selecionarVeiculo` → `informarSegmento` → `informarDadosModulo` → `informarDiagnosticoPendente` →
  `incluirItem` → `registrarOrcamento`.
- **V2 com os documentos preenchidos antes dos dados das chaves, e desistência:**
  `iniciarOrcamento` → `informarCliente` → `selecionarVeiculo` → `informarSegmento` →
  `informarDadosChaves` → `informarComprovacaoPropriedade` → `incluirItem` →
  `solicitarConfirmacaoDesistencia` → `descartarOrcamento`.

Única mensagem a mais que o DSS não mostra no FA-12D: depois de `salvarRascunhoOrcamento`, o botão
"Ir para a lista de orçamentos" do mockup usa `descartarOrcamento()`, a única operação que devolve
a lista. Ela vem **depois** de todas as mensagens do FA-12D, então não altera a ordem.

**Testes:**
- novo cenário `ordemDoDss` (11 verificações);
- a bateria sem interface ganhou 2 verificações (as duas recusas de ordem do DSS);
- a regressão do Registrar OS continua passando com a nova ordem 1.7 → 1.8-1.10.

Total: 158 verificações de interface e 65 sem interface, sem falhas.

### 10.3 Rodada 3 (07/11/2026): varredura de todos os arquivos contra cada diagrama

**Pedido:** *"mais uma varredura em todos os arquivos para verificar se estão de acordo com cada
diagrama relacionado."* Desta vez a conferência foi além da ordem. Cada aspecto de cada diagrama foi
verificado por script, para não depender de leitura visual:

| Diagrama | O que foi conferido | Como | Resultado | Correção |
|---|---|---|---|---|
| Classes v2: 21 classes, inclusive as da 1ª iteração | existência de cada classe; nome, tipo e visibilidade (+/-) de cada atributo; nome, nomes **e** tipos dos parâmetros, retorno e visibilidade de cada operação | script que lê as declarações Java e compara com a especificação transcrita do diagrama (imagem + strings do `.asta`) | **2 divergências**: (1) a classe `InformacoesSegurancaVeiculo` não existia; (2) `OrdemDeServico.associarOrcamento(ocr)` com parâmetro `ocr`, o diagrama diz `orc` | (1) classe criada com os 4 atributos e a associação "Veiculo 1 possui 0..1", preenchida pela `OrcamentoDAO` a partir de `TB_InformacoesSegurancaVeiculo` (os campos antigos que a tela do Registrar OS lê continuam preenchidos); (2) parâmetro renomeado. Depois: **0 divergências** de assinatura e **0** de ordem |
| Classes v2: associações e multiplicidades | 1, 0..1, 0..* e 1..* no código (referência única × lista; obrigatório × opcional) e no banco (`NOT NULL`, `UNIQUE`, FK) | leitura | conforme (ItemOrcamento 1..* é garantido pela RN-01 na controladora, já que o banco não expressa "ao menos um") | - |
| Mapeamento OR v2 | cada tabela e coluna, PK, FK, UNIQUE e ordem das colunas | script que lê o `.docx` e os `CREATE TABLE` do `schema.sql` | 13 tabelas conformes; diferenças só nos dois `[ACRESCIMO]` da 1ª iteração (`TB_ItemOrcamento.quantidade`, `TB_OrdemDeServico.numeroOS`), já registrados (7.14) | - |
| Casos de uso | atores do "1 Solicitar Orçamento" | imagem do `Casos de uso 01 Atualizado PDF.pdf` + strings do `.asta` | **divergência**: o código aceitava o Administrador, que não é ator deste caso de uso (D16) | `iniciarOrcamento` aceita só Técnico de Módulos e Técnico de Chaves |
| Comunicação (1ª e 2ª iteração) | ordem e guardas | marcadores `// 1.x` + DAOs espiãs (rodada 2) | conforme | - |
| DSS da 2ª iteração | ordem das mensagens em todos os fluxos | controladora espiã (rodada 2) | conforme | - |
| DSS da 1ª iteração | ordem das mensagens da `TelaRegistrarOrdemServico` | leitura das imagens corrigidas × chamadas da tela | conforme: 1 → 2 → 4 → 5 → 7 → 10; FA-11B 6 → 8; FA-11C 10; FA-00 12 → 14. Retornos diferem do diagrama de classes (7.18) | - |

**Testes:** a bateria sem interface ganhou a verificação de que o Administrador é recusado. Total:
158 verificações de interface e 66 sem interface, sem falhas. Os números de linha das tabelas 4.1,
4.2 e 4.3 foram recalculados automaticamente a partir do código.

### 10.4 Rodada 4 (08/11/2026): menu para trocar de tela sem a linha de comando

**Pedido:** *"quero poder trocar das telas, mas sem ter que digitar na cli, e sim por algum botao na
interface"*.

**O que mudou:** foi criado o `src/uniaoevolutions/MenuPrincipal.java`, um ACRÉSCIMO fora da modelagem,
como o `MainRegistrarOrdemServico`. Ele fica no pacote raiz, não em `view`, e não passa pelas controladoras. A janela tem:

- **Usuário logado:** a lista substitui o argumento do `MainSolicitarOrcamento`, porque não há caso de
  uso de autenticação (PRE-01). Ela é preenchida só com métodos de `UsuarioDAO` que já estão no
  diagrama de classes: `buscarTecnicosHabilitados` de Módulos e de Chaves, que juntos trazem os 4
  usuários. Nenhum método foi acrescentado à DAO, e a auditoria de assinaturas segue com 0
  divergências.
- **Botões:** "Solicitar Orçamento" e "Registrar Ordem de Serviço". O menu se esconde enquanto a tela
  está aberta e volta quando ela fecha, por Voltar/Cancelar ou Desistir (até a rodada 8, também pelo
  "X" do Solicitar Orçamento). Cada tela abre com o mesmo look and feel do seu `MainRegistrarOrdemServico`.
- **Diagnóstico de conexão:** o menu diz se o banco conectou. Quando falha, mostra a causa provável no
  XAMPP (MySQL desligado, banco não importado, senha do root, schema antigo ou driver ausente) junto
  com a mensagem original do driver, e tem o botão "Tentar conectar de novo".

**README.md reescrito** para cobrir as duas iterações: o menu como forma principal de rodar, os
passos do XAMPP no Windows e no Linux, a estrutura com as 13 tabelas e as classes da 2ª iteração, os
testes e a tabela de problemas comuns, incluindo os erros que o menu diagnostica.

**Não alterados:** as duas telas, as controladoras, as DAOs, o `MainRegistrarOrdemServico` e o `MainSolicitarOrcamento`,
que continuam funcionando como antes.

**Observação:** o "X" da janela do Registrar OS não faz nada, porque ela usa `DO_NOTHING_ON_CLOSE` sem
tratador de evento. Isso já era assim na 1ª iteração e não foi mudado nesta rodada. Para sair dessa
tela, use "Voltar / Cancelar".

**Verificação:** uma prova automática na tela real (Xvfb) abriu o menu e conferiu: 4 usuários
carregados; cada botão esconde o menu e abre a tela certa; fechar a tela faz o menu reaparecer.
Auditorias de assinatura e de ordem: 0 divergências. As seções 4.1-4.3 não mudaram, porque as
controladoras e as telas não foram tocadas.

### 10.5 Rodada 5 (08/11/2026): `Main` renomeada para `MainRegistrarOrdemServico`

**Pedido:** *"E a main.java, nao deveria ser MainCriarOrdem de Servico."*

**O que mudou:** `src/uniaoevolutions/Main.java` passou a se chamar `MainRegistrarOrdemServico.java`.
O nome segue o do caso de uso, RF-6 Registrar Ordem de Serviço, e o padrão do
`MainSolicitarOrcamento`. A classe não faz parte da modelagem, então a troca não afeta nenhum
diagrama. O conteúdo é o mesmo; só o nome da classe e o comentário mudaram, e as linhas citadas no
`RASTREABILIDADE.md` continuam valendo.

**Referências atualizadas:** `MenuPrincipal`, comentário do `MainSolicitarOrcamento`, `README.md`,
`DOCUMENTACAO.md`, `RASTREABILIDADE.md`, `RESUMO_ITERACAO2.md`, `CLAUDE.md` e as seções 9 e 10.4 deste
relatório. Nas seções 3 (tabela "Não alterados") e 6 (D1), o nome `Main` ficou como estava, porque
descrevem o estado daquelas rodadas. O `bin/uniaoevolutions/Main.class` antigo foi apagado.

**Comando novo:** `java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MainRegistrarOrdemServico`.

**Verificação:** compilação sem erros; auditorias de assinatura e de ordem com 0 divergências.

### 10.6 Rodada 6 (08/11/2026): data sem barras, chassi cortado e atendimentos automáticos

**Pedido:** *"Quero poder digitar a data com ou sem barras, e quero que tenha um exemplo de como deve
ser o formato. O numero do chassi nao da pra ler direito. Na janela de mostrar atendimentos esta
cortado tbm. E quando termino de preencher os dados tecnicos do modulo, os dados de atendimentos
anteriores nao é preenchido sozinho, somente depois que eu cliquei no historico tecnico [...]"*

| Problema | Causa | Correção |
|---|---|---|
| "Válido até" só aceitava dd/mm/aaaa | `Validacoes.converterData` exigia as barras | Aceita também 8 dígitos (ddmmaaaa). Ao perder o foco, uma data válida digitada sem barras é reescrita como dd/mm/aaaa. O `DocumentFilter` já permitia dígitos e "/", então não mudou |
| Sem exemplo do formato | A dica só dizia "Igual ou posterior à data da solicitação." | A dica passou a ser "Ex.: *dd/mm/aaaa* ou *ddmmaaaa*. Igual ou posterior à data da solicitação.", com a data de hoje + 15 dias como exemplo. O placeholder "dd/mm/aaaa" continua |
| Nº do chassi cortado (e "Marca / Modelo" também) | Os pesos da linha do veículo davam ao chassi menos largura do que os 17 caracteres pedem | Pesos da linha refeitos: o chassi e a marca/modelo ganham largura; o botão "+ Veículo não listado" fica só com a largura do texto. O campo do chassi também mostra o número inteiro como dica (tooltip) |
| Janela do histórico técnico com as linhas cortadas | `mostrarDialogo` chamava `setResizable(false)` depois do `pack()`. Em vários gerenciadores de janela do Linux, isso troca a moldura depois de calculado o tamanho e corta o conteúdo | `setResizable(false)` passou para antes do `pack()`. Vale para todos os diálogos da tela |
| "Atendimentos anteriores deste módulo" só aparecia ao sair do campo Sintoma | O envio de `informarDadosModulo` (DSS 9) estava ligado só à perda de foco do Sintoma; clicar no histórico técnico apenas tirava o foco do campo | ACRÉSCIMO `enviarAoDigitar`: ao parar de digitar por 0,6 s no Sintoma (ou na Descrição da placa, com etiqueta ilegível), a tela faz o mesmo envio do "perder o foco". O envio continua exigindo os campos da RN-04 completos, então a mensagem e a ordem do DSS não mudam |

**Fidelidade:** nenhuma mudança na controladora, nas entidades ou nas DAOs. A mensagem do DSS 9 continua
a mesma, só é disparada mais cedo. Auditorias de assinatura e de ordem: 0 divergências. Linhas das
tabelas 4.1-4.3 recalculadas.

**Testes:** 3 verificações novas sem interface (data sem barras válida, inexistente e com 7 dígitos)
e 2 na tela real ("Válido até" sem barras ganha as barras e é aceito). O cenário `selecaoSemDigitar`
agora escreve o sintoma **sem** tirar o foco e confere que os atendimentos aparecem sozinhos. Total:
160 verificações de interface e 69 sem interface, sem falhas. Capturas em `docs/telas-iteracao2/`
regeradas.

### 10.7 Rodada 7 (08/11/2026): histórico técnico e atendimentos do módulo não batiam

**Pedido:** *"Nao estao batendo os dados dos historicos"*, com as duas capturas: o bloco "Atendimentos
anteriores deste módulo" mostrava ORC-2026-0233 e ORC-2026-0231, e a janela do "Histórico técnico"
mostrava só OS-2026-0001 e as três OS de 2025. A última linha da janela também saía cortada.

**Causa:** as duas listas vinham de fontes diferentes. O histórico técnico (passo 5) listava as
**Ordens de Serviço** do veículo (D12), e o bloco do módulo (passo 8.2, `buscarAtendimentosModulo`)
lista **orçamentos**. A Descrição Expandida diz no 8.2 que o sistema exibe "os atendimentos anteriores
do mesmo módulo **registrados no histórico técnico do veículo**". Os do módulo devem, portanto, estar
contidos no histórico do veículo, e não estavam: o ORC-2026-0233, ainda aguardando aprovação, não tem
OS. A POS-05 e a regra de irretratabilidade também tratam os orçamentos como parte do histórico do
veículo.

**Correção:**

| Onde | Antes | Depois |
|---|---|---|
| `ControladoraSolicitarOrcamento.consultarHistoricoTecnico()` (acréscimo) | `List<String>` com as OS do veículo | `List<Orcamento>` com os orçamentos do veículo, exceto rascunhos (mesmo critério do `buscarAtendimentosModulo`), do mais recente ao mais antigo. Usa a `listarOrcamentos()` do diagrama, sem método novo na DAO |
| `ControladoraSolicitarOrcamento.consultarOrdensDeServicoDoVeiculo()` | - | ACRÉSCIMO: as OS do veículo, pela mesma consulta da 1ª iteração |
| Janela "Histórico técnico" | lista de OS num único `JLabel` HTML | dois blocos: "Orçamentos do veículo" (cada linha com o mesmo texto do bloco do módulo, mais o segmento e o módulo, ex.: "· Módulos MM-88214-C") e "Ordens de Serviço do veículo". Um `JLabel` por linha, sem HTML, para o tamanho calculado ser o desenhado e nada sair cortado |
| Botão "Ver N atendimentos anteriores" | N = número de OS (3 no QAF-7C21) | N = número de orçamentos (5 no QAF-7C21) |
| Bloco do módulo | texto montado à parte | usa a mesma `linhaAtendimento` da janela, então as linhas são idênticas |

Com os dados de exemplo, o QAF-7C21 tem dois módulos: o MM-88214-C (ORC-2026-0231 e 0233) e o
BC-55120-A (ORC-2025-0118, 0142 e 0187, que viraram as três OS de 2025). Por isso o bloco do
MM-88214-C mostra só dois atendimentos, e o histórico do veículo mostra os cinco.

**Fidelidade:** nenhuma assinatura do diagrama mudou; os dois métodos alterados ou criados são
acréscimos e ficam depois dos membros do diagrama. Auditorias: 0 divergências de assinatura e 0 de
ordem. Linhas das tabelas 4.1-4.3 recalculadas.

**Testes:** o `fluxoPrincipalModulos` agora espera "Ver 5 atendimentos anteriores", abre a janela do
histórico e confere 7 coisas: cada linha do bloco do módulo aparece igual na janela, o orçamento do
outro módulo aparece na janela e não no bloco, e as OS aparecem abaixo. A captura está em
`docs/telas-iteracao2/tela2-historico-tecnico.png`. Total: 167 verificações de interface e 69 sem
interface, sem falhas.

### 10.8 Rodada 8 (11/11/2026): "Incluir item" na seção da OS e telas sem fechar pelo "X"

**Pedido:** *"Na parte de ordem de servico, quero que o botao de adicionar items esteja na secao dele
e nao a parte de baixo junto com os outros, e quero que na tela de orcamento e de ordem de servico,
nao de para fechar pelo x e so pelo botao de cancelar."*

| Onde | Antes | Depois |
|---|---|---|
| `TelaRegistrarOrdemServico`, botão "Incluir item" (FA-11B) | no rodapé, entre "Voltar / Cancelar" e "Salvar rascunho" | na seção EXECUÇÃO, abaixo da tabela de itens, na mesma linha do "Valor total da OS". O rodapé ficou com Voltar / Cancelar, Salvar rascunho, Imprimir OS e Salvar Ordem de Serviço |
| `TelaRegistrarOrdemServico`, "X" da janela | já não fazia nada (`DO_NOTHING_ON_CLOSE` sem tratador) | igual; só ganhou um comentário dizendo que é de propósito |
| `TelaSolicitarOrcamento`, "X" da janela | pedia a confirmação do FA-00 e fechava | não faz nada: o tratador `windowClosing` foi removido |
| `TelaSolicitarOrcamento`, "Voltar / Desistir" | confirmada a desistência, mostrava a lista e **reabria** um orçamento novo | confirmada a desistência, mostra a lista e **fecha a tela** (FA-00.5, "Encerrar caso de uso"). Sem isso, com o "X" desativado, a tela não teria como ser fechada. `desistir(boolean fecharJanela)` virou `desistir()` |

**Fidelidade:** a ordem das mensagens do FA-00 não mudou (`solicitarConfirmacaoDesistencia` →
`descartarOrcamento`), nem a controladora, as DAOs ou o diagrama de classes (as telas não têm membros
no diagrama). Registrar e salvar rascunho continuam reabrindo o formulário (D18). Auditorias: 0
divergências de assinatura e 0 de ordem. Linhas das tabelas 4.1-4.3 recalculadas.

**Testes:** o cenário `desistencia` agora envia o evento de fechamento da janela e confere que a tela
continua aberta e com os dados (2 verificações novas), e no fim confere que a tela foi fechada, no
lugar de "formulário descartado". Rodados todos os cenários de interface, menos o `falhaDeGravacao`
(precisa de `sudo` para parar o MariaDB): 166 verificações, sem falhas. Sem interface: 69
verificações, sem falhas.

### 10.9 Rodada 9 (11/11/2026): validação dos campos no Registrar OS e "Incluir item" no topo da Execução

**Pedido:** *"agora a mesma verificacao de campos, dos erros e validadoes, ao enviar a os e ao tirar o
focus do botao, da mesma forma que foi feito no registar orcamento, agora na registrar orgem de
servico. O botao de incluir item na ordem de servioc quero que fique no canto superior direito da aba
de execucao."*

**Antes:** a `TelaRegistrarOrdemServico` não validava nada ao perder o foco. Ao salvar, mostrava só a
**primeira** exceção do `validar()` num `JOptionPane` (ex.: "Informe a quilometragem atual do
veículo."). Quilometragem e prazo aceitavam qualquer caractere. O "Incluir item" abria três caixas de
texto seguidas e só acusava erro no fim.

**Depois:** o mesmo padrão do Solicitar Orçamento (seção 5), com uma classe interna `Campo` na tela:

| Campo | Regra | Mensagem |
|---|---|---|
| Orçamento associado * | selecionado | "Selecione o orçamento que originará a Ordem de Serviço." |
| Prazo estimado * | obrigatório; só dígitos e "/" (máx. 10); dd/mm/aaaa ou ddmmaaaa (as barras são postas ao perder o foco); data que existe; ≥ data de abertura | "Campo obrigatório." / "Informe uma data válida no formato dd/mm/aaaa." / "O prazo estimado não pode ser anterior à data de abertura." |
| Quilometragem * | obrigatório; só dígitos e "." (máx. 9); 85400 ou 85.400; > 0 | "Campo obrigatório." / "Informe a quilometragem só com números (ex.: 85.400)." / "Informe uma quilometragem maior que zero." |
| Sintomas relatados *, Condição de recebimento *, Diagnóstico em bancada * (V1, RN-03) | obrigatórios no segmento Módulos; ≤ 255 | inline "Campo obrigatório."; caixa "<Campo>: campo obrigatório para o segmento Módulos." |
| Serviço de chaves a executar * (V2, RN-03) | obrigatório no segmento Chaves; ≤ 255 | idem, "…para o segmento Chaves." |
| Técnico responsável * | selecionado (vazio no FA-09) | "Selecione o técnico responsável pela Ordem de Serviço." |
| Observações internas | opcional; ≤ 255 | "Máximo de 255 caracteres." |
| Caixa de pendências (FA-11A.1) | no topo do formulário, ao salvar | "Corrija N pendências antes de salvar a Ordem de Serviço:" + "• Campo: mensagem" |
| Incluir item: Serviço ou peça *, Quantidade *, Valor unitário (R$) * (FA-11B.1) | num formulário só; quantidade só dígitos (máx. 4), > 0; valor no formato brasileiro, > 0 | "Campo obrigatório." / "Informe uma quantidade maior que zero." / "Informe um valor válido (ex.: 120,00 ou 1.250,00)." / "Informe um valor maior que zero." |
| Justificativa da alteração * (FA-11B.3, RN-05) | obrigatória; ≤ 255 | "Campo obrigatório." |

- **(a) ao perder o foco:** cada campo digitável valida e marca a moldura de vermelho, com a mensagem
  embaixo. Trocar de segmento apaga as marcas dos campos do outro segmento.
- **(b) ao salvar:** `validarCampos()` revalida o formulário inteiro e, com pendências, não chama a
  controladora. Os dados ficam preservados (FA-11A.3). O `validar()` do negócio continua valendo
  depois disso.
- **Salvar rascunho (FA-11C):** cobra só orçamento, prazo, quilometragem, técnico e observações, que
  é o que `salvarRascunhoOS` grava. Os dados técnicos ficam para quando o rascunho for retomado
  (FA-11C.3, "a partir do passo 7").
- **Diálogos do FA-11B:** só fecham no OK com tudo válido. Com erro, voltam com o que foi digitado e as
  marcas.
- **Borda vermelha numa moldura em volta do campo:** esta tela usa o look and feel do sistema
  (D19), que pode ignorar a borda e o fundo do próprio `JTextField`.

**Layout:**
- "Incluir item" está no **canto superior direito da seção EXECUÇÃO**, na linha do título. Na rodada
  8 ele estava abaixo da tabela.
- O formulário passou a acompanhar a largura da janela (`PainelLarguraDaJanela`, como no Solicitar
  Orçamento). O texto "Campos exigidos no segmento…" quebra a linha. Isso acabou com a rolagem
  horizontal, que cortava o "Valor total da OS" e esconderia o botão novo.

**Escrito por mim:** a regra "prazo ≥ data de abertura" e todas as mensagens acima, exceto as de
orçamento, técnico e justificativa, que já existiam na controladora e no negócio.

**Fidelidade:** a controladora, o negócio e as DAOs não mudaram. A ordem das mensagens do diagrama de
comunicação (4.3) e do DSS da 1ª iteração é a mesma: a tela só deixa de chamar
`registrarOrdemServico` enquanto houver pendência. Auditorias: 0 divergências de assinatura e 0 de
ordem. `DOCUMENTACAO.md` e `RASTREABILIDADE.md` da 1ª iteração atualizados (32 links de linha da tela
recalculados).

**Verificação:** não há teste automatizado de interface para o Registrar OS. Um roteiro descartável
abriu a tela real e conferiu 27 pontos, sem gravar nada no banco:
- sem rolagem horizontal; botão fora do rodapé, acima do técnico e encostado à direita;
- filtros de digitação ("12a3.4b" vira "123.4");
- mensagens ao perder o foco (quilometragem inválida e zero, 31/02, prazo antes da abertura,
  "15122027" vira "15/12/2027", sintoma em branco) e a marca sumindo com valor válido;
- a caixa com as 5 pendências ao salvar e as 2 do rascunho;
- a troca de segmento limpando as marcas;
- o diálogo "Incluir item" voltando com "Campo obrigatório." no serviço.

A bateria sem interface (`TesteControladoras`) continua passando.

### 10.10 Rodada 10 (11/11/2026): ordem dos campos da Identificação da OS

**Pedido:** *"na tela de os, quero inverter os 2 primeiros campos, orcamento associado ser o primeiro,
e o numero da os o segundo."*

**Mudança:** na seção IDENTIFICAÇÃO DA OS da `TelaRegistrarOrdemServico`, a ordem passou a ser
Orçamento associado → Nº da OS → Data de abertura → Prazo estimado → Situação. As larguras foram
trocadas junto, para o combo do orçamento continuar mais largo. Agora a primeira coisa da tela é o
que o Administrador escolhe no passo 3. Só o layout mudou: nada de controladora, negócio, DAO ou
ordem de mensagens. `DOCUMENTACAO.md` atualizado. Nenhuma linha mudou de número.

### 10.11 Rodada 11 (11/11/2026): estilo dos botões do Registrar OS conforme o mockup

**Pedido:** *"alguns botoes da tela de ordem de servico nao estao seguindo o mockup em questao de
estilo, verifique o mockup e aplique o estilo nos botoes."*

**Comparação com `MOCKUP-registrar-ordem-de-servico.pdf`:**

| Botão | Mockup | Antes | Depois |
|---|---|---|---|
| Salvar Ordem de Serviço | azul cheio, texto branco em negrito | azul sem arredondamento; no GTK, cinza | igual ao mockup |
| Imprimir OS | branco, borda e texto azuis, ícone de impressora | botão padrão do sistema, sem ícone | igual ao mockup, com ícone desenhado (`IconeImpressora`) |
| Voltar / Cancelar | branco, borda cinza-clara, texto escuro em negrito | botão padrão do sistema | igual ao mockup |
| Ver atendimentos anteriores | azul-claro, borda azul-clara, texto azul em negrito | botão padrão do sistema | igual ao mockup |
| Salvar rascunho (não está no mockup) | - | botão padrão do sistema | como o Voltar / Cancelar |
| Incluir item (não está no mockup) | - | botão padrão do sistema | "+ Incluir item", como o Imprimir OS |

Todos têm cantos arredondados, cursor de mão, cor um pouco mais escura ao passar o mouse e ao
pressionar, e aparência apagada quando desabilitados (FA-02). Os botões são desenhados pela classe
interna `Botao`, porque o Registrar OS usa o look and feel do sistema (D19), que ignora fundo e
arredondamento de `JButton`. As capturas foram conferidas no Metal e no GTK.

**Fidelidade:** só a aparência mudou. Controladora, negócio, DAOs e ordem das mensagens ficaram como
estavam. Auditorias: 0 divergências. `DOCUMENTACAO.md` atualizado e 33 links de linha do
`RASTREABILIDADE.md` recalculados.

**Visto de passagem, sem mudança:** o mockup mostra "Ver 3 atendimentos anteriores", com a contagem,
e a tela mostra "Ver atendimentos anteriores". No segmento Chaves, o bloco de dados técnicos tem
espaço vazio porque o `CardLayout` reserva a altura do bloco de Módulos.

### 10.12 Rodada 12 (11/11/2026): comentários mais objetivos e sem travessão

**Pedido:** *"reduzir os comentarios para a forma mais objetiva, e que nao tem o travessao [...] em
nenhum lugar."*

**Comentários:** os de todos os `.java` de `src/` e do `schema.sql` foram reescritos de forma curta.
Ficou o que identifica a origem (classe do diagrama, mensagem 1.x, passo, FA, RN, `ACRÉSCIMO`,
`[ACRESCIMO]`) e saiu a explicação longa. Foram mantidos sem mudança:
- os marcadores `// 1.1` a `// 1.15.3` de `registrarOrcamento` e `// 1.1` a `// 1.11` de
  `registrarOrdemServico`;
- o comentário `DSS 21`, que o `atualizar_linhas.py` usa como âncora.

Três comentários do `schema.sql` que começavam no meio da frase (orçamentos de 2025, itens do
ORC-2026-0231 e OS de 2025) foram completados. O comentário do `MenuPrincipal` que falava em fechar
pelo "X" foi corrigido.

**Travessão longo:** removido de todo o projeto: código, `schema.sql`, `README.md`, `DOCUMENTACAO.md`,
`RASTREABILIDADE.md` e este relatório. Nos textos, cada um virou dois-pontos, vírgula, ponto e vírgula
ou parênteses, conforme a frase; nas tabelas, a célula vazia virou "-". Na tela do Solicitar
Orçamento:
- o texto da perda total ficou "Perda total das chaves (o cliente não possui nenhuma chave funcional
  do veículo)";
- o número vazio da lista de orçamentos ficou "-".

O travessão curto (–) dos dados do mockup ("ECU – Injeção Eletrônica", "Gabriel Abraão – Técnico de
Módulos") foi mantido: é dado, não pontuação.

**Links:** com os comentários menores, as linhas mudaram.
- Os 181 links de linha do `RASTREABILIDADE.md` foram remapeados pelo conteúdo.
- Os que citavam um método e já estavam desatualizados passaram a apontar para a declaração dele.
- As citações de comentários no documento foram atualizadas para o texto novo.
- Tabelas 4.1-4.3 recalculadas.

**Verificação:** compila; auditorias com 0 divergências; `TesteControladoras` (69 verificações, que
recriam o banco a partir do `schema.sql`) e os cenários de interface `variantesChavesComCadastros` e
`desistencia` sem falhas.

