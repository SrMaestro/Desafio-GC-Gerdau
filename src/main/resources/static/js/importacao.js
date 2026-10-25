/*
 * Telas da importação em massa (comprador).
 * O conteúdo da planilha não é confiável: todo texto vindo da API entra na página só por
 * textContent / createElement / setAttribute. Nada de montar HTML com dados.
 */
(function () {
    'use strict';

    const TAMANHO_PAGINA = 50;
    const INTERVALO_PROGRESSO = 2000;

    const ETAPAS = {
        EM_CONFERENCIA: 'Em conferência',
        PRONTA: 'Pronta para enviar',
        ENVIADA: 'Enviada ao aprovador',
        APROVADA: 'Aprovada',
        REJEITADA: 'Rejeitada',
        CONFLITO: 'Conflito',
        IGNORADA: 'Ignorada',
        DESCARTADA: 'Descartada'
    };
    const STATUS_LOTE = { PROCESSANDO: 'Processando', CONCLUIDO: 'Concluído', FALHOU: 'Falhou' };

    // ---------- utilitários ----------

    function el(tag, props, ...filhos) {
        const e = document.createElement(tag);
        if (props) {
            Object.keys(props).forEach((k) => {
                if (k === 'texto') e.textContent = props[k];
                else if (k === 'classe') e.className = props[k];
                else e.setAttribute(k, props[k]);
            });
        }
        filhos.forEach((f) => { if (f) e.append(f); });
        return e;
    }

    function limpar(no) { no.replaceChildren(); }

    class ErroApi extends Error {
        constructor(status, mensagem) {
            super(mensagem);
            this.status = status;
        }
    }

    function mensagemPadrao(status) {
        switch (status) {
            case 401: return 'Sua sessão expirou. Entre novamente.';
            case 403: return 'Você não tem permissão para esta ação.';
            case 404: return 'Não encontrado.';
            case 413: return 'Arquivo maior que 10 MB.';
            default: return 'Não foi possível concluir. Tente novamente.';
        }
    }

    function csrf() {
        const token = document.querySelector('meta[name="_csrf"]');
        const nome = document.querySelector('meta[name="_csrf_header"]');
        return token && token.content ? { nome: (nome && nome.content) || 'X-CSRF-TOKEN', valor: token.content } : null;
    }

    // O corpo da resposta pode não ser JSON (413 do servidor, página de login...): nunca assumir.
    async function api(url, opcoes) {
        const o = Object.assign({ method: 'GET', headers: {}, credentials: 'same-origin' }, opcoes || {});
        o.headers = Object.assign({ Accept: 'application/json' }, o.headers);
        if (o.method !== 'GET') {
            const t = csrf();
            if (t) o.headers[t.nome] = t.valor;
        }
        let res;
        try {
            res = await fetch(url, o);
        } catch (e) {
            throw new ErroApi(0, 'Sem conexão com o servidor. Tente novamente.');
        }
        let corpo = null;
        try {
            corpo = await res.json();
        } catch (e) {
            corpo = null;
        }
        if (res.redirected && /\/login/.test(res.url)) throw new ErroApi(401, mensagemPadrao(401));
        if (!res.ok) {
            const msg = corpo && typeof corpo.erro === 'string' && corpo.erro ? corpo.erro : mensagemPadrao(res.status);
            throw new ErroApi(res.status, msg);
        }
        return corpo;
    }

    function postJson(url, dados) {
        return api(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados === undefined ? {} : dados)
        });
    }

    function mostrar(no, texto) {
        no.textContent = texto;
        no.hidden = !texto;
    }

    function pct(score) { return Math.round((score || 0) * 100) + '%'; }

    function plural(n, um, varios) { return n + ' ' + (n === 1 ? um : varios); }

    // ---------- tela: enviar planilha e listar lotes ----------

    function iniciarLista(raiz) {
        const urlLotes = raiz.dataset.urlLotes;
        const urlTela = raiz.dataset.urlTela;
        const form = document.getElementById('form-envio');
        const arquivo = document.getElementById('arquivo');
        const botao = document.getElementById('btn-enviar');
        const msg = document.getElementById('envio-msg');
        const status = document.getElementById('lotes-status');
        const lista = document.getElementById('lotes-lista');

        form.addEventListener('submit', async (ev) => {
            ev.preventDefault();
            mostrar(msg, '');
            if (!arquivo.files || arquivo.files.length === 0) {
                mostrar(msg, 'Escolha uma planilha para enviar.');
                return;
            }
            const f = arquivo.files[0];
            if (f.size > 10 * 1024 * 1024) {
                mostrar(msg, 'Arquivo maior que 10 MB.');
                return;
            }
            const dados = new FormData();
            dados.append('arquivo', f);
            botao.disabled = true;
            botao.textContent = 'Enviando...';
            try {
                const lote = await api(urlLotes, { method: 'POST', body: dados });
                if (!lote || typeof lote.id !== 'number') throw new ErroApi(500, mensagemPadrao(500));
                window.location.assign(urlTela + '/' + encodeURIComponent(String(lote.id)));
            } catch (e) {
                mostrar(msg, e.message);
                botao.disabled = false;
                botao.textContent = 'Enviar planilha';
            }
        });

        (async () => {
            try {
                const lotes = await api(urlLotes);
                limpar(lista);
                status.textContent = lotes.length === 0 ? 'Você ainda não importou nenhuma planilha.' : '';
                lotes.forEach((l) => {
                    const link = el('a', { href: urlTela + '/' + encodeURIComponent(String(l.id)), texto: l.arquivo });
                    const andamento = l.status === 'PROCESSANDO'
                        ? STATUS_LOTE.PROCESSANDO + ' (' + l.processadas + ' de ' + l.total + ')'
                        : (STATUS_LOTE[l.status] || l.status) + ' · ' + plural(l.total, 'linha', 'linhas');
                    const data = l.criadoEm ? new Date(l.criadoEm).toLocaleString('pt-BR') : '';
                    lista.append(el('li', null, link, el('span', { texto: andamento }), el('span', { classe: 'data', texto: data })));
                });
            } catch (e) {
                status.textContent = e.message;
            }
        })();
    }

    // ---------- tela: um lote ----------

    function iniciarLote(raiz) {
        const loteId = raiz.dataset.loteId;
        const urlApi = raiz.dataset.urlApi + '/' + encodeURIComponent(loteId);
        const urlLinhasBase = raiz.dataset.urlApi + '/linhas/';
        const urlServicos = raiz.dataset.urlServicos;

        const $ = (id) => document.getElementById(id);
        const msgGeral = $('lote-msg');
        const falha = $('lote-falha');
        const blocosEl = $('blocos');
        const btnEnviar = $('btn-enviar-aprovador');
        const enviarInfo = $('enviar-info');

        let resumo = null;
        let encerrada = false; // a página está sendo deixada: nada de novas consultas
        let timer = null;
        let blocosLiberados = false;
        const servicos = new Map();

        window.addEventListener('pagehide', () => {
            encerrada = true;
            if (timer) clearTimeout(timer);
        });

        // Cada bloco guarda só a página atual; o DOM tem no máximo TAMANHO_PAGINA linhas por bloco.
        const blocos = {};
        raiz.querySelectorAll('.bloco[data-situacao]').forEach((no) => {
            const b = {
                no,
                situacao: no.dataset.situacao,
                lista: no.querySelector('[data-lista]'),
                paginas: no.querySelector('[data-paginas]'),
                pendentes: no.querySelector('[data-pendentes]'),
                todas: no.querySelector('[data-mostrar-todas]'),
                pagina: 0,
                carregado: false
            };
            blocos[b.situacao] = b;
            if (no.tagName === 'DETAILS') {
                no.addEventListener('toggle', () => { if (no.open && !b.carregado) carregarBloco(b); });
            }
            if (b.todas) b.todas.addEventListener('change', () => { b.pagina = 0; carregarBloco(b); });
        });

        function etapaFiltro(b) {
            // Erros e parecidas: por padrão só o que ainda precisa de ação do comprador
            if ((b.situacao === 'ERRO' || b.situacao === 'PARECIDA') && !(b.todas && b.todas.checked)) return 'EM_CONFERENCIA';
            return '';
        }

        function urlLinhas(situacao, etapa, pagina, tamanho) {
            const p = new URLSearchParams();
            p.set('situacao', situacao);
            if (etapa) p.set('etapa', etapa);
            p.set('pagina', String(pagina));
            p.set('tamanho', String(tamanho));
            return urlApi + '/linhas?' + p.toString();
        }

        // ---- resumo, progresso e contadores ----

        function contagem(mapa, chave) { return (mapa && mapa[chave]) || 0; }

        function mostrarResumo(r) {
            resumo = r;
            $('lote-arquivo').textContent = r.arquivo || '';
            document.title = 'Importação ' + (r.arquivo || '');
            const porSit = r.porSituacao || {};
            ['ERRO', 'PARECIDA', 'NOVA', 'EXISTENTE', 'REPETIDA'].forEach((s) => {
                const c = $('contagem-' + s.toLowerCase());
                if (c) c.textContent = String(contagem(porSit, s));
            });
            const prontas = contagem(r.porEtapa, 'PRONTA');
            btnEnviar.disabled = prontas === 0;
            enviarInfo.textContent = prontas === 0
                ? 'Nenhuma linha pronta para enviar agora.'
                : plural(prontas, 'linha pronta para enviar', 'linhas prontas para enviar') + '.';
            $('lote-resumo').textContent = plural(r.total, 'linha', 'linhas') + ' na planilha.';
        }

        async function atualizarResumo() {
            const r = await api(urlApi);
            mostrarResumo(r);
            return r;
        }

        function mostrarProgresso(r) {
            const barra = $('progresso-barra');
            const total = r.total > 0 ? r.total : 0;
            barra.max = total > 0 ? total : 100;
            barra.value = total > 0 ? Math.min(r.processadas, total) : 0;
            $('progresso-texto').textContent = total > 0
                ? 'Processando a planilha: ' + r.processadas + ' de ' + total + ' linhas.'
                : 'Lendo a planilha...';
        }

        function agendar() {
            if (encerrada) return;
            timer = setTimeout(consultar, INTERVALO_PROGRESSO);
        }

        async function consultar() {
            timer = null;
            if (encerrada) return;
            try {
                const r = await atualizarResumo();
                if (r.status === 'PROCESSANDO') {
                    $('progresso').hidden = false;
                    mostrarProgresso(r);
                    agendar();
                } else if (r.status === 'FALHOU') {
                    $('progresso').hidden = true;
                    mostrar(falha, 'A importação falhou. Tente novamente ou envie a planilha de novo.');
                    blocosEl.hidden = true;
                } else {
                    $('progresso').hidden = true;
                    falha.hidden = true;
                    liberarBlocos();
                }
            } catch (e) {
                // Falha passageira (rede, 5xx): continua tentando; sessão expirada ou lote inexistente: para
                if (e.status === 401 || e.status === 403 || e.status === 404) {
                    mostrar(falha, e.message);
                    $('progresso').hidden = true;
                } else {
                    mostrar(falha, e.message);
                    agendar();
                }
            }
        }

        function liberarBlocos() {
            if (blocosLiberados) return;
            blocosLiberados = true;
            blocosEl.hidden = false;
            carregarBloco(blocos.ERRO);
            carregarBloco(blocos.PARECIDA);
            atualizarAtencao();
        }

        // Quantas linhas ainda pedem ação: erros e parecidas em conferência (total vem do servidor)
        async function pendentes() {
            const [e, p] = await Promise.all([
                api(urlLinhas('ERRO', 'EM_CONFERENCIA', 0, 1)),
                api(urlLinhas('PARECIDA', 'EM_CONFERENCIA', 0, 1))
            ]);
            return { erros: e.total, parecidas: p.total };
        }

        async function atualizarAtencao() {
            try {
                const n = await pendentes();
                blocos.ERRO.pendentes.textContent = n.erros === 0 ? 'Nenhuma linha com erro para resolver.' : plural(n.erros, 'linha para resolver', 'linhas para resolver') + '.';
                blocos.PARECIDA.pendentes.textContent = n.parecidas === 0 ? 'Nenhuma linha parecida para conferir.' : plural(n.parecidas, 'linha para conferir', 'linhas para conferir') + '.';
                return n;
            } catch (e) {
                return null;
            }
        }

        // ---- blocos de linhas ----

        async function carregarBloco(b) {
            if (!b) return;
            b.carregado = true;
            try {
                const dados = await api(urlLinhas(b.situacao, etapaFiltro(b), b.pagina, TAMANHO_PAGINA));
                if (dados.totalPaginas > 0 && b.pagina >= dados.totalPaginas) {
                    b.pagina = dados.totalPaginas - 1; // a última página esvaziou depois de uma ação
                    return carregarBloco(b);
                }
                desenharBloco(b, dados);
            } catch (e) {
                limpar(b.lista);
                b.lista.append(el('li', { classe: 'msg-erro', texto: e.message }));
            }
        }

        function desenharBloco(b, dados) {
            limpar(b.lista);
            if (dados.linhas.length === 0) {
                b.lista.append(el('li', { classe: 'vazio-msg', texto: 'Nenhuma linha aqui.' }));
            }
            dados.linhas.forEach((l) => b.lista.append(desenharLinha(b, l)));
            desenharPaginas(b, dados);
        }

        function desenharPaginas(b, dados) {
            limpar(b.paginas);
            b.paginas.hidden = dados.totalPaginas <= 1;
            if (dados.totalPaginas <= 1) return;
            const ant = el('button', { type: 'button', texto: 'Anterior' });
            const prox = el('button', { type: 'button', texto: 'Próxima' });
            ant.disabled = dados.pagina <= 0;
            prox.disabled = dados.pagina >= dados.totalPaginas - 1;
            ant.addEventListener('click', () => { b.pagina = Math.max(0, b.pagina - 1); carregarBloco(b); });
            prox.addEventListener('click', () => { b.pagina += 1; carregarBloco(b); });
            b.paginas.append(ant,
                el('span', { texto: 'Página ' + (dados.pagina + 1) + ' de ' + dados.totalPaginas + ' (' + plural(dados.total, 'linha', 'linhas') + ')' }),
                prox);
        }

        function desenharLinha(b, l) {
            const li = el('li');
            li.append(el('span', { classe: 'num', texto: '#' + l.numero }));
            li.append(el('span', { classe: 'texto', texto: l.textoBreve || '(sem texto)' }));
            li.append(el('span', { classe: 'estado', texto: ETAPAS[l.etapa] || l.etapa }));

            if (b.situacao === 'ERRO') {
                li.append(el('span', { classe: 'detalhe motivo', texto: l.motivo || 'Linha com erro.' }));
            }
            if (b.situacao === 'PARECIDA') {
                const det = el('span', { classe: 'detalhe' });
                const alvo = el('span', { texto: 'carregando serviço parecido...' });
                det.append(el('strong', { texto: pct(l.score) + ' parecido com ' }), alvo);
                li.append(det);
                preencherParecido(alvo, l.servicoParecidoId);
            }
            if (b.situacao === 'NOVA' && l.codigoGerado) {
                const det = el('span', { classe: 'detalhe' });
                det.append('Código proposto: ', el('code', { texto: l.codigoGerado }));
                li.append(det);
            }
            if ((b.situacao === 'EXISTENTE' || b.situacao === 'REPETIDA') && l.motivo) {
                li.append(el('span', { classe: 'detalhe', texto: l.motivo }));
            }
            if (b.situacao === 'PARECIDA' && l.decisao) {
                li.append(el('span', { classe: 'detalhe', texto: l.decisao === 'MESMO_SERVICO' ? 'Decidido: é o mesmo serviço.' : 'Decidido: é outro serviço.' }));
            }

            const acoes = acoesDaLinha(b, l, li);
            if (acoes) li.append(acoes);
            return li;
        }

        function acoesDaLinha(b, l, li) {
            const editavel = l.etapa === 'EM_CONFERENCIA' || l.etapa === 'PRONTA';
            if (!editavel) return null;
            const acoes = el('span', { classe: 'acoes' });
            const status = el('span', { classe: 'msg-erro', role: 'alert' });

            const rodar = (botao, fn) => botao.addEventListener('click', async () => {
                botao.disabled = true;
                status.textContent = '';
                try {
                    await fn();
                } catch (e) {
                    status.textContent = e.message;
                    botao.disabled = false;
                    return;
                }
                // A ação já foi salva: falha ao atualizar a lista não é falha da ação (e repetir daria conflito)
                try {
                    await depoisDeAcao();
                } catch (e) {
                    status.textContent = 'A ação foi salva, mas não consegui atualizar a lista. Recarregue a página.';
                }
            });

            if (b.situacao === 'ERRO') {
                const corrigir = el('button', { type: 'button', texto: 'Corrigir' });
                corrigir.addEventListener('click', () => alternarCorrecao(l, li, corrigir));
                acoes.append(corrigir);
            }
            // Decidida mas ainda não enviada (PRONTA): o comprador pode mudar a decisão
            if (b.situacao === 'PARECIDA') {
                const mesmo = el('button', { type: 'button', texto: 'É o mesmo serviço' });
                const outro = el('button', { type: 'button', texto: 'É outro serviço' });
                rodar(mesmo, () => decidir(l.id, 'MESMO_SERVICO'));
                rodar(outro, () => decidir(l.id, 'OUTRO_SERVICO'));
                acoes.append(mesmo, outro);
            }
            const descartar = el('button', { type: 'button', texto: 'Descartar' });
            rodar(descartar, () => descartarLinha(l.id));
            acoes.append(descartar, status);
            return acoes;
        }

        async function decidir(id, decisao) {
            await postJson(urlLinhasBase + encodeURIComponent(String(id)) + '/decisao', { decisao });
        }

        async function descartarLinha(id) {
            await postJson(urlLinhasBase + encodeURIComponent(String(id)) + '/descartar');
        }

        // Recarrega contadores e os blocos que já foram abertos (na página em que estão)
        async function depoisDeAcao() {
            await atualizarResumo();
            await Promise.all(Object.values(blocos).filter((b) => b.carregado).map(carregarBloco));
            await atualizarAtencao();
        }

        function preencherParecido(no, id) {
            if (id === null || id === undefined) {
                no.textContent = 'serviço não informado';
                return;
            }
            if (!servicos.has(id)) {
                servicos.set(id, api(urlServicos + '/' + encodeURIComponent(String(id))).catch(() => null));
            }
            servicos.get(id).then((s) => {
                limpar(no);
                if (!s) {
                    no.textContent = 'serviço #' + id + ' (não foi possível carregar)';
                    return;
                }
                no.append(el('code', { texto: s.codigoPadronizado || '' }), ' ' + (s.textoBreve || ''));
            });
        }

        // ---- correção de linha com erro ----

        const CAMPOS = [
            ['areaServico', 'Área do serviço'],
            ['textoBreve', 'Texto breve'],
            ['abreviatura', 'Abreviatura'],
            ['complemento', 'Complemento'],
            ['condicao', 'Condição'],
            ['cnae', 'CNAE'],
            ['codigoNbs', 'Código NBS'],
            ['criadorCodigo', 'Criador do código']
        ];

        function alternarCorrecao(l, li, botao) {
            const existente = li.querySelector('.correcao');
            if (existente) {
                existente.remove();
                return;
            }
            const form = el('form', { classe: 'correcao' });
            const campos = {};
            CAMPOS.forEach(([nome, rotulo]) => {
                const id = 'corr-' + l.id + '-' + nome;
                const input = el('input', { type: 'text', id, name: nome, autocomplete: 'off' });
                input.value = l[nome] === null || l[nome] === undefined ? '' : String(l[nome]);
                campos[nome] = input;
                form.append(el('label', { for: id, texto: rotulo }, input));
            });
            const msg = el('p', { classe: 'msg msg-erro', role: 'alert' });
            const salvar = el('button', { type: 'submit', classe: 'botao', texto: 'Salvar correção' });
            const cancelar = el('button', { type: 'button', texto: 'Cancelar' });
            cancelar.addEventListener('click', () => form.remove());
            form.append(msg, el('span', { classe: 'botoes' }, salvar, cancelar));

            form.addEventListener('submit', async (ev) => {
                ev.preventDefault();
                msg.textContent = '';
                salvar.disabled = true;
                const corpo = {};
                CAMPOS.forEach(([nome]) => { corpo[nome] = campos[nome].value.trim(); });
                try {
                    const nova = await api(urlLinhasBase + encodeURIComponent(String(l.id)), {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(corpo)
                    });
                    if (nova.situacao === 'ERRO') {
                        msg.textContent = nova.motivo || 'A linha ainda tem erro.';
                        salvar.disabled = false;
                        await atualizarResumo();
                        return;
                    }
                    mostrar(msgGeral, 'Linha #' + nova.numero + ' corrigida: agora está em "' + situacaoTexto(nova.situacao) + '".');
                    await depoisDeAcao();
                } catch (e) {
                    msg.textContent = e.message;
                    salvar.disabled = false;
                }
            });
            li.append(form);
            campos.textoBreve.focus();
        }

        function situacaoTexto(s) {
            return { NOVA: 'novas', PARECIDA: 'parecidas', EXISTENTE: 'já existiam', REPETIDA: 'repetidas na planilha', ERRO: 'com erro' }[s] || s;
        }

        // ---- decisão em bloco ----

        function decidirEmBloco(decisao) {
            const campo = $('score-minimo');
            const valor = Number(campo.value);
            if (campo.value === '' || !Number.isFinite(valor) || valor < 0 || valor > 100) {
                mostrar(msgGeral, 'Informe uma similaridade mínima entre 0 e 100.');
                campo.focus();
                return;
            }
            const rotulo = decisao === 'MESMO_SERVICO' ? 'o mesmo serviço' : 'outro serviço';
            if (!window.confirm('Marcar como ' + rotulo + ' todas as linhas parecidas com similaridade de pelo menos '
                + valor + '%? Isso vale para todas as páginas, não só a que está na tela.')) return;
            const botoes = [$('bloco-mesmo'), $('bloco-outro')];
            botoes.forEach((b) => { b.disabled = true; });
            postJson(urlApi + '/decisao-em-bloco', { scoreMinimo: valor / 100, decisao })
                .then(async (r) => {
                    mostrar(msgGeral, plural(r.decididas, 'linha decidida', 'linhas decididas') + ' como ' + rotulo + '.');
                    await depoisDeAcao();
                })
                .catch((e) => mostrar(msgGeral, e.message))
                .finally(() => botoes.forEach((b) => { b.disabled = false; }));
        }

        $('bloco-mesmo').addEventListener('click', () => decidirEmBloco('MESMO_SERVICO'));
        $('bloco-outro').addEventListener('click', () => decidirEmBloco('OUTRO_SERVICO'));

        // ---- enviar ao aprovador ----

        btnEnviar.addEventListener('click', async () => {
            btnEnviar.disabled = true;
            try {
                const r = await postJson(urlApi + '/enviar');
                const enviadas = r.enviadas || 0;
                await atualizarResumo();
                await Promise.all(Object.values(blocos).filter((b) => b.carregado).map(carregarBloco));
                const n = await atualizarAtencao();
                let texto = plural(enviadas, 'linha enviada', 'linhas enviadas') + ' ao aprovador.';
                if (n) {
                    const total = n.erros + n.parecidas;
                    texto += total === 0
                        ? ' Nenhuma linha precisa de atenção.'
                        : ' Ainda ' + (total === 1 ? 'precisa de atenção 1 linha' : 'precisam de atenção ' + total + ' linhas')
                          + ' (' + plural(n.erros, 'com erro', 'com erro') + ' e ' + plural(n.parecidas, 'parecida', 'parecidas') + ').';
                }
                mostrar(msgGeral, texto);
            } catch (e) {
                mostrar(msgGeral, e.message);
                if (resumo) mostrarResumo(resumo);
            }
        });

        consultar();
    }

    document.addEventListener('DOMContentLoaded', () => {
        const lista = document.getElementById('importacoes');
        if (lista) iniciarLista(lista);
        const lote = document.getElementById('lote');
        if (lote) iniciarLote(lote);
    });
})();
