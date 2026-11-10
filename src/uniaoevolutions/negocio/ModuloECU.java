package uniaoevolutions.negocio;

/**
 * Módulo / central eletrônica (ECU) de um veículo.
 *
 * Diagrama de Classes v2: tipoModulo, modeloECU, identificacaoModulo e o
 * construtor <<create>> ModuloECU(tipoModulo, modeloECU, identificacaoModulo)
 * - mensagem 1.8 do Diagrama de Comunicação do Solicitar Orçamento.
 *
 * Associações: "Veiculo 1 possui 0..* ModuloECU" e
 * "OrcamentoModulos 0..* refere-se a 0..1 ModuloECU".
 *
 * idModuloECU é a PK de TB_ModuloECU no Mapeamento OR v2; vale 0 enquanto o
 * módulo não foi gravado (é assim que a OrcamentoDAO sabe que precisa
 * inseri-lo).
 */
public class ModuloECU {

    private int idModuloECU;
    private String tipoModulo;
    private String modeloECU;
    private String identificacaoModulo;

    public ModuloECU() {
    }

    /** <<create>> do Diagrama de Classes v2 (mensagem 1.8). */
    public ModuloECU(String tipoModulo, String modeloECU, String identificacaoModulo) {
        this.tipoModulo = tipoModulo;
        this.modeloECU = modeloECU;
        this.identificacaoModulo = identificacaoModulo;
    }

    public int getIdModuloECU() {
        return idModuloECU;
    }

    public void setIdModuloECU(int idModuloECU) {
        this.idModuloECU = idModuloECU;
    }

    public String getTipoModulo() {
        return tipoModulo;
    }

    public void setTipoModulo(String tipoModulo) {
        this.tipoModulo = tipoModulo;
    }

    public String getModeloECU() {
        return modeloECU;
    }

    public void setModeloECU(String modeloECU) {
        this.modeloECU = modeloECU;
    }

    public String getIdentificacaoModulo() {
        return identificacaoModulo;
    }

    public void setIdentificacaoModulo(String identificacaoModulo) {
        this.identificacaoModulo = identificacaoModulo;
    }

    @Override
    public String toString() {
        return identificacaoModulo == null ? tipoModulo : identificacaoModulo;
    }
}
