# Rastreabilidade — do artefato ao código

Trabalho Prático de APSOO — UFMS 2026.2 — Grupo G5
Caso de uso **RF-6 — Registrar Ordem de Serviço**

Este documento percorre os **11 artefatos** reunidos em
`artefatos elaborados na primeira iteração.pdf` e, para cada um, aponta **onde ele está no código** e
**como foi traduzido**.

É o complemento do [DOCUMENTACAO.md](DOCUMENTACAO.md): aquele documento se organiza pelas decisões de
implementação (arquitetura, mapeamento, divergências); este se organiza **pelos artefatos**, na ordem
em que aparecem no PDF. Quem vem do documento de modelagem e quer achar o código começa aqui.

---

## Panorama

Três artefatos não têm contrapartida direta no código — e saber disso é parte da resposta.

| # | Artefato no PDF | Tem contrapartida direta no código? |
|---|---|---|
| 1 | Especificação de Requisitos (RF/RNF) | Parcial — só o RF-6 e os RNFs |
| 2 | Modelo de processo de negócio (Diagrama de Atividades) | Indireta — via máquina de status |
| 3 | Diagrama de Casos de Uso | Indireta — via atores e a controladora |
| 4 | Mockup | **Sim** — um arquivo, linha a linha |
| 5 | Descrição Expandida de Caso de Uso | **Sim** — espalhada pelas 4 camadas |
| 6 | Modelo Conceitual | **Sim** — pacote `negocio` |
| 7 | DSS — Diagrama de Sequência de Sistema | **Sim** — métodos públicos da controladora |
| 8 | Contrato de Operação de Sistema | **Sim** — um método |
| 9 | Diagrama de Comunicação | **Sim** — o corpo desse método |
| 10 | Diagrama de Classes | **Sim** — todo o projeto |
| 11 | Mapeamento Objeto-Relacional | **Sim** — `schema.sql` + DAOs |

---

## 1. Especificação de Requisitos de Software

**No PDF:** seções 1 a 4 — Visão Geral, Classes de usuários, Definição de conceitos, RF-1 a RF-17,
RNF-1 a RNF-7 — mais o histórico de versões.

É o artefato mais amplo e o menos presente no código, de propósito: o Plano de Iterações alocou
apenas o RF-6 à 1ª iteração. Há quatro pontos de contato concretos.

### 1.1 As classes de usuários → o enumerado

