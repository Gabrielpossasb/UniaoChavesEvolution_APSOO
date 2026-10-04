-- =====================================================================
-- União Evolutions - Sistema de Gestão de Serviços Automotivos
-- Caso de uso: Registrar Ordem de Serviço (RF-6)
--
-- Este script é a tradução literal do artefato
--   Design/Mapeamento_Objeto_Relacional.corrigido.docx
-- Os nomes das tabelas e das colunas são exatamente os do mapeamento.
-- As duas colunas acrescentadas ao mapeamento estão marcadas com
-- [ACRESCIMO] e justificadas na seção 7 do DOCUMENTACAO.md.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS uniao_evolutions
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE uniao_evolutions;

-- Apaga só as tabelas deste caso de uso, na ordem inversa das dependências
-- (a tabela que aponta sai antes da tabela apontada). O script pode ser
-- executado quantas vezes for preciso, e nenhuma outra tabela do banco é
-- afetada.
DROP TABLE IF EXISTS TB_Pagamento;
DROP TABLE IF EXISTS TB_OrdemDeServicoChaves;
DROP TABLE IF EXISTS TB_OrdemDeServicoModulos;
DROP TABLE IF EXISTS TB_OrdemDeServico;
DROP TABLE IF EXISTS TB_ItemOrcamento;
DROP TABLE IF EXISTS TB_Orcamento;
DROP TABLE IF EXISTS TB_Usuario;
DROP TABLE IF EXISTS TB_ServicoBase;
DROP TABLE IF EXISTS TB_Veiculo;
DROP TABLE IF EXISTS TB_Cliente;

-- ---------------------------------------------------------------------
-- TB_Cliente(idCliente<<PK>>, nome, telefone)
-- Classe Cliente do Diagrama de Classes.
-- ---------------------------------------------------------------------
CREATE TABLE TB_Cliente (
    idCliente INT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    telefone  VARCHAR(20)  NOT NULL
);

-- ---------------------------------------------------------------------
-- TB_Veiculo(idVeiculo<<PK>>, marca, modelo, anoModelo, motorizacao,
--            placa<<UNIQUE>>, numeroChassi<<UNIQUE>>, tipoModulo,
--            modeloECU, identificacaoModulo, codigoMecanico,
--            senhaImobilizador, senhaAlarme, senhaRadio)
-- Classe Veiculo do Diagrama de Classes.
-- ---------------------------------------------------------------------
CREATE TABLE TB_Veiculo (
    idVeiculo           INT AUTO_INCREMENT PRIMARY KEY,
    marca               VARCHAR(50)  NOT NULL,
    modelo              VARCHAR(50)  NOT NULL,
    anoModelo           INT          NOT NULL,
    motorizacao         VARCHAR(20),
    placa               VARCHAR(10)  NOT NULL UNIQUE,
    numeroChassi        VARCHAR(30)  UNIQUE,
    tipoModulo          VARCHAR(60),
    modeloECU           VARCHAR(60),
    identificacaoModulo VARCHAR(60),
    codigoMecanico      VARCHAR(30),
    senhaImobilizador   VARCHAR(30),
    senhaAlarme         VARCHAR(30),
    senhaRadio          VARCHAR(30)
);

