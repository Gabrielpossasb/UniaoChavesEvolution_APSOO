package uniaoevolutions.persistencia;

import uniaoevolutions.negocio.Cliente;
import uniaoevolutions.negocio.InformacoesSegurancaVeiculo;
import uniaoevolutions.negocio.ItemOrcamento;
import uniaoevolutions.negocio.ModuloECU;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.OrcamentoChaves;
import uniaoevolutions.negocio.OrcamentoModulos;
import uniaoevolutions.negocio.ServicoBase;
import uniaoevolutions.negocio.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DAO da hierarquia Orcamento (TB_Orcamento, TB_OrcamentoModulos, TB_OrcamentoChaves,
 * TB_ItemOrcamento). Herança em "uma tabela por subclasse" (PK da subclasse = FK):
 *
 *     Orcamento -> TB_Orcamento
 *       |_ OrcamentoModulos -> TB_OrcamentoModulos
 *       |_ OrcamentoChaves  -> TB_OrcamentoChaves
 */
public class OrcamentoDAO {

    /**
     * Colunas do orçamento, cliente e veículo. O segmento vem da subclasse com linha;
     * módulo e senhas vêm de TB_ModuloECU e TB_InformacoesSegurancaVeiculo.
     */
    private static final String SELECT_ORCAMENTO =
            "SELECT o.idOrcamento, o.numero, o.dataSolicitacao, o.dataValidade, o.origemAtendimento, "
          + "       o.observacoes, o.valorEstimado, o.status, "
          + "       CASE WHEN oc.idOrcamento IS NOT NULL THEN 'Chaves' ELSE 'Módulos' END AS segmento, "
          + "       c.idCliente, c.nome AS nomeCliente, c.cpfCnpj, c.telefone, "
          + "       v.idVeiculo, v.marca, v.modelo, v.anoModelo, v.motorizacao, v.placa, "
          + "       v.numeroChassi, m.tipoModulo, m.modeloECU, m.identificacaoModulo, "
          + "       s.idVeiculo AS idSeguranca, s.codigoMecanico, s.senhaImobilizador, s.senhaAlarme, s.senhaRadio "
          + "  FROM TB_Orcamento o "
          + "  JOIN TB_Cliente  c ON c.idCliente = o.idCliente "
          + "  JOIN TB_Veiculo  v ON v.idVeiculo = o.idVeiculo "
          + "  LEFT JOIN TB_OrcamentoModulos om ON om.idOrcamento = o.idOrcamento "
          + "  LEFT JOIN TB_OrcamentoChaves  oc ON oc.idOrcamento = o.idOrcamento "
          + "  LEFT JOIN TB_ModuloECU         m ON m.idModuloECU = om.idModuloECU "
          + "  LEFT JOIN TB_InformacoesSegurancaVeiculo s ON s.idVeiculo = v.idVeiculo ";

    // =================================================================
    // 1ª iteração - Registrar Ordem de Serviço
    // =================================================================

