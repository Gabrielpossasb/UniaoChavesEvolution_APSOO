package uniaoevolutions.controle;

import uniaoevolutions.negocio.ItemOrcamento;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.OrdemDeServico;
import uniaoevolutions.negocio.OrdemDeServicoChaves;
import uniaoevolutions.negocio.OrdemDeServicoModulos;
import uniaoevolutions.negocio.Segmento;
import uniaoevolutions.negocio.ServicoBase;
import uniaoevolutions.negocio.Usuario;
import uniaoevolutions.negocio.Veiculo;
import uniaoevolutions.persistencia.OrcamentoDAO;
import uniaoevolutions.persistencia.OrdemServicoDAO;
import uniaoevolutions.persistencia.UsuarioDAO;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe <<control>> do Diagrama de Classes e objeto ":Sistema" do Diagrama de
 * Comunicação: coordena o caso de uso entre a tela e as DAOs.
 *
 * Sem SQL e sem JOptionPane - erros sobem como exceção para a tela tratar.
 */
public class ControladoraRegistrarOS {

    private final OrcamentoDAO orcamentoDAO = new OrcamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    /** Ordem de Serviço que está sendo montada no formulário. */
    private OrdemDeServico osEmRegistro;
    /** Item pendente de justificativa, usado pelo FA-11B. */
    private ItemOrcamento itemPendente;
    /** Orçamento a que o item pendente pertence (FA-11B). */
    private Orcamento orcamentoDoItemPendente;

    // =================================================================
    // Passo 1 do fluxo principal
    // =================================================================

    /**
     * Passo 1 (RN-07): reserva o número OS-AAAA-NNNN e marca a data de hoje.
     *
     * O segmento ainda é desconhecido, então o rascunho nasce como
     * OrdemDeServicoModulos (segmento pré-selecionado no mockup); a subclasse
     * definitiva é escolhida em registrarOrdemServico().
     */
    public OrdemDeServico iniciarRegistroOS() throws SQLException {
        OrdemDeServico os = new OrdemDeServicoModulos();
        os.setNumeroOS(ordemServicoDAO.gerarNumeroOS());
        os.marcarComoRascunho(new Date());
        this.osEmRegistro = os;
        this.itemPendente = null;
        this.orcamentoDoItemPendente = null;
        return os;
    }

    // =================================================================
    // Consultas (passos 2, 4, 6 e 9 do fluxo principal)
    // =================================================================

    /** Passo 2 (RN-01): orçamentos aprovados disponíveis. Lista vazia = FA-02. */
    public List<Orcamento> consultarOrcamentosAprovados() throws SQLException {
        return orcamentoDAO.buscarOrcamentosAprovados();
    }

    /** Passo 4 (RN-02): dados de cliente, veículo, segmento e itens do orçamento. */
    public Orcamento consultarOrcamento(int idOrcamento) throws SQLException {
        Orcamento orcamento = orcamentoDAO.buscarOrcamento(idOrcamento);
        if (orcamento == null) {
            throw new IllegalArgumentException("Nenhum orçamento encontrado com o código informado.");
        }
        return orcamento;
    }

    /** Passo 6: campos exigidos pela RN-03 no segmento (variantes V1 e V2). */
    public List<String> consultarBlocosDadosTecnicos(String segmento) {
        List<String> campos = new ArrayList<>();
        if (Segmento.CHAVES.equals(segmento)) {
            campos.add("Serviço de chaves a executar");
            campos.add("Código mecânico do veículo");
            campos.add("Situação do imobilizador");
        } else {
            campos.add("Tipo de módulo");
            campos.add("Fabricante / Modelo da ECU");
            campos.add("Nº da peça / Nº de série");
            campos.add("Sintomas relatados pelo cliente");
            campos.add("Condição de recebimento");
            campos.add("Diagnóstico em bancada");
        }
        return campos;
    }

    /** Passo 9 (RN-04): técnicos habilitados no segmento. Lista vazia = FA-09. */
    public List<Usuario> consultarTecnicosHabilitados(String segmento) throws SQLException {
        return usuarioDAO.buscarTecnicosHabilitados(segmento);
    }

