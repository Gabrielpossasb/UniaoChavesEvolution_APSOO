package uniaoevolutions.negocio;

/**
 * Valores do atributo tipoUsuario da classe Usuario.
 *
 * A grafia é a dos artefatos (minúscula inicial), para que o valor gravado no
 * ENUM do MySQL seja literalmente name().
 */
public enum TipoUsuario {

    administrador,
    tecnicoDeChaves,
    tecnicoDeModulos;

    /**
     * RN-04: diz se este tipo pode ser responsável por uma OS do segmento.
     * O Administrador herda dos dois técnicos, logo é habilitado em ambos.
     */
    public boolean habilitadoEm(String segmento) {
        if (this == administrador) {
            return true;
        }
        if (Segmento.MODULOS.equals(segmento)) {
            return this == tecnicoDeModulos;
        }
        if (Segmento.CHAVES.equals(segmento)) {
            return this == tecnicoDeChaves;
        }
        return false;
    }
}
