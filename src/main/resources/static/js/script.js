/**
 * Основной скрипт для работы с карточками кандидатов.
 * ИСПРАВЛЕНО: зависание isAnimating, фото и профиль не отображаются
 */
let users = [];
let currentUserIndex = 0;
let currentPhotoIndex = 0;
let isAnimating = false;

// Получение CSRF-токена
const csrfMeta = document.querySelector('meta[name="csrf-token"]');
const csrfToken = csrfMeta?.getAttribute('content');

// Флаг: обработчики добавлены
let eventListenersAdded = false;

console.log('CSRF Token:', csrfToken);

/**
 * Загружает следующего кандидата
 */
async function loadNextCandidate() {
    if (isAnimating) {
        console.warn('⚠️ loadNextCandidate: анимация уже идёт, выход');
        return;
    }

    isAnimating = true;
    console.log('🔄 loadNextCandidate: начало загрузки');

    try {
        showLoader();
        updateUIState(true);

        const response = await fetch('/sparkle/users/match/next-candidate', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            credentials: 'include'
        });

        if (response.status === 204) {
            showNoCandidates();
            return;
        }

        if (!response.ok) {
            throw new Error(`Ошибка: ${response.status}`);
        }

        const candidate = await response.json();
        console.log('🔁 Получен кандидат:', JSON.stringify(candidate, null, 2));

        users = [candidate];
        currentUserIndex = 0;
        currentPhotoIndex = 0;

        // Убедимся, что isAnimating всё ещё true
        if (!isAnimating) {
            console.warn('⚠️ Анимация была сброшена до завершения загрузки — возможно, конфликт');
            return;
        }

        // Принудительно разрешаем рендер, даже если isAnimating = true
        console.log('🌀 Принудительный вызов renderProfile');
        renderProfileForced();
    } catch (error) {
        console.error('❌ Ошибка загрузки:', error);
        showError();
    } finally {
        hideLoader();
        updateUIState(false);
        console.log('🔚 loadNextCandidate: завершён (isAnimating = false)');
        isAnimating = false; // Критически важно!
    }
}

/**
 * Отправляет лайк
 */
