import re, os
BASE = 'Implementacao/registrar-ordem-servico/src/uniaoevolutions/'
# Diagrama de classes v2: atributo = 'vis nome:tipo'; método = 'vis nome(p:T, ...):Ret' (construtor sem retorno).
D = {
 'controle/ControladoraSolicitarOrcamento': [
  '- cli:Cliente','- vei:Veiculo','- tec:Usuario','- origemAtendimento:String','- segmento:String','- itens:List<ItemOrcamento>',
  '+ iniciarOrcamento():Orcamento','+ informarCliente(origemAtendimento:String,cpfCnpjCliente:String):Cliente',
  '+ cadastrarCliente(nome:String,telefone:String):Cliente','+ selecionarVeiculo(placa:String):Veiculo',
  '+ cadastrarVeiculo(placa:String,marca:String,modelo:String,anoModelo:int,motorizacao:String,numeroChassi:String):Veiculo',
  '+ informarSegmento(segmento:String):List<ServicoBase>',
  '+ informarDadosModulo(tipoModulo:String,modeloECU:String,identificacaoModulo:String,sintomaRelatado:String):List<Orcamento>',
  '+ informarIdentificacaoIlegivel(tipoModulo:String,sintomaRelatado:String):void',
  '+ informarDescricaoModulo(registroFotografico:String,descricaoFisicaPlaca:String):List<Orcamento>',
  '+ informarDiagnosticoPendente():void',
  '+ informarDadosChaves(tipoChave:String,quantidadeCopias:int,possuiTelecomando:boolean,codigoTransponder:String,situacaoImobilizador:String,perdaTotal:boolean):String',
  '+ informarComprovacaoPropriedade(documentoPropriedade:String,nomeSolicitante:String,documentoSolicitante:String):ServicoBase',
  '+ incluirItem(idServicoBase:int,valorProposto:double):double','+ removerItem(idItem:int):double',
  '+ alterarValorItem(idItem:int,novoValorProposto:double):double','+ registrarOrcamento(dataValidade:Date,observacoes:String):Orcamento',
  '+ salvarRascunhoOrcamento(observacoes:String):void','+ solicitarConfirmacaoDesistencia():String','+ descartarOrcamento():List<Orcamento>'],
 'controle/ControladoraRegistrarOS': [
  '+ iniciarRegistroOS():OrdemDeServico','+ consultarOrcamentosAprovados():List<Orcamento>','+ consultarOrcamento(idOrcamento:int):Orcamento',
  '+ consultarBlocosDadosTecnicos(segmento:String):List<String>','+ consultarTecnicosHabilitados(segmento:String):List<Usuario>',
  '+ registrarOrdemServico(orcamentoSelecionado:int,tecnicoSelecionado:int,quilometragem:int,prazoEstimado:Date,observacoes:String,relatoInicial:String,condicaoRecebimento:String,diagnosticoInicial:String,servicoExecutado:String):OrdemDeServico',
  '+ incluirItemOrdemServico(servicoOuPeca:String,quantidade:int,valor:double):String',
  '+ registrarOSComAlteracao(servicoOuPeca:String,quantidade:int,valor:double,justificativa:String):String',
  '+ salvarRascunhoOS(orcamentoSelecionado:int,tecnicoSelecionado:int,quilometragem:int,prazoEstimado:Date,observacoes:String):void',
  '+ solicitarConfirmacaoDesistencia():String','+ descartarRegistro():void'],
 'persistencia/OrdemServicoDAO': ['+ salvar(os:OrdemDeServico):void','+ gerarNumeroOS():String'],
 'persistencia/UsuarioDAO': ['+ buscarUsuario(id:int):Usuario','+ buscarTecnicosHabilitados(segmento:String):List<Usuario>'],
 'persistencia/OrcamentoDAO': ['+ buscarOrcamento(id:int):Orcamento','+ buscarOrcamentosAprovados():List<Orcamento>',
  '+ gerarNumeroOrcamento():String','+ salvar(orc:Orcamento):void','+ salvarRascunho(orc:Orcamento):void',
  '+ buscarAtendimentosModulo(mod:ModuloECU):List<Orcamento>','+ listarOrcamentos():List<Orcamento>'],
 'persistencia/ClienteDAO': ['+ buscarPorCpfCnpj(cpfCnpj:String):Cliente','+ salvar(cli:Cliente):void'],
 'persistencia/VeiculoDAO': ['+ buscarPorPlaca(placa:String):Veiculo','+ salvar(vei:Veiculo):void'],
 'persistencia/ServicoBaseDAO': ['+ buscarPorCategoria(categoria:String):List<ServicoBase>','+ buscarServico(id:int):ServicoBase'],
 'persistencia/ModuloECUDAO': ['+ buscarModulo(vei:Veiculo,identificacaoModulo:String):ModuloECU'],
 'negocio/Cliente': ['- nome:String','- cpfCnpj:String','- telefone:String','+ Cliente(nome:String,telefone:String)'],
 'negocio/Orcamento': ['- numero:String','- dataSolicitacao:Date','- dataValidade:Date','- origemAtendimento:String','- status:String',
  '- valorEstimado:double','- observacoes:String','- motivoCancelamento:String',
  '+ associarCliente(cli:Cliente):void','+ associarVeiculo(vei:Veiculo):void','+ associarTecnico(tec:Usuario):void',
  '+ adicionarItem(item:ItemOrcamento):void','+ removerItem(item:ItemOrcamento):void','+ sinalizarRegistro():void',
  '- setDataSolicitacao(data:Date):void','- setStatus(status:String):void','+ calcularValorEstimado():double',
  '+ getVeiculo():Veiculo','+ getSegmentoPretendido():String'],
 'negocio/OrcamentoModulos': ['- sintomaRelatado:String','- diagnosticoPendenteBancada:boolean','- identificacaoIlegivel:boolean',
  '- registroFotografico:String','- descricaoFisicaPlaca:String',
  '+ OrcamentoModulos(numero:String,dataValidade:Date,origemAtendimento:String,observacoes:String,sintomaRelatado:String,diagnosticoPendenteBancada:boolean)',
  '+ setModuloIlegivel(registroFotografico:String,descricaoFisicaPlaca:String):void','+ associarModulo(mod:ModuloECU):void'],
 'negocio/OrcamentoChaves': ['- tipoChave:String','- quantidadeCopias:int','- possuiTelecomando:boolean','- codigoTransponder:String',
  '- situacaoImobilizador:String','- perdaTotal:boolean','- documentoPropriedade:String','- nomeSolicitante:String','- documentoSolicitante:String',
  '+ OrcamentoChaves(numero:String,dataValidade:Date,origemAtendimento:String,observacoes:String,tipoChave:String,quantidadeCopias:int,possuiTelecomando:boolean,codigoTransponder:String,situacaoImobilizador:String,perdaTotal:boolean)',
  '+ setComprovacaoPropriedade(documentoPropriedade:String,nomeSolicitante:String,documentoSolicitante:String):void'],
 'negocio/ItemOrcamento': ['- valorProposto:double','+ ItemOrcamento(servico:ServicoBase,valorProposto:double)',
  '+ getValorProposto():double','+ setValorProposto(valor:double):void'],
 'negocio/ServicoBase': ['- descricao:String','- categoria:String','- observacoesTecnicas:String','+ getCategoria():String'],
 'negocio/Veiculo': ['- marca:String','- modelo:String','- anoModelo:int','- motorizacao:String','- placa:String','- numeroChassi:String',
  '+ Veiculo(placa:String,marca:String,modelo:String,anoModelo:int,motorizacao:String,numeroChassi:String)','+ associarModulo(mod:ModuloECU):void'],
 'negocio/ModuloECU': ['- tipoModulo:String','- modeloECU:String','- identificacaoModulo:String',
  '+ ModuloECU(tipoModulo:String,modeloECU:String,identificacaoModulo:String)'],
 'negocio/InformacoesSegurancaVeiculo': ['- codigoMecanico:String','- senhaImobilizador:String','- senhaAlarme:String','- senhaRadio:String'],
 'negocio/Usuario': ['- idUsuario:int','- nome:String','- tipoUsuario:TipoUsuario'],
 'negocio/OrdemDeServico': ['- numeroOS:String','- dataAbertura:Date','- statusOS:String','- dataEmissaoGarantia:Date',
  '- textoExecutadoGarantia:String','- dataEmissaoResponsabilidade:Date','- quilometragem:int','- prazoEstimado:Date','- observacoes:String',
  '+ sinalizarAbertura():void','- setDataAbertura(data:Date):void','- setStatusOS(status:String):void',
  '+ associarOrcamento(orc:Orcamento):void','+ associarVeiculo(vei:Veiculo):void','+ associarTecnico(tec:Usuario):void'],
 'negocio/OrdemDeServicoModulos': ['- relatoInicial:String','- condicaoRecebimento:String','- diagnosticoInicial:String','- testesEntradaSaida:String',
  '- confirmacaoDefeito:String','- causaIdentificada:String','- reparoRealizado:String',
  '+ OrdemDeServicoModulos(quilometragem:int,prazoEstimado:Date,observacoes:String,relatoInicial:String,condicaoRecebimento:String,diagnosticoInicial:String)'],
 'negocio/OrdemDeServicoChaves': ['- servicoExecutado:String',
  '+ OrdemDeServicoChaves(quilometragem:int,prazoEstimado:Date,observacoes:String,servicoExecutado:String)'],
 'view/TelaSolicitarOrcamento': [], 'view/TelaRegistrarOrdemServico': [],
}
VIS = {'public':'+','private':'-','protected':'#'}
def declaracoes(texto):
    texto = re.sub(r'/\*[\s\S]*?\*/', '', texto); texto = re.sub(r'//[^\n]*', '', texto)
    campos, metodos = {}, {}
    for m in re.finditer(r'^    (public|private|protected)((?: static| final| transient)*) ([\w<>, \[\]]+?) (\w+)\s*(=[^;]*)?;', texto, re.M):
        if 'static' in m.group(2) and 'final' in m.group(2): continue
        campos[m.group(4)] = (VIS[m.group(1)], m.group(3).replace(' ', ''))
    for m in re.finditer(r'^    (public|private|protected)((?: static| final| abstract)*) (?:([\w<>, \[\]]+?) )?(\w+)\(([^)]*)\)', texto, re.M):
        params = [p.strip() for p in m.group(5).split(',') if p.strip()]
        ps = []
        for p in params:
            partes = p.replace('final ', '').rsplit(' ', 1)
            if len(partes) == 2:
                ps.append(partes[1] + ':' + partes[0].replace(' ', ''))
        ret = (m.group(3) or '').replace(' ', '')
        metodos.setdefault(m.group(4), []).append((VIS[m.group(1)], ps, ret))
    return campos, metodos
