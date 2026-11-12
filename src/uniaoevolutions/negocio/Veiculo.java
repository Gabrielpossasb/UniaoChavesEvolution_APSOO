package uniaoevolutions.negocio;

import java.util.ArrayList;
import java.util.List;

/**
 * Veículo (Diagrama de Classes v2). Possui 0..* ModuloECU e 0..1 InformacoesSegurancaVeiculo.
 * tipoModulo a senhaRadio são da 1ª iteração e servem à tela do Registrar OS; a OrcamentoDAO
 * os preenche a partir das tabelas do v2.
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

    /** possui 0..* ModuloECU. */
    private final List<ModuloECU> modulos = new ArrayList<>();

    /** possui 0..1 InformacoesSegurancaVeiculo (null = não tem). */
    private InformacoesSegurancaVeiculo informacoesSeguranca;

    public Veiculo() {
    }

    /** <<create>> (FA-04.3). */
    public Veiculo(String placa, String marca, String modelo, int anoModelo,
                   String motorizacao, String numeroChassi) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anoModelo = anoModelo;
        this.motorizacao = motorizacao;
        this.numeroChassi = numeroChassi;
    }

    /** Mensagem 1.9. */
    public void associarModulo(ModuloECU mod) {
        if (mod != null && !modulos.contains(mod)) {
            modulos.add(mod);
        }
    }

    public List<ModuloECU> getModulos() {
        return modulos;
    }

    public InformacoesSegurancaVeiculo getInformacoesSeguranca() {
        return informacoesSeguranca;
    }

    public void setInformacoesSeguranca(InformacoesSegurancaVeiculo informacoesSeguranca) {
        this.informacoesSeguranca = informacoesSeguranca;
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

    /** Campo "Marca / Modelo" do mockup. */
    public String getMarcaModelo() {
        return marca + " " + modelo + (motorizacao == null ? "" : " " + motorizacao);
    }

    @Override
    public String toString() {
        return placa;
    }
}