    /**
     * Passo 8.2 (V1) e botão "Ver atendimentos anteriores" do mockup.
     * ACRÉSCIMO à modelagem - ver DOCUMENTACAO.md.
     */
    public List<String> consultarHistoricoTecnico(int idVeiculo) throws SQLException {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        List<String> linhas = new ArrayList<>();
        for (OrdemDeServico os : ordemServicoDAO.buscarHistoricoTecnico(idVeiculo)) {
            String data = os.getDataAbertura() == null ? "-" : formato.format(os.getDataAbertura());
            linhas.add(os.getNumeroOS() + "  -  " + data
                     + "  -  " + os.getSegmento()
                     + "  -  " + os.getQuilometragem() + " km"
                     + "  -  " + os.getStatusOS());
        }
        return linhas;
    }

    // =================================================================
    // Operação principal - passo 12 do fluxo principal
    // =================================================================

    /**
     * Mensagem 1 do Diagrama de Comunicação e operação do Contrato de Operação.
     * O corpo segue a ordem das mensagens 1.1 a 1.11, marcadas nos comentários.
     *
     * @return a Ordem de Serviço registrada (POS-01).
     */
    public OrdemDeServico registrarOrdemServico(int orcamentoSelecionado,
                                                int tecnicoSelecionado,
                                                int quilometragem,
                                                Date prazoEstimado,
                                                String observacoes,
                                                String relatoInicial,
                                                String condicaoRecebimento,
                                                String diagnosticoInicial,
                                                String servicoExecutado) throws SQLException {

        // 1.1  orc = buscarOrcamento(orcamentoSelecionado) : Orcamento
        Orcamento orc = orcamentoDAO.buscarOrcamento(orcamentoSelecionado);
        if (orc == null) {
            throw new IllegalArgumentException("Selecione o orçamento que originará a Ordem de Serviço.");
        }
        // RN-01: a origem tem de estar aprovada e ainda não convertida.
        if (!orc.podeGerarOrdemDeServico()) {
            throw new IllegalArgumentException(
                    "O orçamento " + orc.getNumeroOrcamento() + " não está aprovado pelo cliente.");
        }

        // 1.2  vei = getVeiculo() : Veiculo
        Veiculo vei = orc.getVeiculo();

        // 1.3  tec = buscarTecnico(tecnicoSelecionado) : Tecnico
        Usuario tec = usuarioDAO.buscarUsuario(tecnicoSelecionado);
        if (tec == null) {
            throw new IllegalArgumentException("Selecione o técnico responsável pela Ordem de Serviço.");
        }

        // 1.4  seg = getSegmentoPretendido() : String
        String seg = orc.getSegmentoPretendido();

        // 1.5 / 1.6  <<create>> da subclasse correspondente ao segmento
        OrdemDeServico os = criarOrdemDeServico(seg, quilometragem, prazoEstimado, observacoes,
                relatoInicial, condicaoRecebimento, diagnosticoInicial, servicoExecutado);

        // 1.8 / 1.9 / 1.10  formação dos vínculos (RN-02 e POS-04)
        os.associarOrcamento(orc);
        os.associarVeiculo(vei);
        os.associarTecnico(tec);

        // RN-03 e RN-04, antes de qualquer acesso ao banco. A exceção sobe
        // para a tela, que trata o FA-11A.
        os.validar();

        // 1.7  sinalizarAbertura() -> 1.7.1 setDataAbertura, 1.7.2 setStatusOs("Aberta")
        os.sinalizarAbertura();

        // RN-07: número definitivo, reservado quando o formulário abriu.
        os.setNumeroOS(numeroReservado());

        // 1.11  salvar(os)
        ordemServicoDAO.salvar(os);

        // POS-02: o orçamento de origem passa para "Convertido em OS".
        orcamentoDAO.atualizarStatus(orc.getIdOrcamento(), Orcamento.STATUS_CONVERTIDO);

        this.osEmRegistro = os;
        return os;
    }

