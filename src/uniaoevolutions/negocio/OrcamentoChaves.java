package uniaoevolutions.negocio;

import java.util.Date;

/** Orçamento do segmento Chaves (V2). Mensagens 1.4 e 1.6. */
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

    /** Mensagem 1.6 (FAV-8.4A): documento do veículo e solicitante. */
    public void setComprovacaoPropriedade(String documentoPropriedade, String nomeSolicitante,
                                          String documentoSolicitante) {
        this.documentoPropriedade = documentoPropriedade;
        this.nomeSolicitante = nomeSolicitante;
        this.documentoSolicitante = documentoSolicitante;
    }

    /** RN-03: segmento da subclasse. */
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
