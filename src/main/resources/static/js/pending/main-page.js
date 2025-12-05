// === Маппинг интересов ===
const INTERESTS_MAPPINGS = {
    "FOOTBALL": "Футбол",
    "BASKETBALL": "Баскетбол",
    "TENNIS": "Теннис",
    "SWIMMING": "Плавание",
    "GYM": "Фитнес",
    "MUSIC": "Музыка",
    "DANCE": "Танцы",
    "WRITING": "Писательство",
    "COOKING": "Кулинария",
    "PHOTOGRAPHY": "Фотография",
    "READING": "Чтение",
    "TRAVEL": "Путешествия",
    "PROGRAMMING": "Программирование",
    "LANGUAGES": "Языки",
    "SCIENCE": "Наука",
    "BUSINESS": "Бизнес",
    "MOVIES": "Фильмы",
    "GAMING": "Игры",
    "SOCIAL_MEDIA": "Соцсети",
    "OTHER": "Другое"
};

document.addEventListener('DOMContentLoaded', () => {
    const csrfMeta = document.querySelector('meta[name="csrf-token"]');
    if (!csrfMeta) {
        console.error('❌ CSRF токен не найден');
        return;
    }
    const csrfToken = csrfMeta.getAttribute('content');

    let users = [];
    let currentUserIndex = 0;
    let currentPhotoIndex = 0;
    let isAnimating = false;
    let startX = 0;

    const likeBtn = document.getElementById('likeBtn');
    const dislikeBtn = document.getElementById('dislikeBtn');
    const profileViewer = document.getElementById('profileViewer');
    const profileCard = document.querySelector('.profile-card');

    // === Обработчики навигации ===
    document.getElementById('profileBtn')?.addEventListener('click', () => {
        window.location.href = '/main/profile';
    });

    document.getElementById('editProfileBtn')?.addEventListener('click', () => {
        window.location.href = '/main/settings/profile';
    });

    document.getElementById('chatsMatchesBtn')?.addEventListener('click', () => {
        window.location.href = '/chats-matches';
    });

    document.getElementById('logoutBtn')?.addEventListener('click', async () => {
        await fetch('/logout', {
            method: 'POST',
            headers: { 'X-XSRF-TOKEN': csrfToken },
            credentials: 'include'
        });
        window.location.href = '/';
    });

    if (!likeBtn || !dislikeBtn || !profileViewer) {
        console.error('❌ Критические элементы не найдены');
        return;
    }

    // === Утилиты ===
    function showLoader() {
        const loader = document.createElement('div');
        loader.className = 'loader';
        loader.textContent = 'Загружаем...';
        document.body.appendChild(loader);
    }

    function hideLoader() {
        const loader = document.querySelector('.loader');
        if (loader) loader.remove();
    }

    function showError(message = "Ошибка загрузки. Попробуйте позже.") {
        document.getElementById('profileInfo').innerHTML = `<p>${message}</p>`;
        document.getElementById('currentPhoto').style.backgroundImage =
            'url(https://placehold.co/400x450/CCCCCC/FFFFFF?text=📸+Нет+фото)';
    }

    function showNoCandidates() {
        const info = document.getElementById('profileInfo');
        info.innerHTML = '<p>Нет доступных кандидатов</p>';

        const photo = document.getElementById('currentPhoto');
        photo.style.backgroundImage = 'url(https://placehold.co/400x450/CCCCCC/FFFFFF?text=👤+Нет+фото)';

        const retryBtn = document.createElement('button');
        retryBtn.className = 'action-btn';
        retryBtn.textContent = 'Обновить';
        retryBtn.onclick = () => loadNextCandidate(true);
        retryBtn.style.cssText = 'display: block; margin: 1rem auto;';

        info.appendChild(retryBtn);
    }

    // === Запросы ===
    async function sendAction(type, userId) {
        if (!userId || isAnimating) return;

        try {
            const response = await fetch(`/sparkle/users/match/${type}/${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
            console.log(`✅ ${type === 'like' ? 'Лайк' : 'Дизлайк'} отправлен:`, userId);
        } catch (error) {
            console.error(`❌ Ошибка ${type}:`, error);
        }
    }

    async function loadNextCandidate(reload = false) {
        if (isAnimating) return;
        isAnimating = true;

        showLoader();

        try {
            if (reload) {
                await fetch('/sparkle/users/match/reload', {
                    method: 'GET',
                    headers: { 'X-XSRF-TOKEN': csrfToken },
                    credentials: 'include'
                });
            }

            const response = await fetch('/sparkle/users/match/next-candidate', {
                method: 'GET',
                headers: { 'X-XSRF-TOKEN': csrfToken },
                credentials: 'include'
            });

            if (response.status === 204 || response.status === 205) {
                showNoCandidates();
                return;
            }

            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

            const candidate = await response.json();
            console.log('🔁 Кандидат:', candidate);

            if (candidate && candidate.userId) {
                users = [candidate];
                currentUserIndex = 0;
                currentPhotoIndex = 0;
                renderProfile();
            } else {
                showError();
            }
        } catch (error) {
            console.error('❌ Загрузка кандидата:', error);
            showError();
        } finally {
            hideLoader();
            isAnimating = false;
        }
    }

    function renderProfile() {
        if (users.length === 0) return;

        const user = users[currentUserIndex];
        const photoEl = document.getElementById('currentPhoto');
        const infoEl = document.getElementById('profileInfo');

        // === Фото: если нет — показываем заглушку с текстом ===
        const photoUrl = user.photos?.[currentPhotoIndex]?.url ||
            'https://placehold.co/400x450/CCCCCC/FFFFFF?text=📸+Нет+фото';

        photoEl.style.backgroundImage = `url('${photoUrl}')`;

        // === Остальное — без изменений ===
        const age = user.birthDate ? new Date().getFullYear() - new Date(user.birthDate).getFullYear() : null;
        const cityName = user.city?.cityName || null;
        const genderText = user.gender === 'WOMEN' ? 'Женщина' : user.gender === 'MEN' ? 'Мужчина' : null;
        const aboutMe = user.aboutMe || null;

        // Интересы — отображаем только если есть
        const interests = Array.isArray(user.interests) ? user.interests.map(interest => {
            return INTERESTS_MAPPINGS[interest] || interest;
        }) : [];

        // Формируем HTML
        let infoHTML = `<h3>${user.username || 'Аноним'}`;
        if (age) infoHTML += `, ${age}`;
        if (genderText) infoHTML += ` <span class="gender-badge">${genderText}</span>`;
        infoHTML += `</h3>`;

        if (cityName) {
            infoHTML += `<p><strong>📍</strong> ${cityName}</p>`;
        }

        if (aboutMe) {
            infoHTML += `<p>💬 ${aboutMe}</p>`;
        }

        if (interests.length > 0) {
            infoHTML += `<div class="interests">`;
            interests.forEach(interest => {
                infoHTML += `<span class="interest-badge">${interest}</span>`;
            });
            infoHTML += `</div>`;
        }

        infoEl.innerHTML = infoHTML;
    }

    // === Обработчики действий ===
    function handleSwipe(diff) {
        if (Math.abs(diff) < 30) return;

        const action = diff > 0 ? 'like' : 'dislike';
        const user = users[currentUserIndex];
        if (user?.userId) {
            sendAction(action, user.userId);
            profileCard.classList.add(`swipe-${action === 'like' ? 'right' : 'left'}`);
            setTimeout(() => {
                profileCard.classList.remove('swipe-right', 'swipe-left');
                loadNextCandidate();
            }, 300);
        }
    }

    likeBtn.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (user?.userId) {
            sendAction('like', user.userId);
            profileCard.classList.add('swipe-right');
            setTimeout(() => {
                profileCard.classList.remove('swipe-right');
                loadNextCandidate();
            }, 300);
        }
    });

    dislikeBtn.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (user?.userId) {
            sendAction('dislike', user.userId);
            profileCard.classList.add('swipe-left');
            setTimeout(() => {
                profileCard.classList.remove('swipe-left');
                loadNextCandidate();
            }, 300);
        }
    });

    // Свайпы
    profileViewer.addEventListener('mousedown', e => startX = e.clientX);
    profileViewer.addEventListener('mouseup', e => handleSwipe(startX - e.clientX));
    profileViewer.addEventListener('touchstart', e => startX = e.touches[0].clientX);
    profileViewer.addEventListener('touchend', e => handleSwipe(startX - e.changedTouches[0].clientX));

    // Переключение фото
    document.getElementById('nextPhoto')?.addEventListener('click', () => {
        if (users[currentUserIndex]?.photos?.length > 1) {
            currentPhotoIndex = (currentPhotoIndex + 1) % users[currentUserIndex].photos.length;
            renderProfile();
        }
    });

    document.getElementById('prevPhoto')?.addEventListener('click', () => {
        if (users[currentUserIndex]?.photos?.length > 1) {
            currentPhotoIndex = (currentPhotoIndex - 1 + users[currentUserIndex].photos.length) % users[currentUserIndex].photos.length;
            renderProfile();
        }
    });

    // Инициализация
    loadNextCandidate();
});