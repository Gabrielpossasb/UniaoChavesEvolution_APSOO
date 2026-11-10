package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da classe Cliente - tabela TB_Cliente.
 *
 * Operações do Diagrama de Classes v2: buscarPorCpfCnpj(cpfCnpj) e
 * salvar(cli). Nenhum método exibe mensagem: a SQLException sobe.
 */
public class ClienteDAO {

    /** Passo 2: cliente pelo CPF/CNPJ (só dígitos), ou null se não houver (FA-02). */
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

    /** FA-02.3: grava o cliente e devolve no objeto o id gerado. */
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

    /**
     * ACRÉSCIMO à modelagem: o mockup diz "Busque por nome, CPF/CNPJ ou
     * telefone", e o cliente do cadastro rápido (FA-02) não tem CPF/CNPJ.
     * Telefone é comparado só pelos dígitos; nome, sem diferenciar
     * maiúsculas, primeiro exato e depois por trecho.
     */
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

    /**
     * ACRÉSCIMO à modelagem: a lista de clientes da busca do mockup ("Busque
     * por nome, CPF/CNPJ ou telefone"), para o Técnico só selecionar. Trecho
     * vazio lista todos, em ordem de nome.
     */
    public List<Cliente> buscarClientes(String trecho) throws SQLException {
        String texto = trecho == null ? "" : trecho.trim();
        String digitos = texto.replaceAll("\\D", "");
        String sql = "SELECT idCliente, nome, cpfCnpj, telefone FROM TB_Cliente "
                   + " WHERE LOWER(nome) LIKE LOWER(?) "
                   + "    OR (? <> '' AND (COALESCE(cpfCnpj, '') LIKE ? OR " + TELEFONE_SO_DIGITOS + " LIKE ?)) "
                   + " ORDER BY nome LIMIT 50";
        return consultar(sql, "%" + texto + "%", digitos, digitos + "%", "%" + digitos + "%");
    }

    /** Telefone só com dígitos, com REPLACE (REGEXP_REPLACE não existe no MySQL 5.7). */
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
