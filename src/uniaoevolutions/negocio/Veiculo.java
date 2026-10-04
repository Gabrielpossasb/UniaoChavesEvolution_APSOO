package uniaoevolutions.negocio;

/**
 * Veículo atendido pela União Evolutions. Os 13 atributos são os do Diagrama
 * de Classes, na mesma ordem; idVeiculo é a PK de TB_Veiculo.
 *
 * De tipoModulo a senhaRadio estão os dados que a tela exibe nos blocos "Dados
 * Técnicos" do mockup: eles pertencem ao veículo, não à Ordem de Serviço.
 */
public class Veiculo {

    private int idVeiculo;
    private String marca;
    private String modelo;
    private int anoModelo;
    private String motorizacao;
    private String placa;
    private String numeroChassi;
    private String tipoModulo;
    private String modeloECU;
    private String identificacaoModulo;
    private String codigoMecanico;
    private String senhaImobilizador;
    private String senhaAlarme;
    private String senhaRadio;

    public Veiculo() {
    }

    public int getIdVeiculo() {
        return idVeiculo;
    }

    public void setIdVeiculo(int idVeiculo) {
        this.idVeiculo = idVeiculo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(int anoModelo) {
        this.anoModelo = anoModelo;
    }

    public String getMotorizacao() {
        return motorizacao;
    }

    public void setMotorizacao(String motorizacao) {
        this.motorizacao = motorizacao;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getNumeroChassi() {
        return numeroChassi;
    }

    public void setNumeroChassi(String numeroChassi) {
        this.numeroChassi = numeroChassi;
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

    public String getCodigoMecanico() {
        return codigoMecanico;
    }

    public void setCodigoMecanico(String codigoMecanico) {
        this.codigoMecanico = codigoMecanico;
    }

    public String getSenhaImobilizador() {
        return senhaImobilizador;
    }

    public void setSenhaImobilizador(String senhaImobilizador) {
        this.senhaImobilizador = senhaImobilizador;
    }

    public String getSenhaAlarme() {
        return senhaAlarme;
    }

    public void setSenhaAlarme(String senhaAlarme) {
        this.senhaAlarme = senhaAlarme;
    }

    public String getSenhaRadio() {
        return senhaRadio;
    }

    public void setSenhaRadio(String senhaRadio) {
        this.senhaRadio = senhaRadio;
    }

    /** Campo "Marca / Modelo" do mockup: "Marca Modelo Motorizacao". */
    public String getMarcaModelo() {
        return marca + " " + modelo + (motorizacao == null ? "" : " " + motorizacao);
    }

    @Override
    public String toString() {
        return placa;
    }
}