problemas = 0
for nome, membros in D.items():
    arq = BASE + nome + '.java'
    if not os.path.exists(arq):
        print(f'XX {nome}: CLASSE DO DIAGRAMA NÃO EXISTE NO CÓDIGO'); problemas += 1; continue
    if not membros:
        print(f'ok {nome}: boundary sem membros no diagrama (existe)'); continue
    campos, metodos = declaracoes(open(arq, encoding='utf-8').read())
    for mb in membros:
        vis, resto = mb.split(' ', 1)
        if '(' not in resto:
            n, t = resto.split(':')
            if n not in campos: print(f'XX {nome}: atributo {n} não existe'); problemas += 1
            elif campos[n] != (vis, t): print(f'XX {nome}: atributo {n} é {campos[n][0]} {campos[n][1]}; diagrama: {vis} {t}'); problemas += 1
            continue
        n = resto.split('(')[0]
        ps = [p for p in resto[resto.index('(')+1:resto.index(')')].split(',') if p]
        ret = resto[resto.index(')')+1:].lstrip(':')
        cands = metodos.get(n, [])
        if not cands: print(f'XX {nome}: método {n} não existe'); problemas += 1; continue
        if any(c == (vis, ps, ret if ret else '') or (not ret and c[0]==vis and c[1]==ps) for c in cands):
            continue
        achado = min(cands, key=lambda c: abs(len(c[1]) - len(ps)))
        print(f'XX {nome}: {n} no código = {achado[0]} ({", ".join(achado[1])}) : {achado[2] or "(construtor)"}\n   diagrama  = {vis} ({", ".join(ps)}) : {ret or "(construtor)"}'); problemas += 1
print(f'\n{problemas} divergência(s) de assinatura')
