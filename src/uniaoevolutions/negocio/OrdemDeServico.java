package uniaoevolutions.negocio;

import java.util.Date;
import java.util.List;

/**
 * Ordem de Serviço da União Evolutions.
 *
 * ABSTRATA (itálico no Diagrama de Classes): toda OS é de Módulos ou de
 * Chaves, conforme o segmento do orçamento de origem.
 *
 * Associações do Diagrama de Classes: 1 originada de 0..1 Orcamento,
 * 0..* refere-se a 1 Veiculo, 0..* executada por 1 Usuario.
 */
public abstract class OrdemDeServico {

    /**
     * Situação gravada por sinalizarAbertura().
     *
     * DIVERGÊNCIA: o Diagrama de Comunicação manda "Aberta" (mensagem 1.7.2),
     * a POS-01 e o mockup falam em "Em execução". Seguimos o diagrama - ver
     * DOCUMENTACAO.md.
     */
    public static final String STATUS_ABERTA = "Aberta";
    /** Situação do rascunho do FA-11C ("sem número definitivo"). */
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

    /**
     * Mensagem 1.7: marca a abertura da OS executando as mensagens aninhadas
     * 1.7.1 setDataAbertura() e 1.7.2 setStatusOs("Aberta").
     *
     * As duas são privadas para que não se grave uma data de abertura sem o
     * status correspondente.
     */
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
    public void associarOrcamento(Orcamento ocr) {
        this.orcamento = ocr;
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

    /**
     * "Módulos" ou "Chaves": cada subclasse sabe o seu próprio segmento, e por
     * isso a tela e as DAOs não precisam perguntar o tipo do objeto.
     */
    public abstract String getSegmento();

    /**
     * RN-03: campos obrigatórios comuns a qualquer OS. Cada subclasse
     * acrescenta os seus chamando super.validar(). A IllegalArgumentException
     * leva a mensagem que o FA-11A exibe.
     */
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
        // RN-04: o técnico tem de ser habilitado no segmento.
        if (!tecnico.habilitadoEm(getSegmento())) {
            throw new IllegalArgumentException(
                    "O técnico selecionado não é habilitado no segmento " + getSegmento() + ".");
        }
    }

    /** POS-03: os itens vêm do orçamento, então o total da OS é o dele. */
    public double getValorTotal() {
        return orcamento == null ? 0 : orcamento.getValorTotal();
    }

    /** POS-03: itens herdados do orçamento, exibidos na tabela do mockup. */
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

    /**
     * FA-11C: grava a OS com situação "Rascunho", sem passar por
     * sinalizarAbertura().
     */
    public void marcarComoRascunho(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
        this.statusOS = STATUS_RASCUNHO;
    }

    /**
     * Repõe a data e a situação que estavam gravadas no banco. Como
     * setDataAbertura e setStatusOS são privados, é o único caminho para a DAO
     * reconstruir uma OS existente.
     */
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
