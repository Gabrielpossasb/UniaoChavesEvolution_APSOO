package uniaoevolutions.negocio;

/** Módulo (ECU) de um veículo (Diagrama de Classes v2). idModuloECU = 0 enquanto não gravado. */
public class ModuloECU {

    private int idModuloECU;
    private String tipoModulo;
    private String modeloECU;
    private String identificacaoModulo;

    public ModuloECU() {
    }

    /** <<create>> (mensagem 1.8). */
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
