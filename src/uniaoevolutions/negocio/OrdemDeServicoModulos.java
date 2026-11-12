package uniaoevolutions.negocio;

import java.util.Date;

/** OS do segmento Módulos (V1). Os campos de execução ficam vazios no registro (outro caso de uso). */
public class OrdemDeServicoModulos extends OrdemDeServico {

    private String relatoInicial;
    private String condicaoRecebimento;
    private String diagnosticoInicial;
    private String testesEntradaSaida;
    private String confirmacaoDefeito;
    private String causaIdentificada;
    private String reparoRealizado;

    public OrdemDeServicoModulos() {
    }

    /** Mensagem 1.5: <<create>> com a guarda [seg = "Módulos"]. */
    public OrdemDeServicoModulos(int quilometragem, Date prazoEstimado, String observacoes,
                                 String relatoInicial, String condicaoRecebimento,
                                 String diagnosticoInicial) {
        super(quilometragem, prazoEstimado, observacoes);
        this.relatoInicial = relatoInicial;
        this.condicaoRecebimento = condicaoRecebimento;
        this.diagnosticoInicial = diagnosticoInicial;
    }

    @Override
    public String getSegmento() {
        return Segmento.MODULOS;
    }

    /** RN-03 (Módulos). */
    @Override
    public void validar() {
        super.validar();
        if (vazio(relatoInicial)) {
            throw new IllegalArgumentException("Informe os sintomas relatados pelo cliente.");
        }
        if (vazio(condicaoRecebimento)) {
            throw new IllegalArgumentException("Informe a condição de recebimento do módulo.");
        }
        if (vazio(diagnosticoInicial)) {
            throw new IllegalArgumentException("Informe o diagnóstico em bancada.");
        }
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    public String getRelatoInicial() {
        return relatoInicial;
    }

    public void setRelatoInicial(String relatoInicial) {
        this.relatoInicial = relatoInicial;
    }

    public String getCondicaoRecebimento() {
        return condicaoRecebimento;
    }

    public void setCondicaoRecebimento(String condicaoRecebimento) {
        this.condicaoRecebimento = condicaoRecebimento;
    }

    public String getDiagnosticoInicial() {
        return diagnosticoInicial;
    }

    public void setDiagnosticoInicial(String diagnosticoInicial) {
        this.diagnosticoInicial = diagnosticoInicial;
    }

    public String getTestesEntradaSaida() {
        return testesEntradaSaida;
    }

    public void setTestesEntradaSaida(String testesEntradaSaida) {
        this.testesEntradaSaida = testesEntradaSaida;
    }

    public String getConfirmacaoDefeito() {
        return confirmacaoDefeito;
    }

    public void setConfirmacaoDefeito(String confirmacaoDefeito) {
        this.confirmacaoDefeito = confirmacaoDefeito;
    }

    public String getCausaIdentificada() {
        return causaIdentificada;
    }

    public void setCausaIdentificada(String causaIdentificada) {
        this.causaIdentificada = causaIdentificada;
    }

    public String getReparoRealizado() {
        return reparoRealizado;
    }

    public void setReparoRealizado(String reparoRealizado) {
        this.reparoRealizado = reparoRealizado;
    }
}
