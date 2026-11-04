document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('busca');

    if (input) {
        document.addEventListener('keydown', (e) => {
            const emCampo = /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement.tagName);
            if (e.key === '/' && !emCampo) {
                e.preventDefault();
                input.focus();
            }
        });
    }

    document.querySelectorAll('.copiar').forEach((botao) => {
        botao.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(botao.dataset.codigo);
                botao.textContent = 'Copiado';
                botao.classList.add('copiado');
                setTimeout(() => {
                    botao.textContent = 'Copiar';
                    botao.classList.remove('copiado');
                }, 1500);
            } catch (e) {
                botao.textContent = 'Falhou';
            }
        });
    });
});

// Serviços: mostra o código em partes coloridas
document.addEventListener('DOMContentLoaded', () => {
    const span = (classe, texto) => {
        const el = document.createElement('span');
        el.className = classe;
        el.textContent = texto;
        return el;
    };

    // código = abreviatura + complemento + "_" + sufixo da condição + CNAE; sem casar, fica como veio
    document.querySelectorAll('code[data-segmentar]').forEach((code) => {
        const texto = code.textContent;
        const abrev = code.dataset.abrev || '';
        const compl = code.dataset.compl || '';
        const cnae = code.dataset.cnae || '';
        const inicio = abrev + compl;
        const sep = texto.indexOf('_', inicio.length);
        if (!abrev || !texto.startsWith(inicio) || sep !== inicio.length || !texto.endsWith(cnae)) {
            return;
        }
        const fimCond = texto.length - cnae.length;
        if (fimCond <= sep) {
            return;
        }
        code.textContent = '';
        code.append(span('p-abrev', abrev));
        if (compl) code.append(span('p-compl', compl));
        code.append(span('p-sep', '_'), span('p-cond', texto.slice(sep + 1, fimCond)));
        if (cnae) code.append(span('p-cnae', cnae));
    });
});
