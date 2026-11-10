package uniaoevolutions;

import uniaoevolutions.view.TelaSolicitarOrcamento;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada do caso de uso Solicitar Orçamento (2ª iteração). Não faz
 * parte da modelagem.
 *
 * O MainRegistrarOrdemServico (1ª iteração) continua abrindo o Registrar Ordem de Serviço.
 *
 * Não há caso de uso de autenticação: o usuário logado (PRE-01) é informado
 * no primeiro argumento, como o idUsuario de TB_Usuario. Sem argumento, é o
 * usuário 1 (Gabriel Abraão - Técnico de Módulos, o do mockup).
 *     java ... uniaoevolutions.MainSolicitarOrcamento 3   -> Jamily Sales, Técnico de Chaves
 */
public class MainSolicitarOrcamento {

    public static void main(String[] args) {
        int idUsuarioLogado = 1;
        if (args.length > 0) {
            try {
                idUsuarioLogado = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException erro) {
                System.err.println("Usuário inválido \"" + args[0] + "\"; usando o usuário 1.");
            }
        }
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorado) {
            // Sem o look and feel, usa o padrão do Swing.
        }
        int usuario = idUsuarioLogado;
        SwingUtilities.invokeLater(() -> new TelaSolicitarOrcamento(usuario).setVisible(true));
    }
}
