/**
 * Упрощённая версия скрипта для страницы чатов и мэтчей
 * ✅ Удалена логика чата — теперь чат на отдельной странице
 */
document.addEventListener('DOMContentLoaded', async () => {
    console.log('✅ DOM загружен, инициализация скрипта');

    // Устанавливаем текущий год
    const yearEl = document.getElementById('year');
    if (yearEl) yearEl.textContent = new Date().getFullYear();

    // Кнопки "Назад"
    const backBtn = document.getElementById('backBtn');
    const bottomBackBtn = document.getElementById('bottomBackBtn');

    [backBtn, bottomBackBtn].forEach(btn => {
        if (btn) {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                window.location.href = '/main';
            });
        }
    });

    // Вкладки
    const tabs = document.querySelectorAll('.tab');
    const panes = document.querySelectorAll('.tab-pane');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const target = tab.dataset.tab;

            tabs.forEach(t => {
                t.classList.remove('active');
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');

            panes.forEach(pane => pane.classList.remove('active'));
            const targetPane = document.getElementById(target);
            if (targetPane) {
                targetPane.classList.add('active');
            }

            loadData(target);
        });
    });

    // Инициализация
    loadData('matches');

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

        if (!loader || !list || !noData) return;

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

            if (tab === 'chats') {
                data.forEach(chat => {
                    const interlocutor = chat.user;
                    if (!interlocutor) return;

                    const li = createUserItemWithActions(interlocutor, tab, chat.chatId);
                    list.appendChild(li);
                });
            } else {
                data.forEach(item => {
                    const user = item.user || item;
                    const userId = user.userId || user.id;
                    if (!userId) return;

                    const id = tab === 'chats' ? item.chatId : userId;

                    const li = createUserItemWithActions(user, tab, id);
                    list.appendChild(li);
                });
            }

        } catch (error) {
            if (error.name !== 'AbortError') console.error('❌ Ошибка загрузки:', tab, error);
            noData.style.display = 'block';
            noData.textContent = 'Ошибка загрузки данных';
        } finally {
            loader.style.display = 'none';
        }

        // Автооткрытие чата по chatId из URL
        if (tab === 'chats') {
            const urlParams = new URLSearchParams(window.location.search);
            const chatId = urlParams.get('chatId');
            if (chatId) {
                const newUrl = new URL(window.location);
                newUrl.searchParams.delete('chatId');
                window.history.replaceState({}, '', newUrl);

                setTimeout(() => {
                    const targetChatElement = document.querySelector(`.user-item[data-chat-id="${chatId}"]`);
                    if (targetChatElement) {
                        targetChatElement.click();
                    } else {
                        console.warn(`❌ Элемент чата с chatId=${chatId} не найден в DOM`);
                    }
                }, 500);
            }
        }
    }

    function createUserItemWithActions(user, tabType, itemId) {
        const li = document.createElement('li');
        li.className = 'user-item';
        li.style.cursor = 'pointer';

        const photoUrl = user.photos?.[0]?.url || 'https://placehold.co/60x60/CCCCCC/FFFFFF?text=Нет+фото';

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
                    // Открываем чат на отдельной странице
                    window.location.href = `/chat/${itemId}`;
                } else {
                    window.location.href = `/profile-user/${itemId}`;
                }
            }
        });

        return li;
    }

    // Удаление мэтча
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

    // Удаление лайка
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

    // Удаление чата
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
            } else {
                alert('Не удалось удалить чат');
            }
        } catch (err) {
            console.error('Ошибка при удалении чата:', err);
            alert('Ошибка при удалении');
        }
    };

    // Автооткрытие чата по chatId из URL
    const urlParams = new URLSearchParams(window.location.search);
    const chatIdParam = urlParams.get('chatId');
    if (chatIdParam) {
        setTimeout(() => {
            const chatsTab = document.querySelector('[data-tab="chats"]');
            if (chatsTab) {
                chatsTab.click();
            }
        }, 100);
    }
});