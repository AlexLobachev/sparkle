/**
 * Скрипт для страницы профиля пользователя
 * Загружает и отображает данные пользователя
 */
document.addEventListener('DOMContentLoaded', async () => {
    console.log('✅ DOM загружен, инициализация профиля');





    // Кнопка "Назад"
    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            window.history.back();
        });
    }

    // Элементы интерфейса
    const usernameEl = document.getElementById('username');
    const ageEl = document.getElementById('age');
    const genderEl = document.getElementById('gender');
    const cityEl = document.getElementById('city');
    const aboutMeEl = document.getElementById('aboutMe').querySelector('p');
    const interestsList = document.getElementById('interests').querySelector('ul');
    const photoSlider = document.getElementById('currentPhoto');
    const photoIndicator = document.getElementById('photoIndicator');
    const prevBtn = document.getElementById('prevPhoto');
    const nextBtn = document.getElementById('nextPhoto');
    const sendMessageBtn = document.getElementById('sendMessageBtn');

    const chatModal = document.getElementById('chatModal');
    const chatCloseBtn = document.getElementById('chatCloseBtn');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const chatInput = document.getElementById('chatInput');

    // Закрытие по кнопке
    chatCloseBtn?.addEventListener('click', closeChatModal);
    chatModal?.addEventListener('click', (e) => {
        if (e.target === chatModal) closeChatModal();
    });

    // Отправка сообщения
    chatSendBtn?.addEventListener('click', sendMessage);
    chatInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    let photos = [];
    let currentPhotoIndex = 0;
    let interestLabels = {}; // ← Глобально для модуля

    // Используем переменную userProfileId из Thymeleaf
    const userId = userProfileId;

    if (!userId || isNaN(Number(userId))) {
        showError('Неверный ID пользователя');
        return;
    }

    // Загружаем метки интересов и профиль
    await loadInterestLabels();
    await loadUserProfile(userId);

    // --- Функции ---

    async function loadInterestLabels() {
        try {
            const response = await fetch('/sparkle/users/interests/all', {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });

            if (response.ok) {
                interestLabels = await response.json();
                console.log('✅ Метки интересов загружены:', interestLabels);
            } else {
                console.warn('⚠️ Не удалось загрузить метки интересов:', response.status);
                fallbackToHardcodedLabels();
            }
        } catch (error) {
            console.error('❌ Ошибка при загрузке меток интересов:', error);
            fallbackToHardcodedLabels();
        }
    }

    function fallbackToHardcodedLabels() {
        interestLabels = {
            'FOOTBALL': 'Футбол',
            'LITRBALL': 'Пьянство',
            'BASKETBALL': 'Баскетбол',
            'TENNIS': 'Теннис',
            'SWIMMING': 'Плавание',
            'GYM': 'Фитнес и спортзал',
            'PAINTING': 'Рисование',
            'MUSIC': 'Музыка',
            'DANCE': 'Танцы',
            'WRITING': 'Писательство',
            'COOKING': 'Кулинария',
            'PHOTOGRAPHY': 'Фотография',
            'READING': 'Чтение',
            'TRAVEL': 'Путешествия',
            'PROGRAMMING': 'Программирование',
            'LANGUAGES': 'Изучение языков',
            'SCIENCE': 'Наука и технологии',
            'BUSINESS': 'Бизнес и предпринимательство',
            'MOVIES': 'Кино',
            'GAMING': 'Видеоигры',
            'SOCIAL_MEDIA': 'Социальные сети',
            'OTHER': 'Другое'
        };
        console.log('🔁 Используем резервные метки');
    }

    async function loadUserProfile(id) {
        try {
            const response = await fetch(`/sparkle/users/${id}`, { // ✅ Правильный URL
                method: 'GET',
                credentials: 'include',
            });

            if (response.status === 404) {
                showError('Пользователь не найден');
                return;
            }

            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status}`);
            }

            const user = await response.json();
            console.log('📥 Получен профиль:', user);
            renderProfile(user);
        } catch (error) {
            console.error('❌ Ошибка загрузки профиля:', error);
            showError('Не удалось загрузить профиль');
        }
    }

    function renderProfile(user) {
        usernameEl.textContent = user.username || 'Аноним';
        ageEl.textContent = calculateAge(user.birthDate) || '—';
        genderEl.textContent = formatGender(user.gender) || '—';
        cityEl.textContent = user.city?.cityName || '—';

        // О себе
        if (user.aboutMe && user.aboutMe.trim() !== '') {
            aboutMeEl.textContent = user.aboutMe;
        } else {
            aboutMeEl.textContent = 'Пользователь пока ничего о себе не рассказал.';
            aboutMeEl.style.color = 'var(--gray-400)';
            aboutMeEl.style.fontStyle = 'italic';
        }

        // Интересы
        interestsList.innerHTML = '';
        if (Array.isArray(user.interests) && user.interests.length > 0) {
            user.interests.forEach(interest => {
                const li = document.createElement('li');
                // Используем метку с бэкенда, если есть
                li.textContent = interestLabels[interest] || interest;
                interestsList.appendChild(li);
            });
        } else {
            const li = document.createElement('li');
            li.textContent = 'Интересы не указаны';
            li.style.color = 'var(--gray-400)';
            interestsList.appendChild(li);
        }

        // Фото
        photos = Array.isArray(user.photos) ? user.photos : [];
        if (photos.length === 0) {
            photoSlider.style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+фото)';
            photoIndicator.innerHTML = '';
            prevBtn.style.display = 'none';
            nextBtn.style.display = 'none';
        } else {
            updatePhoto();
            updatePhotoIndicator();
            prevBtn.style.display = 'block';
            nextBtn.style.display = 'block';
        }

        // Кнопка "Написать"
        sendMessageBtn.onclick = () => {
            console.log(`📩 Пытаемся начать чат с ${user.username}`);
            openChatWithUser(user);
        };
    }

    function updatePhoto() {
        if (photos.length === 0) return;
        const photoUrl = photos[currentPhotoIndex]?.url || 'https://placehold.co/400x400/CCCCCC/FFFFFF?text=Ошибка';
        photoSlider.style.backgroundImage = `url('${photoUrl}')`;
    }

    function updatePhotoIndicator() {
        photoIndicator.innerHTML = '';
        photos.forEach((_, i) => {
            const dot = document.createElement('div');
            dot.className = i === currentPhotoIndex ? 'photo-dot active' : 'photo-dot';
            photoIndicator.appendChild(dot);
        });
    }

    // Управление фото
    prevBtn?.addEventListener('click', () => {
        if (photos.length <= 1) return;
        currentPhotoIndex = (currentPhotoIndex - 1 + photos.length) % photos.length;
        updatePhoto();
        updatePhotoIndicator();
    });

    nextBtn?.addEventListener('click', () => {
        if (photos.length <= 1) return;
        currentPhotoIndex = (currentPhotoIndex + 1) % photos.length;
        updatePhoto();
        updatePhotoIndicator();
    });



    /**
     * Открывает чат с пользователем прямо на странице профиля
     */
    async function openChatWithUser(user) {
        if (!user?.userId) {
            console.warn('❌ Не указан userId пользователя');
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        console.log('🔐 CSRF Token:', csrfToken);
        console.log('📤 Пытаемся создать чат с:', user.userId);

        try {
            // Создаём чат
            const response = await fetch(`/sparkle/chats/${user.userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 403) {
                    alert('Ошибка доступа. Попробуйте перезагрузить страницу.');
                } else if (response.status === 404) {
                    alert('Пользователь не существует.');
                } else {
                    const text = await response.text();
                    console.error('❌ Ошибка:', response.status, text);
                    alert('Не удалось начать чат.');
                }
                return;
            }

            const chatData = await response.json();
            console.log('✅ Чат успешно создан:', chatData);

            // Открываем модальное окно чата
            openChatModal(chatData.chatId, user.username);
        } catch (error) {
            console.error('❌ Ошибка сети:', error);
            alert('Не удалось подключиться к серверу');
        }
    }
    /**
     * Открывает модальное окно чата и загружает переписку
     */
    async function openChatModal(chatId, username) {
        currentChatId = chatId;
        const chatHeader = document.getElementById('chatHeader');
        const chatMessages = document.getElementById('chatMessages');

        if (chatHeader) chatHeader.textContent = `Чат с ${username}`;
        if (chatMessages) chatMessages.innerHTML = '';

        document.getElementById('chatModal').style.display = 'flex';

        try {
            const response = await fetch(`/sparkle/chats/${chatId}/history`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response.status === 204) {
                const placeholder = document.createElement('div');
                placeholder.textContent = 'Переписка ещё не началась';
                placeholder.style.color = '#666';
                placeholder.style.textAlign = 'center';
                placeholder.style.marginTop = '1rem';
                chatMessages.appendChild(placeholder);
                return;
            }

            if (!response.ok) throw new Error('Не удалось загрузить историю');

            const messages = await response.json();
            messages.forEach(msg => {
                // ✅ Определяем отправителя
                const isSent = msg.sender.userId === window.currentUserId;
                appendMessage(msg.id, msg.sender.username, msg.content, isSent, msg.sentAt);
            });
        } catch (err) {
            console.error('❌ Ошибка загрузки истории:', err);
            chatMessages.innerHTML = '<div class="error">Не удалось загрузить сообщения</div>';
        }
    }
    /**
     * Добавляет сообщение в окно чата
     */
    function appendMessage(id, sender, content, isSent, sentAt) {
        const chatMessages = document.getElementById('chatMessages');
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
            ${isSent ? `<button class="delete-message" onclick="window.deleteMessage(${id})">×</button>` : ''}
        </div>
    `;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    /**
     * Закрывает модальное окно чата
     */
    function closeChatModal() {
        document.getElementById('chatModal').style.display = 'none';
        currentChatId = null;
    }

    /**
     * Отправляет сообщение
     */
    async function sendMessage() {
        const content = chatInput.value.trim();
        if (!content || !currentChatId) return;

        const message = {
            content: content,
            chat: { id: currentChatId }
        };

        const blob = new Blob([JSON.stringify(message)], {
            type: 'application/json'
        });

        try {
            const response = await fetch('/sparkle/chats/message', {
                method: 'POST',
                body: blob,
                headers: {
                    'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
                },
                credentials: 'include'
            });

            if (!response.ok) {
                const text = await response.text();
                console.error('❌ Ошибка сервера:', text);
                throw new Error('Не удалось отправить сообщение');
            }

            const data = await response.json();
            appendMessage(data.id, data.sender.username, data.content, true, data.sentAt);
            chatInput.value = '';
        } catch (err) {
            console.error('❌ Ошибка отправки:', err);
            alert('Не удалось отправить сообщение');
        }
    }




    /**
     * Удаляет сообщение
     */
    window.deleteMessage = async function (messageId) {
        if (!confirm('Удалить сообщение?')) return;

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
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
            console.error('❌ Ошибка при удалении:', err);
            alert('Ошибка при удалении');
        }
    };
    // Вспомогательные функции
    function calculateAge(birthDate) {
        if (!birthDate) return null;
        const dob = new Date(birthDate);
        const ageDiff = Date.now() - dob.getTime();
        const ageDate = new Date(ageDiff);
        return Math.abs(ageDate.getUTCFullYear() - 1970);
    }

    function formatGender(gender) {
        if (!gender) return null;
        return {
            'MALE': 'Мужской',
            'FEMALE': 'Женский',
            'WOMEN': 'Женский'
        }[gender] || gender;
    }

    function showError(message) {
        const mainContent = document.querySelector('.main-content');
        if (!mainContent) return;
        mainContent.innerHTML = `
            <div class="error" style="text-align:center; padding:2rem; color:var(--danger);">
                <p>${message}</p>
                <button onclick="window.history.back()" class="action-btn">Назад</button>
            </div>
        `;
    }
});