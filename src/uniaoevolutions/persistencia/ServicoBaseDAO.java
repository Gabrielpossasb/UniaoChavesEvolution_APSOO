package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.ServicoBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO de ServicoBase (TB_ServicoBase). */
public class ServicoBaseDAO {

    /** Passo 7 (RN-03). Exclui os itens avulsos do FA-11B do Registrar OS ("Justificativa%"). */
    public List<ServicoBase> buscarPorCategoria(String categoria) throws SQLException {
        String sql = "SELECT idServicoBase, descricao, categoria, observacoesTecnicas FROM TB_ServicoBase "
                   + " WHERE categoria = ? "
                   + "   AND (observacoesTecnicas IS NULL OR observacoesTecnicas NOT LIKE 'Justificativa%') "
                   + " ORDER BY idServicoBase";
        List<ServicoBase> servicos = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, categoria);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    servicos.add(montarServico(resultado));
                }
            }
            return servicos;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** Passo 9; null se não existir. */
    public ServicoBase buscarServico(int id) throws SQLException {
        String sql = "SELECT idServicoBase, descricao, categoria, observacoesTecnicas FROM TB_ServicoBase "
                   + " WHERE idServicoBase = ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarServico(resultado) : null;
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    private ServicoBase montarServico(ResultSet resultado) throws SQLException {
        return new ServicoBase(
                resultado.getInt("idServicoBase"),
                resultado.getString("descricao"),
                resultado.getString("categoria"),
                resultado.getString("observacoesTecnicas"));
    }
}
