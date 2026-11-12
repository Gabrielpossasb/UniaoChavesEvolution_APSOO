package uniaoevolutions.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexão JDBC com o MySQL. RNF-4: em produção, "localhost" vira o IP do servidor da oficina. */
public class Conexao {

    private static final String URL = "jdbc:mysql://localhost:3306/uniao_evolutions";
    private static final String USUARIO = "root";
    private static final String SENHA = "";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /** Fecha a conexão, se houver. */
    public static void desconectar(Connection conexao) throws SQLException {
        if (conexao != null) {
            conexao.close();
        }
    }
}
