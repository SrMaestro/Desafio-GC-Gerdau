/*
 * Funções compartilhadas pelas telas de importação (comprador e aprovador).
 * O conteúdo da planilha não é confiável: monte a página só com textContent / createElement / setAttribute.
 */
(function () {
    'use strict';

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

    window.Comum = { el, limpar, ErroApi, mensagemPadrao, api, postJson, mostrar, pct, plural };
})();
