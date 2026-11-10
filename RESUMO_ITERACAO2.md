# Resumo da 2ª iteração: Solicitar Orçamento (RF-5)

União Evolutions · APSOO 2026.2 · Grupo G5 · 07/11/2026. O relatório completo está em
`RELATORIO_ITERACAO2.md`.

## O que foi feito

- **Caso de uso Solicitar Orçamento** implementado em Java (Swing + JDBC + MySQL), em 4 camadas:
  tela, controladora, negócio e persistência.
- **Mesmo projeto da 1ª iteração** (`Implementacao/registrar-ordem-servico/`). O Registrar Ordem de
  Serviço continua funcionando.
- **Fiel à modelagem:**
  - nomes, tipos e parâmetros seguem o diagrama de classes v2;
  - `registrarOrcamento` segue as mensagens 1.1-1.15.3 do diagrama de comunicação;
  - a tela envia as mensagens na ordem do DSS e reproduz o mockup.
- **Fluxos cobertos:**
  - fluxo principal;
  - variantes V1 (Módulos) e V2 (Chaves);
  - fluxos alternativos FA-00, FA-02, FA-04, FA-09A/B, FA-11A/B, FA-12A/B/C/D e FA-13;
  - fluxos das variantes FAV-8.1, FAV-8.2 e FAV-8.4A/B.
- **Validação:**
  - cada campo é validado ao perder o foco e de novo ao registrar;
  - CPF e CNPJ com dígitos verificadores;
  - datas que existem no calendário;
  - valores em formato brasileiro;
  - campos numéricos não aceitam letras.
- **Banco** no Mapeamento OR v2, com dados de exemplo para testar só selecionando.
- **Testes:** 167 verificações na tela real e 69 sem interface, sem falhas.

## Como executar

1. Importe `Implementacao/registrar-ordem-servico/sql/schema.sql` no MySQL/MariaDB (phpMyAdmin →
   Importar), com usuário `root` sem senha.
2. No VS Code, abra a pasta `Implementacao/registrar-ordem-servico` (não a raiz do repositório).
3. Clique em ▶ Run em `MainSolicitarOrcamento.java`. Para entrar como técnica de Chaves, passe o
   argumento `3`.
4. Clique na lupa do campo Cliente e escolha um cliente; escolha a placa e o segmento.

## Arquivos

Caminhos relativos a `Implementacao/registrar-ordem-servico/`. **(novo)** = criado nesta iteração;
**(alterado)** = da 1ª iteração, modificado; sem marca = da 1ª iteração, sem mudança.

### Pontos de entrada

| Arquivo | Para que serve |
|---|---|
| `src/uniaoevolutions/MainSolicitarOrcamento.java` **(novo)** | Abre a tela do Solicitar Orçamento; recebe o usuário logado como argumento |
| `src/uniaoevolutions/MainRegistrarOrdemServico.java` | Abre a tela do Registrar Ordem de Serviço |
| `src/uniaoevolutions/MenuPrincipal.java` **(novo)** | Menu com botões para abrir as duas telas e escolher o usuário logado; mostra se o banco conectou |

### Interface (`view`)

| Arquivo | Para que serve |
|---|---|
| `TelaSolicitarOrcamento.java` **(novo)** | Tela do mockup (formulário, janelas e mensagens de erro). Valida campos e conversa só com a controladora |
| `Validacoes.java` **(novo)** | Checa formato: CPF/CNPJ, data, valor, telefone, placa, ano e chassi |
| `TelaRegistrarOrdemServico.java` | Tela do Registrar OS |

### Controladoras (`controle`)

| Arquivo | Para que serve |
|---|---|
| `ControladoraSolicitarOrcamento.java` **(novo)** | As 19 operações do diagrama; aplica as regras RN-01 a RN-08; `registrarOrcamento` monta e grava o orçamento |
| `ControladoraRegistrarOS.java` **(alterado)** | Registrar OS; mensagens reordenadas para seguir o diagrama de comunicação |

### Negócio (`negocio`)

