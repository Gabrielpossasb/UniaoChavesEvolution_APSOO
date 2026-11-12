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
 * <<control>> do Diagrama de Classes e ":Sistema" do Diagrama de Comunicação.
 * Sem SQL e sem JOptionPane: erros sobem como exceção.
 */
public class ControladoraRegistrarOS {

    private final OrcamentoDAO orcamentoDAO = new OrcamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    /** OS em montagem no formulário. */
    private OrdemDeServico osEmRegistro;
    /** FA-11B: item pendente de justificativa. */
    private ItemOrcamento itemPendente;
    /** FA-11B: orçamento do item pendente. */
    private Orcamento orcamentoDoItemPendente;

    // =================================================================
    // Passo 1 do fluxo principal
    // =================================================================

    /**
     * Passo 1 (RN-07): reserva o número e a data. Nasce como OrdemDeServicoModulos
     * (pré-seleção do mockup); a subclasse final sai em registrarOrdemServico().
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

    /** Passo 2 (RN-01). Lista vazia: FA-02. */
    public List<Orcamento> consultarOrcamentosAprovados() throws SQLException {
        return orcamentoDAO.buscarOrcamentosAprovados();
    }

    /** Passo 4 (RN-02): orçamento com cliente, veículo e itens. */
    public Orcamento consultarOrcamento(int idOrcamento) throws SQLException {
        Orcamento orcamento = orcamentoDAO.buscarOrcamento(idOrcamento);
        if (orcamento == null) {
            throw new IllegalArgumentException("Nenhum orçamento encontrado com o código informado.");
        }
        return orcamento;
    }

    /** Passo 6: campos exigidos pela RN-03 no segmento. */
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

    /** Passo 9 (RN-04). Lista vazia: FA-09. */
    public List<Usuario> consultarTecnicosHabilitados(String segmento) throws SQLException {
        return usuarioDAO.buscarTecnicosHabilitados(segmento);
    }

    // =================================================================
    // Operação principal - passo 12 do fluxo principal
    // =================================================================

    /**
     * Mensagem 1 do Diagrama de Comunicação (corrigido): ordem das mensagens 1.1 a 1.11.
     * Nomes do Diagrama de Classes: 1.2 orc.getVeiculo(), 1.3 usuarioDAO.buscarUsuario(),
     * 1.8-1.10 associar*() da OrdemDeServico.
     *
     * @return a OS registrada (POS-01).
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
        // RN-01: aprovado e não convertido.
        if (!orc.podeGerarOrdemDeServico()) {
            throw new IllegalArgumentException(
                    "O orçamento " + orc.getNumeroOrcamento() + " não está aprovado pelo cliente.");
        }

        // 1.2  vei = buscarVeiculo(orc) : Veiculo  -> Orcamento.getVeiculo() do Diagrama de Classes
        Veiculo vei = orc.getVeiculo();

        // 1.3  tec = buscarTecnico(tecnicoSelecionado) : Tecnico  -> UsuarioDAO.buscarUsuario
        Usuario tec = usuarioDAO.buscarUsuario(tecnicoSelecionado);
        if (tec == null) {
            throw new IllegalArgumentException("Selecione o técnico responsável pela Ordem de Serviço.");
        }

        // 1.4  seg = getSegmentoPretendido() : String
        String seg = orc.getSegmentoPretendido();

        // 1.5  [seg = "Modulos"] <<create>> OrdemDeServicoModulos(...)
        // 1.6  [seg = "Chaves"]  <<create>> OrdemDeServicoChaves(...)
        OrdemDeServico os = criarOrdemDeServico(seg, quilometragem, prazoEstimado, observacoes,
                relatoInicial, condicaoRecebimento, diagnosticoInicial, servicoExecutado);

        // 1.7  sinalizarAbertura() -> 1.7.1 setDataAbertura, 1.7.2 setStatusOs("Aberta")
        os.sinalizarAbertura();

        // 1.8  associaOS(os) -> orc   (Diagrama de Classes: associarOrcamento)
        os.associarOrcamento(orc);

        // 1.9  associaOS(os) -> vei   (Diagrama de Classes: associarVeiculo)
        os.associarVeiculo(vei);

        // 1.10 associaOS(os) -> tec   (Diagrama de Classes: associarTecnico)
        os.associarTecnico(tec);

        // RN-03 e RN-04 antes de gravar (não é mensagem do diagrama). Falha: FA-11A, nada gravado.
        os.validar();

        // RN-07: número reservado no passo 1.
        os.setNumeroOS(numeroReservado());

        // 1.11  salvar(os)
        ordemServicoDAO.salvar(os);

        // POS-02: orçamento "Convertido em OS".
        orcamentoDAO.atualizarStatus(orc.getIdOrcamento(), Orcamento.STATUS_CONVERTIDO);

        this.osEmRegistro = os;
        return os;
    }

    // =================================================================
    // FA-11B: inclusão de item não previsto
    // =================================================================

    /** FA-11B.1-2: guarda o item e devolve a exigência de justificativa (RN-05). */
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
     * FA-11B.3-4: grava a alteração e devolve a mensagem com o novo total. RN-05: orçamento
     * volta a "Aguardando aprovação do cliente"; a OS não é registrada (FA-11B.5).
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

        // RN-05: execução suspensa até nova aprovação.
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
    // FA-11C: rascunho
    // =================================================================

    /** FA-11C: grava sem número e sem mudar o orçamento. Não cobra a RN-03. */
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

        // Sem número; situação "Rascunho".
        os.marcarComoRascunho(new Date());
        ordemServicoDAO.salvar(os);

        // Sem atualizarStatus do orçamento.
        this.osEmRegistro = os;
    }

    // =================================================================
    // FA-00: desistência
    // =================================================================

    /** FA-00.2. */
    public String solicitarConfirmacaoDesistencia() {
        return "Confirma a desistência do registro da Ordem de Serviço?";
    }

    /** FA-00.3-4: nada foi gravado; só solta os objetos. */
    public void descartarRegistro() {
        this.osEmRegistro = null;
        this.itemPendente = null;
        this.orcamentoDoItemPendente = null;
    }

    /** ACRÉSCIMO: passo 8.2 (V1), botão "Ver atendimentos anteriores". */
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

    /** Mensagens 1.5 e 1.6: cria a OS na subclasse do segmento. */
    private OrdemDeServico criarOrdemDeServico(String seg, int quilometragem, Date prazoEstimado,
                                               String observacoes, String relatoInicial,
                                               String condicaoRecebimento, String diagnosticoInicial,
                                               String servicoExecutado) {
        if (!Segmento.CHAVES.equals(seg)) {
            // 1.5  [seg = "Modulos"]
            return new OrdemDeServicoModulos(quilometragem, prazoEstimado, observacoes,
                    relatoInicial, condicaoRecebimento, diagnosticoInicial);
        }
        // 1.6  [seg = "Chaves"]
        return new OrdemDeServicoChaves(quilometragem, prazoEstimado, observacoes, servicoExecutado);
    }

    /** Número do passo 1; se faltar, gera. */
    private String numeroReservado() throws SQLException {
        if (osEmRegistro != null && osEmRegistro.getNumeroOS() != null) {
            return osEmRegistro.getNumeroOS();
        }
        return ordemServicoDAO.gerarNumeroOS();
    }

    /** OS em montagem. */
    public OrdemDeServico getOsEmRegistro() {
        return osEmRegistro;
    }

    /** Passo 3: vincula orçamento e veículo (RN-02); recria a OS se o segmento mudar. */
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
