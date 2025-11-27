/**
 * Основной скрипт для страницы чатов и мэтчей
 * Управляет вкладками, загрузкой данных, открытием чатов и отправкой сообщений
 */
document.addEventListener('DOMContentLoaded', async () => {





    console.log('✅ DOM загружен, инициализация скрипта');

    // Устанавливаем текущий год в футер
    const yearEl = document.getElementById('year');
    if (yearEl) {
        yearEl.textContent = new Date().getFullYear();
    }

    // Кнопка "Назад"
    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        console.log('✅ Кнопка "Назад" найдена');
        backBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = '/main';
        });
    } else {
        console.error('❌ Кнопка #backBtn не найдена');
    }

    // Элементы модального окна чата
    const chatModal = document.getElementById('chatModal');
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const chatCloseBtn = document.getElementById('chatCloseBtn');
    const chatHeader = document.getElementById('chatHeader');

    let currentChatId = null;
    const currentUserId = window.currentUserId;

    // Проверка обязательных элементов чата
    if (!chatModal || !chatMessages || !chatInput || !chatSendBtn || !chatCloseBtn || !chatHeader) {
        console.error('❌ Один или несколько элементов чата не найдены');
    }

    // Вкладки (секции)
    const tabs = document.querySelectorAll('.tab');
    const panes = document.querySelectorAll('.tab-pane');

    if (tabs.length === 0) {
        console.error('❌ Вкладки (.tab) не найдены');
    }

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const target = tab.dataset.tab;

            // Активация вкладки
            tabs.forEach(t => {
                t.classList.remove('active');
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');

            // Показ нужной панели
            panes.forEach(pane => pane.classList.remove('active'));
            const targetPane = document.getElementById(target);
            if (targetPane) {
                targetPane.classList.add('active');
            } else {
                console.warn(`❌ Панель с id="${target}" не найдена`);
            }

            // Загрузка данных
            loadData(target);
        });
    });

    // Проверка наличия всех необходимых элементов
    const requiredIds = [
        'matchesLoader', 'matchesList', 'noMatches',
        'chatsLoader', 'chatsList', 'noChats',
        'likesLoader', 'likesList', 'noLikes',
        'whoLikedLoader', 'whoLikedList', 'noWhoLiked'
    ];

    requiredIds.forEach(id => {
        if (!document.getElementById(id)) {
            console.warn(`⚠️ Элемент #${id} не найден в DOM`);
        }
    });

    // Инициализация: загружаем вкладку "matches" при старте
    loadData('matches');

    // === Загрузка данных для вкладки ===
    async function loadData(tab) {
        const endpoints = {
            matches: '/sparkle/users/match/current-matches',
            chats: '/sparkle/chats/users',
            'my-likes': '/sparkle/users/match/like/current-lake-your',
            'who-liked-me': '/sparkle/users/match/like/current-lake-who'
        };

        const loaderId = { matches: 'matchesLoader', chats: 'chatsLoader', 'my-likes': 'likesLoader', 'who-liked-me': 'whoLikedLoader' };
        const listId = { matches: 'matchesList', chats: 'chatsList', 'my-likes': 'likesList', 'who-liked-me': 'whoLikedList' };
        const noDataId = { matches: 'noMatches', chats: 'noChats', 'my-likes': 'noLikes', 'who-liked-me': 'noWhoLiked' };

        const loader = document.getElementById(loaderId[tab]);
        const list = document.getElementById(listId[tab]);
        const noData = document.getElementById(noDataId[tab]);

        if (!loader || !list || !noData) {
            console.error(`❌ Не все элементы найдены для вкладки: ${tab}`);
            return;
        }

        loader.style.display = 'block';
        list.innerHTML = '';
        noData.style.display = 'none';

        try {
            const response = await fetch(endpoints[tab], {
                method: 'GET',
                credentials: 'include'
            });

            if (response.status === 204 || response.status === 404) {
                loader.style.display = 'none';
                noData.style.display = 'block';
                return;
            }

            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

            const data = await response.json();

            if (!data || data.length === 0) {
                loader.style.display = 'none';
                noData.style.display = 'block';
                return;
            }

            // Обработка чатов
            if (tab === 'chats') {
                data.forEach(chat => {
                    const interlocutor = chat.users.find(u => u.userId !== currentUserId);
                    if (!interlocutor) return;

                    const li = createUserItemWithActions(interlocutor, tab, chat.chatId);
                    list.appendChild(li);
                });
            }
            // Обработка остальных вкладок
            else {
                data.forEach(item => {
                    const user = item.user || item;
                    const userId = user.userId || user.id;
                    if (!userId) return;

                    const id = tab === 'chats' ? item.chatId : userId;
                    const li = createUserItemWithActions(user, tab, id);
                    list.appendChild(li);
                });
            }

            noData.style.display = 'none';
        } catch (error) {
            if (error.name !== 'AbortError') {
                console.error('❌ Ошибка загрузки:', tab, error);
            }
            noData.style.display = 'block';
            noData.textContent = 'Ошибка загрузки данных';
        } finally {
            loader.style.display = 'none';
        }

        // Автоматически открываем чат, если передан chatId в URL
        if (tab === 'chats') {
            const urlParams = new URLSearchParams(window.location.search);
            const chatId = urlParams.get('chatId');
            if (chatId) {
                setTimeout(() => {
                    const chatElement = document.querySelector(`.user-item[data-chat-id="${chatId}"]`);
                    if (chatElement) {
                        chatElement.click();
                    }
                }, 300);
            }
        }
    }

    // === Отображение сообщения ===
    function appendMessage(id, sender, content, isSent, sentAt) {
        if (!chatMessages) return;

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;
        messageDiv.dataset.messageId = id;

        let timeString = 'Недавно';
        if (sentAt) {
            const dateStr = sentAt.split('.')[0];
            const date = new Date(dateStr);
            timeString = isNaN(date.getTime())
                ? 'Недавно'
                : date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
        }

        messageDiv.innerHTML = `
            <strong>${sender}:</strong> ${content}
            <div class="message-time">
                ${timeString}
                ${isSent ? `<button class="delete-message" onclick="deleteMessage(${id})">×</button>` : ''}
            </div>
        `;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // === Открытие чата ===
    function openChat(chatId, username) {
        currentChatId = chatId;
        chatHeader.textContent = `Чат с ${username}`;
        chatMessages.innerHTML = '';
        chatModal.style.display = 'flex';

        console.log('Текущий пользователь (currentUserId):', currentUserId);

        fetch(`/sparkle/chats/${chatId}/history`, { method: 'GET', credentials: 'include' })
            .then(response => {
                if (response.status === 204) return [];
                if (!response.ok) throw new Error('Не удалось загрузить историю');
                return response.json();
            })
            .then(messages => {
                if (messages.length === 0) {
                    const placeholder = document.createElement('div');
                    placeholder.textContent = 'Переписка ещё не началась';
                    placeholder.style.color = '#666';
                    placeholder.style.textAlign = 'center';
                    placeholder.style.marginTop = '1rem';
                    chatMessages.appendChild(placeholder);
                    return;
                }

                messages.forEach(msg => {
                    const isSent = msg.sender.userId === currentUserId;
                    appendMessage(msg.id, msg.sender.username, msg.content, isSent, msg.sentAt);
                });
            })
            .catch(err => {
                console.error('Ошибка загрузки истории:', err);
                chatMessages.innerHTML = '<div class="error">Не удалось загрузить сообщения</div>';
            });
    }

    // === Создание элемента пользователя с кнопками действий ===
    function createUserItemWithActions(user, tabType, itemId) {
        const li = document.createElement('li');
        li.className = 'user-item';
        li.style.cursor = 'pointer';
        li.style.position = 'relative';

        const photoUrl = user.photos?.[0]?.url || 'https://placehold.co/60x60/CCCCCC/FFFFFF?text=Нет+фото';

        if (tabType === 'chats') {
            li.setAttribute('data-chat-id', itemId);
        }

        li.innerHTML = `
            <img class="user-photo" src="${photoUrl}" alt="Фото ${user.username}" onerror="this.src='https://placehold.co/60x60/CCCCCC/FFFFFF?text=Ошибка'">
            <div class="user-info">
                <h3>${user.username || 'Аноним'}</h3>
                <p>${user.city?.cityName || 'Город не указан'}</p>
            </div>
            <div class="user-actions">
                ${tabType === 'matches' ? `<button class="user-action-btn delete-match" title="Удалить мэтч" onclick="deleteMatch(event, ${itemId})">×</button>` : ''}
                ${['my-likes', 'who-liked-me'].includes(tabType) ? `<button class="user-action-btn delete-like" title="Удалить лайк" onclick="deleteLike(event, ${itemId})">×</button>` : ''}
                ${tabType === 'chats' ? `<button class="user-action-btn delete-chat" title="Удалить чат" onclick="deleteChat(event, ${itemId})">×</button>` : ''}
            </div>
        `;

        li.addEventListener('click', e => {
            if (!e.target.closest('.user-action-btn')) {
                e.stopPropagation();
                if (tabType === 'chats') {
                    openChat(itemId, user.username);
                } else {
                    window.location.href = `/profile-user/${itemId}`;
                }
            }
        });

        return li;
    }

// === Отправка сообщения ===
// === Отправка сообщения ===
    chatSendBtn?.addEventListener('click', () => {
        const content = chatInput.value.trim();
        if (!content || !currentChatId) return;

        // ✅ Сохраняем старую структуру: { content, chat: { id } }
        const message = {
            content: content,
            chat: { id: currentChatId }
        };

        // Используем Blob, чтобы избежать добавления charset в Content-Type
        const blob = new Blob([JSON.stringify(message)], {
            type: 'application/json'
        });

        fetch('/sparkle/chats/message', {
            method: 'POST',
            body: blob,
            headers: {
                'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
            },
            credentials: 'include'
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        console.error('❌ Ошибка сервера:', text);
                        throw new Error('Не удалось отправить сообщение');
                    });
                }
                return response.json();
            })
            .then(data => {
                appendMessage(data.id, data.sender.username, data.content, true, data.sentAt);
                chatInput.value = '';
            })
            .catch(err => {
                console.error('❌ Ошибка отправки:', err);
                alert('Не удалось отправить сообщение');
            });
    });

    // === Закрытие модального окна ===
    chatCloseBtn?.addEventListener('click', () => {
        chatModal.style.display = 'none';
        currentChatId = null;
    });

    chatModal?.addEventListener('click', e => {
        if (e.target === chatModal) {
            chatModal.style.display = 'none';
            currentChatId = null;
        }
    });

    // === Удаление сообщения ===
    window.deleteMessage = async function (messageId) {
        if (!confirm('Удалить сообщение?')) return;

        try {
            const response = await fetch(`/sparkle/chats/message/${messageId}`, {
                method: 'DELETE',
                headers: {
                    'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
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

    // === Удаление мэтча ===
    window.deleteMatch = async function (e, userId) {
        e.stopPropagation();
        if (!confirm('Удалить мэтч?')) return;

        try {
            const response = await fetch(`/sparkle/users/match/delete/${userId}`, {
                method: 'DELETE',
                headers: {
                    'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
                },
                credentials: 'include'
            });

            if (response.ok) {
                loadData('matches');
            } else {
                alert('Не удалось удалить мэтч');
            }
        } catch (err) {
            console.error('Ошибка при удалении мэтча:', err);
            alert('Ошибка при удалении');
        }
    };

    // === Удаление лайка ===
    window.deleteLike = async function (e, userId) {
        e.stopPropagation();
        if (!confirm('Удалить лайк?')) return;

        try {
            const response = await fetch(`/sparkle/users/match/like/delete/${userId}`, {
                method: 'DELETE',
                headers: {
                    'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
                },
                credentials: 'include'
            });

            if (response.ok) {
                loadData('my-likes');
                loadData('who-liked-me');
            } else {
                alert('Не удалось удалить лайк');
            }
        } catch (err) {
            console.error('Ошибка при удалении лайка:', err);
            alert('Ошибка при удалении');
        }
    };

    // === Удаление чата ===
    window.deleteChat = async function (e, chatId) {
        e.stopPropagation();
        if (!confirm('Удалить чат?')) return;

        try {
            const response = await fetch(`/sparkle/chats/delete?chatId=${chatId}`, {
                method: 'DELETE',
                headers: {
                    'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
                },
                credentials: 'include'
            });

            if (response.ok) {
                loadData('chats');
                if (currentChatId === chatId) {
                    chatModal.style.display = 'none';
                    currentChatId = null;
                }
            } else {
                alert('Не удалось удалить чат');
            }
        } catch (err) {
            console.error('Ошибка при удалении чата:', err);
            alert('Ошибка при удалении');
        }
    };

    // === Автоматическое открытие чата при загрузке ===

    const urlParams = new URLSearchParams(window.location.search);
    const chatIdParam = urlParams.get('chatId');

    if (chatIdParam) {
        // Удаляем параметр chatId из URL, чтобы не срабатывало при переключении вкладок
        const newUrl = new URL(window.location);
        newUrl.searchParams.delete('chatId');
        window.history.replaceState({}, '', newUrl);

        // Переключаемся на вкладку "Чаты" и открываем чат
        setTimeout(() => {
            const chatsTab = document.querySelector('[data-tab="chats"]');
            if (chatsTab) {
                chatsTab.click(); // Активирует вкладку "Чаты"
            }
        }, 100);
    }
});