| Arquivo | Para que serve |
|---|---|
| `Orcamento.java` **(alterado)** | Superclasse do orçamento: número, datas, situação, valor estimado, itens e vínculos |
| `OrcamentoModulos.java` **(novo)** | Orçamento de módulos: sintoma, bancada, etiqueta ilegível e módulo |
| `OrcamentoChaves.java` **(novo)** | Orçamento de chaves: tipo, cópias, imobilizador, perda total e documentos |
| `ItemOrcamento.java` **(alterado)** | Serviço do orçamento com o valor proposto |
| `ServicoBase.java` | Serviço do catálogo, com categoria (segmento) |
| `Cliente.java` **(alterado)** | Cliente; ganhou `cpfCnpj` e o construtor do cadastro rápido |
| `Veiculo.java` **(alterado)** | Veículo; ganhou o construtor do cadastro rápido e os vínculos com módulos e segurança |
| `ModuloECU.java` **(novo)** | Módulo/central eletrônica de um veículo |
| `InformacoesSegurancaVeiculo.java` **(novo)** | Senhas e código mecânico do veículo |
| `Usuario.java`, `TipoUsuario.java` | Usuário e seu perfil (técnico de módulos, técnico de chaves, administrador) |
| `Segmento.java` | Constantes "Módulos" e "Chaves" |
| `OrdemDeServico.java` **(alterado)** e subclasses | Ordem de Serviço (1ª iteração); só o nome de um parâmetro mudou |

### Persistência (`persistencia`)

| Arquivo | Para que serve |
|---|---|
| `OrcamentoDAO.java` **(alterado)** | Lê e grava orçamentos (superclasse, subclasse e itens numa transação); gera o número ORC-AAAA-NNNN |
| `ClienteDAO.java` **(novo)** | Busca cliente por CPF/CNPJ, nome ou telefone; grava cliente |
| `VeiculoDAO.java` **(novo)** | Busca veículo por placa ou por cliente; grava veículo |
| `ServicoBaseDAO.java` **(novo)** | Lista serviços por segmento; busca serviço |
| `ModuloECUDAO.java` **(novo)** | Busca o módulo de um veículo |
| `Conexao.java`, `UsuarioDAO.java`, `OrdemServicoDAO.java` | Conexão com o banco, usuários e OS (1ª iteração) |

### Banco

| Arquivo | Para que serve |
|---|---|
| `sql/schema.sql` **(alterado)** | Cria as 13 tabelas do Mapeamento OR v2 e os dados de exemplo (6 clientes com veículos, módulos e histórico) |

### Testes

| Arquivo | Para que serve |
|---|---|
| `test/uniaoevolutions/controle/TesteControladoras.java` **(novo)** | Regras, validadores, ordem das mensagens e regressão do Registrar OS |
| `test/uniaoevolutions/view/TesteTelaSolicitarOrcamento.java` **(novo)** | 10 cenários que usam a tela real, inclusive teclado e ordem do DSS |
| `test/uniaoevolutions/view/ControladoraEspia.java` **(novo)** | Registra a ordem das mensagens que a tela envia |
| `test/uniaoevolutions/view/Robo.java` **(novo)** | Apoio aos testes de tela (clicar, digitar, capturar imagem) |
| `test/uniaoevolutions/BancoDeTeste.java` **(novo)** | Recria o banco antes de cada teste |
| `test/auditoria/*.py` **(novo)** | Scripts que comparam o código com o diagrama de classes e o `schema.sql` com o Mapeamento OR |

### Documentação

| Arquivo | Para que serve |
|---|---|
| `RELATORIO_ITERACAO2.md` (raiz) **(novo)** | Relatório completo: rastreabilidade, validações, decisões, contradições e correções |
| `RESUMO_ITERACAO2.md` (raiz) **(novo)** | Este resumo |
| `CLAUDE.md` (raiz) **(novo)** | Arquitetura, como rodar e armadilhas, para quem for mexer no código |
| `docs/telas-iteracao2/` **(novo)** | 18 capturas das telas, geradas pelos testes |
| `README.md` **(alterado)** | Instalação e execução; seção da 2ª iteração |
| `DOCUMENTACAO.md`, `RASTREABILIDADE.md` **(alterado)** | Documentação da 1ª iteração, atualizada para a nova ordem das mensagens |

## Pontos de atenção

- **Situações do orçamento:** a 1ª iteração usa "Aprovado pelo cliente" e "Convertido em OS"; o
  diagrama v2 usa "Aprovado" e "Em análise". O grupo precisa unificar.
- **CPFs do mockup:** têm dígito verificador inválido; os dados de exemplo usam CPFs corrigidos.
- **Fora do escopo:** auditoria (POS-07), expiração automática, cancelamento com motivo e retomar
  rascunho. Nenhum diagrama modela essas operações.
- **Driver JDBC:** o Gmail bloqueia `.jar`. Se o projeto for enviado por email sem o driver, baixe
  `mysql-connector-j-8.4.0.jar` e coloque em `lib/`.
