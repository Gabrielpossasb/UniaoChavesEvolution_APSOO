package uniaoevolutions.negocio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Orçamento solicitado pelo cliente. É a origem obrigatória de toda Ordem de
 * Serviço (RN-01).
 *
 * Diagrama de Classes:
 *   atributos : dataSolicitacao : Date, origemAtendimento : String,
 *               segmentoPretendido : String, valorEstimado : double,
 *               status : String
 *   operações : + getVeiculo() : Veiculo
 *               + getSegmentoPretendido() : String
 *   associações: 1 compoe 1..* ItemOrcamento (composição)
 *                0..1 solicita 1 Cliente
 *                0..* refere-se a 1 Veiculo
 *                0..1 originada de 1 OrdemDeServico
 */
public class Orcamento {

    /** Situação exigida pela RN-01 / PRE-02 para o orçamento poder gerar uma OS. */
    public static final String STATUS_APROVADO = "Aprovado pelo cliente";
    /** Situação que o orçamento assume depois da conversão (POS-02). */
    public static final String STATUS_CONVERTIDO = "Convertido em OS";
    /** Situação para a qual a RN-05 devolve o orçamento quando há alteração de item. */
    public static final String STATUS_AGUARDANDO = "Aguardando aprovação do cliente";

    private int idOrcamento;
    private Date dataSolicitacao;
    private String origemAtendimento;
    private String segmentoPretendido;
    private double valorEstimado;
    private String status;

    private Cliente cliente;
    private Veiculo veiculo;
    private final List<ItemOrcamento> itens = new ArrayList<>();

    public Orcamento() {
    }

    /** Operação do Diagrama de Classes: devolve o veículo do orçamento. */
    public Veiculo getVeiculo() {
        return veiculo;
    }

    /** Operação do Diagrama de Classes: devolve o segmento pretendido. */
    public String getSegmentoPretendido() {
        return segmentoPretendido;
    }

    /** RN-01: só um orçamento aprovado e ainda não convertido pode gerar OS. */
    public boolean podeGerarOrdemDeServico() {
        return STATUS_APROVADO.equals(status);
    }

    /**
     * Soma dos subtotais dos itens - é o "Valor total da OS" do mockup.
     * POS-03: os itens do orçamento são herdados pela Ordem de Serviço, então
     * o total da OS é o total do orçamento.
     */
    public double getValorTotal() {
        double total = 0;
        for (ItemOrcamento item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Número de exibição do orçamento, no formato ORC-AAAA-NNNN do mockup.
     * É DERIVADO do id e do ano da solicitação: o Mapeamento OR não tem coluna
     * para esse número, então ele é calculado e não armazenado.
     */
    public String getNumeroOrcamento() {
        String ano = dataSolicitacao == null
                ? "0000"
                : new SimpleDateFormat("yyyy").format(dataSolicitacao);
        return String.format("ORC-%s-%04d", ano, idOrcamento);
    }

    public void adicionarItem(ItemOrcamento item) {
        itens.add(item);
    }

    public List<ItemOrcamento> getItens() {
        return itens;
    }

    public int getIdOrcamento() {
        return idOrcamento;
    }

    public void setIdOrcamento(int idOrcamento) {
        this.idOrcamento = idOrcamento;
    }

    public Date getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(Date dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getOrigemAtendimento() {
        return origemAtendimento;
    }

    public void setOrigemAtendimento(String origemAtendimento) {
        this.origemAtendimento = origemAtendimento;
    }

    public void setSegmentoPretendido(String segmentoPretendido) {
        this.segmentoPretendido = segmentoPretendido;
    }

    public double getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(double valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    /** Rótulo da combo "Orçamento associado" do mockup. */
    @Override
    public String toString() {
        return getNumeroOrcamento();
    }
}
