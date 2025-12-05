const INTERESTS_MAPPINGS = {
    "FOOTBALL": "Футбол", "BASKETBALL": "Баскетбол", "TENNIS": "Теннис",
    "SWIMMING": "Плавание", "GYM": "Фитнес", "MUSIC": "Музыка",
    "DANCE": "Танцы", "WRITING": "Писательство", "COOKING": "Кулинария",
    "PHOTOGRAPHY": "Фотография", "READING": "Чтение", "TRAVEL": "Путешествия",
    "PROGRAMMING": "Программирование", "LANGUAGES": "Языки", "SCIENCE": "Наука",
    "BUSINESS": "Бизнес", "MOVIES": "Фильмы", "GAMING": "Игры",
    "SOCIAL_MEDIA": "Соцсети", "OTHER": "Другое"
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
    let startX = 0, startY = 0, moveX = 0, moveY = 0;

    const profileViewer = document.getElementById('profileViewer');
    const profileCard = document.getElementById('profileCard');
    const currentPhoto = document.getElementById('currentPhoto');
    const profileInfo = document.getElementById('profileInfo');
    const likeBtn = document.getElementById('likeBtn');
    const dislikeBtn = document.getElementById('dislikeBtn');
    const likeOverlay = document.querySelector('.like-overlay');
    const dislikeOverlay = document.querySelector('.dislike-overlay');
    const reloadBtn = document.querySelector('.reload-btn');

    if (!profileViewer || !profileCard) {
        console.error('❌ Критические элементы не найдены');
        return;
    }

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

    function showError() {
        profileInfo.innerHTML = '<p style="color: #e74c3c;">Ошибка загрузки</p>';
        currentPhoto.style.backgroundImage = 'url(https://placehold.co/400x450/CCCCCC/FFFFFF?text=❌)';
        showNoCandidates();
    }

    function showNoCandidates() {
        profileCard.style.display = 'none';
        const noCandidates = document.getElementById('noCandidates');
        if (noCandidates) noCandidates.style.display = 'flex';
    }

    function hideNoCandidates() {
        const noCandidates = document.getElementById('noCandidates');
        if (noCandidates) noCandidates.style.display = 'none';
    }

    async function sendAction(type, userId) {
        if (!userId || isAnimating) return;
        try {
            const response = await fetch(`/sparkle/users/match/${type}/${userId}`, {
                method: 'POST',
                headers: { 'X-XSRF-TOKEN': csrfToken },
                credentials: 'include'
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            console.log(`✅ ${type} отправлен: ${userId}`);
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

            if (!response.ok) throw new Error(`HTTP ${response.status}`);

            const candidate = await response.json();
            if (candidate && candidate.userId) {
                users = [candidate];
                currentUserIndex = 0;
                currentPhotoIndex = 0;

                profileCard.style.transform = 'translate(0) rotate(0)';
                profileCard.style.transition = 'transform 0.4s ease';
                likeOverlay.style.opacity = '0';
                dislikeOverlay.style.opacity = '0';
                profileCard.style.display = 'block';

                renderProfile();
                hideNoCandidates();
            } else {
                showError();
            }
        } catch (error) {
            console.error('❌ Ошибка загрузки:', error);
            showError();
        } finally {
            hideLoader();
            isAnimating = false;
        }
    }

    function renderProfile() {
        if (users.length === 0) return;
        const user = users[currentUserIndex];

        const photoUrl = user.photos?.[currentPhotoIndex]?.url || 'https://placehold.co/400x450/CCCCCC/FFFFFF?text=📸';
        currentPhoto.style.backgroundImage = `url('${photoUrl}')`;

        const age = user.birthDate ? new Date().getFullYear() - new Date(user.birthDate).getFullYear() : null;
        const cityName = user.city?.cityName || null;
        const genderText = user.gender === 'WOMEN' ? 'Женщина' : user.gender === 'MEN' ? 'Мужчина' : null;
        const aboutMe = user.aboutMe || null;
        const interests = Array.isArray(user.interests) ? user.interests.map(i => INTERESTS_MAPPINGS[i] || i) : [];

        let infoHTML = `<h3>${user.username || 'Аноним'}`;
        if (age) infoHTML += `, ${age}`;
        infoHTML += `</h3>`;

        if (genderText) {
            infoHTML += `<span class="gender-badge">${genderText}</span><br>`;
        }

        if (cityName) {
            infoHTML += `<p><strong>📍</strong> ${cityName}</p>`;
        }

        if (aboutMe) {
            infoHTML += `<p>💬 ${aboutMe}</p>`;
        }

        if (interests.length > 0) {
            infoHTML += `<div class="interests">`;
            interests.forEach(i => infoHTML += `<span class="interest-badge">${i}</span>`);
            infoHTML += `</div>`;
        }

        profileInfo.innerHTML = infoHTML;
    }

    // === Клик по фото — переключает фото ===
    currentPhoto?.addEventListener('click', () => {
        if (users[currentUserIndex]?.photos?.length > 1) {
            currentPhotoIndex = (currentPhotoIndex + 1) % users[currentUserIndex].photos.length;
            renderProfile();
        }
    });

    // === Свайп: touchstart ===
    currentPhoto?.addEventListener('touchstart', e => {
        if (isAnimating || users.length === 0) return;
        const touch = e.touches[0];
        startX = touch.clientX;
        startY = touch.clientY;
        profileCard.style.transition = 'none';
    }, { passive: true });

    // === Свайп: touchmove — плавный вверх и в стороны ===
    currentPhoto?.addEventListener('touchmove', e => {
        if (isAnimating || users.length === 0) return;
        const touch = e.touches[0];
        moveX = touch.clientX - startX;
        moveY = touch.clientY - startY;

        const isVertical = Math.abs(moveY) > Math.abs(moveX);
        if (Math.abs(moveX) > 10 || Math.abs(moveY) > 10) {
            e.preventDefault();
        }

        if (isVertical && moveY < 0) {
            // Плавное движение вверх
            const limitedMoveY = Math.max(moveY, -150);
            profileCard.style.transform = `translateY(${limitedMoveY}px) scale(${1 + limitedMoveY / 1000})`;
        } else if (!isVertical) {
            // Горизонтальное — как раньше
            profileCard.style.transform = `translateX(${moveX}px) rotate(${moveX * 0.1}deg)`;
        }
    }, { passive: false });

    // === touchend — с порогом и плавным возвратом ===
    currentPhoto?.addEventListener('touchend', () => {
        if (isAnimating || users.length === 0) return;

        const thresholdX = window.innerWidth * 0.3;
        const thresholdY = 120;
        const isVertical = Math.abs(moveY) > Math.abs(moveX);
        const user = users[currentUserIndex];

        // 🔼 Свайп вверх — только если достаточно
        if (isVertical && moveY < -thresholdY) {
            const userId = user.userId;
            console.log('👆 Свайп вверх — переход в профиль:', userId);

            if (!userId) {
                console.error('❌ userId не определён');
                return;
            }

            // ✅ ТОЧНО ТАК ЖЕ, КАК В chats-matches.js
            window.location.href = `/profile-user/${userId}`;
            return;
        }

        // ↔️ Лайк/дизлайк
        if (!isVertical && Math.abs(moveX) > thresholdX) {
            profileCard.style.transition = 'transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
            if (moveX > 0) {
                profileCard.style.transform = 'translateX(100vw) rotate(30deg)';
                likeOverlay.style.opacity = '0.4';
                setTimeout(() => likeOverlay.style.opacity = '0', 600);
                sendAction('like', user.userId);
            } else {
                profileCard.style.transform = 'translateX(-100vw) rotate(-30deg)';
                dislikeOverlay.style.opacity = '0.4';
                setTimeout(() => dislikeOverlay.style.opacity = '0', 600);
                sendAction('dislike', user.userId);
            }
            setTimeout(() => loadNextCandidate(), 400);
        } else {
            // Возврат
            profileCard.style.transition = 'transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
            profileCard.style.transform = 'translate(0) rotate(0)';
        }

        moveX = 0;
        moveY = 0;
    }, { passive: false });

    // === Кнопки ===
    likeBtn?.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (!user) return;
        profileCard.style.transform = 'translateX(100vw) rotate(30deg)';
        likeOverlay.style.opacity = '0.4';
        setTimeout(() => likeOverlay.style.opacity = '0', 600);
        sendAction('like', user.userId);
        setTimeout(() => loadNextCandidate(), 400);
    });

    dislikeBtn?.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (!user) return;
        profileCard.style.transform = 'translateX(-100vw) rotate(-30deg)';
        dislikeOverlay.style.opacity = '0.4';
        setTimeout(() => dislikeOverlay.style.opacity = '0', 600);
        sendAction('dislike', user.userId);
        setTimeout(() => loadNextCandidate(), 400);
    });

    // === Навигация ===
    document.getElementById('profileBtn')?.addEventListener('click', () => window.location.href = '/main/profile');
    document.getElementById('editProfileBtn')?.addEventListener('click', () => window.location.href = '/main/settings/profile');
    document.getElementById('chatsMatchesBtn')?.addEventListener('click', () => window.location.href = '/chats-matches');
    document.getElementById('logoutBtn')?.addEventListener('click', async () => {
        await fetch('/logout', { method: 'POST', headers: { 'X-XSRF-TOKEN': csrfToken }, credentials: 'include' });
        window.location.href = '/';
    });

    reloadBtn?.addEventListener('click', e => {
        e.preventDefault();
        loadNextCandidate(true);
    });

    // === Инициализация ===
    loadNextCandidate();
});