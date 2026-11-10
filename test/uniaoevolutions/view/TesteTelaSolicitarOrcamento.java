package uniaoevolutions.view;

import uniaoevolutions.BancoDeTeste;
import uniaoevolutions.controle.ControladoraSolicitarOrcamento;
import uniaoevolutions.negocio.ServicoBase;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static uniaoevolutions.view.Robo.*;

/**
 * Testes de interface do Solicitar Orçamento: a tela de verdade, sob um
 * display (Xvfb), exercitada como o Técnico faria. Cada cenário começa com o
 * banco recriado pelo sql/schema.sql.
 *
 * Executar (da pasta registrar-ordem-servico), com o MySQL/MariaDB no ar:
 *   java -cp "bin-test:bin:lib/mysql-connector-j-8.4.0.jar" -Dimagens=<pasta> \
 *        uniaoevolutions.view.TesteTelaSolicitarOrcamento
 */
public class TesteTelaSolicitarOrcamento {

    private static final SimpleDateFormat DATA = new SimpleDateFormat("dd/MM/yyyy");
    private static final String VAL = ControladoraSolicitarOrcamento.MSG_VALOR_INVALIDO;

    private TelaSolicitarOrcamento tela;
    private JComponent conteudo;

    public static void main(String[] args) throws Exception {
        TesteTelaSolicitarOrcamento teste = new TesteTelaSolicitarOrcamento();
        String apenas = args.length > 0 ? args[0] : "";
        String[] cenarios = {"ordemDoDss", "selecaoSemDigitar", "fluxoPrincipalModulos", "variantesChavesComCadastros", "validacoesDosCampos",
                "fluxosDoPasso12", "etiquetaIlegivel", "falhaDeGravacao", "desistencia", "focoReal"};
        for (String cenario : cenarios) {
            if (!apenas.isEmpty() && !apenas.equals(cenario)) {
                continue;
            }
            System.out.println("\n=== " + cenario + " ===");
            teste.getClass().getDeclaredMethod(cenario).invoke(teste);
            teste.fechar();
        }
        System.out.println("\n" + verificacoes + " verificações, " + falhas + " falha(s).");
        System.exit(falhas == 0 ? 0 : 1);
    }

    // =================================================================
    // Cenários
    // =================================================================

    /**
     * A tela envia as mensagens na ordem do DSS, mesmo quando o Técnico
     * preenche fora de ordem: segmento antes do veículo, bancada antes dos
     * dados do módulo, documentos da perda total antes dos dados das chaves.
     */
    void ordemDoDss() throws Exception {
        // V1, com a bancada marcada antes dos dados do módulo.
        ControladoraEspia espia = abrirComEspia(1);
        digitar(tela, "campoValidade", emDias(10));
        clicar(tela, "radioModulos");                       // antes do cliente e do veículo
        JDialog aviso = esperarDialogo("Atenção");
        verificarContem("Selecione o veículo antes de informar o segmento.", textoDoDialogo(aviso),
                "DSS 7 antes do 5 é recusado");
        clicarNoDialogo(aviso, "OK");
        javax.swing.JRadioButton modulos = achar(tela, "radioModulos");
        javax.swing.JRadioButton chaves = achar(tela, "radioChaves");
        verificar(!naEdt(modulos::isSelected) && !naEdt(chaves::isSelected),
                "o segmento recusado volta a nenhum (não ao segmento oposto)");
        escreverNaCombo(tela, "comboVeiculo", "QAF-7C21");     // antes do cliente
        verificarIgual("Identifique o cliente antes de selecionar o veículo.", tela.mensagemDoCampo("Veículo (placa)"),
                "DSS 5 antes do 3 é recusado");
        clicar(tela, "radioPresencial");
        digitar(tela, "campoCliente", "02457731003");
        escolherNaCombo(tela, "comboVeiculo", 0);
        clicar(tela, "radioModulos");
        sincronizar();
        clicar(tela, "checkBancada");                      // FAV-8.2 antes do passo 8.1
        sincronizar();
        verificarIgual(-1, espia.primeira("informarDiagnosticoPendente"),
                "FAV-8.2 marcado antes dos dados do módulo: a mensagem ainda não sai");
        escreverNaCombo(tela, "comboTipoModulo", "ECU – Injeção Eletrônica");
        escreverNaCombo(tela, "comboModeloECU", "Bosch ME7.5.30");
        digitar(tela, "campoSintoma", "Não liga.");
        verificar(espia.primeira("informarDadosModulo") >= 0
                        && espia.primeira("informarDadosModulo") < espia.primeira("informarDiagnosticoPendente"),
                "DSS 9 (informarDadosModulo) antes de DSS variantes 6 (informarDiagnosticoPendente)");
        incluir(0, "150");
        clicar(tela, "botaoRegistrar");
        clicarNoDialogo(esperarDialogo("Orçamento registrado com sucesso"), "Novo orçamento");
        List<String> esperado = List.of("iniciarOrcamento", "informarCliente", "selecionarVeiculo", "informarSegmento",
                "informarDadosModulo", "informarDiagnosticoPendente", "incluirItem", "registrarOrcamento");
        verificarIgual(esperado, semRecusadas(espia.sequencia(), esperado.get(esperado.size() - 1)),
                "V1: sequência das mensagens = DSS fluxo normal + FAV-8.2");
        verificarIgual("1", BancoDeTeste.linha("SELECT m.diagnosticoPendenteBancada FROM TB_OrcamentoModulos m "
                + "JOIN TB_Orcamento o ON o.idOrcamento = m.idOrcamento WHERE o.numero = 'ORC-2026-0234'"),
                "o diagnóstico pendente chegou ao orçamento gravado");
        fechar();

        // V2, com os documentos da perda total antes dos dados das chaves.
        espia = abrirComEspia(3);
        digitar(tela, "campoValidade", emDias(10));
        clicar(tela, "radioPresencial");
        digitar(tela, "campoCliente", "31190422778");
        escolherNaCombo(tela, "comboVeiculo", 0);
        clicar(tela, "radioChaves");
        sincronizar();
        clicar(tela, "checkPerdaTotal");
        sincronizar();
        digitar(tela, "campoDocumentoPropriedade", "CRLV-e nº 01928374651");
        digitar(tela, "campoNomeSolicitante", "Fernanda Lopes Barbosa");
        digitar(tela, "campoDocumentoSolicitante", "RG 1.982.336 SSP/MS");
        verificarIgual(-1, espia.primeira("informarComprovacaoPropriedade"),
                "FAV-8.4A: documentos antes dos dados das chaves - a comprovação ainda não sai");
        escreverNaCombo(tela, "comboTipoChave", "Canivete com transponder");
        digitar(tela, "campoQuantidade", "2");
        escreverNaCombo(tela, "comboSituacaoImobilizador", "Bloqueado – sem chave válida");
        verificar(espia.primeira("informarDadosChaves") >= 0
                        && espia.primeira("informarDadosChaves") < espia.primeira("informarComprovacaoPropriedade"),
                "DSS 11 (informarDadosChaves) antes de DSS variantes 9 (informarComprovacaoPropriedade)");
        verificar(visivel(tela, "avisoAcrescentado"), "FAV-8.4A.4: procedimento acrescentado depois dos dados");
        digitar(tela, "campoValorProposto", "280,00");
        clicar(tela, "botaoIncluirServico");
        sincronizar();
        clicar(tela, "botaoDesistir");
        clicarNoDialogo(esperarDialogo("Desistir deste orçamento?"), "Desistir");
        clicarNoDialogo(esperarDialogo("Orçamentos"), "Fechar");
        List<String> esperado2 = List.of("iniciarOrcamento", "informarCliente", "selecionarVeiculo", "informarSegmento",
                "informarDadosChaves", "informarComprovacaoPropriedade", "incluirItem",
                "solicitarConfirmacaoDesistencia", "descartarOrcamento");
        verificarIgual(esperado2, semRecusadas(espia.sequencia(), "descartarOrcamento"),
                "V2: sequência = DSS fluxo normal + FAV-8.4A + FA-00 (17-20)");
    }

