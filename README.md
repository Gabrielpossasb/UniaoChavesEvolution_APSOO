# União Evolutions — Registrar Ordem de Serviço e Solicitar Orçamento

Trabalho Prático de APSOO — UFMS 2026.2 — Grupo G5

Sistema **Java desktop** (Swing + JDBC + MySQL), em **4 camadas** (interface, controladora, negócio e
persistência) com **padrão DAO**. O projeto tem os casos de uso de duas iterações:

| Iteração | Caso de uso | Tela | Controladora |
|---|---|---|---|
| 1ª | **RF-6 — Registrar Ordem de Serviço** | `TelaRegistrarOrdemServico` | `ControladoraRegistrarOS` |
| 2ª | **RF-5 — Solicitar Orçamento** | `TelaSolicitarOrcamento` | `ControladoraSolicitarOrcamento` |

Documentação completa:

- 1ª iteração: rastreabilidade, mapeamento objeto-relacional e divergências no
  [DOCUMENTACAO.md](DOCUMENTACAO.md);
- 2ª iteração: rastreabilidade, validações, decisões, contradições e rodadas de correção no
  [RELATORIO_ITERACAO2.md](../../RELATORIO_ITERACAO2.md), e a versão curta no
  [RESUMO_ITERACAO2.md](../../RESUMO_ITERACAO2.md).

---

## 1. Requisitos

| O quê | Versão | Para quê |
|---|---|---|
| **JDK** | 17 ou superior | compilar e executar |
| **XAMPP** | 8.x | fornece o MySQL (banco) e o phpMyAdmin (para rodar o script SQL) |
| **VS Code** + *Extension Pack for Java* | — | executar com um clique (opcional — ver 3.1) |

Já vêm no repositório: o driver JDBC (`lib/mysql-connector-j-8.4.0.jar`), o script do banco
(`sql/schema.sql`) e a configuração do VS Code. **Não é preciso baixar mais nada nem ter internet**:
o sistema opera só na rede local (RNF-4).

> Se você recebeu o projeto por email sem o `.jar` (o Gmail bloqueia anexos `.jar`), baixe o
> **MySQL Connector/J 8.4.0** e coloque o arquivo `mysql-connector-j-8.4.0.jar` na pasta `lib/`.

---

## 2. Banco de dados no XAMPP

> O XAMPP instala o **MariaDB**, um fork do MySQL. No painel, o módulo continua chamado "MySQL", e o
> driver e a URL `jdbc:mysql://` funcionam igual. Nada muda no código.

### 2.1 Windows

1. **Abra o XAMPP Control Panel** (`C:\xampp\xampp-control.exe`). Convém abrir **como
   administrador**.
2. **Clique em `Start`** nas linhas **Apache** e **MySQL**. As duas precisam ficar verdes, e o MySQL
   tem de estar na **porta 3306**. *(o Apache só serve para abrir o phpMyAdmin)*
3. **Abra http://localhost/phpmyadmin** (ou o botão `Admin` da linha do MySQL).
4. **Aba `Importar`** → `Escolher arquivo` → selecione o **`sql/schema.sql`** desta pasta → botão
   **`Importar`** no fim da página.

### 2.2 Linux

```bash
sudo /opt/lampp/lampp startmysql                 # liga o MySQL do XAMPP
/opt/lampp/bin/mysql -u root < sql/schema.sql    # cria o banco (rode dentro desta pasta)
```

Use o `mysql` do XAMPP (`/opt/lampp/bin/mysql`), não o do sistema. Se outro MySQL/MariaDB do sistema
estiver ocupando a porta 3306, pare-o antes: `sudo systemctl stop mysql` ou
`sudo systemctl stop mariadb`.

### 2.3 Depois de importar

Não é preciso criar nem selecionar o banco antes, porque o script já começa com
`CREATE DATABASE IF NOT EXISTS uniao_evolutions`.

**Confira:** o banco `uniao_evolutions` deve ter **13 tabelas** e `TB_Orcamento` deve ter **10
orçamentos**, dois deles com status `Aprovado pelo cliente`, que são os que o Registrar OS lista.

