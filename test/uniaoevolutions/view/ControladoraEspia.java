package uniaoevolutions.view;

import uniaoevolutions.controle.ControladoraSolicitarOrcamento;
import uniaoevolutions.negocio.Cliente;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.ServicoBase;
import uniaoevolutions.negocio.Veiculo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Controladora real com um registro das mensagens do DSS (Interface -> Sistema)
 * que a tela envia e que a controladora ACEITA, na ordem em que chegam. As
 * recusadas (exceção) não entram: são as que a ordem do DSS barra. Usada para conferir que a tela
 * respeita a ordem do DSS.
 */
class ControladoraEspia extends ControladoraSolicitarOrcamento {

    final List<String> mensagens = Collections.synchronizedList(new ArrayList<>());

    ControladoraEspia(int idUsuarioLogado) {
        super(idUsuarioLogado);
    }

    @Override
    public Orcamento iniciarOrcamento() throws SQLException {
        var resultado = super.iniciarOrcamento();
        mensagens.add("iniciarOrcamento");   // só depois de aceita
        return resultado;
    }

    @Override
    public Cliente informarCliente(String origemAtendimento, String cpfCnpjCliente) throws SQLException {
        var resultado = super.informarCliente(origemAtendimento, cpfCnpjCliente);
        mensagens.add("informarCliente");   // só depois de aceita
        return resultado;
    }

    @Override
    public Cliente cadastrarCliente(String nome, String telefone) throws SQLException {
        var resultado = super.cadastrarCliente(nome, telefone);
        mensagens.add("cadastrarCliente");   // só depois de aceita
        return resultado;
    }

    @Override
    public Veiculo selecionarVeiculo(String placa) throws SQLException {
        var resultado = super.selecionarVeiculo(placa);
        mensagens.add("selecionarVeiculo");   // só depois de aceita
        return resultado;
    }

    @Override
    public Veiculo cadastrarVeiculo(String placa, String marca, String modelo, int anoModelo,
                                    String motorizacao, String numeroChassi) throws SQLException {
        var resultado = super.cadastrarVeiculo(placa, marca, modelo, anoModelo, motorizacao, numeroChassi);
        mensagens.add("cadastrarVeiculo");   // só depois de aceita
        return resultado;
    }

    @Override
    public List<ServicoBase> informarSegmento(String segmento) throws SQLException {
        var resultado = super.informarSegmento(segmento);
        mensagens.add("informarSegmento");   // só depois de aceita
        return resultado;
    }

    @Override
    public List<Orcamento> informarDadosModulo(String tipoModulo, String modeloECU, String identificacaoModulo,
                                               String sintomaRelatado) throws SQLException {
        var resultado = super.informarDadosModulo(tipoModulo, modeloECU, identificacaoModulo, sintomaRelatado);
        mensagens.add("informarDadosModulo");   // só depois de aceita
        return resultado;
    }

    @Override
    public void informarIdentificacaoIlegivel(String tipoModulo, String sintomaRelatado) {
        super.informarIdentificacaoIlegivel(tipoModulo, sintomaRelatado);
        mensagens.add("informarIdentificacaoIlegivel");   // só depois de aceita
    }

    @Override
    public List<Orcamento> informarDescricaoModulo(String registroFotografico, String descricaoFisicaPlaca)
            throws SQLException {
        var resultado = super.informarDescricaoModulo(registroFotografico, descricaoFisicaPlaca);
        mensagens.add("informarDescricaoModulo");   // só depois de aceita
        return resultado;
    }

    @Override
    public void informarDiagnosticoPendente() {
        super.informarDiagnosticoPendente();
        mensagens.add("informarDiagnosticoPendente");   // só depois de aceita
    }

    @Override
    public String informarDadosChaves(String tipoChave, int quantidadeCopias, boolean possuiTelecomando,
                                      String codigoTransponder, String situacaoImobilizador, boolean perdaTotal) {
        var resultado = super.informarDadosChaves(tipoChave, quantidadeCopias, possuiTelecomando, codigoTransponder,
                situacaoImobilizador, perdaTotal);
        mensagens.add("informarDadosChaves");   // só depois de aceita
        return resultado;
    }

    @Override
    public ServicoBase informarComprovacaoPropriedade(String documentoPropriedade, String nomeSolicitante,
                                                      String documentoSolicitante) throws SQLException {
        var resultado = super.informarComprovacaoPropriedade(documentoPropriedade, nomeSolicitante, documentoSolicitante);
        mensagens.add("informarComprovacaoPropriedade");   // só depois de aceita
        return resultado;
    }

    @Override
    public double incluirItem(int idServicoBase, double valorProposto) throws SQLException {
        var resultado = super.incluirItem(idServicoBase, valorProposto);
        mensagens.add("incluirItem");   // só depois de aceita
        return resultado;
    }

    @Override
    public double removerItem(int idItem) {
        var resultado = super.removerItem(idItem);
        mensagens.add("removerItem");   // só depois de aceita
        return resultado;
    }

    @Override
    public double alterarValorItem(int idItem, double novoValorProposto) {
        var resultado = super.alterarValorItem(idItem, novoValorProposto);
        mensagens.add("alterarValorItem");   // só depois de aceita
        return resultado;
    }

    @Override
    public Orcamento registrarOrcamento(Date dataValidade, String observacoes) throws SQLException {
        var resultado = super.registrarOrcamento(dataValidade, observacoes);
        mensagens.add("registrarOrcamento");   // só depois de aceita
        return resultado;
    }

    @Override
    public void salvarRascunhoOrcamento(String observacoes) throws SQLException {
        super.salvarRascunhoOrcamento(observacoes);
        mensagens.add("salvarRascunhoOrcamento");   // só depois de aceita
    }

    @Override
    public String solicitarConfirmacaoDesistencia() {
        var resultado = super.solicitarConfirmacaoDesistencia();
        mensagens.add("solicitarConfirmacaoDesistencia");   // só depois de aceita
        return resultado;
    }

    @Override
    public List<Orcamento> descartarOrcamento() throws SQLException {
        var resultado = super.descartarOrcamento();
        mensagens.add("descartarOrcamento");   // só depois de aceita
        return resultado;
    }

    /** As mensagens sem repetições seguidas (a tela reenvia ao corrigir um campo). */
    List<String> sequencia() {
        List<String> sem = new ArrayList<>();
        synchronized (mensagens) {
            for (String m : mensagens) {
                if (sem.isEmpty() || !sem.get(sem.size() - 1).equals(m)) {
                    sem.add(m);
                }
            }
        }
        return sem;
    }

    /** Posição da primeira ocorrência (ou -1). */
    int primeira(String mensagem) {
        return mensagens.indexOf(mensagem);
    }
}
