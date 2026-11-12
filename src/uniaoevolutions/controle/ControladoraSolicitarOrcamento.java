package uniaoevolutions.controle;

import uniaoevolutions.negocio.Cliente;
import uniaoevolutions.negocio.ItemOrcamento;
import uniaoevolutions.negocio.ModuloECU;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.OrcamentoChaves;
import uniaoevolutions.negocio.OrcamentoModulos;
import uniaoevolutions.negocio.OrdemDeServico;
import uniaoevolutions.negocio.Segmento;
import uniaoevolutions.negocio.ServicoBase;
import uniaoevolutions.negocio.TipoUsuario;
import uniaoevolutions.negocio.Usuario;
import uniaoevolutions.negocio.Veiculo;
import uniaoevolutions.persistencia.ClienteDAO;
import uniaoevolutions.persistencia.ModuloECUDAO;
import uniaoevolutions.persistencia.OrcamentoDAO;
import uniaoevolutions.persistencia.OrdemServicoDAO;
import uniaoevolutions.persistencia.ServicoBaseDAO;
import uniaoevolutions.persistencia.UsuarioDAO;
import uniaoevolutions.persistencia.VeiculoDAO;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <<control>> do Diagrama de Classes v2 e ":Sistema" do DSS e do Diagrama de
 * Comunicação do Solicitar Orçamento (RF-5). Atributos e operações públicas são os
 * do diagrama; cada operação é uma mensagem do DSS. registrarOrcamento segue as
 * mensagens 1.1 a 1.15.3.
 * Regra violada: IllegalArgumentException. Falha de banco: SQLException (FA-13).
 */
public class ControladoraSolicitarOrcamento {

    // =================================================================
    // Constantes de negócio
    // =================================================================

    /** Valores de origemAtendimento (RF-5: "presencial ou remoto"). */
    public static final String ORIGEM_PRESENCIAL = "Presencial";
    public static final String ORIGEM_REMOTO = "Remoto";

    /** FAV-8.4B: máximo de chaves do imobilizador (valor do mockup; não está no modelo). */
    public static final int QUANTIDADE_MAXIMA_CHAVES = 4;

    /** FAV-8.4A.4: procedimento acrescentado na perda total das chaves. */
    public static final String SERVICO_APAGAMENTO =
            "Apagamento e regravação de todas as chaves do imobilizador";

    // Textos da Descrição Expandida (pontuação do mockup).
    public static final String MSG_VALOR_INVALIDO =
            "Informe um valor proposto válido para o serviço selecionado.";          // FA-09A
    public static final String MSG_FORA_DO_SEGMENTO =
            "Serviço não disponível para o segmento informado.";                     // FA-09B
    public static final String MSG_SEM_ITENS =
            "O orçamento deve conter ao menos um serviço.";                          // FA-12A
    public static final String MSG_VALIDADE_INVALIDA =
            "Informe um prazo de validade igual ou posterior à data de solicitação."; // FA-12C
    public static final String MSG_FALHA_GRAVACAO =
            "Não foi possível registrar o orçamento. Tente novamente.";              // FA-13
    public static final String MSG_RASCUNHO =
            "Orçamento salvo como rascunho";                                          // FA-12D
    public static final String MSG_LIMITE_CHAVES =
            "Limite de chaves do imobilizador atingido. Máximo permitido: "
            + QUANTIDADE_MAXIMA_CHAVES + ".";                                        // FAV-8.4B
    /** Passo 8.5 (V2): retorno de informarDadosChaves (texto do mockup). */
    public static final String MSG_COMPROVACAO_PROPRIEDADE =
            "Comprovação de propriedade obrigatória para serviços de chaves. Em caso de perda total, "
            + "informe o documento do veículo e a identificação de quem está solicitando.";
    /** FA-00.2: retorno de solicitarConfirmacaoDesistencia (texto do mockup). */
    public static final String MSG_CONFIRMAR_DESISTENCIA = "Desistir deste orçamento?";
    /** FA-14.4: recebe "APROVOU"/"RECUSOU" e o número ORC-AAAA-NNNN. */
    public static final String MSG_CONFIRMAR_DECISAO = "Confirmar que o cliente %s o orçamento %s?";
    /** FA-14.6: recebe o número ORC-AAAA-NNNN e "aprovado"/"recusado". */
    public static final String MSG_DECISAO_REGISTRADA = "Orçamento %s %s";

    // =================================================================
    // Atributos do Diagrama de Classes v2
    // =================================================================

    private Cliente cli;
    private Veiculo vei;
    private Usuario tec;
    private String origemAtendimento;
    private String segmento;
    private List<ItemOrcamento> itens = new ArrayList<>();

