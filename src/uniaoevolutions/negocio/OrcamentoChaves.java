package uniaoevolutions.negocio;

import java.util.Date;

/**
 * Orçamento do segmento Programação / Confecção de Chaves - variante V2 do
 * Solicitar Orçamento.
 *
 * Diagrama de Classes v2: tipoChave, quantidadeCopias, possuiTelecomando,
 * codigoTransponder, situacaoImobilizador, perdaTotal, documentoPropriedade,
 * nomeSolicitante, documentoSolicitante; <<create>> OrcamentoChaves(numero,
 * dataValidade, origemAtendimento, observacoes, tipoChave, quantidadeCopias,
 * possuiTelecomando, codigoTransponder, situacaoImobilizador, perdaTotal)
 * (mensagem 1.4) e setComprovacaoPropriedade(documentoPropriedade,
 * nomeSolicitante, documentoSolicitante) (mensagem 1.6).
 */
public class OrcamentoChaves extends Orcamento {

    private String tipoChave;
    private int quantidadeCopias;
    private boolean possuiTelecomando;
    private String codigoTransponder;
    private String situacaoImobilizador;
    private boolean perdaTotal;
    private String documentoPropriedade;
    private String nomeSolicitante;
    private String documentoSolicitante;

    public OrcamentoChaves() {
    }

    /** Mensagem 1.4: [seg = "Chaves"] <<create>> OrcamentoChaves(...). */
    public OrcamentoChaves(String numero, Date dataValidade, String origemAtendimento,
                           String observacoes, String tipoChave, int quantidadeCopias,
                           boolean possuiTelecomando, String codigoTransponder,
                           String situacaoImobilizador, boolean perdaTotal) {
        super(numero, dataValidade, origemAtendimento, observacoes);
        this.tipoChave = tipoChave;
        this.quantidadeCopias = quantidadeCopias;
        this.possuiTelecomando = possuiTelecomando;
        this.codigoTransponder = codigoTransponder;
        this.situacaoImobilizador = situacaoImobilizador;
        this.perdaTotal = perdaTotal;
    }

    /**
     * Mensagem 1.6: [perdaTotal] setComprovacaoPropriedade(...) - FAV-8.4A:
     * na perda total, documento do veículo e identificação do solicitante.
     */
    public void setComprovacaoPropriedade(String documentoPropriedade, String nomeSolicitante,
                                          String documentoSolicitante) {
        this.documentoPropriedade = documentoPropriedade;
        this.nomeSolicitante = nomeSolicitante;
        this.documentoSolicitante = documentoSolicitante;
    }

    /** Polimorfismo: o segmento é o da subclasse (RN-03). */
    @Override
    public String getSegmentoPretendido() {
        return Segmento.CHAVES;
    }

    public String getTipoChave() {
        return tipoChave;
    }

    public int getQuantidadeCopias() {
        return quantidadeCopias;
    }

    public boolean isPossuiTelecomando() {
        return possuiTelecomando;
    }

    public String getCodigoTransponder() {
        return codigoTransponder;
    }

    public String getSituacaoImobilizador() {
        return situacaoImobilizador;
    }

    public boolean isPerdaTotal() {
        return perdaTotal;
    }

    public String getDocumentoPropriedade() {
        return documentoPropriedade;
    }

    public String getNomeSolicitante() {
        return nomeSolicitante;
    }

    public String getDocumentoSolicitante() {
        return documentoSolicitante;
    }
}
