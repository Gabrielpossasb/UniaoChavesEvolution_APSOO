package uniaoevolutions.view;

import uniaoevolutions.controle.ControladoraRegistrarOS;
import uniaoevolutions.negocio.ItemOrcamento;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.OrdemDeServico;
import uniaoevolutions.negocio.Segmento;
import uniaoevolutions.negocio.Usuario;
import uniaoevolutions.negocio.Veiculo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Classe <<boundary>> do Diagrama de Classes e objeto ":Interface" do Diagrama
 * de Comunicação. O layout segue as seções do
 * MOCKUP-registrar-ordem-de-servico.pdf.
 *
 * Sem SQL e sem conhecer as DAOs: lê o que o usuário digitou, pede à
 * ControladoraRegistrarOS e exibe o resultado ou o erro.
 */
public class TelaRegistrarOrdemServico extends JFrame {

    // Cores tiradas do mockup.
    private static final Color AZUL_CABECALHO = new Color(0x1E, 0x3A, 0x8A);
    private static final Color AZUL_TITULO = new Color(0x1D, 0x4E, 0xD8);
    private static final Color CINZA_FUNDO = new Color(0xF1, 0xF3, 0xF7);
    private static final Color CINZA_ROTULO = new Color(0x55, 0x5F, 0x70);
    private static final Color CINZA_CAMPO = new Color(0xF6, 0xF7, 0xF9);
    private static final Color VERDE_SITUACAO = new Color(0x16, 0x7C, 0x3C);
    private static final Color AMARELO_AVISO = new Color(0xFF, 0xFB, 0xEB);

    private static final SimpleDateFormat DATA = new SimpleDateFormat("dd/MM/yyyy");
    private static final DecimalFormat MOEDA = criarFormatoMoeda();

    /** Única classe com que a tela conversa. */
    private final ControladoraRegistrarOS controladora = new ControladoraRegistrarOS();

    // --- Identificação da OS -----------------------------------------
    private final JTextField campoNumeroOS = criarCampoSomenteLeitura();
    private final JComboBox<Orcamento> comboOrcamento = new JComboBox<>();
    private final JTextField campoDataAbertura = criarCampoSomenteLeitura();
    private final JTextField campoPrazoEstimado = new JTextField();
    private final JLabel rotuloSituacao = new JLabel(" ");

    // --- Cliente e Veículo -------------------------------------------
    private final JTextField campoCliente = criarCampoSomenteLeitura();
    private final JTextField campoTelefone = criarCampoSomenteLeitura();
    private final JTextField campoPlaca = criarCampoSomenteLeitura();
    private final JTextField campoMarcaModelo = criarCampoSomenteLeitura();
    private final JTextField campoAno = criarCampoSomenteLeitura();
    private final JTextField campoQuilometragem = new JTextField();
    private final JButton botaoHistorico = new JButton("Ver atendimentos anteriores");

    // --- Segmento do Serviço -----------------------------------------
    private final JRadioButton radioModulos =
            new JRadioButton("Reparação / Programação de Módulos (ECU)", true);
    private final JRadioButton radioChaves =
            new JRadioButton("Programação / Confecção de chaves");

    // --- Dados Técnicos ----------------------------------------------
    private final CardLayout cartoes = new CardLayout();
    private final JPanel painelDadosTecnicos = new JPanel(cartoes);
    private final JLabel rotuloCamposExigidos = new JLabel(" ");
    // Módulos (variante V1)
    private final JTextField campoTipoModulo = criarCampoSomenteLeitura();
    private final JTextField campoModeloECU = criarCampoSomenteLeitura();
    private final JTextField campoIdentificacaoModulo = criarCampoSomenteLeitura();
    private final JTextField campoRelatoInicial = new JTextField();
    private final JTextField campoCondicaoRecebimento = new JTextField();
    private final JTextField campoDiagnosticoInicial = new JTextField();
    // Chaves (variante V2)
    private final JTextField campoCodigoMecanico = criarCampoSomenteLeitura();
    private final JTextField campoSenhaImobilizador = criarCampoSomenteLeitura();
    private final JTextField campoServicoExecutado = new JTextField();

    // --- Execução ----------------------------------------------------
    private final JComboBox<Usuario> comboTecnico = new JComboBox<>();
    private final JTextField campoObservacoes = new JTextField();
    private final DefaultTableModel modeloItens = new DefaultTableModel(
            new String[]{"Serviço / Peça (herdado do orçamento)", "Qtd.", "Valor unit.", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;   // POS-03: itens herdados do orçamento, não digitados.
        }
    };
    private final JTable tabelaItens = new JTable(modeloItens);
    private final JLabel rotuloTotal = new JLabel("Valor total da OS: " + MOEDA.format(0));

