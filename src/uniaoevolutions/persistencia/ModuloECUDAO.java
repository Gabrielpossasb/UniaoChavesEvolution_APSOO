package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.ModuloECU;
import uniaoevolutions.negocio.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO de ModuloECU (TB_ModuloECU). O módulo novo é gravado na transação de OrcamentoDAO.salvar. */
public class ModuloECUDAO {

    /** Mensagem 1.7: módulo do veículo com a identificação; null se não houver ou sem identificação. */
    public ModuloECU buscarModulo(Veiculo vei, String identificacaoModulo) throws SQLException {
        if (vei == null || identificacaoModulo == null || identificacaoModulo.isBlank()) {
            return null;
        }
        String sql = "SELECT idModuloECU, tipoModulo, modeloECU, identificacaoModulo FROM TB_ModuloECU "
                   + " WHERE idVeiculo = ? AND UPPER(identificacaoModulo) = UPPER(?) "
                   + " ORDER BY idModuloECU LIMIT 1";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, vei.getIdVeiculo());
            comando.setString(2, identificacaoModulo.trim());
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                ModuloECU mod = new ModuloECU(
                        resultado.getString("tipoModulo"),
                        resultado.getString("modeloECU"),
                        resultado.getString("identificacaoModulo"));
                mod.setIdModuloECU(resultado.getInt("idModuloECU"));
                return mod;
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** ACRÉSCIMO: módulos já registrados do veículo, para seleção. */
    public List<ModuloECU> buscarModulosDoVeiculo(Veiculo vei) throws SQLException {
        List<ModuloECU> modulos = new ArrayList<>();
        if (vei == null || vei.getIdVeiculo() == 0) {
            return modulos;
        }
        String sql = "SELECT idModuloECU, tipoModulo, modeloECU, identificacaoModulo FROM TB_ModuloECU "
                   + " WHERE idVeiculo = ? AND identificacaoModulo IS NOT NULL ORDER BY idModuloECU";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, vei.getIdVeiculo());
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ModuloECU mod = new ModuloECU(resultado.getString("tipoModulo"),
                            resultado.getString("modeloECU"), resultado.getString("identificacaoModulo"));
                    mod.setIdModuloECU(resultado.getInt("idModuloECU"));
                    modulos.add(mod);
                }
            }
            return modulos;
        } finally {
            Conexao.desconectar(conexao);
        }
    }
}
