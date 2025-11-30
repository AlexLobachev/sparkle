/**
 * Скрипт для страницы профиля пользователя
 * Загружает и отображает данные пользователя
 */
document.addEventListener('DOMContentLoaded', async () => {
    console.log('✅ DOM загружен, инициализация профиля');

    // Кнопки действий
    const bottomBackBtn = document.getElementById('bottomBackBtn');
    const sendMessageBtn = document.getElementById('sendMessageBtn');

    const chatModal = document.getElementById('chatModal');
    const chatCloseBtn = document.getElementById('chatCloseBtn');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const chatInput = document.getElementById('chatInput');

    // === Кнопка "Назад" ===
    bottomBackBtn?.addEventListener('click', () => {
        window.history.back();
    });

    // === Модальное окно чата ===
    chatCloseBtn?.addEventListener('click', closeChatModal);
    chatModal?.addEventListener('click', (e) => {
        if (e.target === chatModal) closeChatModal();
    });

    chatSendBtn?.addEventListener('click', sendMessage);
    chatInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    let photos = [];
    let currentPhotoIndex = 0;
    let interestLabels = {};
    let currentChatId = null;

    const userId = userProfileId;
    if (!userId || isNaN(Number(userId))) {
        showError('Неверный ID пользователя');
        return;
    }

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
            } else {
                fallbackToHardcodedLabels();
            }
        } catch (error) {
            console.error('❌ Ошибка при загрузке меток интересов:', error);
            fallbackToHardcodedLabels();
        }
    }

    function fallbackToHardcodedLabels() {
        interestLabels = {
            'FOOTBALL': 'Футбол', 'LITRBALL': 'Пьянство', 'BASKETBALL': 'Баскетбол',
            'TENNIS': 'Теннис', 'SWIMMING': 'Плавание', 'GYM': 'Фитнес и спортзал',
            'PAINTING': 'Рисование', 'MUSIC': 'Музыка', 'DANCE': 'Танцы',
            'WRITING': 'Писательство', 'COOKING': 'Кулинария', 'PHOTOGRAPHY': 'Фотография',
            'READING': 'Чтение', 'TRAVEL': 'Путешествия', 'PROGRAMMING': 'Программирование',
            'LANGUAGES': 'Изучение языков', 'SCIENCE': 'Наука и технологии',
            'BUSINESS': 'Бизнес и предпринимательство', 'MOVIES': 'Кино',
            'GAMING': 'Видеоигры', 'SOCIAL_MEDIA': 'Социальные сети', 'OTHER': 'Другое'
        };
    }

    async function loadUserProfile(id) {
        try {
            const response = await fetch(`/sparkle/users/${id}`, {
                method: 'GET',
                credentials: 'include',
            });

            if (response.status === 404) {
                showError('Пользователь не найден');
                return;
            }

            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

            const user = await response.json();
            renderProfile(user);
        } catch (error) {
            console.error('❌ Ошибка загрузки профиля:', error);
            showError('Не удалось загрузить профиль');
        }
    }

    function renderProfile(user) {
        document.getElementById('username').textContent = user.username || 'Аноним';
        document.getElementById('age').textContent = calculateAge(user.birthDate) || '—';
        document.getElementById('gender').textContent = formatGender(user.gender) || '—';
        document.getElementById('city').textContent = user.city?.cityName || '—';

        const aboutMeEl = document.getElementById('aboutMe').querySelector('p');
        if (user.aboutMe?.trim()) {
            aboutMeEl.textContent = user.aboutMe;
            aboutMeEl.style.color = 'var(--text)';
            aboutMeEl.style.fontStyle = 'normal';
        }

        const interestsList = document.getElementById('interests').querySelector('ul');
        interestsList.innerHTML = '';
        if (Array.isArray(user.interests) && user.interests.length > 0) {
            user.interests.forEach(interest => {
                const li = document.createElement('li');
                li.textContent = interestLabels[interest] || interest;
                interestsList.appendChild(li);
            });
        } else {
            const li = document.createElement('li');
            li.textContent = 'Интересы не указаны';
            li.style.color = 'var(--gray-400)';
            interestsList.appendChild(li);
        }

        photos = Array.isArray(user.photos) ? user.photos : [];
        if (photos.length === 0) {
            document.getElementById('currentPhoto').style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+фото)';
        } else {
            updatePhoto();
            updatePhotoIndicator();
        }

        sendMessageBtn.onclick = () => openChatWithUser(user);
    }

    function updatePhoto() {
        if (photos.length === 0) return;
        const photoUrl = photos[currentPhotoIndex]?.url || 'https://placehold.co/400x400/CCCCCC/FFFFFF?text=Ошибка';
        document.getElementById('currentPhoto').style.backgroundImage = `url('${photoUrl}')`;
    }

    function updatePhotoIndicator() {
        const indicator = document.getElementById('photoIndicator');
        indicator.innerHTML = '';
        photos.forEach((_, i) => {
            const dot = document.createElement('div');
            dot.className = i === currentPhotoIndex ? 'photo-dot active' : 'photo-dot';
            indicator.appendChild(dot);
        });
    }

    document.getElementById('prevPhoto')?.addEventListener('click', () => {
        if (photos.length <= 1) return;
        currentPhotoIndex = (currentPhotoIndex - 1 + photos.length) % photos.length;
        updatePhoto();
        updatePhotoIndicator();
    });

    document.getElementById('nextPhoto')?.addEventListener('click', () => {
        if (photos.length <= 1) return;
        currentPhotoIndex = (currentPhotoIndex + 1) % photos.length;
        updatePhoto();
        updatePhotoIndicator();
    });

    async function openChatWithUser(user) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        try {
            const response = await fetch(`/sparkle/chats/${user.userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 403) alert('Ошибка доступа.');
                else if (response.status === 404) alert('Пользователь не существует.');
                else alert('Не удалось начать чат.');
                return;
            }

            const chatData = await response.json();
            openChatModal(chatData.chatId, user.username);
        } catch (error) {
            console.error('❌ Ошибка сети:', error);
            alert('Не удалось подключиться к серверу');
        }
    }

    function openChatModal(chatId, username) {
        currentChatId = chatId;
        document.getElementById('chatHeader').textContent = `Чат с ${username}`;
        document.getElementById('chatMessages').innerHTML = '';
        document.getElementById('chatModal').style.display = 'flex';
        loadChatHistory(chatId);
    }

    async function loadChatHistory(chatId) {
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
                document.getElementById('chatMessages').appendChild(placeholder);
                return;
            }

            if (!response.ok) throw new Error('Не удалось загрузить историю');

            const messages = await response.json();
            messages.forEach(msg => {
                const isSent = msg.sender.userId === window.currentUserId;
                appendMessage(msg.id, msg.sender.username, msg.content, isSent, msg.sentAt);
            });
        } catch (err) {
            console.error('❌ Ошибка загрузки истории:', err);
            document.getElementById('chatMessages').innerHTML = '<div class="error">Не удалось загрузить сообщения</div>';
        }
    }

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

    function closeChatModal() {
        document.getElementById('chatModal').style.display = 'none';
        currentChatId = null;
    }

    async function sendMessage() {
        const content = chatInput.value.trim();
        if (!content || !currentChatId) return;

        const message = {
            content: content,
            chat: { id: currentChatId }
        };

        const blob = new Blob([JSON.stringify(message)], { type: 'application/json' });

        try {
            const response = await fetch('/sparkle/chats/message', {
                method: 'POST',
                body: blob,
                headers: { 'X-XSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') },
                credentials: 'include'
            });

            if (!response.ok) throw new Error('Не удалось отправить');
            const data = await response.json();
            appendMessage(data.id, data.sender.username, data.content, true, data.sentAt);
            chatInput.value = '';
        } catch (err) {
            console.error('❌ Ошибка отправки:', err);
            alert('Не удалось отправить сообщение');
        }
    }

    window.deleteMessage = async function (messageId) {
        if (!confirm('Удалить сообщение?')) return;
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        try {
            const response = await fetch(`/sparkle/chats/message/${messageId}`, {
                method: 'DELETE',
                headers: { 'X-XSRF-TOKEN': csrfToken },
                credentials: 'include'
            });

            if (response.ok) {
                document.querySelector(`.message[data-message-id="${messageId}"]`)?.remove();
            } else {
                alert('Не удалось удалить');
            }
        } catch (err) {
            console.error('❌ Ошибка при удалении:', err);
            alert('Ошибка при удалении');
        }
    };

    function calculateAge(birthDate) {
        if (!birthDate) return null;
        const dob = new Date(birthDate);
        const ageDiff = Date.now() - dob.getTime();
        const ageDate = new Date(ageDiff);
        return Math.abs(ageDate.getUTCFullYear() - 1970);
    }

    function formatGender(gender) {
        if (!gender) return null;
        return { 'MALE': 'Мужской', 'FEMALE': 'Женский', 'WOMEN': 'Женский' }[gender] || gender;
    }

    function showError(message) {
        document.querySelector('.main-content').innerHTML = `
            <div class="error" style="text-align:center; padding:2rem; color:var(--danger);">
                <p>${message}</p>
                <button onclick="window.history.back()" class="action-btn primary">Назад</button>
            </div>
        `;
    }
});