    /**
     * Mensagens 1.5 e 1.6, com as guardas [seg = "Módulos"] e [seg = "Chaves"]:
     * cria a OS na subclasse correspondente ao segmento.
     */
    private OrdemDeServico criarOrdemDeServico(String seg, int quilometragem, Date prazoEstimado,
                                               String observacoes, String relatoInicial,
                                               String condicaoRecebimento, String diagnosticoInicial,
                                               String servicoExecutado) {
        if (Segmento.CHAVES.equals(seg)) {
            return new OrdemDeServicoChaves(quilometragem, prazoEstimado, observacoes, servicoExecutado);
        }
        return new OrdemDeServicoModulos(quilometragem, prazoEstimado, observacoes,
                relatoInicial, condicaoRecebimento, diagnosticoInicial);
    }

    /** Número reservado no passo 1; se por algum motivo não houver, gera na hora. */
    private String numeroReservado() throws SQLException {
        if (osEmRegistro != null && osEmRegistro.getNumeroOS() != null) {
            return osEmRegistro.getNumeroOS();
        }
        return ordemServicoDAO.gerarNumeroOS();
    }

    // =================================================================
    // FA-11B - inclusão de item não previsto no orçamento aprovado
    // =================================================================

    /**
     * FA-11B.1 e FA-11B.2: recebe o item a incluir e exige a justificativa.
     * O item fica pendente até registrarOSComAlteracao() (RN-05).
     *
     * @return a mensagem a ser exibida ao Administrador.
     */
    public String incluirItemOrdemServico(String servicoOuPeca, int quantidade, double valor) {
        if (servicoOuPeca == null || servicoOuPeca.isBlank()) {
            throw new IllegalArgumentException("Informe o serviço ou a peça a incluir.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade maior que zero.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("Informe um valor maior que zero.");
        }
        if (osEmRegistro == null || osEmRegistro.getOrcamento() == null) {
            throw new IllegalArgumentException("Selecione antes o orçamento que originará a Ordem de Serviço.");
        }

        ItemOrcamento item = new ItemOrcamento();
        item.setQuantidade(quantidade);
        item.setValorProposto(valor);
        item.setServicoBase(new ServicoBase(
                0, servicoOuPeca, osEmRegistro.getSegmento(), null));

        this.itemPendente = item;
        this.orcamentoDoItemPendente = osEmRegistro.getOrcamento();

        return "A inclusão de \"" + servicoOuPeca + "\" altera o orçamento aprovado.\n"
             + "Informe a justificativa da alteração (RN-05).";
    }

    /**
     * FA-11B.3 e FA-11B.4: grava a alteração e devolve o novo valor total.
     *
     * RN-05: o orçamento volta para "Aguardando aprovação do cliente" e o caso
     * de uso é encerrado sem registrar a Ordem de Serviço (FA-11B.5).
     *
     * @return a mensagem a ser exibida ao Administrador.
     */
    public String registrarOSComAlteracao(String servicoOuPeca, int quantidade, double valor,
                                          String justificativa) throws SQLException {
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("Informe a justificativa da alteração (RN-05).");
        }
        if (itemPendente == null || orcamentoDoItemPendente == null) {
            throw new IllegalArgumentException("Nenhuma alteração de item pendente de justificativa.");
        }

        Orcamento orcamento = orcamentoDoItemPendente;

        orcamentoDAO.incluirItem(orcamento.getIdOrcamento(), servicoOuPeca,
                orcamento.getSegmentoPretendido(), quantidade, valor, justificativa);

        // RN-05: execução suspensa até nova aprovação do cliente.
        orcamentoDAO.atualizarStatus(orcamento.getIdOrcamento(), Orcamento.STATUS_AGUARDANDO);

        double novoTotal = orcamento.getValorTotal() + (quantidade * valor);
        this.itemPendente = null;
        this.orcamentoDoItemPendente = null;

        return String.format(
                "Novo valor total do orçamento %s: R$ %.2f%n%n"
              + "A execução fica suspensa até nova aprovação do cliente.%n"
              + "O orçamento voltou para a situação \"%s\" e a Ordem de Serviço não foi registrada.",
                orcamento.getNumeroOrcamento(), novoTotal, Orcamento.STATUS_AGUARDANDO);
    }

