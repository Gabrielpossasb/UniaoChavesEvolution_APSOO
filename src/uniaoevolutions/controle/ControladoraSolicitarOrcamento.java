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
 * Classe <<control>> ControladoraSolicitarOrcamento do Diagrama de Classes v2
 * e objeto ":Sistema" do DSS e do Diagrama de Comunicação do caso de uso
 * Solicitar Orçamento (RF-5, 2ª iteração).
 *
 * Os seis atributos (cli, vei, tec, origemAtendimento, segmento, itens) e as
 * dezenove operações públicas são os do diagrama, com os mesmos nomes,
 * parâmetros e tipos de retorno. Cada operação corresponde a uma mensagem do
 * DSS Interface -> Sistema; registrarOrcamento é a mensagem 1 do Diagrama de
 * Comunicação, com as mensagens 1.1 a 1.15.3 na ordem do diagrama.
 *
 * Sem SQL e sem JOptionPane: regra violada sobe como IllegalArgumentException
 * (FA-09A, FA-09B, FA-12A, FA-12B, FA-12C) e falha de banco como SQLException
 * (FA-13), como na ControladoraRegistrarOS.
 */
public class ControladoraSolicitarOrcamento {

    // =================================================================
    // Constantes de negócio
    // =================================================================

    /** Valores de origemAtendimento (RF-5: "presencial ou remoto"). */
    public static final String ORIGEM_PRESENCIAL = "Presencial";
    public static final String ORIGEM_REMOTO = "Remoto";

    /**
     * FAV-8.4B: número máximo de chaves suportado pelo imobilizador. Nenhum
     * artefato modela esse dado; o valor 4 é o do mockup ("Máximo do
     * imobilizador: 4.").
     */
    public static final int QUANTIDADE_MAXIMA_CHAVES = 4;

    /** FAV-8.4A.4: procedimento acrescentado na perda total das chaves. */
    public static final String SERVICO_APAGAMENTO =
            "Apagamento e regravação de todas as chaves do imobilizador";

    // Mensagens com o texto da Descrição Expandida (pontuação do mockup).
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
    /** Passo 8.5 (V2): retorno de informarDadosChaves, com o texto do mockup. */
    public static final String MSG_COMPROVACAO_PROPRIEDADE =
            "Comprovação de propriedade obrigatória para serviços de chaves. Em caso de perda total, "
            + "informe o documento do veículo e a identificação de quem está solicitando.";
    /** FA-00.2: retorno de solicitarConfirmacaoDesistencia, com o texto do mockup. */
    public static final String MSG_CONFIRMAR_DESISTENCIA = "Desistir deste orçamento?";

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
    // ACRÉSCIMO: dados da sessão de registro
    //
    // O Contrato de Operação diz que as mensagens anteriores a
    // registrarOrcamento "guardam, na sessão de registro, os dados que o
    // Técnico vai informando". O diagrama só lista seis atributos; os dados
    // técnicos de informarDadosModulo / informarDadosChaves e das variantes
    // precisam de onde ficar até registrarOrcamento usá-los nas mensagens
    // 1.3 a 1.10. Ficam aqui, privados.
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

    // DAOs das dependências do Diagrama de Classes v2.
    private final UsuarioDAO usuarioDAO;
    private final OrcamentoDAO orcamentoDAO;
    private final ClienteDAO clienteDAO;
    private final VeiculoDAO veiculoDAO;
    private final ServicoBaseDAO servicoBaseDAO;
    private final ModuloECUDAO moduloECUDAO;
    /** ACRÉSCIMO: histórico técnico do veículo (passo 5) - ver consultarHistoricoTecnico. */
    private final OrdemServicoDAO ordemServicoDAO;

    /**
     * @param idUsuarioLogado o "idUsuarioLogado" da mensagem 1.1. Não há caso
     *                        de uso de autenticação; quem abre a tela informa
     *                        o usuário (PRE-01).
     */
    public ControladoraSolicitarOrcamento(int idUsuarioLogado) {
        this(idUsuarioLogado, new UsuarioDAO(), new OrcamentoDAO(), new ClienteDAO(), new VeiculoDAO(),
             new ServicoBaseDAO(), new ModuloECUDAO(), new OrdemServicoDAO());
    }

    /** Usado pelos testes, para observar a ordem das chamadas às DAOs. */
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