async function sendLike(userId) {
    if (!userId || isAnimating) return;

    try {
        const response = await fetch(`/sparkle/users/match/like/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const text = await response.text();
            console.warn('❌ Лайк не удался:', text);
            return;
        }

        console.log('✅ Лайк отправлен:', userId);
    } catch (error) {
        console.error('❌ Ошибка лайка:', error);
    }
}

/**
 * Рендерит профиль БЕЗ проверки isAnimating
 */
function renderProfileForced() {
    console.log('🔥 renderProfileForced: начало (игнорируем isAnimating)');

    const user = users[currentUserIndex];
    if (!user) {
        console.warn('⚠️ Нет пользователя для отображения');
        return;
    }

    try {
        // Фото
        const photoContainer = document.getElementById('currentPhoto');
        if (photoContainer) {
            const photoUrl = user.photos?.[0]?.url || '/images/placeholder.jpg';

            photoContainer.style.backgroundImage = `url('${photoUrl}')`;
            photoContainer.style.backgroundSize = 'cover';
            photoContainer.style.backgroundPosition = 'center';
            photoContainer.style.backgroundRepeat = 'no-repeat';
            photoContainer.style.backgroundColor = '#f0f0f0';
            photoContainer.style.display = 'block';

            // Принудительная перерисовка
            photoContainer.offsetHeight;
        }

        // Возраст
        const birthDate = user.birthDate ? new Date(user.birthDate) : null;
        const age = birthDate
            ? new Date().getFullYear() - birthDate.getFullYear()
            : 'Возраст не указан';

        // Город
        const cityName = user.city?.cityName || 'Город не указан';

        // aboutMe
        const aboutMeText = user.aboutMe || 'О себе не рассказано';

        // Информация о пользователе
        const infoContainer = document.getElementById('profileInfo');
        if (infoContainer) {
            infoContainer.innerHTML = `
                <h3>${user.username || 'Аноним'}, ${age}</h3>
                <p><strong>Город:</strong> ${cityName}</p>
                <p>${aboutMeText}</p>
            `;
        }

        updateIndicators(user);
    } catch (error) {
        console.error('❌ Ошибка в renderProfileForced:', error);
    }
}

/**
 * Обновляет индикаторы
 */
function updateIndicators(user) {
    updateProfileIndicator();
    updatePhotoIndicator(user?.photos?.length || 0);
}

function updateProfileIndicator() {
    const indicator = document.getElementById('indicator');
    if (!indicator) return;

    indicator.innerHTML = '';
    const dot = document.createElement('div');
    dot.className = 'dot active';
    indicator.appendChild(dot);
}

function updatePhotoIndicator(totalPhotos) {
    const indicator = document.getElementById('photoIndicator');
    if (!indicator) return;

    indicator.innerHTML = '';
    if (totalPhotos <= 1) return;

    for (let i = 0; i < totalPhotos; i++) {
        const dot = document.createElement('div');
        dot.className = i === currentPhotoIndex ? 'photo-dot active' : 'photo-dot';
        indicator.appendChild(dot);
    }
}

// === Обработчики событий ===

function initEventListeners() {
    if (eventListenersAdded) return;
    eventListenersAdded = true;

    console.log('✅ Обработчики событий инициализированы');

    // Лайк
    const likeBtn = document.getElementById('likeBtn');
    if (likeBtn) {
        likeBtn.onclick = (e) => {
            e.stopPropagation();
            const user = users[currentUserIndex];
            if (user?.id) {
                console.log('❤️ Лайк: отправка для', user.id);
                sendLike(user.id);
            }
            // Убираем проверку isAnimating
            setTimeout(loadNextCandidate, 0);
        };
    }

    // Дизлайк
    const dislikeBtn = document.getElementById('dislikeBtn');
    if (dislikeBtn) {
        dislikeBtn.onclick = (e) => {
            e.stopPropagation();
            console.log('👎 Дизлайк');
            // Убираем проверку isAnimating
            setTimeout(loadNextCandidate, 0);
        };
    }

    // Управление фото
    const nextBtn = document.getElementById('nextPhoto');
    const prevBtn = document.getElementById('prevPhoto');

    nextBtn?.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (!user?.photos?.length || user.photos.length <= 1) return;

        currentPhotoIndex = (currentPhotoIndex + 1) % user.photos.length;
        renderProfileForced();
    });

    prevBtn?.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (!user?.photos?.length || user.photos.length <= 1) return;

        currentPhotoIndex = (currentPhotoIndex - 1 + user.photos.length) % user.photos.length;
        renderProfileForced();
    });

    // Свайпы
    const profileViewer = document.getElementById('profileViewer');
    if (profileViewer) {
        let startX = 0;

        profileViewer.addEventListener('mousedown', (e) => {
            startX = e.clientX;
        });

        profileViewer.addEventListener('mouseup', (e) => {
            if (!startX) return;
            const diff = startX - e.clientX;
            if (Math.abs(diff) > 50) {
                const user = users[currentUserIndex];
                if (diff > 0 && user?.id) sendLike(user.id);
                setTimeout(loadNextCandidate, 0); // асинхронно
            }
            startX = 0;
        });

        profileViewer.addEventListener('touchstart', (e) => {
            startX = e.touches[0].clientX;
        });

        profileViewer.addEventListener('touchend', (e) => {
            if (!startX) return;
            const diff = startX - e.changedTouches[0].clientX;
            if (Math.abs(diff) > 50) {
                const user = users[currentUserIndex];
                if (diff > 0 && user?.id) sendLike(user.id);
                setTimeout(loadNextCandidate, 0); // асинхронно
            }
            startX = 0;
        });
    }

    // Навигация
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
        if (!csrfToken) {
            window.location.href = '/';
            return;
        }

        try {
            const response = await fetch('/logout', {
                method: 'POST',
                headers: { 'X-XSRF-TOKEN': csrfToken },
                credentials: 'include'
            });

            if (response.ok) {
                window.location.href = '/';
            }
        } catch (error) {
            window.location.href = '/';
        }
    });
}

// === Утилиты ===

function showLoader() {
    const loader = document.createElement('div');
    loader.className = 'loader';
    loader.innerHTML = '<span>Загрузка...</span>';
    loader.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: rgba(255, 255, 255, 0.9);
        color: #333;
        padding: 20px 30px;
        border-radius: 12px;
        font-size: 16px;
        z-index: 100;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        text-align: center;
    `;
    const container = document.getElementById('profileViewer');
    if (container && !document.querySelector('.loader')) {
        container.appendChild(loader);
    }
}

function hideLoader() {
    const loader = document.querySelector('.loader');
    if (loader) loader.remove();
}

function showNoCandidates() {
    const profileInfo = document.getElementById('profileInfo');
    const currentPhoto = document.getElementById('currentPhoto');
    const controls = document.getElementById('controls');
    const indicator = document.getElementById('indicator');
    const photoIndicator = document.getElementById('photoIndicator');

    if (currentPhoto) {
        currentPhoto.style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+кандидатов)';
        currentPhoto.style.backgroundColor = '#f5f5f5';
        currentPhoto.style.backgroundSize = 'cover';
    }

    if (profileInfo) {
        profileInfo.innerHTML = '<p>Нет доступных кандидатов</p>';
    }

    [controls, indicator, photoIndicator].forEach(el => {
        if (el) el.style.display = 'none';
    });

    const retryBtn = document.createElement('button');
    retryBtn.className = 'action-btn retry';
    retryBtn.textContent = 'Попробовать снова';
    retryBtn.style.margin = '1rem auto';
    retryBtn.style.display = 'block';

    retryBtn.addEventListener('click', () => {
        [controls, indicator, photoIndicator].forEach(el => {
            if (el) el.style.display = '';
        });
        retryBtn.remove();
        loadNextCandidate();
    });

    if (profileInfo) profileInfo.appendChild(retryBtn);
}

function showError() {
    const profileInfo = document.getElementById('profileInfo');
    const controls = document.getElementById('controls');
    const indicator = document.getElementById('indicator');
    const photoIndicator = document.getElementById('photoIndicator');

    if (profileInfo) {
        profileInfo.innerHTML = '<p>Произошла ошибка. Попробуйте позже.</p>';
    }

    [controls, indicator, photoIndicator].forEach(el => {
        if (el) el.style.display = 'none';
    });

    const retryBtn = document.createElement('button');
    retryBtn.className = 'action-btn retry';
    retryBtn.textContent = 'Повторить';
    retryBtn.style.margin = '1rem auto';
    retryBtn.style.display = 'block';

    retryBtn.addEventListener('click', () => {
        [controls, indicator, photoIndicator].forEach(el => {
            if (el) el.style.display = '';
        });
        retryBtn.remove();
        loadNextCandidate();
    });

    if (profileInfo) profileInfo.appendChild(retryBtn);
}

function updateUIState(loading = false) {
    const controls = document.getElementById('controls');
    const indicator = document.getElementById('indicator');
    const photoIndicator = document.getElementById('photoIndicator');

    if (controls) controls.style.opacity = loading ? '0.6' : '1';
    if (indicator) indicator.style.opacity = loading ? '0.6' : '1';
    if (photoIndicator) photoIndicator.style.opacity = loading ? '0.6' : '1';
}

// Инициализация
document.addEventListener('DOMContentLoaded', () => {
    if (!csrfToken) {
        console.warn('⚠️ CSRF-токен не найден');
    } else {
        console.log('✅ CSRF-токен найден:', csrfToken);
    }

    initEventListeners();
    setTimeout(loadNextCandidate, 0);
});