    // =================================================================
    // ACRÉSCIMO: dados da sessão de registro (Contrato de Operação), usados
    // nas mensagens 1.3 a 1.10. O diagrama só lista seis atributos.
    // =================================================================

    private final int idUsuarioLogado;
    private int proximoIdItem = 1;

    // V1 - Módulos
    private String tipoModulo;
    private String modeloECU;
    private String identificacaoModulo;
    private String sintomaRelatado;
    private boolean identificacaoIlegivel;
    private String registroFotografico;
    private String descricaoFisicaPlaca;
    private boolean diagnosticoPendenteBancada;

    // V2 - Chaves
    private String tipoChave;
    private int quantidadeCopias;
    private boolean possuiTelecomando;
    private String codigoTransponder;
    private String situacaoImobilizador;
    private boolean perdaTotal;
    private String documentoPropriedade;
    private String nomeSolicitante;
    private String documentoSolicitante;

    // DAOs (dependências do Diagrama de Classes v2).
    private final UsuarioDAO usuarioDAO;
    private final OrcamentoDAO orcamentoDAO;
    private final ClienteDAO clienteDAO;
    private final VeiculoDAO veiculoDAO;
    private final ServicoBaseDAO servicoBaseDAO;
    private final ModuloECUDAO moduloECUDAO;
    /** ACRÉSCIMO: consultas do histórico técnico (passo 5). */
    private final OrdemServicoDAO ordemServicoDAO;

    /** @param idUsuarioLogado o da mensagem 1.1 (sem caso de uso de autenticação; PRE-01). */
    public ControladoraSolicitarOrcamento(int idUsuarioLogado) {
        this(idUsuarioLogado, new UsuarioDAO(), new OrcamentoDAO(), new ClienteDAO(), new VeiculoDAO(),
             new ServicoBaseDAO(), new ModuloECUDAO(), new OrdemServicoDAO());
    }

    /** Para os testes observarem a ordem das chamadas às DAOs. */
    ControladoraSolicitarOrcamento(int idUsuarioLogado, UsuarioDAO usuarioDAO, OrcamentoDAO orcamentoDAO,
                                   ClienteDAO clienteDAO, VeiculoDAO veiculoDAO,
                                   ServicoBaseDAO servicoBaseDAO, ModuloECUDAO moduloECUDAO,
                                   OrdemServicoDAO ordemServicoDAO) {
        this.idUsuarioLogado = idUsuarioLogado;
        this.usuarioDAO = usuarioDAO;
        this.orcamentoDAO = orcamentoDAO;
        this.clienteDAO = clienteDAO;
        this.veiculoDAO = veiculoDAO;
        this.servicoBaseDAO = servicoBaseDAO;
        this.moduloECUDAO = moduloECUDAO;
        this.ordemServicoDAO = ordemServicoDAO;
    }

    // =================================================================
    // DSS - fluxo normal
    // =================================================================

    /** DSS 1. Passo 1 (RN-08): número provisório e data. PRE-01: só técnicos. */
    public Orcamento iniciarOrcamento() throws SQLException {
        limparSessao();

        Usuario usuario = usuarioDAO.buscarUsuario(idUsuarioLogado);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário logado não encontrado.");
        }
        // PRE-01 e casos de uso: o Administrador não é ator deste caso de uso.
        if (usuario.getTipoUsuario() != TipoUsuario.tecnicoDeModulos
                && usuario.getTipoUsuario() != TipoUsuario.tecnicoDeChaves) {
            throw new IllegalArgumentException(
                    "Somente Técnico de Módulos ou Técnico de Chaves pode registrar orçamentos.");
        }
        this.tec = usuario;

