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
 * Diagrama de Classes:
 *   + buscarUsuario(id : int) : Usuario
 *   + buscarTecnicosHabilitados(segmento : String) : List<Usuario>
 *
 * Nenhum método desta classe exibe mensagem: se der erro, a SQLException sobe
 * para a controladora, que decide o que fazer.
 */
public class UsuarioDAO {

    /**
     * Mensagem 1.3 do Diagrama de Comunicação: tec = buscarTecnico(tecnicoSelecionado).
     * Devolve null se não existir usuário com esse código.
     */
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
     * RN-04 - lista os técnicos habilitados no segmento informado, para a combo
     * "Técnico responsável" do mockup ("Somente técnicos habilitados no
     * segmento selecionado").
     *
     * O Administrador entra na lista porque, segundo a Descrição Expandida,
     * "o ator administrador herda de ambos os técnicos", então ele também pode
     * assumir esses papéis.
     */
    public List<Usuario> buscarTecnicosHabilitados(String segmento) throws SQLException {
        String tipoDoSegmento = Segmento.CHAVES.equals(segmento)
                ? TipoUsuario.tecnicoDeChaves.name()
                : TipoUsuario.tecnicoDeModulos.name();

        // O CASE no ORDER BY põe os técnicos do segmento à frente e o
        // administrador por último: ele é habilitado, mas é a alternativa,
        // não o responsável natural pelo serviço.
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

    /** Cria um Usuario com os valores da linha atual do ResultSet. */
    private Usuario montarUsuario(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getInt("idUsuario"),
                resultado.getString("nome"),
                TipoUsuario.valueOf(resultado.getString("tipoUsuario")));
    }
}
