package uniaoevolutions.negocio;

/** Cliente (Diagrama de Classes v2). idCliente é a PK de TB_Cliente (MOR). */
public class Cliente {

    private int idCliente;
    private String nome;
    private String cpfCnpj;
    private String telefone;

    public Cliente() {
    }

    /** <<create>> (FA-02.3). */
    public Cliente(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    /** Para a OrcamentoDAO montar o cliente lido. */
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

    /** Só dígitos (11 ou 14); null no FA-02. */
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
