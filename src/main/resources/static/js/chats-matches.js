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
            chats: '/sparkle/users/chats/users',
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
    }
});