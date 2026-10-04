package uniaoevolutions.negocio;

/**
 * Tipos de usuário do sistema.
 *
 * Diagrama de Classes, classe Usuario:
 *     tipoUsuario : {administrador | tecnicoDeChaves | tecnicoDeModulos}
 * Mapeamento Objeto-Relacional:
 *     TB_Usuario(idUsuario<<PK>>, nome, tipoUsuario)
 *     tipoUsuario {administrador | tecnicoDeChaves | tecnicoDeModulos}
 *
 * As constantes mantêm a grafia exata dos artefatos (minúscula inicial), para
 * que o valor gravado no ENUM do MySQL seja literalmente name().
 */
public enum TipoUsuario {

    administrador,
    tecnicoDeChaves,
    tecnicoDeModulos;

    /**
     * RN-04 - habilitação do técnico: diz se este tipo de usuário pode ser
     * designado como responsável por uma OS do segmento informado.
     * O Administrador herda dos dois técnicos (ator principal da Descrição
     * Expandida), por isso é habilitado nos dois segmentos.
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
