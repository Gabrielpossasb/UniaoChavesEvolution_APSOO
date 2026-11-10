package uniaoevolutions;

import uniaoevolutions.persistencia.Conexao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Apoio dos testes: recria o banco com o sql/schema.sql e faz consultas de conferência. */
public final class BancoDeTeste {

    private BancoDeTeste() {
    }

    /** Executa o sql/schema.sql inteiro (o mesmo que se importa no phpMyAdmin). */
    public static void recriar() throws Exception {
        String script = Files.readString(Path.of("sql/schema.sql"), StandardCharsets.UTF_8);
        StringBuilder semComentarios = new StringBuilder();
        for (String linha : script.split("\n")) {
            if (!linha.trim().startsWith("--")) {
                semComentarios.append(linha).append('\n');
            }
        }
        try (Connection conexao = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?characterEncoding=UTF-8", "root", "");
             Statement comando = conexao.createStatement()) {
            // Instruções terminam com ";" no fim da linha (há ";" dentro de textos).
            for (String instrucao : (semComentarios + "\n").split(";\\s*\n")) {
                if (!instrucao.isBlank()) {
                    comando.execute(instrucao);
                }
            }
        }
    }

    /** Primeira linha do resultado, com as colunas separadas por " | ". */
    public static String linha(String sql, Object... parametros) throws SQLException {
        List<String> linhas = linhas(sql, parametros);
        return linhas.isEmpty() ? null : linhas.get(0);
    }

    public static List<String> linhas(String sql, Object... parametros) throws SQLException {
        List<String> linhas = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setObject(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                int colunas = resultado.getMetaData().getColumnCount();
                while (resultado.next()) {
                    StringBuilder texto = new StringBuilder();
                    for (int c = 1; c <= colunas; c++) {
                        texto.append(c > 1 ? " | " : "").append(resultado.getString(c));
                    }
                    linhas.add(texto.toString());
                }
            }
        } finally {
            Conexao.desconectar(conexao);
        }
        return linhas;
    }

    public static int contar(String sql, Object... parametros) throws SQLException {
        return Integer.parseInt(linha(sql, parametros));
    }
}