    /** Mensagem 1.1 do Registrar OS: orçamento com cliente, veículo e itens, ou null. */
    public Orcamento buscarOrcamento(int id) throws SQLException {
        String sql = SELECT_ORCAMENTO + " WHERE o.idOrcamento = ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                Orcamento orcamento = montarOrcamento(resultado);
                carregarItens(conexao, orcamento);
                return orcamento;
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** Passo 2 do Registrar OS (RN-01): aprovados e não convertidos. */
    public List<Orcamento> buscarOrcamentosAprovados() throws SQLException {
        String sql = SELECT_ORCAMENTO
                   + " WHERE o.status = ? "
                   + "   AND NOT EXISTS (SELECT 1 FROM TB_OrdemDeServico os "
                   + "                    WHERE os.idOrcamento = o.idOrcamento "
                   + "                      AND os.statusOS <> 'Rascunho') "
                   + " ORDER BY o.idOrcamento";
        return buscarLista(sql, Orcamento.STATUS_APROVADO);
    }

    // =================================================================
    // 2ª iteração - Solicitar Orçamento
    // =================================================================

    /** Passo 1 e mensagem 1.2: próximo ORC-AAAA-NNNN; a sequência reinicia a cada ano (RN-08). */
    public String gerarNumeroOrcamento() throws SQLException {
        int ano = Calendar.getInstance().get(Calendar.YEAR);
        String prefixo = String.format("ORC-%d-", ano);

        String sql = "SELECT MAX(numero) AS ultimo FROM TB_Orcamento WHERE numero LIKE ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, prefixo + "%");
            try (ResultSet resultado = comando.executeQuery()) {
                int sequencia = 1;
                if (resultado.next() && resultado.getString("ultimo") != null) {
                    String ultimo = resultado.getString("ultimo");
                    sequencia = Integer.parseInt(ultimo.substring(prefixo.length())) + 1;
                }
                return String.format("%s%04d", prefixo, sequencia);
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** Pós-condição do contrato: grava, numa transação, módulo novo (1.8), superclasse, subclasse e itens. */
    public void salvar(Orcamento orc) throws SQLException {
        gravar(orc);
    }

    /** FA-12D: grava o rascunho. */
    public void salvarRascunho(Orcamento orc) throws SQLException {
        gravar(orc);
    }

    /** Passo 8.2 (V1): orçamentos anteriores do mesmo módulo, sem rascunhos. */
    public List<Orcamento> buscarAtendimentosModulo(ModuloECU mod) throws SQLException {
        List<Orcamento> atendimentos = new ArrayList<>();
        if (mod == null || mod.getIdModuloECU() == 0) {
            return atendimentos;   // módulo novo: sem histórico
        }
        String sql = SELECT_ORCAMENTO
                   + " WHERE om.idModuloECU = ? AND o.status <> ? "
                   + " ORDER BY o.dataSolicitacao DESC, o.idOrcamento DESC";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, mod.getIdModuloECU());
            comando.setString(2, Orcamento.STATUS_RASCUNHO);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    atendimentos.add(montarOrcamento(resultado));
                }
            }
            return atendimentos;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** FA-00.4 e FA-12D: do mais recente ao mais antigo. */
    public List<Orcamento> listarOrcamentos() throws SQLException {
        String sql = SELECT_ORCAMENTO + " ORDER BY o.idOrcamento DESC";
        return buscarLista(sql, null);
    }

    /** ACRÉSCIMO (1ª iteração; POS-02 e RN-05 do Registrar OS). */
    public void atualizarStatus(int idOrcamento, String status) throws SQLException {
        String sql = "UPDATE TB_Orcamento SET status = ? WHERE idOrcamento = ?";
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, status);
            comando.setInt(2, idOrcamento);
            comando.executeUpdate();
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** ACRÉSCIMO (1ª iteração; FA-11B do Registrar OS): inclui o item e soma ao valorEstimado. */
    public void incluirItem(int idOrcamento, String descricao, String categoria,
                            int quantidade, double valor, String justificativa) throws SQLException {
        Connection conexao = Conexao.conectar();
        try {
            conexao.setAutoCommit(false);
            try {
                int idServicoBase = inserirServicoBase(conexao, descricao, categoria, justificativa);
                inserirItem(conexao, idOrcamento, idServicoBase, quantidade, valor);
                somarAoValorEstimado(conexao, idOrcamento, quantidade * valor);
                conexao.commit();
            } catch (SQLException erro) {
                conexao.rollback();
                throw erro;
            } finally {
                conexao.setAutoCommit(true);
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    // -----------------------------------------------------------------
    // Gravação da hierarquia (2ª iteração)
    // -----------------------------------------------------------------

    /** Grava tudo ou nada (FA-13). */
    private void gravar(Orcamento orc) throws SQLException {
        Connection conexao = Conexao.conectar();
        boolean moduloNovo = false;
        try {
            conexao.setAutoCommit(false);
            try {
                if (orc instanceof OrcamentoModulos) {
                    moduloNovo = inserirModuloSeNovo(conexao, (OrcamentoModulos) orc);
                }
                int id = inserirSuperclasse(conexao, orc);
                orc.setIdOrcamento(id);
                inserirSubclasse(conexao, orc, id);
                // Os itens mantêm o id da sessão.
                for (ItemOrcamento item : orc.getItens()) {
                    inserirItem(conexao, id, item.getServicoBase().getIdServicoBase(),
                            item.getQuantidade(), item.getValorProposto());
                }
                conexao.commit();
            } catch (SQLException erro) {
                conexao.rollback();
                // FA-13: restaura o objeto para a nova tentativa gravar o módulo.
                orc.setIdOrcamento(0);
                if (moduloNovo) {
                    ((OrcamentoModulos) orc).getModulo().setIdModuloECU(0);
                }
                throw erro;
            } finally {
                conexao.setAutoCommit(true);
            }
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** Módulo novo (1.8, id 0): gravado antes do orçamento, que aponta para ele. */
    private boolean inserirModuloSeNovo(Connection conexao, OrcamentoModulos orc) throws SQLException {
        ModuloECU mod = orc.getModulo();
        if (mod == null || mod.getIdModuloECU() != 0) {
            return false;
        }
        String sql = "INSERT INTO TB_ModuloECU (tipoModulo, modeloECU, identificacaoModulo, idVeiculo) "
                   + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, mod.getTipoModulo());
            comando.setString(2, mod.getModeloECU());
            comando.setString(3, mod.getIdentificacaoModulo());
            comando.setInt(4, orc.getVeiculo().getIdVeiculo());
            comando.executeUpdate();
            mod.setIdModuloECU(chaveGerada(comando, "do módulo"));
            return true;
        }
    }

    private int inserirSuperclasse(Connection conexao, Orcamento orc) throws SQLException {
        String sql = "INSERT INTO TB_Orcamento (numero, dataSolicitacao, dataValidade, origemAtendimento, "
                   + "  status, valorEstimado, observacoes, idCliente, idVeiculo, idUsuario) "
                   + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, orc.getNumero());
            comando.setDate(2, paraDataSql(orc.getDataSolicitacao()));
            comando.setDate(3, paraDataSql(orc.getDataValidade()));
            comando.setString(4, orc.getOrigemAtendimento());
            comando.setString(5, orc.getStatus());
            comando.setDouble(6, orc.getValorEstimado());
            comando.setString(7, orc.getObservacoes());
            comando.setInt(8, orc.getCliente().getIdCliente());
            comando.setInt(9, orc.getVeiculo().getIdVeiculo());
            comando.setInt(10, orc.getTecnico().getIdUsuario());
            comando.executeUpdate();
            return chaveGerada(comando, "do orçamento");
        }
    }

    /** INSERT na tabela da subclasse, com o id da superclasse. */
    private void inserirSubclasse(Connection conexao, Orcamento orc, int id) throws SQLException {
        if (orc instanceof OrcamentoModulos) {
            OrcamentoModulos m = (OrcamentoModulos) orc;
            String sql = "INSERT INTO TB_OrcamentoModulos (idOrcamento, sintomaRelatado, "
                       + "  diagnosticoPendenteBancada, identificacaoIlegivel, registroFotografico, "
                       + "  descricaoFisicaPlaca, idModuloECU) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setInt(1, id);
                comando.setString(2, m.getSintomaRelatado());
                comando.setBoolean(3, m.isDiagnosticoPendenteBancada());
                comando.setBoolean(4, m.isIdentificacaoIlegivel());
                comando.setString(5, m.getRegistroFotografico());
                comando.setString(6, m.getDescricaoFisicaPlaca());
                if (m.getModulo() == null) {
                    comando.setNull(7, java.sql.Types.INTEGER);
                } else {
                    comando.setInt(7, m.getModulo().getIdModuloECU());
                }
                comando.executeUpdate();
            }
        } else if (orc instanceof OrcamentoChaves) {
            OrcamentoChaves c = (OrcamentoChaves) orc;
            String sql = "INSERT INTO TB_OrcamentoChaves (idOrcamento, tipoChave, quantidadeCopias, "
                       + "  possuiTelecomando, codigoTransponder, situacaoImobilizador, perdaTotal, "
                       + "  documentoPropriedade, nomeSolicitante, documentoSolicitante) "
                       + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setInt(1, id);
                comando.setString(2, c.getTipoChave());
                comando.setInt(3, c.getQuantidadeCopias());
                comando.setBoolean(4, c.isPossuiTelecomando());
                comando.setString(5, c.getCodigoTransponder());
                comando.setString(6, c.getSituacaoImobilizador());
                comando.setBoolean(7, c.isPerdaTotal());
                comando.setString(8, c.getDocumentoPropriedade());
                comando.setString(9, c.getNomeSolicitante());
                comando.setString(10, c.getDocumentoSolicitante());
                comando.executeUpdate();
            }
        } else {
            throw new SQLException("Todo orçamento registrado é de Módulos ou de Chaves.");
        }
    }

