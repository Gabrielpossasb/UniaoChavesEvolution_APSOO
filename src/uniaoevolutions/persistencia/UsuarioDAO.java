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

/** DAO de Usuario (TB_Usuario). */
public class UsuarioDAO {

    /** Mensagem 1.3; null se não existir. */
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

    /** RN-04: habilitados no segmento; inclui o Administrador (herda dos dois técnicos). */
    public List<Usuario> buscarTecnicosHabilitados(String segmento) throws SQLException {
        String tipoDoSegmento = Segmento.CHAVES.equals(segmento)
                ? TipoUsuario.tecnicoDeChaves.name()
                : TipoUsuario.tecnicoDeModulos.name();

        // Administrador por último.
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

    /** Usuario da linha atual. */
    private Usuario montarUsuario(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getInt("idUsuario"),
                resultado.getString("nome"),
                TipoUsuario.valueOf(resultado.getString("tipoUsuario")));
    }
}