    // --- Botões do rodapé --------------------------------------------
    private final JButton botaoVoltar = new JButton("Voltar / Cancelar");
    private final JButton botaoIncluirItem = new JButton("Incluir item");
    private final JButton botaoRascunho = new JButton("Salvar rascunho");
    private final JButton botaoImprimir = new JButton("Imprimir OS");
    private final JButton botaoSalvar = new JButton("Salvar Ordem de Serviço");

    /** Impede que o preenchimento automático dispare os eventos de troca. */
    private boolean carregando;

    public TelaRegistrarOrdemServico() {
        super("União Evolutions - Registrar Ordem de Serviço");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1060, 880);
        setLocationRelativeTo(null);
        montarTela();
        ligarEventos();
        // O passo 1 acessa o banco: é agendado para depois de a janela
        // aparecer, para que um erro (FA-12) saia sobre a tela já montada.
        SwingUtilities.invokeLater(this::iniciarRegistro);
    }

    // =================================================================
    // Montagem da tela (layout do mockup)
    // =================================================================

    private void montarTela() {
        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(CINZA_FUNDO);
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel obrigatorios = new JLabel("Campos marcados com * são de preenchimento obrigatório.");
        obrigatorios.setFont(obrigatorios.getFont().deriveFont(Font.ITALIC, 11f));
        obrigatorios.setForeground(CINZA_ROTULO);
        obrigatorios.setAlignmentX(LEFT_ALIGNMENT);
        conteudo.add(obrigatorios);
        conteudo.add(Box.createVerticalStrut(8));

        conteudo.add(criarSecaoIdentificacao());
        conteudo.add(Box.createVerticalStrut(10));
        conteudo.add(criarSecaoClienteVeiculo());
        conteudo.add(Box.createVerticalStrut(10));
        conteudo.add(criarSecaoSegmento());
        conteudo.add(Box.createVerticalStrut(10));
        conteudo.add(criarSecaoDadosTecnicos());
        conteudo.add(Box.createVerticalStrut(10));
        conteudo.add(criarSecaoExecucao());
        conteudo.add(Box.createVerticalGlue());

        JScrollPane rolagem = new JScrollPane(conteudo);
        rolagem.setBorder(null);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);

        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);
        add(rolagem, BorderLayout.CENTER);
        add(criarBarraDeBotoes(), BorderLayout.SOUTH);
    }

    /** Barra azul do topo do mockup. */
    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel esquerda = new JPanel();
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.setOpaque(false);
        JLabel empresa = new JLabel("UNIÃO EVOLUTIONS");
        empresa.setForeground(Color.WHITE);
        empresa.setFont(empresa.getFont().deriveFont(Font.BOLD, 15f));
        JLabel sistema = new JLabel("Sistema de Gestão de Serviços Automotivos");
        sistema.setForeground(new Color(0xC7, 0xD2, 0xFE));
        sistema.setFont(sistema.getFont().deriveFont(Font.PLAIN, 11f));
        esquerda.add(empresa);
        esquerda.add(sistema);

        JLabel titulo = new JLabel("Registrar Ordem de Serviço");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 13f));

        cabecalho.add(esquerda, BorderLayout.WEST);
        cabecalho.add(titulo, BorderLayout.EAST);
        return cabecalho;
    }

    /** Bloco "IDENTIFICAÇÃO DA OS" do mockup. */
    private JPanel criarSecaoIdentificacao() {
        rotuloSituacao.setFont(rotuloSituacao.getFont().deriveFont(Font.BOLD, 12f));
        rotuloSituacao.setForeground(VERDE_SITUACAO);

        JPanel secao = criarSecao("IDENTIFICAÇÃO DA OS");
        secao.add(criarLinha(
                new JComponent[]{
                        criarCampo("Nº da OS", campoNumeroOS),
                        criarCampo("Orçamento associado *", comboOrcamento),
                        criarCampo("Data de abertura *", campoDataAbertura),
                        criarCampo("Prazo estimado * (dd/mm/aaaa)", campoPrazoEstimado),
                        criarCampo("Situação", rotuloSituacao)},
                new double[]{1, 1.4, 1, 1.2, 1}));
        return secao;
    }

    /** Bloco "CLIENTE E VEÍCULO" do mockup. */
    private JPanel criarSecaoClienteVeiculo() {
        JPanel secao = criarSecao("CLIENTE E VEÍCULO");
        secao.add(criarLinha(
                new JComponent[]{
                        criarCampo("Cliente *", campoCliente),
                        criarCampo("Telefone / Whatsapp", campoTelefone)},
                new double[]{2, 1}));
        secao.add(criarLinha(
                new JComponent[]{
                        criarCampo("Placa *", campoPlaca),
                        criarCampo("Marca / Modelo", campoMarcaModelo),
                        criarCampo("Ano", campoAno),
                        criarCampo("Quilometragem *", campoQuilometragem),
                        criarCampo("Histórico técnico", botaoHistorico)},
                new double[]{1, 2, 0.6, 1, 1.6}));
        return secao;
    }

    /** Bloco "SEGMENTO DO SERVIÇO" do mockup. */
    private JPanel criarSecaoSegmento() {
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(radioModulos);
        grupo.add(radioChaves);

        JPanel opcoes = new JPanel(new GridBagLayout());
        opcoes.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        c.gridx = 0;
        radioModulos.setOpaque(false);
        radioChaves.setOpaque(false);
        opcoes.add(radioModulos, c);
        c.gridx = 1;
        c.insets = new Insets(0, 24, 0, 0);
        opcoes.add(radioChaves, c);
        c.gridx = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        opcoes.add(Box.createHorizontalGlue(), c);

        JLabel atencao = new JLabel("Atenção: a seleção do segmento determina os campos técnicos "
                + "exibidos abaixo e restringe a lista de técnicos responsáveis habilitados.");
        atencao.setFont(atencao.getFont().deriveFont(Font.PLAIN, 11f));
        atencao.setOpaque(true);
        atencao.setBackground(AMARELO_AVISO);
        atencao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFD, 0xE6, 0x8A)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        atencao.setAlignmentX(LEFT_ALIGNMENT);

        JPanel secao = criarSecao("SEGMENTO DO SERVIÇO");
        opcoes.setAlignmentX(LEFT_ALIGNMENT);
        secao.add(opcoes);
        secao.add(Box.createVerticalStrut(8));
        secao.add(atencao);
        return secao;
    }

    /**
     * Bloco "DADOS TÉCNICOS" do mockup. V1 e V2 ficam num CardLayout: só um
     * aparece de cada vez, conforme o segmento.
     */
    private JPanel criarSecaoDadosTecnicos() {
        painelDadosTecnicos.setOpaque(false);
        painelDadosTecnicos.add(criarBlocoModulos(), Segmento.MODULOS);
        painelDadosTecnicos.add(criarBlocoChaves(), Segmento.CHAVES);
        painelDadosTecnicos.setAlignmentX(LEFT_ALIGNMENT);

        rotuloCamposExigidos.setFont(rotuloCamposExigidos.getFont().deriveFont(Font.PLAIN, 11f));
        rotuloCamposExigidos.setForeground(CINZA_ROTULO);
        rotuloCamposExigidos.setAlignmentX(LEFT_ALIGNMENT);

        JPanel secao = criarSecao("DADOS TÉCNICOS");
        secao.add(rotuloCamposExigidos);
        secao.add(Box.createVerticalStrut(6));
        secao.add(painelDadosTecnicos);
        return secao;
    }

    /** Variante V1: a identificação do módulo vem do Veiculo. */
    private JPanel criarBlocoModulos() {
        JPanel bloco = new JPanel();
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setOpaque(false);
        bloco.add(criarLinha(
                new JComponent[]{
                        criarCampo("Tipo de módulo *", campoTipoModulo),
                        criarCampo("Fabricante / Modelo da ECU *", campoModeloECU),
                        criarCampo("Nº da peça / Nº de série *", campoIdentificacaoModulo)},
                new double[]{1.2, 1.4, 1.2}));
        bloco.add(criarLinha(
                new JComponent[]{criarCampo("Sintomas relatados pelo cliente *", campoRelatoInicial)},
                new double[]{1}));
        bloco.add(criarLinha(
                new JComponent[]{criarCampo("Condição de recebimento *", campoCondicaoRecebimento)},
                new double[]{1}));
        bloco.add(criarLinha(
                new JComponent[]{criarCampo("Diagnóstico em bancada *", campoDiagnosticoInicial)},
                new double[]{1}));
        return bloco;
    }

    /** Variante V2: os dados do imobilizador vêm do Veiculo. */
    private JPanel criarBlocoChaves() {
        JPanel bloco = new JPanel();
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setOpaque(false);
        bloco.add(criarLinha(
                new JComponent[]{
                        criarCampo("Código mecânico", campoCodigoMecanico),
                        criarCampo("Senha do imobilizador", campoSenhaImobilizador)},
                new double[]{1, 1}));
        bloco.add(criarLinha(
                new JComponent[]{criarCampo("Serviço de chaves a executar *", campoServicoExecutado)},
                new double[]{1}));
        return bloco;
    }

    /** Bloco "EXECUÇÃO" do mockup, com a tabela de itens e o valor total. */
    private JPanel criarSecaoExecucao() {
        JLabel aviso = new JLabel("Somente técnicos habilitados no segmento selecionado.");
        aviso.setFont(aviso.getFont().deriveFont(Font.PLAIN, 11f));
        aviso.setForeground(CINZA_ROTULO);
        aviso.setAlignmentX(LEFT_ALIGNMENT);

        tabelaItens.setRowHeight(22);
        tabelaItens.getTableHeader().setReorderingAllowed(false);
        // A descrição é longa; as colunas de número são curtas.
        tabelaItens.getColumnModel().getColumn(0).setPreferredWidth(520);
        tabelaItens.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabelaItens.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabelaItens.getColumnModel().getColumn(3).setPreferredWidth(110);
        JScrollPane rolagemItens = new JScrollPane(tabelaItens);
        rolagemItens.setPreferredSize(new Dimension(100, 120));
        rolagemItens.setAlignmentX(LEFT_ALIGNMENT);

        rotuloTotal.setFont(rotuloTotal.getFont().deriveFont(Font.BOLD, 13f));
        rotuloTotal.setForeground(AZUL_TITULO);
        rotuloTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        rotuloTotal.setAlignmentX(LEFT_ALIGNMENT);
        rotuloTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JPanel secao = criarSecao("EXECUÇÃO");
        secao.add(criarLinha(
                new JComponent[]{
                        criarCampo("Técnico responsável *", comboTecnico),
                        criarCampo("Observações internas", campoObservacoes)},
                new double[]{1, 2}));
        secao.add(aviso);
        secao.add(Box.createVerticalStrut(10));
        secao.add(rolagemItens);
        secao.add(Box.createVerticalStrut(6));
        secao.add(rotuloTotal);
        return secao;
    }

    /** Botões do rodapé do mockup, mais os do FA-11B e FA-11C. */
    private JPanel criarBarraDeBotoes() {
        JPanel barra = new JPanel(new GridBagLayout());
        barra.setBackground(CINZA_FUNDO);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 16, 12, 16));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        barra.add(Box.createHorizontalGlue(), c);

        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 8, 0, 0);
        for (JButton botao : new JButton[]{botaoVoltar, botaoIncluirItem, botaoRascunho,
                                           botaoImprimir, botaoSalvar}) {
            c.gridx++;
            barra.add(botao, c);
        }

        botaoSalvar.setBackground(AZUL_CABECALHO);
        botaoSalvar.setForeground(Color.WHITE);
        botaoSalvar.setOpaque(true);
        botaoSalvar.setBorderPainted(false);
        return barra;
    }

    private void ligarEventos() {
        comboOrcamento.addActionListener(evento -> aoSelecionarOrcamento());
        radioModulos.addActionListener(evento -> aoTrocarSegmento());
        radioChaves.addActionListener(evento -> aoTrocarSegmento());
        botaoHistorico.addActionListener(evento -> verHistoricoTecnico());
        botaoSalvar.addActionListener(evento -> salvar());
        botaoRascunho.addActionListener(evento -> salvarRascunho());
        botaoIncluirItem.addActionListener(evento -> incluirItem());
        botaoImprimir.addActionListener(evento -> imprimir());
        botaoVoltar.addActionListener(evento -> cancelar());
    }

    // =================================================================
    // Fluxo principal
    // =================================================================

    /**
     * Passos 1 e 2: abre o formulário com o número e a data de abertura e
     * carrega a lista de orçamentos aprovados.
     */
    private void iniciarRegistro() {
        try {
            OrdemDeServico os = controladora.iniciarRegistroOS();
            campoNumeroOS.setText(os.getNumeroOS());
            campoDataAbertura.setText(DATA.format(os.getDataAbertura()));
            rotuloSituacao.setText(os.getStatusOS().toUpperCase());
            carregarOrcamentosAprovados();
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** Passo 2 e FA-02. */
    private void carregarOrcamentosAprovados() throws SQLException {
        List<Orcamento> aprovados = controladora.consultarOrcamentosAprovados();

        // Evita que o ActionListener consulte o banco a cada item adicionado.
        carregando = true;
        comboOrcamento.removeAllItems();
        for (Orcamento orcamento : aprovados) {
            comboOrcamento.addItem(orcamento);
        }
        if (!aprovados.isEmpty()) {
            comboOrcamento.setSelectedIndex(0);
        }
        carregando = false;

        if (aprovados.isEmpty()) {
            // FA-02.1: nenhum orçamento aprovado pendente de conversão.
            limparDadosDoOrcamento();
            JOptionPane.showMessageDialog(this,
                    "Nenhum orçamento aprovado disponível para abertura de Ordem de Serviço",
                    "Registrar Ordem de Serviço", JOptionPane.INFORMATION_MESSAGE);
            botaoSalvar.setEnabled(false);
            botaoRascunho.setEnabled(false);
            return;
        }
        botaoSalvar.setEnabled(true);
        botaoRascunho.setEnabled(true);
        // Passos 3 e 4 para o primeiro orçamento, selecionado acima.
        aoSelecionarOrcamento();
    }

    /** Passos 3 e 4: exibe cliente, veículo, segmento e itens herdados. */
    private void aoSelecionarOrcamento() {
        if (carregando) {
            return;
        }
        Orcamento orcamento = (Orcamento) comboOrcamento.getSelectedItem();
        if (orcamento == null) {
            limparDadosDoOrcamento();
            return;
        }
        try {
            // Relê do banco para trazer cliente, veículo e itens.
            Orcamento completo = controladora.consultarOrcamento(orcamento.getIdOrcamento());
            controladora.selecionarOrcamento(completo);
            preencherClienteVeiculo(completo);
            preencherItens(completo);

            // Passo 5: o segmento vem pré-selecionado com o segmentoPretendido
            // do orçamento, que é o usado na gravação.
            carregando = true;
            if (Segmento.CHAVES.equals(completo.getSegmentoPretendido())) {
                radioChaves.setSelected(true);
            } else {
                radioModulos.setSelected(true);
            }
            carregando = false;

            aoTrocarSegmento();
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** Passo 4: cliente e veículo herdados do orçamento. */
    private void preencherClienteVeiculo(Orcamento orcamento) {
        campoCliente.setText(orcamento.getCliente().getNome());
        campoTelefone.setText(orcamento.getCliente().getTelefone());

        Veiculo veiculo = orcamento.getVeiculo();
        campoPlaca.setText(veiculo.getPlaca());
        campoMarcaModelo.setText(veiculo.getMarcaModelo());
        campoAno.setText(String.valueOf(veiculo.getAnoModelo()));

        // Dados técnicos que pertencem ao veículo.
        campoTipoModulo.setText(veiculo.getTipoModulo());
        campoModeloECU.setText(veiculo.getModeloECU());
        campoIdentificacaoModulo.setText(veiculo.getIdentificacaoModulo());
        campoCodigoMecanico.setText(veiculo.getCodigoMecanico());
        campoSenhaImobilizador.setText(veiculo.getSenhaImobilizador());
    }

    /** Passo 4 / POS-03: itens herdados do orçamento. */
    private void preencherItens(Orcamento orcamento) {
        modeloItens.setRowCount(0);
        for (ItemOrcamento item : orcamento.getItens()) {
            modeloItens.addRow(new Object[]{
                    item.getDescricao(),
                    item.getQuantidade(),
                    MOEDA.format(item.getValorProposto()),
                    MOEDA.format(item.getSubtotal())});
        }
        rotuloTotal.setText("Valor total da OS: " + MOEDA.format(orcamento.getValorTotal()));
    }

    /**
     * Passos 6 e 9: o segmento define o bloco técnico exibido e a lista de
     * técnicos habilitados (RN-04).
     */
    private void aoTrocarSegmento() {
        if (carregando) {
            return;
        }
        String segmento = segmentoSelecionado();

        // Passo 6: campos exigidos pela RN-03.
        List<String> campos = controladora.consultarBlocosDadosTecnicos(segmento);
        rotuloCamposExigidos.setText("Campos exigidos no segmento " + segmento + ": "
                + String.join(" | ", campos));
        cartoes.show(painelDadosTecnicos, segmento);

        // Passo 9: técnicos habilitados.
        try {
            List<Usuario> tecnicos = controladora.consultarTecnicosHabilitados(segmento);
            comboTecnico.removeAllItems();
            for (Usuario tecnico : tecnicos) {
                comboTecnico.addItem(tecnico);
            }
            if (tecnicos.isEmpty()) {
                // FA-09.1
                mostrarAviso("Não há técnico habilitado disponível para este segmento");
            }
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** Passo 12: confirma o registro da Ordem de Serviço. */
    private void salvar() {
        try {
            Orcamento orcamento = orcamentoSelecionado();
            Usuario tecnico = tecnicoSelecionado();

            OrdemDeServico os = controladora.registrarOrdemServico(
                    orcamento.getIdOrcamento(),
                    tecnico.getIdUsuario(),
                    lerQuilometragem(),
                    lerPrazoEstimado(),
                    campoObservacoes.getText().trim(),
                    campoRelatoInicial.getText().trim(),
                    campoCondicaoRecebimento.getText().trim(),
                    campoDiagnosticoInicial.getText().trim(),
                    campoServicoExecutado.getText().trim());

            // Passo 12: mensagem de sucesso da Descrição Expandida.
            rotuloSituacao.setText(os.getStatusOS().toUpperCase());
            campoNumeroOS.setText(os.getNumeroOS());
            JOptionPane.showMessageDialog(this,
                    "Ordem de Serviço " + os.getNumeroOS() + " registrada com sucesso",
                    "Registrar Ordem de Serviço", JOptionPane.INFORMATION_MESSAGE);

            mostrarOrdemRegistrada(os);
            reiniciarFormulario();
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());               // FA-11A
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);                      // FA-12
        }
    }

    // =================================================================
    // Fluxos alternativos
    // =================================================================

    /** FA-11C: salva a Ordem de Serviço como rascunho. */
    private void salvarRascunho() {
        try {
            Orcamento orcamento = orcamentoSelecionado();
            Usuario tecnico = tecnicoSelecionado();

            controladora.salvarRascunhoOS(
                    orcamento.getIdOrcamento(),
                    tecnico.getIdUsuario(),
                    lerQuilometragem(),
                    lerPrazoEstimado(),
                    campoObservacoes.getText().trim());

            // FA-11C.2
            rotuloSituacao.setText(OrdemDeServico.STATUS_RASCUNHO.toUpperCase());
            JOptionPane.showMessageDialog(this, "Ordem de Serviço salva como rascunho",
                    "Registrar Ordem de Serviço", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** FA-11B: inclusão de item não previsto no orçamento aprovado. */
    private void incluirItem() {
        try {
            String servico = JOptionPane.showInputDialog(this,
                    "Serviço ou peça a incluir:", "Incluir item", JOptionPane.QUESTION_MESSAGE);
            if (servico == null) {
                return;
            }
            String quantidadeTexto = JOptionPane.showInputDialog(this,
                    "Quantidade:", "Incluir item", JOptionPane.QUESTION_MESSAGE);
            if (quantidadeTexto == null) {
                return;
            }
            String valorTexto = JOptionPane.showInputDialog(this,
                    "Valor unitário (ex.: 120,00):", "Incluir item", JOptionPane.QUESTION_MESSAGE);
            if (valorTexto == null) {
                return;
            }

            int quantidade = lerInteiro(quantidadeTexto, "Informe uma quantidade numérica válida.");
            double valor = lerDecimal(valorTexto, "Informe um valor numérico válido.");

            // FA-11B.2: exibe a exigência de justificativa.
            String exigencia = controladora.incluirItemOrdemServico(servico.trim(), quantidade, valor);
            String justificativa = JOptionPane.showInputDialog(this, exigencia,
                    "Justificativa da alteração (RN-05)", JOptionPane.WARNING_MESSAGE);
            if (justificativa == null) {
                return;
            }

            // FA-11B.3 e FA-11B.4
            String resultado = controladora.registrarOSComAlteracao(
                    servico.trim(), quantidade, valor, justificativa.trim());
            JOptionPane.showMessageDialog(this, resultado, "Alteração registrada",
                    JOptionPane.INFORMATION_MESSAGE);

            // FA-11B.5: encerra sem registrar a Ordem de Serviço.
            reiniciarFormulario();
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /** FA-00: desistência do Administrador. */
    private void cancelar() {
        int resposta = JOptionPane.showConfirmDialog(this,
                controladora.solicitarConfirmacaoDesistencia(),
                "Voltar / Cancelar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        controladora.descartarRegistro();
        dispose();
    }

    /** Passo 8.2 da V1 / botão "Ver atendimentos anteriores" do mockup. */
    private void verHistoricoTecnico() {
        try {
            Orcamento orcamento = orcamentoSelecionado();
            List<String> historico =
                    controladora.consultarHistoricoTecnico(orcamento.getVeiculo().getIdVeiculo());

            String texto = historico.isEmpty()
                    ? "Não há atendimentos anteriores registrados para este veículo."
                    : String.join("\n", historico);

            JOptionPane.showMessageDialog(this, criarAreaDeTexto(texto),
                    "Histórico técnico - " + orcamento.getVeiculo().getPlaca(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException erro) {
            mostrarAviso(erro.getMessage());
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /**
     * Botão "Imprimir OS" do mockup: exibe a OS como ela está. Pode ser usado
     * antes da gravação, então os campos em branco saem como "-".
     */
    private void imprimir() {
        OrdemDeServico os = controladora.getOsEmRegistro();
        if (os == null || os.getOrcamento() == null) {
            mostrarAviso("Selecione o orçamento antes de imprimir a Ordem de Serviço.");
            return;
        }
        // Copia para a OS o que já foi digitado na tela.
        try {
            os.setQuilometragem(lerQuilometragem());
        } catch (IllegalArgumentException naoInformada) {
            // em branco: sai como "-"
        }
        try {
            os.setPrazoEstimado(lerPrazoEstimado());
        } catch (IllegalArgumentException naoInformado) {
            // em branco: sai como "-"
        }
        os.setObservacoes(campoObservacoes.getText().trim());
        Usuario tecnico = (Usuario) comboTecnico.getSelectedItem();
        if (tecnico != null) {
            os.associarTecnico(tecnico);
        }
        mostrarOrdemRegistrada(os);
    }

    // =================================================================
    // Leitura dos campos da tela
    // =================================================================

    private Orcamento orcamentoSelecionado() {
        Orcamento orcamento = (Orcamento) comboOrcamento.getSelectedItem();
        if (orcamento == null) {
            throw new IllegalArgumentException("Selecione o orçamento que originará a Ordem de Serviço.");
        }
        return orcamento;
    }

    private Usuario tecnicoSelecionado() {
        Usuario tecnico = (Usuario) comboTecnico.getSelectedItem();
        if (tecnico == null) {
            throw new IllegalArgumentException("Selecione o técnico responsável pela Ordem de Serviço.");
        }
        return tecnico;
    }

    private String segmentoSelecionado() {
        return radioChaves.isSelected() ? Segmento.CHAVES : Segmento.MODULOS;
    }

    private int lerQuilometragem() {
        return lerInteiro(campoQuilometragem.getText().replace(".", "").trim(),
                "Informe a quilometragem atual do veículo.");
    }

    private Date lerPrazoEstimado() {
        String texto = campoPrazoEstimado.getText().trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Informe o prazo estimado de conclusão.");
        }
        try {
            DATA.setLenient(false);
            return DATA.parse(texto);
        } catch (ParseException erro) {
            throw new IllegalArgumentException("Informe o prazo estimado no formato dd/mm/aaaa.");
        }
    }

    private int lerInteiro(String texto, String mensagem) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private double lerDecimal(String texto, String mensagem) {
        try {
            return Double.parseDouble(texto.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    // =================================================================
    // Apoio
    // =================================================================

    /** Limpa a tela e volta ao passo 1. */
    private void reiniciarFormulario() {
        campoQuilometragem.setText("");
        campoPrazoEstimado.setText("");
        campoObservacoes.setText("");
        campoRelatoInicial.setText("");
        campoCondicaoRecebimento.setText("");
        campoDiagnosticoInicial.setText("");
        campoServicoExecutado.setText("");
        iniciarRegistro();
    }

    private void limparDadosDoOrcamento() {
        for (JTextField campo : new JTextField[]{campoCliente, campoTelefone, campoPlaca,
                campoMarcaModelo, campoAno, campoTipoModulo, campoModeloECU,
                campoIdentificacaoModulo, campoCodigoMecanico, campoSenhaImobilizador}) {
            campo.setText("");
        }
        modeloItens.setRowCount(0);
        comboTecnico.removeAllItems();
        rotuloTotal.setText("Valor total da OS: " + MOEDA.format(0));
    }

    /** Passo 12: "o sistema exibe a Ordem de Serviço registrada". */
    private void mostrarOrdemRegistrada(OrdemDeServico os) {
        StringBuilder texto = new StringBuilder();
        texto.append("UNIÃO EVOLUTIONS - ORDEM DE SERVIÇO\n");
        texto.append("====================================================\n");
        texto.append("Nº da OS ............ ").append(valor(os.getNumeroOS())).append('\n');
        texto.append("Situação ............ ").append(valor(os.getStatusOS())).append('\n');
        texto.append("Data de abertura .... ").append(data(os.getDataAbertura())).append('\n');
        texto.append("Prazo estimado ...... ").append(data(os.getPrazoEstimado())).append('\n');
        texto.append("Orçamento ........... ").append(os.getOrcamento().getNumeroOrcamento()).append('\n');
        texto.append("Cliente ............. ").append(os.getOrcamento().getCliente().getNome()).append('\n');
        Veiculo veiculo = os.getVeiculo();
        texto.append("Veículo ............. ")
             .append(veiculo == null ? "-" : veiculo.getPlaca() + " - " + veiculo.getMarcaModelo())
             .append('\n');
        texto.append("Quilometragem ....... ")
             .append(os.getQuilometragem() <= 0 ? "-" : os.getQuilometragem() + " km").append('\n');
        texto.append("Segmento ............ ").append(os.getSegmento()).append('\n');
        texto.append("Técnico responsável . ")
             .append(os.getTecnico() == null ? "-" : os.getTecnico().getNome()).append('\n');
        texto.append("Observações ......... ").append(valor(os.getObservacoes())).append('\n');
        texto.append("----------------------------------------------------\n");
        for (ItemOrcamento item : os.getItens()) {
            texto.append(String.format("%-40s %3dx %12s %12s%n", item.getDescricao(),
                    item.getQuantidade(), MOEDA.format(item.getValorProposto()),
                    MOEDA.format(item.getSubtotal())));
        }
        texto.append("----------------------------------------------------\n");
        texto.append(String.format("%-57s %12s%n", "VALOR TOTAL DA OS", MOEDA.format(os.getValorTotal())));

        JOptionPane.showMessageDialog(this, criarAreaDeTexto(texto.toString()),
                "Ordem de Serviço", JOptionPane.PLAIN_MESSAGE);
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private String data(Date data) {
        return data == null ? "-" : DATA.format(data);
    }

    private JScrollPane criarAreaDeTexto(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane rolagem = new JScrollPane(area);
        rolagem.setPreferredSize(new Dimension(620, 380));
        return rolagem;
    }

    /** FA-11A e demais pendências: aviso, não erro de sistema. */
    private void mostrarAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    /** FA-12: indisponibilidade da base de dados. */
    private void mostrarErroDeBanco(SQLException erro) {
        JOptionPane.showMessageDialog(this,
                "Não foi possível registrar a Ordem de Serviço. Tente novamente.\n\n"
                        + "Detalhe técnico: " + erro.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // =================================================================
    // Construção dos componentes visuais
    // =================================================================

    /** Cartão branco com título, como as seções do mockup. */
    private JPanel criarSecao(String titulo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        painel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 12f));
        rotulo.setForeground(AZUL_TITULO);
        rotulo.setAlignmentX(LEFT_ALIGNMENT);
        rotulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE6, 0xEC)),
                BorderFactory.createEmptyBorder(12, 16, 14, 16)));
        painel.add(rotulo);
        return painel;
    }

    /** Campo do formulário: rótulo em cima, componente embaixo. */
    private JPanel criarCampo(String rotulo, JComponent componente) {
        JPanel painel = new JPanel(new BorderLayout(0, 3));
        painel.setOpaque(false);

        JLabel label = new JLabel(rotulo);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setForeground(CINZA_ROTULO);

        componente.setPreferredSize(new Dimension(10, 26));
        painel.add(label, BorderLayout.NORTH);
        painel.add(componente, BorderLayout.CENTER);
        return painel;
    }

    /** Linha de campos, dividindo a largura conforme os pesos. */
    private JPanel criarLinha(JComponent[] campos, double[] pesos) {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);
        painel.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 12);
        c.gridy = 0;
        for (int i = 0; i < campos.length; i++) {
            c.gridx = i;
            c.weightx = pesos[i];
            painel.add(campos[i], c);
        }
        return painel;
    }

    /** Campo não digitável: o conteúdo vem do orçamento ou do sistema. */
    private static JTextField criarCampoSomenteLeitura() {
        JTextField campo = new JTextField();
        campo.setEditable(false);
        campo.setBackground(CINZA_CAMPO);
        return campo;
    }

    private static DecimalFormat criarFormatoMoeda() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("'R$ '#,##0.00", simbolos);
    }
}
