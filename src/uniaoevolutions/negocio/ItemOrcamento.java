package uniaoevolutions.negocio;

/**
 * Item de um orçamento: um serviço ou peça com o valor proposto ao cliente.
 *
 * Diagrama de Classes: ItemOrcamento { valorProposto : double }, ligado a
 * Orcamento por uma COMPOSIÇÃO (losango cheio, papel "compoe", multiplicidade
 * 1 para 1..*). Composição quer dizer que o item não existe sozinho: ele
 * nasce e morre com o seu orçamento. No banco isso virou a chave primária
 * composta (idItemOrcamento, idOrcamento) de TB_ItemOrcamento.
 *
 * O atributo quantidade é um ACRÉSCIMO à modelagem (ver DOCUMENTACAO.md,
 * seção de divergências): ele é exigido pela coluna "Qtd." do mockup e pela
 * operação incluirItemOrdemServico(servicoOuPeca, quantidade, valor) da
 * ControladoraRegistrarOS, mas não consta nem da classe nem do Mapeamento OR.
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

    /** Subtotal do item: quantidade x valor unitário (coluna "Subtotal" do mockup). */
    public double getSubtotal() {
        return quantidade * valorProposto;
    }

    /** Descrição exibida na coluna "Serviço / Peça" - vem do ServicoBase. */
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
