package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.Cliente;
import uniaoevolutions.negocio.ItemOrcamento;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.ServicoBase;
import uniaoevolutions.negocio.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da classe Orcamento - tabelas TB_Orcamento, TB_ItemOrcamento e
 * TB_ServicoBase, mais os JOINs com TB_Cliente e TB_Veiculo.
 *
 * Diagrama de Classes:
 *   + buscarOrcamento(id : int) : Orcamento
 *   + buscarOrcamentosAprovados() : List<Orcamento>
 *
 * Esta classe mostra o ponto central do mapeamento objeto-relacional: no banco
 * o orçamento, o cliente, o veículo e os itens estão em cinco tabelas ligadas
 * por chave estrangeira; aqui eles voltam a ser UM objeto Orcamento com as
 * suas associações preenchidas. Quem chamar a DAO não vê tabela nem JOIN.
 */
public class OrcamentoDAO {

    /** Colunas do orçamento e das suas associações "solicita" (Cliente) e "refere-se a" (Veiculo). */
    private static final String SELECT_ORCAMENTO =
            "SELECT o.idOrcamento, o.dataSolicitacao, o.origemAtendimento, o.segmentoPretendido, "
          + "       o.valorEstimado, o.status, "
          + "       c.idCliente, c.nome AS nomeCliente, c.telefone, "
          + "       v.idVeiculo, v.marca, v.modelo, v.anoModelo, v.motorizacao, v.placa, "
          + "       v.numeroChassi, v.tipoModulo, v.modeloECU, v.identificacaoModulo, "
          + "       v.codigoMecanico, v.senhaImobilizador, v.senhaAlarme, v.senhaRadio "
          + "  FROM TB_Orcamento o "
          + "  JOIN TB_Cliente  c ON c.idCliente = o.idCliente "
          + "  JOIN TB_Veiculo  v ON v.idVeiculo = o.idVeiculo ";

