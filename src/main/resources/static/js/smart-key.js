document.addEventListener('DOMContentLoaded', () => {
    const termo = document.getElementById('termo');
    const lista = document.getElementById('sugestoes');
    const horario = document.getElementById('horario');
    const selecionado = document.getElementById('selecionado');
    const chave = document.getElementById('chave');
    const aviso = document.getElementById('aviso');
    if (!termo) return;

    let escolhido = null;   // { codigoNbs, descricao, score }
    let origem = null;      // EXATA | FUZZY
    let timer = null;

    const json = async (url, opts) => {
        const r = await fetch(url, opts);
        if (!r.ok) throw new Error(r.status);
        return r.json();
    };

    const limparResultado = () => {
        chave.hidden = true;
        aviso.hidden = true;
    };

    termo.addEventListener('input', () => {
        clearTimeout(timer);
        escolhido = null;
        horario.disabled = true;
        horario.value = '';
        selecionado.hidden = true;
        limparResultado();
        const valor = termo.value.trim();
        if (valor.length < 3) { lista.hidden = true; return; }
        timer = setTimeout(() => buscar(valor), 250);
    });

    async function buscar(valor) {
        try {
            const r = await json('/api/nbs/buscar?termo=' + encodeURIComponent(valor));
            origem = r.origem;
            lista.replaceChildren();
            r.sugestoes.forEach((s) => {
                const li = document.createElement('li');
                const texto = document.createElement('span');
                texto.textContent = `${s.descricao} (${s.codigoNbs})`;
                const score = document.createElement('span');
                score.className = 'score';
                score.textContent = Math.round((s.score ?? 0) * 100) + '%';
                li.append(texto, score);
                li.addEventListener('click', () => escolher(s));
                lista.appendChild(li);
            });
            lista.hidden = r.sugestoes.length === 0;
            if (r.sugestoes.length === 0) mostrarAviso('Nenhuma sugestão encontrada.');
        } catch (e) {
            mostrarAviso('Erro ao buscar sugestões.');
        }
    }

    function escolher(s) {
        escolhido = s;
        lista.hidden = true;
        selecionado.textContent = `Selecionado: ${s.descricao} (${s.codigoNbs})`;
        selecionado.hidden = false;
        horario.disabled = false;
        limparResultado();
    }

    horario.addEventListener('change', async () => {
        if (!escolhido || !horario.value) { limparResultado(); return; }
        try {
            const r = await json(`/api/smart-key?nbs=${encodeURIComponent(escolhido.codigoNbs)}&horario=${encodeURIComponent(horario.value)}`);
            chave.textContent = r.chave;
            chave.hidden = false;
            if (origem === 'FUZZY') await registrar();
        } catch (e) {
            mostrarAviso('Não foi possível gerar a chave.');
        }
    });

    // Aprendizado contínuo: grava o termo digitado no De-Para quando veio de busca fuzzy.
    async function registrar() {
        const r = await json('/api/nbs/confirmar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ termo: termo.value, codigoNbs: escolhido.codigoNbs, score: escolhido.score })
        });
        if (r.gravado) mostrarAviso('Termo registrado: na próxima vez esta busca será exata.');
    }

    function mostrarAviso(msg) {
        aviso.textContent = msg;
        aviso.hidden = false;
    }
});
