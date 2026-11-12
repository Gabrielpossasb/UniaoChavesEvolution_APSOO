package uniaoevolutions.negocio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Orçamento (Diagrama de Classes v2). Superclasse de OrcamentoModulos e OrcamentoChaves;
 * não é abstrata (nome sem itálico no diagrama; a 1ª iteração usa Orcamento simples).
 * Associações: compõe 1..* ItemOrcamento; 1 Cliente; 1 Veiculo; 1 Usuario; 0..1 OrdemDeServico.
 */
public class Orcamento {

    // Situações da 1ª iteração (Registrar OS).
    /** RN-01 do Registrar OS: exigida para gerar uma OS. FA-14 do Solicitar Orçamento. */
    public static final String STATUS_APROVADO = "Aprovado";
    /** POS-02 do Registrar OS. */
    public static final String STATUS_CONVERTIDO = "Convertido em OS";

    // Situações do Solicitar Orçamento (RN-06).
    /** POS-01; mensagem 1.15.2. RN-05 do Registrar OS: volta a ela após alteração de item. */
    public static final String STATUS_EM_ANALISE = "Em análise";
    /** FA-14. */
    public static final String STATUS_RECUSADO = "Recusado";
    /** FA-12D. */
    public static final String STATUS_RASCUNHO = "Rascunho";

    private int idOrcamento;
    private String numero;
    private Date dataSolicitacao;
    private Date dataValidade;
    private String origemAtendimento;
    private String status;
    private double valorEstimado;
    private String observacoes;
    private String motivoCancelamento;

    /** Atributo da 1ª iteração. No v2 o segmento vem da subclasse; o campo serve às listagens. */
    private String segmentoPretendido;

    private Cliente cliente;
    private Veiculo veiculo;
    private Usuario tecnico;
    private final List<ItemOrcamento> itens = new ArrayList<>();

    public Orcamento() {
    }

    /** Parte comum dos <<create>> das subclasses (mensagens 1.3 e 1.4). */
    protected Orcamento(String numero, Date dataValidade, String origemAtendimento, String observacoes) {
        this.numero = numero;
        this.dataValidade = dataValidade;
        this.origemAtendimento = origemAtendimento;
        this.observacoes = observacoes;
    }

    // =================================================================
    // Operações do Diagrama de Classes v2
    // =================================================================

    /** Mensagem 1.12: associarCliente(cli). */
    public void associarCliente(Cliente cli) {
        this.cliente = cli;
    }

    /** Mensagem 1.13: associarVeiculo(vei). */
    public void associarVeiculo(Veiculo vei) {
        this.veiculo = vei;
    }

    /** Mensagem 1.14: associarTecnico(tec). */
    public void associarTecnico(Usuario tec) {
        this.tecnico = tec;
    }

    /** Mensagem 1.11: [para cada item em itens] adicionarItem(item). */
    public void adicionarItem(ItemOrcamento item) {
        itens.add(item);
    }

    /** RN-05: remoção de item. */
    public void removerItem(ItemOrcamento item) {
        itens.remove(item);
    }

    /** Mensagem 1.15: dispara 1.15.1, 1.15.2 e 1.15.3, nessa ordem. */
    public void sinalizarRegistro() {
        setDataSolicitacao(new Date());          // 1.15.1
        setStatus(STATUS_EM_ANALISE);            // 1.15.2
        calcularValorEstimado();                 // 1.15.3
    }

    /** Privado (diagrama v2). Mensagem 1.15.1. */
    private void setDataSolicitacao(Date data) {
        this.dataSolicitacao = data;
    }

    /** Privado (diagrama v2). Mensagem 1.15.2. */
    private void setStatus(String status) {
        this.status = status;
    }