    // -----------------------------------------------------------------
    // Métodos privados (1ª iteração)
    // -----------------------------------------------------------------

    private int inserirServicoBase(Connection conexao, String descricao, String categoria,
                                   String justificativa) throws SQLException {
        String sql = "INSERT INTO TB_ServicoBase (descricao, categoria, observacoesTecnicas) "
                   + " VALUES (?, ?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, descricao);
            comando.setString(2, categoria);
            comando.setString(3, "Justificativa (RN-05): " + justificativa);
            comando.executeUpdate();
            return chaveGerada(comando, "do serviço incluído");
        }
    }

    private int inserirItem(Connection conexao, int idOrcamento, int idServicoBase,
                            int quantidade, double valor) throws SQLException {
        String sql = "INSERT INTO TB_ItemOrcamento (valorProposto, quantidade, idOrcamento, idServicoBase) "
                   + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setDouble(1, valor);
            comando.setInt(2, quantidade);
            comando.setInt(3, idOrcamento);
            comando.setInt(4, idServicoBase);
            comando.executeUpdate();
            return chaveGerada(comando, "do item");
        }
    }

    private void somarAoValorEstimado(Connection conexao, int idOrcamento, double acrescimo)
            throws SQLException {
        String sql = "UPDATE TB_Orcamento SET valorEstimado = valorEstimado + ? WHERE idOrcamento = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setDouble(1, acrescimo);
            comando.setInt(2, idOrcamento);
            comando.executeUpdate();
        }
    }

    // -----------------------------------------------------------------
    // Montagem dos objetos
    // -----------------------------------------------------------------

    /** Executa uma consulta de lista, com um parâmetro de texto opcional. */
    private List<Orcamento> buscarLista(String sql, String parametro) throws SQLException {
        List<Orcamento> orcamentos = new ArrayList<>();
        Connection conexao = Conexao.conectar();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            if (parametro != null) {
                comando.setString(1, parametro);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    orcamentos.add(montarOrcamento(resultado));
                }
            }
            for (Orcamento orcamento : orcamentos) {
                carregarItens(conexao, orcamento);
            }
            return orcamentos;
        } finally {
            Conexao.desconectar(conexao);
        }
    }

    /** Itens do orçamento (composição 1..*) com o serviço de cada um. */
    private void carregarItens(Connection conexao, Orcamento orcamento) throws SQLException {
        String sql = "SELECT i.idItemOrcamento, i.valorProposto, i.quantidade, "
                   + "       s.idServicoBase, s.descricao, s.categoria, s.observacoesTecnicas "
                   + "  FROM TB_ItemOrcamento i "
                   + "  JOIN TB_ServicoBase  s ON s.idServicoBase = i.idServicoBase "
                   + " WHERE i.idOrcamento = ? "
                   + " ORDER BY i.idItemOrcamento";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, orcamento.getIdOrcamento());
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ServicoBase servico = new ServicoBase(
                            resultado.getInt("idServicoBase"),
                            resultado.getString("descricao"),
                            resultado.getString("categoria"),
                            resultado.getString("observacoesTecnicas"));

                    orcamento.adicionarItem(new ItemOrcamento(
                            resultado.getInt("idItemOrcamento"),
                            resultado.getDouble("valorProposto"),
                            resultado.getInt("quantidade"),
                            servico));
                }
            }
        }
    }

    /** Monta Orcamento (simples, com segmentoPretendido), Cliente e Veiculo da linha do JOIN. */
    private Orcamento montarOrcamento(ResultSet resultado) throws SQLException {
        Orcamento orcamento = new Orcamento();
        orcamento.setIdOrcamento(resultado.getInt("idOrcamento"));
        orcamento.setNumero(resultado.getString("numero"));
        orcamento.restaurarEstadoPersistido(resultado.getDate("dataSolicitacao"),
                resultado.getString("status"));
        orcamento.setDataValidade(resultado.getDate("dataValidade"));
        orcamento.setOrigemAtendimento(resultado.getString("origemAtendimento"));
        orcamento.setObservacoes(resultado.getString("observacoes"));
        orcamento.setSegmentoPretendido(resultado.getString("segmento"));
        orcamento.setValorEstimado(resultado.getDouble("valorEstimado"));

        Cliente cliente = new Cliente(
                resultado.getInt("idCliente"),
                resultado.getString("nomeCliente"),
                resultado.getString("telefone"));
        cliente.setCpfCnpj(resultado.getString("cpfCnpj"));
        orcamento.associarCliente(cliente);

        orcamento.associarVeiculo(montarVeiculo(resultado));
        return orcamento;
    }

    /** Monta o Veiculo, com os dados de módulo e segurança para o Registrar OS. */
    private Veiculo montarVeiculo(ResultSet resultado) throws SQLException {
        Veiculo veiculo = new Veiculo();
        veiculo.setIdVeiculo(resultado.getInt("idVeiculo"));
        veiculo.setMarca(resultado.getString("marca"));
        veiculo.setModelo(resultado.getString("modelo"));
        veiculo.setAnoModelo(resultado.getInt("anoModelo"));
        veiculo.setMotorizacao(resultado.getString("motorizacao"));
        veiculo.setPlaca(resultado.getString("placa"));
        veiculo.setNumeroChassi(resultado.getString("numeroChassi"));
        veiculo.setTipoModulo(resultado.getString("tipoModulo"));
        veiculo.setModeloECU(resultado.getString("modeloECU"));
        veiculo.setIdentificacaoModulo(resultado.getString("identificacaoModulo"));
        veiculo.setCodigoMecanico(resultado.getString("codigoMecanico"));
        veiculo.setSenhaImobilizador(resultado.getString("senhaImobilizador"));
        veiculo.setSenhaAlarme(resultado.getString("senhaAlarme"));
        veiculo.setSenhaRadio(resultado.getString("senhaRadio"));
        // possui 0..1 InformacoesSegurancaVeiculo.
        if (resultado.getObject("idSeguranca") != null) {
            InformacoesSegurancaVeiculo seguranca = new InformacoesSegurancaVeiculo();
            seguranca.setCodigoMecanico(resultado.getString("codigoMecanico"));
            seguranca.setSenhaImobilizador(resultado.getString("senhaImobilizador"));
            seguranca.setSenhaAlarme(resultado.getString("senhaAlarme"));
            seguranca.setSenhaRadio(resultado.getString("senhaRadio"));
            veiculo.setInformacoesSeguranca(seguranca);
        }
        return veiculo;
    }

    private int chaveGerada(PreparedStatement comando, String doQue) throws SQLException {
        try (ResultSet chaves = comando.getGeneratedKeys()) {
            if (chaves.next()) {
                return chaves.getInt(1);
            }
            throw new SQLException("O banco não devolveu o código " + doQue + ".");
        }
    }

    /** java.util.Date para java.sql.Date. */
    private java.sql.Date paraDataSql(Date data) {
        return data == null ? null : new java.sql.Date(data.getTime());
    }
}