    /** Sequência até a mensagem final, sem as repetições que a tela faz ao reenviar campos. */
    private static List<String> semRecusadas(List<String> sequencia, String ultima) {
        List<String> limpa = new java.util.ArrayList<>();
        for (String m : sequencia) {
            if (!limpa.contains(m) || m.equals("iniciarOrcamento") && !limpa.isEmpty()) {
                if (m.equals("iniciarOrcamento") && !limpa.isEmpty()) {
                    break;   // novo orçamento aberto depois do registro
                }
                limpa.add(m);
            }
            if (m.equals(ultima)) {
                break;
            }
        }
        return limpa;
    }

    private ControladoraEspia abrirComEspia(int usuario) throws Exception {
        BancoDeTeste.recriar();
        ControladoraEspia espia = new ControladoraEspia(usuario);
        tela = naEdt(() -> {
            TelaSolicitarOrcamento nova = new TelaSolicitarOrcamento(usuario);
            java.lang.reflect.Field campo = TelaSolicitarOrcamento.class.getDeclaredField("controladora");
            campo.setAccessible(true);
            campo.set(nova, espia);     // antes do iniciarOrcamento, que está agendado
            nova.setVisible(true);
            return nova;
        });
        conteudo = campoPrivado(tela, "conteudo");
        esperar(600);
        sincronizar();
        return espia;
    }

    /** Os dados de exemplo permitem chegar ao registro só selecionando. */
    void selecaoSemDigitar() throws Exception {
        abrir(1);
        clicar(tela, "radioPresencial");
        naEdt(() -> tela.mostrarSugestoesDeClientes(""));
        sincronizar();
        javax.swing.JList<?> lista = achar(campoPrivado(tela, "popupClientes"), "listaClientes");
        verificarIgual(6, naEdt(() -> lista.getModel().getSize()), "lupa com campo vazio: lista todos os clientes");
        naEdt(() -> tela.mostrarSugestoesDeClientes("fer"));
        sincronizar();
        verificarIgual(1, naEdt(() -> lista.getModel().getSize()), "trecho do nome filtra a lista");
        naEdt(() -> tela.escolherCliente((uniaoevolutions.negocio.Cliente) lista.getModel().getElementAt(0)));
        sincronizar();
        verificarIgual("Fernanda Lopes Barbosa", texto(tela, "campoCliente"), "cliente escolhido na lista");
        verificarIgual("311.904.227-78", texto(tela, "campoCpfCnpj"), "CPF do cliente escolhido");
        escolherNaCombo(tela, "comboVeiculo", 0);
        verificarIgual("Fiat Argo Drive", texto(tela, "campoMarcaModelo"), "veículo da cliente para selecionar");

        naEdt(() -> tela.mostrarSugestoesDeClientes("67991804402"));
        sincronizar();
        naEdt(() -> tela.escolherCliente((uniaoevolutions.negocio.Cliente) lista.getModel().getElementAt(0)));
        sincronizar();
        verificarIgual("Marcos Antônio Ribeiro", texto(tela, "campoCliente"), "busca pelo celular sem pontuação");
        escolherNaCombo(tela, "comboVeiculo", 0);
        clicar(tela, "radioModulos");
        sincronizar();
        JComboBox<?> modulos = achar(tela, "comboIdentificacaoModulo");
        verificarIgual(2, naEdt(modulos::getItemCount), "módulos registrados do veículo para selecionar");
        escolherNaCombo(tela, "comboIdentificacaoModulo", 0);
        JComboBox<?> tipo = achar(tela, "comboTipoModulo");
        JComboBox<?> modelo = achar(tela, "comboModeloECU");
        verificarIgual("ECU – Injeção Eletrônica", naEdt(() -> ((javax.swing.JTextField) tipo.getEditor()
                .getEditorComponent()).getText()), "tipo de módulo preenchido pela seleção");
        verificarIgual("Magneti Marelli IAW 4GV.G1", naEdt(() -> ((javax.swing.JTextField) modelo.getEditor()
                .getEditorComponent()).getText()), "modelo da ECU preenchido pela seleção");
        // Sem sair do campo: os atendimentos aparecem logo depois de digitar (rodada 6).
        javax.swing.text.JTextComponent sintoma = achar(tela, "campoSintoma");
        naEdt(() -> sintoma.setText("Não liga."));   // o único campo de texto livre exigido (RN-04)
        esperar(1000);
        sincronizar();
        verificarContem("ORC-2026-0231", texto(tela, "caixaAtendimentos"),
                "atendimentos do módulo aparecem ao digitar o sintoma, sem perder o foco");
        salvarFormulario(tela, conteudo, "tela1-selecao.png");
    }

