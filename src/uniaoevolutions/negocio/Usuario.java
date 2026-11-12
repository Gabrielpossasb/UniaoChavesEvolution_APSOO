package uniaoevolutions.negocio;

/** Usuário do sistema. O diagrama corrigido trocou a hierarquia de técnicos pelo atributo tipoUsuario. */
public class Usuario {

    private int idUsuario;
    private String nome;
    private TipoUsuario tipoUsuario;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nome, TipoUsuario tipoUsuario) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.tipoUsuario = tipoUsuario;
    }

    /** RN-04: habilitado no segmento. */
    public boolean habilitadoEm(String segmento) {
        return tipoUsuario != null && tipoUsuario.habilitadoEm(segmento);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    /** Rótulo "Nome - Tipo" da combo de técnicos. */
    @Override
    public String toString() {
        if (tipoUsuario == TipoUsuario.tecnicoDeModulos) {
            return nome + " - Técnico de Módulos";
        }
        if (tipoUsuario == TipoUsuario.tecnicoDeChaves) {
            return nome + " - Técnico de Chaves";
        }
        return nome + " - Administrador";
    }
}
