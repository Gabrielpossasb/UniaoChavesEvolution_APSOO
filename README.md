# Registrar Ordem de Serviço — União Evolutions

Trabalho Prático de APSOO — UFMS 2026.2 — Grupo G5

Caso de uso **RF-6 — Registrar Ordem de Serviço** em **Java desktop** (Swing + JDBC + MySQL), em
**4 camadas** (interface, controladora, negócio e persistência) com **padrão DAO**.

A documentação completa — rastreabilidade das regras até os artefatos, mapeamento objeto-relacional
e divergências entre os documentos — está no [DOCUMENTACAO.md](DOCUMENTACAO.md).

---

## 1. Requisitos

| O quê | Versão | Para quê |
|---|---|---|
| **JDK** | 17 ou superior | compilar e executar |
| **XAMPP** | 8.x | fornece o MySQL (banco) e o phpMyAdmin (para rodar o script SQL) |
| **VS Code** + *Extension Pack for Java* | — | executar com um clique (opcional — ver 3.2) |

Já vêm no repositório: o driver JDBC (`lib/mysql-connector-j-8.4.0.jar`), o script do banco
(`sql/schema.sql`) e a configuração do VS Code. **Não é preciso baixar mais nada nem ter internet** —
o sistema opera só na rede local (RNF-4).

---

## 2. O que fazer no XAMPP

> O XAMPP instala o **MariaDB**, um fork do MySQL: o módulo continua chamado "MySQL" no painel, o
> driver e a URL `jdbc:mysql://` funcionam igual. Nada muda no código.

1. **Abra o XAMPP Control Panel** (`C:\xampp\xampp-control.exe`) — no Windows, convém abrir **como
   administrador**.
2. **Clique em `Start`** nas linhas **Apache** e **MySQL**. Os dois precisam ficar verdes, e o MySQL
   tem de estar na **porta 3306**. *(o Apache só serve para abrir o phpMyAdmin)*
3. **Abra http://localhost/phpmyadmin** (ou o botão `Admin` da linha do MySQL).
4. **Aba `Importar`** → `Escolher arquivo` → selecione o **`sql/schema.sql`** desta pasta → botão
   **`Importar`** no fim da página.
   Não precisa criar nem selecionar o banco antes: o script já começa com
   `CREATE DATABASE IF NOT EXISTS uniao_evolutions`.
5. **Confira:** clique em `uniao_evolutions` na lista da esquerda. Devem aparecer **10 tabelas**, e
   `TB_Orcamento` deve ter 3 orçamentos (dois com status `Aprovado pelo cliente`).

