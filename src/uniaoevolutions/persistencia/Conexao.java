package uniaoevolutions.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre e fecha a conexão com o banco MySQL do sistema (API JDBC).
 *
 * É a única classe que conhece onde o banco está. As DAOs pedem a conexão a
 * ela e devolvem no fim; nenhuma outra camada sabe que existe um MySQL.
 *
 * RNF-4 (o sistema opera na rede local, sem nuvem): em produção, "localhost"
 * vira o IP do servidor da oficina.
 */
public class Conexao {

    private static final String URL = "jdbc:mysql://localhost:3306/uniao_evolutions";
    private static final String USUARIO = "root";
    private static final String SENHA = "";

    /** Abre uma conexão com o banco. */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /** Fecha a conexão recebida, se ela existir. */
    public static void desconectar(Connection conexao) throws SQLException {
        if (conexao != null) {
            conexao.close();
        }
    }
}
