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
import javax.swing.ButtonModel;
import javax.swing.Icon;
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
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

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
    // Botões do mockup.
    private static final Color AZUL_CLARO = new Color(0xEF, 0xF6, 0xFF);
    private static final Color AZUL_CLARO_BORDA = new Color(0xBF, 0xDB, 0xFE);
    private static final Color CINZA_BORDA_BOTAO = new Color(0xD1, 0xD5, 0xDB);
    private static final Color CINZA_TEXTO_BOTAO = new Color(0x1F, 0x29, 0x37);
    // Cores de erro: as mesmas do Solicitar Orçamento.
    private static final Color VERMELHO = new Color(0xDC, 0x26, 0x26);
    private static final Color VERMELHO_FUNDO = new Color(0xFE, 0xF2, 0xF2);
    private static final Color VERMELHO_BORDA = new Color(0xFC, 0xA5, 0xA5);
    /** Espaço da moldura do campo; com erro, ela vira a borda vermelha. */
    private static final Border MOLDURA_NORMAL = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private static final Border MOLDURA_ERRO = BorderFactory.createLineBorder(VERMELHO, 2);

    // Caracteres aceitos na digitação (o resto é descartado, também ao colar).
    private static final String SO_DIGITOS = "[0-9]";
    private static final String SO_DATA = "[0-9/]";
    private static final String SO_QUILOMETRAGEM = "[0-9.]";
    private static final String SO_VALOR = "[0-9.,]";

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
    /** "Ver atendimentos anteriores" do mockup: fundo azul-claro, texto azul em negrito. */
    private final JButton botaoHistorico = new Botao("Ver atendimentos anteriores", AZUL_CLARO,
            AZUL_CLARO_BORDA, AZUL_TITULO, null, 12f);

    // --- Segmento do Serviço -----------------------------------------
    private final JRadioButton radioModulos =
            new JRadioButton("Reparação / Programação de Módulos (ECU)", true);
    private final JRadioButton radioChaves =
            new JRadioButton("Programação / Confecção de chaves");

    // --- Dados Técnicos ----------------------------------------------
    private final CardLayout cartoes = new CardLayout();
    private final JPanel painelDadosTecnicos = new JPanel(cartoes);
    /** Quebra a linha na largura da janela (antes, um JLabel longo criava rolagem horizontal). */
    private final JTextArea textoCamposExigidos = new JTextArea(" ");
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
    // Estilos do mockup: Voltar / Cancelar branco com borda cinza; Imprimir OS
    // branco com borda e texto azuis e ícone de impressora; Salvar azul cheio.
    // Salvar rascunho e Incluir item não estão no mockup: seguem o mais próximo.
    private final JButton botaoVoltar = Botao.neutro("Voltar / Cancelar");
    private final JButton botaoIncluirItem = Botao.contorno("+ Incluir item", null);
    private final JButton botaoRascunho = Botao.neutro("Salvar rascunho");
    private final JButton botaoImprimir = Botao.contorno("Imprimir OS", new IconeImpressora());
    private final JButton botaoSalvar = Botao.primario("Salvar Ordem de Serviço");

    // --- Validação (mesmo padrão do Solicitar Orçamento) --------------
    /** FA-11A.1: "identificação dos campos pendentes e a mensagem de cada pendência". */
    private final JPanel caixaPendencias = new JPanel(new BorderLayout());
    private final JLabel textoPendencias = new JLabel(" ");
    private final List<Campo> camposFormulario = new ArrayList<>();
    private Campo cOrcamento;
    private Campo cPrazo;
    private Campo cQuilometragem;
    private Campo cRelato;
    private Campo cCondicao;
    private Campo cDiagnostico;
    private Campo cServicoExecutado;
    private Campo cTecnico;
    private Campo cObservacoes;

    /** Impede que o preenchimento automático dispare os eventos de troca. */
    private boolean carregando;

    public TelaRegistrarOrdemServico() {
        super("União Evolutions - Registrar Ordem de Serviço");
        // O "X" da janela não fecha: só o botão "Voltar / Cancelar" (FA-00).
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1060, 880);
        setLocationRelativeTo(null);
        criarCamposValidados();
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
        // Acompanha a largura da janela: só há rolagem vertical.
        JPanel conteudo = new PainelLarguraDaJanela();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(CINZA_FUNDO);
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel obrigatorios = new JLabel("Campos marcados com * são de preenchimento obrigatório.");
        obrigatorios.setFont(obrigatorios.getFont().deriveFont(Font.ITALIC, 11f));
        obrigatorios.setForeground(CINZA_ROTULO);
        obrigatorios.setAlignmentX(LEFT_ALIGNMENT);
        conteudo.add(obrigatorios);
        conteudo.add(Box.createVerticalStrut(8));

        montarCaixaPendencias();
        conteudo.add(caixaPendencias);

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
                        cOrcamento.painel(),
                        criarCampo("Nº da OS", campoNumeroOS),
                        criarCampo("Data de abertura *", campoDataAbertura),
                        cPrazo.painel(),
                        criarCampo("Situação", rotuloSituacao)},
                new double[]{1.4, 1, 1, 1.2, 1}));
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
                        cQuilometragem.painel(),
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

        textoCamposExigidos.setFont(new JLabel().getFont().deriveFont(Font.PLAIN, 11f));
        textoCamposExigidos.setForeground(CINZA_ROTULO);
        textoCamposExigidos.setEditable(false);
        textoCamposExigidos.setFocusable(false);
        textoCamposExigidos.setOpaque(false);
        textoCamposExigidos.setLineWrap(true);
        textoCamposExigidos.setWrapStyleWord(true);
        textoCamposExigidos.setBorder(null);
        textoCamposExigidos.setAlignmentX(LEFT_ALIGNMENT);

        JPanel secao = criarSecao("DADOS TÉCNICOS");
        secao.add(textoCamposExigidos);
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
        bloco.add(criarLinha(new JComponent[]{cRelato.painel()}, new double[]{1}));
        bloco.add(criarLinha(new JComponent[]{cCondicao.painel()}, new double[]{1}));
        bloco.add(criarLinha(new JComponent[]{cDiagnostico.painel()}, new double[]{1}));
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
        bloco.add(criarLinha(new JComponent[]{cServicoExecutado.painel()}, new double[]{1}));
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

        // "Incluir item" (FA-11B) no canto superior direito da seção.
        JPanel secao = criarSecao("EXECUÇÃO", botaoIncluirItem);
        secao.add(criarLinha(
                new JComponent[]{cTecnico.painel(), cObservacoes.painel()},
                new double[]{1, 2}));
        secao.add(aviso);
        secao.add(Box.createVerticalStrut(10));
        secao.add(rolagemItens);
        secao.add(Box.createVerticalStrut(6));
        secao.add(rotuloTotal);
        return secao;
    }

    /** Botões do rodapé do mockup, mais o do FA-11C. O do FA-11B fica na seção EXECUÇÃO. */
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
        for (JButton botao : new JButton[]{botaoVoltar, botaoRascunho, botaoImprimir, botaoSalvar}) {
            c.gridx++;
            barra.add(botao, c);
        }
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

        // A digitação descarta o que não é permitido (também ao colar).
        restringir(campoQuilometragem, SO_QUILOMETRAGEM, 9);
        restringir(campoPrazoEstimado, SO_DATA, 10);

        // (a) validação ao perder o foco.
        aoPerderFoco(campoPrazoEstimado, () -> {
            // A data digitada sem barras (ddmmaaaa) ganha as barras.
            Date data = Validacoes.converterData(campoPrazoEstimado.getText());
            if (data != null) {
                campoPrazoEstimado.setText(DATA.format(data));
            }
            cPrazo.validar();
        });
        aoPerderFoco(campoQuilometragem, cQuilometragem::validar);
        aoPerderFoco(campoRelatoInicial, cRelato::validar);
        aoPerderFoco(campoCondicaoRecebimento, cCondicao::validar);
        aoPerderFoco(campoDiagnosticoInicial, cDiagnostico::validar);
        aoPerderFoco(campoServicoExecutado, cServicoExecutado::validar);
        aoPerderFoco(campoObservacoes, cObservacoes::validar);
        comboTecnico.addActionListener(evento -> {
            if (!carregando) {
                cTecnico.validar();
            }
        });
    }

    // =================================================================
    // Validação dos campos (momentos (a) e (b), como no Solicitar Orçamento)
    // =================================================================

    /**
     * Cria os campos validados e a regra de cada um. Cada regra roda ao
     * perder o foco (a) e de novo ao salvar, com o formulário inteiro (b).
     */
    private void criarCamposValidados() {
        cOrcamento = new Campo("Orçamento associado", "Orçamento associado *", comboOrcamento)
                .regra(() -> comboOrcamento.getSelectedItem() == null
                        ? "Selecione o orçamento que originará a Ordem de Serviço." : null);
        cPrazo = new Campo("Prazo estimado", "Prazo estimado * (dd/mm/aaaa)", campoPrazoEstimado)
                .regra(this::validarPrazo);
        cQuilometragem = new Campo("Quilometragem", "Quilometragem *", campoQuilometragem)
                .regra(this::validarQuilometragem);

        BooleanSupplier modulos = () -> Segmento.MODULOS.equals(segmentoSelecionado());
        BooleanSupplier chaves = () -> Segmento.CHAVES.equals(segmentoSelecionado());
        cRelato = new Campo("Sintomas relatados pelo cliente", "Sintomas relatados pelo cliente *",
                campoRelatoInicial).quando(modulos).doSegmento(Segmento.MODULOS)
                .regra(() -> Validacoes.validarObrigatorio(campoRelatoInicial.getText()));
        cCondicao = new Campo("Condição de recebimento", "Condição de recebimento *",
                campoCondicaoRecebimento).quando(modulos).doSegmento(Segmento.MODULOS)
                .regra(() -> Validacoes.validarObrigatorio(campoCondicaoRecebimento.getText()));
        cDiagnostico = new Campo("Diagnóstico em bancada", "Diagnóstico em bancada *",
                campoDiagnosticoInicial).quando(modulos).doSegmento(Segmento.MODULOS)
                .regra(() -> Validacoes.validarObrigatorio(campoDiagnosticoInicial.getText()));
        cServicoExecutado = new Campo("Serviço de chaves a executar", "Serviço de chaves a executar *",
                campoServicoExecutado).quando(chaves).doSegmento(Segmento.CHAVES)
                .regra(() -> Validacoes.validarObrigatorio(campoServicoExecutado.getText()));

        cTecnico = new Campo("Técnico responsável", "Técnico responsável *", comboTecnico)
                .regra(() -> comboTecnico.getSelectedItem() == null
                        ? "Selecione o técnico responsável pela Ordem de Serviço." : null);
        cObservacoes = new Campo("Observações internas", "Observações internas", campoObservacoes)
                .regra(() -> Validacoes.validarTamanho(campoObservacoes.getText()));

        // Na ordem da tela, para a caixa de pendências listar de cima para baixo.
        camposFormulario.addAll(List.of(cOrcamento, cPrazo, cQuilometragem, cRelato, cCondicao, cDiagnostico,
                cServicoExecutado, cTecnico, cObservacoes));
    }

    private String validarPrazo() {
        String texto = campoPrazoEstimado.getText().trim();
        if (texto.isEmpty()) {
            return Validacoes.MSG_OBRIGATORIO;
        }
        Date prazo = Validacoes.converterData(texto);
        if (prazo == null) {
            return "Informe uma data válida no formato dd/mm/aaaa.";
        }
        Date abertura = Validacoes.converterData(campoDataAbertura.getText());
        if (abertura != null && prazo.before(abertura)) {
            return "O prazo estimado não pode ser anterior à data de abertura.";
        }
        return null;
    }

    private String validarQuilometragem() {
        String texto = campoQuilometragem.getText().trim();
        if (texto.isEmpty()) {
            return Validacoes.MSG_OBRIGATORIO;
        }
        // Ponto só como separador de milhar: 85400 ou 85.400.
        if (!texto.matches("\\d{1,7}") && !texto.matches("\\d{1,3}(\\.\\d{3}){1,2}")) {
            return "Informe a quilometragem só com números (ex.: 85.400).";
        }
        return Integer.parseInt(texto.replace(".", "")) <= 0
                ? "Informe uma quilometragem maior que zero." : null;
    }

    /** Caixa vermelha do topo do formulário (FA-11A.1). */
    private void montarCaixaPendencias() {
        caixaPendencias.setBackground(VERMELHO_FUNDO);
        caixaPendencias.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(0, 0, 10, 0),
                        BorderFactory.createLineBorder(VERMELHO_BORDA)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        caixaPendencias.setAlignmentX(LEFT_ALIGNMENT);
        textoPendencias.setForeground(VERMELHO);
        textoPendencias.setFont(textoPendencias.getFont().deriveFont(Font.PLAIN, 12f));
        caixaPendencias.add(textoPendencias, BorderLayout.CENTER);
        caixaPendencias.setVisible(false);
    }

    /**
     * Momento (b): valida os campos informados de uma vez. Com pendências,
     * marca cada campo, lista todas na caixa do topo e devolve false
     * (FA-11A.1); o que já foi digitado fica preservado (FA-11A.3).
     */
    private boolean validarCampos(List<Campo> campos, String acao) {
        List<String> pendencias = new ArrayList<>();
        for (Campo campo : campos) {
            String erro = campo.validar();
            if (erro != null) {
                pendencias.add(campo.textoPendencia(erro));
            }
        }
        if (pendencias.isEmpty()) {
            caixaPendencias.setVisible(false);
            return true;
        }
        StringBuilder texto = new StringBuilder("<html><b>Corrija " + pendencias.size()
                + (pendencias.size() == 1 ? " pendência" : " pendências") + " antes de " + acao + ":</b>");
        for (String pendencia : pendencias) {
            texto.append("<br>• ").append(pendencia);
        }
        textoPendencias.setText(texto.append("</html>").toString());
        caixaPendencias.setVisible(true);
        caixaPendencias.setMaximumSize(new Dimension(Integer.MAX_VALUE, caixaPendencias.getPreferredSize().height));
        caixaPendencias.revalidate();
        SwingUtilities.invokeLater(() ->
                caixaPendencias.scrollRectToVisible(new Rectangle(caixaPendencias.getSize())));
        return false;
    }

    /** Ao trocar de segmento ou de formulário, some a marca dos campos que não valem mais. */
    private void limparMarcas(boolean todas) {
        for (Campo campo : camposFormulario) {
            if (todas || !campo.aplicavel()) {
                campo.limpar();
            }
        }
        if (todas) {
            caixaPendencias.setVisible(false);
        }
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
        textoCamposExigidos.setText("Campos exigidos no segmento " + segmento + ": "
                + String.join(" | ", campos));
        cartoes.show(painelDadosTecnicos, segmento);
        limparMarcas(false);

        // Passo 9: técnicos habilitados.
        try {
            List<Usuario> tecnicos = controladora.consultarTecnicosHabilitados(segmento);
            carregando = true;
            comboTecnico.removeAllItems();
            for (Usuario tecnico : tecnicos) {
                comboTecnico.addItem(tecnico);
            }
            carregando = false;
            if (tecnicos.isEmpty()) {
                // FA-09.1
                cTecnico.validar();
                mostrarAviso("Não há técnico habilitado disponível para este segmento");
            } else {
                cTecnico.limpar();
            }
        } catch (SQLException erro) {
            mostrarErroDeBanco(erro);
        }
    }

    /**
     * Passo 11: revalida o formulário inteiro (momento b) e, sem
     * pendências, confirma o registro. Com pendências, FA-11A.
     */
    private void salvar() {
        if (!validarCampos(camposFormulario, "salvar a Ordem de Serviço")) {
            return;   // FA-11A
        }
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

    /**
     * FA-11C: salva a Ordem de Serviço como rascunho. Valida só o que o
     * rascunho grava (os dados técnicos podem ficar para depois: FA-11C.3).
     */
    private void salvarRascunho() {
        if (!validarCampos(List.of(cOrcamento, cPrazo, cQuilometragem, cTecnico, cObservacoes),
                "salvar o rascunho")) {
            return;
        }
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
        // FA-11B.1: serviço ou peça, quantidade e valor, num formulário só,
        // com a mesma validação dos campos da tela.
        JTextField campoServico = new JTextField();
        JTextField campoQuantidade = new JTextField("1");
        JTextField campoValor = new JTextField();
        restringir(campoQuantidade, SO_DIGITOS, 4);
        restringir(campoValor, SO_VALOR, 15);
        Campo cServico = new Campo("Serviço ou peça", "Serviço ou peça a incluir *", campoServico)
                .regra(() -> Validacoes.validarObrigatorio(campoServico.getText()));
        Campo cQuantidade = new Campo("Quantidade", "Quantidade *", campoQuantidade).regra(() -> {
            String texto = campoQuantidade.getText().trim();
            if (texto.isEmpty()) {
                return Validacoes.MSG_OBRIGATORIO;
            }
            return Integer.parseInt(texto) <= 0 ? "Informe uma quantidade maior que zero." : null;
        });
        Campo cValor = new Campo("Valor unitário", "Valor unitário (R$) *", campoValor).regra(() -> {
            String texto = campoValor.getText().trim();
            if (texto.isEmpty()) {
                return Validacoes.MSG_OBRIGATORIO;
            }
            Double valor = Validacoes.converterValor(texto);
            if (valor == null) {
                return "Informe um valor válido (ex.: 120,00 ou 1.250,00).";
            }
            return valor <= 0 ? "Informe um valor maior que zero." : null;
        });
        aoPerderFoco(campoServico, cServico::validar);
        aoPerderFoco(campoQuantidade, cQuantidade::validar);
        aoPerderFoco(campoValor, cValor::validar);

        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.add(criarLinha(new JComponent[]{cServico.painel()}, new double[]{1}));
        corpo.add(criarLinha(new JComponent[]{cQuantidade.painel(), cValor.painel()}, new double[]{1, 1}));
        corpo.setPreferredSize(new Dimension(480, corpo.getPreferredSize().height));
        if (!pedirComValidacao(corpo, "Incluir item", cServico, cQuantidade, cValor)) {
            return;
        }
        String servico = campoServico.getText().trim();
        int quantidade = Integer.parseInt(campoQuantidade.getText().trim());
        double valor = Validacoes.converterValor(campoValor.getText());

        try {
            // FA-11B.2: exibe a exigência de justificativa.
            String exigencia = controladora.incluirItemOrdemServico(servico, quantidade, valor);

            // FA-11B.3: justificativa obrigatória (RN-05).
            JTextField campoJustificativa = new JTextField();
            Campo cJustificativa = new Campo("Justificativa", "Justificativa da alteração *", campoJustificativa)
                    .regra(() -> Validacoes.validarObrigatorio(campoJustificativa.getText()));
            aoPerderFoco(campoJustificativa, cJustificativa::validar);
            JPanel corpoJustificativa = new JPanel(new BorderLayout(0, 10));
            corpoJustificativa.add(new JLabel("<html><div style='width:420px'>" + exigencia + "</div></html>"),
                    BorderLayout.NORTH);
            corpoJustificativa.add(cJustificativa.painel(), BorderLayout.CENTER);
            if (!pedirComValidacao(corpoJustificativa, "Justificativa da alteração (RN-05)", cJustificativa)) {
                return;
            }

            // FA-11B.3 e FA-11B.4
            String resultado = controladora.registrarOSComAlteracao(
                    servico, quantidade, valor, campoJustificativa.getText().trim());
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
        // Aceita dd/mm/aaaa e ddmmaaaa, como o "Válido até" do Solicitar Orçamento.
        Date prazo = Validacoes.converterData(texto);
        if (prazo == null) {
            throw new IllegalArgumentException("Informe o prazo estimado no formato dd/mm/aaaa.");
        }
        return prazo;
    }

    private int lerInteiro(String texto, String mensagem) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    /**
     * Diálogo que só fecha no OK com todos os campos válidos (momento b);
     * cada campo também valida ao perder o foco (momento a). Com erro, o
     * diálogo volta com o que foi digitado e as marcas nos campos.
     */
    private boolean pedirComValidacao(JComponent corpo, String titulo, Campo... campos) {
        while (true) {
            int resposta = JOptionPane.showConfirmDialog(this, corpo, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (resposta != JOptionPane.OK_OPTION) {
                return false;
            }
            boolean valido = true;
            for (Campo campo : campos) {
                valido &= campo.validar() == null;
            }
            if (valido) {
                return true;
            }
        }
    }

    /**
     * O campo descarta, ao digitar ou colar, todo caractere fora do padrão e
     * não passa do tamanho máximo (mesmo filtro do Solicitar Orçamento).
     */
    private static void restringir(JTextComponent campo, String permitido, int maximo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int posicao, String texto, AttributeSet atributos)
                    throws BadLocationException {
                replace(fb, posicao, 0, texto, atributos);
            }

            @Override
            public void replace(FilterBypass fb, int posicao, int tamanho, String texto, AttributeSet atributos)
                    throws BadLocationException {
                StringBuilder aceito = new StringBuilder();
                if (texto != null) {
                    for (char c : texto.toCharArray()) {
                        if (String.valueOf(c).matches(permitido)) {
                            aceito.append(c);
                        }
                    }
                }
                int livre = maximo - (fb.getDocument().getLength() - tamanho);
                if (aceito.length() > livre) {
                    aceito.setLength(Math.max(0, livre));
                }
                if (aceito.length() == 0 && tamanho == 0) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                super.replace(fb, posicao, tamanho, aceito.toString(), atributos);
            }
        });
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
        limparMarcas(true);
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
        return criarSecao(titulo, null);
    }

    /** Cartão branco com título e, opcionalmente, um componente no canto superior direito. */
    private JPanel criarSecao(String titulo, JComponent direita) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        painel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 12f));
        rotulo.setForeground(AZUL_TITULO);

        JPanel cabecalho = new JPanel(new BorderLayout(12, 0));
        cabecalho.setOpaque(false);
        cabecalho.setAlignmentX(LEFT_ALIGNMENT);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        cabecalho.add(rotulo, BorderLayout.WEST);
        if (direita != null) {
            cabecalho.add(direita, BorderLayout.EAST);
        }
        cabecalho.setMaximumSize(new Dimension(Integer.MAX_VALUE, cabecalho.getPreferredSize().height));

        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2, 0xE6, 0xEC)),
                BorderFactory.createEmptyBorder(12, 16, 14, 16)));
        painel.add(cabecalho);
        return painel;
    }

    /**
     * Campo só de leitura: rótulo em cima, componente embaixo. Tem a mesma
     * moldura dos campos validados, para as linhas ficarem alinhadas.
     */
    private JPanel criarCampo(String rotulo, JComponent componente) {
        JPanel painel = new JPanel(new BorderLayout(0, 3));
        painel.setOpaque(false);

        JLabel label = new JLabel(rotulo);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setForeground(CINZA_ROTULO);

        componente.setPreferredSize(new Dimension(10, 26));
        JPanel moldura = new JPanel(new BorderLayout());
        moldura.setOpaque(false);
        moldura.setBorder(MOLDURA_NORMAL);
        moldura.add(componente, BorderLayout.CENTER);
        painel.add(label, BorderLayout.NORTH);
        painel.add(moldura, BorderLayout.CENTER);
        return painel;
    }

    /** Linha de campos, dividindo a largura conforme os pesos. */
    private JPanel criarLinha(JComponent[] campos, double[] pesos) {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);
        painel.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        // Topo: um campo com mensagem de erro embaixo não desalinha os vizinhos.
        c.anchor = GridBagConstraints.NORTH;
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

    // =================================================================
    // Classes internas
    // =================================================================

    /**
     * Um campo com validação, como no Solicitar Orçamento: rótulo,
     * componente e regra. Com erro, a moldura em volta do componente fica
     * vermelha e a mensagem aparece embaixo dele. A moldura (e não a borda
     * do próprio componente) recebe o vermelho porque esta tela usa o look
     * and feel do sistema, que pode ignorar bordas e fundos customizados.
     */
    private final class Campo {
        private final String nome;
        private final String rotuloTela;
        private final JComponent componente;
        private final JPanel moldura = new JPanel(new BorderLayout());
        private final JLabel mensagem = new JLabel(" ") {
            private static final long serialVersionUID = 1L;

            // A mensagem não alarga a coluna do formulário.
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(10, super.getPreferredSize().height);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        private Supplier<String> regra = () -> null;
        private BooleanSupplier aplicavel = () -> true;
        private String segmento;

        Campo(String nome, String rotuloTela, JComponent componente) {
            this.nome = nome;
            this.rotuloTela = rotuloTela;
            this.componente = componente;
            componente.setPreferredSize(new Dimension(10, 26));
            moldura.setOpaque(false);
            moldura.setBorder(MOLDURA_NORMAL);
            moldura.add(componente, BorderLayout.CENTER);
            mensagem.setFont(mensagem.getFont().deriveFont(Font.BOLD, 10.5f));
            mensagem.setForeground(VERMELHO);
        }

        Campo regra(Supplier<String> novaRegra) {
            this.regra = novaRegra;
            return this;
        }

        Campo quando(BooleanSupplier condicao) {
            this.aplicavel = condicao;
            return this;
        }

        /** Campo exigido pela RN-03: a pendência cita o segmento. */
        Campo doSegmento(String nomeSegmento) {
            this.segmento = nomeSegmento;
            return this;
        }

        boolean aplicavel() {
            return aplicavel.getAsBoolean();
        }

        JPanel painel() {
            JPanel painel = new JPanel(new BorderLayout(0, 3));
            painel.setOpaque(false);
            JLabel rotulo = new JLabel(rotuloTela);
            rotulo.setFont(rotulo.getFont().deriveFont(Font.PLAIN, 11f));
            rotulo.setForeground(CINZA_ROTULO);
            painel.add(rotulo, BorderLayout.NORTH);
            painel.add(moldura, BorderLayout.CENTER);
            painel.add(mensagem, BorderLayout.SOUTH);
            return painel;
        }

        /** Valida e marca (ou desmarca) o campo. Devolve a mensagem de erro, ou null. */
        String validar() {
            if (!aplicavel()) {
                limpar();
                return null;
            }
            String erro = regra.get();
            if (erro == null) {
                limpar();
            } else {
                marcar(erro);
            }
            return erro;
        }

        void marcar(String texto) {
            moldura.setBorder(MOLDURA_ERRO);
            mensagem.setText(texto);
            mensagem.setToolTipText(texto);
            mensagem.setName("erro:" + nome);
        }

        void limpar() {
            moldura.setBorder(MOLDURA_NORMAL);
            mensagem.setText(" ");
            mensagem.setToolTipText(null);
            mensagem.setName(null);
        }

        /** Linha da caixa de pendências: "Diagnóstico em bancada: campo obrigatório para o segmento Modulos." */
        String textoPendencia(String erro) {
            if (Validacoes.MSG_OBRIGATORIO.equals(erro)) {
                return nome + ": campo obrigatório" + (segmento == null ? "." : " para o segmento " + segmento + ".");
            }
            return nome + ": " + erro;
        }
    }

    /**
     * Botão com o estilo do mockup: fundo, borda arredondada, texto em
     * negrito e ícone opcional. Desenha tudo sozinho porque esta tela usa o
     * look and feel do sistema (GTK, Windows), que ignora a cor de fundo e o
     * arredondamento de um JButton comum.
     */
    private static final class Botao extends JButton {
        private static final long serialVersionUID = 1L;
        private static final int RAIO = 10;

        private final Color fundo;
        private final Color borda;

        Botao(String texto, Color fundo, Color borda, Color cor, Icon icone, float tamanhoFonte) {
            super(texto, icone);
            this.fundo = fundo;
            this.borda = borda;
            setForeground(cor);
            setFont(getFont().deriveFont(Font.BOLD, tamanhoFonte));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setIconTextGap(8);
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /** "Salvar Ordem de Serviço": azul cheio, texto branco. */
        static Botao primario(String texto) {
            return new Botao(texto, AZUL_CABECALHO, AZUL_CABECALHO, Color.WHITE, null, 13f);
        }

        /** "Imprimir OS": branco, borda e texto azuis. */
        static Botao contorno(String texto, Icon icone) {
            return new Botao(texto, Color.WHITE, AZUL_CABECALHO, AZUL_CABECALHO, icone, 13f);
        }

        /** "Voltar / Cancelar": branco, borda cinza, texto escuro. */
        static Botao neutro(String texto) {
            return new Botao(texto, Color.WHITE, CINZA_BORDA_BOTAO, CINZA_TEXTO_BOTAO, null, 13f);
        }

        @Override
        public Dimension getPreferredSize() {
            if (isPreferredSizeSet()) {
                return super.getPreferredSize();
            }
            FontMetrics fm = getFontMetrics(getFont());
            Insets in = getInsets();
            Icon icone = getIcon();
            int largura = fm.stringWidth(getText()) + (icone == null ? 0 : icone.getIconWidth() + getIconTextGap());
            int altura = Math.max(fm.getHeight(), icone == null ? 0 : icone.getIconHeight());
            return new Dimension(largura + in.left + in.right, altura + in.top + in.bottom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ButtonModel modelo = getModel();
            Color corFundo = fundo;
            Color corBorda = borda;
            Color corTexto = getForeground();
            if (!isEnabled()) {
                corFundo = misturar(fundo, Color.WHITE, 0.55f);
                corBorda = misturar(borda, Color.WHITE, 0.55f);
                corTexto = misturar(corTexto, Color.WHITE, 0.55f);
            } else if (modelo.isPressed()) {
                corFundo = misturar(fundo, Color.BLACK, 0.12f);
            } else if (modelo.isRollover()) {
                corFundo = fundo.equals(Color.WHITE) ? new Color(0xF3, 0xF4, 0xF6) : misturar(fundo, Color.WHITE, 0.12f);
            }
            int w = getWidth();
            int h = getHeight();
            g2.setColor(corFundo);
            g2.fillRoundRect(0, 0, w - 1, h - 1, RAIO, RAIO);
            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, RAIO, RAIO);

            // Ícone e texto centralizados.
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            Icon icone = getIcon();
            int larguraIcone = icone == null ? 0 : icone.getIconWidth() + getIconTextGap();
            int x = (w - fm.stringWidth(getText()) - larguraIcone) / 2;
            if (icone != null) {
                g2.setColor(corTexto);
                icone.paintIcon(this, g2, x, (h - icone.getIconHeight()) / 2);
            }
            g2.setColor(corTexto);
            g2.drawString(getText(), x + larguraIcone, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }

        private static Color misturar(Color a, Color b, float proporcaoB) {
            float p = 1 - proporcaoB;
            return new Color(Math.round(a.getRed() * p + b.getRed() * proporcaoB),
                    Math.round(a.getGreen() * p + b.getGreen() * proporcaoB),
                    Math.round(a.getBlue() * p + b.getBlue() * proporcaoB));
        }
    }

    /** Ícone de impressora do botão "Imprimir OS" do mockup, na cor do texto. */
    private static final class IconeImpressora implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.translate(x, y);
            g2.drawRect(4, 1, 8, 4);              // papel saindo por cima
            g2.drawRoundRect(1, 5, 14, 7, 3, 3);  // corpo
            g2.drawRect(4, 9, 8, 6);              // papel impresso
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    /** O formulário acompanha a largura da janela: as linhas se ajustam e só há rolagem vertical. */
    private static final class PainelLarguraDaJanela extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle area, int orientacao, int direcao) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle area, int orientacao, int direcao) {
            return Math.max(16, area.height - 32);
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

    private static DecimalFormat criarFormatoMoeda() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("'R$ '#,##0.00", simbolos);
    }
}
