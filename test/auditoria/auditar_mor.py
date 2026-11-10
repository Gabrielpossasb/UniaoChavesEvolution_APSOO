import re, docx
d = docx.Document('2° Iteração Solicitar Orçamento/Design/Mapeamento_Objeto_Relacional_v2.docx')
texto = '\n'.join(p.text for p in d.paragraphs)
texto = re.sub(r'\s+', ' ', texto)
mor = {}
for m in re.finditer(r'(TB_\w+)\(([^)]*)\)', texto):
    cols = []
    for c in m.group(2).split(','):
        c = c.strip()
        nome = re.sub(r'<<.*', '', c).strip()
        marcas = set(re.findall(r'<<(\w+)>>', c))
        cols.append((nome, marcas))
    if m.group(1) not in mor:
        mor[m.group(1)] = cols
sql = open('Implementacao/registrar-ordem-servico/sql/schema.sql', encoding='utf-8').read()
sql = re.sub(r'--[^\n]*', '', sql)
tabelas = {}
for m in re.finditer(r'CREATE TABLE (\w+) \(([\s\S]*?)\n\);', sql):
    corpo = m.group(2)
    cols, pk, fks, uniq = [], set(), set(), set()
    for linha in corpo.split('\n'):
        l = linha.strip().rstrip(',')
        if not l: continue
        if l.startswith('PRIMARY KEY'):
            pk |= set(x.strip() for x in re.search(r'\((.*)\)', l).group(1).split(',')); continue
        if l.startswith('CONSTRAINT') or l.startswith('REFERENCES'):
            mf = re.search(r'FOREIGN KEY \((\w+)\)', l)
            if mf: fks.add(mf.group(1))
            continue
        nome = l.split()[0]
        cols.append(nome)
        if 'PRIMARY KEY' in l: pk.add(nome)
        if 'UNIQUE' in l: uniq.add(nome)
    tabelas[m.group(1)] = (cols, pk, fks, uniq)
prob = 0
for t, cols in mor.items():
    if t not in tabelas:
        print(f'XX {t}: no MOR, falta no schema.sql'); prob += 1; continue
    scols, pk, fks, uniq = tabelas[t]
    nomes = [c for c, _ in cols]
    faltam = [c for c in nomes if c not in scols]
    sobram = [c for c in scols if c not in nomes]
    marc = []
    for c, mk in cols:
        if c not in scols: continue
        if ('PK' in mk) != (c in pk): marc.append(f'{c} PK')
        if ('FK' in mk) != (c in fks): marc.append(f'{c} FK')
        if ('UNIQUE' in mk) != (c in uniq): marc.append(f'{c} UNIQUE')
    ordem_ok = [c for c in scols if c in nomes] == [c for c in nomes if c in scols]
    if faltam or sobram or marc or not ordem_ok:
        prob += 1
        print(f'XX {t}: ' + '; '.join(x for x in [
            'faltam ' + ', '.join(faltam) if faltam else '', 'a mais ' + ', '.join(sobram) if sobram else '',
            'restrições diferentes: ' + ', '.join(marc) if marc else '', '' if ordem_ok else 'ordem das colunas difere do MOR'] if x))
    else:
        print(f'ok {t}')
for t in tabelas:
    if t not in mor: print(f'XX {t}: no schema.sql, não está no MOR'); prob += 1
print(f'\n{prob} divergência(s) com o Mapeamento OR v2')
