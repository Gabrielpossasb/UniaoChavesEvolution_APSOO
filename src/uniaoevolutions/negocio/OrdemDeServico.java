package uniaoevolutions.negocio;

import java.util.Date;
import java.util.List;

/**
 * Ordem de Serviço. Abstrata (itálico no diagrama): toda OS é de Módulos ou de Chaves.
 * Associações: 1 Orcamento, 1 Veiculo, 1 Usuario.
 */
public abstract class OrdemDeServico {

    /** Mensagem 1.7.2. O diagrama diz "Aberta"; POS-01 e mockup dizem "Em execução". */
    public static final String STATUS_ABERTA = "Aberta";
    /** FA-11C. */
    public static final String STATUS_RASCUNHO = "Rascunho";

    private int idOrdemDeServico;
    private String numeroOS;
    private Date dataAbertura;
    private String statusOS;
    private Date dataEmissaoGarantia;
    private String textoExecutadoGarantia;
    private Date dataEmissaoResponsabilidade;
    private int quilometragem;
    private Date prazoEstimado;
    private String observacoes;

    private Orcamento orcamento;
    private Veiculo veiculo;
    private Usuario tecnico;

    protected OrdemDeServico() {
    }

    protected OrdemDeServico(int quilometragem, Date prazoEstimado, String observacoes) {
        this.quilometragem = quilometragem;
        this.prazoEstimado = prazoEstimado;
        this.observacoes = observacoes;
    }

    // -----------------------------------------------------------------
    // Operações do Diagrama de Classes
    // -----------------------------------------------------------------

    /** Mensagem 1.7: 1.7.1 e 1.7.2 (privadas, para data e status irem juntos). */
    public void sinalizarAbertura() {
        setDataAbertura(new Date());
        setStatusOS(STATUS_ABERTA);
    }

    private void setDataAbertura(Date data) {
        this.dataAbertura = data;
    }

    private void setStatusOS(String status) {
        this.statusOS = status;
    }

    /** Mensagem 1.8: vínculo com o orçamento (RN-02). */
    public void associarOrcamento(Orcamento orc) {
        this.orcamento = orc;
    }

    /** Mensagem 1.9: vínculo com o veículo (RN-02). */
    public void associarVeiculo(Veiculo vei) {
        this.veiculo = vei;
    }

    /** Mensagem 1.10: aloca o técnico responsável (POS-04). */
    public void associarTecnico(Usuario tec) {
        this.tecnico = tec;
    }

    // -----------------------------------------------------------------
    // Regras de negócio
    // -----------------------------------------------------------------

    /** "Módulos" ou "Chaves", definido pela subclasse. */
    public abstract String getSegmento();

    /** RN-03 (campos comuns; subclasses acrescentam) e RN-04. Erro: IllegalArgumentException (FA-11A). */
    public void validar() {
        if (quilometragem <= 0) {
            throw new IllegalArgumentException("Informe a quilometragem atual do veículo.");
        }
        if (prazoEstimado == null) {
            throw new IllegalArgumentException("Informe o prazo estimado de conclusão.");
        }
        if (orcamento == null) {
            throw new IllegalArgumentException("Selecione o orçamento que originará a Ordem de Serviço.");
        }
        if (veiculo == null) {
            throw new IllegalArgumentException("A Ordem de Serviço precisa estar vinculada a um veículo.");
        }
        if (tecnico == null) {
            throw new IllegalArgumentException("Selecione o técnico responsável pela Ordem de Serviço.");
        }
        // RN-04
        if (!tecnico.habilitadoEm(getSegmento())) {
            throw new IllegalArgumentException(
                    "O técnico selecionado não é habilitado no segmento " + getSegmento() + ".");
        }
    }

    /** POS-03: total do orçamento. */
    public double getValorTotal() {
        return orcamento == null ? 0 : orcamento.getValorTotal();
    }

    /** POS-03: itens do orçamento. */
    public List<ItemOrcamento> getItens() {
        return orcamento == null ? List.of() : orcamento.getItens();
    }

    // -----------------------------------------------------------------
    // Acesso aos atributos
    // -----------------------------------------------------------------

    public int getIdOrdemDeServico() {
        return idOrdemDeServico;
    }

    public void setIdOrdemDeServico(int idOrdemDeServico) {
        this.idOrdemDeServico = idOrdemDeServico;
    }

    public String getNumeroOS() {
        return numeroOS;
    }

    public void setNumeroOS(String numeroOS) {
        this.numeroOS = numeroOS;
    }

    public Date getDataAbertura() {
        return dataAbertura;
    }

    public String getStatusOS() {
        return statusOS;
    }

    /** FA-11C: situação "Rascunho", sem sinalizarAbertura(). */
    public void marcarComoRascunho(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
        this.statusOS = STATUS_RASCUNHO;
    }

    /** Para a DAO restaurar data e situação gravadas (os setters são privados). */
    public void restaurarEstadoPersistido(Date dataAbertura, String statusOS) {
        this.dataAbertura = dataAbertura;
        this.statusOS = statusOS;
    }

    public Date getDataEmissaoGarantia() {
        return dataEmissaoGarantia;
    }

    public void setDataEmissaoGarantia(Date dataEmissaoGarantia) {
        this.dataEmissaoGarantia = dataEmissaoGarantia;
    }

    public String getTextoExecutadoGarantia() {
        return textoExecutadoGarantia;
    }

    public void setTextoExecutadoGarantia(String textoExecutadoGarantia) {
        this.textoExecutadoGarantia = textoExecutadoGarantia;
    }

    public Date getDataEmissaoResponsabilidade() {
        return dataEmissaoResponsabilidade;
    }

    public void setDataEmissaoResponsabilidade(Date dataEmissaoResponsabilidade) {
        this.dataEmissaoResponsabilidade = dataEmissaoResponsabilidade;
    }

    public int getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(int quilometragem) {
        this.quilometragem = quilometragem;
    }

    public Date getPrazoEstimado() {
        return prazoEstimado;
    }

    public void setPrazoEstimado(Date prazoEstimado) {
        this.prazoEstimado = prazoEstimado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public Usuario getTecnico() {
        return tecnico;
    }
}
