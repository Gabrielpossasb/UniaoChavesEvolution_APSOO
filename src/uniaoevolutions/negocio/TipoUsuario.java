package uniaoevolutions.negocio;

/** Valores de Usuario.tipoUsuario, com a grafia do ENUM do banco. */
public enum TipoUsuario {

    administrador,
    tecnicoDeChaves,
    tecnicoDeModulos;

    /** RN-04. O Administrador é habilitado nos dois segmentos. */
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
