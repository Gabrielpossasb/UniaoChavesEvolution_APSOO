package uniaoevolutions.negocio;

import java.util.Date;

/** Orçamento do segmento Módulos (V1). Mensagens 1.3, 1.5 e 1.10. Refere-se a 0..1 ModuloECU. */
public class OrcamentoModulos extends Orcamento {

    private String sintomaRelatado;
    private boolean diagnosticoPendenteBancada;
    private boolean identificacaoIlegivel;
    private String registroFotografico;
    private String descricaoFisicaPlaca;

    private ModuloECU modulo;

    public OrcamentoModulos() {
    }

    /** Mensagem 1.3: [seg = "Modulos"] <<create>> OrcamentoModulos(...). */
    public OrcamentoModulos(String numero, Date dataValidade, String origemAtendimento,
                            String observacoes, String sintomaRelatado,
                            boolean diagnosticoPendenteBancada) {
        super(numero, dataValidade, origemAtendimento, observacoes);
        this.sintomaRelatado = sintomaRelatado;
        this.diagnosticoPendenteBancada = diagnosticoPendenteBancada;
    }

    /** Mensagem 1.5 (FAV-8.1): etiqueta ilegível; valem foto e descrição. */
    public void setModuloIlegivel(String registroFotografico, String descricaoFisicaPlaca) {
        this.identificacaoIlegivel = true;
        this.registroFotografico = registroFotografico;
        this.descricaoFisicaPlaca = descricaoFisicaPlaca;
    }

    /** Mensagem 1.10: [seg = "Modulos"] associarModulo(mod). */
    public void associarModulo(ModuloECU mod) {
        this.modulo = mod;
    }

    /** RN-03: segmento da subclasse. */
    @Override
    public String getSegmentoPretendido() {
        return Segmento.MODULOS;
    }

    public String getSintomaRelatado() {
        return sintomaRelatado;
    }

    public boolean isDiagnosticoPendenteBancada() {
        return diagnosticoPendenteBancada;
    }

    public boolean isIdentificacaoIlegivel() {
        return identificacaoIlegivel;
    }

    public String getRegistroFotografico() {
        return registroFotografico;
    }

    public String getDescricaoFisicaPlaca() {
        return descricaoFisicaPlaca;
    }

    public ModuloECU getModulo() {
        return modulo;
    }
}
