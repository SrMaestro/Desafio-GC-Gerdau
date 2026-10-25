/*
 * Telas da importação em massa (aprovador).
 * O conteúdo da planilha não é confiável: todo texto vindo da API entra na página só por
 * textContent / createElement / setAttribute. Nada de montar HTML com dados.
 * Funções comuns (el, api, postJson...) vêm de comum.js.
 */
(function () {
    'use strict';

    const { el, limpar, api, postJson, mostrar, pct, plural } = window.Comum;

    const TAMANHO_PAGINA = 50;
    const DECISOES = { MESMO_SERVICO: 'é o mesmo serviço', OUTRO_SERVICO: 'é outro serviço' };

    function formatarData(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        return isNaN(d.getTime()) ? '' : d.toLocaleString('pt-BR');
    }

    function desenharPaginas(nav, dados, irPara) {
        limpar(nav);
        nav.hidden = dados.totalPaginas <= 1;
        if (dados.totalPaginas <= 1) return;
        const ant = el('button', { type: 'button', texto: 'Anterior' });
        const prox = el('button', { type: 'button', texto: 'Próxima' });
        ant.disabled = dados.pagina <= 0;
        prox.disabled = dados.pagina >= dados.totalPaginas - 1;
        ant.addEventListener('click', () => irPara(Math.max(0, dados.pagina - 1)));
        prox.addEventListener('click', () => irPara(dados.pagina + 1));
        nav.append(ant,
            el('span', { texto: 'Página ' + (dados.pagina + 1) + ' de ' + dados.totalPaginas + ' (' + plural(dados.total, 'linha', 'linhas') + ')' }),
            prox);
    }

    // ---------- tela: lotes com linhas a decidir ----------

    async function iniciarLotes(raiz) {
        const urlApi = raiz.dataset.urlApi;
        const urlTela = raiz.dataset.urlTela;
        const status = document.getElementById('lotes-status');
        const lista = document.getElementById('lotes-lista');
        const erro = document.getElementById('lotes-erro');
        try {
            const lotes = await api(urlApi + '/aprovacao/lotes');
            limpar(lista);
            if (!Array.isArray(lotes) || lotes.length === 0) {
                status.textContent = 'Nenhuma planilha esperando decisão.';
                return;
            }
            status.textContent = plural(lotes.length, 'planilha com linhas a decidir', 'planilhas com linhas a decidir') + '.';
            lotes.forEach((l) => {
                const li = el('li');
                li.append(el('a', { href: urlTela + '/' + encodeURIComponent(String(l.id)), texto: l.arquivo || ('Lote ' + l.id) }));
                li.append(el('span', { texto: 'enviada por ' + (l.enviadoPor || '?') }));
                li.append(el('span', { classe: 'contagens', texto: plural(l.enviadas, 'linha aguardando', 'linhas aguardando') + (l.conflitos ? ' · ' + plural(l.conflitos, 'em conflito', 'em conflito') : '') }));
                li.append(el('span', { classe: 'data', texto: formatarData(l.criadoEm) }));
                lista.append(li);
            });
        } catch (e) {
            status.textContent = '';
            mostrar(erro, e.message);
        }
    }

    // ---------- tela: um lote ----------

    function iniciarLote(raiz) {
        const loteId = raiz.dataset.loteId;
        const urlApi = raiz.dataset.urlApi + '/' + encodeURIComponent(loteId);
        const urlAprovacao = raiz.dataset.urlApi + '/aprovacao';
        const urlLotes = urlAprovacao + '/lotes';
        const urlServicos = raiz.dataset.urlServicos;
        const $ = (id) => document.getElementById(id);

        const lista = $('linhas');
        const paginas = $('paginas');
        const msgGeral = $('lote-msg');
        const erroGeral = $('lote-erro');
        const info = $('bloco-info');
        const btnAprovar = $('btn-aprovar-bloco');
        const btnRejeitar = $('btn-rejeitar-bloco');
        const motivoBloco = $('motivo-bloco');
        const tipo = $('filtro-tipo');
        const scoreCampo = $('filtro-score-campo');
        const score = $('filtro-score');
        const conflito = $('bloco-conflito');

        let pagina = 0;
        let totalFiltrado = null; // quantas linhas o filtro atual alcança (do servidor)
        let ocupado = false; // ação em bloco em andamento
        const servicos = new Map();

        // ---- filtro: a mesma fonte para a listagem, a contagem e a ação em bloco ----

        // Devolve {situacao, scoreMinimo} ou null se o mínimo digitado for inválido
        function filtroAtual() {
            const situacao = tipo.value || null;
            let scoreMinimo = null;
            if (situacao === 'PARECIDA' && score.value.trim() !== '') {
                const v = Number(score.value);
                if (!Number.isFinite(v) || v < 0 || v > 100) return null;
                scoreMinimo = Number((v / 100).toFixed(4));
            }
            return { situacao, scoreMinimo };
        }

        function consultaLinhas(f, pg, tamanho) {
            const p = new URLSearchParams();
            p.set('etapa', 'ENVIADA');
            if (f.situacao) p.set('situacao', f.situacao);
            if (f.scoreMinimo !== null) p.set('scoreMinimo', String(f.scoreMinimo));
            p.set('pagina', String(pg));
            p.set('tamanho', String(tamanho));
            return urlApi + '/linhas?' + p.toString();
        }

        // Corpo do filtro da ação em bloco (situacoes vazio = todas; scoreMinimo só com PARECIDA)
        function corpoDoFiltro(f) {
            return { situacoes: f.situacao ? [f.situacao] : null, scoreMinimo: f.scoreMinimo };
        }

        function descreverFiltro(f) {
            if (!f.situacao) return 'todas as linhas aguardando decisão';
            if (f.situacao === 'NOVA') return 'as linhas novas';
            return 'as linhas parecidas' + (f.scoreMinimo !== null ? ' com similaridade de pelo menos ' + Math.round(f.scoreMinimo * 100) + '%' : '');
        }

        function atualizarBotoes() {
            const semLinhas = totalFiltrado === null || totalFiltrado === 0;
            btnAprovar.disabled = ocupado || semLinhas;
            btnRejeitar.disabled = ocupado || semLinhas;
        }

        function mostrarContagem() {
            if (filtroAtual() === null) {
                info.textContent = 'Informe a similaridade mínima entre 0 e 100.';
            } else if (totalFiltrado === null) {
                info.textContent = 'Carregando...';
            } else if (totalFiltrado === 0) {
                info.textContent = 'Nenhuma linha neste filtro.';
            } else {
                info.textContent = 'Este filtro alcança ' + plural(totalFiltrado, 'linha', 'linhas') + ', em todas as páginas.';
            }
            atualizarBotoes();
        }

        // ---- cabeçalho do lote ----

        async function atualizarCabecalho() {
            try {
                const lotes = await api(urlLotes);
                const l = Array.isArray(lotes) ? lotes.find((x) => String(x.id) === String(loteId)) : null;
                if (!l) {
                    $('lote-arquivo').textContent = '';
                    $('lote-resumo').textContent = 'Este lote não tem mais linhas esperando decisão.';
                    $('contagem-conflito').textContent = '0';
                    return;
                }
                $('lote-arquivo').textContent = l.arquivo || '';
                $('lote-resumo').textContent = 'Enviado por ' + (l.enviadoPor || '?') + (l.criadoEm ? ' em ' + formatarData(l.criadoEm) : '')
                    + '. ' + plural(l.enviadas, 'linha aguardando decisão', 'linhas aguardando decisão') + '.';
                $('contagem-conflito').textContent = String(l.conflitos || 0);
            } catch (e) {
                $('lote-resumo').textContent = '';
                mostrar(erroGeral, e.message);
            }
        }

        // ---- listagem das linhas enviadas ----

        let seq = 0; // descarta respostas antigas quando o filtro muda no meio de uma carga
        async function carregarLinhas() {
            const f = filtroAtual();
            if (f === null) {
                totalFiltrado = null;
                limpar(lista);
                paginas.hidden = true;
                mostrarContagem();
                return;
            }
            const minha = ++seq;
            try {
                const dados = await api(consultaLinhas(f, pagina, TAMANHO_PAGINA));
                if (minha !== seq) return;
                if (dados.totalPaginas > 0 && pagina >= dados.totalPaginas) {
                    pagina = dados.totalPaginas - 1; // a última página esvaziou depois de uma ação
                    return carregarLinhas();
                }
                totalFiltrado = dados.total;
                limpar(lista);
                if (dados.linhas.length === 0) {
                    lista.append(el('li', { classe: 'vazio-msg', texto: 'Nenhuma linha aguardando decisão neste filtro.' }));
                }
                dados.linhas.forEach((l) => lista.append(desenharLinha(l)));
                desenharPaginas(paginas, dados, (pg) => { pagina = pg; carregarLinhas(); });
                mostrarContagem();
            } catch (e) {
                if (minha !== seq) return;
                totalFiltrado = null;
                limpar(lista);
                lista.append(el('li', { classe: 'msg-erro', texto: e.message }));
                paginas.hidden = true;
                info.textContent = 'Não foi possível contar as linhas.';
                atualizarBotoes();
            }
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

        function linhaBase(l, rotuloEstado) {
            const li = el('li');
            li.append(el('span', { classe: 'num', texto: '#' + l.numero }));
            li.append(el('span', { classe: 'texto', texto: l.textoBreve || '(sem texto)' }));
            li.append(el('span', { classe: 'estado', texto: rotuloEstado }));
            return li;
        }

        function detalhesDaLinha(l, li) {
            if (l.situacao === 'PARECIDA') {
                const det = el('span', { classe: 'detalhe' });
                const alvo = el('span', { texto: 'carregando serviço parecido...' });
                det.append(el('strong', { texto: pct(l.score) + ' parecido com ' }), alvo);
                li.append(det);
                preencherParecido(alvo, l.servicoParecidoId);
                if (l.decisao) {
                    li.append(el('span', { classe: 'detalhe', texto: 'O comprador decidiu: ' + (DECISOES[l.decisao] || l.decisao) + '.' }));
                }
            } else if (l.codigoGerado) {
                const det = el('span', { classe: 'detalhe' });
                det.append('Código proposto: ', el('code', { texto: l.codigoGerado }));
                li.append(det);
            }
            const partes = [];
            if (l.areaServico) partes.push('Área: ' + l.areaServico);
            if (l.condicao) partes.push('Condição: ' + l.condicao);
            if (l.cnae) partes.push('CNAE: ' + l.cnae);
            if (l.conferidoPor) partes.push('Conferido por: ' + l.conferidoPor
                + (l.conferidoEm ? ' em ' + new Date(l.conferidoEm).toLocaleString('pt-BR') : ''));
            if (partes.length) li.append(el('span', { classe: 'detalhe', texto: partes.join(' · ') }));
        }

        function desenharLinha(l) {
            const li = linhaBase(l, l.situacao === 'PARECIDA' ? 'Parecida' : 'Nova');
            detalhesDaLinha(l, li);

            const acoes = el('span', { classe: 'acoes' });
            const aviso = el('span', { classe: 'msg-erro', role: 'alert' });
            const aprovar = el('button', { type: 'button', texto: 'Aprovar' });
            const rejeitar = el('button', { type: 'button', texto: 'Rejeitar' });
            acoes.append(aprovar, rejeitar, aviso);
            li.append(acoes);

            const base = urlAprovacao + '/linhas/' + encodeURIComponent(String(l.id));
            const travar = (v) => { aprovar.disabled = v; rejeitar.disabled = v; };

            aprovar.addEventListener('click', async () => {
                travar(true);
                aviso.textContent = '';
                try {
                    const r = await postJson(base + '/aprovar');
                    mostrar(msgGeral, r && r.etapa === 'CONFLITO'
                        ? 'A linha #' + l.numero + ' foi para conflito (veja o motivo no bloco de conflitos).'
                        : 'Linha #' + l.numero + ' aprovada.');
                    await depoisDeAcao();
                } catch (e) {
                    aviso.textContent = e.message;
                    travar(false);
                    if (e.status === 409) depoisDeAcao().catch(() => {});
                }
            });

            rejeitar.addEventListener('click', () => {
                const existente = li.querySelector('.rejeicao');
                if (existente) {
                    existente.remove();
                    return;
                }
                const form = el('form', { classe: 'rejeicao' });
                const campo = el('input', { type: 'text', maxlength: '500', autocomplete: 'off', 'aria-label': 'Motivo da rejeição da linha ' + l.numero, placeholder: 'Motivo da rejeição' });
                const ok = el('button', { type: 'submit', texto: 'Confirmar rejeição' });
                const cancelar = el('button', { type: 'button', texto: 'Cancelar' });
                cancelar.addEventListener('click', () => form.remove());
                form.append(campo, ok, cancelar);
                form.addEventListener('submit', async (ev) => {
                    ev.preventDefault();
                    const motivo = campo.value.trim();
                    if (!motivo) {
                        aviso.textContent = 'Informe o motivo da rejeição.';
                        campo.focus();
                        return;
                    }
                    ok.disabled = true;
                    travar(true);
                    aviso.textContent = '';
                    try {
                        await postJson(base + '/rejeitar', { motivo });
                        mostrar(msgGeral, 'Linha #' + l.numero + ' rejeitada.');
                        await depoisDeAcao();
                    } catch (e) {
                        aviso.textContent = e.message;
                        ok.disabled = false;
                        travar(false);
                        if (e.status === 409) depoisDeAcao().catch(() => {});
                    }
                });
                li.append(form);
                campo.focus();
            });
            return li;
        }

        // Recarrega cabeçalho, a página atual e, se aberto, o bloco de conflitos
        async function depoisDeAcao() {
            await Promise.all([
                atualizarCabecalho(),
                carregarLinhas(),
                conflitos.carregado ? carregarConflitos() : Promise.resolve()
            ]);
        }

        // ---- ação em bloco ----

        // Conta de novo no servidor, com o mesmo filtro, logo antes de confirmar
        async function contarAgora(f) {
            const dados = await api(consultaLinhas(f, 0, 1));
            return dados.total;
        }

        async function emBloco(rejeitar) {
            const f = filtroAtual();
            if (f === null) {
                mostrar(erroGeral, 'Informe a similaridade mínima entre 0 e 100.');
                return;
            }
            const motivo = motivoBloco.value.trim();
            if (rejeitar && !motivo) {
                mostrar(erroGeral, 'Para rejeitar em bloco, informe o motivo.');
                motivoBloco.focus();
                return;
            }
            mostrar(erroGeral, '');
            mostrar(msgGeral, '');
            ocupado = true;
            atualizarBotoes();
            try {
                const total = await contarAgora(f);
                totalFiltrado = total;
                mostrarContagem();
                if (total === 0) {
                    mostrar(msgGeral, 'Nenhuma linha neste filtro.');
                    return;
                }
                const o = descreverFiltro(f);
                let pergunta;
                if (rejeitar) {
                    pergunta = 'Rejeitar ' + plural(total, 'linha', 'linhas') + ' (' + o + ') com o motivo "' + motivo + '"?'
                        + (f.situacao ? '' : '\n\nNenhum filtro está aplicado: isto rejeita TODAS as linhas aguardando decisão deste lote.')
                        + '\n\nEsta ação não pode ser desfeita.';
                } else {
                    pergunta = 'Aprovar ' + plural(total, 'linha', 'linhas') + ' (' + o + ')?\n\nOs códigos serão criados no catálogo.';
                }
                if (!window.confirm(pergunta)) return;

                const corpo = corpoDoFiltro(f);
                if (rejeitar) {
                    const r = await postJson(urlAprovacao + '/' + encodeURIComponent(loteId) + '/rejeitar-em-bloco', Object.assign({ motivo }, corpo));
                    mostrar(msgGeral, plural(r.rejeitadas, 'linha rejeitada', 'linhas rejeitadas') + '.');
                    motivoBloco.value = '';
                } else {
                    const r = await postJson(urlAprovacao + '/' + encodeURIComponent(loteId) + '/aprovar-em-bloco', corpo);
                    mostrar(msgGeral, resumoAprovacao(r));
                }
                pagina = 0;
            } catch (e) {
                mostrar(erroGeral, e.message);
            } finally {
                ocupado = false;
                await depoisDeAcao().catch(() => {});
                atualizarBotoes();
            }
        }

        function resumoAprovacao(r) {
            let t = plural(r.aprovadas, 'linha aprovada', 'linhas aprovadas') + '. ' + plural(r.conflitos, 'linha foi para conflito', 'linhas foram para conflito') + '.';
            if (r.falhas > 0) {
                t += ' ' + r.falhas + (r.falhas === 1 ? ' linha não pôde' : ' linhas não puderam') + ' ser processada' + (r.falhas === 1 ? '' : 's')
                    + ' agora e continuam aguardando; tente de novo.';
            }
            return t;
        }

        btnAprovar.addEventListener('click', () => emBloco(false));
        btnRejeitar.addEventListener('click', () => emBloco(true));

        // ---- filtros ----

        tipo.addEventListener('change', () => {
            scoreCampo.hidden = tipo.value !== 'PARECIDA';
            if (tipo.value !== 'PARECIDA') score.value = ''; // o mínimo só vale junto com PARECIDA
            pagina = 0;
            totalFiltrado = null;
            mostrarContagem();
            carregarLinhas();
        });
        score.addEventListener('change', () => {
            pagina = 0;
            totalFiltrado = null;
            mostrarContagem();
            carregarLinhas();
        });

        // ---- conflitos (só leitura, carregado ao abrir) ----

        const conflitos = { carregado: false, pagina: 0 };
        async function carregarConflitos() {
            const status = $('conflito-status');
            const ul = $('conflitos');
            conflitos.carregado = true;
            try {
                const p = new URLSearchParams({ etapa: 'CONFLITO', pagina: String(conflitos.pagina), tamanho: String(TAMANHO_PAGINA) });
                const dados = await api(urlApi + '/linhas?' + p.toString());
                if (dados.totalPaginas > 0 && conflitos.pagina >= dados.totalPaginas) {
                    conflitos.pagina = dados.totalPaginas - 1;
                    return carregarConflitos();
                }
                status.textContent = dados.total === 0 ? 'Nenhuma linha em conflito.' : '';
                limpar(ul);
                dados.linhas.forEach((l) => {
                    const li = linhaBase(l, 'Conflito');
                    li.append(el('span', { classe: 'detalhe motivo', texto: l.motivo || 'Sem motivo registrado.' }));
                    detalhesDaLinha(l, li);
                    ul.append(li);
                });
                desenharPaginas($('paginas-conflito'), dados, (pg) => { conflitos.pagina = pg; carregarConflitos(); });
            } catch (e) {
                status.textContent = e.message;
            }
        }

        conflito.addEventListener('toggle', () => {
            if (conflito.open && !conflitos.carregado) carregarConflitos();
        });

        mostrarContagem();
        atualizarCabecalho();
        carregarLinhas();
    }

    document.addEventListener('DOMContentLoaded', () => {
        const lotes = document.getElementById('aprovacao-lotes');
        if (lotes) iniciarLotes(lotes);
        const lote = document.getElementById('aprovacao-lote');
        if (lote) iniciarLote(lote);
    });
})();