    /** Fluxo principal com a variante V1 e o FAV-8.2 - Tela 1 do mockup. */
    void fluxoPrincipalModulos() throws Exception {
        abrir(1);
        verificarIgual("ORC-2026-0234", texto(tela, "campoNumero"), "passo 1: número provisório (RN-08)");
        verificarIgual(DATA.format(new Date()), texto(tela, "campoDataSolicitacao"), "passo 1: data da solicitação");
        verificarIgual("Gabriel Abraão – Técnico de Módulos", texto(tela, "campoResponsavel"), "responsável = usuário logado");
        verificarIgual("EM ANÁLISE", texto(tela, "rotuloSituacao"), "situação exibida");
        verificar(!visivel(tela, "secaoDadosTecnicos"), "passo 7: bloco técnico só aparece depois do segmento");

        digitar(tela, "campoValidade", emDias(15));
        clicar(tela, "radioPresencial");
        digitar(tela, "campoCliente", "024.577.310-03");
        verificarIgual("Marcos Antônio Ribeiro", texto(tela, "campoCliente"), "passo 3: nome do cliente");
        verificarIgual("024.577.310-03", texto(tela, "campoCpfCnpj"), "passo 3: CPF/CNPJ");
        verificarIgual("(67) 99180-4402", texto(tela, "campoTelefone"), "passo 3: telefone");
        JComboBox<?> veiculos = achar(tela, "comboVeiculo");
        verificarIgual(1, naEdt(veiculos::getItemCount), "passo 3: relação de veículos vinculados");

        escolherNaCombo(tela, "comboVeiculo", 0);
        verificarIgual("Volkswagen Gol G6", texto(tela, "campoMarcaModelo"), "passo 5: marca / modelo");
        verificarIgual("2014", texto(tela, "campoAnoModelo"), "passo 5: ano-modelo");
        verificarIgual("1.6 MSI", texto(tela, "campoMotorizacao"), "passo 5: motorização");
        verificarIgual("9BWAB45U1ET043219", texto(tela, "campoChassi"), "passo 5: chassi");
        verificarContem("Ver 5 atendimentos anteriores", texto(tela, "botaoHistorico"),
                "passo 5: histórico técnico = os 5 orçamentos do veículo (rodada 7)");

        clicar(tela, "radioModulos");
        sincronizar();
        verificar(visivel(tela, "secaoDadosTecnicos"), "passo 7.1: bloco de módulos exibido");
        JComboBox<?> servicos = achar(tela, "comboServico");
        verificarIgual(3, naEdt(servicos::getItemCount), "passo 7.2: só serviços base de Módulos (RN-03)");

        escreverNaCombo(tela, "comboTipoModulo", "ECU – Injeção Eletrônica");
        escreverNaCombo(tela, "comboModeloECU", "Magneti Marelli IAW 4GV.G1");
        escreverNaCombo(tela, "comboIdentificacaoModulo", "MM-88214-C");
        digitar(tela, "campoSintoma", "Veículo não dá partida a frio; luz de injeção acesa no painel.");
        verificarContem("ORC-2026-0231", texto(tela, "caixaAtendimentos"),
                "passo 8.2: atendimentos anteriores do mesmo módulo");

        // Rodada 7: cada atendimento do módulo aparece, com o mesmo texto, no histórico técnico do veículo.
        String doModulo = texto(tela, "caixaAtendimentos");
        clicar(tela, "botaoHistorico");
        JDialog historico = esperarDialogo("Histórico técnico – QAF-7C21");
        String doVeiculo = textoDoDialogo(historico);
        for (String linha : new String[]{
                "ORC-2026-0233 · 25/08/2026 · Aguardando aprovação do cliente · R$ 300,00",
                "ORC-2026-0231 · 20/08/2026 · Aprovado pelo cliente · R$ 750,00"}) {
            verificarContem(linha, doModulo, "passo 8.2: " + linha.substring(0, 13) + " no bloco do módulo");
            verificarContem(linha, doVeiculo, "passo 5: " + linha.substring(0, 13) + " também no histórico técnico");
        }
        verificarContem("ORC-2025-0187", doVeiculo, "histórico técnico: orçamento de outro módulo do veículo");
        verificar(!doModulo.contains("ORC-2025-0187"), "bloco do módulo: orçamento de outro módulo fica de fora");
        verificarContem("OS-2025-0112", doVeiculo, "histórico técnico: Ordens de Serviço do veículo abaixo");
        salvarJanela(historico, "tela2-historico-tecnico.png");
        clicarNoDialogo(historico, "Fechar");

        clicar(tela, "checkBancada");
        sincronizar();
        verificar(naEdt(() -> tela.limiteChavesAtingido() == false), "(sem efeito de chaves no V1)");

        incluir(0, "150,00");
        incluir(1, "380,00");
        incluir(2, "220,00");
        verificarIgual(3, naEdt(() -> tela.itensDaTela().size()), "passos 9-11: três serviços incluídos");
        verificarIgual("R$ 750,00", texto(tela, "rotuloValorEstimado"), "passo 10 / RN-05: valor estimado");

        digitar(tela, "campoObservacoes",
                "Valores estimativos: sujeitos a revisão após a confirmação do defeito em teste de bancada.");
        salvarFormulario(tela, conteudo, "tela1-v1-preenchida.png");

        clicar(tela, "botaoRegistrar");
        JDialog sucesso = esperarDialogo("Orçamento registrado com sucesso");
        salvarJanela(sucesso, "tela2-passo13-sucesso.png");
        String textoSucesso = textoDoDialogo(sucesso);
        verificarContem("Orçamento ORC-2026-0234 registrado com sucesso.", textoSucesso, "passo 13: mensagem");
        verificarContem("Módulos (ECU)", textoSucesso, "passo 13: segmento");
        verificarContem("R$ 750,00", textoSucesso, "passo 13: valor estimado");
        verificarContem("EM ANÁLISE", textoSucesso, "passo 13: situação (RN-06)");
        clicarNoDialogo(sucesso, "Ver orçamento");
        JDialog detalhes = esperarDialogo("Orçamento ORC-2026-0234");
        salvarJanela(detalhes, "tela2-ver-orcamento.png");
        clicarNoDialogo(detalhes, "Fechar");
        sincronizar();

        verificarIgual("ORC-2026-0234 | Em análise | 750.0 | Presencial | 1 | 1 | 1 | " + hojeIso() + " | " + isoEmDias(15),
                BancoDeTeste.linha("SELECT numero, status, valorEstimado, origemAtendimento, idCliente, idVeiculo, "
                        + "idUsuario, dataSolicitacao, dataValidade FROM TB_Orcamento WHERE numero = 'ORC-2026-0234'"),
                "POS-01..04: TB_Orcamento");
        verificarIgual("Veículo não dá partida a frio; luz de injeção acesa no painel. | 1 | 0 | 1",
                BancoDeTeste.linha("SELECT m.sintomaRelatado, m.diagnosticoPendenteBancada, m.identificacaoIlegivel, "
                        + "m.idModuloECU FROM TB_OrcamentoModulos m JOIN TB_Orcamento o ON o.idOrcamento = m.idOrcamento "
                        + "WHERE o.numero = 'ORC-2026-0234'"),
                "subclasse TB_OrcamentoModulos e módulo existente reaproveitado (1.7)");
        verificarIgual(3, BancoDeTeste.contar("SELECT COUNT(*) FROM TB_ItemOrcamento i JOIN TB_Orcamento o "
                + "ON o.idOrcamento = i.idOrcamento WHERE o.numero = 'ORC-2026-0234'"), "POS-03: itens gravados");
        verificarIgual("ORC-2026-0235", texto(tela, "campoNumero"), "novo orçamento após o registro");
    }

