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
 * DAO da hierarquia OrdemDeServico. É ONDE A HERANÇA VIRA TABELA: o
 * Mapeamento OR usa "uma tabela por classe", com a PK da subclasse servindo
 * também de FK para a tabela da superclasse.
 *
 *     OrdemDeServico (abstrata)  -> TB_OrdemDeServico
 *       |_ OrdemDeServicoModulos -> TB_OrdemDeServicoModulos (PK = FK)
 *       |_ OrdemDeServicoChaves  -> TB_OrdemDeServicoChaves  (PK = FK)
 *
 * Gravar uma OS é sempre dois INSERTs numa transação: a superclasse gera o id,
 * a subclasse reusa esse mesmo id.
 */
public class OrdemServicoDAO {

    /**
     * Mensagem 1.11: salvar(os). O objeto recebe de volta o id gerado pelo
     * banco.
     */
    public void salvar(OrdemDeServico os) throws SQLException {
        Connection conexao = Conexao.conectar();
        try {
            // As linhas da superclasse e da subclasse têm de nascer juntas.
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

    /**
     * RN-07: próximo número OS-AAAA-NNNN, "com sequência reiniciada a cada
     * ano". O reinício sai do LIKE, que só olha os números do ano corrente.
     */
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

    /**
     * OS anteriores do mesmo veículo, da mais recente para a mais antiga.
     * ACRÉSCIMO à modelagem, exigido pelo passo 8.2 da V1, pelo botão
     * "Ver atendimentos anteriores" do mockup e pela POS-05.
     *
     * São objetos só de consulta: as associações (orçamento, veículo, técnico)
     * ficam vazias, porque isto é só para exibir o histórico.
     */
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

    /** INSERT na superclasse. Devolve o id gerado pelo AUTO_INCREMENT. */
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

    /**
     * INSERT na subclasse, com o MESMO id da superclasse. O instanceof traduz
     * o tipo do objeto na tabela em que a linha filha é gravada.
     */
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
     * Remove o rascunho (FA-11C) do orçamento, se houver.
     *
     * TB_OrdemDeServico.idOrcamento é UNIQUE (RN-02) e o FA-11C.3 permite
     * retomar o registro, então o rascunho dá lugar à OS definitiva.
     *
     * Não fere a RN-06: a cláusula statusOS = 'Rascunho' garante que só o
     * rascunho - que não é uma OS registrada - pode ser removido.
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

        // A linha da subclasse sai primeiro: ela aponta para a da superclasse.
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

    /**
     * Caminho inverso do inserirSubclasse(): escolhe a subclasse conforme o
     * LEFT JOIN que trouxe linha.
     */
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

    /** Converte a java.util.Date do modelo na java.sql.Date do JDBC. */
    private java.sql.Date paraDataSql(Date data) {
        return data == null ? null : new java.sql.Date(data.getTime());
    }
}
