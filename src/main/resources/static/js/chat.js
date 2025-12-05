/**
 * Отдельный скрипт для страницы чата
 * ✅ Рефакторинг: весь чат-функционал вынесен сюда
 */
document.addEventListener('DOMContentLoaded', async () => {
    console.log('✅ DOM загружен, инициализация чата');

    // Устанавливаем текущий год
    const yearEl = document.getElementById('year');
    if (yearEl) yearEl.textContent = new Date().getFullYear();

    // Кнопка "Назад"
    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        backBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = '/chats-matches';
        });
    }

    // Элементы чата
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const chatTitle = document.getElementById('chatTitle');

    const currentChatId = window.currentChatId;
    const interlocutorName = window.interlocutorName;
    const currentUserId = window.currentUserId;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

    if (!chatMessages || !chatInput || !chatSendBtn || !currentChatId || !interlocutorName) {
        console.error('❌ Один или несколько элементов чата не найдены');
        return;
    }

    // Устанавливаем заголовок
    chatTitle.textContent = `Чат с ${interlocutorName}`;

    // WebSocket
    let stompClient = null;

    function connectToChatWebSocket() {
        const socket = new SockJS('/ws', null, {
            transports: ['xhr-polling', 'xhr-streaming']
        });

        stompClient = Stomp.over(socket);
        stompClient.debug = function(str) {
            console.log('💌 STOMP Debug:', str);
        };

        stompClient.connect({}, function(frame) {
            console.log('✅ Подключено к WebSocket:', frame);

            // Подписываемся на /topic/chat.{id}
            stompClient.subscribe(`/topic/chat.${currentChatId}`, function(message) {
                const msg = JSON.parse(message.body);
                console.log('📩 Получено сообщение:', msg);

                // Фильтр дублей
                if (msg.id && document.querySelector(`.message[data-message-id="${msg.id}"]`)) {
                    return;
                }

                const isSent = msg.sender.userId === currentUserId;
                const senderName = isSent ? 'Вы' : msg.sender.username;

                appendMessage(msg.id, senderName, msg.content, isSent, msg.sentAt);
            });
        }, function(error) {
            console.error('❌ Ошибка подключения STOMP:', error);
            setTimeout(connectToChatWebSocket, 10000);
        });
    }

    // Отправка сообщения
    function sendMessage(content) {
        if (!stompClient || !stompClient.connected) {
            console.warn('WebSocket не подключён');
            return;
        }

        const message = {
            content: content,
            chat: { id: currentChatId }
        };

        stompClient.send("/app/chat.send", {}, JSON.stringify(message));
    }

    // Загрузка истории
    async function fetchHistory() {
        try {
            const response = await fetch(`/sparkle/chats/${currentChatId}/history`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response.status === 204) return;

            if (!response.ok) throw new Error('Не удалось загрузить историю');

            const messages = await response.json();
            messages.forEach(msg => {
                const isSent = msg.sender.userId === currentUserId;
                const senderName = isSent ? 'Вы' : msg.sender.username;
                appendMessage(msg.id, senderName, msg.content, isSent, msg.sentAt);
            });
        } catch (err) {
            console.error('Ошибка загрузки истории:', err);
            chatMessages.innerHTML = '<div class="error">Не удалось загрузить сообщения</div>';
        }
    }

    // Отображение сообщения
    function appendMessage(id, sender, content, isSent, sentAt) {
        let timeString = 'Недавно';
        if (sentAt) {
            const dateStr = sentAt.split('.')[0];
            const date = new Date(dateStr);
            timeString = isNaN(date.getTime())
                ? 'Недавно'
                : date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;
        if (id) {
            messageDiv.dataset.messageId = id;
        }
        messageDiv.innerHTML = `
            <strong>${sender}:</strong> ${content}
            <div class="message-time">
                ${timeString}
                ${isSent ? `<button class="delete-message" onclick="deleteMessage(${id || 'null'})">×</button>` : ''}
            </div>
        `;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Отправка
    chatSendBtn?.addEventListener('click', () => {
        const content = chatInput.value.trim();
        if (!content) return;
        sendMessage(content);
        chatInput.value = '';
    });

    chatInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            const content = chatInput.value.trim();
            if (!content) return;
            sendMessage(content);
            chatInput.value = '';
        }
    });

    // Удаление сообщения
    window.deleteMessage = async function (messageId) {
        if (!confirm('Удалить сообщение?')) return;
        try {
            const response = await fetch(`/sparkle/chats/message/${messageId}`, {
                method: 'DELETE',
                headers: {
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });
            if (response.ok) {
                document.querySelector(`.message[data-message-id="${messageId}"]`)?.remove();
            } else {
                alert('Не удалось удалить сообщение');
            }
        } catch (err) {
            console.error('Ошибка при удалении сообщения:', err);
            alert('Ошибка при удалении');
        }
    };

    // Запуск
    connectToChatWebSocket();
    fetchHistory();
});