    /** V2 com FA-02, FA-04, FAV-8.4A, FAV-8.4B, FA-11A e FA-11B. */
    void variantesChavesComCadastros() throws Exception {
        abrir(3);
        verificarIgual("Jamily Sales – Técnico de Chaves", texto(tela, "campoResponsavel"), "responsável (Técnico de Chaves)");
        digitar(tela, "campoValidade", emDias(15));
        clicar(tela, "radioRemoto");

        // FA-02
        digitar(tela, "campoCliente", "Ricardo Teixeira Lima");
        verificarIgual("Cliente não encontrado. Use “+ Cliente não cadastrado”.", tela.mensagemDoCampo("Cliente"),
                "FA-02: cliente sem cadastro");
        clicar(tela, "botaoClienteNaoCadastrado");
        JDialog cadastroCliente = esperarDialogo("Cliente não cadastrado");
        verificarIgual("Ricardo Teixeira Lima", texto(cadastroCliente, "campoNomeCliente"), "FA-02.2: nome trazido da busca");
        digitar(cadastroCliente, "campoTelefoneCliente", "(67) 2122-67");
        verificarContem(Validacoes.MSG_TELEFONE_INVALIDO, textoDoDialogo(cadastroCliente), "FA-02.3: telefone inválido ao perder o foco");
        clicarNoDialogo(cadastroCliente, "Salvar cliente");
        verificar(haDialogo("Cliente não cadastrado"), "FA-02.3: não salva com telefone inválido");
        digitar(cadastroCliente, "campoTelefoneCliente", "(67) 98877-1020");
        salvarJanela(cadastroCliente, "tela2-fa02-cadastrar-cliente.png");
        clicarNoDialogo(cadastroCliente, "Salvar cliente");

        // FA-02.5 -> FA-04
        JDialog cadastroVeiculo = esperarDialogo("Veículo não listado");
        verificarIgual("(67) 98877-1020", texto(tela, "campoTelefone"), "FA-02.4: cliente cadastrado exibido");
        digitar(cadastroVeiculo, "campoPlacaVeiculo", "mtz5f31");
        escreverNaCombo(cadastroVeiculo, "comboMarcaVeiculo", "Fiat");
        digitar(cadastroVeiculo, "campoModeloVeiculo", "Cronos Drive");
        digitar(cadastroVeiculo, "campoAnoVeiculo", "1890");
        verificarContem("Ano-modelo inválido", textoDoDialogo(cadastroVeiculo), "FA-04.3: ano inválido ao perder o foco");
        digitar(cadastroVeiculo, "campoAnoVeiculo", "2021");
        digitar(cadastroVeiculo, "campoMotorizacaoVeiculo", "1.0 Firefly");
        digitar(cadastroVeiculo, "campoChassiVeiculo", "9BD359A1NMYK2299O");
        verificarContem(Validacoes.MSG_CHASSI_INVALIDO, textoDoDialogo(cadastroVeiculo), "FA-04.3: chassi com letra O recusado");
        digitar(cadastroVeiculo, "campoChassiVeiculo", "9BD359A1NMYK22990");
        salvarJanela(cadastroVeiculo, "tela2-fa04-cadastrar-veiculo.png");
        clicarNoDialogo(cadastroVeiculo, "Salvar e vincular");
        sincronizar();
        verificarIgual("Fiat Cronos Drive", texto(tela, "campoMarcaModelo"), "FA-04.4: veículo cadastrado e vinculado");
        verificarIgual("MTZ-5F31 | Fiat | Cronos Drive | 2021 | 1.0 Firefly | 9BD359A1NMYK22990",
                BancoDeTeste.linha("SELECT placa, marca, modelo, anoModelo, motorizacao, numeroChassi FROM TB_Veiculo "
                        + "WHERE placa = 'MTZ-5F31'"), "FA-04: TB_Veiculo (placa digitada em minúsculas)");

        clicar(tela, "radioChaves");
        sincronizar();
        JComboBox<?> servicos = achar(tela, "comboServico");
        verificarIgual(5, naEdt(servicos::getItemCount), "passo 7.4: só serviços base de Chaves (RN-03)");

        escreverNaCombo(tela, "comboTipoChave", "Canivete com transponder");
        digitar(tela, "campoQuantidade", "5");
        clicar(tela, "radioTelecomandoSim");
        digitar(tela, "campoTransponder", "ID46 – PCF7946");
        escreverNaCombo(tela, "comboSituacaoImobilizador", "Bloqueado – sem chave válida");
        verificarIgual(ControladoraSolicitarOrcamento.MSG_LIMITE_CHAVES, tela.mensagemDoCampo("Qtd. de cópias"),
                "FAV-8.4B.1: limite do imobilizador");
        verificar(visivel(tela, "checkApagarChaves"), "FAV-8.4B.2: opção de apagar as chaves existentes");
        salvarFormulario(tela, conteudo, "tela3-fav84b-limite.png");
        clicar(tela, "checkApagarChaves");
        sincronizar();
        verificarIgual(null, tela.mensagemDoCampo("Qtd. de cópias"), "FAV-8.4B.2: apagar as existentes libera");
        clicar(tela, "checkApagarChaves");
        sincronizar();
        verificarIgual(ControladoraSolicitarOrcamento.MSG_LIMITE_CHAVES, tela.mensagemDoCampo("Qtd. de cópias"),
                "desmarcar volta a cobrar o limite");
        digitar(tela, "campoQuantidade", "2");
        verificarIgual(null, tela.mensagemDoCampo("Qtd. de cópias"), "FAV-8.4B.2: nova quantidade aceita");
        verificar(visivel(tela, "checkApagarChaves") == false, "opção de apagar some quando não há limite");

        // FAV-8.4A
        clicar(tela, "checkPerdaTotal");
        sincronizar();
        verificar(visivel(tela, "campoDocumentoPropriedade"), "FAV-8.4A.2: documentos exigidos");
        digitar(tela, "campoDocumentoPropriedade", "CRLV-e nº 01928374651");
        digitar(tela, "campoNomeSolicitante", "Fernanda Lopes Barbosa");
        digitar(tela, "campoDocumentoSolicitante", "RG 1.982.336 SSP/MS");
        verificar(visivel(tela, "avisoAcrescentado"), "FAV-8.4A.4: procedimento acrescentado");
        ServicoBase escolhido = naEdt(() -> (ServicoBase) servicos.getSelectedItem());
        verificarIgual(ControladoraSolicitarOrcamento.SERVICO_APAGAMENTO, escolhido == null ? null : escolhido.getDescricao(),
                "FAV-8.4A.4: apagamento já selecionado para receber o valor");
        digitar(tela, "campoValorProposto", "280,00");
        clicar(tela, "botaoIncluirServico");
        sincronizar();
        incluirPorDescricao("Confecção de chave canivete com transponder", "360");
        verificarIgual("R$ 640,00", texto(tela, "rotuloValorEstimado"), "FAV-8.4A.6: valor estimado recalculado");

        // FA-11B
        Object[] confeccao = naEdt(() -> tela.itensDaTela().get(1));
        SwingUtilities_invokeLater(() -> tela.editarValorItem(confeccao));
        JDialog edicao = esperarDialogo("Alterar valor proposto");
        digitar(edicao, "campoNovoValor", "-0");
        verificarContem(VAL, textoDoDialogo(edicao), "FA-11B: valor zero recusado (o \"-\" nem entra)");
        digitar(edicao, "campoNovoValor", "350,00");
        salvarJanela(edicao, "tela2-fa11b-editar.png");
        clicarNoDialogo(edicao, "Atualizar valor");
        verificarIgual("R$ 630,00", texto(tela, "rotuloValorEstimado"), "FA-11B.2: valor estimado recalculado");

        // FA-11A
        SwingUtilities_invokeLater(() -> tela.removerItem(confeccao));
        JDialog remocao = esperarDialogo("Remover serviço do orçamento?");
        verificarContem("Novo valor estimado: R$ 280,00", textoDoDialogo(remocao), "FA-11A.2: confirmação com o novo valor");
        salvarJanela(remocao, "tela2-fa11a-remover.png");
        clicarNoDialogo(remocao, "Manter");
        verificarIgual("R$ 630,00", texto(tela, "rotuloValorEstimado"), "FA-11A: Manter não remove");
        SwingUtilities_invokeLater(() -> tela.removerItem(confeccao));
        remocao = esperarDialogo("Remover serviço do orçamento?");
        clicarNoDialogo(remocao, "Remover item");
        verificarIgual("R$ 280,00", texto(tela, "rotuloValorEstimado"), "FA-11A.4: removido e recalculado");
        incluirPorDescricao("Confecção de chave canivete com transponder", "360,00");

        digitar(tela, "campoObservacoes", "Atendimento na oficina. Documento de propriedade conferido.");
        salvarFormulario(tela, conteudo, "tela1-v2-preenchida.png");
        clicar(tela, "botaoRegistrar");
        JDialog sucesso = esperarDialogo("Orçamento registrado com sucesso");
        verificarContem("Chaves", textoDoDialogo(sucesso), "passo 13: segmento Chaves");
        clicarNoDialogo(sucesso, "Novo orçamento");
        verificarIgual("Canivete com transponder | 2 | 1 | ID46 – PCF7946 | Bloqueado – sem chave válida | 1 | "
                        + "CRLV-e nº 01928374651 | Fernanda Lopes Barbosa | RG 1.982.336 SSP/MS | 640.0 | Remoto | 3",
                BancoDeTeste.linha("SELECT c.tipoChave, c.quantidadeCopias, c.possuiTelecomando, c.codigoTransponder, "
                        + "c.situacaoImobilizador, c.perdaTotal, c.documentoPropriedade, c.nomeSolicitante, "
                        + "c.documentoSolicitante, o.valorEstimado, o.origemAtendimento, o.idUsuario "
                        + "FROM TB_OrcamentoChaves c JOIN TB_Orcamento o ON o.idOrcamento = c.idOrcamento "
                        + "WHERE o.numero = 'ORC-2026-0234'"), "1.4, 1.6: TB_OrcamentoChaves");
        verificarIgual("Ricardo Teixeira Lima | None | (67) 98877-1020",
                BancoDeTeste.linha("SELECT nome, COALESCE(cpfCnpj, 'None'), telefone FROM TB_Cliente "
                        + "WHERE nome = 'Ricardo Teixeira Lima'"), "FA-02: TB_Cliente (sem CPF/CNPJ)");

        // O veículo cadastrado agora aparece vinculado à cliente (RN-02).
        digitar(tela, "campoValidade", emDias(10));
        clicar(tela, "radioPresencial");
        digitar(tela, "campoCliente", "(67) 98877-1020");
        verificarIgual("Ricardo Teixeira Lima", texto(tela, "campoCliente"), "busca por telefone");
        JComboBox<?> veiculos = achar(tela, "comboVeiculo");
        verificarIgual("MTZ-5F31", naEdt(() -> veiculos.getItemCount() == 1 ? veiculos.getItemAt(0) : null),
                "RN-02: veículo vinculado pelo orçamento");
    }