**Usuário e senha:** o projeto já está no padrão do XAMPP, `root` com senha em branco. Se o seu `root`
tiver senha, coloque-a em [Conexao.java:18](src/uniaoevolutions/persistencia/Conexao.java#L18), a única
classe que conhece as credenciais do banco.

**Reimporte se tiver o banco da 1ª iteração.** O schema da 2ª iteração segue o Mapeamento OR v2, e o
banco antigo não é compatível.

**Reimportar zera a demonstração.** O script apaga e recria apenas as tabelas deste projeto, sem
afetar outros bancos, e pode ser executado quantas vezes quiser.

**Sem XAMPP** funciona igual, mudando só como o script é executado:
`mysql -u root -p < sql/schema.sql` (ou o MySQL Workbench).

---

## 3. Como rodar

Com o **MySQL do XAMPP rodando** e o `schema.sql` importado, abra o **menu**. Ele tem um botão para
cada caso de uso e uma lista para escolher o usuário logado, sem digitar nada no terminal.

### 3.1 Pelo VS Code (recomendado)

1. **File → Open Folder** → escolha a pasta **`registrar-ordem-servico`**, a que tem `src/` e `lib/`
   dentro. Abrir a pasta de cima causa os erros `No suitable driver found` e
   `The type ClienteDAO is already defined`.
2. Abra [src/uniaoevolutions/MenuPrincipal.java](src/uniaoevolutions/MenuPrincipal.java) e clique no
   **▶ Run** acima do `main`.

### 3.2 Pelo terminal

Windows (`cmd`):

```cmd
javac -encoding UTF-8 -d bin -cp "lib\mysql-connector-j-8.4.0.jar" -sourcepath src src\uniaoevolutions\MenuPrincipal.java src\uniaoevolutions\MainRegistrarOrdemServico.java src\uniaoevolutions\MainSolicitarOrcamento.java
java -cp "bin;lib\mysql-connector-j-8.4.0.jar" uniaoevolutions.MenuPrincipal
```

Linux e macOS:

```bash
javac -encoding UTF-8 -d bin -cp lib/mysql-connector-j-8.4.0.jar $(find src -name '*.java')
java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MenuPrincipal
```

### 3.3 Usando o menu

1. Escolha o **usuário logado**. Ele vale para o Solicitar Orçamento, que não tem tela de login:

   | Usuário | Abre o Solicitar Orçamento? |
   |---|---|
   | Gabriel Abraão — Técnico de Módulos | sim (é o do mockup) |
   | Eduardo José — Técnico de Módulos | sim |
   | Jamily Sales — Técnico de Chaves | sim |
   | Caetano Raimundo — Administrador | não: o Administrador não é ator deste caso de uso |

2. Clique em **Solicitar Orçamento** ou em **Registrar Ordem de Serviço**. O menu some enquanto a tela
   está aberta e volta quando ela é fechada:
   - no Solicitar Orçamento, pelo **Desistir** ou pelo "X" da janela;
   - no Registrar OS, pelo **Voltar / Cancelar**. O "X" dessa janela não fecha nada.
3. O menu informa se o banco conectou. Se a conexão falhar, ele mostra a causa provável e a mensagem do
   banco, e tem o botão **Tentar conectar de novo**.

### 3.4 Abrir uma tela direto (sem o menu)

Os pontos de entrada antigos continuam funcionando:

```bash
java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MainRegistrarOrdemServico   # Registrar OS
java -cp "bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.MainSolicitarOrcamento 3   # Solicitar Orçamento como Jamily
```

O número do `MainSolicitarOrcamento` é o `idUsuario` do usuário logado (1 a 4, na ordem da tabela
acima). Sem número, entra o usuário 1.

---

## 4. Estrutura

```
registrar-ordem-servico/
├── src/uniaoevolutions/
│   ├── MenuPrincipal.java                       menu com as duas telas (fora da modelagem)
│   ├── MainRegistrarOrdemServico.java           abre o Registrar OS (fora da modelagem)
│   ├── MainSolicitarOrcamento.java              abre o Solicitar Orçamento (fora da modelagem)
│   ├── view/                                    INTERFACE      <<boundary>>
│   │   ├── TelaRegistrarOrdemServico.java           1ª iteração
│   │   ├── TelaSolicitarOrcamento.java              2ª iteração (Telas 1, 2 e 3 do mockup)
│   │   └── Validacoes.java                          formato de CPF/CNPJ, data, valor, placa…
│   ├── controle/                                CONTROLADORA   <<control>>
│   │   ├── ControladoraRegistrarOS.java
│   │   └── ControladoraSolicitarOrcamento.java
│   ├── negocio/                                 NEGÓCIO
│   │   ├── Orcamento.java                           superclasse
│   │   ├── OrcamentoModulos.java  OrcamentoChaves.java          subclasses V1 e V2
│   │   ├── OrdemDeServico.java                      (ABSTRATA) superclasse
│   │   ├── OrdemDeServicoModulos.java  OrdemDeServicoChaves.java subclasses V1 e V2
│   │   ├── ItemOrcamento.java  ServicoBase.java  ModuloECU.java
│   │   ├── Cliente.java  Veiculo.java  InformacoesSegurancaVeiculo.java
│   │   └── Usuario.java  TipoUsuario.java  Segmento.java
│   └── persistencia/                            PERSISTÊNCIA   (padrão DAO)
│       ├── Conexao.java            abre e fecha a conexão JDBC
│       ├── OrcamentoDAO.java       orçamentos (busca, grava, rascunho, numeração, listagem)
│       ├── OrdemServicoDAO.java    salvar, gerarNumeroOS, buscarHistoricoTecnico
│       ├── UsuarioDAO.java         buscarUsuario, buscarTecnicosHabilitados
│       ├── ClienteDAO.java         buscarPorCpfCnpj, salvar
│       ├── VeiculoDAO.java         buscarPorPlaca, salvar
│       ├── ModuloECUDAO.java       buscarModulo
│       └── ServicoBaseDAO.java     buscarPorCategoria, buscarServico
├── test/
│   ├── uniaoevolutions/            testes em Java puro (controladoras e tela do Solicitar Orçamento)
│   └── auditoria/                  scripts Python que comparam o código com os diagramas
├── docs/telas-iteracao2/           capturas das telas, geradas pelos testes
├── sql/schema.sql                  cria o banco, as 13 tabelas e os dados de exemplo
├── lib/mysql-connector-j-8.4.0.jar driver JDBC do MySQL
├── bin/                            saída da compilação
├── README.md                       este guia
└── DOCUMENTACAO.md                 documentação da 1ª iteração
```

Cada subpasta de `src/uniaoevolutions/` é **uma camada**, e **cada camada só fala com a de baixo**:

```
Usuário → view → controle → negocio
                    ↓
              persistencia ──JDBC──► MySQL (uniao_evolutions)
```

---

## 5. Como funciona

Na União Evolutions, o cliente pede um **orçamento** (2ª iteração); aprovado o orçamento, ele vira uma
**Ordem de Serviço** (1ª iteração). Os dois casos de uso têm duas variantes pelo segmento:
**Módulos** (V1) e **Chaves** (V2).

| Camada | Faz | Não faz |
|---|---|---|
| **`view`** | desenha as telas do mockup, lê e valida o formato dos campos, exibe as mensagens | nenhuma linha de SQL; não conhece as DAOs, só a controladora |
| **`controle`** | coordena o caso de uso: ordem das operações e chamadas às DAOs | não tem SQL nem abre `JOptionPane` |
| **`negocio`** | guarda os dados e valida as regras de negócio | não sabe que existe banco nem tela: **lança** exceção |
| **`persistencia`** | executa o SQL, sempre com `PreparedStatement` | não exibe mensagem: lança a exceção para cima |

### 5.1 Solicitar Orçamento (2ª iteração)

O Técnico informa a origem do atendimento, o cliente e o veículo, escolhe o segmento, preenche os dados
técnicos (módulo e sintoma em Módulos; tipo de chave, quantidade e situação em Chaves) e os serviços
com os valores. O sistema grava o orçamento com o número `ORC-AAAA-NNNN`. Também é possível salvar
rascunho e cadastrar na hora um cliente ou um veículo que ainda não existe.

O método [`registrarOrcamento`](src/uniaoevolutions/controle/ControladoraSolicitarOrcamento.java) é o
Diagrama de Comunicação transcrito (mensagens 1.1 a 1.15.3 nos comentários).

**Dá para testar só selecionando.** Os dados de exemplo trazem 6 clientes com veículos, módulos e
histórico. Clique na lupa do campo Cliente para ver a lista. O roteiro do mockup está na seção 9 do
[RELATORIO_ITERACAO2.md](../../RELATORIO_ITERACAO2.md); ele termina no orçamento **ORC-2026-0234**, de
**R$ 750,00**.

### 5.2 Registrar Ordem de Serviço (1ª iteração)

A tela lista os orçamentos aprovados → carrega cliente, veículo e itens do escolhido → mostra os campos
técnicos do segmento → lista os técnicos habilitados → grava a OS com o número `OS-AAAA-NNNN` e passa
o orçamento para "Convertido em OS".

O método [`registrarOrdemServico`](src/uniaoevolutions/controle/ControladoraRegistrarOS.java) é o
Diagrama de Comunicação transcrito (mensagens 1.1 a 1.11 nos comentários).

### 5.3 Herança no banco

`Orcamento` e `OrdemDeServico` têm uma subclasse por segmento. No banco, cada uma virou uma tabela da
superclasse mais uma por subclasse, com a PK sendo também FK para a superclasse (`TB_Orcamento` +
`TB_OrcamentoModulos` / `TB_OrcamentoChaves`; `TB_OrdemDeServico` + `TB_OrdemDeServicoModulos` /
`TB_OrdemDeServicoChaves`). Por isso gravar é **vários `INSERT` numa transação**.

---

## 6. Testes

Os testes são em Java puro, sem JUnit, e **recriam o banco** a cada execução. Rode dentro desta
pasta, depois de compilar o `src` (3.2):

```bash
javac -encoding UTF-8 -d bin-test -cp "bin:lib/mysql-connector-j-8.4.0.jar" $(find test -name '*.java')
java -cp "bin-test:bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.controle.TesteControladoras
java -cp "bin-test:bin:lib/mysql-connector-j-8.4.0.jar" uniaoevolutions.view.TesteTelaSolicitarOrcamento
```

O cenário `falhaDeGravacao` desliga e religa o MariaDB com `sudo service mariadb stop/start`, por isso
só funciona em Linux com o MariaDB do sistema. Para rodar só um cenário, passe o nome dele como
argumento.

---

## 7. Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| Menu mostra "Sem conexão com o banco" | Leia a dica da janela de erro; as causas estão nas linhas abaixo |
| MySQL do XAMPP não inicia (*"Port 3306 in use"*) | Outro MySQL ocupa a porta: pare o serviço dele (`services.msc` no Windows, `systemctl` no Linux) |
| Apache não inicia (porta 80) | Skype/IIS ocupando a porta. Sem Apache, importe o script pelo terminal (seção 2) |
| `Access denied for user 'root'` | O `root` tem senha: coloque-a em `Conexao.java` e compile de novo |
| `No suitable driver found for jdbc:mysql:` | Pasta errada aberta no VS Code (ver 3.1) ou `.jar` ausente em `lib/` |
| `Communications link failure` | O MySQL não está rodando ou não está na porta 3306 |
| `Unknown database 'uniao_evolutions'` | O `schema.sql` não foi importado (seção 2) |
| `Unknown column` ou `Table ... doesn't exist` | O banco é o da 1ª iteração: reimporte o `schema.sql` |
| "Nenhum orçamento aprovado disponível" no Registrar OS | Os orçamentos já foram convertidos numa demonstração anterior: reimporte o `schema.sql` |
| Solicitar Orçamento recusa o usuário | O Administrador não abre esta tela: escolha um técnico no menu |

O roteiro de demonstração da 1ª iteração está na **seção 8.5** do [DOCUMENTACAO.md](DOCUMENTACAO.md); o
da 2ª, na **seção 9** do [RELATORIO_ITERACAO2.md](../../RELATORIO_ITERACAO2.md).