    // =================================================================
    // FA-11C - salvar como rascunho
    // =================================================================

    /**
     * FA-11C: grava a OS "sem número definitivo e sem alteração da situação do
     * orçamento".
     *
     * Os dados técnicos não são parâmetros: a RN-03 não é cobrada no rascunho,
     * só o mínimo que o banco exige para guardar a linha.
     */
    public void salvarRascunhoOS(int orcamentoSelecionado, int tecnicoSelecionado, int quilometragem,
                                 Date prazoEstimado, String observacoes) throws SQLException {

        Orcamento orc = orcamentoDAO.buscarOrcamento(orcamentoSelecionado);
        if (orc == null) {
            throw new IllegalArgumentException("Selecione o orçamento que originará a Ordem de Serviço.");
        }
        Usuario tec = usuarioDAO.buscarUsuario(tecnicoSelecionado);
        if (tec == null) {
            throw new IllegalArgumentException("Selecione o técnico responsável pela Ordem de Serviço.");
        }
        if (quilometragem <= 0) {
            throw new IllegalArgumentException("Informe a quilometragem atual do veículo.");
        }
        if (prazoEstimado == null) {
            throw new IllegalArgumentException("Informe o prazo estimado de conclusão.");
        }

        OrdemDeServico os = criarOrdemDeServico(orc.getSegmentoPretendido(), quilometragem,
                prazoEstimado, observacoes, null, null, null, null);
        os.associarOrcamento(orc);
        os.associarVeiculo(orc.getVeiculo());
        os.associarTecnico(tec);

        // "sem número definitivo": numeroOS nulo e situação "Rascunho".
        os.marcarComoRascunho(new Date());
        ordemServicoDAO.salvar(os);

        // Sem atualizarStatus: é a diferença para o fluxo principal.
        this.osEmRegistro = os;
    }

    // =================================================================
    // FA-00 - desistência do Administrador
    // =================================================================

    /** FA-00.2: solicitação de confirmação da desistência. */
    public String solicitarConfirmacaoDesistencia() {
        return "Confirma a desistência do registro da Ordem de Serviço?";
    }

    /**
     * FA-00.3 e FA-00.4: descarta o registro em andamento "mantendo o orçamento
     * em sua situação original" - nada foi gravado, basta soltar os objetos.
     */
    public void descartarRegistro() {
        this.osEmRegistro = null;
        this.itemPendente = null;
        this.orcamentoDoItemPendente = null;
    }

    /** Ordem de Serviço que está sendo montada, para a tela consultar. */
    public OrdemDeServico getOsEmRegistro() {
        return osEmRegistro;
    }

    /**
     * Passo 3: guarda o orçamento escolhido e forma desde já os vínculos com
     * ele e com o seu veículo (RN-02).
     *
     * Se o segmento mudar, o rascunho é recriado na subclasse correta,
     * preservando o número e a data reservados no passo 1.
     */
    public void selecionarOrcamento(Orcamento orcamento) {
        if (osEmRegistro == null || orcamento == null) {
            return;
        }
        if (!osEmRegistro.getSegmento().equals(orcamento.getSegmentoPretendido())) {
            OrdemDeServico novoRascunho = criarOrdemDeServico(orcamento.getSegmentoPretendido(),
                    0, null, null, null, null, null, null);
            novoRascunho.setNumeroOS(osEmRegistro.getNumeroOS());
            novoRascunho.marcarComoRascunho(osEmRegistro.getDataAbertura());
            this.osEmRegistro = novoRascunho;
        }
        osEmRegistro.associarOrcamento(orcamento);
        osEmRegistro.associarVeiculo(orcamento.getVeiculo());
    }
}
