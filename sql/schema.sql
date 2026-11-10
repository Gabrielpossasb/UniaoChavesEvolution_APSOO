-- =====================================================================
-- União Evolutions - Sistema de Gestão de Serviços Automotivos
-- Casos de uso: Registrar Ordem de Serviço (RF-6, 1ª iteração)
--               Solicitar Orçamento        (RF-5, 2ª iteração)
--
-- Este script é a tradução literal do artefato
--   2° Iteração Solicitar Orçamento/Design/Mapeamento_Objeto_Relacional_v2.docx
-- que substitui o Mapeamento OR da 1ª iteração. Os nomes das tabelas e das
-- colunas são exatamente os do mapeamento.
--
-- Colunas que não constam do mapeamento estão marcadas com [ACRESCIMO]:
--   [ACRESCIMO 1] TB_ItemOrcamento.quantidade  (vem da 1ª iteração)
--   [ACRESCIMO 2] TB_OrdemDeServico.numeroOS   (vem da 1ª iteração)
-- Justificativas na seção 7 do DOCUMENTACAO.md e no RELATORIO_ITERACAO2.md.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS uniao_evolutions
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE uniao_evolutions;

-- Apaga só as tabelas destes casos de uso, na ordem inversa das
-- dependências (a tabela que aponta sai antes da tabela apontada). O script
-- pode ser executado quantas vezes for preciso, e nenhuma outra tabela do
-- banco é afetada.
DROP TABLE IF EXISTS TB_Pagamento;
DROP TABLE IF EXISTS TB_OrdemDeServicoChaves;
DROP TABLE IF EXISTS TB_OrdemDeServicoModulos;
DROP TABLE IF EXISTS TB_OrdemDeServico;
DROP TABLE IF EXISTS TB_ItemOrcamento;
DROP TABLE IF EXISTS TB_OrcamentoChaves;
DROP TABLE IF EXISTS TB_OrcamentoModulos;
DROP TABLE IF EXISTS TB_Orcamento;
DROP TABLE IF EXISTS TB_Usuario;
DROP TABLE IF EXISTS TB_ServicoBase;
DROP TABLE IF EXISTS TB_ModuloECU;
DROP TABLE IF EXISTS TB_InformacoesSegurancaVeiculo;
DROP TABLE IF EXISTS TB_Veiculo;
DROP TABLE IF EXISTS TB_Cliente;

-- ---------------------------------------------------------------------
-- TB_Cliente(idCliente<<PK>>, nome, cpfCnpj<<UNIQUE>>, telefone)
-- 2ª iteração: incluída a coluna cpfCnpj.
-- cpfCnpj aceita NULL: o cadastro rápido do FA-02 pede só nome e telefone
-- (<<create>> Cliente(nome, telefone)). O UNIQUE do MySQL/MariaDB permite
-- vários NULL. O valor é gravado só com dígitos (11 = CPF, 14 = CNPJ).
-- ---------------------------------------------------------------------
CREATE TABLE TB_Cliente (
    idCliente INT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    cpfCnpj   VARCHAR(14)  UNIQUE,
    telefone  VARCHAR(20)  NOT NULL
);

