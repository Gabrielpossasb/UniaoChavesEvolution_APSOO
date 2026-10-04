package uniaoevolutions.negocio;

/**
 * Usuário do sistema: quem executa a Ordem de Serviço (associação "executada
 * por", 0..* OS para 1 Usuario).
 *
 * RASTREABILIDADE: a versão anterior do Diagrama de Classes tinha a hierarquia
 * Tecnico / TecnicoDeChaves / TecnicoDeModulos / Administrador. A versão
 * corrigida (01/10/2026), implementada aqui, usa uma única classe com o
 * atributo enumerado tipoUsuario, e o Mapeamento OR ficou só com TB_Usuario.
 */
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

    /**
     * RN-04: só técnicos habilitados no segmento podem ser responsáveis. É
     * regra de negócio, por isso mora aqui e não na tela nem na DAO.
     */
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

    /** Rótulo "Nome - Tipo" da combo "Técnico responsável" do mockup. */
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
