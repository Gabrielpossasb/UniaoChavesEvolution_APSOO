package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.OrdemDeServico;
import uniaoevolutions.negocio.OrdemDeServicoChaves;
import uniaoevolutions.negocio.OrdemDeServicoModulos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DAO da hierarquia OrdemDeServico. "Uma tabela por subclasse" (PK da subclasse = FK):
 *
 *     OrdemDeServico (abstrata)  -> TB_OrdemDeServico
 *       |_ OrdemDeServicoModulos -> TB_OrdemDeServicoModulos
 *       |_ OrdemDeServicoChaves  -> TB_OrdemDeServicoChaves
 *
 * Gravar = dois INSERTs numa transação, com o mesmo id.
 */
public class OrdemServicoDAO {

    /** Mensagem 1.11; o objeto recebe o id gerado. */
    public void salvar(OrdemDeServico os) throws SQLException {
        Connection conexao = Conexao.conectar();
        try {
            // Superclasse e subclasse na mesma transação.
            conexao.setAutoCommit(false);
            try {
                removerRascunhoDoOrcamento(conexao, os.getOrcamento().getIdOrcamento());
                int id = inserirSuperclasse(conexao, os);
                os.setIdOrdemDeServico(id);
                inserirSubclasse(conexao, os, id);
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

    /** RN-07: próximo OS-AAAA-NNNN; a sequência reinicia a cada ano. */
    public String gerarNumeroOS() throws SQLException {
        int ano = Calendar.getInstance().get(Calendar.YEAR);
        String prefixo = String.format("OS-%d-", ano);

        String sql = "SELECT MAX(numeroOS) AS ultimo FROM TB_OrdemDeServico WHERE numeroOS LIKE ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, prefixo + "%");
            try (ResultSet resultado = comando.executeQuery()) {
                int sequencia = 1;
                if (resultado.next() && resultado.getString("ultimo") != null) {
                    String ultimo = resultado.getString("ultimo");
                    sequencia = Integer.parseInt(ultimo.substring(prefixo.length())) + 1;
                }
                return String.format("%s%04d", prefixo, sequencia);
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** ACRÉSCIMO (passo 8.2, POS-05): OS do veículo, da mais recente à mais antiga (só para exibir). */
    public List<OrdemDeServico> buscarHistoricoTecnico(int idVeiculo) throws SQLException {
        String sql = "SELECT os.idOrdemDeServico, os.numeroOS, os.dataAbertura, os.statusOS, "
                   + "       os.quilometragem, m.diagnosticoInicial, c.servicoExecutado "
                   + "  FROM TB_OrdemDeServico os "
                   + "  LEFT JOIN TB_OrdemDeServicoModulos m ON m.idOrdemDeServico = os.idOrdemDeServico "
                   + "  LEFT JOIN TB_OrdemDeServicoChaves  c ON c.idOrdemDeServico = os.idOrdemDeServico "
                   + " WHERE os.idVeiculo = ? AND os.statusOS <> 'Rascunho' "
                   + " ORDER BY os.idOrdemDeServico DESC";

        List<OrdemDeServico> historico = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idVeiculo);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    historico.add(montarOrdemDeServico(resultado));
                }
            }
            return historico;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    // -----------------------------------------------------------------
    // Métodos privados
    // -----------------------------------------------------------------

    /** INSERT na superclasse; devolve o id gerado. */
    private int inserirSuperclasse(Connection conexao, OrdemDeServico os) throws SQLException {
        String sql = "INSERT INTO TB_OrdemDeServico "
                   + " (numeroOS, dataAbertura, statusOS, quilometragem, prazoEstimado, "
                   + "  observacoes, idOrcamento, idVeiculo, idUsuario) "
                   + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement comando =
                     conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, os.getNumeroOS());
            comando.setDate(2, paraDataSql(os.getDataAbertura()));
            comando.setString(3, os.getStatusOS());
            comando.setInt(4, os.getQuilometragem());
            comando.setDate(5, paraDataSql(os.getPrazoEstimado()));
            comando.setString(6, os.getObservacoes());
            comando.setInt(7, os.getOrcamento().getIdOrcamento());
            comando.setInt(8, os.getVeiculo().getIdVeiculo());
            comando.setInt(9, os.getTecnico().getIdUsuario());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getInt(1);
                }
                throw new SQLException("O banco não devolveu o código da Ordem de Serviço gravada.");
            }
        }
    }

    /** INSERT na tabela da subclasse, com o id da superclasse. */
    private void inserirSubclasse(Connection conexao, OrdemDeServico os, int id) throws SQLException {
        if (os instanceof OrdemDeServicoModulos) {
            OrdemDeServicoModulos osModulos = (OrdemDeServicoModulos) os;
            String sql = "INSERT INTO TB_OrdemDeServicoModulos "
                       + " (idOrdemDeServico, relatoInicial, condicaoRecebimento, diagnosticoInicial) "
                       + " VALUES (?, ?, ?, ?)";
            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setInt(1, id);
                comando.setString(2, osModulos.getRelatoInicial());
                comando.setString(3, osModulos.getCondicaoRecebimento());
                comando.setString(4, osModulos.getDiagnosticoInicial());
                comando.executeUpdate();
            }
        } else if (os instanceof OrdemDeServicoChaves) {
            OrdemDeServicoChaves osChaves = (OrdemDeServicoChaves) os;
            String sql = "INSERT INTO TB_OrdemDeServicoChaves (idOrdemDeServico, servicoExecutado) "
                       + " VALUES (?, ?)";
            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setInt(1, id);
                comando.setString(2, osChaves.getServicoExecutado());
                comando.executeUpdate();
            }
        }
    }

    /**
     * Remove o rascunho do orçamento, se houver: idOrcamento é UNIQUE (RN-02) e o
     * FA-11C.3 permite retomar. Só apaga statusOS = 'Rascunho' (RN-06).
     */
    private void removerRascunhoDoOrcamento(Connection conexao, int idOrcamento) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String busca = "SELECT idOrdemDeServico FROM TB_OrdemDeServico "
                     + " WHERE idOrcamento = ? AND statusOS = ?";
        try (PreparedStatement comando = conexao.prepareStatement(busca)) {
            comando.setInt(1, idOrcamento);
            comando.setString(2, OrdemDeServico.STATUS_RASCUNHO);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ids.add(resultado.getInt("idOrdemDeServico"));
                }
            }
        }

        // Subclasse primeiro (aponta para a superclasse).
        for (int id : ids) {
            executarExclusao(conexao, "DELETE FROM TB_OrdemDeServicoModulos WHERE idOrdemDeServico = ?", id);
            executarExclusao(conexao, "DELETE FROM TB_OrdemDeServicoChaves  WHERE idOrdemDeServico = ?", id);
            executarExclusao(conexao, "DELETE FROM TB_OrdemDeServico        WHERE idOrdemDeServico = ?", id);
        }
    }

    private void executarExclusao(Connection conexao, String sql, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    /** Inverso de inserirSubclasse(): a subclasse vem do LEFT JOIN com linha. */
    private OrdemDeServico montarOrdemDeServico(ResultSet resultado) throws SQLException {
        OrdemDeServico os;
        if (resultado.getString("servicoExecutado") != null) {
            OrdemDeServicoChaves osChaves = new OrdemDeServicoChaves();
            osChaves.setServicoExecutado(resultado.getString("servicoExecutado"));
            os = osChaves;
        } else {
            OrdemDeServicoModulos osModulos = new OrdemDeServicoModulos();
            osModulos.setDiagnosticoInicial(resultado.getString("diagnosticoInicial"));
            os = osModulos;
        }
        os.setIdOrdemDeServico(resultado.getInt("idOrdemDeServico"));
        os.setNumeroOS(resultado.getString("numeroOS"));
        os.restaurarEstadoPersistido(resultado.getDate("dataAbertura"), resultado.getString("statusOS"));
        os.setQuilometragem(resultado.getInt("quilometragem"));
        return os;
    }

    /** java.util.Date para java.sql.Date. */
    private java.sql.Date paraDataSql(Date data) {
        return data == null ? null : new java.sql.Date(data.getTime());
    }
}