    /**
     * Mensagem 1.1 do Diagrama de Comunicação:
     *   orc = buscarOrcamento(orcamentoSelecionado) : Orcamento
     * Devolve o orçamento já com cliente, veículo e itens carregados, ou null
     * se o código não existir.
     */
    public Orcamento buscarOrcamento(int id) throws SQLException {
        String sql = SELECT_ORCAMENTO + " WHERE o.idOrcamento = ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                Orcamento orcamento = montarOrcamento(resultado);
                carregarItens(conexao, orcamento);
                return orcamento;
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /**
     * RN-01 / PRE-02 - lista os orçamentos "Aprovado pelo cliente" que ainda
     * não foram convertidos em Ordem de Serviço. É o passo 2 do fluxo
     * principal e alimenta a combo "Orçamento associado" do mockup.
     *
     * O NOT EXISTS é o que garante o "ainda não convertido": se já existe uma
     * OS apontando para este orçamento, ele sai da lista.
     */
    public List<Orcamento> buscarOrcamentosAprovados() throws SQLException {
        String sql = SELECT_ORCAMENTO
                   + " WHERE o.status = ? "
                   + "   AND NOT EXISTS (SELECT 1 FROM TB_OrdemDeServico os "
                   + "                    WHERE os.idOrcamento = o.idOrcamento "
                   + "                      AND os.statusOS <> 'Rascunho') "
                   + " ORDER BY o.idOrcamento";

        List<Orcamento> orcamentos = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, Orcamento.STATUS_APROVADO);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    orcamentos.add(montarOrcamento(resultado));
                }
            }
            for (Orcamento orcamento : orcamentos) {
                carregarItens(conexao, orcamento);
            }
            return orcamentos;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /**
     * Atualiza a situação do orçamento.
     *
     * ACRÉSCIMO à modelagem: este método não está no Diagrama de Classes, mas
     * é necessário para a POS-02 ("o orçamento de origem passa para a situação
     * 'Convertido em OS'") e para a RN-05 (a alteração de item devolve o
     * orçamento a "Aguardando aprovação do cliente"). Sem ele, as duas regras
     * não teriam como ser cumpridas.
     */
    public void atualizarStatus(int idOrcamento, String status) throws SQLException {
        String sql = "UPDATE TB_Orcamento SET status = ? WHERE idOrcamento = ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, status);
            comando.setInt(2, idOrcamento);
            comando.executeUpdate();
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /**
     * Inclui no orçamento um serviço ou peça que não estava previsto e soma o
     * valor dele ao valorEstimado do orçamento.
     *
     * ACRÉSCIMO à modelagem: não está no Diagrama de Classes, mas é o que o
     * FA-11B exige para que a inclusão de item tenha efeito.
     *
     * Dois INSERTs numa transação, porque o item precisa de um serviço de
     * catálogo: primeiro a linha em TB_ServicoBase, depois a linha em
     * TB_ItemOrcamento que a referencia.
     *
     * A justificativa da RN-05 é gravada em TB_ServicoBase.observacoesTecnicas:
     * o Mapeamento Objeto-Relacional não tem coluna própria para ela, e esta é
     * a única coluna de texto livre ligada ao item. A lacuna está registrada na
     * DOCUMENTACAO.md.
     */
    public void incluirItem(int idOrcamento, String descricao, String categoria,
                            int quantidade, double valor, String justificativa) throws SQLException {
        Connection conexao = Conexao.conectar();
        try {
            conexao.setAutoCommit(false);
            try {
                int idServicoBase = inserirServicoBase(conexao, descricao, categoria, justificativa);
                inserirItem(conexao, idOrcamento, idServicoBase, quantidade, valor);
                somarAoValorEstimado(conexao, idOrcamento, quantidade * valor);
                conexao.commit();
            } catch (SQLException erro) {
                conexao.rollback();
                throw erro;
            } finally {
                conexao.setAutoCommit(true);
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    private int inserirServicoBase(Connection conexao, String descricao, String categoria,
                                   String justificativa) throws SQLException {
        String sql = "INSERT INTO TB_ServicoBase (descricao, categoria, observacoesTecnicas) "
                   + " VALUES (?, ?, ?)";
        try (PreparedStatement comando =
                     conexao.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, descricao);
            comando.setString(2, categoria);
            comando.setString(3, "Justificativa (RN-05): " + justificativa);
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getInt(1);
                }
                throw new SQLException("O banco não devolveu o código do serviço incluído.");
            }
        }
    }

    private void inserirItem(Connection conexao, int idOrcamento, int idServicoBase,
                             int quantidade, double valor) throws SQLException {
        String sql = "INSERT INTO TB_ItemOrcamento (valorProposto, quantidade, idOrcamento, idServicoBase) "
                   + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setDouble(1, valor);
            comando.setInt(2, quantidade);
            comando.setInt(3, idOrcamento);
            comando.setInt(4, idServicoBase);
            comando.executeUpdate();
        }
    }

    private void somarAoValorEstimado(Connection conexao, int idOrcamento, double acrescimo)
            throws SQLException {
        String sql = "UPDATE TB_Orcamento SET valorEstimado = valorEstimado + ? WHERE idOrcamento = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setDouble(1, acrescimo);
            comando.setInt(2, idOrcamento);
            comando.executeUpdate();
        }
    }

    // -----------------------------------------------------------------
    // Métodos privados de montagem dos objetos
    // -----------------------------------------------------------------

    /**
     * Carrega os itens do orçamento (composição "compoe" 1..*), com o serviço
     * de catálogo de cada um. É daqui que sai a tabela de itens do mockup.
     */
    private void carregarItens(Connection conexao, Orcamento orcamento) throws SQLException {
        String sql = "SELECT i.idItemOrcamento, i.valorProposto, i.quantidade, "
                   + "       s.idServicoBase, s.descricao, s.categoria, s.observacoesTecnicas "
                   + "  FROM TB_ItemOrcamento i "
                   + "  JOIN TB_ServicoBase  s ON s.idServicoBase = i.idServicoBase "
                   + " WHERE i.idOrcamento = ? "
                   + " ORDER BY i.idItemOrcamento";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, orcamento.getIdOrcamento());
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ServicoBase servico = new ServicoBase(
                            resultado.getInt("idServicoBase"),
                            resultado.getString("descricao"),
                            resultado.getString("categoria"),
                            resultado.getString("observacoesTecnicas"));

                    orcamento.adicionarItem(new ItemOrcamento(
                            resultado.getInt("idItemOrcamento"),
                            resultado.getDouble("valorProposto"),
                            resultado.getInt("quantidade"),
                            servico));
                }
            }
        }
    }

    /** Monta o Orcamento, o Cliente e o Veiculo a partir da linha do JOIN. */
    private Orcamento montarOrcamento(ResultSet resultado) throws SQLException {
        Orcamento orcamento = new Orcamento();
        orcamento.setIdOrcamento(resultado.getInt("idOrcamento"));
        orcamento.setDataSolicitacao(resultado.getDate("dataSolicitacao"));
        orcamento.setOrigemAtendimento(resultado.getString("origemAtendimento"));
        orcamento.setSegmentoPretendido(resultado.getString("segmentoPretendido"));
        orcamento.setValorEstimado(resultado.getDouble("valorEstimado"));
        orcamento.setStatus(resultado.getString("status"));

        orcamento.setCliente(new Cliente(
                resultado.getInt("idCliente"),
                resultado.getString("nomeCliente"),
                resultado.getString("telefone")));

        orcamento.setVeiculo(montarVeiculo(resultado));
        return orcamento;
    }

    /** Monta o Veiculo a partir das colunas de TB_Veiculo presentes no JOIN. */
    private Veiculo montarVeiculo(ResultSet resultado) throws SQLException {
        Veiculo veiculo = new Veiculo();
        veiculo.setIdVeiculo(resultado.getInt("idVeiculo"));
        veiculo.setMarca(resultado.getString("marca"));
        veiculo.setModelo(resultado.getString("modelo"));
        veiculo.setAnoModelo(resultado.getInt("anoModelo"));
        veiculo.setMotorizacao(resultado.getString("motorizacao"));
        veiculo.setPlaca(resultado.getString("placa"));
        veiculo.setNumeroChassi(resultado.getString("numeroChassi"));
        veiculo.setTipoModulo(resultado.getString("tipoModulo"));
        veiculo.setModeloECU(resultado.getString("modeloECU"));
        veiculo.setIdentificacaoModulo(resultado.getString("identificacaoModulo"));
        veiculo.setCodigoMecanico(resultado.getString("codigoMecanico"));
        veiculo.setSenhaImobilizador(resultado.getString("senhaImobilizador"));
        veiculo.setSenhaAlarme(resultado.getString("senhaAlarme"));
        veiculo.setSenhaRadio(resultado.getString("senhaRadio"));
        return veiculo;
    }
}
