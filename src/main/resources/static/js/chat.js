/**
 * Sistema de Chat Interno - ERP Corporativo
 * Implementação completa do cliente WebSocket com STOMP
 */
document.getElementById('newChatBtn').addEventListener('click', async () => {
    try {
        // Buscar todos os usuários da API
        const response = await fetch('/chat/usuarios');
        if (!response.ok) throw new Error('Erro ao buscar usuários');

        const usuarios = await response.json();

        // Preencher a lista no modal
        const userList = document.getElementById('userList');
        userList.innerHTML = ''; // Limpar lista anterior
        usuarios.forEach(user => {
            const li = document.createElement('li');
            li.textContent = user.nome;
            li.style.cursor = 'pointer';
            li.addEventListener('click', () => {
                alert(`Iniciando conversa com ${user.nome}`);
                closeModal();
            });
            userList.appendChild(li);
        });

        // Abrir modal
        openModal();

    } catch (error) {
        console.error(error);
        alert('Não foi possível carregar os usuários.');
    }
});

document.getElementById('closeModalBtn').addEventListener('click', closeModal);

function openModal() {
    const modal = document.getElementById('userModal');
    modal.style.display = 'flex';
}

function closeModal() {
    const modal = document.getElementById('userModal');
    modal.style.display = 'none';
}
