package uniaoevolutions.controle;

import uniaoevolutions.BancoDeTeste;
import uniaoevolutions.negocio.Cliente;
import uniaoevolutions.negocio.ModuloECU;
import uniaoevolutions.negocio.Orcamento;
import uniaoevolutions.negocio.OrcamentoModulos;
import uniaoevolutions.negocio.OrdemDeServico;
import uniaoevolutions.negocio.Usuario;
import uniaoevolutions.negocio.Veiculo;
import uniaoevolutions.persistencia.ClienteDAO;
import uniaoevolutions.persistencia.ModuloECUDAO;
import uniaoevolutions.persistencia.OrcamentoDAO;
import uniaoevolutions.persistencia.OrdemServicoDAO;
import uniaoevolutions.persistencia.ServicoBaseDAO;
import uniaoevolutions.persistencia.UsuarioDAO;
import uniaoevolutions.persistencia.VeiculoDAO;
import uniaoevolutions.view.Validacoes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Testes sem interface: a ordem das mensagens de registrarOrcamento (com DAOs
 * espiãs), as regras da ControladoraSolicitarOrcamento, os validadores de
 * formato e a regressão do Registrar Ordem de Serviço (1ª iteração).
 *
 *   java -cp "bin-test:bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.controle.TesteControladoras
 */
public class TesteControladoras {

