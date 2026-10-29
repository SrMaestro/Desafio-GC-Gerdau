# Validação da similaridade

Este documento mostra, com números medidos, o que a comparação de textos do sistema acerta, onde ela fica em dúvida e onde erra. O objetivo não é provar que ela funciona sempre, e sim deixar claro **o que dá para confiar e o que precisa de decisão humana**: um texto parecido não é automaticamente o mesmo serviço.

## 1. Como a similaridade funciona

- **Medida:** `similarity()` da extensão `pg_trgm` do PostgreSQL, sobre o `texto_breve` em minúsculas. Ela compara trechos de 3 letras (trigramas) e devolve de 0 (nada em comum) a 1 (iguais).
- **Duas réguas** (detalhes em `docs/REGRAS_DE_NEGOCIO.md`):
  - **A partir de 0,4** o serviço é mostrado como parecido e o cadastro **pede confirmação**.
  - **A partir de 0,8, com a mesma condição e o mesmo CNAE**, o cadastro é **bloqueado** como duplicado.
- **Quem decide:** o sistema só bloqueia sozinho no caso mais claro (texto quase igual, mesma condição, mesmo CNAE). No resto, quem decide é o comprador, e a decisão fica registrada (`/ui/consultas`).

### O que mudou da V1 para a V2

| V1 | V2 | Por que melhora |
|----|----|-----------------|
| O bloqueio olhava só os **5** serviços mais parecidos e, dentro deles, procurava um com a mesma condição e CNAE. | O bloqueio tem **consulta própria** (`ServicoRepository.buscarDuplicado`) que filtra a condição e o CNAE no banco. | O mesmo texto costuma existir em várias das 14 condições. Medido no banco: com 7 serviços de mesmo texto, os 5 mais parecidos eram todos de outras condições e o duplicado verdadeiro ficava de fora; o cadastro passava. Agora ele é encontrado. |
| Quando bloqueava, o serviço duplicado podia nem aparecer nas sugestões. | O duplicado aparece **em primeiro lugar** nas sugestões. | O aviso "use o código dele" passa a apontar para algo visível, com o botão **Usar este código**. |
| Não havia registro do que o comprador via e decidia. | Cada tentativa fica gravada (texto, sugestões mostradas, resultado e decisão) e vira **indicadores** na tela inicial. | Dá para verificar o que aconteceu em cada solicitação e medir o efeito em uso real. |

## 2. Como foi medido

- **Base usada:** o catálogo de exemplo do repositório, com **21 serviços**. **Não é a base real**: ela tem milhares de registros e vem da trilha IA & Dados. Os números abaixo mostram o comportamento da medida, não a taxa de acerto esperada em produção.
- **Casos:** 25 textos que um comprador poderia digitar, em três grupos, mais 4 pares extras (grupo D). Eles foram escritos pelo autor deste documento.
- **O que é "o mesmo serviço"** em cada caso foi **julgado pelo autor** a partir do nome. **A equipe que conhece os serviços deve revisar esses julgamentos**, principalmente os do grupo C.
- **Como reproduzir:** os scripts estão em `docs/validacao/` e rodam só leituras no banco:
  - `casos_similaridade.sql`: a tabela da seção 3 e os pares do grupo D.
  - `cortes_e_acentos.sql`: a tabela de cortes da seção 4.

## 3. Resultados por caso

"Nota" é a similaridade com o serviço mais parecido do catálogo. "Resposta" é o que o sistema faria. Para o bloqueio, assume-se o pior caso: a condição e o CNAE iguais aos do serviço encontrado.

**Veredito:** ✅ correto (chegou ao esperado) · 🟡 conservador (achou o serviço certo, mas só pede confirmação) · 🔶 ambíguo · ❌ incorreto.

### Grupo A: o mesmo serviço escrito de outro jeito (esperado: achar o serviço)

| Texto digitado | Serviço encontrado | Nota | Resposta | Veredito |
|----------------|--------------------|------|----------|----------|
| Concretagem de fundação | Concretagem de fundação | 1,00 | bloqueia | ✅ |
| concretagem de fundacao | Concretagem de fundação | 0,71 | pede confirmação | 🟡 sem acento |
| Concretagem de fundações | Concretagem de fundação | 0,75 | pede confirmação | 🟡 plural |
| Manutenção de motor elétrico | Manutenção de motor elétrico | 1,00 | bloqueia | ✅ |
| manutencao motor eletrico | Manutenção de motor elétrico | 0,51 | pede confirmação | 🟡 sem acento e sem "de" |
| Manutenção do motor elétrico | Manutenção de motor elétrico | 0,87 | bloqueia | ✅ |
| Instalação de painel eletrico | Instalação de painel elétrico | 0,82 | bloqueia | ✅ |
| Montagem de tubulacao industrial | Montagem de tubulação industrial | 0,78 | pede confirmação | 🟡 sem acento |
| Calandragem de chapas de aço carbono | Calandragrem de chapas de aco carbono | 0,70 | pede confirmação | 🟡 o catálogo tem erro de digitação |
| Calibração de instrumento de campo | Calibração de instrumentos de campo | 0,91 | bloqueia | ✅ |
| Solda TIG aço inox | Solda TIG em aço inox | 0,86 | bloqueia | ✅ |

