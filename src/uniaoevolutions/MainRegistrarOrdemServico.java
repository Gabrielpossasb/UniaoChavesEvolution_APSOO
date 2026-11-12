package uniaoevolutions;

import uniaoevolutions.view.TelaRegistrarOrdemServico;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entrada do Registrar Ordem de Serviço (fora da modelagem). */
public class MainRegistrarOrdemServico {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorado) {
            // Sem ele, fica o padrão do Swing.
        }

        // Janela na Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new TelaRegistrarOrdemServico().setVisible(true));
    }
}
