package uniaoevolutions;

import uniaoevolutions.view.TelaRegistrarOrdemServico;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Ponto de entrada: abre a tela do caso de uso. Não faz parte da modelagem. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorado) {
            // Sem o look and feel do sistema, usa o padrão do Swing.
        }

        // Cria a janela na Event Dispatch Thread do Swing.
        SwingUtilities.invokeLater(() -> new TelaRegistrarOrdemServico().setVisible(true));
    }
}
