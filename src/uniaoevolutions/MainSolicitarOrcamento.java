package uniaoevolutions;

import uniaoevolutions.view.TelaSolicitarOrcamento;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entrada do Solicitar Orçamento (fora da modelagem). Argumento: idUsuario do usuário
 * logado (PRE-01); padrão 1.
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
            // Sem ele, fica o padrão do Swing.
        }
        int usuario = idUsuarioLogado;
        SwingUtilities.invokeLater(() -> new TelaSolicitarOrcamento(usuario).setVisible(true));
    }
}