**Resumo A:** os 11 foram encontrados em primeiro lugar. 6 seriam bloqueados e 5 só pedem confirmação. Nenhum foi perdido.

### Grupo B: serviço diferente com texto parecido (esperado: **não** tratar como o mesmo)

| Texto digitado | Serviço mais parecido | Nota | Resposta | Veredito |
|----------------|-----------------------|------|----------|----------|
| Manutenção de motor | Manutenção de motor elétrico | 0,68 | pede confirmação | ✅ |
| Manutenção de bomba de óleo | Manutenção de bomba centrífuga | 0,56 | pede confirmação | ✅ |
| Instalação de sensor de temperatura | Instalação de sensor de pressão | 0,53 | pede confirmação | ✅ |
| Instalação de painel solar | Instalação de painel elétrico | 0,58 | pede confirmação | ✅ |
| Solda TIG em aço carbono | Solda TIG em aço inox | 0,57 | pede confirmação | ✅ |
| Pintura epóxi residencial | Pintura epóxi industrial | 0,46 | pede confirmação | ✅ |
| Corte a plasma de tubos | Corte a plasma de chapas | 0,60 | pede confirmação | ✅ |

**Resumo B:** nenhum chegou a 0,8, então **nenhum foi bloqueado por engano**. Todos pedem confirmação, que é o comportamento certo: mostrar o parecido e deixar o comprador dizer que é outro serviço.

### Grupo C: o mesmo serviço com outras palavras (sinônimo, sigla, ordem, texto curto)

| Texto digitado | Serviço esperado ou encontrado | Nota | Resposta | Veredito |
|----------------|--------------------------------|------|----------|----------|
| Aço inox solda TIG | Solda TIG em aço inox | 0,86 | bloqueia | ✅ a ordem das palavras pouco importa |
| Soldagem TIG de inox | Solda TIG em aço inox | 0,54 | pede confirmação | 🟡 sinônimo |
| CLP programação | Programação de CLP industrial | 0,53 | pede confirmação | 🟡 |
| Mobilizar canteiro | Mobilização de canteiro de obras | 0,53 | pede confirmação | 🟡 verbo no lugar do substantivo |
| Eletricista industrial | eletricista | 0,55 | pede confirmação | 🟡 texto curto no catálogo |
| Montar andaime | andaime (id 19) | 0,53 | pede confirmação | 🔶 ver nota abaixo |
| Programação de controlador lógico | Programação de CLP industrial | 0,33 | **sem correspondência** | ❌ sigla × nome por extenso |

**Nota sobre "Montar andaime":** o catálogo tem dois serviços sobre andaime, `andaime` (id 19) e `Locação de andaime tubular` (id 11). O sistema aponta o primeiro, e a nota para o segundo é só 0,27. Qual é o "certo" depende do que o comprador quer, e isso mostra que o próprio catálogo já tem entradas que se sobrepõem.

### Grupo D: serviços diferentes que só mudam num detalhe curto no fim (risco de falso bloqueio)

Estes pares não estão no catálogo de exemplo; foram medidos diretamente.

| Serviço 1 | Serviço 2 | Nota | Resposta |
|-----------|-----------|------|----------|
| Solda TIG em aço inox 304 | Solda TIG em aço inox 316 | 0,79 | pede confirmação (por pouco) |
| Manutenção de bomba centrífuga 10 cv | Manutenção de bomba centrífuga 20 cv | **0,85** | **bloquearia** |
| Locação de andaime tubular | Locação de andaime fachadeiro | 0,50 | pede confirmação |
| Pintura epóxi industrial interna | Pintura epóxi industrial externa | **0,88** | **bloquearia** |

**Isto é um erro do sistema:** dois serviços diferentes, com a mesma condição e o mesmo CNAE, seriam bloqueados como duplicados. É a limitação mais séria encontrada (ver seção 5). O comprador pode contestar o bloqueio com uma justificativa, e isso fica registrado.

### Placar dos 25 casos dos grupos A, B e C

| Veredito | Casos |
|----------|-------|
| ✅ Correto | 14 (6 no A, 7 no B, 1 no C) |
| 🟡 Conservador (achou, mas só pede confirmação) | 9 (5 no A, 4 no C) |
| 🔶 Ambíguo | 1 |
| ❌ Incorreto | 1 |

