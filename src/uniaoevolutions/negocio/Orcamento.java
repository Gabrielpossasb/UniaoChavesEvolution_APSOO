package uniaoevolutions.negocio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Orçamento solicitado pelo cliente: origem obrigatória de toda Ordem de
 * Serviço.
 *
 * Diagrama de Classes v2 (2ª iteração) - atributos:
 *   numero, dataSolicitacao, dataValidade, origemAtendimento, status,
 *   /valorEstimado (derivado), observacoes, motivoCancelamento
 * operações:
 *   associarCliente, associarVeiculo, associarTecnico, adicionarItem,
 *   removerItem, sinalizarRegistro, -setDataSolicitacao, -setStatus,
 *   calcularValorEstimado, getVeiculo, getSegmentoPretendido
 *
 * Superclasse de OrcamentoModulos e OrcamentoChaves. Não é abstrata porque
 * o nome no diagrama não está em itálico (ao contrário de OrdemDeServico) e
 * porque a 1ª iteração lê orçamentos como Orcamento simples.
 *
 * Associações: 1 compõe 1..* ItemOrcamento (composição), 0..* solicita 1
 * Cliente, 0..* refere-se a 1 Veiculo, 0..* elaborado por 1 Usuario,
 * 1 originada de 0..1 OrdemDeServico.
 */
public class Orcamento {

    // Situações usadas pela 1ª iteração (Registrar Ordem de Serviço).
    /** Situação exigida pela RN-01 / PRE-02 do Registrar OS para gerar uma OS. */
    public static final String STATUS_APROVADO = "Aprovado pelo cliente";
    /** Situação após a conversão (POS-02 do Registrar OS). */
    public static final String STATUS_CONVERTIDO = "Convertido em OS";
    /** Situação de volta pela RN-05 do Registrar OS, quando há alteração de item. */
    public static final String STATUS_AGUARDANDO = "Aguardando aprovação do cliente";

    // Situações do Solicitar Orçamento (RN-06 da 2ª iteração).
    /** RN-06 / POS-01: situação do orçamento registrado (mensagem 1.15.2). */
    public static final String STATUS_EM_ANALISE = "Em análise";
    /** RN-06 / FA-12D: situação do orçamento salvo como rascunho. */
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

    /**
     * Atributo do Diagrama de Classes da 1ª iteração. No v2 o segmento é dado
     * pela subclasse (OrcamentoModulos / OrcamentoChaves), que sobrescreve
     * getSegmentoPretendido(); o campo continua servindo à OrcamentoDAO da 1ª
     * iteração, que lê orçamentos como Orcamento simples.
     */
    private String segmentoPretendido;

    private Cliente cliente;
    private Veiculo veiculo;
    private Usuario tecnico;
    private final List<ItemOrcamento> itens = new ArrayList<>();

    public Orcamento() {
    }

    /**
     * Parte comum dos <<create>> das subclasses (mensagens 1.3 e 1.4):
     * numero, dataValidade, origemAtendimento e observacoes.
     */
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

    /** Mensagem 1.14: associarTecnico(tec) - "elaborado por". */
    public void associarTecnico(Usuario tec) {
        this.tecnico = tec;
    }

    /** Mensagem 1.11: [para cada item em itens] adicionarItem(item). */
    public void adicionarItem(ItemOrcamento item) {
        itens.add(item);
    }

    /** Operação do Diagrama de Classes v2 (RN-05: remoção de item). */
    public void removerItem(ItemOrcamento item) {
        itens.remove(item);
    }

    /**
     * Mensagem 1.15: sinalizarRegistro(), que dispara
     * 1.15.1 setDataSolicitacao(dataAtual), 1.15.2 setStatus("Em análise") e
     * 1.15.3 calcularValorEstimado(), nessa ordem.
     */
    public void sinalizarRegistro() {
        setDataSolicitacao(new Date());          // 1.15.1
        setStatus(STATUS_EM_ANALISE);            // 1.15.2
        calcularValorEstimado();                 // 1.15.3
    }

    /** Privado como no Diagrama de Classes v2 (mensagem 1.15.1). */
    private void setDataSolicitacao(Date data) {
        this.dataSolicitacao = data;
    }

    /** Privado como no Diagrama de Classes v2 (mensagem 1.15.2). */
    private void setStatus(String status) {
        this.status = status;
    }

    /**
     * Mensagem 1.15.3 e RN-05: o valor estimado é o somatório dos valores
     * propostos dos itens. É o atributo derivado "/valorEstimado".
     */
    public double calcularValorEstimado() {
        double total = 0;
        for (ItemOrcamento item : itens) {
            total += item.getSubtotal();
        }
        this.valorEstimado = total;
        return total;
    }

    /** Operação do Diagrama de Classes (mensagem 1.2 do Registrar OS). */
    public Veiculo getVeiculo() {
        return veiculo;
    }

    /** Operação do Diagrama de Classes (mensagem 1.4 do Registrar OS). */
    public String getSegmentoPretendido() {
        return segmentoPretendido;
    }

    // =================================================================
    // Acréscimos à modelagem (ver RELATORIO_ITERACAO2.md)
    // =================================================================

    /**
     * Passo 1 do Solicitar Orçamento: o formulário mostra o número provisório
     * e a data da solicitação antes do registro.
     */
    public void marcarComoProvisorio(String numeroProvisorio, Date dataSolicitacao) {
        this.numero = numeroProvisorio;
        setDataSolicitacao(dataSolicitacao);
    }

    /**
     * FA-12D e RN-06: rascunho "sem número definitivo e sem contagem do prazo
     * de validade".
     */
    public void marcarComoRascunho(Date dataSolicitacao) {
        this.numero = null;
        this.dataValidade = null;
        setDataSolicitacao(dataSolicitacao);
        setStatus(STATUS_RASCUNHO);
        calcularValorEstimado();
    }

    /**
     * Usado só pela camada de persistência para devolver ao objeto a data e a
     * situação gravadas, sem abrir os dois setters privados.
     */
    public void restaurarEstadoPersistido(Date dataSolicitacao, String status) {
        setDataSolicitacao(dataSolicitacao);
        setStatus(status);
    }

    // =================================================================
    // Usados pela 1ª iteração (Registrar Ordem de Serviço)
    // =================================================================

    /** RN-01 do Registrar OS: só orçamento aprovado e ainda não convertido gera OS. */
    public boolean podeGerarOrdemDeServico() {
        return STATUS_APROVADO.equals(status);
    }

    /** Soma dos subtotais dos itens: o "Valor total da OS" do mockup (POS-03). */
    public double getValorTotal() {
        double total = 0;
        for (ItemOrcamento item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Número ORC-AAAA-NNNN. Na 2ª iteração ele é a coluna numero; se o
     * orçamento ainda não tem número (rascunho), cai no formato derivado do
     * id que a 1ª iteração usava.
     */
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
