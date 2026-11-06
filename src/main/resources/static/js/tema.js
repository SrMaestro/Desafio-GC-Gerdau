// Tema claro/escuro: segue o sistema até a pessoa escolher; a escolha fica neste navegador.
(function () {
    const raiz = document.documentElement;
    const escuroDoSistema = window.matchMedia('(prefers-color-scheme: dark)');
    const guardado = () => { try { return localStorage.getItem('tema'); } catch (e) { return null; } };
    const aplicar = () => { raiz.dataset.tema = guardado() || (escuroDoSistema.matches ? 'dark' : 'light'); };
    aplicar();
    escuroDoSistema.addEventListener('change', aplicar);

    document.addEventListener('DOMContentLoaded', () => {
        const botao = document.getElementById('alternar-tema');
        if (!botao) return;
        const rotular = () => {
            const escuro = raiz.dataset.tema === 'dark';
            botao.setAttribute('aria-pressed', String(escuro));
            botao.querySelector('span').textContent = escuro ? 'Tema claro' : 'Tema escuro';
        };
        rotular();
        botao.addEventListener('click', () => {
            const novo = raiz.dataset.tema === 'dark' ? 'light' : 'dark';
            try { localStorage.setItem('tema', novo); } catch (e) { /* sem armazenamento: vale só nesta página */ }
            raiz.dataset.tema = novo;
            rotular();
        });
    });
})();
