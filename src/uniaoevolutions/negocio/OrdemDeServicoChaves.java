package uniaoevolutions.negocio;

import java.util.Date;

/**
 * OS do segmento Programação / Confecção de Chaves - variante V2 da Descrição
 * Expandida.
 *
 * LACUNA DA MODELAGEM: a V2 fala em tipo de chave, cópias e transponder, mas a
 * classe só tem servicoExecutado; os demais dados são atributos do Veiculo e é
 * de lá que a tela os exibe. Ver DOCUMENTACAO.md.
 */
public class OrdemDeServicoChaves extends OrdemDeServico {

    private String servicoExecutado;

    public OrdemDeServicoChaves() {
    }

    /** Mensagem 1.6: <<create>> com a guarda [seg = "Chaves"]. */
    public OrdemDeServicoChaves(int quilometragem, Date prazoEstimado, String observacoes,
                                String servicoExecutado) {
        super(quilometragem, prazoEstimado, observacoes);
        this.servicoExecutado = servicoExecutado;
    }

    @Override
    public String getSegmento() {
        return Segmento.CHAVES;
    }

    /** RN-03 no segmento Chaves: servicoExecutado é obrigatório. */
    @Override
    public void validar() {
        super.validar();
        if (servicoExecutado == null || servicoExecutado.isBlank()) {
            throw new IllegalArgumentException("Informe o serviço de chaves a ser executado.");
        }
    }

    public String getServicoExecutado() {
        return servicoExecutado;
    }

    public void setServicoExecutado(String servicoExecutado) {
        this.servicoExecutado = servicoExecutado;
    }
}
