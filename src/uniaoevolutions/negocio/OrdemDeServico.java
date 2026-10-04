package uniaoevolutions.negocio;

import java.util.Date;
import java.util.List;

/**
 * Ordem de Serviço da União Evolutions.
 *
 * É ABSTRATA (nome em itálico no Diagrama de Classes): não existe "uma Ordem
 * de Serviço" genérica no negócio da empresa - toda OS é de Módulos ou de
 * Chaves. Quem decide qual das duas é o segmento do orçamento de origem, como
 * manda o Contrato de Operação:
 *
 *   "Um objeto os de OrdemDeServico foi criado, sendo da subclasse
 *    OrdemDeServicoModulos ou OrdemDeServicoChaves conforme seg."
 *
 * Diagrama de Classes:
 *   atributos : numeroOS : String, dataAbertura : Date, statusOS : String,
 *               dataEmissaoGarantia : Date, textoExecutadoGarantia : String,
 *               dataEmissaoResponsabilidade : Date, quilometragem : int,
 *               prazoEstimado : Date, observacoes : String
 *   operações : + sinalizarAbertura() : void
 *               - setDataAbertura(data : Date) : void
 *               - setStatusOS(status : String) : void
 *               + associarOrcamento(ocr : Orcamento) : void
 *               + associarVeiculo(vei : Veiculo) : void
 *               + associarTecnico(tec : Usuario) : void
 *   associações: 1 originada de 0..1 Orcamento
 *                0..* refere-se a 1 Veiculo
 *                0..* executada por 1 Usuario
 */
public abstract class OrdemDeServico {

    /**
     * Situação gravada por sinalizarAbertura().
     *
     * DIVERGÊNCIA ENTRE ARTEFATOS: o Diagrama de Comunicação manda
     * setStatusOs("Aberta") (mensagem 1.7.2), enquanto a POS-01 da Descrição
     * Expandida e o mockup falam em "Em execução". Seguimos o Diagrama de
     * Comunicação, que é o artefato de projeto que esta implementação
     * concretiza. A divergência está registrada na DOCUMENTACAO.md.
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
     * Mensagem 1.7 do Diagrama de Comunicação. Marca a abertura da Ordem de
     * Serviço, executando internamente as mensagens aninhadas:
     *   1.7.1 setDataAbertura()
     *   1.7.2 setStatusOs("Aberta")
     *
     * As duas são privadas de propósito: quem está de fora pede "sinalize a
     * abertura" e a própria OS decide o que isso significa. Assim não há como
     * alguém gravar uma data de abertura sem o status correspondente.
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

    /** Mensagem 1.8 do Diagrama de Comunicação. Forma o vínculo com o orçamento (RN-02). */
    public void associarOrcamento(Orcamento ocr) {
        this.orcamento = ocr;
    }

    /** Mensagem 1.9 do Diagrama de Comunicação. Forma o vínculo com o veículo (RN-02). */
    public void associarVeiculo(Veiculo vei) {
        this.veiculo = vei;
    }

    /** Mensagem 1.10 do Diagrama de Comunicação. Aloca o técnico responsável (POS-04). */
    public void associarTecnico(Usuario tec) {
        this.tecnico = tec;
    }

    // -----------------------------------------------------------------
    // Regras de negócio
    // -----------------------------------------------------------------

    /**
     * Devolve "Módulos" ou "Chaves". É abstrato: cada subclasse sabe o seu
     * próprio segmento. É este método que permite à camada de persistência e
     * à tela tratarem as duas subclasses sem perguntar o tipo a cada passo.
     */
    public abstract String getSegmento();

    /**
     * RN-03 - campos obrigatórios. Esta parte vale para qualquer Ordem de
     * Serviço; cada subclasse acrescenta os seus campos chamando super.validar().
     * Lança IllegalArgumentException com a mensagem a ser exibida pelo FA-11A.
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
        // RN-04: o técnico tem de ser habilitado no segmento da OS.
        if (!tecnico.habilitadoEm(getSegmento())) {
            throw new IllegalArgumentException(
                    "O técnico selecionado não é habilitado no segmento " + getSegmento() + ".");
        }
    }

    /** POS-03: os itens são herdados do orçamento, então o total da OS é o dele. */
    public double getValorTotal() {
        return orcamento == null ? 0 : orcamento.getValorTotal();
    }

    /** POS-03: lista de itens herdada do orçamento, exibida na tabela do mockup. */
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
     * Usado apenas pelo FA-11C (rascunho), que grava a OS com situação
     * "Rascunho" sem passar por sinalizarAbertura().
     */
    public void marcarComoRascunho(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
        this.statusOS = STATUS_RASCUNHO;
    }

    /**
     * Repõe no objeto a data e a situação que estavam gravadas no banco.
     *
     * Existe por causa do encapsulamento: setDataAbertura e setStatusOS são
     * privados (só sinalizarAbertura pode mudá-los), mas a camada de
     * persistência precisa reconstruir um objeto já existente exatamente como
     * ele foi gravado. Este método é o único caminho para isso, e só a DAO o
     * usa - a tela e a controladora não têm motivo para chamá-lo.
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
