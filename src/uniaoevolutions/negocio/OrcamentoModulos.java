package uniaoevolutions.negocio;

import java.util.Date;

/**
 * Orçamento do segmento Reparação / Programação de Módulos (ECU) - variante
 * V1 do Solicitar Orçamento.
 *
 * Diagrama de Classes v2: sintomaRelatado, diagnosticoPendenteBancada,
 * identificacaoIlegivel, registroFotografico, descricaoFisicaPlaca;
 * <<create>> OrcamentoModulos(numero, dataValidade, origemAtendimento,
 * observacoes, sintomaRelatado, diagnosticoPendenteBancada) (mensagem 1.3),
 * setModuloIlegivel(registroFotografico, descricaoFisicaPlaca) (mensagem 1.5)
 * e associarModulo(mod) (mensagem 1.10).
 *
 * Associação: "OrcamentoModulos 0..* refere-se a 0..1 ModuloECU".
 */
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

    /**
     * Mensagem 1.5: [identificacaoIlegivel] setModuloIlegivel(...) - FAV-8.1:
     * a etiqueta do módulo está ilegível, então valem a foto e a descrição.
     */
    public void setModuloIlegivel(String registroFotografico, String descricaoFisicaPlaca) {
        this.identificacaoIlegivel = true;
        this.registroFotografico = registroFotografico;
        this.descricaoFisicaPlaca = descricaoFisicaPlaca;
    }

    /** Mensagem 1.10: [seg = "Modulos"] associarModulo(mod). */
    public void associarModulo(ModuloECU mod) {
        this.modulo = mod;
    }

    /** Polimorfismo: o segmento é o da subclasse (RN-03). */
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
