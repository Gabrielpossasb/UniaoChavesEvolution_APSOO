package uniaoevolutions.negocio;

/**
 * Cliente da União Evolutions.
 *
 * idCliente não consta do Diagrama de Classes, mas é a PK de TB_Cliente no
 * Mapeamento OR - é o identificador do objeto no banco.
 */
public class Cliente {

    private int idCliente;
    private String nome;
    private String telefone;

    public Cliente() {
    }

    public Cliente(int idCliente, String nome, String telefone) {
        this.idCliente = idCliente;
        this.nome = nome;
        this.telefone = telefone;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public String toString() {
        return nome;
    }
}
