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
 * DAO da hierarquia OrdemDeServico - tabelas TB_OrdemDeServico,
 * TB_OrdemDeServicoModulos e TB_OrdemDeServicoChaves.
 *
 * Diagrama de Classes:
 *   + salvar(os : OrdemDeServico) : void
 *   + gerarNumeroOS() : String
 *
 * ESTA CLASSE É ONDE A HERANÇA VIRA TABELA.
 * O Mapeamento Objeto-Relacional escolheu a estratégia "uma tabela por
 * classe": a superclasse tem TB_OrdemDeServico e cada subclasse tem a sua
 * tabela, com a chave primária que é ao mesmo tempo chave estrangeira para a
 * tabela da superclasse.
 *
 *     OrdemDeServico (abstrata)        -> TB_OrdemDeServico
 *       |_ OrdemDeServicoModulos       -> TB_OrdemDeServicoModulos (PK = FK)
 *       |_ OrdemDeServicoChaves        -> TB_OrdemDeServicoChaves  (PK = FK)
 *
 * Por isso gravar uma OS é sempre DOIS INSERTs: um na tabela da superclasse,
 * que gera o id, e outro na tabela da subclasse, reusando esse mesmo id. Os
 * dois INSERTs são feitos dentro de uma transação: ou as duas linhas existem,
 * ou nenhuma existe.
 */
public class OrdemServicoDAO {

    /**
     * Mensagem 1.11 do Diagrama de Comunicação: salvar(os).
     * Última pós-condição do Contrato de Operação: "O objeto os foi persistido".
     *
     * Depois de gravar, o objeto recebe de volta o id gerado pelo banco.
     */
    public void salvar(OrdemDeServico os) throws SQLException {
        Connection conexao = Conexao.conectar();
        try {
            // Transação: a linha da superclasse e a da subclasse têm de nascer juntas.
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
     * RN-07 - numeração: devolve o próximo número no formato OS-AAAA-NNNN,
     * "com sequência reiniciada a cada ano".
     *
     * O reinício anual sai do próprio LIKE: a busca só olha os números do ano
     * corrente, então em 1º de janeiro não há nenhum e a sequência recomeça
     * em 0001.
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
     * Ordens de Serviço anteriores do mesmo veículo, da mais recente para a
     * mais antiga.
     *
     * ACRÉSCIMO à modelagem: não está no Diagrama de Classes, mas é exigido
     * pelo passo 8.2 da variante V1 ("o sistema exibe os atendimentos
     * anteriores ... registrados no histórico técnico do veículo") e pelo
     * botão "Ver 3 atendimentos anteriores" do mockup. Também é o que torna
     * verificável a POS-05.
     *
     * Devolve objetos de consulta: as colunas da própria OS e as da subclasse
     * estão preenchidas; as associações (orçamento, veículo, técnico) não,
     * porque esta consulta é só para exibição do histórico.
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

    /** INSERT na tabela da superclasse. Devolve o id gerado pelo AUTO_INCREMENT. */
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
     * INSERT na tabela da subclasse, com o MESMO id da superclasse.
     *
     * O "instanceof" é o ponto em que o mundo OO e o mundo relacional se
     * encontram: em Java a diferença entre as duas subclasses é o tipo do
     * objeto; no banco ela é a tabela em que a linha filha é gravada.
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
     * Remove o rascunho (FA-11C) existente para o orçamento, se houver.
     *
     * Por que isto existe: TB_OrdemDeServico.idOrcamento é UNIQUE (RN-02), e o
     * FA-11C.3 diz que "o registro pode ser retomado posteriormente". Quando o
     * registro é retomado e concluído, o rascunho dá lugar à OS definitiva.
     *
     * Isto NÃO fere a RN-06 (irretratabilidade): o rascunho é salvo "sem
     * número definitivo" e não é uma Ordem de Serviço registrada - a cláusula
     * statusOS = 'Rascunho' garante que só ele pode ser removido.
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
     * Monta a OS do histórico, escolhendo a subclasse pelo LEFT JOIN que
     * trouxe linha: é o caminho inverso do inserirSubclasse().
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

    /** Converte a java.util.Date do modelo na java.sql.Date que o JDBC grava. */
    private java.sql.Date paraDataSql(Date data) {
        return data == null ? null : new java.sql.Date(data.getTime());
    }
}
