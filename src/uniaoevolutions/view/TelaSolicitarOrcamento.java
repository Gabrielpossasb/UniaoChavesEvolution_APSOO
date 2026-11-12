package uniaoevolutions.view;

import uniaoevolutions.controle.ControladoraSolicitarOrcamento;
import uniaoevolutions.negocio.Cliente;
import uniaoevolutions.negocio.ItemOrcamento;
import uniaoevolutions.negocio.ModuloECU;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.Segmento;
import uniaoevolutions.negocio.ServicoBase;
import uniaoevolutions.negocio.TipoUsuario;
import uniaoevolutions.negocio.Usuario;
import uniaoevolutions.negocio.Veiculo;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * <<boundary>> do Diagrama de Classes v2 e ":Interface" do DSS do Solicitar Orçamento.
 * Layout: MOCKUP-solicitar-orcamento.pdf (Telas 1, 2 e 3).
 * Valida só o formato dos campos (ao perder o foco e ao registrar); as regras de
 * negócio ficam na ControladoraSolicitarOrcamento. Sem SQL e sem DAO.
 */
public class TelaSolicitarOrcamento extends JFrame {

    private static final long serialVersionUID = 1L;

    // Cores tiradas do mockup.
    private static final Color AZUL_CABECALHO = new Color(0x1E, 0x40, 0xAF);
    private static final Color AZUL_TEXTO_CABECALHO = new Color(0x0F, 0x17, 0x3A);
    private static final Color AZUL_TITULO = new Color(0x1D, 0x4E, 0xD8);
    private static final Color AZUL_CLARO = new Color(0xEF, 0xF6, 0xFF);
    private static final Color AZUL_BORDA_CLARA = new Color(0xBF, 0xDB, 0xFE);
    private static final Color CINZA_FUNDO = new Color(0xF1, 0xF2, 0xF6);
    private static final Color CINZA_ROTULO = new Color(0x1F, 0x29, 0x37);
    private static final Color CINZA_DICA = new Color(0x6B, 0x72, 0x80);
    private static final Color CINZA_BORDA = new Color(0xD1, 0xD5, 0xDB);
    private static final Color CINZA_LINHA = new Color(0xE5, 0xE7, 0xEB);
    private static final Color CINZA_SOMENTE_LEITURA = new Color(0xEE, 0xF0, 0xF4);
    private static final Color CINZA_TABELA = new Color(0xF8, 0xF9, 0xFB);
    private static final Color AMARELO_FUNDO = new Color(0xFF, 0xFB, 0xEB);
    private static final Color AMARELO_BORDA = new Color(0xFC, 0xD3, 0x4D);
    private static final Color AMARELO_TEXTO = new Color(0x92, 0x40, 0x0E);
    private static final Color AMARELO_SITUACAO = new Color(0xFE, 0xF3, 0xC7);
    private static final Color LARANJA_SITUACAO = new Color(0xB4, 0x53, 0x09);
    private static final Color VERDE_FUNDO = new Color(0xF0, 0xFD, 0xF4);
    private static final Color VERDE_BORDA = new Color(0xBB, 0xF7, 0xD0);
    private static final Color VERDE_TEXTO = new Color(0x16, 0x65, 0x34);
    private static final Color VERDE_ICONE = new Color(0x2E, 0x7D, 0x32);
    private static final Color VERMELHO = new Color(0xDC, 0x26, 0x26);
    private static final Color VERMELHO_FUNDO = new Color(0xFE, 0xF2, 0xF2);
    private static final Color VERMELHO_BORDA = new Color(0xFC, 0xA5, 0xA5);
    private static final Color AMBAR_ICONE = new Color(0xEA, 0xA3, 0x08);

    private static final String CARTAO_MODULOS = "modulos";
    private static final String CARTAO_CHAVES = "chaves";

    private static final String[] TIPOS_MODULO = {
            "ECU – Injeção Eletrônica", "BCM – Módulo de Carroceria", "TCU – Câmbio Automático",
            "Painel de Instrumentos", "Módulo de Airbag", "Módulo do Imobilizador"};
    private static final String[] MODELOS_ECU = {
            "Magneti Marelli IAW 4GV.G1", "Magneti Marelli IAW 7GF", "Bosch ME7.5.30", "Bosch ME17.9.65",
            "Delphi MT22.2", "Continental SIM2K-245"};
    private static final String[] TIPOS_CHAVE = {
            "Canivete com transponder", "Chave com transponder", "Chave simples (sem transponder)",
            "Chave presencial (keyless)"};
    private static final String[] SITUACOES_IMOBILIZADOR = {
            "Funcionando – com chave válida", "Bloqueado – sem chave válida", "Sem imobilizador",
            "Não verificada"};
    private static final String[] MARCAS = {
            "Volkswagen", "Fiat", "Chevrolet", "Ford", "Toyota", "Hyundai", "Renault", "Honda", "Jeep",
            "Nissan", "Peugeot", "Citroën"};

    private final SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");

    /** Única classe de outra camada que a tela usa. */
    private final transient ControladoraSolicitarOrcamento controladora;

    // --- Estado da tela ----------------------------------------------
    private Date dataSolicitacao;
    private transient Cliente clienteAtual;
    private transient Veiculo veiculoAtual;
    private String chaveBuscaCliente;
    private String placaSelecionada;
    private transient ServicoBase servicoAcrescentado;
    private boolean limiteChavesAtingido;
    private int proximoIdItem = 1;
    private double valorEstimado;
    /** Ordem do DSS nas variantes: true quando os dados do módulo/das chaves já foram enviados. */
    private boolean dadosModuloEnviados;
    private boolean dadosChavesEnviados;
    /** Último segmento aceito (null = nenhum); usado para desfazer uma troca recusada. */
    private String segmentoAceito;
    private String caminhoFoto;
    private final transient List<Object[]> itensTela = new ArrayList<>();
    private final transient List<Orcamento> historicoTecnico = new ArrayList<>();
    private final transient List<String> ordensDeServicoDoVeiculo = new ArrayList<>();
    /** true durante preenchimento automático: os eventos de troca são ignorados. */
    private boolean carregando;

    // --- Validação ---------------------------------------------------
    private final transient List<Campo> camposFormulario = new ArrayList<>();

    // --- Identificação do orçamento ----------------------------------
    private final JTextField campoNumero = criarCampoSomenteLeitura("campoNumero");
    private final JTextField campoDataSolicitacao = criarCampoSomenteLeitura("campoDataSolicitacao");
    private final CampoTexto campoValidade = new CampoTexto("campoValidade", "dd/mm/aaaa");
    private final JRadioButton radioPresencial = criarRadio("Presencial", "radioPresencial");
    private final JRadioButton radioRemoto = criarRadio("Remoto", "radioRemoto");
    private final ButtonGroup grupoOrigem = new ButtonGroup();
    private final JPanel painelOrigem = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
    private final JTextField campoResponsavel = criarCampoSomenteLeitura("campoResponsavel");
    private final JLabel rotuloSituacao = new JLabel("EM ANÁLISE");

    // --- Cliente e veículo -------------------------------------------
    private final CampoTexto campoCliente = new CampoTexto("campoCliente", "");
    private final JPanel painelCliente = new JPanel(new BorderLayout());
    private final JTextField campoCpfCnpj = criarCampoSomenteLeitura("campoCpfCnpj");
    private final JTextField campoTelefone = criarCampoSomenteLeitura("campoTelefone");
    private final JButton botaoClienteNaoCadastrado = criarBotaoContorno("+ Cliente não cadastrado");
    private final JComboBox<String> comboVeiculo = criarComboEditavel("comboVeiculo", new String[0], "");
    private final JTextField campoMarcaModelo = criarCampoSomenteLeitura("campoMarcaModelo");
    private final JTextField campoAnoModelo = criarCampoSomenteLeitura("campoAnoModelo");
    private final JTextField campoMotorizacao = criarCampoSomenteLeitura("campoMotorizacao");
    private final JTextField campoChassi = criarCampoSomenteLeitura("campoChassi");
    private final JButton botaoHistorico = new JButton(" ");
    private final JButton botaoVeiculoNaoListado = criarBotaoContorno("+ Veículo não listado");

    /** Lista de clientes cadastrados que se abre sob o campo "Cliente". */
    private final javax.swing.JPopupMenu popupClientes = new javax.swing.JPopupMenu();
    private final javax.swing.DefaultListModel<Cliente> modeloClientes = new javax.swing.DefaultListModel<>();
    private final JList<Cliente> listaClientes = new JList<>(modeloClientes);

    // --- Segmento pretendido -----------------------------------------
    private final JRadioButton radioModulos =
            criarRadio("Reparação / Programação de Módulos (ECU)", "radioModulos");
    private final JRadioButton radioChaves = criarRadio("Programação / Confecção de Chaves", "radioChaves");
    private final ButtonGroup grupoSegmento = new ButtonGroup();
    private final JPanel painelSegmento = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    // --- Dados técnicos ----------------------------------------------
    private final JPanel secaoDadosTecnicos = new JPanel();
    private final JLabel tituloDadosTecnicos = new JLabel();
    private final CardLayout cartoes = new CardLayout();
    private final JPanel painelCartoes = new JPanel(cartoes);
    // V1 - Módulos
    private final JComboBox<String> comboTipoModulo = criarComboEditavel("comboTipoModulo", TIPOS_MODULO, "Selecione…");
    private final JComboBox<String> comboModeloECU = criarComboEditavel("comboModeloECU", MODELOS_ECU, "Selecione…");
    /** Editável; lista os módulos já registrados do veículo. */
    private final JComboBox<String> comboIdentificacaoModulo =
            criarComboEditavel("comboIdentificacaoModulo", new String[0], "");
    private final transient List<ModuloECU> modulosDoVeiculo = new ArrayList<>();
    private final JTextField campoModeloDispensado = criarCampoSomenteLeitura("campoModeloDispensado");
    private final JTextField campoIdentificacaoDispensada = criarCampoSomenteLeitura("campoIdentificacaoDispensada");
    private final CardLayout cartaoModelo = new CardLayout();
    private final JPanel painelModelo = new JPanel(cartaoModelo);
    private final CardLayout cartaoIdentificacao = new CardLayout();
    private final JPanel painelIdentificacao = new JPanel(cartaoIdentificacao);
    private final CampoTexto campoSintoma = new CampoTexto("campoSintoma", "");
    private final JCheckBox checkIlegivel =
            criarCheck("Etiqueta de identificação do módulo ilegível ou ausente", "checkIlegivel");
    private final JCheckBox checkBancada =
            criarCheck("Diagnóstico definitivo depende de teste em bancada", "checkBancada");
    private final JPanel alertaIlegivel = criarAlertaInfo(
            "Modelo da ECU e identificação do módulo dispensados. Anexe uma foto do módulo e descreva a placa.");
    private final JPanel celulaFoto = new JPanel(new BorderLayout(0, 4));
    private final JLabel caixaFoto = new JLabel("", SwingConstants.CENTER);
    private final JPanel linhaDescricaoPlaca = new JPanel();
    private final CampoTexto campoDescricaoPlaca = new CampoTexto("campoDescricaoPlaca", "");
    private final JPanel alertaBancada = criarAlertaAmarelo("Diagnóstico pendente de bancada:",
            " os valores propostos serão estimativos e a confirmação do defeito pode exigir revisão do "
            + "orçamento. Registre essa condição nas observações.");
    private final JLabel caixaAtendimentos = new TextoQuebravel(" ");
    // V2 - Chaves
    private final JComboBox<String> comboTipoChave = criarComboEditavel("comboTipoChave", TIPOS_CHAVE, "Selecione…");
    private final CampoTexto campoQuantidade = new CampoTexto("campoQuantidade", "");
    private final JCheckBox checkApagarChaves = criarCheck("<html>Apagar chaves<br>existentes</html>", "checkApagarChaves");
    private final JRadioButton radioTelecomandoSim = criarRadio("Sim", "radioTelecomandoSim");
    private final JRadioButton radioTelecomandoNao = criarRadio("Não", "radioTelecomandoNao");
    private final CampoTexto campoTransponder = new CampoTexto("campoTransponder", "");
    private final JComboBox<String> comboSituacaoImobilizador =
            criarComboEditavel("comboSituacaoImobilizador", SITUACOES_IMOBILIZADOR, "Selecione…");
    private final JCheckBox checkPerdaTotal = criarCheck("", "checkPerdaTotal");
    private final JPanel alertaComprovacao = criarAlertaAmarelo("Comprovação de propriedade obrigatória",
            " para serviços de chaves. Em caso de perda total, informe o documento do veículo e a "
            + "identificação de quem está solicitando.");
    private final JPanel linhaDocumentos = new JPanel();
    private final CampoTexto campoDocumentoPropriedade = new CampoTexto("campoDocumentoPropriedade", "");
    private final CampoTexto campoNomeSolicitante = new CampoTexto("campoNomeSolicitante", "");
    private final CampoTexto campoDocumentoSolicitante = new CampoTexto("campoDocumentoSolicitante", "");

    // --- Serviços a orçar --------------------------------------------
    private final JLabel rotuloCategoriaServicos = new JLabel(" ");
    private final JComboBox<ServicoBase> comboServico = new JComboBox<>();
    private final CampoTexto campoValorProposto = new CampoTexto("campoValorProposto", "0,00");
    private final JButton botaoIncluirServico = criarBotaoPrimario("+ Incluir serviço");
    private final JLabel avisoItensMantidos = new JLabel("Os itens já incluídos são mantidos.");
    private final DefaultTableModel modeloItens = new DefaultTableModel(
            new String[]{"Serviço", "Categoria", "Valor proposto", "Ações"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabelaItens = new JTable(modeloItens);
    private final JLabel rotuloSemItens = new JLabel("Nenhum serviço incluído.", SwingConstants.CENTER);
    private final JLabel rotuloValorEstimado = new JLabel(Validacoes.formatarMoeda(0));
    private final JPanel erroSemItens = criarAlertaErro(ControladoraSolicitarOrcamento.MSG_SEM_ITENS);
    private final JPanel avisoAcrescentado = new JPanel(new BorderLayout(8, 0));
    private final JLabel textoAcrescentado = new TextoQuebravel("");
    private final CampoTexto campoObservacoes = new CampoTexto("campoObservacoes", "");

    // --- Pendências (FA-12B) e rodapé --------------------------------
    private final JPanel caixaPendencias = new JPanel(new BorderLayout(8, 0));
    private final JLabel textoPendencias = new TextoQuebravel("");
    private final JPanel conteudo = new PainelLarguraDaJanela();
    private final JButton botaoDesistir = criarBotaoContorno("Voltar / Desistir");
    private final JButton botaoRascunho = criarBotaoContorno("Salvar rascunho");
    private final JButton botaoRegistrar = criarBotaoPrimario("Registrar orçamento");

    // Campos com validação (montados em registrarValidacoes).
    private Campo cValidade;
    private Campo cOrigem;
    private Campo cCliente;
    private Campo cVeiculo;
    private Campo cSegmento;
    private Campo cTipoModulo;
    private Campo cModeloECU;
    private Campo cIdentificacao;
    private Campo cSintoma;
    private Campo cFoto;
    private Campo cDescricaoPlaca;
    private Campo cTipoChave;
    private Campo cQuantidade;
    private Campo cTransponder;
    private Campo cSituacao;
    private Campo cDocumentoPropriedade;
    private Campo cNomeSolicitante;
    private Campo cDocumentoSolicitante;
    private Campo cServico;
    private Campo cValorProposto;
    private Campo cObservacoes;

    /** @param idUsuarioLogado usuário do atendimento (PRE-01; mensagem 1.1). */
    public TelaSolicitarOrcamento(int idUsuarioLogado) {
        super("União Evolutions - Solicitar Orçamento");
        this.controladora = new ControladoraSolicitarOrcamento(idUsuarioLogado);
        // O "X" não fecha; só "Voltar / Desistir" (FA-00).
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        java.awt.Dimension tela = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1400, tela.width), Math.min(920, tela.height - 40));
        setLocationRelativeTo(null);
        montarTela();
        registrarValidacoes();
        restringirDigitacao();
        ligarEventos();
        // Passo 1 depois de a janela aparecer: um erro de banco sai sobre a tela montada.
        SwingUtilities.invokeLater(this::iniciarOrcamento);
    }

    // =================================================================
    // Montagem da tela (Tela 1 do mockup)
    // =================================================================

    private void montarTela() {
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(CINZA_FUNDO);
        conteudo.setBorder(BorderFactory.createEmptyBorder(10, 18, 12, 18));

        JLabel obrigatorios = new JLabel("<html><i>Campos marcados com <font color='#DC2626'>*</font>"
                + " são de preenchimento obrigatório.</i></html>");
        obrigatorios.setFont(obrigatorios.getFont().deriveFont(Font.PLAIN, 11f));
        obrigatorios.setForeground(CINZA_DICA);
        obrigatorios.setAlignmentX(LEFT_ALIGNMENT);
        conteudo.add(obrigatorios);
        conteudo.add(Box.createVerticalStrut(8));

        montarCaixaPendencias();
        conteudo.add(caixaPendencias);

        conteudo.add(criarSecaoIdentificacao());
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(criarSecaoClienteVeiculo());
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(criarSecaoSegmento());
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(criarSecaoDadosTecnicos());
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(criarSecaoServicos());
        conteudo.add(Box.createVerticalGlue());

        JScrollPane rolagem = new JScrollPane(conteudo);
        rolagem.setBorder(null);
        rolagem.getVerticalScrollBar().setUnitIncrement(18);
        rolagem.getViewport().setBackground(CINZA_FUNDO);

        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);
        add(rolagem, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
    }