A seção 2 do PDF define três perfis. Eles estão em
[TipoUsuario.java:11-13](src/uniaoevolutions/negocio/TipoUsuario.java#L11-L13):

```java
administrador,
tecnicoDeChaves,
tecnicoDeModulos;
```

A grafia com minúscula inicial não é descuido: é a grafia dos artefatos, para que `name()` possa ser
gravado direto no `ENUM` do MySQL ([schema.sql:90](sql/schema.sql#L90)).

### 1.2 A definição de conceitos → o vocabulário dos identificadores

A seção 3 é um glossário, e o código **usa esses termos como nomes**, em vez de traduzir ou abreviar:

| Conceito do PDF | Identificador no código |
|---|---|
| ECU / Módulo / Central | `modeloECU`, `tipoModulo`, `identificacaoModulo` — [Veiculo.java:19-21](src/uniaoevolutions/negocio/Veiculo.java#L19-L21) |
| Imobilizador | `senhaImobilizador` — [Veiculo.java:23](src/uniaoevolutions/negocio/Veiculo.java#L23) |
| Orçamento | classe `Orcamento` |
| Ordem de Serviço | classe `OrdemDeServico` |
| Teste em bancada | `diagnosticoInicial`, rótulo "Diagnóstico em bancada" |

É rastreabilidade de vocabulário: quem lê o requisito reconhece o nome no código sem tradução
intermediária.

### 1.3 Os requisitos funcionais

| RF | Onde está — ou por que não está |
|---|---|
| **RF-1** Veículo (marca, modelo, ano, motorização, placa, chassi) | [Veiculo.java:13-18](src/uniaoevolutions/negocio/Veiculo.java#L13-L18) + `TB_Veiculo` ([schema.sql:50](sql/schema.sql#L50)). Só leitura aqui — o cadastro é outro caso de uso |
| **RF-2** Informações de segurança do veículo | [Veiculo.java:22-25](src/uniaoevolutions/negocio/Veiculo.java#L22-L25) — os quatro atributos existem |
| **RF-3** Módulo/Central | [Veiculo.java:19-21](src/uniaoevolutions/negocio/Veiculo.java#L19-L21) |
| **RF-4** Serviço Base | [ServicoBase.java](src/uniaoevolutions/negocio/ServicoBase.java) — os três atributos |
| **RF-5** Solicitar Orçamento | [Orcamento.java:26-30](src/uniaoevolutions/negocio/Orcamento.java#L26-L30) (`origemAtendimento`, `segmentoPretendido`, `valorEstimado`, `status`). A regra "só efetivado com aprovação" → [podeGerarOrdemDeServico():50](src/uniaoevolutions/negocio/Orcamento.java#L50) |
| **RF-6** Gerenciar a Ordem de Serviço | **É o projeto inteiro** |
| **RF-7** Atributos do fluxo de módulos | [OrdemDeServicoModulos.java:15-21](src/uniaoevolutions/negocio/OrdemDeServicoModulos.java#L15-L21) — os 7 atributos estão lá, mas só 3 são preenchidos no registro (ver comentário das linhas 9-11) |
| **RF-8** Atributos do fluxo de chaves | `servicoExecutado` + os atributos de segurança do `Veiculo` |
| **RF-14** Consultar histórico técnico | [OrdemServicoDAO.buscarHistoricoTecnico():90](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L90) |
| **RF-9** Fechamento financeiro | Só a tabela: `TB_Pagamento` ([schema.sql:222](sql/schema.sql#L222)), criada para o banco ser fiel ao artefato |
| **RF-10 a RF-13** Comprovantes e termos | Só os atributos: `dataEmissaoGarantia`, `textoExecutadoGarantia`, `dataEmissaoResponsabilidade` ([OrdemDeServico.java:32-34](src/uniaoevolutions/negocio/OrdemDeServico.java#L32-L34)) |
| **RF-15 a RF-17** Relatórios | Não implementados — outras iterações |

Note o padrão: os atributos e tabelas dos RFs futuros **existem**, vazios. Não é código morto — é o
Diagrama de Classes e o Mapeamento OR implementados por inteiro, inclusive nas partes que este caso
de uso não exercita.

### 1.4 Os requisitos não funcionais → as decisões de tecnologia

| RNF | Onde |
|---|---|
| **RNF-4** só rede local, sem nuvem | [Conexao.java:16](src/uniaoevolutions/persistencia/Conexao.java#L16) — `jdbc:mysql://localhost:3306`. O comentário das linhas 11-12 registra que, em produção, `localhost` vira o IP do servidor da oficina. Vale também para o ambiente: o driver está em `lib/`, nada é baixado em tempo de execução |
| **RNF-7** desktop multiplataforma | Java + Swing. [MainRegistrarOrdemServico.java:13](src/uniaoevolutions/MainRegistrarOrdemServico.java#L13) pede o *look and feel* do sistema operacional, e o `catch` garante que o programa roda em qualquer SO |
| **RNF-6** não armazenar binários da ECU | [schema.sql:192-203](sql/schema.sql#L192-L203): `TB_OrdemDeServicoModulos` tem **só colunas `VARCHAR`**. Nenhum `BLOB`, nenhum campo de arquivo. "Limitando-se a registrar apenas os dados em texto" virou uma escolha de tipo de coluna |

---

## 2. Modelo de processo de negócio (Diagrama de Atividades)

**No PDF:** página "Modelo de processo e negócio". Entrou na versão 2.0 do documento de requisitos
("Adição da modelagem do processo de negócio (Diagrama de Atividades) associada à transação
principal").

**No código:** não existe uma classe "processo". O diagrama modela as **três transações em
sequência** — Solicitar Orçamento → Realizar Ordem de Serviço → Efetuar Entrega — e o que o código
implementa dele é a **máquina de estados** que liga uma transação à seguinte.

Ela está nas constantes, em dois arquivos:

[Orcamento.java:19-23](src/uniaoevolutions/negocio/Orcamento.java#L19-L23)

```java
public static final String STATUS_APROVADO   = "Aprovado pelo cliente";
public static final String STATUS_CONVERTIDO = "Convertido em OS";
public static final String STATUS_AGUARDANDO = "Aguardando aprovação do cliente";
```

[OrdemDeServico.java:24-26](src/uniaoevolutions/negocio/OrdemDeServico.java#L24-L26)

```java
public static final String STATUS_ABERTA   = "Aberta";
public static final String STATUS_RASCUNHO = "Rascunho";
```

O fluxo do diagrama virou transições concretas:

```
Transação 1 (outro caso de uso)      "Aprovado pelo cliente"
                                            |
                    +-----------------------+-----------------------+
                    | é a PORTA DE ENTRADA deste caso de uso        |
                    | OrcamentoDAO.buscarOrcamentosAprovados():66   |
                    +-----------------------+-----------------------+
                                            v
Transação 2 (ESTE projeto)    OS nasce "Aberta" + orçamento -> "Convertido em OS"
                              registrarOrdemServico():177 e :186
                                            |
                              desvio RN-05 -+--> orçamento volta a "Aguardando"
                                                 registrarOSComAlteracao():275
                                            v
Transação 3 (outro caso de uso)      pagamento, termos, entrega
```

Ou seja: o diagrama de atividades está no código como **contrato de fronteira**. Este caso de uso só
aceita entrada no estado "Aprovado pelo cliente" (é a RN-01) e só entrega saída no estado
"Convertido em OS" (é a POS-02). O que vem antes e depois pertence a outras iterações.

A seta de volta é a mais interessante:
[ControladoraRegistrarOS.java:275](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L275)
devolve o orçamento a "Aguardando aprovação do cliente" — o processo **retrocede** uma transação, e é
por isso que o FA-11B encerra sem registrar a Ordem de Serviço.

---

## 3. Diagrama de Casos de Uso

**No PDF:** aparece duas vezes (duas páginas de diagrama).

**No código:** um caso de uso virou **uma classe controladora**. É a convenção que o projeto segue:

> Caso de uso "Registrar Ordem de Serviço" → `ControladoraRegistrarOS`

Está dito no comentário de
[ControladoraRegistrarOS.java:23-24](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L23-L24):
*"Classe `<<control>>` do Diagrama de Classes e objeto `:Sistema` do Diagrama de Comunicação"*. Se os
outros 16 RFs fossem implementados, haveria uma controladora por caso de uso — por isso o nome não é
`Controladora` nem `Fachada`, é `ControladoraRegistrarOS`.

A parte do diagrama que o código realmente **executa** é a **generalização entre atores**. A
Descrição Expandida diz: *"Como o ator administrador herda de ambos os técnicos, ele também pode
assumir esses papéis."* Essa herança está em
[TipoUsuario.java:19-22](src/uniaoevolutions/negocio/TipoUsuario.java#L19-L22):

```java
public boolean habilitadoEm(String segmento) {
    if (this == administrador) {
        return true;           // <- a generalização do diagrama de casos de uso
    }
    ...
```

O `return true` incondicional **é** a seta de herança desenhada no diagrama. E ela reaparece na
consulta, em [UsuarioDAO.java:49-51](src/uniaoevolutions/persistencia/UsuarioDAO.java#L49-L51):

```sql
WHERE tipoUsuario = ? OR tipoUsuario = ?    -- o técnico do segmento OU o administrador
ORDER BY CASE WHEN tipoUsuario = ? THEN 1 ELSE 0 END, nome
```

O `ORDER BY` com `CASE` joga o administrador para o fim da lista. O comentário das linhas 47-48
justifica: *"ele é habilitado, mas é a alternativa, não o responsável natural"*. É uma leitura fina
do diagrama — herdar o papel não significa ser a primeira escolha.

O ator principal (Administrador) aparece também no título da janela e no texto das mensagens, que
falam sempre "o Administrador".

---

## 4. Mockup

**No PDF:** página "Mockup".

**No código:** [TelaRegistrarOrdemServico.java](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java)
— 962 linhas, e é o artefato com a tradução mais **literal** de todos. Em quatro níveis.

### 4.1 As cores, extraídas em hexadecimal

[Linhas 79-85](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L79-L85):

```java
private static final Color AZUL_CABECALHO = new Color(0x1E, 0x3A, 0x8A);
private static final Color AZUL_TITULO    = new Color(0x1D, 0x4E, 0xD8);
```

O comentário acima diz "Cores tiradas do mockup". Cada constante tem nome de função, não de cor —
`AZUL_CABECALHO`, não `AZUL_ESCURO`.

### 4.2 As cinco seções, na ordem

[Linhas 228-236](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L228-L236) são o mockup
lido de cima para baixo:

```java
conteudo.add(criarSecaoIdentificacao());
conteudo.add(criarSecaoClienteVeiculo());
conteudo.add(criarSecaoSegmento());
conteudo.add(criarSecaoDadosTecnicos());
conteudo.add(criarSecaoExecucao());
```

| Seção do mockup | Método |
|---|---|
| IDENTIFICAÇÃO DA OS | [criarSecaoIdentificacao():277](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L277) |
| CLIENTE E VEÍCULO | [criarSecaoClienteVeiculo():294](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L294) |
| SEGMENTO DO SERVIÇO | [criarSecaoSegmento():313](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L313) |
| DADOS TÉCNICOS | [criarSecaoDadosTecnicos():357](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L357) |
| EXECUÇÃO | [criarSecaoExecucao():412](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L412) |

### 4.3 Os textos, copiados sem reescrever

O aviso amarelo da seção de segmento, em
[linhas 335-336](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L335-L336):

```java
JLabel atencao = new JLabel("Atenção: a seleção do segmento determina os campos técnicos "
        + "exibidos abaixo e restringe a lista de técnicos responsáveis habilitados.");
```

E até a nota de rodapé em itálico
([linha 218](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L218)): *"Campos marcados com *
são de preenchimento obrigatório."* Os rótulos trazem o asterisco um por um:
`"Prazo estimado * (dd/mm/aaaa)"`, `"Quilometragem *"`.

### 4.4 Os dados de exemplo — o mockup virou `INSERT`

[schema.sql:232-284](sql/schema.sql#L232-L284) reproduz o cenário desenhado no mockup para que a tela
**abra igual à figura**:

```sql
('Volkswagen', 'Gol G6', 2014, '1.6', 'QAF-7C21', ...)
```

O comentário de [schema.sql:266-270](sql/schema.sql#L266-L270) revela um detalhe de engenharia
reversa: o id do orçamento é fixado em `231` de propósito, porque
[Orcamento.getNumeroOrcamento():67-72](src/uniaoevolutions/negocio/Orcamento.java#L67-L72) monta o
número a partir do id — então `231` faz a tela exibir exatamente `ORC-2026-0231`, como no mockup. Os
três itens somam R$ 750,00, também como na figura.

### 4.5 Onde o mockup e o Diagrama de Classes divergem

Dois pontos, resolvidos em favor do Diagrama de Classes:

- **"Tipo de módulo", "Fabricante", "Nº da peça"** parecem campos da OS no mockup, mas no Diagrama de
  Classes são atributos do **`Veiculo`**. Por isso a tela os exibe com `criarCampoSomenteLeitura()` e
  os preenche em
  [preencherClienteVeiculo():735-739](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L735-L739).
- **"CPF / CNPJ"** existe no mockup e **não** foi implementado: `Cliente` tem só `nome` e `telefone`.
  Registrado como lacuna 8 na seção 7 do [DOCUMENTACAO.md](DOCUMENTACAO.md).

---

## 5. Descrição Expandida de Caso de Uso

**No PDF:** a parte mais longa — pré e pós-condições, 7 regras de negócio, 12 passos, 7 fluxos
alternativos, variantes V1/V2 e 4 fluxos alternativos das variantes.

**No código:** espalhada pelas quatro camadas. É o artefato que **nomeia os métodos** — cada um traz o
passo ou o fluxo no Javadoc.

### 5.1 Pré-condições

| Pré-condição | Onde |
|---|---|
| **PRE-01** usuário autenticado como Administrador | Não implementado — a seção 2 do documento de requisitos diz que *"não há restrições rígidas de acesso implementadas"* |
| **PRE-02** existe orçamento aprovado não convertido | O `NOT EXISTS` de [buscarOrcamentosAprovados():69-71](src/uniaoevolutions/persistencia/OrcamentoDAO.java#L69-L71) |
| **PRE-03** cliente e veículo cadastrados | Os `JOIN` obrigatórios de [SELECT_ORCAMENTO:34-35](src/uniaoevolutions/persistencia/OrcamentoDAO.java#L34-L35) — sem cliente ou veículo o orçamento nem aparece na lista |
| **PRE-04** existe técnico habilitado no segmento | [buscarTecnicosHabilitados():42](src/uniaoevolutions/persistencia/UsuarioDAO.java#L42); lista vazia é o FA-09 |

A PRE-02 é verificada **por construção**: a tela não pergunta "existe orçamento aprovado?" — ela pede
a lista, e se vier vazia, é o FA-02. A pré-condição e o fluxo alternativo são o mesmo código.

### 5.2 Os 12 passos do fluxo principal

| Passo | Onde |
|---|---|
| 1. Formulário com nº da OS e data (RN-07) | [iniciarRegistroOS():52](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L52) → [gerarNumeroOS():61](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L61) |
| 2. Lista de orçamentos aprovados (RN-01) | [consultarOrcamentosAprovados():67](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L67) |
| 3. Seleciona o orçamento | [aoSelecionarOrcamento():690](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L690) + [selecionarOrcamento():362](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L362) |
| 4. Exibe cliente, veículo, segmento e itens (RN-02) | [consultarOrcamento():72](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L72) + [preencherClienteVeiculo():725](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L725) + [preencherItens():743](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L743) |
| 5. Informa o segmento | Os dois `JRadioButton` ([linhas 130-133](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L130-L133)), pré-marcados em [709-713](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L709-L713) |
| 6. Exibe o bloco técnico do segmento | [consultarBlocosDadosTecnicos():81](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L81) + `cartoes.show()` em [769](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L769) |
| 7. Quilometragem e prazo estimado | `campoQuilometragem`, `campoPrazoEstimado`; lidos em [1030-1046](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L1030-L1046) |
| 8. Dados técnicos obrigatórios (RN-03) | Bloco V1 ou V2; validados em [OrdemDeServicoModulos.validar():43](src/uniaoevolutions/negocio/OrdemDeServicoModulos.java#L43) / [OrdemDeServicoChaves.validar():34](src/uniaoevolutions/negocio/OrdemDeServicoChaves.java#L34) |
| 9. Lista de técnicos habilitados (RN-04) | [consultarTecnicosHabilitados():99](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L99) |
| 10. Seleciona o técnico responsável | `comboTecnico` ([linha 153](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L153)) |
| 11. Observações e confirma | `campoObservacoes` + botão **Salvar Ordem de Serviço** |
| 12. Mensagem de sucesso e exibe a OS | [salvar():797](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L797) + [mostrarOrdemRegistrada():1154](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L1154) |

O texto do passo 12 está **literal** em
[linha 820](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L820):

```java
"Ordem de Serviço " + os.getNumeroOS() + " registrada com sucesso"
```

### 5.3 As 7 regras de negócio

| RN | Onde está — e em quantas camadas |
|---|---|
| **RN-01** Origem obrigatória | **Duas**: [Orcamento.podeGerarOrdemDeServico():50](src/uniaoevolutions/negocio/Orcamento.java#L50) (negócio) e o `NOT EXISTS` de [OrcamentoDAO:69](src/uniaoevolutions/persistencia/OrcamentoDAO.java#L69) (banco). Verificada de novo na gravação, em [146-149](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L146-L149) |
| **RN-02** Vínculo único | **Três**: `associarOrcamento/Veiculo/Tecnico` ([OrdemDeServico.java:77-89](src/uniaoevolutions/negocio/OrdemDeServico.java#L77-L89)), o `validar()` que exige os três não-nulos, e `idOrcamento INT NOT NULL UNIQUE` ([schema.sql:170](sql/schema.sql#L170)). Os *getters* sem *setter* público garantem o "não podem ser alterados após a gravação" |
| **RN-03** Campos obrigatórios por segmento | O `validar()` da superclasse ([106](src/uniaoevolutions/negocio/OrdemDeServico.java#L106)) + `super.validar()` nas duas subclasses — é polimorfismo implementando a regra |
| **RN-04** Habilitação do técnico | **Duas**: [Usuario.habilitadoEm():31](src/uniaoevolutions/negocio/Usuario.java#L31), chamado dentro do `validar()` ([linha 123](src/uniaoevolutions/negocio/OrdemDeServico.java#L123)), e o filtro de [UsuarioDAO:50](src/uniaoevolutions/persistencia/UsuarioDAO.java#L50). O combo **e** a validação |
| **RN-05** Herança e alteração de valores | [incluirItemOrdemServico():225](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L225) e [registrarOSComAlteracao():260](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L260) |
| **RN-06** Irretratabilidade | Uma **ausência**: não existe `DELETE` de OS registrada. O único `DELETE` do projeto ([OrdemServicoDAO:185-205](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L185-L205)) tem a cláusula `statusOS = 'Rascunho'`, e o comentário das linhas 182-183 justifica por que isso não fere a regra |
| **RN-07** Numeração OS-AAAA-NNNN | [gerarNumeroOS():61-80](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L61-L80) |

A RN-07 tem a implementação mais econômica do projeto. O "sequência reiniciada a cada ano" não
precisou de rotina de virada de ano:

```java
String prefixo = String.format("OS-%d-", ano);
... WHERE numeroOS LIKE ?        // "OS-2026-%"
```

Em 1º de janeiro nenhum número casa com o novo prefixo, `MAX` devolve `null`, e a sequência recomeça
em `0001` sozinha.

### 5.4 Os fluxos alternativos

| Fluxo | Onde | Observação |
|---|---|---|
| **FA-00** Desistência | [cancelar():945](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L945) + [solicitarConfirmacaoDesistencia():336](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L336) + [descartarRegistro():344](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L344) | O FA-00.4 "mantendo o orçamento em sua situação original" é cumprido por **não fazer nada**: nada foi gravado, basta soltar as referências |
| **FA-02** Sem orçamento aprovado | [carregarOrcamentosAprovados():673-682](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L673-L682) | Desabilita os botões de gravação |
| **FA-09** Sem técnico habilitado | [aoTrocarSegmento():781-784](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L781-L784) | |
| **FA-11A** Campo obrigatório pendente | Na tela: [validarCampos():600](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L600) marca cada campo e lista as pendências na caixa do topo (FA-11A.1); cada campo também valida ao perder o foco. Depois, no negócio: `catch (IllegalArgumentException)` → [mostrarAviso():1205](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L1205) com a mensagem do `validar()` | A tela confere formato e obrigatoriedade antes de chamar a controladora; o `validar()` continua sendo a garantia das RN-03 e RN-04. O FA-11A.3 "preservando os dados já informados" é automático: a tela não é limpa |
| **FA-11B** Item não previsto | [incluirItem():868](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L868) → os dois métodos da controladora | É o único fluxo que **encerra sem registrar a OS** (FA-11B.5, [linha 936](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L936)) |
| **FA-11C** Salvar rascunho | [salvarRascunhoOS():299](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L299) | O "sem número definitivo" é a coluna `numeroOS` aceitando `NULL` ([schema.sql:161](sql/schema.sql#L161)) |
| **FA-12** Base de dados indisponível | `catch (SQLException)` → [mostrarErroDeBanco():1210](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L1210) | |

O FA-11C deixou marca até no banco. O comentário de
[schema.sql:188-191](sql/schema.sql#L188-L191) explica:

> *"As colunas aceitam NULL por causa do FA-11C: o rascunho é salvo justamente quando o registro NÃO
> pode ser concluído, portanto sem os dados técnicos. A obrigatoriedade da RN-03 é cobrada na camada
> de negócio."*

A RN-03 **não** virou `NOT NULL`, porque um fluxo alternativo exige gravar sem ela. A regra foi para
`validar()`, que só é chamado no registro definitivo. É uma decisão tomada pela leitura cruzada de
dois trechos da Descrição Expandida.

### 5.5 As variantes V1 e V2

A Descrição Expandida diz que as variantes "afetam os passos 6, 8 e 9". No código, afetam **cinco**
coisas:

| | V1 — Módulos (ECU) | V2 — Chaves |
|---|---|---|
| Classe instanciada | `OrdemDeServicoModulos` | `OrdemDeServicoChaves` |
| Tabela filha | `TB_OrdemDeServicoModulos` | `TB_OrdemDeServicoChaves` |
| Cartão do `CardLayout` | [criarBlocoModulos():381](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L381) | [criarBlocoChaves():398](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L398) |
| Campos exigidos (RN-03) | [consultarBlocosDadosTecnicos():88-93](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L88-L93) | [:84-86](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L84-L86) |
| Técnicos listados | `tecnicoDeModulos` + administrador | `tecnicoDeChaves` + administrador |

O ponto de decisão único é
[criarOrdemDeServico():196-205](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L196-L205),
cujo Javadoc cita as guardas do diagrama: *"com as guardas [seg = "Módulos"] e [seg = "Chaves"]"*.

**O que não foi implementado, e por quê.** A V2 do PDF fala em *tipo de chave, quantidade de cópias,
telecomando, código do transponder, situação do imobilizador e perda total*. A classe
`OrdemDeServicoChaves` tem **um** atributo: `servicoExecutado`. O comentário de
[OrdemDeServicoChaves.java:9-11](src/uniaoevolutions/negocio/OrdemDeServicoChaves.java#L9-L11)
registra isso como lacuna da modelagem — a tela exibe o que a modelagem oferece. Pelo mesmo motivo,
os **4 fluxos alternativos das variantes** (FAV-8.1, FAV-8.2, FAV-8.3A, FAV-8.3B) não têm código:
dependem de campos que nenhuma classe possui.

> **Observação sobre o artefato:** o PDF traz a **V2 duas vezes**, com numerações de passo diferentes
> (uma com 6.2/8.3/8.4/9.3, outra com 5.2/7.3/7.4/8.2) — aparentemente resíduo de uma versão anterior
> do documento. A implementação segue a primeira.

---

## 6. Modelo Conceitual

**No PDF:** página "Modelo conceitual".

**No código:** o pacote [src/uniaoevolutions/negocio/](src/uniaoevolutions/negocio/) — 9 classes.

A diferença entre o Modelo Conceitual e o Diagrama de Classes (artefato 10) é que o conceitual tem
**conceitos, atributos e associações, sem operações**. No código, o que corresponde a ele são os
**campos** e as **referências entre objetos**:

| Associação do modelo conceitual | Como virou código |
|---|---|
| Orcamento 0..1 solicita 1 Cliente | `private Cliente cliente;` — [Orcamento.java:32](src/uniaoevolutions/negocio/Orcamento.java#L32) |
| Orcamento 0..* refere-se a 1 Veiculo | `private Veiculo veiculo;` — [linha 33](src/uniaoevolutions/negocio/Orcamento.java#L33) |
| Orcamento 1 **compoe** 1..* ItemOrcamento | `private final List<ItemOrcamento> itens = new ArrayList<>();` — [linha 34](src/uniaoevolutions/negocio/Orcamento.java#L34) |
| OrdemDeServico 1 originada de 0..1 Orcamento | `private Orcamento orcamento;` — [OrdemDeServico.java:39](src/uniaoevolutions/negocio/OrdemDeServico.java#L39) |
| OrdemDeServico 0..* refere-se a 1 Veiculo | `private Veiculo veiculo;` — [linha 40](src/uniaoevolutions/negocio/OrdemDeServico.java#L40) |
| OrdemDeServico 0..* executada por 1 Usuario | `private Usuario tecnico;` — [linha 41](src/uniaoevolutions/negocio/OrdemDeServico.java#L41) |

Três leituras de notação UML que ficaram visíveis no código:

**Multiplicidade `1` vira referência; `1..*` vira `List`.** `cliente` é um campo simples, `itens` é
uma coleção. A multiplicidade do diagrama determinou o tipo do campo.

**A composição (losango cheio) é mais forte que a associação.** O `final` em `itens` diz que a lista
nasce e morre com o orçamento — não existe trocar a coleção. No banco, a mesma composição virou chave
primária composta ([schema.sql:132](sql/schema.sql#L132)).

**A navegabilidade é de mão única.** `OrdemDeServico` conhece o `Orcamento`, mas `Orcamento` **não**
tem um campo `ordemDeServico`. A associação é navegável só no sentido de que o caso de uso precisa —
evita referência circular e deixa claro quem depende de quem.

Os comentários de cabeçalho de cada classe de negócio transcrevem as associações.
[Orcamento.java:12-14](src/uniaoevolutions/negocio/Orcamento.java#L12-L14):

```java
/* Associações do Diagrama de Classes: 1 compoe 1..* ItemOrcamento
 * (composição), 0..1 solicita 1 Cliente, 0..* refere-se a 1 Veiculo,
 * 0..1 originada de 1 OrdemDeServico. */
```

**Uma diferença importante:** o Modelo Conceitual não tem identificadores (`idCliente`,
`idVeiculo`...), mas as classes têm. Eles vêm do Mapeamento OR, e a seção 4.1 do
[DOCUMENTACAO.md](DOCUMENTACAO.md) explica por quê: em memória dois objetos se distinguem pela
referência; no banco é preciso uma coluna que cumpra esse papel.

---

## 7. DSS — Diagrama de Sequência de Sistema

**No PDF:** página "DSS", imediatamente antes do Contrato de Operação.

**No código:** a **interface pública** de
[ControladoraRegistrarOS](src/uniaoevolutions/controle/ControladoraRegistrarOS.java). Cada mensagem
que o ator envia ao `:Sistema` no DSS é um método público:

| Mensagem do DSS | Método | Tipo |
|---|---|---|
| `iniciarRegistroOS()` | [linha 52](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L52) | consulta + reserva de número |
| `consultarOrcamentosAprovados()` | [67](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L67) | consulta |
| `consultarOrcamento(id)` | [72](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L72) | consulta |
| `consultarBlocosDadosTecnicos(segmento)` | [81](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L81) | consulta — **sem banco** |
| `consultarTecnicosHabilitados(segmento)` | [99](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L99) | consulta |
| **`registrarOrdemServico(...)`** | **[130](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L130)** | **altera o estado** |
| `incluirItemOrdemServico(...)` | [225](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L225) | FA-11B |
| `registrarOSComAlteracao(...)` | [260](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L260) | FA-11B |
| `salvarRascunhoOS(...)` | [299](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L299) | FA-11C |
| `solicitarConfirmacaoDesistencia()` | [336](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L336) | FA-00 |
| `descartarRegistro()` | [344](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L344) | FA-00 |

A frase do PDF que mais se vê no código é esta:

> *"As demais mensagens do DSS são consultas de sistema, que apenas recuperam informações sem alterar
> os dados armazenados."*

Essa classificação está materializada: todos os `consultar*` só fazem `SELECT` e devolvem objetos;
nenhum deles faz `INSERT` ou `UPDATE`. A prova de que a distinção foi levada a sério está em
[consultarBlocosDadosTecnicos()](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L81), o
único método da controladora que **não** declara `throws SQLException` — ele monta a lista de campos
em memória, sem tocar no banco, porque a RN-03 é conhecimento fixo, não dado armazenado.

**O DSS também define a fronteira do sistema**, e é por isso que a tela não tem SQL: ela está **fora**
da caixa `:Sistema` do DSS, e só pode enviar as mensagens da tabela acima. Os imports de
[TelaRegistrarOrdemServico.java:3-9](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L3-L9)
não incluem nenhum `uniaoevolutions.persistencia`. A fronteira desenhada no diagrama virou restrição
de dependência entre pacotes.

---

## 8. Contrato de Operação de Sistema

**No PDF:** página "Contrato de Operação de Sistema", com a escolha da operação, pré-condições e 14
pós-condições.

**No código:** um método —
[registrarOrdemServico(), linhas 130-190](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L130-L190).

### 8.1 A assinatura, parâmetro por parâmetro

O PDF declara:

```
registrarOrdemServico(orcamentoSelecionado, tecnicoSelecionado, quilometragem,
prazoEstimado, observacoes, relatoInicial, condicaoRecebimento,
diagnosticoInicial, servicoExecutado)
```

O código, em
[130-138](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L130-L138), tem os **9 parâmetros
com os mesmos nomes e na mesma ordem**. Isso explica duas coisas que pareceriam estranhas:

- **Por que o método recebe `int orcamentoSelecionado` em vez de `Orcamento`:** o contrato diz
  "identificado por orcamentoSelecionado", e a pós-condição é "o objeto orc **foi encontrado**" — ou
  seja, encontrar é trabalho da operação.
- **Por que recebe os campos dos dois segmentos de uma vez:** o contrato os lista todos, com
  pré-condições condicionais (*"se o segmento for 'Módulos', relatoInicial... foram informados"*).

### 8.2 As pós-condições → o corpo do método

| Pós-condição do contrato | Linha |
|---|---|
| "O objeto orc de Orcamento ... foi encontrado" | [141](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L141) |
| "O objeto vei de Veiculo associado a orc foi encontrado" | [152](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L152) |
| "O objeto tec de Tecnico ... foi encontrado" | [155](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L155) |
| "O segmento seg do orçamento orc foi identificado" | [161](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L161) |
| "Um objeto os ... foi criado, sendo da subclasse ... conforme seg" | [164](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L164) → [criarOrdemDeServico():196](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L196) |
| `os.quilometragem`, `os.prazoEstimado`, `os.observacoes` atualizados | Construtor de [OrdemDeServico:46-50](src/uniaoevolutions/negocio/OrdemDeServico.java#L46-L50) |
| "Se os for OrdemDeServicoModulos: relatoInicial, condicaoRecebimento, diagnosticoInicial" | Construtor de [OrdemDeServicoModulos:27-34](src/uniaoevolutions/negocio/OrdemDeServicoModulos.java#L27-L34) |
| "Se os for OrdemDeServicoChaves: servicoExecutado" | Construtor de [OrdemDeServicoChaves:21-25](src/uniaoevolutions/negocio/OrdemDeServicoChaves.java#L21-L25) |
| `os.dataAbertura` = data atual **e** `os.statusOS` = "Aberta" | [177](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L177) → [sinalizarAbertura():63-66](src/uniaoevolutions/negocio/OrdemDeServico.java#L63-L66) |
| orc, vei e tec associados a os | [168-170](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L168-L170) |
| "O objeto os foi persistido" | [183](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L183) |

Duas observações que o contrato explica:

**Por que os valores são passados ao construtor e não por `setter`.** O contrato diz "um objeto os foi
criado" **e depois** "os.quilometragem foi atualizado". Poderia ser `new` + `setQuilometragem()`. Mas
o Diagrama de Classes marca o construtor com `<<create>>` recebendo os parâmetros, então o projeto
escolheu construtor — e com isso a OS nunca existe num estado incompleto.

**Por que as duas pós-condições de `dataAbertura` e `statusOS` viraram um método só.**
`sinalizarAbertura()` executa as duas juntas, e os dois `setter` são **privados**
([OrdemDeServico.java:68-74](src/uniaoevolutions/negocio/OrdemDeServico.java#L68-L74)). O comentário
explica: *"para que não se grave uma data de abertura sem o status correspondente"*. Duas
pós-condições que sempre valem juntas viraram uma operação atômica.

### 8.3 As pré-condições → onde falham

O contrato lista pré-condições; o código as verifica e **lança exceção** quando falham, alimentando os
fluxos alternativos:

```java
if (orc == null) throw new IllegalArgumentException("Selecione o orçamento...");   // linha 142
if (!orc.podeGerarOrdemDeServico()) throw new IllegalArgumentException(...);       // linha 146 — RN-01
if (tec == null) throw new IllegalArgumentException("Selecione o técnico...");     // linha 156
os.validar();                                                                     // linha 174 — RN-03/RN-04
```

A ordem importa:
[os.validar()](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L174) vem **antes** de
`sinalizarAbertura()` e de `salvar()`. O comentário das linhas 172-173 diz: *"antes de qualquer acesso
ao banco"*. Se uma pré-condição falhar, nada foi gravado — é o FA-11A com integridade garantida.

---

## 9. Diagrama de Comunicação

**No PDF:** página "Diagrama de comunicação".

**No código:** o **mesmo método** do artefato anterior, mas agora o que corresponde é a **ordem das
linhas** e os comentários numerados. É o artefato mais literalmente transcrito do projeto.

[ControladoraRegistrarOS.java:140-183](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L140-L183):

```java
// 1.1  orc = buscarOrcamento(orcamentoSelecionado) : Orcamento
Orcamento orc = orcamentoDAO.buscarOrcamento(orcamentoSelecionado);
// 1.2  vei = buscarVeiculo(orc) : Veiculo  -> Orcamento.getVeiculo() do Diagrama de Classes
Veiculo vei = orc.getVeiculo();
// 1.3  tec = buscarTecnico(tecnicoSelecionado) : Tecnico  -> UsuarioDAO.buscarUsuario
Usuario tec = usuarioDAO.buscarUsuario(tecnicoSelecionado);
// 1.4  seg = getSegmentoPretendido() : String
String seg = orc.getSegmentoPretendido();
// 1.5  [seg = "Modulos"] / 1.6 [seg = "Chaves"]  <<create>>
OrdemDeServico os = criarOrdemDeServico(seg, ...);
// 1.7  sinalizarAbertura() -> 1.7.1 setDataAbertura, 1.7.2 setStatusOs("Aberta")
os.sinalizarAbertura();
// 1.8 / 1.9 / 1.10  associaOS(os) -> associarOrcamento, associarVeiculo, associarTecnico
os.associarOrcamento(orc);
os.associarVeiculo(vei);
os.associarTecnico(tec);
os.validar();                       // RN-03 e RN-04 (não é mensagem do diagrama)
// 1.11 salvar(os)
ordemServicoDAO.salvar(os);
```

Os números do diagrama estão também **nos destinos** das mensagens, cada um no seu arquivo:

| Mensagem | Onde o Javadoc a cita |
|---|---|
| 1.2 | [Orcamento.getVeiculo():39](src/uniaoevolutions/negocio/Orcamento.java#L39) — *"Operação do Diagrama de Classes (mensagem 1.2)"* |
| 1.3 | [UsuarioDAO.buscarUsuario():21](src/uniaoevolutions/persistencia/UsuarioDAO.java#L21) |
| 1.4 | [Orcamento.getSegmentoPretendido():44](src/uniaoevolutions/negocio/Orcamento.java#L44) |
| 1.5 | [OrdemDeServicoModulos:26](src/uniaoevolutions/negocio/OrdemDeServicoModulos.java#L26) — *"`<<create>>` com a guarda [seg = "Módulos"]"* |
| 1.6 | [OrdemDeServicoChaves:20](src/uniaoevolutions/negocio/OrdemDeServicoChaves.java#L20) |
| 1.7, 1.7.1, 1.7.2 | [OrdemDeServico.sinalizarAbertura():56-62](src/uniaoevolutions/negocio/OrdemDeServico.java#L56-L62) |
| 1.8, 1.9, 1.10 | [OrdemDeServico:76-89](src/uniaoevolutions/negocio/OrdemDeServico.java#L76-L89) |
| 1.11 | [OrdemServicoDAO.salvar():31-34](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L31-L34) |

### 9.1 Os objetos do diagrama

| Objeto do diagrama | Classe |
|---|---|
| `:Interface` | `TelaRegistrarOrdemServico` — dito em [linha 69](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java#L69) |
| `:Sistema` | `ControladoraRegistrarOS` — dito em [linha 23](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L23) |
| `orc`, `vei`, `tec`, `os` | as variáveis locais, com **os mesmos nomes** |

As variáveis se chamarem `orc`, `vei`, `tec`, `seg` — e não `orcamento`, `veiculo`, `tecnico` — é
escolha deliberada: são os nomes dos objetos no diagrama. Quem lê o diagrama ao lado do código
encontra os mesmos identificadores.

### 9.2 As divergências assumidas

**1. A ordem 1.7 depois de 1.8–1.10 — corrigida.** O código chamava `sinalizarAbertura()` depois das
associações. Na auditoria da 2ª iteração (RELATORIO_ITERACAO2.md, seção 10.2) a ordem passou a ser a
do diagrama: 1.7 antes de 1.8–1.10. O `validar()` (RN-04) ficou depois de 1.10 e antes de 1.11, e a
regressão do Registrar OS continua passando.

**2. O nome das mensagens de associação.** O diagrama envia `associaOS(os)` para `orc`, `vei` e `tec`;
o Diagrama de Classes (mais recente) põe `associarOrcamento(ocr)`, `associarVeiculo(vei)` e
`associarTecnico(tec)` na própria OS. O projeto seguiu o Diagrama de Classes — e é por isso que
[OrdemDeServico.java:77](src/uniaoevolutions/negocio/OrdemDeServico.java#L77) tem o parâmetro grafado
`ocr`, com o erro de digitação do artefato preservado.

**3. "Aberta" × "Em execução".** O diagrama manda `setStatusOs("Aberta")` (1.7.2) e o Contrato de
Operação repete; a POS-01 e o mockup dizem "Em execução". O código seguiu `"Aberta"`, e a divergência
está registrada no próprio arquivo, em
[OrdemDeServico.java:17-23](src/uniaoevolutions/negocio/OrdemDeServico.java#L17-L23).

---

## 10. Diagrama de Classes

**No PDF:** página "Diagrama de classes".

**No código:** **todo o projeto**. É a fonte principal — a seção 0 do
[DOCUMENTACAO.md](DOCUMENTACAO.md) o identifica assim. Os estereótipos do diagrama viraram pacotes:

| Estereótipo | Classe | Pacote |
|---|---|---|
| `<<boundary>>` | `TelaRegistrarOrdemServico` | `view` |
| `<<control>>` | `ControladoraRegistrarOS` | `controle` |
| (entidades) | as 9 classes de domínio | `negocio` |
| (DAOs) | `OrcamentoDAO`, `UsuarioDAO`, `OrdemServicoDAO` | `persistencia` |

### 10.1 As quatro notações de UML que o código honra

**Nome em itálico = classe abstrata.**
[OrdemDeServico.java:15](src/uniaoevolutions/negocio/OrdemDeServico.java#L15):

```java
public abstract class OrdemDeServico {
```

O comentário das linhas 9-10 dá as duas razões: *"ABSTRATA (itálico no Diagrama de Classes): toda OS é
de Módulos ou de Chaves"*. Notação **e** semântica de negócio.

**Visibilidade `-` = `private`.** Os dois `setter` privados de
[OrdemDeServico:68-74](src/uniaoevolutions/negocio/OrdemDeServico.java#L68-L74) são privados porque o
diagrama os marca com `-`. A consequência: a DAO precisou de um caminho próprio para reconstruir uma
OS lida do banco, que é
[restaurarEstadoPersistido():181](src/uniaoevolutions/negocio/OrdemDeServico.java#L181) — e o Javadoc
diz exatamente isso: *"Como setDataAbertura e setStatusOS são privados, é o único caminho para a DAO
reconstruir uma OS existente"*.

**Triângulo de generalização = `extends`.** As duas subclasses, com `@Override` em `getSegmento()` e
`validar()`.

**`<<create>>` = construtor parametrizado.** Os construtores das subclasses
([OrdemDeServicoModulos:27](src/uniaoevolutions/negocio/OrdemDeServicoModulos.java#L27),
[OrdemDeServicoChaves:21](src/uniaoevolutions/negocio/OrdemDeServicoChaves.java#L21)).

### 10.2 As operações, uma por uma

Cada operação do diagrama está implementada com **a mesma grafia**:

| Classe | Operações do diagrama |
|---|---|
| `OrdemDeServico` | `sinalizarAbertura()`, `setDataAbertura()`, `setStatusOS()`, `associarOrcamento()`, `associarVeiculo()`, `associarTecnico()` |
| `Orcamento` | `getVeiculo()`, `getSegmentoPretendido()` |
| `OrcamentoDAO` | `buscarOrcamento(id)`, `buscarOrcamentosAprovados()` |
| `UsuarioDAO` | `buscarUsuario(id)`, `buscarTecnicosHabilitados(segmento)` |
| `OrdemServicoDAO` | `salvar(os)`, `gerarNumeroOS()` |

### 10.3 As três classes que não estão no diagrama

As três têm justificativa no próprio arquivo:

| Classe | Por que existe |
|---|---|
| [ServicoBase](src/uniaoevolutions/negocio/ServicoBase.java) | Está no **Mapeamento OR** (`TB_ServicoBase`, referenciada por `TB_ItemOrcamento`), só não no Diagrama de Classes — que cobre apenas este caso de uso. Comentário nas linhas 6-8 |
| [TipoUsuario](src/uniaoevolutions/negocio/TipoUsuario.java) | É o **enumerado do atributo** `tipoUsuario : {administrador \| tecnicoDeChaves \| tecnicoDeModulos}` da classe `Usuario`. O diagrama declara os valores; o código os torna um tipo |
| [Segmento](src/uniaoevolutions/negocio/Segmento.java) | Não é conceito do domínio: guarda num lugar só os literais `"Módulos"` e `"Chaves"`, *"para a controladora, as DAOs e a tela não divergirem na grafia"* (linhas 7-8). Como os valores levam acento, uma divergência de grafia seria um defeito silencioso |

### 10.4 A troca de `Tecnico` por `Usuario`

O diagrama **mudou de versão** durante o trabalho, e o código acompanhou. Está documentado em
[Usuario.java:7-10](src/uniaoevolutions/negocio/Usuario.java#L7-L10):

> *"a versão anterior do Diagrama de Classes tinha a hierarquia Tecnico / TecnicoDeChaves /
> TecnicoDeModulos / Administrador. A versão corrigida (01/10/2026), implementada aqui, usa uma única
> classe com o atributo enumerado tipoUsuario, e o Mapeamento OR ficou só com TB_Usuario."*

Daí uma particularidade que vale saber explicar: o Diagrama de Comunicação ainda diz
`tec = buscarTecnico(...) : Tecnico`, mas o código tem
`Usuario tec = usuarioDAO.buscarUsuario(...)`. O comentário da linha 154 preserva o texto original da
mensagem ao lado da chamada nova. O **nome da variável** (`tec`) veio do diagrama antigo; o **tipo**
(`Usuario`) veio do novo.

---

## 11. Mapeamento Objeto-Relacional

**No PDF:** última seção, com a notação `TB_Tabela(coluna1, ...)`, `<<PK>>`, `<<FK>>`, `<<UNIQUE>>`.

**No código:** [sql/schema.sql](sql/schema.sql) e as três DAOs. O cabeçalho do script
([linhas 5-7](sql/schema.sql#L5-L7)) declara: *"Este script é a tradução literal do artefato
Design/Mapeamento_Objeto_Relacional.corrigido.docx. Os nomes das tabelas e das colunas são exatamente
os do mapeamento."*

Cada `CREATE TABLE` traz, em comentário, **a linha do artefato que o originou**. Por exemplo,
[schema.sql:34](sql/schema.sql#L34):

```sql
-- TB_Cliente(idCliente<<PK>>, nome, telefone)
-- Classe Cliente do Diagrama de Classes.
```

### 11.1 As 10 tabelas

| Tabela do PDF | Linha no `schema.sql` | Situação |
|---|---|---|
| `TB_Cliente` | [37](sql/schema.sql#L37) | classe simples → tabela |
| `TB_Veiculo` | [50](sql/schema.sql#L50) | classe simples; `placa` e `numeroChassi` com `UNIQUE` |
| `TB_ServicoBase` | [74](sql/schema.sql#L74) | classe simples |
| `TB_Usuario` | [87](sql/schema.sql#L87) | `tipoUsuario` virou `ENUM` |
| `TB_Orcamento` | [100](sql/schema.sql#L100) | 2 chaves estrangeiras |
| `TB_ItemOrcamento` | [126](sql/schema.sql#L126) | **composição** → PK composta |
| `TB_OrdemDeServico` | [159](sql/schema.sql#L159) | tabela da **superclasse** |
| `TB_OrdemDeServicoModulos` | [192](sql/schema.sql#L192) | tabela da **subclasse** V1 |
| `TB_OrdemDeServicoChaves` | [209](sql/schema.sql#L209) | tabela da **subclasse** V2 |
| `TB_Pagamento` | [222](sql/schema.sql#L222) | consta do artefato; **não usada** neste caso de uso |

### 11.2 As três situações de mapeamento

**Classe simples → tabela.** Cada atributo vira coluna com o mesmo nome. Mais o `id`, que o Diagrama
de Classes não tem mas o Mapeamento OR exige como chave primária.

**Associação → chave estrangeira.** `TB_Orcamento.idCliente<<FK>>` em
[linha 109](sql/schema.sql#L109). A regra: em 1-para-muitos, a FK fica no lado *muitos*.

A **composição** é mais forte. [schema.sql:132](sql/schema.sql#L132):

```sql
PRIMARY KEY (idItemOrcamento, idOrcamento),
```

Chave primária **composta**. O comentário das linhas 116-118 explica: *"o item é parte de uma
composição (losango cheio "compoe" no Diagrama de Classes): o item não existe fora do seu
orçamento"*. O losango cheio da UML virou uma PK de duas colunas.

E a RN-02 virou uma palavra-chave. [schema.sql:170](sql/schema.sql#L170):

```sql
idOrcamento INT NOT NULL UNIQUE,
```

O `UNIQUE` é o que impede duas Ordens de Serviço para o mesmo orçamento — a regra de negócio
fiscalizada pelo banco, não só pelo código.

**Herança → três tabelas.** O caso mais importante, e o que o enunciado destaca. O artefato escolheu
"uma tabela por classe":

```
TB_OrdemDeServico(idOrdemDeServico<<PK>>, ...)
   |-- TB_OrdemDeServicoModulos(idOrdemDeServico<<PK>><<FK>>, relatoInicial, ...)
   |-- TB_OrdemDeServicoChaves (idOrdemDeServico<<PK>><<FK>>, servicoExecutado)
```

A PK da filha é **ao mesmo tempo FK** para a tabela da mãe
([schema.sql:193, 201-202](sql/schema.sql#L193-L202)). Isso tem **quatro** consequências no código,
todas em [OrdemServicoDAO.java](src/uniaoevolutions/persistencia/OrdemServicoDAO.java):

1. **Gravar são dois `INSERT` numa transação**
   ([salvar():35-55](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L35-L55)): a mãe gera o id
   pelo `AUTO_INCREMENT`, a filha reusa **o mesmo** id. O `commit`/`rollback` existe porque uma OS
   pela metade não faz sentido.
2. **A escolha da tabela é feita pelo tipo do objeto**
   ([inserirSubclasse():151-174](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L151-L174)):
   ```java
   if (os instanceof OrdemDeServicoModulos) { ... }
   else if (os instanceof OrdemDeServicoChaves) { ... }
   ```
   Esse `if` é o encontro dos dois mundos: em Java a diferença entre as subclasses é o **tipo**; no
   banco é a **tabela**.
3. **A leitura é o caminho inverso**
   ([buscarHistoricoTecnico():91-97](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L91-L97)):
   dois `LEFT JOIN`, e
   [montarOrdemDeServico():218-234](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L218)
   decide a subclasse pela coluna que veio não-nula.
4. **A exclusão é na ordem inversa**
   ([linhas 199-204](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L199-L204)): a linha filha
   sai primeiro, porque é ela que aponta para a mãe.

O caminho de volta completo está em
[OrcamentoDAO](src/uniaoevolutions/persistencia/OrcamentoDAO.java): cinco tabelas ligadas por chave
estrangeira voltam a ser **um** objeto `Orcamento` com `cliente`, `veiculo` e `itens` preenchidos. O
comentário das linhas 20-21 resume: *"quem chama a DAO não vê tabela nem JOIN"*. É a definição de
mapeamento objeto-relacional em uma frase.

### 11.3 As duas colunas acrescentadas ao artefato

O script declara onde **divergiu** do Mapeamento OR, com a tag `[ACRESCIMO]`:

| Acréscimo | Linha | Por quê |
|---|---|---|
| `TB_ItemOrcamento.quantidade` | [129](sql/schema.sql#L129) | O mockup tem a coluna "Qtd." e `incluirItemOrdemServico(..., quantidade, ...)` recebe o valor, mas nem a classe nem o artefato preveem o atributo |
| `TB_OrdemDeServico.numeroOS` | [161](sql/schema.sql#L161) | O atributo existe na classe e a RN-07 exige o formato `OS-AAAA-NNNN`, mas o artefato não tem a coluna. Aceita `NULL` por causa do FA-11C |

Há uma terceira lacuna que **não** virou coluna: a justificativa exigida pela RN-05. A decisão foi
gravá-la em `TB_ServicoBase.observacoesTecnicas`, prefixada —
[OrcamentoDAO.java:149](src/uniaoevolutions/persistencia/OrcamentoDAO.java#L149):

```java
comando.setString(3, "Justificativa (RN-05): " + justificativa);
```

O comentário das linhas 117-118 justifica: é *"a única coluna de texto livre ligada ao item"*. Não foi
inventada estrutura nova.

---

## Resumo de bolso

Para a pergunta "onde está o artefato X no código":

| Artefato | Arquivo-âncora |
|---|---|
| Requisitos (RNFs) | [Conexao.java:16](src/uniaoevolutions/persistencia/Conexao.java#L16) e [MainRegistrarOrdemServico.java:13](src/uniaoevolutions/MainRegistrarOrdemServico.java#L13) |
| Processo de negócio | As constantes de status em [Orcamento.java:19-23](src/uniaoevolutions/negocio/Orcamento.java#L19-L23) |
| Casos de uso | Uma controladora por caso de uso + [TipoUsuario.java:20](src/uniaoevolutions/negocio/TipoUsuario.java#L20) |
| Mockup | [TelaRegistrarOrdemServico.java](src/uniaoevolutions/view/TelaRegistrarOrdemServico.java) + os dados de [schema.sql:232](sql/schema.sql#L232) |
| Descrição Expandida | Os **nomes dos métodos** das 4 camadas |
| Modelo Conceitual | Os **campos** do pacote [negocio](src/uniaoevolutions/negocio/) |
| DSS | Os **métodos públicos** de [ControladoraRegistrarOS](src/uniaoevolutions/controle/ControladoraRegistrarOS.java) |
| Contrato de Operação | A **assinatura** de [registrarOrdemServico():130](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L130) |
| Diagrama de Comunicação | O **corpo** do mesmo método, [140-183](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L140-L183) |
| Diagrama de Classes | Os **pacotes** e a hierarquia `OrdemDeServico` |
| Mapeamento OR | [sql/schema.sql](sql/schema.sql) + [OrdemServicoDAO.salvar():35](src/uniaoevolutions/persistencia/OrdemServicoDAO.java#L35) |

---

## Observações sobre o documento de artefatos

Dois pontos que são do PDF, não do código, e que valem conferir no original antes da entrega:

1. A **variante V2 aparece duas vezes** na Descrição Expandida, com numerações de passo diferentes
   (6.2/8.3/8.4/9.3 e 5.2/7.3/7.4/8.2). Aparenta ser resíduo de uma versão anterior. A implementação
   segue a primeira.
2. A tabela de **regras de negócio** saiu com os códigos RN-01 a RN-07 desalinhados das respectivas
   descrições. Pode ser apenas efeito da diagramação, mas convém verificar.

As divergências **entre** artefatos — e as decisões tomadas em cada caso — estão na seção 7 do
[DOCUMENTACAO.md](DOCUMENTACAO.md).