    private static int falhas;
    private static int verificacoes;
    private static final List<String> chamadas = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("=== validadores ===");
        validadores();
        System.out.println("=== ordem das mensagens de registrarOrcamento ===");
        ordemDasMensagens();
        System.out.println("=== regras da controladora ===");
        regrasDaControladora();
        System.out.println("=== regressão: Registrar Ordem de Serviço ===");
        regressaoRegistrarOS();
        System.out.println("\n" + verificacoes + " verificações, " + falhas + " falha(s).");
        System.exit(falhas == 0 ? 0 : 1);
    }

    static void validadores() {
        verificar(Validacoes.cpfValido("024.577.310-03"), "CPF válido");
        verificar(!Validacoes.cpfValido("024.577.310-45"), "CPF do mockup recusado (DV)");
        verificar(!Validacoes.cpfValido("000.000.000-00"), "CPF com dígitos iguais recusado");
        verificar(!Validacoes.cpfValido("0245773100"), "CPF com 10 dígitos recusado");
        verificar(Validacoes.cnpjValido("11.222.333/0001-81"), "CNPJ válido");
        verificar(!Validacoes.cnpjValido("11.222.333/0001-82"), "CNPJ com DV errado recusado");
        verificar(Validacoes.converterData("29/02/2028") != null, "29/02 em ano bissexto aceito");
        verificar(Validacoes.converterData("29/02/2027") == null, "29/02 fora de ano bissexto recusado");
        verificar(Validacoes.converterData("12/31/2027") == null, "mês 31 recusado (formato americano)");
        verificar(Validacoes.converterData("1/2/2027") != null, "dia e mês com um dígito aceitos");
        verificar(Validacoes.converterData("25112026") != null, "data sem barras (ddmmaaaa) aceita");
        verificar(Validacoes.converterData("31022027") == null, "data sem barras inexistente recusada");
        verificar(Validacoes.converterData("2511202") == null, "data sem barras com 7 dígitos recusada");
        verificarIgual(1234.56, Validacoes.converterValor("R$ 1.234,56"), "valor com R$ e milhar");
        verificarIgual(150.5, Validacoes.converterValor("150,5"), "valor com uma casa");
        verificarIgual(null, Validacoes.converterValor("1.23,00"), "milhar malformado recusado");
        verificarIgual(null, Validacoes.converterValor("-1,00"), "negativo recusado");
        verificarIgual(null, Validacoes.validarTelefone("(67) 99180-4402"), "celular válido");
        verificarIgual(null, Validacoes.validarTelefone("(67) 3321-7788"), "fixo válido");
        verificar(Validacoes.validarTelefone("(67) 89180-4402") != null, "celular sem o 9 recusado");
        verificarIgual(null, Validacoes.validarPlaca("QAF-7C21"), "placa Mercosul");
        verificarIgual(null, Validacoes.validarPlaca("rtb2j45"), "placa sem hífen");
        verificarIgual("QAF-7C21", Validacoes.normalizarPlaca("qaf7c21"), "placa normalizada");
        verificar(Validacoes.validarChassi("9BWAB45U1ET04321Q") != null, "chassi com Q recusado");
        verificarIgual(null, Validacoes.validarBuscaCliente("67991804402"), "busca: celular sem pontuação é aceito");
        verificarIgual(Validacoes.MSG_CPF_INVALIDO, Validacoes.validarBuscaCliente("02457731045"),
                "busca: 11 dígitos que não são CPF nem celular são recusados");
    }

    /** Diagrama de Comunicação: 1.1 buscarUsuario, 1.2 gerarNumeroOrcamento, 1.7 buscarModulo, e salvar. */
    static void ordemDasMensagens() throws Exception {
        BancoDeTeste.recriar();
        ControladoraSolicitarOrcamento c = new ControladoraSolicitarOrcamento(1,
                new UsuarioDAO() {
                    @Override
                    public Usuario buscarUsuario(int id) throws SQLException {
                        chamadas.add("buscarUsuario");
                        return super.buscarUsuario(id);
                    }
                },
                new OrcamentoDAO() {
                    @Override
                    public String gerarNumeroOrcamento() throws SQLException {
                        chamadas.add("gerarNumeroOrcamento");
                        return super.gerarNumeroOrcamento();
                    }

                    @Override
                    public void salvar(Orcamento orc) throws SQLException {
                        chamadas.add("salvar");
                        super.salvar(orc);
                    }
                },
                new ClienteDAO(), new VeiculoDAO(), new ServicoBaseDAO(),
                new ModuloECUDAO() {
                    @Override
                    public ModuloECU buscarModulo(Veiculo vei, String identificacao) throws SQLException {
                        chamadas.add("buscarModulo");
                        return super.buscarModulo(vei, identificacao);
                    }
                },
                new OrdemServicoDAO());

        c.iniciarOrcamento();
        c.informarCliente(ControladoraSolicitarOrcamento.ORIGEM_PRESENCIAL, "02457731003");
        c.selecionarVeiculo("QAF-7C21");
        c.informarSegmento("Módulos");
        c.informarDadosModulo("ECU – Injeção Eletrônica", "Novo Modelo", "NV-0001", "Não liga.");
        c.incluirItem(1, 150);
        chamadas.clear();
        Orcamento orc = c.registrarOrcamento(emDias(5), "obs");
        verificarIgual(List.of("buscarUsuario", "gerarNumeroOrcamento", "buscarModulo", "salvar"), chamadas,
                "1.1 -> 1.2 -> 1.7 -> persistência, nessa ordem");
        verificar(orc instanceof OrcamentoModulos, "1.3: subclasse OrcamentoModulos");
        ModuloECU mod = ((OrcamentoModulos) orc).getModulo();
        verificar(mod != null && mod.getIdModuloECU() > 0, "1.8: módulo novo criado e gravado");
        verificar(orc.getVeiculo().getModulos().contains(mod), "1.9: módulo associado ao veículo");
        verificarIgual("Em análise", orc.getStatus(), "1.15.2: setStatus(\"Em análise\")");
        verificarIgual(150.0, orc.getValorEstimado(), "1.15.3: calcularValorEstimado()");
        verificarIgual(1, orc.getTecnico().getIdUsuario(), "1.14: associarTecnico(tec)");
    }

    static void regrasDaControladora() throws Exception {
        BancoDeTeste.recriar();
        ControladoraSolicitarOrcamento c = new ControladoraSolicitarOrcamento(1);
        c.iniciarOrcamento();
        espera(() -> c.selecionarVeiculo("QAF-7C21"), "Identifique o cliente antes de selecionar o veículo.",
                "DSS: selecionarVeiculo (5) antes de informarCliente (3) é recusado");
        espera(() -> c.informarSegmento("Módulos"), "Selecione o veículo antes de informar o segmento.",
                "DSS: informarSegmento (7) antes de selecionarVeiculo (5) é recusado");
        espera(() -> c.informarCliente("Online", "02457731003"), "Selecione a origem do atendimento.",
                "origem fora de Presencial/Remoto");
        Cliente cli = c.informarCliente("Remoto", "Marcos Antônio Ribeiro");
        verificar(cli != null && cli.getIdCliente() == 1, "busca por nome");
        verificar(c.informarCliente("Remoto", "Ninguém Cadastrado") == null, "FA-02: cliente inexistente -> null");
        Cliente porCelular = c.informarCliente("Remoto", "67991804402");
        verificar(porCelular != null && porCelular.getIdCliente() == 1, "celular de 11 dígitos sem pontuação encontra o cliente");
        verificarIgual(6, c.consultarClientes("").size(), "lista de clientes para selecionar (todos)");
        verificarIgual(1, c.consultarClientes("3321").size(), "lista filtrada por trecho de telefone fixo");
        verificarIgual(1, c.consultarClientes("024577").size(), "lista filtrada por início de CPF");
        c.informarCliente("Remoto", "02457731003");
        verificar(c.selecionarVeiculo("ZZZ-9Z99") == null, "FA-04: placa inexistente -> null");
        c.selecionarVeiculo("QAF-7C21");
        espera(() -> c.cadastrarVeiculo("QAF-7C21", "VW", "Gol", 2014, "1.6", "9BWAB45U1ET043211"),
                "Já existe veículo cadastrado com a placa QAF-7C21. Informe a placa no campo Veículo (placa).",
                "placa duplicada no FA-04");
        c.informarSegmento("Módulos");
        espera(() -> c.incluirItem(4, 100), ControladoraSolicitarOrcamento.MSG_FORA_DO_SEGMENTO,
                "FA-09B: serviço de Chaves no segmento Módulos (RN-03)");
        espera(() -> c.incluirItem(1, 0), ControladoraSolicitarOrcamento.MSG_VALOR_INVALIDO, "FA-09A: valor zero");
        espera(() -> c.registrarOrcamento(emDias(5), null), ControladoraSolicitarOrcamento.MSG_SEM_ITENS,
                "FA-12A: sem itens (RN-01)");
        verificarIgual(100.0, c.incluirItem(1, 100), "RN-05: soma após inclusão");
        verificarIgual(300.0, c.incluirItem(2, 200), "RN-05: soma após segunda inclusão");
        verificarIgual(250.0, c.alterarValorItem(2, 150), "FA-11B: soma após alteração");
        verificarIgual(150.0, c.removerItem(1), "FA-11A: soma após remoção");
        espera(() -> c.informarSegmento("Chaves"), "Remova os serviços incluídos antes de trocar o segmento.",
                "RN-03: não troca de segmento com itens incluídos");
        espera(() -> c.registrarOrcamento(emDias(5), null), "Corrija 3 pendências antes de registrar:\n"
                        + "• Tipo de módulo: campo obrigatório para o segmento Módulos.\n"
                        + "• Modelo da ECU: campo obrigatório para o segmento Módulos.\n"
                        + "• Sintoma relatado pelo cliente: campo obrigatório para o segmento Módulos.",
                "FA-12B: pendências da RN-04");
        c.informarDadosModulo("ECU – Injeção Eletrônica", "Bosch ME7.5.30", "", "Falha.");
        espera(() -> c.registrarOrcamento(emDias(-1), null), ControladoraSolicitarOrcamento.MSG_VALIDADE_INVALIDA,
                "FA-12C: validade anterior (RN-07)");
        verificar(c.registrarOrcamento(emDias(0), null) != null, "RN-07: validade igual à data aceita");

        ControladoraSolicitarOrcamento chaves = new ControladoraSolicitarOrcamento(3);
        chaves.iniciarOrcamento();
        chaves.informarCliente("Presencial", "11222333000181");
        chaves.selecionarVeiculo("RTB-2J45");
        chaves.informarSegmento("Chaves");
        verificarIgual(ControladoraSolicitarOrcamento.MSG_LIMITE_CHAVES,
                chaves.informarDadosChaves("Chave com transponder", 5, false, null, "Bloqueado", false),
                "FAV-8.4B: limite do imobilizador");
        verificarIgual(ControladoraSolicitarOrcamento.MSG_COMPROVACAO_PROPRIEDADE,
                chaves.informarDadosChaves("Chave com transponder", 2, false, null, "Bloqueado", true),
                "passo 8.5: exigência de comprovação");
        verificarIgual(ControladoraSolicitarOrcamento.SERVICO_APAGAMENTO,
                chaves.informarComprovacaoPropriedade("CRLV 1", "Fulano", "RG 1").getDescricao(),
                "FAV-8.4A.4: procedimento acrescentado");

        ControladoraSolicitarOrcamento inexistente = new ControladoraSolicitarOrcamento(99);
        espera(inexistente::iniciarOrcamento, "Usuário logado não encontrado.", "PRE-01: usuário inexistente");
        ControladoraSolicitarOrcamento admin = new ControladoraSolicitarOrcamento(4);
        espera(admin::iniciarOrcamento, "Somente Técnico de Módulos ou Técnico de Chaves pode registrar orçamentos.",
                "PRE-01 / casos de uso: Administrador não é ator do Solicitar Orçamento");
    }

    /** Mesma sequência que foi executada antes das mudanças da 2ª iteração. */
    static void regressaoRegistrarOS() throws Exception {
        BancoDeTeste.recriar();
        ControladoraRegistrarOS c = new ControladoraRegistrarOS();
        verificarIgual("OS-" + Calendar.getInstance().get(Calendar.YEAR) + "-0001",
                c.iniciarRegistroOS().getNumeroOS(), "número da OS");
        List<Orcamento> aprovados = c.consultarOrcamentosAprovados();
        verificarIgual("[ORC-2026-0231, ORC-2026-0232]", aprovados.toString(), "orçamentos aprovados");
        Orcamento o = c.consultarOrcamento(231);
        verificarIgual("Módulos|3|750.0", o.getSegmentoPretendido() + "|" + o.getItens().size() + "|"
                + o.getValorTotal(), "segmento, itens e total do 231");
        Veiculo v = o.getVeiculo();
        verificarIgual("QAF-7C21|Magneti Marelli IAW 4GV.G1|MM-88214-C|VW-4471|7781",
                v.getPlaca() + "|" + v.getModeloECU() + "|" + v.getIdentificacaoModulo() + "|"
                        + v.getCodigoMecanico() + "|" + v.getSenhaImobilizador(),
                "dados de módulo e de segurança vindos das tabelas novas");
        verificarIgual("Chaves", c.consultarOrcamento(232).getSegmentoPretendido(), "segmento do 232 pela subclasse");
        c.selecionarOrcamento(o);
        OrdemDeServico os = c.registrarOrdemServico(231, 1, 85000, emDias(5), "obs", "relato", "cond", "diag", "");
        verificarIgual("Aberta|750.0", os.getStatusOS() + "|" + os.getValorTotal(), "OS registrada");
        verificarIgual("[ORC-2026-0232]", c.consultarOrcamentosAprovados().toString(), "231 saiu da lista");
        verificarIgual("Convertido em OS",
                BancoDeTeste.linha("SELECT status FROM TB_Orcamento WHERE idOrcamento = 231"), "POS-02");
        espera(() -> c.registrarOrdemServico(232, 3, 0, null, "", "", "", "", ""),
                "Informe a quilometragem atual do veículo.", "FA-11A do Registrar OS");
    }

    // -----------------------------------------------------------------

    interface Acao {
        Object executar() throws Exception;
    }

    static void espera(Acao acao, String mensagem, String descricao) {
        try {
            acao.executar();
            verificar(false, descricao + "  [não lançou exceção]");
        } catch (IllegalArgumentException erro) {
            verificarIgual(mensagem, erro.getMessage(), descricao);
        } catch (Exception erro) {
            verificar(false, descricao + "  [" + erro + "]");
        }
    }

    static Date emDias(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, dias);
        return c.getTime();
    }

    static void verificar(boolean condicao, String descricao) {
        verificacoes++;
        if (!condicao) {
            falhas++;
        }
        System.out.println((condicao ? "  ok   " : "  FALHA ") + descricao);
    }

    static void verificarIgual(Object esperado, Object obtido, String descricao) {
        boolean igual = esperado == null ? obtido == null : esperado.equals(obtido);
        verificar(igual, descricao + (igual ? "" : "  [esperado=" + esperado + " | obtido=" + obtido + "]"));
    }
}