-- ---------------------------------------------------------------------
-- TB_ServicoBase(idServicoBase<<PK>>, descricao, categoria,
--                observacoesTecnicas)
-- Catálogo de serviços. Não aparece no Diagrama de Classes (que cobre
-- apenas o caso de uso Registrar OS), mas é exigido pelo Mapeamento OR
-- porque TB_ItemOrcamento referencia esta tabela.
-- ---------------------------------------------------------------------
CREATE TABLE TB_ServicoBase (
    idServicoBase       INT AUTO_INCREMENT PRIMARY KEY,
    descricao           VARCHAR(150) NOT NULL,
    categoria           VARCHAR(30)  NOT NULL,
    observacoesTecnicas VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- TB_Usuario(idUsuario<<PK>>, nome, tipoUsuario)
--   tipoUsuario {administrador | tecnicoDeChaves | tecnicoDeModulos}
-- Classe Usuario do Diagrama de Classes. É o técnico responsável da OS
-- (associação "executada por").
-- ---------------------------------------------------------------------
CREATE TABLE TB_Usuario (
    idUsuario   INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    tipoUsuario ENUM('administrador', 'tecnicoDeChaves', 'tecnicoDeModulos') NOT NULL
);

-- ---------------------------------------------------------------------
-- TB_Orcamento(idOrcamento<<PK>>, dataSolicitacao, origemAtendimento,
--              segmentoPretendido, valorEstimado, status,
--              idCliente<<FK>>, idVeiculo<<FK>>)
-- Classe Orcamento. O status controla a RN-01 (só orçamentos
-- "Aprovado pelo cliente" podem gerar OS) e a POS-02.
-- ---------------------------------------------------------------------
CREATE TABLE TB_Orcamento (
    idOrcamento        INT AUTO_INCREMENT PRIMARY KEY,
    dataSolicitacao    DATE         NOT NULL,
    origemAtendimento  VARCHAR(50),
    segmentoPretendido VARCHAR(20)  NOT NULL,
    valorEstimado      DOUBLE       NOT NULL DEFAULT 0,
    status             VARCHAR(40)  NOT NULL,
    idCliente          INT          NOT NULL,
    idVeiculo          INT          NOT NULL,
    CONSTRAINT fk_orcamento_cliente FOREIGN KEY (idCliente) REFERENCES TB_Cliente(idCliente),
    CONSTRAINT fk_orcamento_veiculo FOREIGN KEY (idVeiculo) REFERENCES TB_Veiculo(idVeiculo)
);

-- ---------------------------------------------------------------------
-- TB_ItemOrcamento(idItemOrcamento<<PK>>, valorProposto,
--                  idOrcamento<<PK>><<FK>>, idServicoBase<<FK>>)
-- Classe ItemOrcamento. A chave primária é composta porque o item é
-- parte de uma composição (losango cheio "compoe" no Diagrama de
-- Classes): o item não existe fora do seu orçamento.
--
-- [ACRESCIMO 1] coluna "quantidade".
--   Justificativa: o mockup tem a coluna "Qtd." na tabela de itens e a
--   operação ControladoraRegistrarOS.incluirItemOrdemServico recebe
--   "quantidade : int". Nem a classe ItemOrcamento nem o Mapeamento OR
--   preveem o atributo. Registrado como lacuna da modelagem.
-- ---------------------------------------------------------------------
CREATE TABLE TB_ItemOrcamento (
    idItemOrcamento INT AUTO_INCREMENT,
    valorProposto   DOUBLE NOT NULL,
    quantidade      INT    NOT NULL DEFAULT 1,   -- [ACRESCIMO 1]
    idOrcamento     INT    NOT NULL,
    idServicoBase   INT    NOT NULL,
    PRIMARY KEY (idItemOrcamento, idOrcamento),
    CONSTRAINT fk_item_orcamento   FOREIGN KEY (idOrcamento)   REFERENCES TB_Orcamento(idOrcamento),
    CONSTRAINT fk_item_servicobase FOREIGN KEY (idServicoBase) REFERENCES TB_ServicoBase(idServicoBase)
);

-- ---------------------------------------------------------------------
-- TB_OrdemDeServico(idOrdemDeServico<<PK>>, dataAbertura, statusOS,
--   dataEmissaoGarantia, textoExecutadoGarantia,
--   dataEmissaoResponsabilidade, quilometragem, prazoEstimado,
--   observacoes, idOrcamento<<FK>><<UNIQUE>>, idVeiculo<<FK>>,
--   idUsuario<<FK>>)
--
-- Esta é a tabela da SUPERCLASSE da hierarquia. O Mapeamento OR usou a
-- estratégia "uma tabela por classe": a superclasse tem a sua tabela e
-- cada subclasse tem a sua, compartilhando a mesma chave primária.
--
-- idOrcamento é UNIQUE: implementa a RN-02 (vínculo único) e a POS-02
-- (um orçamento gera no máximo uma OS).
--
-- [ACRESCIMO 2] coluna "numeroOS".
--   Justificativa: o atributo numeroOS : String existe na classe
--   OrdemDeServico e a RN-07 exige o formato OS-AAAA-NNNN, mas a coluna
--   não consta do Mapeamento OR. Sem ela a RN-07 não teria onde ser
--   gravada. Registrado como lacuna da modelagem.
--   Aceita NULL por causa do FA-11C: o rascunho é salvo "sem número
--   definitivo".
-- ---------------------------------------------------------------------
CREATE TABLE TB_OrdemDeServico (
    idOrdemDeServico            INT AUTO_INCREMENT PRIMARY KEY,
    numeroOS                    VARCHAR(15) UNIQUE,          -- [ACRESCIMO 2]
    dataAbertura                DATE,
    statusOS                    VARCHAR(20)  NOT NULL,
    dataEmissaoGarantia         DATE,
    textoExecutadoGarantia      VARCHAR(255),
    dataEmissaoResponsabilidade DATE,
    quilometragem               INT          NOT NULL,
    prazoEstimado               DATE         NOT NULL,
    observacoes                 VARCHAR(255),
    idOrcamento                 INT          NOT NULL UNIQUE,
    idVeiculo                   INT          NOT NULL,
    idUsuario                   INT          NOT NULL,
    CONSTRAINT fk_os_orcamento FOREIGN KEY (idOrcamento) REFERENCES TB_Orcamento(idOrcamento),
    CONSTRAINT fk_os_veiculo   FOREIGN KEY (idVeiculo)   REFERENCES TB_Veiculo(idVeiculo),
    CONSTRAINT fk_os_usuario   FOREIGN KEY (idUsuario)   REFERENCES TB_Usuario(idUsuario)
);

-- ---------------------------------------------------------------------
-- TB_OrdemDeServicoModulos(idOrdemDeServico<<PK>><<FK>>, relatoInicial,
--   condicaoRecebimento, diagnosticoInicial, testesEntradaSaida,
--   confirmacaoDefeito, causaIdentificada, reparoRealizado)
--
-- Tabela da SUBCLASSE OrdemDeServicoModulos. A chave primária é também
-- chave estrangeira para a tabela da superclasse: é assim que a herança
-- OO vira relacional nesta estratégia. Uma linha aqui só existe se
-- existir a linha correspondente em TB_OrdemDeServico.
-- ---------------------------------------------------------------------
-- As colunas aceitam NULL por causa do FA-11C: o rascunho é salvo justamente
-- quando o registro NÃO pode ser concluído, portanto sem os dados técnicos.
-- A obrigatoriedade da RN-03 é cobrada na camada de negócio, no método
-- OrdemDeServicoModulos.validar(), que só é chamado no registro definitivo.
CREATE TABLE TB_OrdemDeServicoModulos (
    idOrdemDeServico    INT PRIMARY KEY,
    relatoInicial       VARCHAR(255),
    condicaoRecebimento VARCHAR(255),
    diagnosticoInicial  VARCHAR(255),
    testesEntradaSaida  VARCHAR(255),
    confirmacaoDefeito  VARCHAR(255),
    causaIdentificada   VARCHAR(255),
    reparoRealizado     VARCHAR(255),
    CONSTRAINT fk_osmodulos_os FOREIGN KEY (idOrdemDeServico)
        REFERENCES TB_OrdemDeServico(idOrdemDeServico)
);

-- ---------------------------------------------------------------------
-- TB_OrdemDeServicoChaves(idOrdemDeServico<<PK>><<FK>>, servicoExecutado)
-- Tabela da SUBCLASSE OrdemDeServicoChaves. Mesma estratégia acima.
-- ---------------------------------------------------------------------
CREATE TABLE TB_OrdemDeServicoChaves (
    idOrdemDeServico INT PRIMARY KEY,
    servicoExecutado VARCHAR(255),   -- aceita NULL pelo mesmo motivo do FA-11C
    CONSTRAINT fk_oschaves_os FOREIGN KEY (idOrdemDeServico)
        REFERENCES TB_OrdemDeServico(idOrdemDeServico)
);

-- ---------------------------------------------------------------------
-- TB_Pagamento(idPagamento<<PK>>, formaPagamento, valor, data,
--              idOrdemDeServico<<FK>><<UNIQUE>>)
-- Consta do Mapeamento OR. Não é usada por este caso de uso; criada
-- apenas para que o banco seja fiel ao artefato.
-- ---------------------------------------------------------------------
CREATE TABLE TB_Pagamento (
    idPagamento      INT AUTO_INCREMENT PRIMARY KEY,
    formaPagamento   VARCHAR(30) NOT NULL,
    valor            DOUBLE      NOT NULL,
    data             DATE        NOT NULL,
    idOrdemDeServico INT         NOT NULL UNIQUE,
    CONSTRAINT fk_pagamento_os FOREIGN KEY (idOrdemDeServico)
        REFERENCES TB_OrdemDeServico(idOrdemDeServico)
);

-- =====================================================================
-- DADOS DE EXEMPLO
-- Reproduzem o cenário do MOCKUP-registrar-ordem-de-servico.pdf, para
-- que a tela abra já com as pré-condições PRE-02, PRE-03 e PRE-04
-- satisfeitas.
-- =====================================================================

INSERT INTO TB_Cliente (nome, telefone) VALUES
    ('Marcos Antônio Ribeiro', '(67) 99180-4402'),
    ('Oficina Center Car',     '(67) 3321-7788');

INSERT INTO TB_Veiculo (marca, modelo, anoModelo, motorizacao, placa, numeroChassi,
                        tipoModulo, modeloECU, identificacaoModulo, codigoMecanico,
                        senhaImobilizador, senhaAlarme, senhaRadio) VALUES
    ('Volkswagen', 'Gol G6', 2014, '1.6', 'QAF-7C21', '9BWAB45U8E4123456',
     'ECU - Injeção Eletrônica', 'Magneti Marelli IAW 4GV.G1', 'MM-88214-C', 'VW-4471',
     '7781', '2290', '1456'),
    ('Fiat', 'Uno Way', 2018, '1.0', 'RTB-2J45', '9BD19512TJ7654321',
     'ECU - Injeção Eletrônica', 'Delphi MT22.2', 'DP-11903-A', 'FI-2208',
     '3390', '5512', '9087');

INSERT INTO TB_ServicoBase (descricao, categoria, observacoesTecnicas) VALUES
    ('Diagnóstico em bancada com simulação de sinais',      'Módulos', 'Requer fonte simuladora de sinais'),
    ('Reparo de trilha e substituição de driver de ignição', 'Módulos', 'Reparo com retrabalho de trilha'),
    ('Regravação de EEPROM e sincronismo com imobilizador',  'Módulos', 'Exige leitura prévia da EEPROM'),
    ('Confecção de chave codificada com transponder',        'Chaves',  'Transponder conforme o imobilizador'),
    ('Programação de telecomando original',                  'Chaves',  'Requer senha do imobilizador');

INSERT INTO TB_Usuario (nome, tipoUsuario) VALUES
    ('Gabriel Abraão',  'tecnicoDeModulos'),
    ('Eduardo José',    'tecnicoDeModulos'),
    ('Jamily Sales',    'tecnicoDeChaves'),
    ('Caetano Raimundo','administrador');

-- Orçamento do mockup, aprovado e ainda não convertido.
-- O idOrcamento é informado explicitamente (231, 232, 233) porque o número
-- exibido na tela é DERIVADO do id: Orcamento.getNumeroOrcamento() monta
-- ORC-<ano da solicitação>-<id com 4 dígitos>. Com idOrcamento = 231 a tela
-- mostra "ORC-2026-0231", exatamente como o mockup.
INSERT INTO TB_Orcamento (idOrcamento, dataSolicitacao, origemAtendimento, segmentoPretendido,
                          valorEstimado, status, idCliente, idVeiculo) VALUES
    (231, '2026-08-20', 'Oficina parceira', 'Módulos', 750.00, 'Aprovado pelo cliente', 1, 1),
    (232, '2026-08-24', 'Balcão',           'Chaves',  480.00, 'Aprovado pelo cliente', 2, 2),
    (233, '2026-08-25', 'Balcão',           'Módulos', 300.00, 'Aguardando aprovação do cliente', 1, 1);

-- Itens do orçamento 231 - os três serviços do mockup (total R$ 750,00).
INSERT INTO TB_ItemOrcamento (valorProposto, quantidade, idOrcamento, idServicoBase) VALUES
    (150.00, 1, 231, 1),
    (380.00, 1, 231, 2),
    (220.00, 1, 231, 3),
    (130.00, 2, 232, 4),
    (220.00, 1, 232, 5),
    (300.00, 1, 233, 1);
