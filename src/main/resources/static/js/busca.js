document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('busca-nbs');
    if (!input) return;

    const lista = document.getElementById('busca-lista');
    const status = document.getElementById('busca-status');
    const urlBusca = input.dataset.url;
    const urlConfirmar = input.dataset.confirmar;
    let timer;
    let ultimaBusca = 0;

    const pct = (score) => Math.round(score * 100) + '% parecido';

    function limpar() {
        lista.replaceChildren();
    }

    function item(sugestao, tipo, texto) {
        const li = document.createElement('li');
        const info = document.createElement('div');
        const codigo = document.createElement('code');
        codigo.textContent = sugestao.codigoNbs;
        const descricao = document.createElement('span');
        descricao.textContent = ' ' + sugestao.descricao;
        info.append(codigo, descricao);
        li.appendChild(info);

        if (tipo === 'EXATO') {
            const selo = document.createElement('span');
            selo.className = 'selo';
            selo.textContent = 'Aprovado';
            li.appendChild(selo);
        } else {
            const detalhe = document.createElement('span');
            detalhe.className = 'score';
            detalhe.textContent = pct(sugestao.score);
            const botao = document.createElement('button');
            botao.type = 'button';
            botao.className = 'copiar';
            botao.textContent = 'Usar este';
            botao.addEventListener('click', () => confirmar(sugestao, texto, botao));
            li.append(detalhe, botao);
        }
        return li;
    }

    async function confirmar(sugestao, texto, botao) {
        botao.disabled = true;
        try {
            const resp = await fetch(urlConfirmar, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({nomeDigitado: texto, codigoNbs: sugestao.codigoNbs, score: sugestao.score})
            });
            if (!resp.ok) throw new Error();
            botao.textContent = 'Registrado';
            botao.classList.add('copiado');
            status.textContent = 'Escolha registrada para revisão.';
        } catch (e) {
            botao.disabled = false;
            status.textContent = 'Não foi possível registrar a escolha. Tente de novo.';
        }
    }

    async function buscar() {
        const texto = input.value.trim();
        const id = ++ultimaBusca;
        if (texto.length < 2) {
            limpar();
            status.textContent = '';
            return;
        }
        try {
            const resp = await fetch(urlBusca + '?texto=' + encodeURIComponent(texto));
            if (!resp.ok) throw new Error();
            const dados = await resp.json();
            if (id !== ultimaBusca) return; // resposta antiga
            limpar();
            if (dados.resultados.length === 0) {
                status.textContent = 'Nenhum código parecido. Tente outras palavras.';
                return;
            }
            status.textContent = dados.tipo === 'EXATO'
                ? 'Este termo já tem um código aprovado.'
                : 'Sugestões ainda não revisadas. Escolha a que corresponde.';
            dados.resultados.forEach((s) => lista.appendChild(item(s, dados.tipo, texto)));
        } catch (e) {
            status.textContent = 'Não foi possível buscar agora. Tente de novo.';
        }
    }

    input.addEventListener('input', () => {
        clearTimeout(timer);
        timer = setTimeout(buscar, 250);
    });
});
