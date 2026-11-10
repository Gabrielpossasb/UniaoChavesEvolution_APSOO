"""Recalcula os números de linha das tabelas 4.1, 4.2 e 4.3 do RELATORIO_ITERACAO2.md a partir do código."""
import re
R = 'RELATORIO_ITERACAO2.md'
B = 'Implementacao/registrar-ordem-servico/src/uniaoevolutions/'
ctrl = open(B + 'controle/ControladoraSolicitarOrcamento.java', encoding='utf-8').read().split('\n')
tela = open(B + 'view/TelaSolicitarOrcamento.java', encoding='utf-8').read().split('\n')
ros = open(B + 'controle/ControladoraRegistrarOS.java', encoding='utf-8').read().replace('\r', '').split('\n')
def linha(linhas, padrao, depois=0):
    for i, l in enumerate(linhas, 1):
        if i > depois and re.search(padrao, l):
            return i
    raise SystemExit('não achei: ' + padrao)
reg = linha(ctrl, r'public Orcamento registrarOrcamento\(')
t41 = {
 '(pré-condições do contrato: RN-01, RN-04, RN-07)': linha(ctrl, r'verificarPreCondicoes\(dataValidade\);'),
 '1 `registrarOrcamento(dataValidade, observacoes)`': reg,
 '1.1 `tec = buscarUsuario(idUsuarioLogado)`': linha(ctrl, r'^        tec = usuarioDAO\.buscarUsuario', reg),
 '1.2 `numero = gerarNumeroOrcamento()`': linha(ctrl, r'String numero = orcamentoDAO\.gerarNumeroOrcamento', reg),
 '1.3 `[seg="Modulos"] <<create>> OrcamentoModulos(…)`': linha(ctrl, r'orc = new OrcamentoModulos\(numero', reg),
 '1.4 `[seg="Chaves"] <<create>> OrcamentoChaves(…)`': linha(ctrl, r'orc = new OrcamentoChaves\(numero', reg),
 '1.5 `[identificacaoIlegivel] setModuloIlegivel(…)`': linha(ctrl, r'\(\(OrcamentoModulos\) orc\)\.setModuloIlegivel', reg),
 '1.6 `[perdaTotal] setComprovacaoPropriedade(…)`': linha(ctrl, r'\(\(OrcamentoChaves\) orc\)\.setComprovacaoPropriedade', reg),
 '1.7 `[seg="Modulos"] mod = buscarModulo(vei, identificacaoModulo)`': linha(ctrl, r'ModuloECU mod = moduloECUDAO\.buscarModulo', reg),
 '1.8 `[mod == null] <<create>> ModuloECU(…)`': linha(ctrl, r'mod = new ModuloECU\(', reg),
 '1.9 `[mod criado] associarModulo(mod)` → vei': linha(ctrl, r'vei\.associarModulo\(mod\)', reg),
 '1.10 `[seg="Modulos"] associarModulo(mod)` → orc': linha(ctrl, r'\(\(OrcamentoModulos\) orc\)\.associarModulo', reg),
 '1.11 `[para cada item] adicionarItem(item)`': linha(ctrl, r'orc\.adicionarItem\(item\)', reg),
 '1.12 `associarCliente(cli)`': linha(ctrl, r'orc\.associarCliente\(cli\)', reg),
 '1.13 `associarVeiculo(vei)`': linha(ctrl, r'orc\.associarVeiculo\(vei\)', reg),
 '1.14 `associarTecnico(tec)`': linha(ctrl, r'orc\.associarTecnico\(tec\)', reg),
 '1.15 `sinalizarRegistro()`': linha(ctrl, r'orc\.sinalizarRegistro\(\)', reg),
 '(pós-condição "orc foi persistido")': linha(ctrl, r'orcamentoDAO\.salvar\(orc\)', reg),
}
s = open(R, encoding='utf-8').read()
for k, v in t41.items():
    s, n = re.subn(r'(\| ' + re.escape(k) + r' \|[^\n]*\| )\d+( \|)', r'\g<1>' + str(v) + r'\g<2>', s); assert n == 1, k
def op(nome):
    return linha(ctrl, r'^    public [\w<>]+ ' + nome + r'\(')
def chamadas(nome):
    return [i for i, l in enumerate(tela, 1) if re.search(r'controladora\.' + nome + r'\(', l)]