**Usuário e senha:** o projeto já está no padrão do XAMPP — `root` com senha em branco. Se o seu
`root` tiver senha, coloque-a em [Conexao.java:18-20](src/uniaoevolutions/persistencia/Conexao.java#L18-L20),
a única classe que conhece as credenciais do banco.

**Reimportar zera a demonstração.** O script apaga e recria apenas as 10 tabelas deste caso de uso,
sem afetar outros bancos, e pode ser executado quantas vezes quiser.

**Sem XAMPP** funciona igual, mudando só como o script é executado:
`mysql -u root -p < sql\schema.sql` (ou o MySQL Workbench).

---

## 3. Como rodar

Com o **MySQL do XAMPP rodando** e o `schema.sql` importado:

### 3.1 Pelo VS Code (recomendado)

1. **File → Open Folder** → escolha a pasta **`registrar-ordem-servico`** (a que tem `src/` e `lib/`
   dentro — abrir a pasta de cima causa o erro `No suitable driver found`).
2. Abra [src/uniaoevolutions/Main.java](src/uniaoevolutions/Main.java) e clique no **▶ Run** acima do
   `main`.

### 3.2 Pelo terminal

```cmd
javac -encoding UTF-8 -d bin -cp "lib\mysql-connector-j-8.4.0.jar" -sourcepath src src\uniaoevolutions\Main.java
java -cp "bin;lib\mysql-connector-j-8.4.0.jar" uniaoevolutions.Main
```

No Linux e no macOS, troque a `\` por `/` e o `;` do classpath por `:`.

---

## 4. Estrutura

```
registrar-ordem-servico/
├── src/uniaoevolutions/
│   ├── Main.java                                abre a tela (fora da modelagem)
│   ├── view/TelaRegistrarOrdemServico.java      INTERFACE      <<boundary>>
│   ├── controle/ControladoraRegistrarOS.java    CONTROLADORA   <<control>>
│   ├── negocio/                                 NEGÓCIO
│   │   ├── OrdemDeServico.java                      (ABSTRATA) superclasse
│   │   ├── OrdemDeServicoModulos.java               subclasse — variante V1
│   │   ├── OrdemDeServicoChaves.java                subclasse — variante V2
│   │   ├── Orcamento.java   ItemOrcamento.java   ServicoBase.java
│   │   ├── Cliente.java     Veiculo.java         Usuario.java
│   │   └── TipoUsuario.java Segmento.java
│   └── persistencia/                            PERSISTÊNCIA   (padrão DAO)
│       ├── Conexao.java            abre e fecha a conexão JDBC
│       ├── OrcamentoDAO.java       buscarOrcamento, buscarOrcamentosAprovados,
│       │                           atualizarStatus, incluirItem
│       ├── UsuarioDAO.java         buscarUsuario, buscarTecnicosHabilitados
│       └── OrdemServicoDAO.java    salvar, gerarNumeroOS, buscarHistoricoTecnico
├── sql/schema.sql                   cria o banco, as 10 tabelas e os dados do mockup
├── lib/mysql-connector-j-8.4.0.jar  driver JDBC do MySQL
├── bin/                             saída da compilação
├── README.md                        este guia
└── DOCUMENTACAO.md                  documentação completa
```

Cada subpasta de `src/uniaoevolutions/` é **uma camada**, e **cada camada só fala com a de baixo**:

```
Administrador → view → controle → negocio
                         ↓
                   persistencia ──JDBC──► MySQL (uniao_evolutions)
```

---

## 5. Como funciona

Na União Evolutions, o cliente pede um **orçamento**; aprovado o orçamento, ele vira uma **Ordem de
Serviço**. Este caso de uso é exatamente esse ponto: a tela lista os orçamentos aprovados → carrega
cliente, veículo e itens do escolhido → mostra os campos técnicos do segmento (**Módulos** *ou*
**Chaves**) → lista os técnicos habilitados → grava a OS com o número `OS-AAAA-NNNN`.

| Camada | Faz | Não faz |
|---|---|---|
| **`view`** | desenha as 5 seções do mockup, lê os campos, exibe as mensagens | nenhuma linha de SQL; não conhece as DAOs — só a controladora |
| **`controle`** | coordena o caso de uso: ordem das operações e chamadas às DAOs | não tem SQL nem abre `JOptionPane` |
| **`negocio`** | guarda os dados e valida as regras (RN-03 e RN-04, nos `validar()`) | não sabe que existe banco nem tela: **lança** exceção |
| **`persistencia`** | executa o SQL, sempre com `PreparedStatement` | não exibe mensagem: lança a exceção para cima |

O método [`registrarOrdemServico`](src/uniaoevolutions/controle/ControladoraRegistrarOS.java#L178) é
o Diagrama de Comunicação transcrito (mensagens 1.1 a 1.11 nos comentários): busca o orçamento, o
veículo e o técnico, cria a OS na subclasse do segmento, forma os vínculos, **valida antes de tocar
no banco**, grava e passa o orçamento para "Convertido em OS".

**A herança é o ponto central.** `OrdemDeServico` é abstrata — toda OS é de Módulos ou de Chaves — e
no banco isso virou três tabelas (`TB_OrdemDeServico` + uma por subclasse, com a PK sendo também FK
para a superclasse). Por isso gravar uma OS são **dois `INSERT` numa transação**. Detalhado na
seção 4 do [DOCUMENTACAO.md](DOCUMENTACAO.md).

---

## 6. Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| MySQL do XAMPP não inicia (*"Port 3306 in use"*) | Outro MySQL ocupa a porta: pare o serviço dele em `services.msc` |
| Apache não inicia (porta 80) | Skype/IIS ocupando a porta. Sem Apache, importe o script pelo terminal (seção 2) |
| phpMyAdmin: `#1045 Access denied` | O `root` tem senha: use-a e replique em `Conexao.java` |
| `No suitable driver found for jdbc:mysql:` | Pasta errada aberta no VS Code — ver 3.1 |
| `Communications link failure` | O módulo MySQL não está rodando ou não está na porta 3306 |
| `Unknown database 'uniao_evolutions'` | O `schema.sql` não foi importado — ver seção 2 |
| "Nenhum orçamento aprovado disponível" ao abrir | Os orçamentos já foram convertidos numa demonstração anterior: reimporte o `schema.sql` |

O roteiro de demonstração passo a passo está na **seção 8.5** do [DOCUMENTACAO.md](DOCUMENTACAO.md).