-- ---------------------------------------------------------------------
-- TB_Veiculo(idVeiculo<<PK>>, marca, modelo, anoModelo, motorizacao,
--            placa<<UNIQUE>>, numeroChassi<<UNIQUE>>)
-- 2ª iteração: as colunas de módulo e de senhas saíram daqui e foram para
-- TB_ModuloECU e TB_InformacoesSegurancaVeiculo.
-- ---------------------------------------------------------------------
CREATE TABLE TB_Veiculo (
    idVeiculo    INT AUTO_INCREMENT PRIMARY KEY,
    marca        VARCHAR(50) NOT NULL,
    modelo       VARCHAR(50) NOT NULL,
    anoModelo    INT         NOT NULL,
    motorizacao  VARCHAR(20) NOT NULL,
    placa        VARCHAR(8)  NOT NULL UNIQUE,
    numeroChassi VARCHAR(17) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------
-- TB_InformacoesSegurancaVeiculo(idVeiculo<<PK>><<FK>>, codigoMecanico,
--   senhaImobilizador, senhaAlarme, senhaRadio)
-- Associação "Veiculo 1 possui 0..1 InformacoesSegurancaVeiculo".
-- ---------------------------------------------------------------------
CREATE TABLE TB_InformacoesSegurancaVeiculo (
    idVeiculo         INT PRIMARY KEY,
    codigoMecanico    VARCHAR(30),
    senhaImobilizador VARCHAR(30),
    senhaAlarme       VARCHAR(30),
    senhaRadio        VARCHAR(30),
    CONSTRAINT fk_seguranca_veiculo FOREIGN KEY (idVeiculo) REFERENCES TB_Veiculo(idVeiculo)
);

-- ---------------------------------------------------------------------
-- TB_ModuloECU(idModuloECU<<PK>>, tipoModulo, modeloECU,
--              identificacaoModulo, idVeiculo<<FK>>)
-- Associação "Veiculo 1 possui 0..* ModuloECU".
-- modeloECU e identificacaoModulo aceitam NULL: no FAV-8.1 (etiqueta
-- ilegível) os dois são dispensados.
-- ---------------------------------------------------------------------
CREATE TABLE TB_ModuloECU (
    idModuloECU         INT AUTO_INCREMENT PRIMARY KEY,
    tipoModulo          VARCHAR(60) NOT NULL,
    modeloECU           VARCHAR(60),
    identificacaoModulo VARCHAR(60),
    idVeiculo           INT         NOT NULL,
    CONSTRAINT fk_modulo_veiculo FOREIGN KEY (idVeiculo) REFERENCES TB_Veiculo(idVeiculo)
);

-- ---------------------------------------------------------------------
-- TB_ServicoBase(idServicoBase<<PK>>, descricao, categoria,
--                observacoesTecnicas)
-- Catálogo de serviços base (RF-4). A categoria é o segmento (RN-03).
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
-- ---------------------------------------------------------------------
CREATE TABLE TB_Usuario (
    idUsuario   INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    tipoUsuario ENUM('administrador', 'tecnicoDeChaves', 'tecnicoDeModulos') NOT NULL
);

-- ---------------------------------------------------------------------
-- TB_Orcamento(idOrcamento<<PK>>, numero<<UNIQUE>>, dataSolicitacao,
--   dataValidade, origemAtendimento, status, valorEstimado, observacoes,
--   motivoCancelamento, idCliente<<FK>>, idVeiculo<<FK>>, idUsuario<<FK>>)
--   status {Rascunho | Em análise | Aprovado | Recusado | Expirado | Cancelado}
--
-- Tabela da SUPERCLASSE Orcamento. 2ª iteração: incluídas numero,
-- dataValidade, observacoes, motivoCancelamento e idUsuario (técnico que
-- elaborou o orçamento); removida segmentoPretendido - o segmento passa a
-- ser dado pela tabela de subclasse em que o orçamento tem linha.
--
-- numero e dataValidade aceitam NULL por causa do FA-12D: o rascunho é
-- salvo "sem número definitivo e sem contagem do prazo de validade".
-- status é VARCHAR (e não ENUM) porque a 1ª iteração usa as situações
-- "Aprovado pelo cliente", "Convertido em OS" e "Aguardando aprovação do
-- cliente", que não constam da lista acima - ver RELATORIO_ITERACAO2.md.
-- ---------------------------------------------------------------------
CREATE TABLE TB_Orcamento (
    idOrcamento        INT AUTO_INCREMENT PRIMARY KEY,
    numero             VARCHAR(13)  UNIQUE,
    dataSolicitacao    DATE         NOT NULL,
    dataValidade       DATE,
    origemAtendimento  VARCHAR(20),
    status             VARCHAR(40)  NOT NULL,
    valorEstimado      DOUBLE       NOT NULL DEFAULT 0,
    observacoes        VARCHAR(255),
    motivoCancelamento VARCHAR(255),
    idCliente          INT          NOT NULL,
    idVeiculo          INT          NOT NULL,
    idUsuario          INT          NOT NULL,
    CONSTRAINT fk_orcamento_cliente FOREIGN KEY (idCliente) REFERENCES TB_Cliente(idCliente),
    CONSTRAINT fk_orcamento_veiculo FOREIGN KEY (idVeiculo) REFERENCES TB_Veiculo(idVeiculo),
    CONSTRAINT fk_orcamento_usuario FOREIGN KEY (idUsuario) REFERENCES TB_Usuario(idUsuario)
);

-- ---------------------------------------------------------------------
-- TB_OrcamentoModulos(idOrcamento<<PK>><<FK>>, sintomaRelatado,
--   diagnosticoPendenteBancada, identificacaoIlegivel, registroFotografico,
--   descricaoFisicaPlaca, idModuloECU<<FK>>)
--   idModuloECU - opcional (0..1)
--
-- Tabela da SUBCLASSE OrcamentoModulos: "uma tabela por subclasse", a
-- mesma estratégia de OrdemDeServico. A PK é também FK para a superclasse.
-- sintomaRelatado aceita NULL por causa do rascunho (FA-12D); a RN-04 é
-- cobrada na controladora, no registro definitivo.
-- ---------------------------------------------------------------------
CREATE TABLE TB_OrcamentoModulos (
    idOrcamento                INT PRIMARY KEY,
    sintomaRelatado            VARCHAR(255),
    diagnosticoPendenteBancada BOOLEAN NOT NULL DEFAULT FALSE,
    identificacaoIlegivel      BOOLEAN NOT NULL DEFAULT FALSE,
    registroFotografico        VARCHAR(255),
    descricaoFisicaPlaca       VARCHAR(255),
    idModuloECU                INT,
    CONSTRAINT fk_orcmodulos_orcamento FOREIGN KEY (idOrcamento) REFERENCES TB_Orcamento(idOrcamento),
    CONSTRAINT fk_orcmodulos_modulo    FOREIGN KEY (idModuloECU) REFERENCES TB_ModuloECU(idModuloECU)
);

-- ---------------------------------------------------------------------
-- TB_OrcamentoChaves(idOrcamento<<PK>><<FK>>, tipoChave, quantidadeCopias,
--   possuiTelecomando, codigoTransponder, situacaoImobilizador, perdaTotal,
--   documentoPropriedade, nomeSolicitante, documentoSolicitante)
-- Tabela da SUBCLASSE OrcamentoChaves. Mesma estratégia acima.
-- ---------------------------------------------------------------------
CREATE TABLE TB_OrcamentoChaves (
    idOrcamento          INT PRIMARY KEY,
    tipoChave            VARCHAR(60),
    quantidadeCopias     INT,
    possuiTelecomando    BOOLEAN NOT NULL DEFAULT FALSE,
    codigoTransponder    VARCHAR(40),
    situacaoImobilizador VARCHAR(60),
    perdaTotal           BOOLEAN NOT NULL DEFAULT FALSE,
    documentoPropriedade VARCHAR(60),
    nomeSolicitante      VARCHAR(100),
    documentoSolicitante VARCHAR(60),
    CONSTRAINT fk_orcchaves_orcamento FOREIGN KEY (idOrcamento) REFERENCES TB_Orcamento(idOrcamento)
);

-- ---------------------------------------------------------------------
-- TB_ItemOrcamento(idItemOrcamento<<PK>>, valorProposto,
--                  idOrcamento<<PK>><<FK>>, idServicoBase<<FK>>)
-- Composição "Orcamento 1 compõe 1..* ItemOrcamento": PK composta.
--
-- [ACRESCIMO 1] coluna "quantidade" (1ª iteração): a tela do Registrar OS
--   tem a coluna "Qtd." e incluirItemOrdemServico recebe a quantidade. Os
--   itens do Solicitar Orçamento são gravados com o DEFAULT 1.
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
-- Inalterada em relação à 1ª iteração.
--
-- [ACRESCIMO 2] coluna "numeroOS" (1ª iteração): exigida pela RN-07 do
--   Registrar OS (formato OS-AAAA-NNNN). Aceita NULL por causa do rascunho.
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

-- TB_OrdemDeServicoModulos(idOrdemDeServico<<PK>><<FK>>, relatoInicial, ...)
-- Tabela da SUBCLASSE OrdemDeServicoModulos (1ª iteração, inalterada).
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

-- TB_OrdemDeServicoChaves(idOrdemDeServico<<PK>><<FK>>, servicoExecutado)
-- Tabela da SUBCLASSE OrdemDeServicoChaves (1ª iteração, inalterada).
CREATE TABLE TB_OrdemDeServicoChaves (
    idOrdemDeServico INT PRIMARY KEY,
    servicoExecutado VARCHAR(255),
    CONSTRAINT fk_oschaves_os FOREIGN KEY (idOrdemDeServico)
        REFERENCES TB_OrdemDeServico(idOrdemDeServico)
);

-- TB_Pagamento(idPagamento<<PK>>, formaPagamento, valor, data,
--              idOrdemDeServico<<FK>><<UNIQUE>>)
-- Consta do Mapeamento OR; não é usada pelos dois casos de uso.
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
-- Prontos para testar só selecionando: cada cliente tem veículo, módulo,
-- informações de segurança e orçamentos anteriores (é o orçamento que liga
-- o veículo ao cliente - RN-02). Inclui os clientes e veículos dos dois
-- mockups. Os orçamentos 231, 232 e 233 são os da 1ª iteração e não mudaram;
-- os demais têm números menores que 0231, então o próximo número continua
-- ORC-2026-0234, e situações que não aparecem no Registrar OS.
--
-- Os CPFs do MOCKUP-solicitar-orcamento.pdf (024.577.310-45 e
-- 311.904.227-80) têm dígitos verificadores inválidos; aqui foram usados
-- os mesmos nove primeiros dígitos com os verificadores corretos.
-- =====================================================================

INSERT INTO TB_Cliente (nome, cpfCnpj, telefone) VALUES
    ('Marcos Antônio Ribeiro',   '02457731003',    '(67) 99180-4402'),   -- 1 mockup V1
    ('Oficina Center Car',       '11222333000181', '(67) 3321-7788'),    -- 2 1ª iteração
    ('Fernanda Lopes Barbosa',   '31190422778',    '(67) 98122-6730'),   -- 3 mockup V2
    ('Ana Paula Moreira',        '45812033684',    '(67) 99234-1188'),   -- 4
    ('Auto Elétrica Pantanal',   '27411065000186', '(67) 3384-2210'),    -- 5
    ('João Victor Albuquerque',  '70255391803',    '(67) 99607-5521');

INSERT INTO TB_Veiculo (marca, modelo, anoModelo, motorizacao, placa, numeroChassi) VALUES
    ('Volkswagen', 'Gol G6',       2014, '1.6 MSI',    'QAF-7C21', '9BWAB45U1ET043219'),  -- 1 Marcos
    ('Fiat',       'Uno Way',      2018, '1.0',        'RTB-2J45', '9BD19512TJ7654321'),  -- 2 Center Car
    ('Fiat',       'Argo Drive',   2021, '1.0 Firefly','RCD-2H48', '9BD358A1NMYK11872'),  -- 3 Fernanda
    ('Chevrolet',  'Onix LT',      2019, '1.0 Turbo',  'HSK-4B19', '9BGKS48U0KG112233'),  -- 4 Ana Paula
    ('Toyota',     'Corolla XEi',  2017, '2.0 Flex',   'NRX-3E77', '9BRBL3HE8H0145566'),  -- 5 Pantanal
    ('Hyundai',    'HB20 Comfort', 2020, '1.6',        'PQT-8D12', '9BHBG51CALP778899');

INSERT INTO TB_InformacoesSegurancaVeiculo (idVeiculo, codigoMecanico, senhaImobilizador,
                                            senhaAlarme, senhaRadio) VALUES
    (1, 'VW-4471', '7781', '2290', '1456'),
    (2, 'FI-2208', '3390', '5512', '9087'),
    (3, 'FI-9935', '4402', '1188', '3021'),
    (4, 'GM-1270', '6618', '7004', '5590'),
    (5, 'TY-0381', '2245', '8810', '1203'),
    (6, 'HY-5524', '9073', '3366', '4417');

INSERT INTO TB_ModuloECU (tipoModulo, modeloECU, identificacaoModulo, idVeiculo) VALUES
    ('ECU – Injeção Eletrônica',   'Magneti Marelli IAW 4GV.G1', 'MM-88214-C', 1),  -- 1
    ('ECU – Injeção Eletrônica',   'Delphi MT22.2',              'DP-11903-A', 2),  -- 2
    ('BCM – Módulo de Carroceria', 'Continental BSI 6R0937',     'BC-55120-A', 1),  -- 3
    ('ECU – Injeção Eletrônica',   'Delco E78',                  'DL-30077-B', 4),  -- 4
    ('TCU – Câmbio Automático',    'Aisin TC-8956',              'AS-89661-K', 5),  -- 5
    ('Painel de Instrumentos',     'Bosch 0263',                 'BP-71542-D', 6);

-- PRE-02: ao menos um serviço base em cada segmento. Os três últimos são os
-- serviços de chaves que aparecem no MOCKUP-solicitar-orcamento.pdf; o
-- último é o procedimento acrescentado na perda total (FAV-8.4A.4).
INSERT INTO TB_ServicoBase (descricao, categoria, observacoesTecnicas) VALUES
    ('Diagnóstico em bancada com simulação de sinais',      'Módulos', 'Requer fonte simuladora de sinais'),
    ('Reparo de trilha e substituição de driver de ignição', 'Módulos', 'Reparo com retrabalho de trilha'),
    ('Regravação de EEPROM e sincronismo com imobilizador',  'Módulos', 'Exige leitura prévia da EEPROM'),
    ('Confecção de chave codificada com transponder',        'Chaves',  'Transponder conforme o imobilizador'),
    ('Programação de telecomando original',                  'Chaves',  'Requer senha do imobilizador'),
    ('Confecção de chave canivete com transponder',          'Chaves',  'Transponder conforme o imobilizador'),
    ('Cópia de chave com telecomando',                       'Chaves',  'Requer chave original funcional'),
    ('Apagamento e regravação de todas as chaves do imobilizador', 'Chaves',
     'Obrigatório na perda total das chaves (FAV-8.4A)');

INSERT INTO TB_Usuario (nome, tipoUsuario) VALUES
    ('Gabriel Abraão',  'tecnicoDeModulos'),
    ('Eduardo José',    'tecnicoDeModulos'),
    ('Jamily Sales',    'tecnicoDeChaves'),
    ('Caetano Raimundo','administrador');
-- 187 são de 2025 e viraram as três OS anteriores do QAF-7C21 ("Ver 3
-- atendimentos anteriores", como no mockup). Os demais ligam os outros
-- veículos aos seus clientes.
INSERT INTO TB_Orcamento (idOrcamento, numero, dataSolicitacao, dataValidade, origemAtendimento,
                          status, valorEstimado, observacoes, idCliente, idVeiculo, idUsuario) VALUES
    (118, 'ORC-2025-0118', '2025-03-11', '2025-03-26', 'Presencial', 'Convertido em OS',                180.00, NULL, 1, 1, 1),
    (142, 'ORC-2025-0142', '2025-06-02', '2025-06-17', 'Presencial', 'Convertido em OS',                260.00, NULL, 1, 1, 2),
    (187, 'ORC-2025-0187', '2025-10-20', '2025-11-04', 'Remoto',     'Convertido em OS',                150.00, NULL, 1, 1, 1),
    (205, 'ORC-2026-0205', '2026-07-14', '2026-07-29', 'Presencial', 'Recusado',                        310.00, NULL, 3, 3, 3),
    (212, 'ORC-2026-0212', '2026-07-30', '2026-08-14', 'Remoto',     'Expirado',                        420.00, NULL, 4, 4, 1),
    (220, 'ORC-2026-0220', '2026-08-05', '2026-08-20', 'Presencial', 'Em análise',                      520.00, NULL, 5, 5, 2),
    (224, 'ORC-2026-0224', '2026-08-12', '2026-08-27', 'Presencial', 'Recusado',                        180.00, NULL, 5, 6, 3),
    (231, 'ORC-2026-0231', '2026-08-20', '2026-09-04', 'Presencial', 'Aprovado pelo cliente',           750.00, NULL, 1, 1, 1),
    (232, 'ORC-2026-0232', '2026-08-24', '2026-09-08', 'Presencial', 'Aprovado pelo cliente',           480.00, NULL, 2, 2, 3),
    (233, 'ORC-2026-0233', '2026-08-25', '2026-09-09', 'Remoto',     'Aguardando aprovação do cliente', 300.00, NULL, 1, 1, 2);

INSERT INTO TB_OrcamentoModulos (idOrcamento, sintomaRelatado, diagnosticoPendenteBancada,
                                 identificacaoIlegivel, idModuloECU) VALUES
    (118, 'Travas elétricas não respondem ao controle.',                     FALSE, FALSE, 3),
    (142, 'Luzes internas permanecem acesas com o veículo desligado.',       FALSE, FALSE, 3),
    (187, 'Alarme dispara sozinho durante a noite.',                         TRUE,  FALSE, 3),
    (212, 'Marcha lenta oscilando e consumo alto.',                          TRUE,  FALSE, 4),
    (220, 'Trancos na troca de marcha.',                                     TRUE,  FALSE, 5),
    (231, 'Veículo não dá partida a frio; luz de injeção acesa no painel.',  TRUE,  FALSE, 1),
    (233, 'Falha intermitente de marcha lenta.',                             FALSE, FALSE, 1);

INSERT INTO TB_OrcamentoChaves (idOrcamento, tipoChave, quantidadeCopias, possuiTelecomando,
                                codigoTransponder, situacaoImobilizador, perdaTotal) VALUES
    (205, 'Canivete com transponder', 1, TRUE,  'ID46 – PCF7946', 'Funcionando – com chave válida', FALSE),
    (224, 'Chave com transponder',    2, FALSE, 'ID4D',           'Funcionando – com chave válida', FALSE),
    (232, 'Chave com transponder',    2, TRUE,  'ID48',           'Funcionando – com chave válida', FALSE);
-- serviços do mockup (total R$ 750,00).
INSERT INTO TB_ItemOrcamento (valorProposto, quantidade, idOrcamento, idServicoBase) VALUES
    (180.00, 1, 118, 1),
    (260.00, 1, 142, 2),
    (150.00, 1, 187, 1),
    (310.00, 1, 205, 6),
    (420.00, 1, 212, 3),
    (150.00, 1, 220, 1),
    (370.00, 1, 220, 2),
    (180.00, 1, 224, 7),
    (150.00, 1, 231, 1),
    (380.00, 1, 231, 2),
    (220.00, 1, 231, 3),
    (130.00, 2, 232, 4),
    (220.00, 1, 232, 5),
    (300.00, 1, 233, 1);
-- de 2025: não interfere no OS-2026-NNNN do Registrar OS.
INSERT INTO TB_OrdemDeServico (numeroOS, dataAbertura, statusOS, quilometragem, prazoEstimado,
                               observacoes, idOrcamento, idVeiculo, idUsuario) VALUES
    ('OS-2025-0031', '2025-03-13', 'Concluída', 121400, '2025-03-18', NULL, 118, 1, 1),
    ('OS-2025-0067', '2025-06-04', 'Concluída', 128950, '2025-06-09', NULL, 142, 1, 2),
    ('OS-2025-0112', '2025-10-22', 'Concluída', 136210, '2025-10-27', NULL, 187, 1, 1);

INSERT INTO TB_OrdemDeServicoModulos (idOrdemDeServico, relatoInicial, diagnosticoInicial) VALUES
    (1, 'Travas elétricas não respondem ao controle.', 'Relé de travamento do BCM em curto.'),
    (2, 'Luzes internas permanecem acesas.',           'Saída do BCM sem desligamento.'),
    (3, 'Alarme dispara sozinho.',                     'Sensor de porta com sinal intermitente.');