        Orcamento provisorio = new Orcamento();
        provisorio.marcarComoProvisorio(orcamentoDAO.gerarNumeroOrcamento(), hoje());
        provisorio.associarTecnico(tec);
        return provisorio;
    }

    /**
     * DSS 3. Passos 2 e 3; null se o cliente não tem cadastro (FA-02).
     * Busca por CPF/CNPJ (11 ou 14 dígitos) ou, senão, por nome ou telefone (mockup).
     */
    public Cliente informarCliente(String origemAtendimento, String cpfCnpjCliente) throws SQLException {
        if (!ORIGEM_PRESENCIAL.equals(origemAtendimento) && !ORIGEM_REMOTO.equals(origemAtendimento)) {
            throw new IllegalArgumentException("Selecione a origem do atendimento.");
        }
        if (cpfCnpjCliente == null || cpfCnpjCliente.isBlank()) {
            throw new IllegalArgumentException("Informe o cliente.");
        }
        this.origemAtendimento = origemAtendimento;

        Cliente encontrado;
        String termo = cpfCnpjCliente.trim();
        String digitos = termo.replaceAll("\\D", "");
        boolean pareceDocumento = termo.matches("[0-9./\\-]+")
                && (digitos.length() == 11 || digitos.length() == 14);
        if (pareceDocumento) {
            encontrado = clienteDAO.buscarPorCpfCnpj(digitos);
            // 11 dígitos também podem ser celular.
            if (encontrado == null && digitos.length() == 11) {
                List<Cliente> porTelefone = clienteDAO.buscarPorNomeOuTelefone(termo);
                encontrado = porTelefone.size() == 1 ? porTelefone.get(0) : null;
            }
        } else {
            List<Cliente> clientes = clienteDAO.buscarPorNomeOuTelefone(termo);
            if (clientes.size() > 1) {
                throw new IllegalArgumentException(
                        "Mais de um cliente encontrado. Informe o CPF/CNPJ ou o telefone.");
            }
            encontrado = clientes.isEmpty() ? null : clientes.get(0);
        }

        // Outro cliente: descarta o veículo (RN-02).
        if (encontrado == null || cli == null || encontrado.getIdCliente() != cli.getIdCliente()) {
            this.vei = null;
        }
        this.cli = encontrado;
        return encontrado;
    }

    /**
     * DSS alternativo 4. FA-02.3-4: cliente cadastrado sem veículos. cpfCnpj é opcional
     * (NULL se não informado) e é gravado só com os dígitos; não pode repetir (UNIQUE).
     */
    public Cliente cadastrarCliente(String nome, String cpfCnpj, String telefone) throws SQLException {
        if (nome == null || nome.isBlank() || telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Informe o nome e o telefone do cliente.");
        }
        String documento = vazioParaNulo(cpfCnpj == null ? null : cpfCnpj.replaceAll("\\D", ""));
        if (documento != null && documento.length() != 11 && documento.length() != 14) {
            throw new IllegalArgumentException("Informe o CPF (11 dígitos) ou o CNPJ (14 dígitos).");
        }
        if (documento != null && clienteDAO.buscarPorCpfCnpj(documento) != null) {
            throw new IllegalArgumentException("Já existe cliente cadastrado com o CPF/CNPJ informado.");
        }
        Cliente novo = new Cliente(nome.trim(), telefone.trim());
        novo.setCpfCnpj(documento);
        clienteDAO.salvar(novo);
        this.cli = novo;
        this.vei = null;
        return novo;
    }

    /** DSS 5. Passos 4 e 5; null se a placa não tem cadastro (FA-04). */
    public Veiculo selecionarVeiculo(String placa) throws SQLException {
        // DSS: 5 depois de 3.
        if (cli == null) {
            throw new IllegalArgumentException("Identifique o cliente antes de selecionar o veículo.");
        }
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Selecione o veículo.");
        }
        this.vei = veiculoDAO.buscarPorPlaca(placa.trim());
        return vei;
    }

    /** DSS alternativo 6. FA-04.3-4: cadastra o veículo; o orçamento o vincula ao cliente (RN-02). */
    public Veiculo cadastrarVeiculo(String placa, String marca, String modelo, int anoModelo,
                                    String motorizacao, String numeroChassi) throws SQLException {
        if (cli == null) {
            throw new IllegalArgumentException("Identifique o cliente antes de cadastrar o veículo.");
        }
        if (veiculoDAO.buscarPorPlaca(placa) != null) {
            throw new IllegalArgumentException("Já existe veículo cadastrado com a placa " + placa
                    + ". Informe a placa no campo Veículo (placa).");
        }
        Veiculo novo = new Veiculo(placa, marca, modelo, anoModelo, motorizacao, numeroChassi);
        try {
            veiculoDAO.salvar(novo);
        } catch (SQLIntegrityConstraintViolationException duplicado) {
            throw new IllegalArgumentException("Já existe veículo cadastrado com o número de chassi informado.");
        }
        this.vei = novo;
        return novo;
    }

    /** DSS 7. Passos 6 e 7 (RN-03): devolve os serviços do segmento e zera os dados técnicos. */
    public List<ServicoBase> informarSegmento(String segmento) throws SQLException {
        if (!Segmento.MODULOS.equals(segmento) && !Segmento.CHAVES.equals(segmento)) {
            throw new IllegalArgumentException("Selecione o segmento pretendido.");
        }
        // DSS: 7 depois de 5.
        if (vei == null) {
            throw new IllegalArgumentException("Selecione o veículo antes de informar o segmento.");
        }
        if (!itens.isEmpty() && !segmento.equals(this.segmento)) {
            throw new IllegalArgumentException("Remova os serviços incluídos antes de trocar o segmento.");
        }
        this.segmento = segmento;
        limparDadosTecnicos();
        return servicoBaseDAO.buscarPorCategoria(segmento);
    }

    /** DSS 9. Passos 8.1 e 8.2 (V1, RN-04). */
    public List<Orcamento> informarDadosModulo(String tipoModulo, String modeloECU,
                                               String identificacaoModulo, String sintomaRelatado)
            throws SQLException {
        exigirSegmento(Segmento.MODULOS);
        this.tipoModulo = vazioParaNulo(tipoModulo);
        this.modeloECU = vazioParaNulo(modeloECU);
        this.identificacaoModulo = vazioParaNulo(identificacaoModulo);
        this.sintomaRelatado = vazioParaNulo(sintomaRelatado);
        this.identificacaoIlegivel = false;
        this.registroFotografico = null;
        this.descricaoFisicaPlaca = null;

        ModuloECU mod = moduloECUDAO.buscarModulo(vei, this.identificacaoModulo);
        return orcamentoDAO.buscarAtendimentosModulo(mod);
    }

    /** DSS variantes 2. FAV-8.1.1-2: dispensa modelo da ECU e identificação. */
    public void informarIdentificacaoIlegivel(String tipoModulo, String sintomaRelatado) {
        exigirSegmento(Segmento.MODULOS);
        this.tipoModulo = vazioParaNulo(tipoModulo);
        this.sintomaRelatado = vazioParaNulo(sintomaRelatado);
        this.modeloECU = null;
        this.identificacaoModulo = null;
        this.identificacaoIlegivel = true;
    }

    /** DSS variantes 4. FAV-8.1.3-4: sem identificação, não há atendimentos a listar. */
    public List<Orcamento> informarDescricaoModulo(String registroFotografico, String descricaoFisicaPlaca)
            throws SQLException {
        exigirSegmento(Segmento.MODULOS);
        this.registroFotografico = vazioParaNulo(registroFotografico);
        this.descricaoFisicaPlaca = vazioParaNulo(descricaoFisicaPlaca);
        return orcamentoDAO.buscarAtendimentosModulo(moduloECUDAO.buscarModulo(vei, identificacaoModulo));
    }

    /** DSS variantes 6. FAV-8.2: valores passam a ser estimativos (RN-05). */
    public void informarDiagnosticoPendente() {
        exigirSegmento(Segmento.MODULOS);
        this.diagnosticoPendenteBancada = true;
    }

    /**
     * DSS 11; variantes 13 e 15. Passos 8.4 e 8.5 (V2, RN-04).
     * Devolve MSG_LIMITE_CHAVES (FAV-8.4B) ou a exigência de comprovação (8.5).
     */
    public String informarDadosChaves(String tipoChave, int quantidadeCopias, boolean possuiTelecomando,
                                      String codigoTransponder, String situacaoImobilizador,
                                      boolean perdaTotal) {
        exigirSegmento(Segmento.CHAVES);
        this.tipoChave = vazioParaNulo(tipoChave);
        this.quantidadeCopias = quantidadeCopias;
        this.possuiTelecomando = possuiTelecomando;
        this.codigoTransponder = vazioParaNulo(codigoTransponder);
        this.situacaoImobilizador = vazioParaNulo(situacaoImobilizador);
        this.perdaTotal = perdaTotal;
        if (!perdaTotal) {
            this.documentoPropriedade = null;
            this.nomeSolicitante = null;
            this.documentoSolicitante = null;
        }

        if (quantidadeCopias > QUANTIDADE_MAXIMA_CHAVES) {
            return MSG_LIMITE_CHAVES;
        }
        return MSG_COMPROVACAO_PROPRIEDADE;
    }

    /** DSS variantes 9. FAV-8.4A.3-4: devolve o procedimento de apagamento e regravação. */
    public ServicoBase informarComprovacaoPropriedade(String documentoPropriedade, String nomeSolicitante,
                                                      String documentoSolicitante) throws SQLException {
        exigirSegmento(Segmento.CHAVES);
        this.documentoPropriedade = vazioParaNulo(documentoPropriedade);
        this.nomeSolicitante = vazioParaNulo(nomeSolicitante);
        this.documentoSolicitante = vazioParaNulo(documentoSolicitante);

        for (ServicoBase servico : servicoBaseDAO.buscarPorCategoria(Segmento.CHAVES)) {
            if (SERVICO_APAGAMENTO.equals(servico.getDescricao())) {
                return servico;
            }
        }
        throw new IllegalArgumentException("O serviço \"" + SERVICO_APAGAMENTO
                + "\" não está cadastrado no catálogo de serviços base.");
    }

    /** DSS 13. Passos 9 e 10 (RN-05); FA-09A e FA-09B (RN-03). */
    public double incluirItem(int idServicoBase, double valorProposto) throws SQLException {
        if (segmento == null) {
            throw new IllegalArgumentException("Selecione o segmento pretendido.");
        }
        ServicoBase servico = servicoBaseDAO.buscarServico(idServicoBase);
        if (servico == null) {
            throw new IllegalArgumentException("Selecione um serviço base.");
        }
        if (!(valorProposto > 0)) {
            throw new IllegalArgumentException(MSG_VALOR_INVALIDO);                 // FA-09A
        }
        if (!segmento.equals(servico.getCategoria())) {
            throw new IllegalArgumentException(MSG_FORA_DO_SEGMENTO);               // FA-09B
        }
        ItemOrcamento item = new ItemOrcamento(servico, valorProposto);
        item.setIdItemOrcamento(proximoIdItem++);
        itens.add(item);
        return somarItens();
    }

    /** DSS alternativo 10. FA-11A.3-4 (RN-05). */
    public double removerItem(int idItem) {
        itens.remove(buscarItem(idItem));
        return somarItens();
    }

    /** DSS alternativo 12. FA-11B (RN-05). */
    public double alterarValorItem(int idItem, double novoValorProposto) {
        ItemOrcamento item = buscarItem(idItem);
        if (!(novoValorProposto > 0)) {
            throw new IllegalArgumentException(MSG_VALOR_INVALIDO);
        }
        item.setValorProposto(novoValorProposto);
        return somarItens();
    }

    /**
     * DSS 15; mensagem 1 do Diagrama de Comunicação. Pré-condições do contrato,
     * mensagens 1.1 a 1.15.3 na ordem do diagrama e a pós-condição (persistência).
     */
    public Orcamento registrarOrcamento(Date dataValidade, String observacoes) throws SQLException {
        verificarPreCondicoes(dataValidade);

        // 1.1  tec = buscarUsuario(idUsuarioLogado) : Usuario
        tec = usuarioDAO.buscarUsuario(idUsuarioLogado);

        // 1.2  numero = gerarNumeroOrcamento() : String
        String numero = orcamentoDAO.gerarNumeroOrcamento();

        Orcamento orc;
        if (Segmento.MODULOS.equals(segmento)) {
            // 1.3  [seg = "Modulos"] <<create>> OrcamentoModulos(numero, dataValidade,
            //      origemAtendimento, observacoes, sintomaRelatado, diagnosticoPendenteBancada)
            orc = new OrcamentoModulos(numero, dataValidade, origemAtendimento, vazioParaNulo(observacoes),
                    sintomaRelatado, diagnosticoPendenteBancada);
        } else {
            // 1.4  [seg = "Chaves"] <<create>> OrcamentoChaves(numero, dataValidade,
            //      origemAtendimento, observacoes, tipoChave, quantidadeCopias,
            //      possuiTelecomando, codigoTransponder, situacaoImobilizador, perdaTotal)
            orc = new OrcamentoChaves(numero, dataValidade, origemAtendimento, vazioParaNulo(observacoes),
                    tipoChave, quantidadeCopias, possuiTelecomando, codigoTransponder,
                    situacaoImobilizador, perdaTotal);
        }

        // 1.5  [identificacaoIlegivel] setModuloIlegivel(registroFotografico, descricaoFisicaPlaca)
        if (identificacaoIlegivel && orc instanceof OrcamentoModulos) {
            ((OrcamentoModulos) orc).setModuloIlegivel(registroFotografico, descricaoFisicaPlaca);
        }

        // 1.6  [perdaTotal] setComprovacaoPropriedade(documentoPropriedade,
        //      nomeSolicitante, documentoSolicitante)
        if (perdaTotal && orc instanceof OrcamentoChaves) {
            ((OrcamentoChaves) orc).setComprovacaoPropriedade(documentoPropriedade, nomeSolicitante,
                    documentoSolicitante);
        }

        if (Segmento.MODULOS.equals(segmento)) {
            // 1.7  [seg = "Modulos"] mod = buscarModulo(vei, identificacaoModulo) : ModuloECU
            ModuloECU mod = moduloECUDAO.buscarModulo(vei, identificacaoModulo);
            boolean modCriado = false;

            // 1.8  [mod == null] <<create>> ModuloECU(tipoModulo, modeloECU, identificacaoModulo)
            if (mod == null) {
                mod = new ModuloECU(tipoModulo, modeloECU, identificacaoModulo);
                modCriado = true;
            }

            // 1.9  [mod criado] vei.associarModulo(mod)
            if (modCriado) {
                vei.associarModulo(mod);
            }

            // 1.10 [seg = "Modulos"] orc.associarModulo(mod)
            ((OrcamentoModulos) orc).associarModulo(mod);
        }

        // 1.11 [para cada item em itens] adicionarItem(item)
        for (ItemOrcamento item : itens) {
            orc.adicionarItem(item);
        }

        // 1.12 associarCliente(cli)
        orc.associarCliente(cli);

        // 1.13 associarVeiculo(vei)
        orc.associarVeiculo(vei);

        // 1.14 associarTecnico(tec)
        orc.associarTecnico(tec);

        // 1.15 sinalizarRegistro() -> 1.15.1 setDataSolicitacao(dataAtual),
        //      1.15.2 setStatus("Em análise"), 1.15.3 calcularValorEstimado()
        orc.sinalizarRegistro();

        // Pós-condição: orc persistido. Se falhar, a sessão continua (FA-13.2).
        orcamentoDAO.salvar(orc);

        return orc;
    }

    /**
     * DSS alternativo 16. FA-12D: grava como "Rascunho", sem número e sem validade (RN-06).
     * Não cobra RN-01 e RN-04. Exige cliente, veículo e segmento (FKs do MOR).
     */
    public void salvarRascunhoOrcamento(String observacoes) throws SQLException {
        if (cli == null || vei == null || segmento == null) {
            throw new IllegalArgumentException(
                    "Para salvar o rascunho, identifique o cliente, o veículo e o segmento pretendido.");
        }
        // Mesma sequência de registrarOrcamento, sem a 1.2 e com marcarComoRascunho no lugar da 1.15.

        // (1.1) tec = buscarUsuario(idUsuarioLogado)
        tec = usuarioDAO.buscarUsuario(idUsuarioLogado);

        Orcamento orc;
        if (Segmento.MODULOS.equals(segmento)) {
            // (1.3) <<create>> OrcamentoModulos, sem número e sem validade
            orc = new OrcamentoModulos(null, null, origemAtendimento, vazioParaNulo(observacoes),
                    sintomaRelatado, diagnosticoPendenteBancada);
        } else {
            // (1.4) <<create>> OrcamentoChaves, sem número e sem validade
            orc = new OrcamentoChaves(null, null, origemAtendimento, vazioParaNulo(observacoes),
                    tipoChave, quantidadeCopias, possuiTelecomando, codigoTransponder,
                    situacaoImobilizador, perdaTotal);
        }

        // (1.5) [identificacaoIlegivel] setModuloIlegivel
        if (identificacaoIlegivel && orc instanceof OrcamentoModulos) {
            ((OrcamentoModulos) orc).setModuloIlegivel(registroFotografico, descricaoFisicaPlaca);
        }

        // (1.6) [perdaTotal] setComprovacaoPropriedade
        if (perdaTotal && orc instanceof OrcamentoChaves) {
            ((OrcamentoChaves) orc).setComprovacaoPropriedade(documentoPropriedade, nomeSolicitante,
                    documentoSolicitante);
        }

        // (1.7 a 1.10) só se o tipo de módulo foi informado.
        if (Segmento.MODULOS.equals(segmento) && tipoModulo != null) {
            ModuloECU mod = moduloECUDAO.buscarModulo(vei, identificacaoModulo);
            boolean modCriado = false;
            if (mod == null) {
                mod = new ModuloECU(tipoModulo, modeloECU, identificacaoModulo);
                modCriado = true;
            }
            if (modCriado) {
                vei.associarModulo(mod);
            }
            ((OrcamentoModulos) orc).associarModulo(mod);
        }

        // (1.11) [para cada item em itens] adicionarItem(item)
        for (ItemOrcamento item : itens) {
            orc.adicionarItem(item);
        }

        // (1.12), (1.13), (1.14)
        orc.associarCliente(cli);
        orc.associarVeiculo(vei);
        orc.associarTecnico(tec);

        // FA-12D e RN-06: no lugar da 1.15.
        orc.marcarComoRascunho(hoje());

        orcamentoDAO.salvarRascunho(orc);
    }

    /**
     * FA-14.5: registra a decisão do cliente (ator secundário) sobre um orçamento "Em análise".
     * Mensagens 1.1 a 1.3. Grava só a situação (sem colunas novas).
     * Regra violada (situação, decisão ou RN-07): IllegalArgumentException, nada gravado.
     */
    public Orcamento registrarDecisao(int idOrcamento, String decisao) throws SQLException {
        // 1.1  orc = buscarOrcamento(idOrcamento) : Orcamento
        Orcamento orc = orcamentoDAO.buscarOrcamento(idOrcamento);
        if (orc == null) {
            throw new IllegalArgumentException("Nenhum orçamento encontrado com o código informado.");
        }

        // 1.2  registrarDecisao(decisao) -> 1.2.1 setStatus(decisao)
        orc.registrarDecisao(decisao);

        // 1.3  atualizarStatus(idOrcamento, status)
        orcamentoDAO.atualizarStatus(orc.getIdOrcamento(), orc.getStatus());

        return orc;
    }

    /** DSS alternativo 18. FA-00.2. */
    public String solicitarConfirmacaoDesistencia() {
        return MSG_CONFIRMAR_DESISTENCIA;
    }

    /** DSS alternativo 20. FA-00.3-4: nada é gravado; devolve a listagem. */
    public List<Orcamento> descartarOrcamento() throws SQLException {
        limparSessao();
        return orcamentoDAO.listarOrcamentos();
    }

    // =================================================================
    // ACRÉSCIMOS: consultas que o DSS mostra, mas fora do retorno declarado
    // =================================================================

    /** DSS 3: a "listaVeiculos" de dadosCli (Cliente não tem veículos no diagrama). */
    public List<Veiculo> consultarVeiculosVinculados() throws SQLException {
        if (cli == null || cli.getIdCliente() == 0) {
            return new ArrayList<>();
        }
        return veiculoDAO.buscarPorCliente(cli);
    }

    /** Clientes para a lista de busca do mockup; a identificação segue por informarCliente. */
    public List<Cliente> consultarClientes(String trecho) throws SQLException {
        return clienteDAO.buscarClientes(trecho);
    }

    /** Módulos já registrados do veículo (bloco V1); a escolha segue por informarDadosModulo. */
    public List<ModuloECU> consultarModulosDoVeiculo() throws SQLException {
        return moduloECUDAO.buscarModulosDoVeiculo(vei);
    }

    /**
     * DSS 5 "historicoTecnico" e passo 5: orçamentos do veículo, sem rascunhos, do mais
     * recente ao mais antigo (rodada 7). Contém os atendimentos do módulo (8.2).
     */
    public List<Orcamento> consultarHistoricoTecnico() throws SQLException {
        List<Orcamento> atendimentos = new ArrayList<>();
        if (vei == null || vei.getIdVeiculo() == 0) {
            return atendimentos;
        }
        for (Orcamento orcamento : orcamentoDAO.listarOrcamentos()) {
            if (orcamento.getVeiculo() != null && orcamento.getVeiculo().getIdVeiculo() == vei.getIdVeiculo()
                    && !Orcamento.STATUS_RASCUNHO.equals(orcamento.getStatus())) {
                atendimentos.add(orcamento);
            }
        }
        atendimentos.sort((a, b) -> {
            int porData = compararDatas(b.getDataSolicitacao(), a.getDataSolicitacao());
            return porData != 0 ? porData : Integer.compare(b.getIdOrcamento(), a.getIdOrcamento());
        });
        return atendimentos;
    }

    /** ACRÉSCIMO (rodada 7): OS do veículo para o histórico (consulta da 1ª iteração). */
    public List<String> consultarOrdensDeServicoDoVeiculo() throws SQLException {
        List<String> linhas = new ArrayList<>();
        if (vei == null || vei.getIdVeiculo() == 0) {
            return linhas;
        }
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        for (OrdemDeServico os : ordemServicoDAO.buscarHistoricoTecnico(vei.getIdVeiculo())) {
            String data = os.getDataAbertura() == null ? "-" : formato.format(os.getDataAbertura());
            linhas.add(os.getNumeroOS() + " · " + data + " · " + os.getSegmento() + " · " + os.getStatusOS());
        }
        return linhas;
    }

    private static int compararDatas(Date a, Date b) {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : -1) : 1;
        }
        return a.compareTo(b);
    }

    // =================================================================
    // Apoio
    // =================================================================

    /** Pré-condições do contrato de registrarOrcamento: FA-12A, FA-12B e FA-12C. */
    private void verificarPreCondicoes(Date dataValidade) {
        if (itens.isEmpty()) {
            throw new IllegalArgumentException(MSG_SEM_ITENS);                       // FA-12A / RN-01
        }

        List<String> pendencias = new ArrayList<>();                                 // FA-12B / RN-04
        if (origemAtendimento == null) {
            pendencias.add("Origem do atendimento: campo obrigatório.");
        }
        if (cli == null) {
            pendencias.add("Cliente: campo obrigatório.");
        }
        if (vei == null) {
            pendencias.add("Veículo (placa): campo obrigatório.");
        }
        if (Segmento.MODULOS.equals(segmento)) {
            exigir(pendencias, tipoModulo, "Tipo de módulo", Segmento.MODULOS);
            if (identificacaoIlegivel) {
                exigir(pendencias, registroFotografico, "Registro fotográfico do módulo", Segmento.MODULOS);
                exigir(pendencias, descricaoFisicaPlaca, "Descrição física da placa", Segmento.MODULOS);
            } else {
                exigir(pendencias, modeloECU, "Modelo da ECU", Segmento.MODULOS);
            }
            exigir(pendencias, sintomaRelatado, "Sintoma relatado pelo cliente", Segmento.MODULOS);
        } else if (Segmento.CHAVES.equals(segmento)) {
            exigir(pendencias, tipoChave, "Tipo de chave", Segmento.CHAVES);
            if (quantidadeCopias <= 0) {
                pendencias.add("Qtd. de cópias: campo obrigatório para o segmento Chaves.");
            }
            exigir(pendencias, situacaoImobilizador, "Situação do imobilizador", Segmento.CHAVES);
            if (perdaTotal) {
                exigir(pendencias, documentoPropriedade, "Documento de propriedade (CRLV)", Segmento.CHAVES);
                exigir(pendencias, nomeSolicitante, "Solicitante – nome", Segmento.CHAVES);
                exigir(pendencias, documentoSolicitante, "Solicitante – documento", Segmento.CHAVES);
            }
        } else {
            pendencias.add("Segmento pretendido: campo obrigatório.");
        }
        if (!pendencias.isEmpty()) {
            StringBuilder texto = new StringBuilder("Corrija " + pendencias.size()
                    + (pendencias.size() == 1 ? " pendência" : " pendências") + " antes de registrar:");
            for (String pendencia : pendencias) {
                texto.append("\n• ").append(pendencia);
            }
            throw new IllegalArgumentException(texto.toString());
        }

        // FA-12C / RN-07: validade >= data da solicitação.
        if (dataValidade == null || inicioDoDia(dataValidade).before(hoje())) {
            throw new IllegalArgumentException(MSG_VALIDADE_INVALIDA);
        }
    }

    private void exigir(List<String> pendencias, String valor, String campo, String seg) {
        if (valor == null || valor.isBlank()) {
            pendencias.add(campo + ": campo obrigatório para o segmento " + seg + ".");
        }
    }

    private void exigirSegmento(String esperado) {
        if (!esperado.equals(segmento)) {
            throw new IllegalArgumentException("Selecione o segmento " + esperado + ".");
        }
    }

    private ItemOrcamento buscarItem(int idItem) {
        for (ItemOrcamento item : itens) {
            if (item.getIdItemOrcamento() == idItem) {
                return item;
            }
        }
        throw new IllegalArgumentException("Item não encontrado no orçamento.");
    }

    /** RN-05: soma dos valores propostos. */
    private double somarItens() {
        double total = 0;
        for (ItemOrcamento item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void limparSessao() {
        this.cli = null;
        this.vei = null;
        this.tec = null;
        this.origemAtendimento = null;
        this.segmento = null;
        this.itens = new ArrayList<>();
        this.proximoIdItem = 1;
        limparDadosTecnicos();
    }

    private void limparDadosTecnicos() {
        tipoModulo = null;
        modeloECU = null;
        identificacaoModulo = null;
        sintomaRelatado = null;
        identificacaoIlegivel = false;
        registroFotografico = null;
        descricaoFisicaPlaca = null;
        diagnosticoPendenteBancada = false;
        tipoChave = null;
        quantidadeCopias = 0;
        possuiTelecomando = false;
        codigoTransponder = null;
        situacaoImobilizador = null;
        perdaTotal = false;
        documentoPropriedade = null;
        nomeSolicitante = null;
        documentoSolicitante = null;
    }

    private static String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private static Date hoje() {
        return inicioDoDia(new Date());
    }

    private static Date inicioDoDia(Date data) {
        Calendar c = Calendar.getInstance();
        c.setTime(data);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
