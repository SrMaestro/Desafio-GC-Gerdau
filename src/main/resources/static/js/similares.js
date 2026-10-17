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
            const codigo = document.createElement('code');
            codigo.textContent = s.codigoPadronizado;
            li.append(link, ' — ', codigo, ` (${Math.round(s.score * 100)}% parecido)`);
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
