package uniaoevolutions;

import uniaoevolutions.negocio.Segmento;
import uniaoevolutions.negocio.Usuario;
import uniaoevolutions.persistencia.UsuarioDAO;
import uniaoevolutions.view.TelaRegistrarOrdemServico;
import uniaoevolutions.view.TelaSolicitarOrcamento;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ACRÉSCIMO: menu para abrir as telas dos dois casos de uso sem digitar nada
 * na linha de comando. Não faz parte da modelagem (como o MainRegistrarOrdemServico), por isso fica
 * fora do pacote view e não passa pelas controladoras.
 *
 * Não há caso de uso de autenticação: o usuário escolhido na lista faz o papel
 * do usuário logado (PRE-01) do Solicitar Orçamento. A lista usa só métodos de
 * UsuarioDAO que já estão no diagrama de classes (buscarTecnicosHabilitados
 * dos dois segmentos, que juntos trazem todos os técnicos e o administrador).
 *
 * Quando a tela aberta é fechada (Voltar/Cancelar, Desistir ou o "X"), o
 * menu volta a aparecer.
 */
public class MenuPrincipal extends JFrame {

    private final JComboBox<Usuario> comboUsuario = new JComboBox<>();
    private final JLabel rotuloBanco = new JLabel(" ");
    private final JButton botaoRegistrarOS = new JButton("Registrar Ordem de Serviço");
    private final JButton botaoSolicitarOrcamento = new JButton("Solicitar Orçamento");
    private final JButton botaoRecarregar = new JButton("Tentar conectar de novo");
    private final JButton botaoSair = new JButton("Sair");

    public MenuPrincipal() {
        super("União Evolutions - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        montarTela();
        botaoRegistrarOS.addActionListener(e -> abrirRegistrarOS());
        botaoSolicitarOrcamento.addActionListener(e -> abrirSolicitarOrcamento());
        botaoRecarregar.addActionListener(e -> carregarUsuarios());
        botaoSair.addActionListener(e -> System.exit(0));
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(this::carregarUsuarios);
    }

    private void montarTela() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titulo = new JLabel("União Evolutions");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        painel.add(alinhar(titulo));
        painel.add(Box.createVerticalStrut(14));

        painel.add(alinhar(new JLabel("Usuário logado (para o Solicitar Orçamento):")));
        painel.add(Box.createVerticalStrut(4));
        comboUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboUsuario.getPreferredSize().height));
        painel.add(alinhar(comboUsuario));
        painel.add(Box.createVerticalStrut(14));

        for (JButton botao : new JButton[]{botaoSolicitarOrcamento, botaoRegistrarOS}) {
            botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            painel.add(alinhar(botao));
            painel.add(Box.createVerticalStrut(8));
        }

        rotuloBanco.setFont(rotuloBanco.getFont().deriveFont(Font.PLAIN, 11f));
        painel.add(alinhar(rotuloBanco));
        painel.add(Box.createVerticalStrut(8));

        JPanel rodape = new JPanel();
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.X_AXIS));
        rodape.add(botaoRecarregar);
        rodape.add(Box.createHorizontalGlue());
        rodape.add(botaoSair);
        painel.add(alinhar(rodape));

        painel.setPreferredSize(new Dimension(440, painel.getPreferredSize().height));
        setContentPane(painel);
    }

    private static Component alinhar(javax.swing.JComponent componente) {
        componente.setAlignmentX(LEFT_ALIGNMENT);
        return componente;
    }

    /** Preenche a lista de usuários; também serve de teste da conexão com o banco. */
    private void carregarUsuarios() {
        comboUsuario.removeAllItems();
        try {
            UsuarioDAO dao = new UsuarioDAO();
            Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
            for (String segmento : new String[]{Segmento.MODULOS, Segmento.CHAVES}) {
                for (Usuario usuario : dao.buscarTecnicosHabilitados(segmento)) {
                    usuarios.putIfAbsent(usuario.getIdUsuario(), usuario);
                }
            }
            usuarios.values().stream()
                    .sorted((a, b) -> Integer.compare(a.getIdUsuario(), b.getIdUsuario()))
                    .forEach(comboUsuario::addItem);
            rotuloBanco.setText("<html><font color='#15803D'>Banco conectado.</font></html>");
            botaoSolicitarOrcamento.setEnabled(comboUsuario.getItemCount() > 0);
        } catch (SQLException erro) {
            rotuloBanco.setText("<html><font color='#B91C1C'>Sem conexão com o banco.</font></html>");
            botaoSolicitarOrcamento.setEnabled(false);
            JOptionPane.showMessageDialog(this, mensagemDeConexao(erro),
                    "Banco de dados", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Explica as causas mais comuns no XAMPP, com a mensagem original do driver. */
    private static String mensagemDeConexao(SQLException erro) {
        String texto = String.valueOf(erro.getMessage());
        String dica;
        if (texto.contains("Communications link failure") || texto.contains("Connection refused")) {
            dica = "O MySQL não está ligado na porta 3306.\nNo XAMPP: sudo /opt/lampp/lampp startmysql";
        } else if (texto.contains("Unknown database")) {
            dica = "O banco uniao_evolutions não existe.\nImporte: /opt/lampp/bin/mysql -u root < sql/schema.sql";
        } else if (texto.contains("Access denied")) {
            dica = "O usuário root tem senha.\nPonha a senha em SENHA, na persistencia/Conexao.java, e compile de novo.";
        } else if (texto.contains("doesn't exist") || texto.contains("Unknown column")) {
            dica = "O banco é de uma versão antiga.\nImporte de novo: /opt/lampp/bin/mysql -u root < sql/schema.sql";
        } else if (erro.getCause() instanceof ClassNotFoundException || texto.contains("No suitable driver")) {
            dica = "O driver JDBC não está no classpath (lib/mysql-connector-j-8.4.0.jar).";
        } else {
            dica = "Verifique se o MySQL do XAMPP está ligado e se o schema.sql foi importado.";
        }
        return dica + "\n\nMensagem do banco:\n" + texto;
    }

    private void abrirRegistrarOS() {
        usarLookAndFeel(UIManager.getSystemLookAndFeelClassName());   // o mesmo do MainRegistrarOrdemServico
        abrir(new TelaRegistrarOrdemServico());
    }

    private void abrirSolicitarOrcamento() {
        Usuario usuario = (Usuario) comboUsuario.getSelectedItem();
        if (usuario == null) {
            return;
        }
        usarLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());   // o mesmo do MainSolicitarOrcamento
        abrir(new TelaSolicitarOrcamento(usuario.getIdUsuario()));
    }

    /** Esconde o menu enquanto a tela estiver aberta e o mostra de novo quando ela fechar. */
    private void abrir(JFrame tela) {
        tela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                setVisible(true);
                toFront();
            }
        });
        setVisible(false);
        tela.setVisible(true);
    }

    private static void usarLookAndFeel(String classe) {
        try {
            UIManager.setLookAndFeel(classe);
        } catch (Exception ignorado) {
            // Sem o look and feel, usa o padrão do Swing.
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
