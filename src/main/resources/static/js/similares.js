document.addEventListener('DOMContentLoaded', () => {
    const campo = document.querySelector('[name="textoBreve"]');
    const caixa = document.getElementById('similares-ao-digitar');
    const lista = document.getElementById('similares-lista');
    if (!campo || !caixa || !campo.dataset.url) return;

    const ESPERA_MS = 300;
    let timer;
    let ultimaBusca = 0;

    function mostrar(similares) {
        lista.replaceChildren();
        similares.forEach((s) => {
            const li = document.createElement('li');
            const link = document.createElement('a');
            link.href = `/ui/servicos/${s.id}`;
            link.target = '_blank';
            link.rel = 'noopener';
            link.textContent = s.textoBreve;
            const score = document.createElement('span');
            score.className = 'score';
            score.textContent = `${Math.round(s.score * 100)}% parecido`;
            const codigo = document.createElement('code');
            codigo.textContent = s.codigoPadronizado;
            // Botão dentro do formulário: leva o texto digitado e o token CSRF, e formnovalidate dispensa os outros campos
            const usar = document.createElement('button');
            usar.type = 'submit';
            usar.className = 'usar';
            usar.name = 'servicoId';
            usar.value = s.id;
            usar.textContent = 'Usar este código';
            usar.setAttribute('formaction', campo.dataset.urlReutilizar);
            usar.setAttribute('formmethod', 'post');
            usar.setAttribute('formnovalidate', '');
            li.append(link, codigo, score, usar);
            lista.appendChild(li);
        });
        caixa.hidden = similares.length === 0;
    }

    async function buscar() {
        const texto = campo.value.trim();
        const numero = ++ultimaBusca;
        if (texto.length < 3) {
            mostrar([]);
            return;
        }
        try {
            const resp = await fetch(`${campo.dataset.url}?texto=${encodeURIComponent(texto)}`);
            if (!resp.ok) throw new Error();
            const similares = await resp.json();
            if (numero === ultimaBusca) mostrar(similares); // descarta respostas antigas
        } catch (e) {
            if (numero === ultimaBusca) mostrar([]);
        }
    }

    campo.addEventListener('input', () => {
        clearTimeout(timer);
        timer = setTimeout(buscar, ESPERA_MS);
    });
    buscar();
});
