document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('busca-nbs');
    if (!input) return;

    const lista = document.getElementById('busca-lista');
    const status = document.getElementById('busca-status');
    const urlBusca = input.dataset.url;
    const urlConfirmar = input.dataset.confirmar;
    const urlHorarios = input.dataset.horarios;
    const urlSmartKey = input.dataset.smartKey;
    const urlCadastrar = input.dataset.cadastrar;

    const painel = document.getElementById('smart-key');
    const painelNbs = document.getElementById('sk-nbs');
    const seletor = document.getElementById('sk-horario');
    const chave = document.getElementById('sk-chave');
    const copiar = document.getElementById('sk-copiar');
    const erro = document.getElementById('sk-erro');
    const cadastrar = document.getElementById('sk-cadastrar');

    let timer;
    let ultimaBusca = 0;
    let nbsEscolhido = null;
    let horariosCarregados = null;

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

        const botao = document.createElement('button');
        botao.type = 'button';
        botao.className = 'copiar';
        botao.textContent = 'Usar este';

        if (tipo === 'EXATO') {
            const selo = document.createElement('span');
            selo.className = 'selo';
            selo.textContent = 'Aprovado';
            botao.addEventListener('click', () => escolher(sugestao));
            li.append(selo, botao);
        } else {
            const detalhe = document.createElement('span');
            detalhe.className = 'score';
            detalhe.textContent = pct(sugestao.score);
            botao.addEventListener('click', () => {
                escolher(sugestao);
                confirmar(sugestao, texto, botao);
            });
            li.append(detalhe, botao);
        }
        return li;
    }

    function cabecalhoCsrf() {
        const token = document.querySelector('meta[name="_csrf"]');
        const nome = document.querySelector('meta[name="_csrf_header"]');
        return token && nome ? {[nome.content]: token.content} : {};
    }

    async function confirmar(sugestao, texto, botao) {
        botao.disabled = true;
        try {
            const resp = await fetch(urlConfirmar, {
                method: 'POST',
                headers: {'Content-Type': 'application/json', ...cabecalhoCsrf()},
                body: JSON.stringify({nomeDigitado: texto, codigoNbs: sugestao.codigoNbs, score: sugestao.score})
            });
            if (!resp.ok) throw new Error();
            botao.textContent = 'Registrado';
            botao.classList.add('copiado');
            status.textContent = 'Escolha registrada para revisão.';
        } catch (e) {
            botao.disabled = false;
            status.textContent = 'Não foi possível registrar a escolha para revisão, mas você pode copiar o código abaixo.';
        }
    }

    async function carregarHorarios() {
        if (horariosCarregados) return horariosCarregados;
        const resp = await fetch(urlHorarios);
        if (!resp.ok) throw new Error();
        horariosCarregados = await resp.json();
        horariosCarregados.forEach((h) => {
            const opcao = document.createElement('option');
            opcao.value = h.sufixo;
            opcao.textContent = h.descricao + ' (' + h.sufixo + ')';
            seletor.appendChild(opcao);
        });
        return horariosCarregados;
    }

    async function escolher(sugestao) {
        nbsEscolhido = sugestao;
        chave.textContent = '';
        erro.textContent = '';
        cadastrar.hidden = true;
        copiar.textContent = 'Copiar';
        copiar.classList.remove('copiado');
        painel.hidden = false;
        painelNbs.replaceChildren();
        const codigo = document.createElement('code');
        codigo.textContent = sugestao.codigoNbs;
        painelNbs.append(codigo, ' ' + sugestao.descricao);
        try {
            await carregarHorarios();
            await gerarChave();
        } catch (e) {
            chave.textContent = '';
            erro.textContent = 'Não foi possível carregar os horários. Tente de novo.';
        }
    }

    async function gerarChave() {
        if (!nbsEscolhido) return;
        const nbs = nbsEscolhido.codigoNbs;
        const horario = seletor.value;
        const url = urlSmartKey + '?nbs=' + encodeURIComponent(nbs) + '&horario=' + encodeURIComponent(horario);
        const resp = await fetch(url);
        if (nbs !== nbsEscolhido.codigoNbs || horario !== seletor.value) return; // escolha mudou durante a chamada
        if (!resp.ok) {
            chave.textContent = '';
            erro.textContent = 'Não foi possível montar o código. Tente de novo.';
            return;
        }
        const dados = await resp.json();
        erro.textContent = '';
        chave.textContent = dados.chave;
        cadastrar.href = urlCadastrar + '?nbs=' + encodeURIComponent(nbs) + '&horario=' + encodeURIComponent(horario);
        cadastrar.hidden = false;
        copiar.textContent = 'Copiar';
        copiar.classList.remove('copiado');
    }

    seletor.addEventListener('change', () => {
        gerarChave().catch(() => {
            erro.textContent = 'Não foi possível montar o código. Tente de novo.';
        });
    });

    copiar.addEventListener('click', async () => {
        if (!chave.textContent) return;
        try {
            await navigator.clipboard.writeText(chave.textContent);
            copiar.textContent = 'Copiado';
            copiar.classList.add('copiado');
        } catch (e) {
            copiar.textContent = 'Falhou';
        }
    });

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
