package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.Cliente;
import uniaoevolutions.negocio.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da classe Veiculo - tabela TB_Veiculo.
 *
 * Operações do Diagrama de Classes v2: buscarPorPlaca(placa) e salvar(vei).
 */
public class VeiculoDAO {

    private static final String SELECT_VEICULO =
            "SELECT v.idVeiculo, v.marca, v.modelo, v.anoModelo, v.motorizacao, v.placa, v.numeroChassi "
          + "  FROM TB_Veiculo v ";

    /**
     * Passo 4: veículo pela placa, ou null. A comparação ignora o hífen, para
     * "QAF7C21" e "QAF-7C21" acharem o mesmo veículo.
     */
    public Veiculo buscarPorPlaca(String placa) throws SQLException {
        String sql = SELECT_VEICULO + " WHERE REPLACE(v.placa, '-', '') = REPLACE(?, '-', '')";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, placa.toUpperCase());
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarVeiculo(resultado) : null;
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** FA-04.3: grava o veículo e devolve no objeto o id gerado. */
    public void salvar(Veiculo vei) throws SQLException {
        String sql = "INSERT INTO TB_Veiculo (marca, modelo, anoModelo, motorizacao, placa, numeroChassi) "
                   + " VALUES (?, ?, ?, ?, ?, ?)";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, vei.getMarca());
            comando.setString(2, vei.getModelo());
            comando.setInt(3, vei.getAnoModelo());
            comando.setString(4, vei.getMotorizacao());
            comando.setString(5, vei.getPlaca());
            comando.setString(6, vei.getNumeroChassi());
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco não devolveu o código do veículo gravado.");
                }
                vei.setIdVeiculo(chaves.getInt(1));
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /**
     * ACRÉSCIMO à modelagem: passo 3, "a relação de veículos a ele
     * vinculados". No Diagrama de Classes v2 não há associação direta entre
     * Cliente e Veiculo, nem FK de cliente em TB_Veiculo: o vínculo (RN-02) é
     * formado pelos orçamentos. Por isso a relação são os veículos dos
     * orçamentos do cliente.
     */
    public List<Veiculo> buscarPorCliente(Cliente cli) throws SQLException {
        String sql = SELECT_VEICULO
                   + " WHERE v.idVeiculo IN (SELECT o.idVeiculo FROM TB_Orcamento o WHERE o.idCliente = ?) "
                   + " ORDER BY v.placa";
        List<Veiculo> veiculos = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, cli.getIdCliente());
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    veiculos.add(montarVeiculo(resultado));
                }
            }
            return veiculos;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    private Veiculo montarVeiculo(ResultSet resultado) throws SQLException {
        Veiculo veiculo = new Veiculo(
                resultado.getString("placa"),
                resultado.getString("marca"),
                resultado.getString("modelo"),
                resultado.getInt("anoModelo"),
                resultado.getString("motorizacao"),
                resultado.getString("numeroChassi"));
        veiculo.setIdVeiculo(resultado.getInt("idVeiculo"));
        return veiculo;
    }
}