    /** Barra azul do topo do mockup. */
    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel esquerda = new JPanel();
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.setOpaque(false);
        JLabel empresa = new JLabel("UNIÃO EVOLUTIONS");
        empresa.setForeground(AZUL_TEXTO_CABECALHO);
        empresa.setFont(empresa.getFont().deriveFont(Font.BOLD, 17f));
        JLabel sistema = new JLabel("Sistema de Gestão de Serviços Automotivos");
        sistema.setForeground(Color.WHITE);
        sistema.setFont(sistema.getFont().deriveFont(Font.PLAIN, 11f));
        esquerda.add(empresa);
        esquerda.add(sistema);

        JLabel titulo = new JLabel("Solicitar Orçamento");
        titulo.setForeground(AZUL_TEXTO_CABECALHO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));

        cabecalho.add(esquerda, BorderLayout.WEST);
        cabecalho.add(titulo, BorderLayout.EAST);
        return cabecalho;
    }

    /** Caixa vermelha "Corrija N pendências antes de registrar" (Tela 3, FA-12B). */
    private void montarCaixaPendencias() {
        caixaPendencias.setName("caixaPendencias");
        caixaPendencias.setBackground(VERMELHO_FUNDO);
        caixaPendencias.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VERMELHO_BORDA),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        caixaPendencias.setAlignmentX(LEFT_ALIGNMENT);
        textoPendencias.setForeground(VERMELHO);
        textoPendencias.setFont(textoPendencias.getFont().deriveFont(Font.PLAIN, 12f));
        JLabel icone = new JLabel(new IconeCirculo('!', VERMELHO, false, 18));
        icone.setVerticalAlignment(SwingConstants.TOP);
        caixaPendencias.add(icone, BorderLayout.WEST);
        caixaPendencias.add(textoPendencias, BorderLayout.CENTER);
        caixaPendencias.setVisible(false);
    }

    /** Bloco "IDENTIFICAÇÃO DO ORÇAMENTO" - passos 1 e 12. */
    private JPanel criarSecaoIdentificacao() {
        grupoOrigem.add(radioPresencial);
        grupoOrigem.add(radioRemoto);
        painelOrigem.setName("painelOrigem");
        painelOrigem.setBackground(Color.WHITE);
        painelOrigem.add(radioPresencial);
        painelOrigem.add(radioRemoto);
        painelOrigem.setBorder(bordaCampo(CINZA_BORDA));

        rotuloSituacao.setName("rotuloSituacao");
        rotuloSituacao.setOpaque(true);
        rotuloSituacao.setBackground(AMARELO_SITUACAO);
        rotuloSituacao.setForeground(LARANJA_SITUACAO);
        rotuloSituacao.setFont(rotuloSituacao.getFont().deriveFont(Font.BOLD, 12f));
        rotuloSituacao.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        JPanel situacao = new JPanel(new BorderLayout());
        situacao.setOpaque(false);
        situacao.add(rotuloSituacao, BorderLayout.WEST);

        JPanel secao = criarSecao("IDENTIFICAÇÃO DO ORÇAMENTO", null);
        cValidade = new Campo("Válido até", campoValidade, campoValidade,
                "Ex.: " + exemploDeData() + ". Igual ou posterior à data da solicitação.");
        cOrigem = new Campo("Origem do atendimento", painelOrigem, painelOrigem, "");
        secao.add(criarLinha(new JComponent[]{
                criarCampo("Nº do orçamento", false, campoNumero, "Número provisório, gerado pelo sistema."),
                criarCampo("Data da solicitação", false, campoDataSolicitacao, ""),
                cValidade.painel(true),
                cOrigem.painel(true),
                criarCampo("Responsável", false, campoResponsavel, "Usuário logado."),
                criarCampo("Situação", false, situacao, "")},
                new double[]{1.0, 0.85, 0.95, 1.15, 1.6, 0.6}));
        return secao;
    }

    /** Bloco "CLIENTE E VEÍCULO" - passos 2 a 5, FA-02 e FA-04. */
    private JPanel criarSecaoClienteVeiculo() {
        campoCliente.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
        painelCliente.setBackground(Color.WHITE);
        painelCliente.add(campoCliente, BorderLayout.CENTER);
        JLabel lupa = new JLabel(new IconeLupa());
        lupa.setName("lupaCliente");
        lupa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lupa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                campoCliente.requestFocusInWindow();
                mostrarSugestoesDeClientes(campoCliente.getText());
            }
        });
        montarListaDeClientes();
        lupa.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        painelCliente.add(lupa, BorderLayout.EAST);
        painelCliente.setBorder(BorderFactory.createLineBorder(CINZA_BORDA));

        botaoHistorico.setName("botaoHistorico");
        botaoHistorico.setHorizontalAlignment(SwingConstants.LEFT);
        botaoHistorico.setBackground(AZUL_CLARO);
        botaoHistorico.setForeground(AZUL_TITULO);
        botaoHistorico.setFont(botaoHistorico.getFont().deriveFont(Font.BOLD, 11f));
        botaoHistorico.setFocusPainted(false);
        botaoHistorico.setContentAreaFilled(false);
        botaoHistorico.setOpaque(true);
        botaoHistorico.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_BORDA_CLARA), BorderFactory.createEmptyBorder(0, 10, 0, 6)));

        botaoClienteNaoCadastrado.setName("botaoClienteNaoCadastrado");
        botaoVeiculoNaoListado.setName("botaoVeiculoNaoListado");

        cCliente = new Campo("Cliente", painelCliente, campoCliente, "Busque por nome, CPF/CNPJ ou telefone.");
        cVeiculo = new Campo("Veículo (placa)", comboVeiculo, comboVeiculo, "Veículos vinculados ao cliente.");

        JPanel secao = criarSecao("CLIENTE E VEÍCULO", null);
        secao.add(criarLinha(new JComponent[]{
                cCliente.painel(true),
                criarCampo("CPF / CNPJ", false, campoCpfCnpj, ""),
                criarCampo("Telefone / WhatsApp", false, campoTelefone, ""),
                criarCampo(" ", false, botaoClienteNaoCadastrado, "")},
                new double[]{2.6, 1.05, 1.05, 0.9}));
        secao.add(criarLinha(new JComponent[]{
                cVeiculo.painel(true),
                criarCampo("Marca / Modelo", false, campoMarcaModelo, ""),
                criarCampo("Ano-modelo", false, campoAnoModelo, ""),
                criarCampo("Motorização", false, campoMotorizacao, ""),
                criarCampo("Nº do chassi", false, campoChassi, ""),
                criarCampo("Histórico técnico", false, botaoHistorico, ""),
                criarCampo(" ", false, botaoVeiculoNaoListado, "")},
                new double[]{0.55, 1.3, 0.35, 0.45, 1.9, 0.9, 0.2}));
        return secao;
    }

    /** Bloco "SEGMENTO PRETENDIDO *" - passos 6 e 7. */
    private JPanel criarSecaoSegmento() {
        grupoSegmento.add(radioModulos);
        grupoSegmento.add(radioChaves);
        painelSegmento.setName("painelSegmento");
        painelSegmento.setOpaque(false);
        painelSegmento.add(radioModulos);
        painelSegmento.add(Box.createHorizontalStrut(40));
        painelSegmento.add(radioChaves);

        JPanel secao = criarSecao("SEGMENTO PRETENDIDO", null);
        cSegmento = new Campo("Segmento pretendido", painelSegmento, painelSegmento, "");
        JPanel campo = cSegmento.painelSemRotulo();
        campo.setAlignmentX(LEFT_ALIGNMENT);
        secao.add(campo);
        secao.add(Box.createVerticalStrut(6));
        secao.add(criarAlertaAmarelo("", "O segmento define o bloco de dados técnicos exibido abaixo e os "
                + "serviços base que podem compor o orçamento. A Ordem de Serviço gerada a partir deste "
                + "orçamento herdará o mesmo segmento."));
        return secao;
    }

    /** Bloco "DADOS TÉCNICOS" - V1 (passos 7.1 a 8.3) e V2 (passos 7.3 a 8.6). */
    private JPanel criarSecaoDadosTecnicos() {
        secaoDadosTecnicos.setLayout(new BoxLayout(secaoDadosTecnicos, BoxLayout.Y_AXIS));
        secaoDadosTecnicos.setName("secaoDadosTecnicos");
        configurarSecao(secaoDadosTecnicos, tituloDadosTecnicos, null);
        painelCartoes.setOpaque(false);
        painelCartoes.setAlignmentX(LEFT_ALIGNMENT);
        painelCartoes.add(criarBlocoModulos(), CARTAO_MODULOS);
        painelCartoes.add(criarBlocoChaves(), CARTAO_CHAVES);
        secaoDadosTecnicos.add(painelCartoes);
        // Passo 7: o bloco aparece após o segmento ser informado.
        secaoDadosTecnicos.setVisible(false);
        return secaoDadosTecnicos;
    }

    /** Variante V1 - Módulos (ECU). */
    private JPanel criarBlocoModulos() {
        JPanel bloco = criarBloco();

        campoModeloDispensado.setText("Dispensado");
        campoIdentificacaoDispensada.setText("Dispensado");
        campoModeloDispensado.setForeground(CINZA_DICA);
        campoIdentificacaoDispensada.setForeground(CINZA_DICA);
        painelModelo.setOpaque(false);
        painelModelo.add(comboModeloECU, "editavel");
        painelModelo.add(campoModeloDispensado, "dispensado");
        painelIdentificacao.setOpaque(false);
        painelIdentificacao.add(comboIdentificacaoModulo, "editavel");
        painelIdentificacao.add(campoIdentificacaoDispensada, "dispensado");

        cTipoModulo = new Campo("Tipo de módulo", comboTipoModulo, comboTipoModulo, "").doSegmento(Segmento.MODULOS);
        cModeloECU = new Campo("Modelo da ECU", painelModelo, comboModeloECU, "").doSegmento(Segmento.MODULOS);
        cIdentificacao = new Campo("Identificação do módulo", painelIdentificacao, comboIdentificacaoModulo, "");
        cSintoma = new Campo("Sintoma relatado pelo cliente", campoSintoma, campoSintoma, "")
                .doSegmento(Segmento.MODULOS);

        caixaFoto.setName("caixaFoto");
        caixaFoto.setOpaque(true);
        caixaFoto.setBackground(CINZA_TABELA);
        caixaFoto.setForeground(CINZA_DICA);
        caixaFoto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        caixaFoto.setBorder(BorderFactory.createDashedBorder(new Color(0x9C, 0xA3, 0xAF), 4, 3));
        caixaFoto.setPreferredSize(new Dimension(10, 46));
        cFoto = new Campo("Registro fotográfico do módulo", caixaFoto, caixaFoto, "").doSegmento(Segmento.MODULOS);
        celulaFoto.setOpaque(false);
        celulaFoto.add(cFoto.painel(true), BorderLayout.CENTER);
        celulaFoto.setVisible(false);

        bloco.add(criarLinha(new JComponent[]{cTipoModulo.painel(true), cModeloECU.painel(true),
                cIdentificacao.painel(false),
                celulaFoto}, new double[]{1, 1.15, 0.8, 1.15}));
        bloco.add(criarLinha(new JComponent[]{cSintoma.painel(true)}, new double[]{1}));

        JPanel checks = new JPanel(new GridBagLayout());
        checks.setOpaque(false);
        checks.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.gridx = 0;
        checks.add(checkIlegivel, c);
        c.gridx = 1;
        checks.add(checkBancada, c);
        bloco.add(checks);
        bloco.add(Box.createVerticalStrut(4));

        alertaIlegivel.setVisible(false);
        bloco.add(alertaIlegivel);

        cDescricaoPlaca = new Campo("Descrição física da placa", campoDescricaoPlaca, campoDescricaoPlaca, "")
                .doSegmento(Segmento.MODULOS);
        linhaDescricaoPlaca.setLayout(new BoxLayout(linhaDescricaoPlaca, BoxLayout.Y_AXIS));
        linhaDescricaoPlaca.setOpaque(false);
        linhaDescricaoPlaca.setAlignmentX(LEFT_ALIGNMENT);
        linhaDescricaoPlaca.add(criarLinha(new JComponent[]{cDescricaoPlaca.painel(true)}, new double[]{1}));
        linhaDescricaoPlaca.setVisible(false);
        bloco.add(linhaDescricaoPlaca);

        alertaBancada.setVisible(false);
        bloco.add(Box.createVerticalStrut(4));
        bloco.add(alertaBancada);
        bloco.add(Box.createVerticalStrut(8));

        caixaAtendimentos.setName("caixaAtendimentos");
        caixaAtendimentos.setOpaque(true);
        caixaAtendimentos.setBackground(CINZA_TABELA);
        caixaAtendimentos.setForeground(CINZA_ROTULO);
        caixaAtendimentos.setFont(caixaAtendimentos.getFont().deriveFont(Font.PLAIN, 12f));
        caixaAtendimentos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CINZA_LINHA), BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        bloco.add(criarLinha(new JComponent[]{criarCampo("Atendimentos anteriores deste módulo", false,
                caixaAtendimentos, "")}, new double[]{1}));
        return bloco;
    }

    /** Variante V2 - Chaves. */
    private JPanel criarBlocoChaves() {
        JPanel bloco = criarBloco();

        ButtonGroup grupoTelecomando = new ButtonGroup();
        grupoTelecomando.add(radioTelecomandoSim);
        grupoTelecomando.add(radioTelecomandoNao);
        JPanel telecomando = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        telecomando.setBackground(Color.WHITE);
        telecomando.setBorder(bordaCampo(CINZA_BORDA));
        telecomando.add(radioTelecomandoSim);
        telecomando.add(radioTelecomandoNao);

        cTipoChave = new Campo("Tipo de chave", comboTipoChave, comboTipoChave, "").doSegmento(Segmento.CHAVES);
        JPanel quantidade = new JPanel(new BorderLayout(10, 0));
        quantidade.setOpaque(false);
        quantidade.add(campoQuantidade, BorderLayout.CENTER);
        checkApagarChaves.setVisible(false);
        quantidade.add(checkApagarChaves, BorderLayout.EAST);
        cQuantidade = new Campo("Qtd. de cópias", quantidade, campoQuantidade,
                "Máximo do imobilizador: " + ControladoraSolicitarOrcamento.QUANTIDADE_MAXIMA_CHAVES + ".")
                .doSegmento(Segmento.CHAVES);
        cTransponder = new Campo("Código do transponder", campoTransponder, campoTransponder, "");
        cSituacao = new Campo("Situação do imobilizador", comboSituacaoImobilizador, comboSituacaoImobilizador, "")
                .doSegmento(Segmento.CHAVES);

        bloco.add(criarLinha(new JComponent[]{cTipoChave.painel(true), cQuantidade.painel(true),
                criarCampo("Telecomando", false, telecomando, ""), cTransponder.painel(false),
                cSituacao.painel(true)}, new double[]{1.05, 0.6, 0.75, 0.95, 1.05}));

        checkPerdaTotal.setText("<html><b>Perda total das chaves</b> &nbsp;(o cliente não possui nenhuma "
                + "chave funcional do veículo)</html>");
        checkPerdaTotal.setAlignmentX(LEFT_ALIGNMENT);
        bloco.add(checkPerdaTotal);
        bloco.add(Box.createVerticalStrut(6));

        alertaComprovacao.setVisible(false);
        bloco.add(alertaComprovacao);

        cDocumentoPropriedade = new Campo("Documento de propriedade (CRLV)", campoDocumentoPropriedade,
                campoDocumentoPropriedade, "").doSegmento(Segmento.CHAVES);
        cNomeSolicitante = new Campo("Solicitante – nome", campoNomeSolicitante, campoNomeSolicitante, "")
                .doSegmento(Segmento.CHAVES);
        cDocumentoSolicitante = new Campo("Solicitante – documento", campoDocumentoSolicitante,
                campoDocumentoSolicitante, "").doSegmento(Segmento.CHAVES);
        linhaDocumentos.setLayout(new BoxLayout(linhaDocumentos, BoxLayout.Y_AXIS));
        linhaDocumentos.setOpaque(false);
        linhaDocumentos.setAlignmentX(LEFT_ALIGNMENT);
        linhaDocumentos.add(Box.createVerticalStrut(4));
        linhaDocumentos.add(criarLinha(new JComponent[]{cDocumentoPropriedade.painel(true),
                cNomeSolicitante.painel(true), cDocumentoSolicitante.painel(true)}, new double[]{1, 1, 1}));
        linhaDocumentos.setVisible(false);
        bloco.add(linhaDocumentos);
        return bloco;
    }

    /** Bloco "SERVIÇOS A ORÇAR" - passos 9 a 12, FA-11A e FA-11B. */
    private JPanel criarSecaoServicos() {
        JPanel secao = criarSecao("SERVIÇOS A ORÇAR", rotuloCategoriaServicos);

        comboServico.setName("comboServico");
        comboServico.setBackground(Color.WHITE);
        comboServico.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                JLabel rotulo = (JLabel) super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                if (valor == null) {
                    rotulo.setText("Selecione um serviço…");
                    rotulo.setForeground(CINZA_DICA);
                }
                rotulo.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                return rotulo;
            }
        });
        botaoIncluirServico.setName("botaoIncluirServico");
        cServico = new Campo("Serviço base", comboServico, comboServico, "");
        cValorProposto = new Campo("Valor proposto (R$)", campoValorProposto, campoValorProposto, "");

        avisoItensMantidos.setFont(avisoItensMantidos.getFont().deriveFont(Font.PLAIN, 11f));
        avisoItensMantidos.setForeground(CINZA_DICA);
        avisoItensMantidos.setVisible(false);
        JPanel celulaServico = new JPanel(new BorderLayout(0, 2));
        celulaServico.setOpaque(false);
        celulaServico.add(cServico.painel(true), BorderLayout.CENTER);
        celulaServico.add(avisoItensMantidos, BorderLayout.SOUTH);

        secao.add(criarLinha(new JComponent[]{celulaServico, cValorProposto.painel(true),
                criarCampo(" ", false, botaoIncluirServico, "")}, new double[]{2.7, 1, 0.5}));
        secao.add(Box.createVerticalStrut(8));
        secao.add(criarTabelaItens());

        erroSemItens.setVisible(false);
        secao.add(Box.createVerticalStrut(8));
        secao.add(erroSemItens);

        avisoAcrescentado.setName("avisoAcrescentado");
        avisoAcrescentado.setBackground(VERDE_FUNDO);
        avisoAcrescentado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VERDE_BORDA), BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        avisoAcrescentado.setAlignmentX(LEFT_ALIGNMENT);
        textoAcrescentado.setForeground(VERDE_TEXTO);
        textoAcrescentado.setFont(textoAcrescentado.getFont().deriveFont(Font.PLAIN, 12f));
        avisoAcrescentado.add(new JLabel(new IconeCirculo('+', VERDE_TEXTO, false, 18)), BorderLayout.WEST);
        avisoAcrescentado.add(textoAcrescentado, BorderLayout.CENTER);
        avisoAcrescentado.setVisible(false);
        secao.add(avisoAcrescentado);

        cObservacoes = new Campo("Observações", campoObservacoes, campoObservacoes, "");
        secao.add(Box.createVerticalStrut(4));
        secao.add(criarLinha(new JComponent[]{cObservacoes.painel(false)}, new double[]{1}));
        secao.add(Box.createVerticalStrut(4));
        secao.add(criarAlertaInfo("<html>Ao registrar, o orçamento recebe número definitivo e fica com a "
                + "situação <b>EM ANÁLISE</b> até a manifestação do cliente. Só pode ser convertido em Ordem "
                + "de Serviço depois de aprovado e dentro do prazo de validade.</html>"));
        return secao;
    }

    /** Tabela Serviço | Categoria | Valor proposto | Ações, com o total no rodapé. */
    private JPanel criarTabelaItens() {
        tabelaItens.setName("tabelaItens");
        tabelaItens.setRowHeight(40);
        tabelaItens.setShowVerticalLines(false);
        tabelaItens.setGridColor(CINZA_LINHA);
        tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaItens.setFocusable(false);
        tabelaItens.setRowSelectionAllowed(false);
        tabelaItens.getTableHeader().setReorderingAllowed(false);
        tabelaItens.getTableHeader().setResizingAllowed(false);
        tabelaItens.getTableHeader().setPreferredSize(new Dimension(10, 36));
        tabelaItens.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int l, int c) {
                JLabel r = (JLabel) super.getTableCellRendererComponent(t, v, s, f, l, c);
                r.setFont(r.getFont().deriveFont(Font.BOLD, 12f));
                r.setBackground(CINZA_TABELA);
                r.setForeground(CINZA_ROTULO);
                r.setHorizontalAlignment(c == 2 ? SwingConstants.RIGHT : c == 3 ? SwingConstants.CENTER
                        : SwingConstants.LEFT);
                r.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, CINZA_LINHA),
                        BorderFactory.createEmptyBorder(0, 16, 0, 16)));
                return r;
            }
        });
        tabelaItens.setDefaultRenderer(Object.class, new RenderizadorItens());
        int[] larguras = {640, 120, 140, 90};
        for (int i = 0; i < larguras.length; i++) {
            tabelaItens.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabelaItens.getColumnModel().getColumn(0).setMinWidth(300);
        tabelaItens.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                aoClicarNaTabela(evento);
            }
        });

        rotuloSemItens.setForeground(CINZA_DICA);
        rotuloSemItens.setFont(rotuloSemItens.getFont().deriveFont(Font.PLAIN, 12f));
        rotuloSemItens.setPreferredSize(new Dimension(10, 52));
        rotuloSemItens.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        rotuloSemItens.setOpaque(true);
        rotuloSemItens.setBackground(Color.WHITE);

        JPanel total = new JPanel(new BorderLayout());
        total.setBackground(CINZA_TABELA);
        total.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CINZA_LINHA),
                BorderFactory.createEmptyBorder(12, 16, 12, 106)));
        JLabel rotuloTotal = new JLabel("Valor estimado do orçamento");
        rotuloTotal.setFont(rotuloTotal.getFont().deriveFont(Font.BOLD, 12f));
        rotuloTotal.setForeground(CINZA_ROTULO);
        rotuloValorEstimado.setName("rotuloValorEstimado");
        rotuloValorEstimado.setFont(rotuloValorEstimado.getFont().deriveFont(Font.BOLD, 16f));
        rotuloValorEstimado.setForeground(AZUL_TITULO);
        total.add(rotuloTotal, BorderLayout.WEST);
        total.add(rotuloValorEstimado, BorderLayout.EAST);

        JPanel tabela = new JPanel();
        tabela.setLayout(new BoxLayout(tabela, BoxLayout.Y_AXIS));
        tabela.setBorder(BorderFactory.createLineBorder(CINZA_LINHA));
        tabela.setAlignmentX(LEFT_ALIGNMENT);
        tabela.add(tabelaItens.getTableHeader());
        tabela.add(tabelaItens);
        tabela.add(rotuloSemItens);
        tabela.add(total);
        for (Component c : tabela.getComponents()) {
            ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        }
        return tabela;
    }

    /** Rodapé: aviso da RN-09 e os três botões do mockup. */
    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(CINZA_FUNDO);
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CINZA_LINHA),
                BorderFactory.createEmptyBorder(10, 18, 12, 18)));
        JLabel aviso = new JLabel("Orçamentos registrados não podem ser excluídos, apenas cancelados com motivo.");
        aviso.setFont(aviso.getFont().deriveFont(Font.PLAIN, 11f));
        aviso.setForeground(CINZA_DICA);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botoes.setOpaque(false);
        botaoDesistir.setName("botaoDesistir");
        botaoDesistir.setForeground(CINZA_ROTULO);
        botaoRascunho.setName("botaoRascunho");
        botaoRegistrar.setName("botaoRegistrar");
        botoes.add(botaoDesistir);
        botoes.add(botaoRascunho);
        botoes.add(botaoRegistrar);

        rodape.add(aviso, BorderLayout.WEST);
        rodape.add(botoes, BorderLayout.EAST);
        return rodape;
    }

    // =================================================================
    // Validações (Tela 3 do mockup)
    // =================================================================

    /** Regras de formato dos campos: rodam ao perder o foco e em validarFormulario. */
    private void registrarValidacoes() {
        cValidade.regra(envio -> {
            Date data = Validacoes.converterData(campoValidade.getText());
            return data == null || data.before(dataSolicitacao)
                    ? ControladoraSolicitarOrcamento.MSG_VALIDADE_INVALIDA : null;
        });
        cOrigem.regra(envio -> origemSelecionada() == null ? "Selecione a origem do atendimento." : null);
        cCliente.regra(envio -> {
            String erro = Validacoes.validarBuscaCliente(campoCliente.getText());
            if (erro == null && envio && clienteAtual == null) {
                erro = "Cliente não encontrado. Use “+ Cliente não cadastrado”.";
            }
            return erro;
        });
        cVeiculo.regra(envio -> {
            String erro = Validacoes.validarPlaca(textoVeiculo());
            if (erro == null && envio && veiculoAtual == null) {
                erro = "Veículo não encontrado. Use “+ Veículo não listado”.";
            }
            return erro;
        });
        cSegmento.regra(envio -> segmentoSelecionado() == null ? "Selecione o segmento pretendido." : null);

        BooleanSupplier modulos = () -> Segmento.MODULOS.equals(segmentoSelecionado());
        BooleanSupplier chaves = () -> Segmento.CHAVES.equals(segmentoSelecionado());
        cTipoModulo.quando(modulos).regra(envio -> Validacoes.validarObrigatorio(texto(comboTipoModulo)));
        cModeloECU.quando(() -> modulos.getAsBoolean() && !checkIlegivel.isSelected())
                .regra(envio -> Validacoes.validarObrigatorio(texto(comboModeloECU)));
        cIdentificacao.quando(() -> modulos.getAsBoolean() && !checkIlegivel.isSelected())
                .regra(envio -> Validacoes.validarTamanho(texto(comboIdentificacaoModulo)));
        cSintoma.quando(modulos).regra(envio -> Validacoes.validarObrigatorio(campoSintoma.getText()));
        cFoto.quando(() -> modulos.getAsBoolean() && checkIlegivel.isSelected())
                .regra(envio -> caminhoFoto == null ? Validacoes.MSG_OBRIGATORIO : Validacoes.validarTamanho(caminhoFoto));
        cDescricaoPlaca.quando(() -> modulos.getAsBoolean() && checkIlegivel.isSelected())
                .regra(envio -> Validacoes.validarObrigatorio(campoDescricaoPlaca.getText()));

        cTipoChave.quando(chaves).regra(envio -> Validacoes.validarObrigatorio(texto(comboTipoChave)));
        cQuantidade.quando(chaves).regra(envio -> {
            if (campoQuantidade.getText().trim().isEmpty()) {
                return Validacoes.MSG_OBRIGATORIO;
            }
            Integer quantidade = Validacoes.converterQuantidade(campoQuantidade.getText());
            if (quantidade == null) {
                return Validacoes.MSG_QUANTIDADE_INVALIDA;
            }
            // FAV-8.4B.2: "Apagar chaves existentes" libera o limite.
            return quantidade > ControladoraSolicitarOrcamento.QUANTIDADE_MAXIMA_CHAVES
                    && !checkApagarChaves.isSelected() ? ControladoraSolicitarOrcamento.MSG_LIMITE_CHAVES : null;
        });
        cTransponder.quando(chaves).regra(envio -> Validacoes.validarTamanho(campoTransponder.getText()));
        cSituacao.quando(chaves).regra(envio -> Validacoes.validarObrigatorio(texto(comboSituacaoImobilizador)));
        BooleanSupplier perda = () -> chaves.getAsBoolean() && checkPerdaTotal.isSelected();
        cDocumentoPropriedade.quando(perda)
                .regra(envio -> Validacoes.validarObrigatorio(campoDocumentoPropriedade.getText()));
        cNomeSolicitante.quando(perda).regra(envio -> Validacoes.validarObrigatorio(campoNomeSolicitante.getText()));
        cDocumentoSolicitante.quando(perda)
                .regra(envio -> Validacoes.validarObrigatorio(campoDocumentoSolicitante.getText()));

        // Serviço e valor só são cobrados ao incluir; no registro, só se há serviço escolhido sem incluir.
        cServico.regra(envio -> null);
        cValorProposto.regra(envio -> {
            String textoValor = campoValorProposto.getText().trim();
            if (textoValor.isEmpty()) {
                return comboServico.getSelectedItem() != null ? ControladoraSolicitarOrcamento.MSG_VALOR_INVALIDO
                        : null;
            }
            Double valor = Validacoes.converterValor(textoValor);
            return valor == null || valor <= 0 ? ControladoraSolicitarOrcamento.MSG_VALOR_INVALIDO : null;
        });
        cObservacoes.regra(envio -> Validacoes.validarTamanho(campoObservacoes.getText()));

        for (Campo campo : new Campo[]{cValidade, cOrigem, cCliente, cVeiculo, cSegmento, cTipoModulo, cModeloECU,
                cIdentificacao, cSintoma, cFoto, cDescricaoPlaca, cTipoChave, cQuantidade, cTransponder, cSituacao,
                cDocumentoPropriedade, cNomeSolicitante, cDocumentoSolicitante, cServico, cValorProposto,
                cObservacoes}) {
            camposFormulario.add(campo);
        }
    }

    private void ligarEventos() {
        // (a) ao perder o foco: valida e envia a mensagem ao Sistema.
        aoPerderFoco(campoValidade, () -> {
            // ACRÉSCIMO (rodada 6): ddmmaaaa vira dd/mm/aaaa.
            Date data = Validacoes.converterData(campoValidade.getText());
            if (data != null) {
                campoValidade.setText(formatoData.format(data));
            }
            cValidade.validar();
        });
        aoPerderFoco(campoCliente, this::aoInformarCliente);
        campoCliente.addActionListener(e -> aoInformarCliente());
        radioPresencial.addActionListener(e -> aoTrocarOrigem());
        radioRemoto.addActionListener(e -> aoTrocarOrigem());
        botaoClienteNaoCadastrado.addActionListener(e -> cadastrarCliente());

        aoPerderFoco(editor(comboVeiculo), this::aoSelecionarVeiculo);
        comboVeiculo.addActionListener(e -> {
            if (!carregando && "comboBoxChanged".equals(e.getActionCommand())) {
                aoSelecionarVeiculo();
            }
        });
        editor(comboVeiculo).addActionListener(e -> aoSelecionarVeiculo());
        botaoVeiculoNaoListado.addActionListener(e -> cadastrarVeiculo());
        botaoHistorico.addActionListener(e -> verHistoricoTecnico());

        radioModulos.addActionListener(e -> aoInformarSegmento());
        radioChaves.addActionListener(e -> aoInformarSegmento());

        // V1
        ligarCombo(comboTipoModulo, cTipoModulo, this::enviarDadosModulo);
        ligarCombo(comboModeloECU, cModeloECU, this::enviarDadosModulo);
        ligarCombo(comboIdentificacaoModulo, cIdentificacao, this::aoEscolherIdentificacao);
        aoPerderFoco(campoSintoma, () -> {
            cSintoma.validar();
            enviarDadosModulo();
        });
        // ACRÉSCIMO (rodada 6): envia os dados do módulo ao parar de digitar.
        enviarAoDigitar(campoSintoma, this::enviarDadosModulo);
        checkIlegivel.addActionListener(e -> aoMarcarIlegivel());
        checkBancada.addActionListener(e -> aoMarcarBancada());
        caixaFoto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                escolherFoto();
            }
        });
        caixaFoto.setTransferHandler(new ReceptorDeFoto());
        aoPerderFoco(campoDescricaoPlaca, () -> {
            cDescricaoPlaca.validar();
            enviarDadosModulo();
        });
        enviarAoDigitar(campoDescricaoPlaca, this::enviarDadosModulo);

        // V2
        ligarCombo(comboTipoChave, cTipoChave, this::enviarDadosChaves);
        aoPerderFoco(campoQuantidade, () -> {
            cQuantidade.validar();
            enviarDadosChaves();
        });
        radioTelecomandoSim.addActionListener(e -> enviarDadosChaves());
        radioTelecomandoNao.addActionListener(e -> enviarDadosChaves());
        aoPerderFoco(campoTransponder, () -> {
            if (cTransponder.validar() == null) {
                enviarDadosChaves();
            }
        });
        ligarCombo(comboSituacaoImobilizador, cSituacao, this::enviarDadosChaves);
        checkPerdaTotal.addActionListener(e -> aoMarcarPerdaTotal());
        checkApagarChaves.addActionListener(e -> {
            cQuantidade.validar();
            enviarDadosChaves();
        });
        aoPerderFoco(campoDocumentoPropriedade, () -> aoInformarDocumento(cDocumentoPropriedade));
        aoPerderFoco(campoNomeSolicitante, () -> aoInformarDocumento(cNomeSolicitante));
        aoPerderFoco(campoDocumentoSolicitante, () -> aoInformarDocumento(cDocumentoSolicitante));

        // Serviços
        comboServico.addActionListener(e -> {
            if (!carregando) {
                cServico.limpar();
                avisoItensMantidos.setVisible(false);
            }
        });
        aoPerderFoco(campoValorProposto, cValorProposto::validar);
        campoValorProposto.addActionListener(e -> incluirServico());
        botaoIncluirServico.addActionListener(e -> incluirServico());
        aoPerderFoco(campoObservacoes, cObservacoes::validar);

        // Rodapé
        botaoDesistir.addActionListener(e -> desistir());
        botaoRascunho.addActionListener(e -> salvarRascunho());
        botaoRegistrar.addActionListener(e -> registrar());
    }

    // =================================================================
    // Fluxo principal
    // =================================================================

    /** Passo 1 (DSS 1): número provisório, data da solicitação e responsável. */
    private void iniciarOrcamento() {
        try {
            limparFormulario();
            Orcamento provisorio = controladora.iniciarOrcamento();
            dataSolicitacao = provisorio.getDataSolicitacao();
            campoNumero.setText(provisorio.getNumero());
            campoDataSolicitacao.setText(formatoData.format(dataSolicitacao));
            campoResponsavel.setText(descreverUsuario(provisorio.getTecnico()));
            campoResponsavel.setCaretPosition(0);
            rotuloSituacao.setText(Orcamento.STATUS_EM_ANALISE.toUpperCase());
            botaoRegistrar.setEnabled(true);
            botaoRascunho.setEnabled(true);
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
            botaoRegistrar.setEnabled(false);
            botaoRascunho.setEnabled(false);
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
            botaoRegistrar.setEnabled(false);
            botaoRascunho.setEnabled(false);
        }
    }

    /** Passos 2 e 3 (DSS 2 e 3): origem + identificação -> dados do cliente e veículos. */
    private void aoInformarCliente() {
        if (popupClientes.isVisible()) {
            return;   // escolha em andamento na lista
        }
        if (carregando || cCliente.validar() != null) {
            return;
        }
        String termo = campoCliente.getText().trim();
        String origem = origemSelecionada();
        String chave = origem + "|" + termo;
        if (chave.equals(chaveBuscaCliente)) {
            return;   // mesma busca: não consulta de novo
        }
        if (origem == null) {
            cOrigem.validar();
            return;
        }
        try {
            Cliente encontrado = controladora.informarCliente(origem, termo);
            chaveBuscaCliente = chave;
            if (encontrado == null) {
                // FA-02: o cliente não tem cadastro.
                exibirCliente(null);
                cCliente.marcar("Cliente não encontrado. Use “+ Cliente não cadastrado”.");
                return;
            }
            exibirCliente(encontrado);
        } catch (IllegalArgumentException erro) {
            chaveBuscaCliente = null;
            cCliente.marcar(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    // -----------------------------------------------------------------
    // Seleção do cliente na lista (busca do mockup)
    // -----------------------------------------------------------------

    private void montarListaDeClientes() {
        popupClientes.setName("popupClientes");
        popupClientes.setFocusable(false);
        popupClientes.setBorder(BorderFactory.createLineBorder(CINZA_BORDA));
        listaClientes.setName("listaClientes");
        listaClientes.setFocusable(false);
        listaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaClientes.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                JLabel rotulo = (JLabel) super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                Cliente cliente = (Cliente) valor;
                String documento = cliente.getCpfCnpj() == null ? "sem CPF/CNPJ"
                        : Validacoes.formatarCpfCnpj(cliente.getCpfCnpj());
                rotulo.setText("<html><b>" + cliente.getNome() + "</b> &nbsp;<font color='#6B7280'>" + documento
                        + " · " + cliente.getTelefone() + "</font></html>");
                rotulo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return rotulo;
            }
        });
        listaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                int indice = listaClientes.locationToIndex(evento.getPoint());
                if (indice >= 0) {
                    escolherCliente(modeloClientes.get(indice));
                }
            }
        });
        JScrollPane rolagem = new JScrollPane(listaClientes);
        rolagem.setBorder(null);
        rolagem.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        popupClientes.add(rolagem);

        campoCliente.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                aoDigitarCliente();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                aoDigitarCliente();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                aoDigitarCliente();
            }
        });
        campoCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evento) {
                if (!popupClientes.isVisible()) {
                    return;
                }
                int indice = listaClientes.getSelectedIndex();
                switch (evento.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_DOWN:
                        listaClientes.setSelectedIndex(Math.min(indice + 1, modeloClientes.size() - 1));
                        listaClientes.ensureIndexIsVisible(listaClientes.getSelectedIndex());
                        evento.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_UP:
                        listaClientes.setSelectedIndex(Math.max(indice - 1, 0));
                        listaClientes.ensureIndexIsVisible(listaClientes.getSelectedIndex());
                        evento.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_ENTER:
                        if (indice >= 0) {
                            escolherCliente(modeloClientes.get(indice));
                            evento.consume();
                        }
                        break;
                    case java.awt.event.KeyEvent.VK_ESCAPE:
                        popupClientes.setVisible(false);
                        evento.consume();
                        break;
                    default:
                        break;
                }
            }
        });
        aoPerderFoco(campoCliente, () -> popupClientes.setVisible(false));
    }

    private void aoDigitarCliente() {
        if (carregando || !campoCliente.isFocusOwner()) {
            return;
        }
        SwingUtilities.invokeLater(() -> mostrarSugestoesDeClientes(campoCliente.getText()));
    }

    /** Lista os clientes cadastrados que combinam com o texto (vazio = todos). */
    void mostrarSugestoesDeClientes(String trecho) {
        try {
            List<Cliente> clientes = controladora.consultarClientes(trecho);
            modeloClientes.clear();
            for (Cliente cliente : clientes) {
                modeloClientes.addElement(cliente);
            }
            if (clientes.isEmpty() || !painelCliente.isShowing()) {
                popupClientes.setVisible(false);
                return;
            }
            listaClientes.setSelectedIndex(0);
            int altura = Math.min(clientes.size(), 7) * 30 + 4;
            int largura = Math.max(painelCliente.getWidth(), listaClientes.getPreferredSize().width + 24);
            popupClientes.setPopupSize(largura, altura);
            popupClientes.show(painelCliente, 0, painelCliente.getHeight());
            campoCliente.requestFocusInWindow();
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** Cliente escolhido na lista: envia informarCliente com o CPF/CNPJ (ou o telefone). */
    void escolherCliente(Cliente cliente) {
        popupClientes.setVisible(false);
        carregando = true;
        campoCliente.setText(cliente.getCpfCnpj() != null ? Validacoes.formatarCpfCnpj(cliente.getCpfCnpj())
                : cliente.getTelefone());
        carregando = false;
        chaveBuscaCliente = null;
        aoInformarCliente();
    }

    // -----------------------------------------------------------------
    // Seleção do módulo do veículo (bloco V1)
    // -----------------------------------------------------------------

    private void carregarModulosDoVeiculo() throws SQLException {
        modulosDoVeiculo.clear();
        if (veiculoAtual != null) {
            modulosDoVeiculo.addAll(controladora.consultarModulosDoVeiculo());
        }
        carregando = true;
        String digitado = texto(comboIdentificacaoModulo);
        comboIdentificacaoModulo.removeAllItems();
        for (ModuloECU modulo : modulosDoVeiculo) {
            comboIdentificacaoModulo.addItem(modulo.getIdentificacaoModulo());
        }
        comboIdentificacaoModulo.setSelectedItem(null);
        editor(comboIdentificacaoModulo).setText(digitado);
        carregando = false;
    }

    /** Módulo já registrado escolhido: preenche tipo e modelo e envia informarDadosModulo (DSS 9). */
    private void aoEscolherIdentificacao() {
        String identificacao = texto(comboIdentificacaoModulo);
        for (ModuloECU modulo : modulosDoVeiculo) {
            if (modulo.getIdentificacaoModulo().equalsIgnoreCase(identificacao)) {
                carregando = true;
                editor(comboTipoModulo).setText(modulo.getTipoModulo());
                editor(comboModeloECU).setText(modulo.getModeloECU() == null ? "" : modulo.getModeloECU());
                editor(comboTipoModulo).setCaretPosition(0);
                editor(comboModeloECU).setCaretPosition(0);
                carregando = false;
                cTipoModulo.validar();
                cModeloECU.validar();
                break;
            }
        }
        enviarDadosModulo();
    }

    /** A origem faz parte de informarCliente: ao mudar, reenvia. */
    private void aoTrocarOrigem() {
        cOrigem.limpar();
        if (!campoCliente.getText().trim().isEmpty()) {
            aoInformarCliente();
        }
    }

    /** Passo 3: "exibe os dados do cliente e a relação de veículos a ele vinculados". */
    private void exibirCliente(Cliente cliente) throws SQLException {
        boolean mesmoCliente = cliente != null && clienteAtual != null
                && cliente.getIdCliente() == clienteAtual.getIdCliente();
        clienteAtual = cliente;
        campoCpfCnpj.setText(cliente == null ? "" : Validacoes.formatarCpfCnpj(cliente.getCpfCnpj()));
        campoTelefone.setText(cliente == null ? "" : cliente.getTelefone());
        if (cliente != null) {
            carregando = true;
            campoCliente.setText(cliente.getNome());
            carregando = false;
            cCliente.limpar();
            chaveBuscaCliente = origemSelecionada() + "|" + cliente.getNome();
        }
        if (mesmoCliente) {
            return;   // a controladora manteve o veículo
        }
        carregando = true;
        comboVeiculo.removeAllItems();
        for (Veiculo veiculo : controladora.consultarVeiculosVinculados()) {
            comboVeiculo.addItem(veiculo.getPlaca());
        }
        comboVeiculo.setSelectedItem(null);
        editor(comboVeiculo).setText("");
        carregando = false;
        exibirVeiculo(null);
    }

    /** Passos 4 e 5 (DSS 4 e 5): placa -> dados do veículo e histórico técnico. */
    private void aoSelecionarVeiculo() {
        if (carregando) {
            return;
        }
        String placa = textoVeiculo();
        if (placa.isEmpty() && veiculoAtual == null) {
            return;
        }
        if (cVeiculo.validar() != null) {
            return;
        }
        placa = Validacoes.normalizarPlaca(placa);
        if (placa.equals(placaSelecionada)) {
            return;
        }
        try {
            Veiculo veiculo = controladora.selecionarVeiculo(placa);
            if (veiculo == null) {
                // FA-04: o veículo não tem cadastro.
                exibirVeiculo(null);
                cVeiculo.marcar("Veículo não encontrado. Use “+ Veículo não listado”.");
                return;
            }
            exibirVeiculo(veiculo);
        } catch (IllegalArgumentException erro) {
            cVeiculo.marcar(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    private void exibirVeiculo(Veiculo veiculo) throws SQLException {
        veiculoAtual = veiculo;
        placaSelecionada = veiculo == null ? null : veiculo.getPlaca();
        campoMarcaModelo.setText(veiculo == null ? "" : veiculo.getMarca() + " " + veiculo.getModelo());
        campoAnoModelo.setText(veiculo == null ? "" : String.valueOf(veiculo.getAnoModelo()));
        campoMotorizacao.setText(veiculo == null ? "" : veiculo.getMotorizacao());
        campoChassi.setText(veiculo == null ? "" : veiculo.getNumeroChassi());
        campoChassi.setToolTipText(veiculo == null ? null : veiculo.getNumeroChassi());
        historicoTecnico.clear();
        ordensDeServicoDoVeiculo.clear();
        if (veiculo == null) {
            carregarModulosDoVeiculo();
            botaoHistorico.setText(" ");
            botaoHistorico.setEnabled(false);
            return;
        }
        carregando = true;
        editor(comboVeiculo).setText(veiculo.getPlaca());
        carregando = false;
        cVeiculo.limpar();
        historicoTecnico.addAll(controladora.consultarHistoricoTecnico());
        ordensDeServicoDoVeiculo.addAll(controladora.consultarOrdensDeServicoDoVeiculo());
        int quantidade = historicoTecnico.size();
        botaoHistorico.setText("<html>" + (quantidade == 0 ? "Nenhum atendimento anterior"
                : "Ver " + quantidade + (quantidade == 1 ? " atendimento anterior" : " atendimentos anteriores"))
                + "</html>");
        botaoHistorico.setEnabled(true);
        carregarModulosDoVeiculo();
        // Passo 8.2: com outro veículo, os atendimentos do módulo mudam.
        if (Segmento.MODULOS.equals(segmentoSelecionado())) {
            enviarDadosModulo();
        }
    }

    /**
     * Passo 5: histórico técnico (orçamentos do veículo, depois as OS). Linhas
     * iguais às do passo 8.2, mais segmento e módulo. Um JLabel por linha, sem HTML,
     * para nada sair cortado.
     */
    private void verHistoricoTecnico() {
        if (veiculoAtual == null || (historicoTecnico.isEmpty() && ordensDeServicoDoVeiculo.isEmpty())) {
            return;
        }
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setOpaque(false);
        adicionarBlocoDoHistorico(corpo, "Orçamentos do veículo", historicoTecnico.stream()
                .map(o -> linhaAtendimento(o) + " · " + descreverSegmento(o)).collect(java.util.stream.Collectors.toList()));
        if (!ordensDeServicoDoVeiculo.isEmpty()) {
            corpo.add(Box.createVerticalStrut(12));
            adicionarBlocoDoHistorico(corpo, "Ordens de Serviço do veículo", ordensDeServicoDoVeiculo);
        }
        mostrarDialogo(Icone.INFO, "Histórico técnico – " + veiculoAtual.getPlaca(), corpo,
                new String[]{"Fechar"}, 0, -1, null);
    }

    private static void adicionarBlocoDoHistorico(JPanel corpo, String titulo, List<String> linhas) {
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 12f));
        rotulo.setForeground(CINZA_ROTULO);
        rotulo.setAlignmentX(LEFT_ALIGNMENT);
        rotulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        corpo.add(rotulo);
        if (linhas.isEmpty()) {
            linhas = java.util.Collections.singletonList("Nenhum.");
        }
        for (String texto : linhas) {
            JLabel linha = new JLabel(texto);
            linha.setFont(linha.getFont().deriveFont(Font.PLAIN, 12f));
            linha.setAlignmentX(LEFT_ALIGNMENT);
            linha.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            corpo.add(linha);
        }
    }

    /** "Módulos MM-88214-C", "Módulos (etiqueta ilegível)" ou "Chaves". */
    private static String descreverSegmento(Orcamento orcamento) {
        String segmento = orcamento.getSegmentoPretendido();
        if (Segmento.MODULOS.equals(segmento)) {
            // O módulo do orçamento vem no Veiculo (OrcamentoDAO, compatível com a 1ª iteração).
            String id = orcamento.getVeiculo() == null ? null : orcamento.getVeiculo().getIdentificacaoModulo();
            return segmento + (id == null || id.isEmpty() ? " (etiqueta ilegível)" : " " + id);
        }
        return segmento == null ? "-" : segmento;
    }

    /** Uma linha de atendimento, igual no histórico técnico e no bloco do módulo. */
    private String linhaAtendimento(Orcamento orcamento) {
        return orcamento.getNumeroOrcamento() + " · "
                + (orcamento.getDataSolicitacao() == null ? "-" : formatoData.format(orcamento.getDataSolicitacao()))
                + " · " + orcamento.getStatus()
                + " · " + Validacoes.formatarMoeda(orcamento.getValorEstimado());
    }

    /** Passos 6 e 7 (DSS 6 e 7): segmento -> bloco técnico e serviços base. */
    private void aoInformarSegmento() {
        if (carregando) {
            return;
        }
        String segmento = segmentoSelecionado();
        cSegmento.limpar();
        try {
            List<ServicoBase> servicos = controladora.informarSegmento(segmento);
            segmentoAceito = segmento;
            dadosModuloEnviados = false;
            dadosChavesEnviados = false;
            exibirServicos(servicos);
            boolean modulos = Segmento.MODULOS.equals(segmento);
            tituloDadosTecnicos.setText(modulos ? "DADOS TÉCNICOS – MÓDULOS (ECU)" : "DADOS TÉCNICOS – CHAVES");
            cartoes.show(painelCartoes, modulos ? CARTAO_MODULOS : CARTAO_CHAVES);
            secaoDadosTecnicos.setVisible(true);
            rotuloCategoriaServicos.setText("Somente serviços base da categoria " + segmento + ".");
            enviarDadosDoBloco();
        } catch (IllegalArgumentException erro) {
            // Recusado: volta à seleção anterior (ou a nenhuma).
            carregando = true;
            if (Segmento.MODULOS.equals(segmentoAceito)) {
                radioModulos.setSelected(true);
            } else if (Segmento.CHAVES.equals(segmentoAceito)) {
                radioChaves.setSelected(true);
            } else {
                grupoSegmento.clearSelection();
            }
            carregando = false;
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    private void exibirServicos(List<ServicoBase> servicos) {
        carregando = true;
        comboServico.removeAllItems();
        for (ServicoBase servico : servicos) {
            comboServico.addItem(servico);
        }
        comboServico.setSelectedItem(null);
        carregando = false;
    }

    // -----------------------------------------------------------------
    // V1 - Módulos (passos 8.1 a 8.3, FAV-8.1 e FAV-8.2)
    // -----------------------------------------------------------------

    /**
     * Passo 8.1: com os campos da RN-04 completos, envia informarDadosModulo (DSS 9) e
     * exibe os atendimentos (8.2). Etiqueta ilegível: DSS variantes 2 e 4.
     */
    private void enviarDadosModulo() {
        if (carregando || !Segmento.MODULOS.equals(segmentoSelecionado())) {
            return;
        }
        try {
            if (checkIlegivel.isSelected()) {
                if (cTipoModulo.valido() && cSintoma.valido()) {
                    // DSS variantes 2 e 4 (FAV-8.1), nessa ordem.
                    controladora.informarIdentificacaoIlegivel(texto(comboTipoModulo), campoSintoma.getText());
                    dadosModuloEnviados = true;
                    if (cFoto.valido() && cDescricaoPlaca.valido()) {
                        exibirAtendimentos(controladora.informarDescricaoModulo(caminhoFoto,
                                campoDescricaoPlaca.getText()), null);
                    }
                }
            } else if (cTipoModulo.valido() && cModeloECU.valido() && cIdentificacao.valido() && cSintoma.valido()) {
                // DSS 9.
                String identificacao = texto(comboIdentificacaoModulo);
                exibirAtendimentos(controladora.informarDadosModulo(texto(comboTipoModulo), texto(comboModeloECU),
                        identificacao, campoSintoma.getText()), identificacao);
                dadosModuloEnviados = true;
            }
            // DSS variantes 6 (FAV-8.2): só depois dos dados do módulo.
            if (dadosModuloEnviados && checkBancada.isSelected()) {
                controladora.informarDiagnosticoPendente();
            }
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    private void exibirAtendimentos(List<Orcamento> atendimentos, String identificacao) {
        if (atendimentos.isEmpty()) {
            caixaAtendimentos.setText(identificacao == null || identificacao.isEmpty()
                    ? "Nenhum atendimento anterior registrado para este módulo neste veículo."
                    : "Nenhum atendimento anterior registrado para o módulo " + identificacao + " neste veículo.");
            return;
        }
        StringBuilder texto = new StringBuilder("<html>");
        for (Orcamento orcamento : atendimentos) {
            texto.append(linhaAtendimento(orcamento)).append("<br>");
        }
        caixaAtendimentos.setText(texto.append("</html>").toString());
    }

    /** FAV-8.1: dispensa modelo e identificação; exige foto e descrição. */
    private void aoMarcarIlegivel() {
        boolean ilegivel = checkIlegivel.isSelected();
        cartaoModelo.show(painelModelo, ilegivel ? "dispensado" : "editavel");
        cartaoIdentificacao.show(painelIdentificacao, ilegivel ? "dispensado" : "editavel");
        cModeloECU.rotuloObrigatorio(!ilegivel);
        cModeloECU.limpar();
        cIdentificacao.limpar();
        alertaIlegivel.setVisible(ilegivel);
        celulaFoto.setVisible(ilegivel);
        linhaDescricaoPlaca.setVisible(ilegivel);
        if (!ilegivel) {
            cFoto.limpar();
            cDescricaoPlaca.limpar();
        }
        caixaAtendimentos.setText(" ");
        revalidate();
        if (ilegivel) {
            enviarDadosModulo();
        } else {
            reenviarDadosTecnicos();
        }
    }

    /** FAV-8.2 (DSS variantes 5 e 6): diagnóstico depende de bancada. */
    private void aoMarcarBancada() {
        boolean pendente = checkBancada.isSelected();
        alertaBancada.setVisible(pendente);
        revalidate();
        if (pendente) {
            // Sem os dados do módulo, a mensagem sai depois deles, em enviarDadosModulo.
            if (dadosModuloEnviados) {
                try {
                    controladora.informarDiagnosticoPendente();
                } catch (IllegalArgumentException erro) {
                    mostrarAviso(erro.getMessage());
                }
            }
        } else {
            reenviarDadosTecnicos();
        }
    }

    /** FAV-8.1.3: "Arraste a foto ou clique para anexar". */
    private void escolherFoto() {
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Registro fotográfico do módulo");
        seletor.setFileFilter(new FileNameExtensionFilter("Imagens (jpg, jpeg, png)", "jpg", "jpeg", "png"));
        if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            anexarFoto(seletor.getSelectedFile());
        }
    }

    private void anexarFoto(File arquivo) {
        String nome = arquivo.getName().toLowerCase();
        if (!nome.endsWith(".jpg") && !nome.endsWith(".jpeg") && !nome.endsWith(".png")) {
            cFoto.marcar("Anexe uma imagem jpg, jpeg ou png.");
            return;
        }
        caminhoFoto = arquivo.getAbsolutePath();
        atualizarCaixaFoto();
        if (cFoto.validar() == null) {
            enviarDadosModulo();
        }
    }

    private void atualizarCaixaFoto() {
        caixaFoto.setText("<html>⬆ Arraste a foto ou clique para anexar"
                + (caminhoFoto == null ? "" : " · <b>" + new File(caminhoFoto).getName() + "</b>") + "</html>");
    }

    // -----------------------------------------------------------------
    // V2 - Chaves (passos 8.4 a 8.6, FAV-8.4A e FAV-8.4B)
    // -----------------------------------------------------------------

    /**
     * Passo 8.4 (DSS 11; variantes 13 e 15): envia os dados das chaves. Retorno: exigência
     * de comprovação (8.5) ou limite do imobilizador (FAV-8.4B).
     */
    private void enviarDadosChaves() {
        if (carregando || !Segmento.CHAVES.equals(segmentoSelecionado())) {
            return;
        }
        Integer quantidade = Validacoes.converterQuantidade(campoQuantidade.getText());
        if (!cTipoChave.valido() || quantidade == null || !cSituacao.valido() || !cTransponder.valido()) {
            return;
        }
        try {
            String resultado = controladora.informarDadosChaves(texto(comboTipoChave), quantidade,
                    radioTelecomandoSim.isSelected(), campoTransponder.getText(),
                    texto(comboSituacaoImobilizador), checkPerdaTotal.isSelected());
            dadosChavesEnviados = true;
            limiteChavesAtingido = ControladoraSolicitarOrcamento.MSG_LIMITE_CHAVES.equals(resultado);
            checkApagarChaves.setVisible(limiteChavesAtingido || checkApagarChaves.isSelected());
            if (limiteChavesAtingido && !checkApagarChaves.isSelected()) {
                cQuantidade.marcar(resultado);              // FAV-8.4B.1
                alertaComprovacao.setVisible(false);
            } else {
                cQuantidade.limpar();
                alertaComprovacao.setVisible(true);         // passo 8.5
            }
            // FAV-8.4A: comprovação depois dos dados das chaves.
            if (checkPerdaTotal.isSelected()) {
                enviarComprovacao(true);
            }
            revalidate();
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        }
    }

    /** FAV-8.4A.1-2: perda total exige documento e solicitante. */
    private void aoMarcarPerdaTotal() {
        boolean perda = checkPerdaTotal.isSelected();
        linhaDocumentos.setVisible(perda);
        if (!perda) {
            cDocumentoPropriedade.limpar();
            cNomeSolicitante.limpar();
            cDocumentoSolicitante.limpar();
            avisoAcrescentado.setVisible(false);
            servicoAcrescentado = null;
        }
        revalidate();
        enviarDadosChaves();
    }

    private void aoInformarDocumento(Campo campo) {
        campo.validar();
        enviarComprovacao(true);
    }

    /**
     * FAV-8.4A.3-5 (DSS variantes 8 e 9): com os três documentos, envia a comprovação e
     * acrescenta o procedimento de apagamento e regravação para o Técnico dar o valor.
     */
    private void enviarComprovacao(boolean destacar) {
        if (!dadosChavesEnviados || !checkPerdaTotal.isSelected() || !cDocumentoPropriedade.valido()
                || !cNomeSolicitante.valido() || !cDocumentoSolicitante.valido()) {
            return;   // DSS: informarDadosChaves vem antes
        }
        try {
            ServicoBase servico = controladora.informarComprovacaoPropriedade(campoDocumentoPropriedade.getText(),
                    campoNomeSolicitante.getText(), campoDocumentoSolicitante.getText());
            boolean novo = servicoAcrescentado == null;
            servicoAcrescentado = servico;
            textoAcrescentado.setText("<html>O procedimento <b>" + servico.getDescricao() + "</b> foi acrescentado "
                    + "automaticamente por causa da perda total. Informe o valor proposto para ele.</html>");   // FAV-8.4A
            avisoAcrescentado.setVisible(true);
            if (destacar && novo && !servicoIncluido(servico)) {
                selecionarServico(servico);
                campoValorProposto.requestFocusInWindow();
            }
            revalidate();
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** Desmarcar opção de variante (sem mensagem no DSS): reenvia o segmento e os dados do bloco. */
    private void reenviarDadosTecnicos() {
        String segmento = segmentoSelecionado();
        if (segmento == null) {
            return;
        }
        try {
            controladora.informarSegmento(segmento);
            dadosModuloEnviados = false;
            dadosChavesEnviados = false;
            enviarDadosDoBloco();
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** Envia o que já estiver preenchido no bloco do segmento. */
    private void enviarDadosDoBloco() {
        if (Segmento.MODULOS.equals(segmentoSelecionado())) {
            enviarDadosModulo();
        } else {
            enviarDadosChaves();
        }
    }

    // -----------------------------------------------------------------
    // Serviços (passos 9 a 11, FA-09A, FA-09B, FA-11A, FA-11B)
    // -----------------------------------------------------------------

    /** Passos 9 e 10 (DSS 12 e 13). */
    private void incluirServico() {
        ServicoBase servico = (ServicoBase) comboServico.getSelectedItem();
        boolean ok = true;
        if (servico == null) {
            cServico.marcar(segmentoSelecionado() == null ? "Selecione o segmento pretendido."
                    : "Selecione um serviço base.");
            ok = false;
        }
        Double valor = Validacoes.converterValor(campoValorProposto.getText());
        if (valor == null || valor <= 0) {
            cValorProposto.marcar(ControladoraSolicitarOrcamento.MSG_VALOR_INVALIDO);   // FA-09A
            ok = false;
        }
        if (!ok) {
            return;
        }
        try {
            double total = controladora.incluirItem(servico.getIdServicoBase(), valor);
            itensTela.add(new Object[]{proximoIdItem++, servico.getDescricao(), servico.getCategoria(), valor});
            atualizarItens(total);
            carregando = true;
            comboServico.setSelectedItem(null);
            carregando = false;
            campoValorProposto.setText("");
            cServico.limpar();
            cValorProposto.limpar();
            avisoItensMantidos.setVisible(false);
        } catch (IllegalArgumentException erro) {
            if (ControladoraSolicitarOrcamento.MSG_VALOR_INVALIDO.equals(erro.getMessage())) {
                cValorProposto.marcar(erro.getMessage());                                 // FA-09A
            } else if (ControladoraSolicitarOrcamento.MSG_FORA_DO_SEGMENTO.equals(erro.getMessage())) {
                cServico.marcar(erro.getMessage());                                       // FA-09B
                avisoItensMantidos.setVisible(true);
            } else {
                cServico.marcar(erro.getMessage());
            }
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    private void aoClicarNaTabela(MouseEvent evento) {
        int linha = tabelaItens.rowAtPoint(evento.getPoint());
        int coluna = tabelaItens.columnAtPoint(evento.getPoint());
        if (linha < 0 || coluna != 3) {
            return;
        }
        Rectangle celula = tabelaItens.getCellRect(linha, coluna, false);
        boolean editar = evento.getX() < celula.x + celula.width / 2;
        Object[] item = itensTela.get(linha);
        if (editar) {
            editarValorItem(item);
        } else {
            removerItem(item);
        }
    }

    /** FA-11A (DSS alternativo 9 e 10): remoção com confirmação. */
    void removerItem(Object[] item) {
        double valor = (Double) item[3];
        JLabel corpo = new JLabel("<html>O serviço <b>" + item[1] + "</b> (" + Validacoes.formatarMoeda(valor)
                + ") será removido.<br>Novo valor estimado: <b><font color='#1D4ED8'>"
                + Validacoes.formatarMoeda(valorEstimado - valor) + "</font></b>.</html>");
        corpo.setFont(corpo.getFont().deriveFont(Font.PLAIN, 13f));
        int escolha = mostrarDialogo(Icone.PERGUNTA, "Remover serviço do orçamento?", corpo,
                new String[]{"Manter", "Remover item"}, -1, 1, null);
        if (escolha != 1) {
            return;
        }
        double total = controladora.removerItem((Integer) item[0]);
        itensTela.remove(item);
        atualizarItens(total);
    }

    /** FA-11B (DSS alternativo 11 e 12): alteração do valor proposto. */
    void editarValorItem(Object[] item) {
        JTextField servico = criarCampoSomenteLeitura("campoServicoEdicao");
        servico.setText((String) item[1]);
        JTextField atual = criarCampoSomenteLeitura("campoValorAtual");
        atual.setText(Validacoes.formatarMoeda((Double) item[3]));
        CampoTexto novo = new CampoTexto("campoNovoValor", "");
        restringir(novo, SO_VALOR, 15, false);
        novo.setText(Validacoes.formatarValorEditavel((Double) item[3]));
        Campo cNovo = new Campo("Novo valor proposto", novo, novo, "");
        cNovo.regra(envio -> {
            Double valor = Validacoes.converterValor(novo.getText());
            return valor == null || valor <= 0 ? ControladoraSolicitarOrcamento.MSG_VALOR_INVALIDO : null;
        });
        aoPerderFoco(novo, cNovo::validar);

        JPanel corpo = criarBloco();
        corpo.add(criarLinha(new JComponent[]{criarCampo("Serviço", false, servico, "")}, new double[]{1}));
        corpo.add(criarLinha(new JComponent[]{criarCampo("Valor atual", false, atual, ""), cNovo.painel(true)},
                new double[]{1, 1}));
        corpo.setPreferredSize(new Dimension(520, corpo.getPreferredSize().height));

        mostrarDialogo(Icone.LAPIS, "Alterar valor proposto", corpo, new String[]{"Cancelar", "Atualizar valor"},
                1, -1, escolha -> {
                    if (escolha != 1) {
                        return true;
                    }
                    if (cNovo.validar() != null) {
                        return false;
                    }
                    try {
                        double valor = Validacoes.converterValor(novo.getText());
                        double total = controladora.alterarValorItem((Integer) item[0], valor);
                        item[3] = valor;
                        atualizarItens(total);
                        return true;
                    } catch (IllegalArgumentException erro) {
                        cNovo.marcar(erro.getMessage());
                        return false;
                    }
                });
    }

    private void atualizarItens(double total) {
        valorEstimado = total;
        modeloItens.setRowCount(0);
        for (Object[] item : itensTela) {
            modeloItens.addRow(new Object[]{item[1], item[2], Validacoes.formatarMoeda((Double) item[3]), ""});
        }
        boolean vazio = itensTela.isEmpty();
        rotuloSemItens.setVisible(vazio);
        tabelaItens.setPreferredSize(new Dimension(10, itensTela.size() * tabelaItens.getRowHeight()));
        rotuloValorEstimado.setText(Validacoes.formatarMoeda(total));
        if (!vazio) {
            erroSemItens.setVisible(false);
        }
        revalidate();
        repaint();
    }

    // -----------------------------------------------------------------
    // Passos 12 e 13: registrar
    // -----------------------------------------------------------------

    /** Passo 12 (DSS 14 e 15): revalida tudo e registra. Passo 13: sucesso. */
    private void registrar() {
        if (!validarFormulario()) {
            return;
        }
        try {
            Orcamento orc = controladora.registrarOrcamento(Validacoes.converterData(campoValidade.getText()),
                    campoObservacoes.getText());
            mostrarOrcamentoRegistrado(orc);
        } catch (IllegalArgumentException erro) {
            String mensagem = erro.getMessage();
            if (ControladoraSolicitarOrcamento.MSG_SEM_ITENS.equals(mensagem)) {
                erroSemItens.setVisible(true);                                   // FA-12A
                rolarAte(erroSemItens);
            } else if (ControladoraSolicitarOrcamento.MSG_VALIDADE_INVALIDA.equals(mensagem)) {
                cValidade.marcar(mensagem);                                       // FA-12C
                rolarAte(cValidade.componente);
            } else if (mensagem.startsWith("Corrija")) {
                exibirPendencias(List.of(mensagem.substring(mensagem.indexOf('\n') + 3).split("\n• ")));
            } else {
                mostrarAviso(mensagem);
            }
        } catch (SQLException erro) {
            mostrarFalhaGravacao(erro);                                           // FA-13
        }
    }

    /** Validação (b): todos os campos. FA-12B na caixa, FA-12C no "Válido até", FA-12A sob a tabela. */
    private boolean validarFormulario() {
        List<String> pendencias = new ArrayList<>();
        JComponent primeiro = null;
        for (Campo campo : camposFormulario) {
            String erro = campo.validar(true);
            if (erro == null) {
                continue;
            }
            if (primeiro == null) {
                primeiro = campo.componente;
            }
            if (campo != cValidade) {
                pendencias.add(campo.textoPendencia(erro));
            }
        }
        boolean semItens = itensTela.isEmpty();
        erroSemItens.setVisible(semItens);
        if (pendencias.isEmpty()) {
            caixaPendencias.setVisible(false);
        } else {
            exibirPendencias(pendencias);
        }
        revalidate();
        if (!pendencias.isEmpty()) {
            rolarAte(caixaPendencias);
        } else if (primeiro != null) {
            rolarAte(primeiro);
        } else if (semItens) {
            rolarAte(erroSemItens);
        }
        return primeiro == null && !semItens;
    }

    private void exibirPendencias(List<String> pendencias) {
        StringBuilder texto = new StringBuilder("<html><b>Corrija " + pendencias.size()
                + (pendencias.size() == 1 ? " pendência" : " pendências") + " antes de registrar:</b>");
        for (String pendencia : pendencias) {
            texto.append("<br>• ").append(pendencia);
        }
        textoPendencias.setText(texto.append("</html>").toString());
        caixaPendencias.setVisible(true);
        revalidate();
        rolarAte(caixaPendencias);
    }

    /** Passo 13 - janela "Orçamento registrado com sucesso" (Tela 2). */
    private void mostrarOrcamentoRegistrado(Orcamento orc) {
        JPanel corpo = new JPanel(new BorderLayout(0, 12));
        corpo.setOpaque(false);
        JLabel mensagem = new JLabel("Orçamento " + orc.getNumero() + " registrado com sucesso.");
        mensagem.setFont(mensagem.getFont().deriveFont(Font.BOLD, 13f));
        corpo.add(mensagem, BorderLayout.NORTH);

        JPanel dados = new JPanel(new GridBagLayout());
        dados.setOpaque(false);
        boolean modulos = Segmento.MODULOS.equals(orc.getSegmentoPretendido());
        String[][] linhas = {
                {"Cliente", orc.getCliente().getNome()},
                {"Veículo", orc.getVeiculo().getPlaca() + " – " + orc.getVeiculo().getMarca() + " "
                        + orc.getVeiculo().getModelo()},
                {"Segmento", modulos ? "Módulos (ECU)" : "Chaves"},
                {"Valor estimado", Validacoes.formatarMoeda(orc.getValorEstimado())},
                {"Válido até", formatoData.format(orc.getDataValidade())},
                {"Situação", orc.getStatus().toUpperCase()}};
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 40);
        for (int i = 0; i < linhas.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            JLabel rotulo = new JLabel(linhas[i][0]);
            rotulo.setFont(rotulo.getFont().deriveFont(Font.PLAIN, 13f));
            dados.add(rotulo, c);
            c.gridx = 1;
            JLabel valor = new JLabel(linhas[i][1]);
            valor.setFont(valor.getFont().deriveFont(Font.PLAIN, 13f));
            if (i == 3) {
                valor.setForeground(AZUL_TITULO);
                valor.setFont(valor.getFont().deriveFont(Font.BOLD));
            }
            if (i == 5) {
                valor.setOpaque(true);
                valor.setBackground(AMARELO_SITUACAO);
                valor.setForeground(LARANJA_SITUACAO);
                valor.setFont(valor.getFont().deriveFont(Font.BOLD, 11f));
                valor.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            }
            dados.add(valor, c);
        }
        corpo.add(dados, BorderLayout.CENTER);

        int escolha = mostrarDialogo(Icone.SUCESSO, "Orçamento registrado com sucesso", corpo,
                new String[]{"Novo orçamento", "Ver orçamento"}, 1, -1, null);
        if (escolha == 1) {
            mostrarDetalhesOrcamento(orc);
        }
        iniciarOrcamento();
    }

    /**
     * "Ver orçamento": o orçamento registrado (passo 13) ou aberto pela listagem. FA-14.1-2:
     * "Em análise" ganha "Cliente recusou" e "Cliente aprovou"; as demais situações, só "Fechar".
     *
     * @return o orçamento com a situação atual (a nova, se a decisão foi registrada).
     */
    private Orcamento mostrarDetalhesOrcamento(Orcamento orc) {
        boolean emAnalise = Orcamento.STATUS_EM_ANALISE.equals(orc.getStatus());
        String[] botoes = emAnalise ? new String[]{"Cliente recusou", "Cliente aprovou", "Fechar"}
                                    : new String[]{"Fechar"};
        int escolha = mostrarDialogo(Icone.INFO, "Orçamento " + valorOuTraco(orc.getNumero()),
                criarCorpoDetalhes(orc), botoes, emAnalise ? 1 : 0, emAnalise ? 0 : -1, null);
        if (emAnalise && (escolha == 0 || escolha == 1)) {
            // FA-14.3
            return registrarDecisaoCliente(orc, escolha == 1 ? Orcamento.STATUS_APROVADO : Orcamento.STATUS_RECUSADO);
        }
        return orc;
    }

    /** FA-14.4 a FA-14.7: confirma, registra e exibe o orçamento com a nova situação. */
    private Orcamento registrarDecisaoCliente(Orcamento orc, String decisao) {
        boolean aprovou = Orcamento.STATUS_APROVADO.equals(decisao);

        // FA-14.4
        JLabel corpo = new JLabel("<html>Cliente: <b>" + orc.getCliente().getNome() + "</b> · Valor estimado: <b>"
                + Validacoes.formatarMoeda(orc.getValorEstimado()) + "</b></html>");
        corpo.setFont(corpo.getFont().deriveFont(Font.PLAIN, 13f));
        String pergunta = String.format(ControladoraSolicitarOrcamento.MSG_CONFIRMAR_DECISAO,
                aprovou ? "APROVOU" : "RECUSOU", orc.getNumero());
        int escolha = mostrarDialogo(Icone.PERGUNTA, pergunta, corpo, new String[]{"Cancelar", "Confirmar"},
                aprovou ? 1 : -1, aprovou ? -1 : 1, null);
        if (escolha != 1) {
            return orc;   // não confirmou: nada gravado
        }
        try {
            // FA-14.5
            Orcamento atualizado = controladora.registrarDecisao(orc.getIdOrcamento(), decisao);

            // FA-14.6: mensagem e o orçamento com a nova situação; FA-14.7 encerra.
            String titulo = String.format(ControladoraSolicitarOrcamento.MSG_DECISAO_REGISTRADA,
                    atualizado.getNumero(), aprovou ? "aprovado" : "recusado");
            mostrarDialogo(aprovou ? Icone.SUCESSO : Icone.CONFIRMADO, titulo, criarCorpoDetalhes(atualizado),
                    new String[]{"Fechar"}, 0, -1, null);
            return atualizado;
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());   // RN-07 (validade vencida) ou situação diferente de "Em análise"
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
        return orc;
    }

    /** Dados do orçamento em tabela (usados pelo "Ver orçamento" e pelo FA-14.6). */
    private JLabel criarCorpoDetalhes(Orcamento orc) {
        StringBuilder texto = new StringBuilder("<html><table cellpadding='2'>");
        linhaDetalhe(texto, "Nº do orçamento", valorOuTraco(orc.getNumero()));
        linhaDetalhe(texto, "Data da solicitação", dataOuTraco(orc.getDataSolicitacao()));
        linhaDetalhe(texto, "Válido até", dataOuTraco(orc.getDataValidade()));
        linhaDetalhe(texto, "Origem do atendimento", valorOuTraco(orc.getOrigemAtendimento()));
        linhaDetalhe(texto, "Responsável", descreverUsuario(orc.getTecnico()));
        linhaDetalhe(texto, "Situação", orc.getStatus().toUpperCase());
        linhaDetalhe(texto, "Cliente", orc.getCliente().getNome());
        linhaDetalhe(texto, "Veículo", orc.getVeiculo().getPlaca() + " – " + orc.getVeiculo().getMarca() + " "
                + orc.getVeiculo().getModelo());
        linhaDetalhe(texto, "Segmento", orc.getSegmentoPretendido());
        for (ItemOrcamento item : orc.getItens()) {
            linhaDetalhe(texto, item.getDescricao(), Validacoes.formatarMoeda(item.getValorProposto()));
        }
        linhaDetalhe(texto, "<b>Valor estimado do orçamento</b>",
                "<b>" + Validacoes.formatarMoeda(orc.getValorEstimado()) + "</b>");
        linhaDetalhe(texto, "Observações", orc.getObservacoes() == null ? "-" : orc.getObservacoes());
        JLabel corpo = new JLabel(texto.append("</table></html>").toString());
        corpo.setFont(corpo.getFont().deriveFont(Font.PLAIN, 12f));
        return corpo;
    }

    /** Rascunhos da listagem não têm número nem validade. */
    private static String valorOuTraco(String valor) {
        return valor == null ? "-" : valor;
    }

    private String dataOuTraco(Date data) {
        return data == null ? "-" : formatoData.format(data);
    }

    private void linhaDetalhe(StringBuilder texto, String rotulo, String valor) {
        texto.append("<tr><td><font color='#6B7280'>").append(rotulo).append("</font></td><td>")
             .append(valor).append("</td></tr>");
    }

    // =================================================================
    // Fluxos alternativos do passo 12 e FA-00
    // =================================================================

    /** FA-12D (DSS alternativo 15 e 16): salvar rascunho. */
    private void salvarRascunho() {
        if (cObservacoes.validar() != null) {
            return;
        }
        try {
            controladora.salvarRascunhoOrcamento(campoObservacoes.getText());
            rotuloSituacao.setText(Orcamento.STATUS_RASCUNHO.toUpperCase());
            JLabel corpo = new JLabel("<html><div style='width:430px'>O orçamento foi salvo como <b>rascunho</b>, "
                    + "sem número definitivo e sem contagem do prazo de validade. Ele pode ser retomado depois "
                    + "pela lista de orçamentos.</div></html>");
            corpo.setFont(corpo.getFont().deriveFont(Font.PLAIN, 13f));
            mostrarDialogo(Icone.CONFIRMADO, ControladoraSolicitarOrcamento.MSG_RASCUNHO, corpo,
                    new String[]{"Ir para a lista de orçamentos"}, 0, -1, null);
            // FA-12D.3: encerra; mostra a lista do FA-00.4.
            mostrarListaOrcamentos(controladora.descartarOrcamento(), null);
            iniciarOrcamento();
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarFalhaGravacao(erro);
        }
    }

    /** FA-00 (DSS alternativo 17 a 21): única forma de fechar a tela; confirmada, mostra a lista e fecha. */
    private void desistir() {
        JLabel corpo = new JLabel("<html>Os dados informados serão descartados e o orçamento <b>não</b> será "
                + "registrado.</html>");
        corpo.setFont(corpo.getFont().deriveFont(Font.PLAIN, 13f));
        int escolha = mostrarDialogo(Icone.PERGUNTA, controladora.solicitarConfirmacaoDesistencia(), corpo,
                new String[]{"Continuar editando", "Desistir"}, -1, 1, null);
        if (escolha != 1) {
            return;   // DSS 21: continuarEditando()
        }
        try {
            List<Orcamento> lista = controladora.descartarOrcamento();
            mostrarListaOrcamentos(lista, "Registro cancelado");
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
        dispose();
    }

    /** FA-00.4 / FA-12D: a listagem de orçamentos. FA-14.1: "Ver orçamento" ou duplo clique abre um deles. */
    private void mostrarListaOrcamentos(List<Orcamento> orcamentos, String aviso) {
        List<Orcamento> linhas = new ArrayList<>(orcamentos);
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"Nº do orçamento", "Data da solicitação",
                "Cliente", "Veículo", "Segmento", "Valor estimado", "Situação"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        for (Orcamento o : orcamentos) {
            modelo.addRow(new Object[]{o.getNumero() == null ? "-" : o.getNumero(),
                    o.getDataSolicitacao() == null ? "-" : formatoData.format(o.getDataSolicitacao()),
                    o.getCliente().getNome(), o.getVeiculo().getPlaca(), o.getSegmentoPretendido(),
                    Validacoes.formatarMoeda(o.getValorEstimado()), o.getStatus()});
        }
        JTable tabela = new JTable(modelo);
        tabela.setName("tabelaOrcamentos");
        tabela.setRowHeight(26);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        // FA-14.1: abre o orçamento da linha; a situação nova volta para a tabela.
        Runnable verOrcamento = () -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                mostrarAviso("Selecione um orçamento na lista.");
                return;
            }
            Orcamento atual = mostrarDetalhesOrcamento(linhas.get(linha));
            linhas.set(linha, atual);
            modelo.setValueAt(atual.getStatus(), linha, 6);
        };
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2 && tabela.rowAtPoint(evento.getPoint()) >= 0) {
                    verOrcamento.run();
                }
            }
        });
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setPreferredSize(new Dimension(820, 260));
        JPanel corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setOpaque(false);
        if (aviso != null) {
            JLabel rotulo = new JLabel(aviso + ".");
            rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 13f));
            corpo.add(rotulo, BorderLayout.NORTH);
        }
        corpo.add(rolagem, BorderLayout.CENTER);
        // "Ver orçamento" não fecha a listagem.
        mostrarDialogo(Icone.INFO, "Orçamentos", corpo, new String[]{"Ver orçamento", "Fechar"}, 1, -1,
                indice -> {
                    if (indice == 0) {
                        verOrcamento.run();
                        return false;
                    }
                    return true;
                });
    }

    // =================================================================
    // FA-02 e FA-04: cadastros rápidos (Tela 2)
    // =================================================================

    /** FA-02 (DSS alternativo 3 e 4): "Cliente não cadastrado". */
    private void cadastrarCliente() {
        // DSS: informarCliente (que guarda a origem) vem antes de cadastrarCliente.
        if (origemSelecionada() == null) {
            cOrigem.validar();
            return;
        }
        if (campoCliente.getText().trim().isEmpty()) {
            cCliente.marcar("Busque o cliente pelo nome ou telefone antes de cadastrá-lo.");
            return;
        }
        aoInformarCliente();
        if (clienteAtual != null || cCliente.mensagemAtual() != null
                && !cCliente.mensagemAtual().startsWith("Cliente não encontrado")) {
            return;   // já cadastrado ou busca inválida
        }
        CampoTexto nome = new CampoTexto("campoNomeCliente", "");
        CampoTexto cpfCnpj = new CampoTexto("campoCpfCnpjCliente", "");
        restringir(cpfCnpj, SO_DOCUMENTO, 18, false);
        CampoTexto telefone = new CampoTexto("campoTelefoneCliente", "");
        restringir(telefone, SO_TELEFONE, 15, false);
        Campo cNome = new Campo("Nome", nome, nome, "");
        Campo cCpfCnpj = new Campo("CPF / CNPJ", cpfCnpj, cpfCnpj, "");
        Campo cTelefone = new Campo("Telefone / WhatsApp", telefone, telefone, "");
        cNome.regra(envio -> Validacoes.validarObrigatorio(nome.getText()));
        cCpfCnpj.regra(envio -> validarCpfCnpjOpcional(cpfCnpj.getText()));
        cTelefone.regra(envio -> Validacoes.validarTelefone(telefone.getText()));
        aoPerderFoco(nome, cNome::validar);
        aoPerderFoco(cpfCnpj, () -> {
            // Só dígitos vira 000.000.000-00 / 00.000.000/0000-00.
            if (cCpfCnpj.validar() == null && !cpfCnpj.getText().isBlank()) {
                cpfCnpj.setText(Validacoes.formatarCpfCnpj(cpfCnpj.getText()));
            }
        });
        aoPerderFoco(telefone, cTelefone::validar);
        // Pré-preenche com o texto buscado.
        String buscado = campoCliente.getText().trim();
        if (!buscado.isEmpty() && Validacoes.validarTelefone(buscado) == null) {
            telefone.setText(Validacoes.formatarTelefone(buscado));
        } else if (!buscado.isEmpty() && buscado.matches("[0-9./\\-]+")
                && validarCpfCnpjOpcional(buscado) == null) {
            cpfCnpj.setText(Validacoes.formatarCpfCnpj(buscado));   // buscou por CPF/CNPJ não cadastrado
        } else if (!buscado.isEmpty() && !buscado.matches("[0-9./\\-()\\s+]+")) {
            nome.setText(buscado);
        }

        JPanel corpo = criarBloco();
        corpo.add(criarLinha(new JComponent[]{cNome.painel(true)}, new double[]{1}));
        corpo.add(criarLinha(new JComponent[]{cCpfCnpj.painel(false), cTelefone.painel(true)}, new double[]{1, 1}));
        JLabel nota = criarDica("Após salvar, o sistema exibe o cliente sem veículos vinculados e abre o cadastro "
                + "de veículo.");
        nota.setAlignmentX(LEFT_ALIGNMENT);
        corpo.add(nota);
        corpo.setPreferredSize(new Dimension(540, corpo.getPreferredSize().height + 8));

        int escolha = mostrarDialogo(Icone.MAIS, "Cliente não cadastrado", corpo,
                new String[]{"Cancelar", "Salvar cliente"}, 1, -1, opcao -> {
                    if (opcao != 1) {
                        return true;
                    }
                    boolean valido = cNome.validar() == null & cCpfCnpj.validar() == null
                            & cTelefone.validar() == null;
                    if (!valido) {
                        return false;
                    }
                    try {
                        Cliente cliente = controladora.cadastrarCliente(nome.getText().trim(),
                                Validacoes.somenteDigitos(cpfCnpj.getText()),
                                Validacoes.formatarTelefone(telefone.getText()));
                        exibirCliente(cliente);
                        return true;
                    } catch (IllegalArgumentException erro) {
                        (erro.getMessage().contains("CPF") ? cCpfCnpj : cNome).marcar(erro.getMessage());
                        return false;
                    } catch (SQLException erro) {
                        mostrarErroDeBanco(erro);
                        return false;
                    }
                });
        if (escolha == 1) {
            // FA-02.4-5: sem veículos, segue para o FA-04.
            cadastrarVeiculo();
        }
    }

    /** CPF/CNPJ do cadastro rápido: opcional; se informado, 11 ou 14 dígitos com verificadores corretos. */
    private static String validarCpfCnpjOpcional(String texto) {
        String d = Validacoes.somenteDigitos(texto);
        if (d.isEmpty()) {
            return null;
        }
        if (d.length() == 11) {
            return Validacoes.cpfValido(d) ? null : Validacoes.MSG_CPF_INVALIDO;
        }
        if (d.length() == 14) {
            return Validacoes.cnpjValido(d) ? null : Validacoes.MSG_CNPJ_INVALIDO;
        }
        return "Informe o CPF (11 dígitos) ou o CNPJ (14 dígitos).";
    }

    /** FA-04 (DSS alternativo 5 e 6): "Veículo não listado". */
    private void cadastrarVeiculo() {
        if (clienteAtual == null) {
            mostrarAviso("Identifique o cliente antes de cadastrar o veículo.");
            return;
        }
        CampoTexto placa = new CampoTexto("campoPlacaVeiculo", "");
        restringir(placa, SO_PLACA, 8, true);
        JComboBox<String> marca = criarComboEditavel("comboMarcaVeiculo", MARCAS, "Selecione…");
        CampoTexto modelo = new CampoTexto("campoModeloVeiculo", "");
        CampoTexto ano = new CampoTexto("campoAnoVeiculo", "");
        restringir(ano, SO_DIGITOS, 4, false);
        CampoTexto motorizacao = new CampoTexto("campoMotorizacaoVeiculo", "");
        CampoTexto chassi = new CampoTexto("campoChassiVeiculo", "");
        restringir(chassi, SO_LETRAS_E_DIGITOS, 17, true);
        String placaDigitada = textoVeiculo();
        if (Validacoes.validarPlaca(placaDigitada) == null) {
            placa.setText(Validacoes.normalizarPlaca(placaDigitada));
        }

        Campo cPlaca = new Campo("Placa", placa, placa, "");
        Campo cMarca = new Campo("Marca", marca, marca, "");
        Campo cModelo = new Campo("Modelo", modelo, modelo, "");
        Campo cAno = new Campo("Ano-modelo", ano, ano, "");
        Campo cMotorizacao = new Campo("Motorização", motorizacao, motorizacao, "");
        Campo cChassi = new Campo("Nº do chassi", chassi, chassi, "");
        cPlaca.regra(envio -> Validacoes.validarPlaca(placa.getText()));
        cMarca.regra(envio -> Validacoes.validarObrigatorio(texto(marca)));
        cModelo.regra(envio -> Validacoes.validarObrigatorio(modelo.getText()));
        cAno.regra(envio -> Validacoes.validarAnoModelo(ano.getText()));
        cMotorizacao.regra(envio -> Validacoes.validarObrigatorio(motorizacao.getText()));
        cChassi.regra(envio -> Validacoes.validarChassi(chassi.getText()));
        Campo[] campos = {cPlaca, cMarca, cModelo, cAno, cMotorizacao, cChassi};
        for (Campo campo : campos) {
            JComponent alvo = campo.componente instanceof JComboBox
                    ? editor((JComboBox<?>) campo.componente) : campo.componente;
            aoPerderFoco(alvo, campo::validar);
        }

        JPanel corpo = criarBloco();
        corpo.add(criarLinha(new JComponent[]{cPlaca.painel(true), cMarca.painel(true), cModelo.painel(true)},
                new double[]{1, 1, 1}));
        corpo.add(criarLinha(new JComponent[]{cAno.painel(true), cMotorizacao.painel(true), cChassi.painel(true)},
                new double[]{0.8, 0.8, 1.4}));
        JLabel nota = criarDica("O veículo é vinculado ao cliente selecionado.");
        nota.setAlignmentX(LEFT_ALIGNMENT);
        corpo.add(nota);
        corpo.setPreferredSize(new Dimension(600, corpo.getPreferredSize().height + 8));

        mostrarDialogo(Icone.MAIS, "Veículo não listado", corpo, new String[]{"Cancelar", "Salvar e vincular"}, 1, -1,
                opcao -> {
                    if (opcao != 1) {
                        return true;
                    }
                    boolean valido = true;
                    for (Campo campo : campos) {
                        valido &= campo.validar() == null;
                    }
                    if (!valido) {
                        return false;
                    }
                    try {
                        Veiculo veiculo = controladora.cadastrarVeiculo(Validacoes.normalizarPlaca(placa.getText()),
                                texto(marca), modelo.getText().trim(), Integer.parseInt(ano.getText().trim()),
                                motorizacao.getText().trim(), chassi.getText().trim().toUpperCase());
                        carregando = true;
                        comboVeiculo.addItem(veiculo.getPlaca());
                        comboVeiculo.setSelectedItem(veiculo.getPlaca());
                        carregando = false;
                        exibirVeiculo(veiculo);
                        return true;
                    } catch (IllegalArgumentException erro) {
                        (erro.getMessage().contains("chassi") ? cChassi : cPlaca).marcar(erro.getMessage());
                        return false;
                    } catch (SQLException erro) {
                        mostrarErroDeBanco(erro);
                        return false;
                    }
                });
    }

    // =================================================================
    // Leitura dos campos e apoio
    // =================================================================

    private String origemSelecionada() {
        if (radioPresencial.isSelected()) {
            return ControladoraSolicitarOrcamento.ORIGEM_PRESENCIAL;
        }
        return radioRemoto.isSelected() ? ControladoraSolicitarOrcamento.ORIGEM_REMOTO : null;
    }

    private String segmentoSelecionado() {
        if (radioModulos.isSelected()) {
            return Segmento.MODULOS;
        }
        return radioChaves.isSelected() ? Segmento.CHAVES : null;
    }

    private String textoVeiculo() {
        return editor(comboVeiculo).getText().trim();
    }

    private boolean servicoIncluido(ServicoBase servico) {
        for (Object[] item : itensTela) {
            if (servico.getDescricao().equals(item[1])) {
                return true;
            }
        }
        return false;
    }

    private void selecionarServico(ServicoBase servico) {
        for (int i = 0; i < comboServico.getItemCount(); i++) {
            if (comboServico.getItemAt(i).getIdServicoBase() == servico.getIdServicoBase()) {
                comboServico.setSelectedIndex(i);
                return;
            }
        }
    }

    private static String descreverUsuario(Usuario usuario) {
        if (usuario == null) {
            return "";
        }
        if (usuario.getTipoUsuario() == TipoUsuario.tecnicoDeModulos) {
            return usuario.getNome() + " – Técnico de Módulos";
        }
        if (usuario.getTipoUsuario() == TipoUsuario.tecnicoDeChaves) {
            return usuario.getNome() + " – Técnico de Chaves";
        }
        return usuario.getNome() + " – Administrador";
    }

    /** Volta o formulário ao estado do passo 1. */
    private void limparFormulario() {
        carregando = true;
        clienteAtual = null;
        veiculoAtual = null;
        chaveBuscaCliente = null;
        placaSelecionada = null;
        servicoAcrescentado = null;
        segmentoAceito = null;
        dadosModuloEnviados = false;
        dadosChavesEnviados = false;
        limiteChavesAtingido = false;
        caminhoFoto = null;
        proximoIdItem = 1;
        itensTela.clear();
        historicoTecnico.clear();
        ordensDeServicoDoVeiculo.clear();
        for (JTextField campo : new JTextField[]{campoValidade, campoCliente, campoCpfCnpj, campoTelefone,
                campoMarcaModelo, campoAnoModelo, campoMotorizacao, campoChassi, campoSintoma,
                campoDescricaoPlaca, campoQuantidade, campoTransponder, campoDocumentoPropriedade, campoNomeSolicitante,
                campoDocumentoSolicitante, campoValorProposto, campoObservacoes}) {
            campo.setText("");
        }
        grupoOrigem.clearSelection();
        grupoSegmento.clearSelection();
        radioTelecomandoNao.setSelected(true);
        comboVeiculo.removeAllItems();
        editor(comboVeiculo).setText("");
        modulosDoVeiculo.clear();
        comboIdentificacaoModulo.removeAllItems();
        popupClientes.setVisible(false);
        for (JComboBox<?> combo : new JComboBox<?>[]{comboTipoModulo, comboModeloECU, comboTipoChave,
                comboSituacaoImobilizador, comboIdentificacaoModulo}) {
            combo.setSelectedItem(null);
            editor(combo).setText("");
        }
        comboServico.removeAllItems();
        for (JCheckBox check : new JCheckBox[]{checkIlegivel, checkBancada, checkPerdaTotal, checkApagarChaves}) {
            check.setSelected(false);
        }
        carregando = false;

        cartaoModelo.show(painelModelo, "editavel");
        cartaoIdentificacao.show(painelIdentificacao, "editavel");
        cModeloECU.rotuloObrigatorio(true);
        for (JComponent componente : new JComponent[]{secaoDadosTecnicos, alertaIlegivel, celulaFoto,
                linhaDescricaoPlaca, alertaBancada, checkApagarChaves, alertaComprovacao, linhaDocumentos,
                avisoAcrescentado, erroSemItens, caixaPendencias, avisoItensMantidos}) {
            componente.setVisible(false);
        }
        for (Campo campo : camposFormulario) {
            campo.limpar();
        }
        caixaAtendimentos.setText(" ");
        atualizarCaixaFoto();
        botaoHistorico.setText(" ");
        botaoHistorico.setEnabled(false);
        rotuloCategoriaServicos.setText(" ");
        atualizarItens(0);
        rolarAte(campoNumero);
    }

    private void rolarAte(JComponent componente) {
        SwingUtilities.invokeLater(() -> {
            Rectangle area = SwingUtilities.convertRectangle(componente.getParent(), componente.getBounds(), conteudo);
            area.y = Math.max(0, area.y - 40);
            area.height += 80;
            conteudo.scrollRectToVisible(area);
        });
    }

    private void mostrarAviso(String mensagem) {
        JLabel corpo = new JLabel("<html><div style='width:400px'>" + mensagem.replace("\n", "<br>")
                + "</div></html>");
        corpo.setFont(corpo.getFont().deriveFont(Font.PLAIN, 13f));
        mostrarDialogo(Icone.ALERTA, "Atenção", corpo, new String[]{"OK"}, 0, -1, null);
    }

    /** FA-13 - janela "Erro ao registrar" (Tela 2). */
    private void mostrarFalhaGravacao(SQLException erro) {
        System.err.println("Falha ao gravar o orçamento: " + erro.getMessage());
        JPanel corpo = new JPanel(new BorderLayout(0, 4));
        corpo.setOpaque(false);
        JLabel mensagem = new JLabel(ControladoraSolicitarOrcamento.MSG_FALHA_GRAVACAO);
        mensagem.setFont(mensagem.getFont().deriveFont(Font.PLAIN, 13f));
        corpo.add(mensagem, BorderLayout.NORTH);
        corpo.add(criarDica("Os dados informados foram mantidos no formulário."), BorderLayout.CENTER);
        mostrarDialogo(Icone.ERRO, "Erro ao registrar", corpo, new String[]{"Voltar ao formulário"}, 0, -1, null);
    }

    /** Falha de banco fora do registro (consultas e cadastros rápidos). */
    private void mostrarErroDeBanco(SQLException erro) {
        System.err.println("Falha de acesso ao banco: " + erro.getMessage());
        JLabel corpo = new JLabel("<html><div style='width:400px'>Não foi possível acessar a base de dados. "
                + "Tente novamente.</div></html>");
        corpo.setFont(corpo.getFont().deriveFont(Font.PLAIN, 13f));
        mostrarDialogo(Icone.ERRO, "Erro", corpo, new String[]{"OK"}, 0, -1, null);
    }

    // =================================================================
    // Janelas no estilo da Tela 2 do mockup
    // =================================================================

    private enum Icone { SUCESSO, CONFIRMADO, MAIS, PERGUNTA, ALERTA, ERRO, LAPIS, INFO }

    /**
     * Janela modal com ícone, título, corpo e botões no rodapé.
     *
     * @param primario índice do botão azul (ou -1)
     * @param perigo   índice do botão vermelho (ou -1)
     * @param aoClicar decide se a janela fecha com o botão clicado (null = fecha sempre)
     * @return o índice do botão que fechou a janela, ou -1 se fechada pelo "X"
     */
    private int mostrarDialogo(Icone icone, String titulo, JComponent corpo, String[] botoes, int primario,
                               int perigo, Function<Integer, Boolean> aoClicar) {
        JDialog dialogo = new JDialog(this, "Solicitar Orçamento", true);
        dialogo.setName("dialogo:" + titulo);
        int[] escolha = {-1};

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        topo.setBackground(Color.WHITE);
        topo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CINZA_LINHA));
        topo.add(new JLabel(criarIcone(icone)));
        JLabel rotuloTitulo = new JLabel(titulo);
        rotuloTitulo.setFont(rotuloTitulo.getFont().deriveFont(Font.BOLD, 15f));
        rotuloTitulo.setForeground(CINZA_ROTULO);
        topo.add(rotuloTitulo);

        JPanel meio = new JPanel(new BorderLayout());
        meio.setBackground(Color.WHITE);
        meio.setBorder(BorderFactory.createEmptyBorder(16, 22, 18, 22));
        meio.add(corpo, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        rodape.setBackground(CINZA_TABELA);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CINZA_LINHA));
        for (int i = 0; i < botoes.length; i++) {
            JButton botao = i == primario ? criarBotaoPrimario(botoes[i])
                    : i == perigo ? criarBotaoPerigo(botoes[i]) : criarBotaoContorno(botoes[i]);
            botao.setName("botao:" + botoes[i]);
            if (i != primario && i != perigo) {
                botao.setForeground(CINZA_ROTULO);
            }
            int indice = i;
            botao.addActionListener(e -> {
                if (aoClicar == null || aoClicar.apply(indice)) {
                    escolha[0] = indice;
                    dialogo.dispose();
                }
            });
            rodape.add(botao);
            if (i == botoes.length - 1) {
                dialogo.getRootPane().setDefaultButton(botao);
            }
        }

        JPanel cartao = new JPanel(new BorderLayout());
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(meio, BorderLayout.CENTER);
        cartao.add(rodape, BorderLayout.SOUTH);
        dialogo.setContentPane(cartao);
        // setResizable antes do pack: depois dele, alguns gerenciadores Linux cortam o conteúdo (rodada 6).
        dialogo.setResizable(false);
        dialogo.pack();
        dialogo.setMinimumSize(new Dimension(Math.max(560, dialogo.getWidth()), dialogo.getHeight()));
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
        return escolha[0];
    }

    private Icon criarIcone(Icone icone) {
        switch (icone) {
            case SUCESSO:
                return new IconeCirculo('✓', VERDE_ICONE, true, 30);
            case CONFIRMADO:
                return new IconeCirculo('✓', AZUL_TITULO, true, 30);
            case MAIS:
                return new IconeCirculo('+', AZUL_TITULO, true, 30);
            case PERGUNTA:
                return new IconeCirculo('?', AMBAR_ICONE, true, 30);
            case ERRO:
                return new IconeCirculo('!', VERMELHO, true, 30);
            case LAPIS:
                return new IconeCirculo('✎', AZUL_TITULO, true, 30);
            case ALERTA:
                return new IconeCirculo('!', AMBAR_ICONE, true, 30);
            default:
                return new IconeCirculo('i', AZUL_TITULO, true, 30);
        }
    }

    // =================================================================
    // Construção dos componentes visuais
    // =================================================================

    private JPanel criarSecao(String titulo, JLabel direita) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        JLabel rotulo = new JLabel(titulo);
        configurarSecao(painel, rotulo, direita);
        return painel;
    }

    /** Cartão branco com título azul e linha, como as seções do mockup. */
    private void configurarSecao(JPanel painel, JLabel rotulo, JLabel direita) {
        painel.setBackground(Color.WHITE);
        painel.setAlignmentX(LEFT_ALIGNMENT);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CINZA_LINHA),
                BorderFactory.createEmptyBorder(14, 20, 16, 20)));

        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 13f));
        rotulo.setForeground(AZUL_TITULO);
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.setAlignmentX(LEFT_ALIGNMENT);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CINZA_LINHA),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));
        if (rotulo.getText() != null && rotulo.getText().startsWith("SEGMENTO")) {
            rotulo.setText("<html>" + rotulo.getText() + " <font color='#DC2626'>*</font></html>");
        }
        cabecalho.add(rotulo, BorderLayout.WEST);
        if (direita != null) {
            direita.setFont(direita.getFont().deriveFont(Font.PLAIN, 11f));
            direita.setForeground(CINZA_DICA);
            cabecalho.add(direita, BorderLayout.EAST);
        }
        cabecalho.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        painel.add(cabecalho);
        painel.add(Box.createVerticalStrut(10));
    }

    private JPanel criarBloco() {
        JPanel bloco = new JPanel();
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setOpaque(false);
        bloco.setAlignmentX(LEFT_ALIGNMENT);
        return bloco;
    }

    /** Campo sem validação: rótulo em cima, componente, dica embaixo. */
    private JPanel criarCampo(String rotulo, boolean obrigatorio, JComponent componente, String dica) {
        JPanel painel = new JPanel(new BorderLayout(0, 5));
        painel.setOpaque(false);
        JLabel label = new JLabel(obrigatorio ? rotuloComAsterisco(rotulo) : rotulo);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setForeground(CINZA_ROTULO);
        // Botões ficam na largura do texto; campos dividem a linha.
        componente.setPreferredSize(new Dimension(componente instanceof AbstractButton
                ? componente.getPreferredSize().width : 10, 36));
        componente.setMinimumSize(componente.getPreferredSize());
        painel.add(label, BorderLayout.NORTH);
        painel.add(componente, BorderLayout.CENTER);
        JLabel textoDica = criarDica(dica);
        painel.add(textoDica, BorderLayout.SOUTH);
        return painel;
    }

    private JLabel criarDica(String texto) {
        JLabel dica = new TextoQuebravel(texto == null || texto.isEmpty() ? " " : "<html>" + texto + "</html>");
        dica.setFont(dica.getFont().deriveFont(Font.PLAIN, 10.5f));
        dica.setForeground(CINZA_DICA);
        return dica;
    }

    private static String rotuloComAsterisco(String rotulo) {
        return "<html>" + rotulo + " <font color='#DC2626'>*</font></html>";
    }

    /** Linha de campos, dividindo a largura conforme os pesos. */
    private JPanel criarLinha(JComponent[] campos, double[] pesos) {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);
        painel.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.gridy = 0;
        for (int i = 0; i < campos.length; i++) {
            c.gridx = i;
            c.weightx = pesos[i];
            c.insets = new Insets(4, 0, 6, i == campos.length - 1 ? 0 : 16);
            painel.add(campos[i], c);
        }
        return painel;
    }

    private static Border bordaCampo(Color cor) {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cor),
                BorderFactory.createEmptyBorder(0, 10, 0, 10));
    }

    private static JTextField criarCampoSomenteLeitura(String nome) {
        // Mostra o começo do texto.
        JTextField campo = new JTextField() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setText(String texto) {
                super.setText(texto);
                setCaretPosition(0);
            }
        };
        campo.setName(nome);
        campo.setEditable(false);
        campo.setFocusable(false);
        campo.setBackground(CINZA_SOMENTE_LEITURA);
        campo.setForeground(CINZA_ROTULO);
        campo.setFont(campo.getFont().deriveFont(Font.BOLD, 13f));
        campo.setBorder(bordaCampo(CINZA_LINHA));
        return campo;
    }

    private static JRadioButton criarRadio(String texto, String nome) {
        JRadioButton radio = new JRadioButton(texto);
        radio.setName(nome);
        radio.setOpaque(false);
        radio.setFont(radio.getFont().deriveFont(Font.PLAIN, 13f));
        return radio;
    }

    private static JCheckBox criarCheck(String texto, String nome) {
        JCheckBox check = new JCheckBox(texto);
        check.setName(nome);
        check.setOpaque(false);
        check.setFont(check.getFont().deriveFont(Font.PLAIN, 13f));
        return check;
    }

    private static JComboBox<String> criarComboEditavel(String nome, String[] opcoes, String placeholder) {
        JComboBox<String> combo = new JComboBox<>(opcoes);
        combo.setName(nome);
        combo.setEditable(true);
        combo.setEditor(new EditorComPlaceholder(nome, placeholder));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(CINZA_BORDA));
        combo.setSelectedItem(null);
        return combo;
    }

    private static JTextField editor(JComboBox<?> combo) {
        return (JTextField) combo.getEditor().getEditorComponent();
    }

    private static String texto(JComboBox<String> combo) {
        return editor(combo).getText().trim();
    }

    private static JButton criarBotaoPrimario(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(AZUL_CABECALHO);
        botao.setForeground(Color.WHITE);
        estilizarBotao(botao, AZUL_CABECALHO);
        return botao;
    }

    private static JButton criarBotaoPerigo(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(VERMELHO);
        botao.setForeground(Color.WHITE);
        estilizarBotao(botao, VERMELHO);
        return botao;
    }

    private static JButton criarBotaoContorno(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(Color.WHITE);
        botao.setForeground(AZUL_TITULO);
        estilizarBotao(botao, AZUL_CABECALHO);
        return botao;
    }

    private static void estilizarBotao(AbstractButton botao, Color borda) {
        botao.setFont(botao.getFont().deriveFont(Font.BOLD, 13f));
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);
        botao.setOpaque(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borda),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)));
    }

    private JPanel criarAlertaAmarelo(String destaque, String texto) {
        return criarAlerta(new IconeCirculo('!', AMARELO_TEXTO, false, 18), AMARELO_FUNDO, AMARELO_BORDA, AMARELO_TEXTO,
                "<html>" + (destaque.isEmpty() ? "" : "<b>" + destaque + "</b>") + texto + "</html>");
    }

    private JPanel criarAlertaInfo(String texto) {
        return criarAlerta(new IconeCirculo('i', AZUL_TITULO, false, 18), AZUL_CLARO, AZUL_BORDA_CLARA, AZUL_TITULO,
                texto.startsWith("<html>") ? texto : "<html>" + texto + "</html>");
    }

    private JPanel criarAlertaErro(String texto) {
        return criarAlerta(new IconeCirculo('!', VERMELHO, false, 18), VERMELHO_FUNDO, VERMELHO_BORDA, VERMELHO,
                "<html>" + texto + "</html>");
    }

    private JPanel criarAlerta(Icon icone, Color fundo, Color borda, Color cor, String html) {
        JPanel alerta = new JPanel(new BorderLayout(10, 0));
        alerta.setBackground(fundo);
        alerta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borda),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        alerta.setAlignmentX(LEFT_ALIGNMENT);
        JLabel rotuloIcone = new JLabel(icone);
        rotuloIcone.setVerticalAlignment(SwingConstants.TOP);
        JLabel texto = new TextoQuebravel(html);
        texto.setForeground(cor);
        texto.setFont(texto.getFont().deriveFont(Font.PLAIN, 12f));
        alerta.add(rotuloIcone, BorderLayout.WEST);
        alerta.add(texto, BorderLayout.CENTER);
        alerta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        return alerta;
    }

    // =================================================================
    // Restrição de digitação: campos numéricos não aceitam letras
    // =================================================================

    private static final String SO_DIGITOS = "[0-9]";
    private static final String SO_DATA = "[0-9/]";
    private static final String SO_VALOR = "[0-9.,]";
    private static final String SO_TELEFONE = "[0-9()\\- ]";
    private static final String SO_DOCUMENTO = "[0-9./\\-]";
    private static final String SO_PLACA = "[A-Za-z0-9\\-]";
    private static final String SO_LETRAS_E_DIGITOS = "[A-Za-z0-9]";

    /** Campos do formulário principal que só aceitam certos caracteres. */
    private void restringirDigitacao() {
        restringir(campoValidade, SO_DATA, 10, false);
        restringir(campoQuantidade, SO_DIGITOS, 3, false);
        restringir(campoValorProposto, SO_VALOR, 15, false);
        restringir(editor(comboVeiculo), SO_PLACA, 8, true);
    }

    /** Descarta, ao digitar ou colar, caracteres fora do padrão e o que passar do máximo. */
    private static void restringir(javax.swing.text.JTextComponent campo, String permitido, int maximo,
                                   boolean maiusculas) {
        ((javax.swing.text.AbstractDocument) campo.getDocument()).setDocumentFilter(
                new javax.swing.text.DocumentFilter() {
                    @Override
                    public void insertString(FilterBypass fb, int posicao, String texto,
                                             javax.swing.text.AttributeSet atributos)
                            throws javax.swing.text.BadLocationException {
                        replace(fb, posicao, 0, texto, atributos);
                    }

                    @Override
                    public void replace(FilterBypass fb, int posicao, int tamanho, String texto,
                                        javax.swing.text.AttributeSet atributos)
                            throws javax.swing.text.BadLocationException {
                        StringBuilder aceito = new StringBuilder();
                        if (texto != null) {
                            for (char c : texto.toCharArray()) {
                                String caractere = String.valueOf(c);
                                if (caractere.matches(permitido)) {
                                    aceito.append(maiusculas ? caractere.toUpperCase() : caractere);
                                }
                            }
                        }
                        int livre = maximo - (fb.getDocument().getLength() - tamanho);
                        if (aceito.length() > livre) {
                            aceito.setLength(Math.max(0, livre));
                        }
                        if (aceito.length() == 0 && tamanho == 0) {
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            return;
                        }
                        super.replace(fb, posicao, tamanho, aceito.toString(), atributos);
                    }
                });
    }

    /** ACRÉSCIMO (rodada 6): roda a ação 600 ms depois da última tecla. */
    private void enviarAoDigitar(javax.swing.text.JTextComponent campo, Runnable acao) {
        javax.swing.Timer espera = new javax.swing.Timer(600, e -> {
            if (campo.isShowing() && !campo.getText().trim().isEmpty()) {
                acao.run();
            }
        });
        espera.setRepeats(false);
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!carregando) {
                    espera.restart();
                }
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (!carregando) {
                    espera.restart();
                }
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                // só estilo; o texto não muda
            }
        });
    }

    /** ACRÉSCIMO (rodada 6): exemplo de data (hoje + 15 dias), com e sem barras. */
    private String exemploDeData() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.add(java.util.Calendar.DAY_OF_MONTH, 15);
        String data = formatoData.format(c.getTime());
        return data + " ou " + data.replace("/", "");
    }

    private static void aoPerderFoco(JComponent componente, Runnable acao) {
        componente.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evento) {
                if (!evento.isTemporary()) {
                    acao.run();
                }
            }
        });
    }

    /** Combo editável: valida e envia ao escolher na lista e ao perder o foco. */
    private void ligarCombo(JComboBox<String> combo, Campo campo, Runnable enviar) {
        combo.addActionListener(e -> {
            if (!carregando && "comboBoxChanged".equals(e.getActionCommand()) && combo.getSelectedItem() != null) {
                campo.validar();
                enviar.run();
            }
        });
        aoPerderFoco(editor(combo), () -> {
            editor(combo).setCaretPosition(0);
            campo.validar();
            enviar.run();
        });
    }

    // =================================================================
    // Classes internas de apoio visual
    // =================================================================

    /** Campo com validação: com erro, borda vermelha e mensagem no lugar da dica (Tela 3). */
    private final class Campo {
        private final String rotulo;
        private final JComponent painelComponente;
        private final JComponent componente;
        private final String textoDica;
        private final JLabel dica = new TextoQuebravel(" ");
        private JLabel rotuloLabel;
        private Function<Boolean, String> regra = envio -> null;
        private BooleanSupplier aplicavel = () -> true;
        private String segmentoRegra;
        private Border bordaNormal;
        private Color fundoNormal;
        private boolean aparenciaGuardada;

        Campo(String rotulo, JComponent painelComponente, JComponent componente, String dica) {
            this.rotulo = rotulo;
            this.painelComponente = painelComponente;
            this.componente = componente;
            this.textoDica = dica;
        }

        Campo regra(Function<Boolean, String> novaRegra) {
            this.regra = novaRegra;
            return this;
        }

        Campo quando(BooleanSupplier condicao) {
            this.aplicavel = condicao;
            return this;
        }

        /** RN-04: a pendência cita o segmento (Tela 3). */
        Campo doSegmento(String segmento) {
            this.segmentoRegra = segmento;
            return this;
        }

        JPanel painel(boolean obrigatorio) {
            JPanel painel = new JPanel(new BorderLayout(0, 5));
            painel.setOpaque(false);
            rotuloLabel = new JLabel(obrigatorio ? rotuloComAsterisco(rotulo) : rotulo);
            rotuloLabel.setFont(rotuloLabel.getFont().deriveFont(Font.BOLD, 11f));
            rotuloLabel.setForeground(CINZA_ROTULO);
            if (painelComponente != caixaFoto) {
                painelComponente.setPreferredSize(new Dimension(10, 36));
            }
            painel.add(rotuloLabel, BorderLayout.NORTH);
            painel.add(painelComponente, BorderLayout.CENTER);
            configurarDica();
            painel.add(dica, BorderLayout.SOUTH);
            guardarAparencia();
            return painel;
        }

        JPanel painelSemRotulo() {
            JPanel painel = new JPanel(new BorderLayout(0, 4));
            painel.setOpaque(false);
            painel.add(painelComponente, BorderLayout.CENTER);
            configurarDica();
            painel.add(dica, BorderLayout.SOUTH);
            guardarAparencia();
            return painel;
        }

        void rotuloObrigatorio(boolean obrigatorio) {
            if (rotuloLabel != null) {
                rotuloLabel.setText(obrigatorio ? rotuloComAsterisco(rotulo) : rotulo);
            }
        }

        private void configurarDica() {
            dica.setFont(dica.getFont().deriveFont(Font.PLAIN, 10.5f));
            dica.setForeground(CINZA_DICA);
            dica.setText(textoDica.isEmpty() ? " " : "<html>" + textoDica + "</html>");
        }

        /** Componente que recebe a borda vermelha. */
        private JComponent alvoBorda() {
            return componente == campoCliente ? painelCliente : componente;
        }

        private void guardarAparencia() {
            bordaNormal = alvoBorda().getBorder();
            fundoNormal = alvoBorda().getBackground();
            aparenciaGuardada = true;
        }

        /** Validação (a): ao perder o foco. */
        String validar() {
            return validar(false);
        }

        String validar(boolean envio) {
            if (!aplicavel.getAsBoolean()) {
                limpar();
                return null;
            }
            String erro = regra.apply(envio);
            if (erro == null) {
                limpar();
            } else {
                marcar(erro);
            }
            return erro;
        }

        /** Verifica sem marcar o campo. */
        boolean valido() {
            return !aplicavel.getAsBoolean() || regra.apply(false) == null;
        }

        void marcar(String mensagem) {
            JComponent alvo = alvoBorda();
            if (!aparenciaGuardada) {
                guardarAparencia();
            }
            alvo.setBorder(alvo == painelOrigem || alvo == painelSegmento
                    ? BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VERMELHO),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6))
                    : bordaCampo(VERMELHO));
            if (!(alvo instanceof JPanel && !alvo.isOpaque())) {
                alvo.setBackground(VERMELHO_FUNDO);
            }
            if (componente instanceof JComboBox) {
                editor((JComboBox<?>) componente).setBackground(VERMELHO_FUNDO);
            }
            if (componente == campoCliente) {
                campoCliente.setBackground(VERMELHO_FUNDO);
            }
            dica.setForeground(VERMELHO);
            dica.setFont(dica.getFont().deriveFont(Font.BOLD, 10.5f));
            dica.setText("<html>" + mensagem + "</html>");
            dica.setName("erro:" + rotulo);
        }

        void limpar() {
            JComponent alvo = alvoBorda();
            if (aparenciaGuardada) {
                alvo.setBorder(bordaNormal);
                alvo.setBackground(fundoNormal);
            }
            if (componente instanceof JComboBox) {
                editor((JComboBox<?>) componente).setBackground(Color.WHITE);
            }
            if (componente == campoCliente) {
                campoCliente.setBackground(Color.WHITE);
            }
            configurarDica();
            dica.setName(null);
        }

        /** Linha da caixa de pendências: "Modelo da ECU: campo obrigatório para o segmento Módulos." */
        String textoPendencia(String erro) {
            if (Validacoes.MSG_OBRIGATORIO.equals(erro)) {
                return rotulo + ": campo obrigatório" + (segmentoRegra == null ? "." : " para o segmento "
                        + segmentoRegra + ".");
            }
            return rotulo + ": " + erro;
        }

        String mensagemAtual() {
            return dica.getName() == null ? null : dica.getText().replaceAll("<[^>]+>", "");
        }
    }

    /** Texto HTML que quebra linha na largura recebida, sem alargar a coluna. */
    private static final class TextoQuebravel extends JLabel {
        private static final long serialVersionUID = 1L;

        TextoQuebravel(String texto) {
            super(texto);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamanho = super.getPreferredSize();
            javax.swing.text.View visao = (javax.swing.text.View) getClientProperty(
                    javax.swing.plaf.basic.BasicHTML.propertyKey);
            int largura = getWidth() - getInsets().left - getInsets().right;
            if (visao != null && largura > 0) {
                visao.setSize(largura, 0);
                int altura = (int) Math.ceil(visao.getPreferredSpan(javax.swing.text.View.Y_AXIS));
                return new Dimension(10, altura + getInsets().top + getInsets().bottom);
            }
            return new Dimension(10, tamanho.height);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public void setBounds(int x, int y, int largura, int altura) {
            int anterior = getWidth();
            super.setBounds(x, y, largura, altura);
            if (anterior != largura) {
                SwingUtilities.invokeLater(this::revalidate);
            }
        }
    }

    /** Formulário na largura da janela: só rolagem vertical. */
    private static class PainelLarguraDaJanela extends JPanel implements javax.swing.Scrollable {
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle area, int orientacao, int direcao) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle area, int orientacao, int direcao) {
            return area.height - 40;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Campo de texto com placeholder (ex.: "0,00", "Selecione…"). */
    private static class CampoTexto extends JTextField {
        private static final long serialVersionUID = 1L;
        private final String placeholder;

        CampoTexto(String nome, String placeholder) {
            this.placeholder = placeholder;
            setName(nome);
            setFont(getFont().deriveFont(Font.PLAIN, 13f));
            setBorder(bordaCampo(CINZA_BORDA));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0x9C, 0xA3, 0xAF));
                Insets m = getInsets();
                int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
                g2.drawString(placeholder, m.left, y);
                g2.dispose();
            }
        }
    }

    /** Editor das combos editáveis, com placeholder. */
    private static class EditorComPlaceholder extends BasicComboBoxEditor {
        EditorComPlaceholder(String nome, String placeholder) {
            CampoTexto campo = new CampoTexto(nome + ".editor", placeholder);
            campo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
            editor = campo;
        }
    }

    /** Linhas da tabela de serviços: pílula da categoria, valor em negrito e ações ✎ ×. */
    private final class RenderizadorItens extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                                                       boolean foco, int linha, int coluna) {
            if (coluna == 1) {
                JPanel celula = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
                celula.setBackground(Color.WHITE);
                JLabel pilula = new JLabel(String.valueOf(valor));
                pilula.setOpaque(true);
                pilula.setBackground(new Color(0xE0, 0xE7, 0xFF));
                pilula.setForeground(AZUL_TITULO);
                pilula.setFont(pilula.getFont().deriveFont(Font.BOLD, 10.5f));
                pilula.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                celula.add(pilula);
                return celula;
            }
            JLabel rotulo = (JLabel) super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            rotulo.setBackground(Color.WHITE);
            rotulo.setForeground(CINZA_ROTULO);
            rotulo.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            rotulo.setFont(rotulo.getFont().deriveFont(coluna == 2 ? Font.BOLD : Font.PLAIN, 13f));
            rotulo.setHorizontalAlignment(coluna == 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (coluna == 3) {
                rotulo.setHorizontalAlignment(SwingConstants.CENTER);
                rotulo.setText("<html><font color='#1D4ED8'>✎</font>&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<font color='#DC2626'><b>×</b></font></html>");
                rotulo.setToolTipText("✎ alterar valor  ·  × remover");
            }
            return rotulo;
        }
    }

    /** "Arraste a foto ou clique para anexar". */
    private final class ReceptorDeFoto extends TransferHandler {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean canImport(TransferSupport suporte) {
            return suporte.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport suporte) {
            try {
                @SuppressWarnings("unchecked")
                List<File> arquivos = (List<File>) suporte.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                if (!arquivos.isEmpty()) {
                    anexarFoto(arquivos.get(0));
                    return true;
                }
            } catch (Exception erro) {
                cFoto.marcar("Não foi possível anexar o arquivo.");
            }
            return false;
        }
    }

    /** Ícone redondo com símbolo, desenhado (sem depender de fonte). */
    private static final class IconeCirculo implements Icon {
        private final char simbolo;
        private final Color cor;
        private final boolean cheio;
        private final int tamanho;

        IconeCirculo(char simbolo, Color cor, boolean cheio, int tamanho) {
            this.simbolo = simbolo;
            this.cor = cor;
            this.cheio = cheio;
            this.tamanho = tamanho;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            float s = tamanho;
            g2.setColor(cor);
            if (cheio) {
                g2.fillOval(0, 0, tamanho - 1, tamanho - 1);
            } else {
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawOval(1, 1, tamanho - 3, tamanho - 3);
            }
            Color tinta = cheio ? Color.WHITE : cor;
            g2.setColor(tinta);
            g2.setStroke(new BasicStroke(s / 11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (simbolo == '✓') {
                g2.drawPolyline(new int[]{(int) (s * .28), (int) (s * .44), (int) (s * .72)},
                        new int[]{(int) (s * .52), (int) (s * .67), (int) (s * .36)}, 3);
            } else if (simbolo == '+') {
                g2.drawLine((int) (s * .5), (int) (s * .3), (int) (s * .5), (int) (s * .7));
                g2.drawLine((int) (s * .3), (int) (s * .5), (int) (s * .7), (int) (s * .5));
            } else if (simbolo == '✎') {
                g2.drawLine((int) (s * .34), (int) (s * .66), (int) (s * .66), (int) (s * .34));
                g2.drawLine((int) (s * .30), (int) (s * .70), (int) (s * .38), (int) (s * .70));
            } else {
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(s * .62f)));
                String texto = String.valueOf(simbolo);
                int largura = g2.getFontMetrics().stringWidth(texto);
                int alturaTexto = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
                g2.drawString(texto, (tamanho - largura) / 2f, (tamanho + alturaTexto) / 2f);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return tamanho;
        }

        @Override
        public int getIconHeight() {
            return tamanho;
        }
    }

    /** Lupa do campo "Cliente". */
    private static final class IconeLupa implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CINZA_DICA);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(x + 1, y + 1, 11, 11);
            g2.drawLine(x + 11, y + 11, x + 16, y + 16);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 17;
        }

        @Override
        public int getIconHeight() {
            return 17;
        }
    }

    // =================================================================
    // Consultas para os testes de interface
    // =================================================================

    /** Mensagem exibida sob o campo de rótulo informado, ou null. */
    String mensagemDoCampo(String rotulo) {
        for (Campo campo : camposFormulario) {
            if (campo.rotulo.equals(rotulo)) {
                return campo.mensagemAtual();
            }
        }
        return null;
    }

    boolean limiteChavesAtingido() {
        return limiteChavesAtingido;
    }

    List<Object[]> itensDaTela() {
        return itensTela;
    }
}
