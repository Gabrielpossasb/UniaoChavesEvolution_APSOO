# Execução passo a passo

Guia para preparar o banco no XAMPP, rodar pelo VS Code ou pelo terminal e resolver os erros mais
comuns. O resumo está no [README](../README.md).

## 1. Banco de dados no XAMPP

O XAMPP traz o MariaDB, compatível com o MySQL. O driver e a URL `jdbc:mysql://` funcionam igual.

### Windows

1. Abra o XAMPP Control Panel (`C:\xampp\xampp-control.exe`), de preferência como administrador.
2. Clique em `Start` nas linhas **Apache** e **MySQL**. O MySQL precisa estar na porta 3306. O Apache
   só serve para abrir o phpMyAdmin.
3. Abra http://localhost/phpmyadmin.
4. Aba `Importar` → `Escolher arquivo` → `sql/schema.sql` → `Importar`.

### Linux

```bash
sudo /opt/lampp/lampp startmysql
/opt/lampp/bin/mysql -u root < sql/schema.sql    # dentro da pasta do projeto
```

Se outro MySQL ou MariaDB do sistema ocupar a porta 3306, pare-o antes:
`sudo systemctl stop mysql` ou `sudo systemctl stop mariadb`.

### Depois de importar

- O script começa com `CREATE DATABASE IF NOT EXISTS uniao_evolutions`: não é preciso criar o banco
  antes.
- Confira: o banco `uniao_evolutions` tem 14 tabelas, e `TB_Orcamento` tem 10 orçamentos, dois com
  situação `Aprovado pelo cliente` (os que o Registrar OS lista).
- Credenciais: `root` sem senha, nas constantes `URL`, `USUARIO` e `SENHA` de
  [Conexao.java](../src/uniaoevolutions/persistencia/Conexao.java). Se o seu `root` tiver senha, altere
  `SENHA` e compile de novo.
- Reimportar apaga e recria só as tabelas deste projeto e volta os dados de exemplo. Faça isso antes de
  cada demonstração e sempre que o banco for de uma versão antiga do projeto.

## 2. Rodar

### VS Code

1. **File → Open Folder** na pasta do projeto, a que tem `src/` e `lib/`. A configuração em
   `.vscode/settings.json` já aponta o driver.
2. Abra `src/uniaoevolutions/MenuPrincipal.java` e clique em **Run** acima do `main`.

### Terminal no Windows (`cmd`)

```cmd
javac -encoding UTF-8 -d bin -cp "lib\mysql-connector-j-8.4.0.jar" -sourcepath src src\uniaoevolutions\MenuPrincipal.java src\uniaoevolutions\MainRegistrarOrdemServico.java src\uniaoevolutions\MainSolicitarOrcamento.java
java -cp "bin;lib\mysql-connector-j-8.4.0.jar" uniaoevolutions.MenuPrincipal
```

No Linux e no macOS, os comandos estão no [README](../README.md#como-rodar).

## 3. Usando o menu

1. Escolha o usuário logado. Não há tela de login: o usuário escolhido vale para o Solicitar Orçamento.

   | Usuário | `idUsuario` | Abre o Solicitar Orçamento? |
   |---|---|---|
   | Gabriel Abraão (Técnico de Módulos) | 1 | sim |
   | Eduardo José (Técnico de Módulos) | 2 | sim |
   | Jamily Sales (Técnico de Chaves) | 3 | sim |
   | Caetano Raimundo (Administrador) | 4 | não: o Administrador não é ator deste caso de uso |

2. Clique em **Solicitar Orçamento** ou **Registrar Ordem de Serviço**. O menu some enquanto a tela está
   aberta e volta quando ela fecha, pelo **Voltar / Desistir** ou pelo **Voltar / Cancelar**. O "X" das
   janelas não fecha, de propósito.
3. Se o banco não conectar, o menu mostra "Sem conexão com o banco.", a causa provável, a mensagem do
   driver e o botão **Tentar conectar de novo**.

**Testar só selecionando:** no Solicitar Orçamento, clique na lupa do campo Cliente para ver os 6
clientes de exemplo. Cada um tem veículo, módulo e histórico.

## 4. Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| MySQL do XAMPP não inicia (*"Port 3306 in use"*) | Outro MySQL ocupa a porta: pare o serviço dele (`services.msc` no Windows, `systemctl` no Linux) |
| Apache não inicia (porta 80) | Outro programa usa a porta. Sem Apache, importe o script pelo terminal |
| `Access denied for user 'root'` | O `root` tem senha: altere `SENHA` em `Conexao.java` e compile de novo |
| `No suitable driver found for jdbc:mysql:` | Pasta errada aberta no VS Code ou `lib/mysql-connector-j-8.4.0.jar` ausente |
| `Communications link failure` | O MySQL não está rodando ou não está na porta 3306 |
| `Unknown database 'uniao_evolutions'` | O `schema.sql` não foi importado |
| `Unknown column` ou `Table ... doesn't exist` | O banco é de uma versão antiga: reimporte o `schema.sql` |
| "Nenhum orçamento aprovado disponível para abertura de Ordem de Serviço" | Os orçamentos aprovados já viraram OS numa demonstração anterior: reimporte o `schema.sql` |
| "Somente Técnico de Módulos ou Técnico de Chaves pode registrar orçamentos." | O usuário escolhido é o Administrador: escolha um técnico no menu |
