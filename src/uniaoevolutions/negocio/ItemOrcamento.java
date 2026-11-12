package uniaoevolutions.negocio;

/**
 * Item de orçamento (composição 1..* de Orcamento; PK composta no banco).
 * ACRÉSCIMO: quantidade (coluna "Qtd." do mockup da OS); no Solicitar Orçamento é sempre 1.
 */
public class ItemOrcamento {

    private int idItemOrcamento;
    private double valorProposto;
    private int quantidade;
    private ServicoBase servicoBase;

    public ItemOrcamento() {
        this.quantidade = 1;
    }

    /** <<create>> (passo 9). */
    public ItemOrcamento(ServicoBase servico, double valorProposto) {
        this.servicoBase = servico;
        this.valorProposto = valorProposto;
        this.quantidade = 1;
    }

    public ItemOrcamento(int idItemOrcamento, double valorProposto, int quantidade, ServicoBase servicoBase) {
        this.idItemOrcamento = idItemOrcamento;
        this.valorProposto = valorProposto;
        this.quantidade = quantidade;
        this.servicoBase = servicoBase;
    }

    public double getValorProposto() {
        return valorProposto;
    }

    /** FA-11B (alterarValorItem). */
    public void setValorProposto(double valor) {
        this.valorProposto = valor;
    }

    /** "Subtotal": quantidade x valor. */
    public double getSubtotal() {
        return quantidade * valorProposto;
    }

    /** "Serviço / Peça": descrição do ServicoBase. */
    public String getDescricao() {
        return servicoBase == null ? "" : servicoBase.getDescricao();
    }

    public int getIdItemOrcamento() {
        return idItemOrcamento;
    }

    public void setIdItemOrcamento(int idItemOrcamento) {
        this.idItemOrcamento = idItemOrcamento;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public ServicoBase getServicoBase() {
        return servicoBase;
    }

    public void setServicoBase(ServicoBase servicoBase) {
        this.servicoBase = servicoBase;
    }
}