    /** Cada campo digitável: inválido e válido, ao perder o foco; e a revalidação no envio. */
    void validacoesDosCampos() throws Exception {
        abrir(1);
        String validade = ControladoraSolicitarOrcamento.MSG_VALIDADE_INVALIDA;
        digitar(tela, "campoValidade", "31/02/2027");
        verificarIgual(validade, tela.mensagemDoCampo("Válido até"), "Válido até: 31/02 não existe");
        digitar(tela, "campoValidade", "2027-01-10");
        verificarIgual("20270110", texto(tela, "campoValidade"), "Válido até: hífen não entra");
        verificarIgual(validade, tela.mensagemDoCampo("Válido até"), "Válido até: formato não brasileiro");
        digitar(tela, "campoValidade", "dez/12/2027");
        verificarIgual("/12/2027", texto(tela, "campoValidade"), "Válido até: letras não entram");
        digitar(tela, "campoValidade", emDias(-1));
        verificarIgual(validade, tela.mensagemDoCampo("Válido até"), "Válido até: anterior à solicitação (RN-07)");
        digitar(tela, "campoValidade", "");
        verificarIgual(validade, tela.mensagemDoCampo("Válido até"), "Válido até: obrigatório");
        digitar(tela, "campoValidade", emDias(15).replace("/", ""));
        verificarIgual(emDias(15), texto(tela, "campoValidade"), "Válido até: data sem barras ganha as barras");
        verificarIgual(null, tela.mensagemDoCampo("Válido até"), "Válido até: data sem barras é aceita");
        digitar(tela, "campoValidade", emDias(0));
        verificarIgual(null, tela.mensagemDoCampo("Válido até"), "Válido até: igual à solicitação é aceito");

        digitar(tela, "campoCliente", "024.577.310-45");
        verificarIgual(Validacoes.MSG_CPF_INVALIDO, tela.mensagemDoCampo("Cliente"), "Cliente: CPF do mockup (DV errado)");
        digitar(tela, "campoCliente", "111.111.111-11");
        verificarIgual(Validacoes.MSG_CPF_INVALIDO, tela.mensagemDoCampo("Cliente"), "Cliente: CPF com dígitos iguais");
        digitar(tela, "campoCliente", "11.222.333/0001-80");
        verificarIgual(Validacoes.MSG_CNPJ_INVALIDO, tela.mensagemDoCampo("Cliente"), "Cliente: CNPJ com DV errado");
        digitar(tela, "campoCliente", "(67) 8122-673");
        verificarIgual(Validacoes.MSG_TELEFONE_INVALIDO, tela.mensagemDoCampo("Cliente"), "Cliente: telefone incompleto");
        digitar(tela, "campoCliente", "");
        verificarIgual(Validacoes.MSG_OBRIGATORIO, tela.mensagemDoCampo("Cliente"), "Cliente: obrigatório");
        digitar(tela, "campoCliente", "11.222.333/0001-81");
        verificarIgual("Selecione a origem do atendimento.", tela.mensagemDoCampo("Origem do atendimento"),
                "passo 2: a origem é pedida junto com o cliente");
        clicar(tela, "radioPresencial");
        sincronizar();
        verificarIgual("Oficina Center Car", texto(tela, "campoCliente"), "Cliente: CNPJ válido encontra");

        escreverNaCombo(tela, "comboVeiculo", "RT-2J45");
        verificarIgual(Validacoes.MSG_PLACA_INVALIDA, tela.mensagemDoCampo("Veículo (placa)"), "Placa: formato inválido");
        escreverNaCombo(tela, "comboVeiculo", "ZZZ-9Z99");
        verificarContem("Veículo não encontrado", tela.mensagemDoCampo("Veículo (placa)"), "Placa: sem cadastro (FA-04)");
        escreverNaCombo(tela, "comboVeiculo", "rtb2j45");
        verificarIgual("Fiat Uno Way", texto(tela, "campoMarcaModelo"), "Placa: minúscula e sem hífen é aceita");

        clicar(tela, "radioChaves");
        sincronizar();
        digitar(tela, "campoQuantidade", "0");
        verificarIgual(Validacoes.MSG_QUANTIDADE_INVALIDA, tela.mensagemDoCampo("Qtd. de cópias"), "Qtd.: zero");
        digitar(tela, "campoQuantidade", "dois");
        verificarIgual("", texto(tela, "campoQuantidade"), "Qtd.: letras não entram no campo");
        digitar(tela, "campoQuantidade", "2a5");
        verificarIgual("25", texto(tela, "campoQuantidade"), "Qtd.: só os dígitos ficam");
        digitar(tela, "campoQuantidade", "");
        verificarIgual(Validacoes.MSG_OBRIGATORIO, tela.mensagemDoCampo("Qtd. de cópias"), "Qtd.: obrigatória");
        digitar(tela, "campoQuantidade", "3");
        verificarIgual(null, tela.mensagemDoCampo("Qtd. de cópias"), "Qtd.: 3 aceita");
        escreverNaCombo(tela, "comboTipoChave", "");
        verificarIgual(Validacoes.MSG_OBRIGATORIO, tela.mensagemDoCampo("Tipo de chave"), "Tipo de chave: obrigatório");
        escreverNaCombo(tela, "comboTipoChave", "Chave com transponder");
        verificarIgual(null, tela.mensagemDoCampo("Tipo de chave"), "Tipo de chave: preenchido");
        escreverNaCombo(tela, "comboSituacaoImobilizador", "");
        verificarIgual(Validacoes.MSG_OBRIGATORIO, tela.mensagemDoCampo("Situação do imobilizador"),
                "Situação do imobilizador: obrigatória");
        escreverNaCombo(tela, "comboSituacaoImobilizador", "Funcionando – com chave válida");
        digitar(tela, "campoTransponder", "x".repeat(300));
        verificarContem("Máximo de 255", tela.mensagemDoCampo("Código do transponder"), "Transponder: tamanho");
        digitar(tela, "campoTransponder", "ID48");
        verificarIgual(null, tela.mensagemDoCampo("Código do transponder"), "Transponder: válido");
        clicar(tela, "checkPerdaTotal");
        sincronizar();
        digitar(tela, "campoDocumentoPropriedade", "  ");
        verificarIgual(Validacoes.MSG_OBRIGATORIO, tela.mensagemDoCampo("Documento de propriedade (CRLV)"),
                "CRLV: obrigatório na perda total");
        clicar(tela, "checkPerdaTotal");
        sincronizar();
        verificarIgual(null, tela.mensagemDoCampo("Documento de propriedade (CRLV)"), "CRLV: dispensado sem perda total");

        // Valor proposto (FA-09A)
        digitar(tela, "campoValorProposto", "12,345");
        verificarIgual(VAL, tela.mensagemDoCampo("Valor proposto (R$)"), "Valor: três casas decimais");
        digitar(tela, "campoValorProposto", "1,234.56");
        verificarIgual(VAL, tela.mensagemDoCampo("Valor proposto (R$)"), "Valor: formato americano");
        digitar(tela, "campoValorProposto", "-50,00");
        verificarIgual("50,00", texto(tela, "campoValorProposto"), "Valor: sinal de menos não entra (negativo impossível)");
        digitar(tela, "campoValorProposto", "R$ 9x9");
        verificarIgual("99", texto(tela, "campoValorProposto"), "Valor: letras e símbolos não entram");
        digitar(tela, "campoValorProposto", "1.250,50");
        verificarIgual(null, tela.mensagemDoCampo("Valor proposto (R$)"), "Valor: 1.250,50 aceito");
        escolherNaCombo(tela, "comboServico", 0);
        digitar(tela, "campoValorProposto", "0,00");
        clicar(tela, "botaoIncluirServico");
        sincronizar();
        verificarIgual(VAL, tela.mensagemDoCampo("Valor proposto (R$)"), "FA-09A.1: 0,00 recusado ao incluir");
        salvarFormulario(tela, conteudo, "tela3-fa09a-valor.png");
        verificarIgual(0, naEdt(() -> tela.itensDaTela().size()), "FA-09A: nada incluído");
        digitar(tela, "campoValorProposto", "");
        verificarIgual(VAL, tela.mensagemDoCampo("Valor proposto (R$)"), "Valor: obrigatório com serviço escolhido");
        digitar(tela, "campoValorProposto", "130");
        clicar(tela, "botaoIncluirServico");
        sincronizar();
        verificarIgual(1, naEdt(() -> tela.itensDaTela().size()), "FA-09A.3: novo valor aceito e incluído");

        // FA-09B: um serviço de Módulos oferecido com o segmento Chaves.
        JComboBox<ServicoBase> servicos = achar(tela, "comboServico");
        naEdt(() -> {
            servicos.addItem(new ServicoBase(1, "Diagnóstico em bancada com simulação de sinais", "Módulos", null));
            servicos.setSelectedIndex(servicos.getItemCount() - 1);
        });
        digitar(tela, "campoValorProposto", "180,00");
        clicar(tela, "botaoIncluirServico");
        sincronizar();
        verificarIgual(ControladoraSolicitarOrcamento.MSG_FORA_DO_SEGMENTO, tela.mensagemDoCampo("Serviço base"),
                "FA-09B.1: serviço fora do segmento");
        verificar(visivel(tela, "tabelaItens") && naEdt(() -> tela.itensDaTela().size()) == 1,
                "FA-09B.2: itens já incluídos preservados");
        salvarFormulario(tela, conteudo, "tela3-fa09b-fora-do-segmento.png");

        // Observações
        digitar(tela, "campoObservacoes", "o".repeat(256));
        verificarContem("Máximo de 255", tela.mensagemDoCampo("Observações"), "Observações: tamanho");
        digitar(tela, "campoObservacoes", "ok");
        verificarIgual(null, tela.mensagemDoCampo("Observações"), "Observações: válida");

        // (b) revalidação no envio: um campo inválido digitado sem sair dele.
        JComponent campo = achar(tela, "campoQuantidade");
        naEdt(() -> ((javax.swing.JTextField) campo).setText("0"));
        clicar(tela, "botaoRegistrar");
        sincronizar();
        verificarIgual(Validacoes.MSG_QUANTIDADE_INVALIDA, tela.mensagemDoCampo("Qtd. de cópias"),
                "envio: revalida o campo que não perdeu o foco");
        verificarContem("Qtd. de cópias: " + Validacoes.MSG_QUANTIDADE_INVALIDA, texto(tela, "caixaPendencias") == null ? ""
                : textoPendencias(), "envio: pendência listada");
        verificarIgual(0, BancoDeTeste.contar("SELECT COUNT(*) FROM TB_Orcamento WHERE numero = 'ORC-2026-0234'"),
                "envio: nada gravado com pendência");
    }