    /**
     * DSS 1: numeroOrc, dataSolicitacao = iniciarOrcamento() : Orcamento.
     * Passo 1 (RN-08): o formulário abre com o número provisório e a data.
     * PRE-01: o usuário logado é Técnico de Módulos ou de Chaves.
     */
    public Orcamento iniciarOrcamento() throws SQLException {
        limparSessao();

        Usuario usuario = usuarioDAO.buscarUsuario(idUsuarioLogado);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário logado não encontrado.");
        }
        // PRE-01 e diagrama de casos de uso: só o Técnico de Módulos e o
        // Técnico de Chaves estão ligados ao "Solicitar Orçamento"; o
        // Administrador não (e não há generalização dele para os técnicos).
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
     * DSS 3: dadosCli = informarCliente(origemAtendimento, cpfCnpjCliente) : Cliente.
     * Passos 2 e 3. Devolve null quando o cliente não tem cadastro (FA-02).
     *
     * O mockup pede "Busque por nome, CPF/CNPJ ou telefone": se o texto tem
     * 11 ou 14 dígitos e só caracteres de CPF/CNPJ, busca por CPF/CNPJ; senão,
     * por nome ou telefone.
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
            // 11 dígitos sem pontuação também podem ser um celular: (67) 99180-4402.
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

