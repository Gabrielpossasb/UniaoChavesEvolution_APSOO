package uniaoevolutions.negocio;

/**
 * Os dois segmentos de serviço da União Evolutions.
 *
 * Os textos são os do Contrato de Operação ("se o segmento do orçamento for
 * 'Módulos' ... se o segmento for 'Chaves'") e do Diagrama de Comunicação
 * (guardas [seg = "Módulos"] e [seg = "Chaves"]).
 *
 * Esta classe não está no Diagrama de Classes: é só o lugar único onde esses
 * dois literais são escritos, para que a controladora, as DAOs e a tela não
 * repitam a String solta e não corram o risco de divergir na grafia.
 */
public final class Segmento {

    public static final String MODULOS = "Módulos";
    public static final String CHAVES = "Chaves";

    private Segmento() {
    }
}
