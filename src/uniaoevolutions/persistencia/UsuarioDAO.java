package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.Segmento;
import uniaoevolutions.negocio.TipoUsuario;
import uniaoevolutions.negocio.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da classe Usuario - tabela TB_Usuario.
 *
 * Nenhum método exibe mensagem: a SQLException sobe para a controladora.
 */
public class UsuarioDAO {

    /** Mensagem 1.3: buscarTecnico(). Devolve null se o código não existir. */
    public Usuario buscarUsuario(int id) throws SQLException {
        String sql = "SELECT idUsuario, nome, tipoUsuario FROM TB_Usuario WHERE idUsuario = ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarUsuario(resultado) : null;
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /**
     * RN-04: técnicos habilitados no segmento, para a combo "Técnico
     * responsável" do mockup.
     *
     * O Administrador entra na lista porque "herda de ambos os técnicos"
     * (Descrição Expandida), podendo assumir esses papéis.
     */
    public List<Usuario> buscarTecnicosHabilitados(String segmento) throws SQLException {
        String tipoDoSegmento = Segmento.CHAVES.equals(segmento)
                ? TipoUsuario.tecnicoDeChaves.name()
                : TipoUsuario.tecnicoDeModulos.name();

        // O CASE no ORDER BY deixa o administrador por último: ele é
        // habilitado, mas é a alternativa, não o responsável natural.
        String sql = "SELECT idUsuario, nome, tipoUsuario FROM TB_Usuario "
                   + " WHERE tipoUsuario = ? OR tipoUsuario = ? "
                   + " ORDER BY CASE WHEN tipoUsuario = ? THEN 1 ELSE 0 END, nome";

        List<Usuario> tecnicos = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, tipoDoSegmento);
            comando.setString(2, TipoUsuario.administrador.name());
            comando.setString(3, TipoUsuario.administrador.name());
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    tecnicos.add(montarUsuario(resultado));
                }
            }
            return tecnicos;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** Monta um Usuario com a linha atual do ResultSet. */
    private Usuario montarUsuario(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getInt("idUsuario"),
                resultado.getString("nome"),
                TipoUsuario.valueOf(resultado.getString("tipoUsuario")));
    }
}
