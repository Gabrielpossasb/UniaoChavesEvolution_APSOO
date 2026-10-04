package uniaoevolutions.negocio;

/**
 * Usuário do sistema. É quem executa a Ordem de Serviço (associação
 * "executada por" do Diagrama de Classes, com multiplicidade 1 do lado do
 * Usuario e 0..* do lado da OrdemDeServico).
 *
 * Diagrama de Classes: Usuario { idUsuario : int, nome : String,
 *                                tipoUsuario : {administrador |
 *                                tecnicoDeChaves | tecnicoDeModulos} }
 *
 * Observação de rastreabilidade: a versão anterior do Diagrama de Classes
 * tinha as classes Tecnico, TecnicoDeChaves, TecnicoDeModulos e Administrador
 * em hierarquia. A versão corrigida (01/10/2026), que é a implementada aqui,
 * trocou essa hierarquia por uma única classe Usuario com o atributo
 * enumerado tipoUsuario - e o Mapeamento OR corrigido acompanhou, deixando
 * apenas TB_Usuario.
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
     * RN-04: somente técnicos habilitados no segmento da Ordem de Serviço
     * podem ser designados como responsáveis. A regra é do negócio, por isso
     * mora aqui e não na tela nem na DAO.
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

    /** Rótulo da combo "Técnico responsável" do mockup: "Nome - Tipo". */
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
