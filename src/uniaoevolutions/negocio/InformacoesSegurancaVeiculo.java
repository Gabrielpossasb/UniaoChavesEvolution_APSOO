package uniaoevolutions.negocio;

/** Senhas e código mecânico do veículo (Diagrama de Classes v2). PK da tabela = FK de TB_Veiculo. */
public class InformacoesSegurancaVeiculo {

    private String codigoMecanico;
    private String senhaImobilizador;
    private String senhaAlarme;
    private String senhaRadio;

    public InformacoesSegurancaVeiculo() {
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
}
