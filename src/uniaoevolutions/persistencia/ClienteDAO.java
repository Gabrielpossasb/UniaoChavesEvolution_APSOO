package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO de Cliente (TB_Cliente). */
public class ClienteDAO {

    /** Passo 2: pelo CPF/CNPJ (só dígitos); null = FA-02. */
    public Cliente buscarPorCpfCnpj(String cpfCnpj) throws SQLException {
        String sql = "SELECT idCliente, nome, cpfCnpj, telefone FROM TB_Cliente WHERE cpfCnpj = ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, cpfCnpj);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarCliente(resultado) : null;
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** FA-02.3; o objeto recebe o id gerado. */
    public void salvar(Cliente cli) throws SQLException {
        String sql = "INSERT INTO TB_Cliente (nome, cpfCnpj, telefone) VALUES (?, ?, ?)";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, cli.getNome());
            comando.setString(2, cli.getCpfCnpj());
            comando.setString(3, cli.getTelefone());
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco não devolveu o código do cliente gravado.");
                }
                cli.setIdCliente(chaves.getInt(1));
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** ACRÉSCIMO: busca por nome ou telefone (mockup). Telefone pelos dígitos; nome exato, depois trecho. */
    public List<Cliente> buscarPorNomeOuTelefone(String termo) throws SQLException {
        String digitos = termo.replaceAll("\\D", "");
        String sql = "SELECT idCliente, nome, cpfCnpj, telefone FROM TB_Cliente "
                   + " WHERE (? <> '' AND " + TELEFONE_SO_DIGITOS + " = ?) "
                   + "    OR LOWER(nome) = LOWER(?) "
                   + " ORDER BY nome";
        List<Cliente> clientes = consultar(sql, digitos, digitos, termo.trim());
        if (clientes.isEmpty()) {
            clientes = consultar("SELECT idCliente, nome, cpfCnpj, telefone FROM TB_Cliente "
                               + " WHERE LOWER(nome) LIKE LOWER(?) ORDER BY nome",
                                 "%" + termo.trim() + "%");
        }
        return clientes;
    }

    /** ACRÉSCIMO: lista da busca do mockup; trecho vazio lista todos, por nome. */
    public List<Cliente> buscarClientes(String trecho) throws SQLException {
        String texto = trecho == null ? "" : trecho.trim();
        String digitos = texto.replaceAll("\\D", "");
        String sql = "SELECT idCliente, nome, cpfCnpj, telefone FROM TB_Cliente "
                   + " WHERE LOWER(nome) LIKE LOWER(?) "
                   + "    OR (? <> '' AND (COALESCE(cpfCnpj, '') LIKE ? OR " + TELEFONE_SO_DIGITOS + " LIKE ?)) "
                   + " ORDER BY nome LIMIT 50";
        return consultar(sql, "%" + texto + "%", digitos, digitos + "%", "%" + digitos + "%");
    }

    /** Telefone só com dígitos (REPLACE; o MySQL 5.7 não tem REGEXP_REPLACE). */
    private static final String TELEFONE_SO_DIGITOS =
            "REPLACE(REPLACE(REPLACE(REPLACE(telefone, '(', ''), ')', ''), '-', ''), ' ', '')";

    private List<Cliente> consultar(String sql, String... parametros) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setString(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    clientes.add(montarCliente(resultado));
                }
            }
            return clientes;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    private Cliente montarCliente(ResultSet resultado) throws SQLException {
        Cliente cliente = new Cliente(
                resultado.getInt("idCliente"),
                resultado.getString("nome"),
                resultado.getString("telefone"));
        cliente.setCpfCnpj(resultado.getString("cpfCnpj"));
        return cliente;
    }
}
