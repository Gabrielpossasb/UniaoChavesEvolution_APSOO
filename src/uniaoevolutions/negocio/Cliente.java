package uniaoevolutions.negocio;

/**
 * Cliente da União Evolutions.
 *
 * Diagrama de Classes: Cliente { nome : String, telefone : String }.
 * O atributo idCliente não aparece na classe, mas é a chave primária de
 * TB_Cliente no Mapeamento Objeto-Relacional: ele é o identificador do objeto
 * no banco e por isso faz parte da classe na camada de negócio.
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
