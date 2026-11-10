package uniaoevolutions.negocio;

/**
 * Cliente da União Evolutions.
 *
 * Diagrama de Classes v2 (2ª iteração): nome, cpfCnpj, telefone e o
 * construtor <<create>> Cliente(nome, telefone), usado no FA-02.
 *
 * idCliente não consta do Diagrama de Classes, mas é a PK de TB_Cliente no
 * Mapeamento OR - é o identificador do objeto no banco.
 */
public class Cliente {

    private int idCliente;
    private String nome;
    private String cpfCnpj;
    private String telefone;

    public Cliente() {
    }

    /** <<create>> do Diagrama de Classes v2 - cadastro rápido do FA-02.3. */
    public Cliente(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    /** Usado pela 1ª iteração (OrcamentoDAO) para montar o cliente lido do banco. */
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

    /** Só dígitos: 11 para CPF, 14 para CNPJ. Pode ser null (FA-02). */
    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
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
