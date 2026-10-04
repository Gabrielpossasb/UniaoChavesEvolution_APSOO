package uniaoevolutions.negocio;

/**
 * Os dois segmentos de serviço, com a grafia das guardas [seg = "Módulos"] e
 * [seg = "Chaves"] do Diagrama de Comunicação.
 *
 * Não está no Diagrama de Classes: é só o lugar único desses literais, para a
 * controladora, as DAOs e a tela não divergirem na grafia.
 */
public final class Segmento {

    public static final String MODULOS = "Módulos";
    public static final String CHAVES = "Chaves";

    private Segmento() {
    }
}