    /** FA-12A, FA-12B, FA-12C e FA-12D. */
    void fluxosDoPasso12() throws Exception {
        abrir(1);
        clicar(tela, "botaoRegistrar");
        sincronizar();
        verificar(visivel(tela, "caixaPendencias"), "FA-12B: caixa de pendências com o formulário vazio");

        preencherCabecalhoMarcos(emDias(5));
        clicar(tela, "radioModulos");
        sincronizar();
        escreverNaCombo(tela, "comboTipoModulo", "ECU – Injeção Eletrônica");
        clicar(tela, "botaoRegistrar");
        sincronizar();
        String pendencias = textoPendencias();
        verificarContem("Corrija 2 pendências antes de registrar:", pendencias, "FA-12B.1: total de pendências");
        verificarContem("Modelo da ECU: campo obrigatório para o segmento Módulos.", pendencias, "FA-12B.1: texto da Tela 3");
        verificarContem("Sintoma relatado pelo cliente: campo obrigatório para o segmento Módulos.", pendencias,
                "FA-12B.1: texto da Tela 3");
        verificar(visivel(tela, "avisoAcrescentado") == false, "sem aviso de chaves no V1");
        salvarFormulario(tela, conteudo, "tela3-fa12a-fa12b.png");
        verificar(visivel(tela, "caixaPendencias"), "FA-12A/B exibidos juntos");

        escreverNaCombo(tela, "comboModeloECU", "Bosch ME7.5.30");
        digitar(tela, "campoSintoma", "Falha na partida.");
        clicar(tela, "botaoRegistrar");
        sincronizar();
        verificar(!visivel(tela, "caixaPendencias"), "FA-12B.3: pendências resolvidas");
        verificar(textoErroSemItensVisivel(), "FA-12A.1: \"O orçamento deve conter ao menos um serviço.\"");

        incluir(0, "150");
        digitar(tela, "campoValidade", emDias(-3));
        clicar(tela, "botaoRegistrar");
        sincronizar();
        verificarIgual(ControladoraSolicitarOrcamento.MSG_VALIDADE_INVALIDA, tela.mensagemDoCampo("Válido até"),
                "FA-12C.1: validade anterior");
        salvarFormulario(tela, conteudo, "tela3-fa12c-validade.png");
        verificarIgual(0, BancoDeTeste.contar("SELECT COUNT(*) FROM TB_Orcamento WHERE numero = 'ORC-2026-0234'"),
                "FA-12C: nada registrado");

        // FA-12D
        clicar(tela, "botaoRascunho");
        JDialog rascunho = esperarDialogo(ControladoraSolicitarOrcamento.MSG_RASCUNHO);
        salvarJanela(rascunho, "tela2-fa12d-rascunho.png");
        clicarNoDialogo(rascunho, "Ir para a lista de orçamentos");
        JDialog lista = esperarDialogo("Orçamentos");
        verificarContem("Rascunho", textoDaTabela(lista), "FA-12D.3: rascunho na lista de orçamentos");
        clicarNoDialogo(lista, "Fechar");
        verificarIgual("Rascunho | None | None | 150.0 | 1",
                BancoDeTeste.linha("SELECT status, COALESCE(numero, 'None'), COALESCE(dataValidade, 'None'), "
                        + "valorEstimado, (SELECT COUNT(*) FROM TB_ItemOrcamento i WHERE i.idOrcamento = o.idOrcamento) "
                        + "FROM TB_Orcamento o WHERE status = 'Rascunho'"),
                "FA-12D.2: sem número definitivo e sem prazo de validade (RN-06, RN-07, RN-08)");
        verificarIgual("ORC-2026-0234", texto(tela, "campoNumero"), "o rascunho não consome número (RN-08)");
    }

