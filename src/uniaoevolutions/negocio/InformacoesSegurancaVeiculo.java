package uniaoevolutions.negocio;

/**
 * Informações de segurança de um veículo (senhas e código mecânico).
 *
 * Diagrama de Classes v2: codigoMecanico, senhaImobilizador, senhaAlarme,
 * senhaRadio, sem operações. Associação "Veiculo 1 possui 0..1
 * InformacoesSegurancaVeiculo". Tabela TB_InformacoesSegurancaVeiculo, cuja
 * PK é também FK para TB_Veiculo (a classe não tem identificador próprio).
 */
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
