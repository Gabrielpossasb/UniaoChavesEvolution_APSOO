package uniaoevolutions.view;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Apoio aos testes de interface: acha componentes pelo nome, digita, dispara
 * a perda de foco, clica e trata as janelas modais. Roda fora da EDT; toda
 * mexida em componente vai para a EDT.
 */
final class Robo {

    static int falhas = 0;
    static int verificacoes = 0;
    static File pastaImagens = new File(System.getProperty("imagens", "."));

    private Robo() {
    }

    static <T> T naEdt(Callable<T> acao) {
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Exception> erro = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    resultado.set(acao.call());
                } catch (Exception e) {
                    erro.set(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (erro.get() != null) {
            throw new RuntimeException(erro.get());
        }
        return resultado.get();
    }

    static void naEdt(Runnable acao) {
        naEdt(() -> {
            acao.run();
            return null;
        });
    }

    static void esperar(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Espera a fila de eventos esvaziar (inclui os invokeLater da tela). */
    static void sincronizar() {
        for (int i = 0; i < 3; i++) {
            naEdt(() -> { });
            esperar(40);
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Component> T achar(Container raiz, String nome) {
        T achado = naEdt(() -> (T) buscar(raiz, nome));
        if (achado == null) {
            throw new IllegalStateException("Componente não encontrado: " + nome);
        }
        return achado;
    }

    private static Component buscar(Container raiz, String nome) {
        for (Component c : raiz.getComponents()) {
            if (nome.equals(c.getName())) {
                return c;
            }
            if (c instanceof Container) {
                Component dentro = buscar((Container) c, nome);
                if (dentro != null) {
                    return dentro;
                }
            }
        }
        return null;
    }

    /** Digita e dispara a perda de foco do campo - momento (a) da validação. */
    static void digitar(Container raiz, String nome, String texto) {
        JTextComponent campo = achar(raiz, nome);
        naEdt(() -> campo.setText(texto));
        perderFoco(campo);
    }

    static void perderFoco(Component campo) {
        naEdt(() -> {
            for (FocusListener ouvinte : campo.getFocusListeners()) {
                ouvinte.focusLost(new FocusEvent(campo, FocusEvent.FOCUS_LOST, false));
            }
        });
        sincronizar();
    }

    /** Combo editável: escreve no editor e dispara a perda de foco do editor. */
    static void escreverNaCombo(Container raiz, String nome, String texto) {
        JComboBox<?> combo = achar(raiz, nome);
        JTextComponent editor = naEdt(() -> (JTextComponent) combo.getEditor().getEditorComponent());
        naEdt(() -> editor.setText(texto));
        perderFoco(editor);
    }

    static void escolherNaCombo(Container raiz, String nome, int indice) {
        JComboBox<?> combo = achar(raiz, nome);
        naEdt(() -> combo.setSelectedIndex(indice));
        sincronizar();
    }

    /** Clica sem esperar (o clique pode abrir janela modal). */
    static void clicar(Container raiz, String nome) {
        AbstractButton botao = achar(raiz, nome);
        SwingUtilities.invokeLater(botao::doClick);
        esperar(250);
    }

    static JDialog esperarDialogo(String titulo) {
        for (int i = 0; i < 60; i++) {
            JDialog achado = naEdt(() -> {
                for (Window janela : Window.getWindows()) {
                    if (janela instanceof JDialog && janela.isShowing()
                            && ("dialogo:" + titulo).equals(janela.getName())) {
                        return (JDialog) janela;
                    }
                }
                return null;
            });
            if (achado != null) {
                esperar(150);
                return achado;
            }
            esperar(100);
        }
        throw new IllegalStateException("Janela não apareceu: " + titulo);
    }

    static boolean haDialogo(String titulo) {
        return naEdt(() -> {
            for (Window janela : Window.getWindows()) {
                if (janela.isShowing() && ("dialogo:" + titulo).equals(janela.getName())) {
                    return true;
                }
            }
            return false;
        });
    }

    static void clicarNoDialogo(JDialog dialogo, String botao) {
        clicar(dialogo.getContentPane(), "botao:" + botao);
        sincronizar();
    }

    static String texto(Container raiz, String nome) {
        Component c = achar(raiz, nome);
        return naEdt(() -> {
            if (c instanceof JTextComponent) {
                return ((JTextComponent) c).getText();
            }
            if (c instanceof JLabel) {
                return ((JLabel) c).getText();
            }
            if (c instanceof AbstractButton) {
                return ((AbstractButton) c).getText();
            }
            return String.valueOf(c);
        });
    }

    static boolean visivel(Container raiz, String nome) {
        Component c = achar(raiz, nome);
        return naEdt(c::isShowing);
    }

    static String textoDoDialogo(JDialog dialogo) {
        return naEdt(() -> juntarTextos(dialogo.getContentPane()));
    }

    private static String juntarTextos(Container raiz) {
        StringBuilder texto = new StringBuilder();
        for (Component c : raiz.getComponents()) {
            if (c instanceof JLabel && ((JLabel) c).getText() != null) {
                texto.append(((JLabel) c).getText()).append(" | ");
            } else if (c instanceof JTextComponent) {
                texto.append(((JTextComponent) c).getText()).append(" | ");
            }
            if (c instanceof Container) {
                texto.append(juntarTextos((Container) c));
            }
        }
        return texto.toString().replaceAll("<[^>]+>", "");
    }

    // ---------------------------------------------------------------
    // Acesso a membros privados (só para observar e para anexar a foto)
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    static <T> T campoPrivado(Object alvo, String nome) {
        try {
            Field campo = alvo.getClass().getDeclaredField(nome);
            campo.setAccessible(true);
            return (T) campo.get(alvo);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static void chamarPrivado(Object alvo, String nome, Class<?> tipo, Object argumento) {
        naEdt(() -> {
            try {
                Method metodo = alvo.getClass().getDeclaredMethod(nome, tipo);
                metodo.setAccessible(true);
                metodo.invoke(alvo, argumento);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
        sincronizar();
    }

    // ---------------------------------------------------------------
    // Verificações e imagens
    // ---------------------------------------------------------------

    static void verificar(boolean condicao, String descricao) {
        verificacoes++;
        if (condicao) {
            System.out.println("  ok   " + descricao);
        } else {
            falhas++;
            System.out.println("  FALHA " + descricao);
        }
    }

    static void verificarIgual(Object esperado, Object obtido, String descricao) {
        boolean igual = esperado == null ? obtido == null : esperado.equals(obtido);
        verificar(igual, descricao + (igual ? "" : "  [esperado=" + esperado + " | obtido=" + obtido + "]"));
    }

    static void verificarContem(String trecho, String texto, String descricao) {
        boolean contem = texto != null && texto.replaceAll("<[^>]+>", "").contains(trecho);
        verificar(contem, descricao + (contem ? "" : "  [procurado=\"" + trecho + "\" em \"" + texto + "\"]"));
    }

    /** Página inteira do formulário (não só a parte visível da janela). */
    static void salvarFormulario(JFrame janela, JComponent conteudo, String arquivo) {
        naEdt(() -> {
            java.awt.Dimension tamanho = conteudo.getPreferredSize();
            int largura = conteudo.getWidth();
            conteudo.setSize(largura, tamanho.height);
            conteudo.doLayout();
            validarTudo(conteudo);
            BufferedImage imagem = new BufferedImage(largura, tamanho.height, BufferedImage.TYPE_INT_RGB);
            conteudo.printAll(imagem.getGraphics());
            gravar(imagem, arquivo);
            janela.validate();
        });
    }

    private static void validarTudo(Container c) {
        c.validate();
    }

    static void salvarJanela(Window janela, String arquivo) {
        naEdt(() -> {
            BufferedImage imagem = new BufferedImage(janela.getWidth(), janela.getHeight(), BufferedImage.TYPE_INT_RGB);
            janela.printAll(imagem.getGraphics());
            gravar(imagem, arquivo);
        });
    }

    private static void gravar(BufferedImage imagem, String arquivo) {
        try {
            pastaImagens.mkdirs();
            ImageIO.write(imagem, "png", new File(pastaImagens, arquivo));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