    /** FAV-8.1 e o desmarcar do FAV-8.2. */
    void etiquetaIlegivel() throws Exception {
        abrir(2);
        preencherCabecalhoMarcos(emDias(7));
        clicar(tela, "radioModulos");
        sincronizar();
        escreverNaCombo(tela, "comboTipoModulo", "BCM – Módulo de Carroceria");
        digitar(tela, "campoSintoma", "Vidros elétricos não respondem.");
        clicar(tela, "checkIlegivel");
        sincronizar();
        verificarIgual("Dispensado", texto(tela, "campoModeloDispensado"), "FAV-8.1.2: modelo da ECU dispensado");
        verificar(visivel(tela, "caixaFoto") && visivel(tela, "campoDescricaoPlaca"),
                "FAV-8.1.2: foto e descrição exigidas");
        clicar(tela, "checkBancada");
        sincronizar();
        clicar(tela, "checkBancada");   // desmarca: o diagnóstico pendente não vale mais
        sincronizar();
        incluir(0, "200");
        clicar(tela, "botaoRegistrar");
        sincronizar();
        String pendencias = textoPendencias();
        verificarContem("Registro fotográfico do módulo: campo obrigatório para o segmento Módulos.", pendencias,
                "FAV-8.1: foto obrigatória");
        verificarContem("Descrição física da placa: campo obrigatório para o segmento Módulos.", pendencias,
                "FAV-8.1: descrição obrigatória");
        verificar(!pendencias.contains("Modelo da ECU"), "FAV-8.1: modelo da ECU não é cobrado");

        File foto = File.createTempFile("modulo_qaf7c21", ".jpg");
        foto.deleteOnExit();
        chamarPrivado(tela, "anexarFoto", File.class, foto);
        digitar(tela, "campoDescricaoPlaca", "Carcaça de alumínio, conector preto de 2 vias (80 pinos); etiqueta queimada.");
        salvarFormulario(tela, conteudo, "tela3-fav81-ilegivel.png");
        clicar(tela, "botaoRegistrar");
        JDialog sucesso = esperarDialogo("Orçamento registrado com sucesso");
        clicarNoDialogo(sucesso, "Novo orçamento");
        verificarIgual("1 | " + foto.getAbsolutePath() + " | 0 | BCM – Módulo de Carroceria | None | None | 1",
                BancoDeTeste.linha("SELECT m.identificacaoIlegivel, m.registroFotografico, m.diagnosticoPendenteBancada, "
                        + "e.tipoModulo, COALESCE(e.modeloECU, 'None'), COALESCE(e.identificacaoModulo, 'None'), e.idVeiculo "
                        + "FROM TB_OrcamentoModulos m JOIN TB_Orcamento o ON o.idOrcamento = m.idOrcamento "
                        + "JOIN TB_ModuloECU e ON e.idModuloECU = m.idModuloECU WHERE o.numero = 'ORC-2026-0234'"),
                "1.5 setModuloIlegivel; 1.8 módulo criado; 1.9 ligado ao veículo; bancada desmarcada = 0");
    }

    /** FA-13: base de dados indisponível na gravação. */
    void falhaDeGravacao() throws Exception {
        abrir(1);
        preencherCabecalhoMarcos(emDias(5));
        clicar(tela, "radioModulos");
        sincronizar();
        escreverNaCombo(tela, "comboTipoModulo", "ECU – Injeção Eletrônica");
        escreverNaCombo(tela, "comboModeloECU", "Bosch ME17.9.65");
        escreverNaCombo(tela, "comboIdentificacaoModulo", "BS-0001");
        digitar(tela, "campoSintoma", "Motor falhando.");
        incluir(0, "150");

        executar("sudo", "service", "mariadb", "stop");
        clicar(tela, "botaoRegistrar");
        JDialog erro = esperarDialogo("Erro ao registrar");
        verificarContem(ControladoraSolicitarOrcamento.MSG_FALHA_GRAVACAO, textoDoDialogo(erro), "FA-13.1: mensagem");
        salvarJanela(erro, "tela2-fa13-falha.png");
        clicarNoDialogo(erro, "Voltar ao formulário");
        verificarIgual("Motor falhando.", texto(tela, "campoSintoma"), "FA-13.2: dados preservados");
        executar("sudo", "service", "mariadb", "start");
        esperar(1500);

        clicar(tela, "botaoRegistrar");
        JDialog sucesso = esperarDialogo("Orçamento registrado com sucesso");
        clicarNoDialogo(sucesso, "Novo orçamento");
        verificarIgual(1, BancoDeTeste.contar("SELECT COUNT(*) FROM TB_ModuloECU WHERE identificacaoModulo = 'BS-0001'"),
                "FA-13 → passo 12: nova tentativa grava o módulo uma vez só");
    }

