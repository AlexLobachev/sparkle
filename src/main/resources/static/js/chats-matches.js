/**
 * Скрипт для страницы "Чаты и Мэтчи"
 * Управляет вкладками и загрузкой данных
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log('✅ DOM загружен, инициализация скрипта');

    // Устанавливаем год в футер
    const yearEl = document.getElementById('year');
    if (yearEl) {
        yearEl.textContent = new Date().getFullYear();
    }

    // Кнопка "Назад"
    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        console.log('✅ Кнопка "Назад" найдена');
        backBtn.addEventListener('click', (e) => {
            console.log('➡️ Клик по кнопке "Назад"');
            e.preventDefault();
            window.location.href = '/main';
        });
    } else {
        console.error('❌ Кнопка #backBtn не найдена');
    }

    // Вкладки
    const tabs = document.querySelectorAll('.tab');
    const panes = document.querySelectorAll('.tab-pane');
    // Добавьте после других переменных

    const chatModal = document.getElementById('chatModal');
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const chatCloseBtn = document.getElementById('chatCloseBtn');
    const chatHeader = document.getElementById('chatHeader');
    let currentChatId = null;

    tabs.forEach((tab) => {
        tab.addEventListener('click', () => {
            const target = tab.dataset.tab;

            // Активация вкладки
            tabs.forEach(t => {
                t.classList.remove('active');
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');

            // Показ контента
            panes.forEach(pane => {
                pane.classList.remove('active');
            });
            document.getElementById(target).classList.add('active');

            // Загрузка данных при переключении
            loadData(target);
        });
    });

    // Инициализация: загружаем мэтчи при старте
    loadData('matches');

    /**
     * Загружает данные по выбранной вкладке
     */
    async function loadData(tab) {
        console.log(`📥 Загрузка данных: ${tab}`);

        const endpoints = {
            matches: '/sparkle/users/match/current-matches',
            chats: '/sparkle/chats/users',
            'my-likes': '/sparkle/users/match/like/current-lake-your',
            'who-liked-me': '/sparkle/users/match/like/current-lake-who'
        };

        const loaderId = {
            matches: 'matchesLoader',
            chats: 'chatsLoader',
            'my-likes': 'likesLoader',
            'who-liked-me': 'whoLikedLoader'
        };

        const listId = {
            matches: 'matchesList',
            chats: 'chatsList',
            'my-likes': 'likesList',
            'who-liked-me': 'whoLikedList'
        };

        const noDataId = {
            matches: 'noMatches',
            chats: 'noChats',
            'my-likes': 'noLikes',
            'who-liked-me': 'noWhoLiked'
        };

        const loader = document.getElementById(loaderId[tab]);
        const list = document.getElementById(listId[tab]);
        const noData = document.getElementById(noDataId[tab]);

        // Показываем лоадер
        loader.style.display = 'block';
        list.innerHTML = '';
        noData.style.display = 'none';

        try {
            const response = await fetch(endpoints[tab], {
                method: 'GET',
                credentials: 'include'
            });

            if (response.status === 204 || response.status === 404) {
                console.log(`🟡 Нет данных для вкладки: ${tab}`);
                loader.style.display = 'none';
                noData.style.display = 'block';
                return;
            }

            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status}`);
            }

            const data = await response.json();

            if (!data || data.length === 0) {
                console.log(`🟡 Пустой массив для: ${tab}`);
                loader.style.display = 'none';
                noData.style.display = 'block';
                return;
            }

            // Рендерим пользователей
            data.forEach(item => {
                // Извлекаем user из объекта (например, { matchId: 69, user: { ... } })
                const user = item.user || item; // если есть поле user — используем его, иначе считаем, что объект и есть пользователь
                const userId = user.userId || user.id; // поддержка разных имён ID

                if (!userId) {
                    console.warn('Пропущен элемент: не найден userId', item);
                    return;
                }

                const li = document.createElement('li');
                li.className = 'user-item';
                li.style.cursor = 'pointer';
                li.setAttribute('title', `Посмотреть профиль ${user.username}`);

                const photoUrl = user.photos?.[0]?.url || 'https://placehold.co/60x60/CCCCCC/FFFFFF?text=Нет+фото';

                li.innerHTML = `
                    <img class="user-photo" src="${photoUrl}" alt="Фото ${user.username}" onerror="this.src='https://placehold.co/60x60/CCCCCC/FFFFFF?text=Ошибка'">
                    <div class="user-info">
                        <h3>${user.username || 'Аноним'}</h3>
                        <p>${user.city?.cityName || 'Город не указан'}</p>
                    </div>
                `;

                // Переход на профиль пользователя
                li.addEventListener('click', (e) => {
                    e.stopPropagation();
                    window.location.href = `/profile-user/${userId}`;
                });

                list.appendChild(li);
            });
        } catch (error) {
            if (error.name !== 'AbortError') {
                console.error('❌ Ошибка загрузки:', tab, error);
                // Показываем сообщение "Нет данных"
                loader.style.display = 'none';
                noData.style.display = 'block';
                noData.textContent = `У вас пока нет совпадений`;
            }
        } finally {
            loader.style.display = 'none';
        }



// Замените блок обработки чатов в loadData(tab) на этот исправленный код

        if (tab === 'chats') {
            try {
                const response = await fetch('/sparkle/chats/users', {
                    method: 'GET',
                    credentials: 'include'
                });

                if (!response.ok) throw new Error('Network response was not ok');

                const chatsData = await response.json();
                const chatsList = document.getElementById('chatsList');
                chatsList.innerHTML = '';

                if (!chatsData || chatsData.length === 0) {
                    document.getElementById('noChats').style.display = 'block';
                    return;
                }

                document.getElementById('noChats').style.display = 'none';

                // Получаем ID текущего пользователя (добавьте в начало скрипта: let currentUserId = 123;)
                const currentUserId = window.currentUserId; // Должен быть установлен на сервере

                chatsData.forEach(chat => {
                    // Исправлено: используем chat.users вместо chat.interlocutors
                    const interlocutor = chat.users.find(user => user.userId !== currentUserId);
                    if (!interlocutor) return;

                    const li = document.createElement('li');
                    li.className = 'user-item';
                    li.style.cursor = 'pointer';

                    const photoUrl = interlocutor.photos?.[0]?.url || 'https://placehold.co/60x60/CCCCCC/FFFFFF?text=Нет+фото';

                    li.innerHTML = `
                <img class="user-photo" src="${photoUrl}" alt="Фото ${interlocutor.username}" onerror="this.src='https://placehold.co/60x60/CCCCCC/FFFFFF?text=Ошибка'">
                <div class="user-info">
                    <h3>${interlocutor.username || 'Аноним'}</h3>
                    <p>${interlocutor.city?.cityName || 'Город не указан'}</p>
                </div>
            `;

                    li.addEventListener('click', () => {
                        openChat(chat.chatId, interlocutor.username);
                    });

                    chatsList.appendChild(li);
                });
            } catch (error) {
                console.error('Ошибка загрузки чатов:', error);
                document.getElementById('noChats').style.display = 'block';
                document.getElementById('noChats').textContent = 'Ошибка загрузки чатов';
            }
        }
    }
    // Добавьте в конец файла, после loadData
    function openChat(chatId, username) {
        currentChatId = chatId;
        chatHeader.textContent = `Чат с ${username}`;
        chatMessages.innerHTML = '';
        chatModal.style.display = 'flex';

        // Загружаем историю чата
        fetch(`/sparkle/chats/${chatId}/history`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include'
        })
            .then(response => {
                if (response.status === 204) {
                    // Нет истории — это нормально
                    console.log('Чат пуст, переписки ещё не было');
                    return []; // Возвращаем пустой массив
                }
                if (!response.ok) {
                    throw new Error('Не удалось загрузить историю');
                }
                return response.json(); // Только если статус 200
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
                    appendMessage(msg.sender.username, msg.content, msg.sender.userId === currentUserId);
                });
                chatMessages.scrollTop = chatMessages.scrollHeight;
            })
            .catch(err => {
                if (err.name !== 'SyntaxError') {
                    console.error('Ошибка загрузки истории:', err);
                }
                chatMessages.innerHTML = '<div class="error">Не удалось загрузить сообщения</div>';
            });
    }

    function appendMessage(sender, content, isSent) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;
        messageDiv.innerHTML = `<strong>${sender}:</strong> ${content}`;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

// Отправка сообщения
    chatSendBtn.addEventListener('click', () => {
        const content = chatInput.value.trim();
        if (!content || !currentChatId) return;

        const message = {
            content: content,
            chat: { id: currentChatId }
        };

        fetch('sparkle/chats/message', {
            method: 'POST',
            body: JSON.stringify(message),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
            },
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                appendMessage(data.sender.username, data.content, true);
                chatInput.value = '';
            })
            .catch(err => {
                console.error('Ошибка отправки:', err);
                alert('Не удалось отправить сообщение');
            });
    });

    // Закрытие модального окна
    chatCloseBtn.addEventListener('click', () => {
        chatModal.style.display = 'none';
        currentChatId = null;
    });

    // Закрытие по клику вне окна
    chatModal.addEventListener('click', (e) => {
        if (e.target === chatModal) {
            chatModal.style.display = 'none';
            currentChatId = null;
        }
    });

});