t42 = {'iniciarOrcamento':'`iniciarOrcamento(): Orcamento`', 'informarCliente':'`informarCliente(origemAtendimento, cpfCnpjCliente): Cliente`',
 'cadastrarCliente':'`cadastrarCliente(nome, telefone): Cliente`', 'selecionarVeiculo':'`selecionarVeiculo(placa): Veiculo`',
 'cadastrarVeiculo':'`cadastrarVeiculo(placa, marca, modelo, anoModelo, motorizacao, numeroChassi): Veiculo`',
 'informarSegmento':'`informarSegmento(segmento): List<ServicoBase>`',
 'informarDadosModulo':'`informarDadosModulo(tipoModulo, modeloECU, identificacaoModulo, sintomaRelatado): List<Orcamento>`',
 'informarIdentificacaoIlegivel':'`informarIdentificacaoIlegivel(tipoModulo, sintomaRelatado)`',
 'informarDescricaoModulo':'`informarDescricaoModulo(registroFotografico, descricaoFisicaPlaca): List<Orcamento>`',
 'informarDiagnosticoPendente':'`informarDiagnosticoPendente()`', 'informarDadosChaves':'`informarDadosChaves(…6 parâmetros…): String`',
 'informarComprovacaoPropriedade':'`informarComprovacaoPropriedade(documentoPropriedade, nomeSolicitante, documentoSolicitante): ServicoBase`',
 'incluirItem':'`incluirItem(idServicoBase, valorProposto): double`', 'removerItem':'`removerItem(idItem): double`',
 'alterarValorItem':'`alterarValorItem(idItem, novoValorProposto): double`', 'registrarOrcamento':'`registrarOrcamento(dataValidade, observacoes): Orcamento`',
 'salvarRascunhoOrcamento':'`salvarRascunhoOrcamento(observacoes)`', 'solicitarConfirmacaoDesistencia':'`solicitarConfirmacaoDesistencia(): String`',
 'descartarOrcamento':'`descartarOrcamento(): List<Orcamento>`', 'consultarVeiculosVinculados':'`consultarVeiculosVinculados()`',
 'consultarClientes':'`consultarClientes(trecho)`', 'consultarModulosDoVeiculo':'`consultarModulosDoVeiculo()`',
 'consultarHistoricoTecnico':'`consultarHistoricoTecnico()`'}
for nome, rot in t42.items():
    cs = chamadas(nome)
    if nome == 'descartarOrcamento':
        txt = f'{cs[-1]} (e {cs[0]}, FA-12D)'
    else:
        txt = ', '.join(map(str, cs))
    s, n = re.subn(r'(\| ' + re.escape(rot) + r' \| )[^|]+\| [^|]+\|', r'\g<1>' + f'{op(nome)} | {txt} |', s); assert n == 1, nome
s, n = re.subn(r'(Técnico → Interface, sem mensagem ao Sistema \| — \| )\d+', r'\g<1>' + str(linha(tela, r'DSS 21')), s); assert n == 1
rr = linha(ros, r'public OrdemDeServico registrarOrdemServico\(')
t43 = {'1.1 `orc = buscarOrcamento(orcamentoSelecionado)`': r'orcamentoDAO\.buscarOrcamento\(orcamentoSelecionado\)',
 '1.2 `vei = buscarVeiculo(orc)` (`:VeiculoDAO`)': r'Veiculo vei = orc\.getVeiculo', '1.3 `tec = buscarTecnico(tecnicoSelecionado)` (`:TecnicoDAO`)': r'Usuario tec = usuarioDAO',
 '1.4 `seg = getSegmentoPretendido()`': r'String seg = orc', '1.5 `[seg="Modulos"] <<create>> OrdemDeServicoModulos(…)` / 1.6 `[seg="Chaves"] <<create>> OrdemDeServicoChaves(…)`': r'OrdemDeServico os = criarOrdemDeServico',
 '1.7 `sinalizarAbertura()` → 1.7.1 `setDataAbertura()`, 1.7.2 `setStatusOs("Aberta")`': r'os\.sinalizarAbertura\(\)',
 '1.8 `associaOS(os)` → orc': r'os\.associarOrcamento\(orc\)', '1.9 `associaOS(os)` → vei': r'os\.associarVeiculo\(vei\)',
 '1.10 `associaOS(os)` → tec': r'os\.associarTecnico\(tec\)', '(RN-03, RN-04: não é mensagem do diagrama)': r'os\.validar\(\)',
 '1.11 `salvar(os)`': r'ordemServicoDAO\.salvar\(os\)'}
for k, p in t43.items():
    s, n = re.subn(r'(\| ' + re.escape(k) + r' \|[^\n]*\| )\d+( \|)', r'\g<1>' + str(linha(ros, p, rr)) + r'\g<2>', s); assert n == 1, k
open(R, 'w', encoding='utf-8').write(s)
print('linhas atualizadas')