    /**
     * FA-14 (mensagem 1.2): decisão do cliente, ator secundário. Só "Em análise" recebe a
     * decisão; aprovação exige validade não vencida (RN-07). Dispara 1.2.1 setStatus(decisao).
     */
    public void registrarDecisao(String decisao) {
        if (!STATUS_APROVADO.equals(decisao) && !STATUS_RECUSADO.equals(decisao)) {
            throw new IllegalArgumentException("Informe a decisão do cliente: aprovado ou recusado.");
        }
        if (!STATUS_EM_ANALISE.equals(status)) {
            throw new IllegalArgumentException("Somente orçamento com situação \"" + STATUS_EM_ANALISE
                    + "\" recebe a decisão do cliente.");
        }
        if (STATUS_APROVADO.equals(decisao) && validadeVencida()) {
            throw new IllegalArgumentException("Orçamento expirado em "
                    + new SimpleDateFormat("dd/MM/yyyy").format(dataValidade)
                    + ". Não pode mais ser aprovado.");                         // RN-07
        }
        setStatus(decisao);                                                      // 1.2.1
    }

    /** RN-07: validade anterior a hoje. */
    private boolean validadeVencida() {
        if (dataValidade == null) {
            return false;
        }
        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);
        return dataValidade.before(hoje.getTime());
    }

    /** Mensagem 1.15.3 e RN-05: /valorEstimado = soma dos valores propostos. */
    public double calcularValorEstimado() {
        double total = 0;
        for (ItemOrcamento item : itens) {
            total += item.getSubtotal();
        }
        this.valorEstimado = total;
        return total;
    }

    /** Mensagem 1.2 do Registrar OS. */
    public Veiculo getVeiculo() {
        return veiculo;
    }

    /** Mensagem 1.4 do Registrar OS. */
    public String getSegmentoPretendido() {
        return segmentoPretendido;
    }

    // =================================================================
    // ACRÉSCIMOS
    // =================================================================

    /** Passo 1: número provisório e data antes do registro. */
    public void marcarComoProvisorio(String numeroProvisorio, Date dataSolicitacao) {
        this.numero = numeroProvisorio;
        setDataSolicitacao(dataSolicitacao);
    }

    /** FA-12D e RN-06: rascunho sem número e sem validade. */
    public void marcarComoRascunho(Date dataSolicitacao) {
        this.numero = null;
        this.dataValidade = null;
        setDataSolicitacao(dataSolicitacao);
        setStatus(STATUS_RASCUNHO);
        calcularValorEstimado();
    }

    /** Para a DAO restaurar data e situação gravadas (os setters são privados). */
    public void restaurarEstadoPersistido(Date dataSolicitacao, String status) {
        setDataSolicitacao(dataSolicitacao);
        setStatus(status);
    }

    // =================================================================
    // Usados pela 1ª iteração (Registrar OS)
    // =================================================================

    /** RN-01 do Registrar OS. */
    public boolean podeGerarOrdemDeServico() {
        return STATUS_APROVADO.equals(status);
    }

    /** "Valor total da OS" (POS-03). */
    public double getValorTotal() {
        double total = 0;
        for (ItemOrcamento item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    /** ORC-AAAA-NNNN; sem número (rascunho), usa o formato derivado do id da 1ª iteração. */
    public String getNumeroOrcamento() {
        if (numero != null) {
            return numero;
        }
        String ano = dataSolicitacao == null
                ? "0000"
                : new SimpleDateFormat("yyyy").format(dataSolicitacao);
        return String.format("ORC-%s-%04d", ano, idOrcamento);
    }

    public void setSegmentoPretendido(String segmentoPretendido) {
        this.segmentoPretendido = segmentoPretendido;
    }

    // =================================================================
    // Getters e setters simples
    // =================================================================

    public List<ItemOrcamento> getItens() {
        return itens;
    }

    public int getIdOrcamento() {
        return idOrcamento;
    }

    public void setIdOrcamento(int idOrcamento) {
        this.idOrcamento = idOrcamento;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Date getDataSolicitacao() {
        return dataSolicitacao;
    }

    public Date getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getOrigemAtendimento() {
        return origemAtendimento;
    }

    public void setOrigemAtendimento(String origemAtendimento) {
        this.origemAtendimento = origemAtendimento;
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Usuario getTecnico() {
        return tecnico;
    }

    /** Rótulo da combo "Orçamento associado" do Registrar OS. */
    @Override
    public String toString() {
        return getNumeroOrcamento();
    }
}