        // Outro cliente: o veículo escolhido antes deixa de valer (RN-02).
        if (encontrado == null || cli == null || encontrado.getIdCliente() != cli.getIdCliente()) {
            this.vei = null;
        }
        this.cli = encontrado;
        return encontrado;
    }

    /**
     * DSS alternativo 4: dadosCli = cadastrarCliente(nome, telefone) : Cliente.
     * FA-02.3 e FA-02.4: o cliente é cadastrado sem veículos vinculados.
     */
    public Cliente cadastrarCliente(String nome, String telefone) throws SQLException {
        if (nome == null || nome.isBlank() || telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Informe o nome e o telefone do cliente.");
        }
        Cliente novo = new Cliente(nome.trim(), telefone.trim());
        clienteDAO.salvar(novo);
        this.cli = novo;
        this.vei = null;
        return novo;
    }

    /**
     * DSS 5: dadosVeic = selecionarVeiculo(placa) : Veiculo.
     * Passos 4 e 5. Devolve null quando a placa não tem cadastro (FA-04).
     */
    public Veiculo selecionarVeiculo(String placa) throws SQLException {
        // DSS: selecionarVeiculo (5) vem depois de informarCliente (3).
        if (cli == null) {
            throw new IllegalArgumentException("Identifique o cliente antes de selecionar o veículo.");
        }
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Selecione o veículo.");
        }
        this.vei = veiculoDAO.buscarPorPlaca(placa.trim());
        return vei;
    }

    /**
     * DSS alternativo 6: dadosVeic = cadastrarVeiculo(placa, marca, modelo,
     * anoModelo, motorizacao, numeroChassi) : Veiculo.
     * FA-04.3 e FA-04.4: o veículo é cadastrado e passa a ser o veículo deste
     * orçamento - é o orçamento que o vincula ao cliente (RN-02).
     */
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

    /**
     * DSS 7: listaServ = informarSegmento(segmento) : List.
     * Passos 6 e 7 (RN-03): o segmento define o bloco técnico e os serviços
     * base. Os dados técnicos informados antes são descartados: valem os que
     * forem informados para este segmento.
     */
    public List<ServicoBase> informarSegmento(String segmento) throws SQLException {
        if (!Segmento.MODULOS.equals(segmento) && !Segmento.CHAVES.equals(segmento)) {
            throw new IllegalArgumentException("Selecione o segmento pretendido.");
        }
        // DSS: informarSegmento (7) vem depois de selecionarVeiculo (5).
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

    /**
     * DSS 9: atendAnteriores = informarDadosModulo(tipoModulo, modeloECU,
     * identificacaoModulo, sintomaRelatado) : List.
     * Passos 8.1 e 8.2 (V1, RN-04).
     */
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

    /**
     * DSS variantes 2: informarIdentificacaoIlegivel(tipoModulo, sintomaRelatado).
     * FAV-8.1.1 e FAV-8.1.2: modelo da ECU e identificação ficam dispensados.
     */
    public void informarIdentificacaoIlegivel(String tipoModulo, String sintomaRelatado) {
        exigirSegmento(Segmento.MODULOS);
        this.tipoModulo = vazioParaNulo(tipoModulo);
        this.sintomaRelatado = vazioParaNulo(sintomaRelatado);
        this.modeloECU = null;
        this.identificacaoModulo = null;
        this.identificacaoIlegivel = true;
    }

    /**
     * DSS variantes 4: atendAnteriores = informarDescricaoModulo(
     * registroFotografico, descricaoFisicaPlaca) : List.
     * FAV-8.1.3 e FAV-8.1.4 (volta ao passo 8.2). Sem a identificação o
     * módulo não pode ser reconhecido no histórico, então a lista é vazia.
     */
    public List<Orcamento> informarDescricaoModulo(String registroFotografico, String descricaoFisicaPlaca)
            throws SQLException {
        exigirSegmento(Segmento.MODULOS);
        this.registroFotografico = vazioParaNulo(registroFotografico);
        this.descricaoFisicaPlaca = vazioParaNulo(descricaoFisicaPlaca);
        return orcamentoDAO.buscarAtendimentosModulo(moduloECUDAO.buscarModulo(vei, identificacaoModulo));
    }

    /**
     * DSS variantes 6: informarDiagnosticoPendente().
     * FAV-8.2: os valores propostos passam a ser estimativos (RN-05).
     */
    public void informarDiagnosticoPendente() {
        exigirSegmento(Segmento.MODULOS);
        this.diagnosticoPendenteBancada = true;
    }

    /**
     * DSS 11 / variantes 13 e 15: resultado = informarDadosChaves(tipoChave,
     * quantidadeCopias, possuiTelecomando, codigoTransponder,
     * situacaoImobilizador, perdaTotal) : String.
     * Passos 8.4 e 8.5 (V2, RN-04). Devolve MSG_LIMITE_CHAVES quando a
     * quantidade excede o imobilizador (FAV-8.4B) e, senão, a exigência de
     * comprovação de propriedade (passo 8.5).
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

    /**
     * DSS variantes 9: servicoAcrescentado = informarComprovacaoPropriedade(
     * documentoPropriedade, nomeSolicitante, documentoSolicitante) : ServicoBase.
     * FAV-8.4A.3 e FAV-8.4A.4: devolve o procedimento de apagamento e
     * regravação, que a tela acrescenta à relação de serviços a orçar.
     */
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

    /**
     * DSS 13: valorEstimado = incluirItem(idServicoBase, valorProposto) : double.
     * Passos 9 e 10 (RN-05), com FA-09A (valor inválido) e FA-09B (serviço
     * fora do segmento, RN-03).
     */
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

    /**
     * DSS alternativo 10: valorEstimado = removerItem(idItem) : double.
     * FA-11A.3 e FA-11A.4: valor estimado recalculado (RN-05).
     */
    public double removerItem(int idItem) {
        itens.remove(buscarItem(idItem));
        return somarItens();
    }

    /**
     * DSS alternativo 12: valorEstimado = alterarValorItem(idItem,
     * novoValorProposto) : double.
     * FA-11B: valor estimado recalculado (RN-05).
     */
    public double alterarValorItem(int idItem, double novoValorProposto) {
        ItemOrcamento item = buscarItem(idItem);
        if (!(novoValorProposto > 0)) {
            throw new IllegalArgumentException(MSG_VALOR_INVALIDO);
        }
        item.setValorProposto(novoValorProposto);
        return somarItens();
    }

    /**
     * DSS 15: orc = registrarOrcamento(dataValidade, observacoes) : Orcamento.
     * Mensagem 1 do Diagrama de Comunicação e operação do Contrato de
     * Operação. As pré-condições do contrato (RN-01, RN-03, RN-04, RN-07) são
     * conferidas antes da mensagem 1.1; depois, o corpo segue as mensagens
     * 1.1 a 1.15.3 na ordem do diagrama, e por fim a pós-condição "o objeto
     * orc foi persistido".
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

        // Contrato de Operação: "O objeto orc foi persistido". Se falhar, a
        // SQLException sobe e a sessão continua intacta (FA-13.2).
        orcamentoDAO.salvar(orc);

        return orc;
    }

    /**
     * DSS alternativo 16: salvarRascunhoOrcamento(observacoes).
     * FA-12D: grava "sem número definitivo e sem contagem do prazo de
     * validade", na situação "Rascunho" (RN-06). A RN-01 e a RN-04 não são
     * cobradas: o rascunho existe justamente porque o registro não pôde ser
     * concluído. Cliente, veículo e segmento são exigidos porque o Mapeamento
     * OR os torna obrigatórios (FKs e tabela da subclasse).
     */
    public void salvarRascunhoOrcamento(String observacoes) throws SQLException {
        if (cli == null || vei == null || segmento == null) {
            throw new IllegalArgumentException(
                    "Para salvar o rascunho, identifique o cliente, o veículo e o segmento pretendido.");
        }
        // Mesma sequência de registrarOrcamento (Diagrama de Comunicação), sem
        // a 1.2 (rascunho não tem número) e com marcarComoRascunho no lugar da 1.15.

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

        // (1.7 a 1.10) só se o tipo de módulo já foi informado: no rascunho a
        // RN-04 ainda não é cobrada.
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

        // FA-12D e RN-06: no lugar da 1.15, situação "Rascunho", sem número e
        // sem prazo de validade.
        orc.marcarComoRascunho(hoje());

        orcamentoDAO.salvarRascunho(orc);
    }

    /** DSS alternativo 18: solicitarConfirmacaoDesistencia() - FA-00.2. */
    public String solicitarConfirmacaoDesistencia() {
        return MSG_CONFIRMAR_DESISTENCIA;
    }

    /**
     * DSS alternativo 20: listaOrc = descartarOrcamento() : List.
     * FA-00.3 e FA-00.4: nada é gravado; devolve a listagem de orçamentos.
     */
    public List<Orcamento> descartarOrcamento() throws SQLException {
        limparSessao();
        return orcamentoDAO.listarOrcamentos();
    }

    // =================================================================
    // ACRÉSCIMOS à modelagem: consultas cujo resultado o DSS mostra, mas
    // que não cabem no tipo de retorno declarado (ver RELATORIO_ITERACAO2.md)
    // =================================================================

    /**
     * DSS 3: "dadosCli(nome, telefone, listaVeiculos)". informarCliente
     * devolve Cliente, que não tem veículos no Diagrama de Classes v2; a
     * lista sai daqui.
     */
    public List<Veiculo> consultarVeiculosVinculados() throws SQLException {
        if (cli == null || cli.getIdCliente() == 0) {
            return new ArrayList<>();
        }
        return veiculoDAO.buscarPorCliente(cli);
    }

    /**
     * Lista de clientes cadastrados para o Técnico selecionar na busca do
     * mockup ("Busque por nome, CPF/CNPJ ou telefone"). A identificação do
     * escolhido continua sendo feita por informarCliente (DSS 3).
     */
    public List<Cliente> consultarClientes(String trecho) throws SQLException {
        return clienteDAO.buscarClientes(trecho);
    }

    /**
     * Módulos já registrados do veículo selecionado, para o Técnico
     * selecionar no bloco V1. Os dados escolhidos seguem por
     * informarDadosModulo (DSS 9).
     */
    public List<ModuloECU> consultarModulosDoVeiculo() throws SQLException {
        return moduloECUDAO.buscarModulosDoVeiculo(vei);
    }

    /**
     * DSS 5: "dadosVeic(..., historicoTecnico)" e passo 5: os atendimentos
     * anteriores do veículo, isto é, os orçamentos dele (rascunhos não são
     * atendimentos), do mais recente ao mais antigo.
     *
     * Rodada 7: antes devolvia só as Ordens de Serviço, e por isso não batia
     * com os "atendimentos anteriores do mesmo módulo registrados no histórico
     * técnico do veículo" (passo 8.2), que são orçamentos. Agora os do módulo
     * são um subconjunto destes. Usa a listarOrcamentos do diagrama.
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

    /**
     * ACRÉSCIMO (rodada 7): as Ordens de Serviço do veículo, exibidas junto do
     * histórico técnico. Reaproveita a consulta da 1ª iteração na OrdemServicoDAO.
     */
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

    /**
     * Pré-condições do Contrato de Operação de registrarOrcamento, na ordem
     * dos fluxos alternativos do passo 12: FA-12A, FA-12B e FA-12C.
     */
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

        // FA-12C / RN-07: a validade não pode ser anterior à data da solicitação.
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

    /** RN-05: somatório dos valores propostos dos itens da sessão. */
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