## 4. O que os cortes mostram

Para cada corte de similaridade, quantos serviços **equivalentes** (18 casos: grupos A e C) e quantos **diferentes** (7 casos: grupo B) ficam com nota igual ou acima dele:

| Corte | Equivalentes acima | Diferentes acima | Equivalentes acima, ignorando acentos | Diferentes acima, ignorando acentos |
|-------|--------------------|------------------|----------------------------------------|--------------------------------------|
| 0,4 | 16 de 18 | 7 de 7 | 16 | 7 |
| 0,5 | 16 de 18 | 6 de 7 | 16 | 6 |
| 0,6 | 11 de 18 | 2 de 7 | 12 | 2 |
| 0,7 | 11 de 18 | 0 de 7 | 12 | 0 |
| **0,8** | **7 de 18** | **0 de 7** | **11** | **0** |

O que se lê daí:

- **Nenhum corte separa perfeitamente os dois grupos.** Entre 0,5 e 0,7 equivalentes e diferentes se misturam. Por isso a faixa de **0,4 a 0,8 é de decisão humana**, e o sistema não decide sozinho nela.
- **0,8 é um corte seguro nesta amostra** (nenhum falso bloqueio), mas **conservador**: só 7 dos 18 equivalentes passam. O preço é pedir confirmação em muitos casos que são mesmo duplicados.
- **Ignorar acentos ajudaria bastante:** com ele, 11 dos 18 passariam de 0,8 (contra 7), ainda sem nenhum falso bloqueio nesta amostra. Isto foi só medido, **não está implementado**.
- O corte de 0,4 não deixa de mostrar nenhum serviço diferente (7 de 7 aparecem). Isso não é erro, porque mostrar não é bloquear, mas pode virar ruído numa base grande.

## 5. Limitações

1. **Amostra pequena e fabricada.** 21 serviços e 29 casos escritos pelo autor. Não é possível afirmar taxa de acerto. A validação tem que ser repetida com a base saneada da trilha IA & Dados.
2. **Falso bloqueio por detalhe curto** (grupo D): nomes que diferem em poucas letras no fim (capacidade, lado, número) passam de 0,8. Com a mesma condição e o mesmo CNAE, o sistema bloqueia dois serviços diferentes. **Mitigação implementada:** o comprador pode contestar esse bloqueio ("É outro serviço") com uma justificativa obrigatória, que fica registrada para o aprovador conferir. O sistema continua bloqueando por padrão, e o erro vira uma decisão humana rastreável. Não se contesta o bloqueio por código idêntico nem por sinônimo aprovado.
3. **Acentos pesam.** A comparação não ignora acentos: "manutencao motor eletrico" cai para 0,51 enquanto sem acento seria 0,89.
4. **Sinônimos, siglas e verbos não são entendidos.** "CLP" e "controlador lógico", "soldagem" e "solda", "mobilizar" e "mobilização" só se aproximam pelas letras em comum. O que falta é coberto, em parte, pelos **sinônimos aprovados**: o aprovador liga um texto a um serviço e dali em diante ele é reconhecido. Isso aprende com o uso; não cobre o que ninguém ainda importou.
5. **Só o `texto_breve` é comparado.** Área, abreviatura e complemento não entram na nota. Dois serviços de áreas diferentes com o mesmo nome contam como parecidos.
6. **O catálogo pode ter erros e sobreposição.** Há erro de digitação (`Calandragrem`) e entradas vagas (`andaime`, `eletricista`, `soldagem`) que atrapalham a comparação. O saneamento da base é da trilha IA & Dados.
7. **Os cortes (0,4 e 0,8) vêm da prática, não de uma calibração.** Com a base real eles devem ser revistos.

## 6. Próximos passos

1. **Ignorar acentos na comparação** (ganho medido na seção 4). Exige migration (extensão `unaccent` ou `translate` e um novo índice, porque o índice atual é sobre `lower(texto_breve)`).
2. **Reduzir o falso bloqueio do grupo D na origem**, além da contestação que já existe: por exemplo, exigir que números e a última palavra do texto também coincidam antes de bloquear. Acompanhar nos indicadores quantas contestações acontecem diz se vale a pena.
3. **Repetir esta validação com a base saneada** e com casos reais de compradores, e então calibrar os cortes. As consultas registradas em `/ui/consultas` e os indicadores da tela inicial já dão o material: quantas vezes o sistema bloqueou, quantas o comprador reaproveitou o código e quantas vezes cadastrou "mesmo assim" depois de ver as sugestões.
4. **Ampliar os sinônimos** com as siglas e os termos mais comuns do catálogo real.
