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
 * DAO da hierarquia Orcamento - tabelas TB_Orcamento, TB_OrcamentoModulos,
 * TB_OrcamentoChaves e TB_ItemOrcamento, mais os JOINs com TB_Cliente,
 * TB_Veiculo, TB_ModuloECU, TB_InformacoesSegurancaVeiculo e TB_ServicoBase.
 *
 * Operações do Diagrama de Classes v2: buscarOrcamento(id),
 * buscarOrcamentosAprovados(), gerarNumeroOrcamento(), salvar(orc),
 * salvarRascunho(orc), buscarAtendimentosModulo(mod) e listarOrcamentos().
 *
 * É ONDE A HERANÇA DO ORÇAMENTO VIRA TABELA, com a mesma estratégia da
 * OrdemServicoDAO ("uma tabela por subclasse", PK da subclasse = FK):
 *
 *     Orcamento         -> TB_Orcamento
 *       |_ OrcamentoModulos -> TB_OrcamentoModulos (PK = FK)
 *       |_ OrcamentoChaves  -> TB_OrcamentoChaves  (PK = FK)
 */
public class OrcamentoDAO {

    /**
     * Colunas do orçamento e das associações com Cliente e Veiculo.
     *
     * 2ª iteração: TB_Orcamento não tem mais segmentoPretendido - o segmento
     * é dado pela tabela de subclasse que tem linha. E os campos de módulo e
     * de senhas que a tela do Registrar OS exibe saíram de TB_Veiculo: vêm de
     * TB_ModuloECU (o módulo do orçamento) e de TB_InformacoesSegurancaVeiculo.
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

    /**
     * Mensagem 1.1 do Registrar OS: devolve o orçamento com cliente, veículo e
     * itens carregados, ou null se o código não existir.
     */
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

    /**
     * Passo 2 do Registrar OS (RN-01 / PRE-02): orçamentos "Aprovado pelo
     * cliente" ainda não convertidos, para a combo "Orçamento associado".
     */
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

    /**
     * Passo 1 e mensagem 1.2: próximo número ORC-AAAA-NNNN (RN-08), "com
     * sequência reiniciada a cada ano" - o LIKE só olha os números do ano.
     */
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

    /**
     * Pós-condição "o objeto orc foi persistido" do Contrato de Operação de
     * registrarOrcamento. Grava, numa única transação, o módulo novo (se a
     * mensagem 1.8 o criou), a superclasse, a subclasse e os itens.
     */
    public void salvar(Orcamento orc) throws SQLException {
        gravar(orc);
    }

    /** FA-12D: grava o rascunho (situação "Rascunho", sem número e sem validade). */
    public void salvarRascunho(Orcamento orc) throws SQLException {
        gravar(orc);
    }

    /**
     * Passo 8.2 da V1: orçamentos registrados anteriormente para o mesmo
     * módulo. Rascunhos não são atendimentos, por isso ficam de fora.
     */
    public List<Orcamento> buscarAtendimentosModulo(ModuloECU mod) throws SQLException {
        List<Orcamento> atendimentos = new ArrayList<>();
        if (mod == null || mod.getIdModuloECU() == 0) {
            return atendimentos;   // módulo ainda não gravado: não há histórico
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

    /** FA-00.4 e FA-12D: a listagem de orçamentos, do mais recente ao mais antigo. */
    public List<Orcamento> listarOrcamentos() throws SQLException {
        String sql = SELECT_ORCAMENTO + " ORDER BY o.idOrcamento DESC";
        return buscarLista(sql, null);
    }

    /**
     * Atualiza a situação do orçamento.
     * ACRÉSCIMO da 1ª iteração (POS-02 e RN-05 do Registrar OS).
     */
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

    /**
     * Inclui um serviço ou peça não previsto e soma o valor ao valorEstimado.
     * ACRÉSCIMO da 1ª iteração, exigido pelo FA-11B do Registrar OS.
     */
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

    /**
     * As linhas do módulo, da superclasse, da subclasse e dos itens têm de
     * nascer juntas: ou todas, ou nenhuma (FA-13: nada fica pela metade).
     */
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
                // Os ids gerados para os itens não voltam para os objetos: os
                // itens ainda são identificados pelo id da sessão de registro.
                for (ItemOrcamento item : orc.getItens()) {
                    inserirItem(conexao, id, item.getServicoBase().getIdServicoBase(),
                            item.getQuantidade(), item.getValorProposto());
                }
                conexao.commit();
            } catch (SQLException erro) {
                conexao.rollback();
                // FA-13: desfeito o INSERT, o objeto volta ao estado de antes,
                // para que a nova tentativa grave o módulo de novo.
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

    /**
     * Mensagem 1.8 criou um ModuloECU novo (id 0): ele é gravado antes do
     * orçamento, porque TB_OrcamentoModulos.idModuloECU aponta para ele.
     */
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

    /**
     * INSERT na subclasse, com o MESMO id da superclasse. O instanceof traduz
     * o tipo do objeto na tabela em que a linha filha é gravada.
     */
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

    /**
     * Itens do orçamento (composição "compõe" 1..*) com o serviço de catálogo
     * de cada um.
     */
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

    /**
     * Monta Orcamento, Cliente e Veiculo a partir da linha do JOIN. O objeto
     * é um Orcamento simples, com o segmento no campo segmentoPretendido:
     * é a forma que a 1ª iteração usa e basta para as listagens.
     */
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

    /**
     * Monta o Veiculo com as colunas de TB_Veiculo e, para a tela do Registrar
     * OS, os dados de módulo e de segurança vindos das tabelas novas.
     */
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
        // "possui 0..1 InformacoesSegurancaVeiculo": só existe se há linha na tabela.
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

    /** Converte a java.util.Date do modelo na java.sql.Date do JDBC. */
    private java.sql.Date paraDataSql(Date data) {
        return data == null ? null : new java.sql.Date(data.getTime());
    }
}
