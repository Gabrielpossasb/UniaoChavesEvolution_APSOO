package uniaoevolutions.negocio;

/**
 * Item de um orçamento: um serviço ou peça com o valor proposto ao cliente.
 *
 * Ligado a Orcamento por COMPOSIÇÃO ("compoe", 1 para 1..*): nasce e morre com
 * o seu orçamento, o que no banco virou a PK composta
 * (idItemOrcamento, idOrcamento) de TB_ItemOrcamento.
 *
 * ACRÉSCIMO à modelagem: o atributo quantidade, exigido pela coluna "Qtd." do
 * mockup e por incluirItemOrdemServico(). Ver DOCUMENTACAO.md.
 */
public class ItemOrcamento {

    private int idItemOrcamento;
    private double valorProposto;
    private int quantidade;
    private ServicoBase servicoBase;

    public ItemOrcamento() {
        this.quantidade = 1;
    }

    public ItemOrcamento(int idItemOrcamento, double valorProposto, int quantidade, ServicoBase servicoBase) {
        this.idItemOrcamento = idItemOrcamento;
        this.valorProposto = valorProposto;
        this.quantidade = quantidade;
        this.servicoBase = servicoBase;
    }

    /** Coluna "Subtotal" do mockup: quantidade x valor unitário. */
    public double getSubtotal() {
        return quantidade * valorProposto;
    }

    /** Coluna "Serviço / Peça" do mockup: vem do ServicoBase. */
    public String getDescricao() {
        return servicoBase == null ? "" : servicoBase.getDescricao();
    }

    public int getIdItemOrcamento() {
        return idItemOrcamento;
    }

    public void setIdItemOrcamento(int idItemOrcamento) {
        this.idItemOrcamento = idItemOrcamento;
    }

    public double getValorProposto() {
        return valorProposto;
    }

    public void setValorProposto(double valorProposto) {
        this.valorProposto = valorProposto;
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
