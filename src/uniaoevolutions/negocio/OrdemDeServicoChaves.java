package uniaoevolutions.negocio;

import java.util.Date;

/**
 * Ordem de Serviço do segmento Programação / Confecção de Chaves.
 * É a variante V2 da Descrição Expandida.
 *
 * Diagrama de Classes:
 *   atributo  : servicoExecutado : String
 *   construtor: + <<create>> OrdemDeServicoChaves(quilometragem : int,
 *               prazoEstimado : Date, observacoes : String,
 *               servicoExecutado : String)
 *
 * O construtor é a mensagem 1.6 do Diagrama de Comunicação, com a guarda
 * [seg = "Chaves"].
 *
 * LACUNA DA MODELAGEM: a variante V2 da Descrição Expandida fala em tipo de
 * chave, quantidade de cópias, código do transponder e situação do
 * imobilizador, mas a classe só tem servicoExecutado. Os dados do imobilizador
 * e do transponder que existem na modelagem são atributos do Veiculo
 * (codigoMecanico, senhaImobilizador, senhaAlarme, senhaRadio), e é assim que
 * a tela os exibe. Ver DOCUMENTACAO.md.
 */
public class OrdemDeServicoChaves extends OrdemDeServico {

    private String servicoExecutado;

    public OrdemDeServicoChaves() {
    }

    /** Mensagem 1.6 do Diagrama de Comunicação: <<create>> com a guarda [seg = "Chaves"]. */
    public OrdemDeServicoChaves(int quilometragem, Date prazoEstimado, String observacoes,
                                String servicoExecutado) {
        super(quilometragem, prazoEstimado, observacoes);
        this.servicoExecutado = servicoExecutado;
    }

    @Override
    public String getSegmento() {
        return Segmento.CHAVES;
    }

    /**
     * RN-03 no segmento Chaves: além do que vale para qualquer OS, o Contrato
     * de Operação exige que servicoExecutado tenha sido informado.
     */
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
