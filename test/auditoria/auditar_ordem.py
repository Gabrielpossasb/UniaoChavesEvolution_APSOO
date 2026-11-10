import re, sys, os
BASE = 'Implementacao/registrar-ordem-servico/src/uniaoevolutions/'
# Ordem do diagrama de classes v2 (2ª iteração), lida da imagem e das strings do .asta.
D = {
 'controle/ControladoraSolicitarOrcamento': (['cli','vei','tec','origemAtendimento','segmento','itens'],
   ['iniciarOrcamento','informarCliente','cadastrarCliente','selecionarVeiculo','cadastrarVeiculo','informarSegmento',
    'informarDadosModulo','informarIdentificacaoIlegivel','informarDescricaoModulo','informarDiagnosticoPendente',
    'informarDadosChaves','informarComprovacaoPropriedade','incluirItem','removerItem','alterarValorItem',
    'registrarOrcamento','salvarRascunhoOrcamento','solicitarConfirmacaoDesistencia','descartarOrcamento']),
 'controle/ControladoraRegistrarOS': ([], ['iniciarRegistroOS','consultarOrcamentosAprovados','consultarOrcamento',
    'consultarBlocosDadosTecnicos','consultarTecnicosHabilitados','registrarOrdemServico','incluirItemOrdemServico',
    'registrarOSComAlteracao','salvarRascunhoOS','solicitarConfirmacaoDesistencia','descartarRegistro']),
 'negocio/Orcamento': (['numero','dataSolicitacao','dataValidade','origemAtendimento','status','valorEstimado','observacoes','motivoCancelamento'],
   ['associarCliente','associarVeiculo','associarTecnico','adicionarItem','removerItem','sinalizarRegistro','setDataSolicitacao',
    'setStatus','calcularValorEstimado','getVeiculo','getSegmentoPretendido']),
 'negocio/OrcamentoModulos': (['sintomaRelatado','diagnosticoPendenteBancada','identificacaoIlegivel','registroFotografico','descricaoFisicaPlaca'],
   ['OrcamentoModulos','setModuloIlegivel','associarModulo']),
 'negocio/OrcamentoChaves': (['tipoChave','quantidadeCopias','possuiTelecomando','codigoTransponder','situacaoImobilizador','perdaTotal',
    'documentoPropriedade','nomeSolicitante','documentoSolicitante'], ['OrcamentoChaves','setComprovacaoPropriedade']),
 'negocio/ItemOrcamento': (['valorProposto'], ['ItemOrcamento','getValorProposto','setValorProposto']),
 'negocio/ServicoBase': (['descricao','categoria','observacoesTecnicas'], ['getCategoria']),
 'negocio/Cliente': (['nome','cpfCnpj','telefone'], ['Cliente']),
 'negocio/Veiculo': (['marca','modelo','anoModelo','motorizacao','placa','numeroChassi'], ['Veiculo','associarModulo']),
 'negocio/ModuloECU': (['tipoModulo','modeloECU','identificacaoModulo'], ['ModuloECU']),
 'negocio/Usuario': (['idUsuario','nome','tipoUsuario'], []),
 'negocio/OrdemDeServico': (['numeroOS','dataAbertura','statusOS','dataEmissaoGarantia','textoExecutadoGarantia','dataEmissaoResponsabilidade',
    'quilometragem','prazoEstimado','observacoes'], ['sinalizarAbertura','setDataAbertura','setStatusOS','associarOrcamento','associarVeiculo','associarTecnico']),
 'negocio/OrdemDeServicoModulos': (['relatoInicial','condicaoRecebimento','diagnosticoInicial','testesEntradaSaida','confirmacaoDefeito',
    'causaIdentificada','reparoRealizado'], ['OrdemDeServicoModulos']),
 'negocio/OrdemDeServicoChaves': (['servicoExecutado'], ['OrdemDeServicoChaves']),
 'persistencia/OrdemServicoDAO': ([], ['salvar','gerarNumeroOS']),
 'persistencia/UsuarioDAO': ([], ['buscarUsuario','buscarTecnicosHabilitados']),
 'persistencia/OrcamentoDAO': ([], ['buscarOrcamento','buscarOrcamentosAprovados','gerarNumeroOrcamento','salvar','salvarRascunho',
    'buscarAtendimentosModulo','listarOrcamentos']),
 'persistencia/ClienteDAO': ([], ['buscarPorCpfCnpj','salvar']),
 'persistencia/VeiculoDAO': ([], ['buscarPorPlaca','salvar']),
 'persistencia/ServicoBaseDAO': ([], ['buscarPorCategoria','buscarServico']),
 'persistencia/ModuloECUDAO': ([], ['buscarModulo']),
}
campo_re = re.compile(r'^    (?! )(?:private|protected|public)(?: static| final| transient)* [\w<>\[\], ]+? (\w+)\s*(?:=[^;]*)?;')
metodo_re = re.compile(r'^    (?! )(?:(?:public|private|protected)(?: static| final| abstract)* )?(?:[\w<>\[\],]+(?:<[^>]*>)? )?(\w+)\(')
total = 0
for nome, (attrs, ops) in D.items():
    linhas = open(BASE + nome + '.java', encoding='utf-8').read().split('\n')
    campos, metodos = [], []
    for n, l in enumerate(linhas, 1):
        m = campo_re.match(l)
        if m and '(' not in l.split('=')[0] and not re.search(r'static final', l):
            campos.append((m.group(1), n)); continue
        m = metodo_re.match(l)
        if m and m.group(1) not in ('if','for','while','switch','return','new','catch'):
            metodos.append((m.group(1), n))
    for tipo, esperado, achado in (('atributos', attrs, campos), ('métodos', ops, metodos)):
        if not esperado: continue
        ordem = [x for x in achado]
        nomes = [x[0] for x in ordem]
        falta = [e for e in esperado if e not in nomes]
        # primeira ocorrência de cada membro do diagrama
        pos = [nomes.index(e) for e in esperado if e in nomes]
        problema = []
        if falta: problema.append('FALTA ' + ', '.join(falta))
        if pos != sorted(pos): problema.append('ORDEM ERRADA: ' + ' > '.join(nomes[p] for p in pos))
        elif pos:
            entre = [nomes[i] for i in range(pos[0], pos[-1] + 1) if nomes[i] not in esperado]
            # sobrecargas do mesmo nome não contam
            if entre: problema.append('INTERCALADOS: ' + ', '.join(f'{nomes[i]}(l.{ordem[i][1]})' for i in range(pos[0], pos[-1]+1) if nomes[i] not in esperado))
        status = 'ok ' if not problema else 'XX '
        if problema: total += 1
        print(f'{status} {nome.split("/")[1]:34} {tipo:9} ' + ('; '.join(problema) if problema else 'na ordem do diagrama'))
print(f'\n{total} problema(s)')
