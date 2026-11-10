package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.ModuloECU;
import uniaoevolutions.negocio.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da classe ModuloECU - tabela TB_ModuloECU.
 *
 * Operação do Diagrama de Classes v2: buscarModulo(vei, identificacaoModulo).
 * A gravação de um módulo novo acontece dentro da transação de
 * OrcamentoDAO.salvar, porque o diagrama não dá a esta DAO uma operação de
 * salvar e o módulo só pode nascer junto com o orçamento (mensagem 1.8).
 */
public class ModuloECUDAO {

    /**
     * Mensagem 1.7 e passo 8.2: o módulo do veículo com a identificação
     * informada, ou null (mensagem 1.8 cria). Sem identificação (FAV-8.1 ou
     * campo opcional em branco) não há como reconhecer o módulo: null.
     */
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

    /**
     * ACRÉSCIMO à modelagem: os módulos já registrados do veículo
     * ("Veiculo 1 possui 0..* ModuloECU"), oferecidos para o Técnico
     * selecionar em vez de digitar.
     */
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