    /** FA-00: desistência. */
    void desistencia() throws Exception {
        abrir(1);
        preencherCabecalhoMarcos(emDias(5));
        clicar(tela, "botaoDesistir");
        JDialog confirmacao = esperarDialogo("Desistir deste orçamento?");
        salvarJanela(confirmacao, "tela2-fa00-desistir.png");
        clicarNoDialogo(confirmacao, "Continuar editando");
        verificarIgual("Marcos Antônio Ribeiro", texto(tela, "campoCliente"), "FA-00: continuar editando preserva");
        clicar(tela, "botaoDesistir");
        confirmacao = esperarDialogo("Desistir deste orçamento?");
        clicarNoDialogo(confirmacao, "Desistir");
        JDialog lista = esperarDialogo("Orçamentos");
        verificarContem("Registro cancelado.", textoDoDialogo(lista), "FA-00.4: listagem de orçamentos");
        verificarContem("ORC-2026-0231", textoDaTabela(lista), "FA-00.4: orçamentos existentes listados");
        clicarNoDialogo(lista, "Fechar");
        verificarIgual("", texto(tela, "campoCliente"), "FA-00.5: formulário descartado");
        verificarIgual(10, BancoDeTeste.contar("SELECT COUNT(*) FROM TB_Orcamento"), "FA-00: nada gravado");
    }

    /** O momento (a) com a perda de foco real do Swing (teclado/mouse), não simulada. */
    void focoReal() throws Exception {
        abrir(1);
        javax.swing.JTextField validade = achar(tela, "campoValidade");
        javax.swing.JTextField observacoes = achar(tela, "campoObservacoes");
        naEdt(() -> tela.toFront());
        naEdt(() -> validade.requestFocusInWindow());
        esperar(500);
        naEdt(() -> validade.setText("30/02/2027"));
        naEdt(() -> observacoes.requestFocusInWindow());
        esperar(500);
        sincronizar();
        verificarIgual(ControladoraSolicitarOrcamento.MSG_VALIDADE_INVALIDA, tela.mensagemDoCampo("Válido até"),
                "foco real: Válido até validado ao sair do campo");
        naEdt(() -> validade.requestFocusInWindow());
        esperar(300);
        naEdt(() -> validade.setText(emDias(3)));
        naEdt(() -> observacoes.requestFocusInWindow());
        esperar(500);
        sincronizar();
        verificarIgual(null, tela.mensagemDoCampo("Válido até"), "foco real: erro some com data válida");
        salvarJanela(tela, "tela1-janela-inicial.png");

        // Digitação real no campo Cliente: a lista abre sem tirar o foco do campo,
        // as setas escolhem e o Enter confirma.
        clicar(tela, "radioRemoto");
        javax.swing.JTextField cliente = achar(tela, "campoCliente");
        naEdt(() -> cliente.requestFocusInWindow());
        esperar(400);
        java.awt.Robot robo = new java.awt.Robot();
        robo.setAutoDelay(60);
        for (int tecla : new int[]{java.awt.event.KeyEvent.VK_A, java.awt.event.KeyEvent.VK_N}) {
            robo.keyPress(tecla);
            robo.keyRelease(tecla);
        }
        esperar(700);
        sincronizar();
        javax.swing.JPopupMenu popup = campoPrivado(tela, "popupClientes");
        verificar(naEdt(popup::isVisible), "digitando \"an\": a lista de clientes abre");
        verificar(naEdt(cliente::isFocusOwner), "a lista não tira o foco do campo");
        salvarJanela(tela, "tela1-lista-de-clientes.png");
        robo.keyPress(java.awt.event.KeyEvent.VK_ENTER);
        robo.keyRelease(java.awt.event.KeyEvent.VK_ENTER);
        esperar(700);
        sincronizar();
        verificar(!naEdt(popup::isVisible), "Enter escolhe e fecha a lista");
        verificarIgual("Ana Paula Moreira", texto(tela, "campoCliente"), "cliente escolhido pelo teclado");
    }

    // =================================================================
    // Apoio
    // =================================================================

    private void abrir(int usuario) throws Exception {
        BancoDeTeste.recriar();
        tela = naEdt(() -> {
            TelaSolicitarOrcamento nova = new TelaSolicitarOrcamento(usuario);
            nova.setVisible(true);
            return nova;
        });
        conteudo = campoPrivado(tela, "conteudo");
        esperar(600);
        sincronizar();
    }

    private void fechar() {
        if (tela != null) {
            naEdt(() -> tela.dispose());
        }
    }

    private void preencherCabecalhoMarcos(String validade) {
        digitar(tela, "campoValidade", validade);
        clicar(tela, "radioPresencial");
        digitar(tela, "campoCliente", "02457731003");
        escolherNaCombo(tela, "comboVeiculo", 0);
    }

    private void incluir(int indice, String valor) {
        escolherNaCombo(tela, "comboServico", indice);
        digitar(tela, "campoValorProposto", valor);
        clicar(tela, "botaoIncluirServico");
        sincronizar();
    }

    private void incluirPorDescricao(String descricao, String valor) {
        JComboBox<ServicoBase> servicos = achar(tela, "comboServico");
        int indice = naEdt(() -> {
            for (int i = 0; i < servicos.getItemCount(); i++) {
                if (servicos.getItemAt(i).getDescricao().equals(descricao)) {
                    return i;
                }
            }
            return -1;
        });
        incluir(indice, valor);
    }

    private String textoPendencias() {
        javax.swing.JLabel rotulo = campoPrivado(tela, "textoPendencias");
        return naEdt(() -> rotulo.getText().replace("<br>", "\n").replaceAll("<[^>]+>", ""));
    }

    private boolean textoErroSemItensVisivel() {
        JComponent erro = campoPrivado(tela, "erroSemItens");
        return naEdt(erro::isShowing);
    }

    private String textoDaTabela(JDialog dialogo) {
        javax.swing.JTable tabela = achar(dialogo.getContentPane(), "tabelaOrcamentos");
        return naEdt(() -> {
            StringBuilder texto = new StringBuilder();
            for (int l = 0; l < tabela.getRowCount(); l++) {
                for (int c = 0; c < tabela.getColumnCount(); c++) {
                    texto.append(tabela.getValueAt(l, c)).append(" | ");
                }
                texto.append('\n');
            }
            return texto.toString();
        });
    }

    private static void SwingUtilities_invokeLater(Runnable acao) {
        javax.swing.SwingUtilities.invokeLater(acao);
        esperar(300);
    }

    private static void executar(String... comando) throws Exception {
        new ProcessBuilder(comando).inheritIO().start().waitFor();
    }

    private static String emDias(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, dias);
        return DATA.format(c.getTime());
    }

    private static String isoEmDias(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, dias);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    private static String hojeIso() {
        return isoEmDias(0);
    }
}
