/**
 * Скрипт управления профилем пользователя
 * Полная версия с исправлением ошибок и единым стилем
 */

// Глобальные переменные
window.currentPhotoIndex = 0;
window.photos = [];
window.interests = [];

// Словарь переводов интересов
const INTERESTS_TRANSLATIONS = {
    "FOOTBALL": { ru: "Футбол", en: "Football" },
    "LITRBALL": { ru: "Пьянство", en: "drunkenness" },
    "BASKETBALL": { ru: "Баскетбол", en: "Basketball" },
    "TENNIS": { ru: "Теннис", en: "Tennis" },
    "SWIMMING": { ru: "Плавание", en: "Swimming" },
    "GYM": { ru: "Тренажёрный зал", en: "Gym" },
    "PAINTING": { ru: "Рисование", en: "Painting" },
    "MUSIC": { ru: "Музыка", en: "Music" },
    "DANCE": { ru: "Танцы", en: "Dance" },
    "WRITING": { ru: "Писательство", en: "Writing" },
    "COOKING": { ru: "Кулинария", en: "Cooking" },
    "PHOTOGRAPHY": { ru: "Фотография", en: "Photography" },
    "READING": { ru: "Чтение", en: "Reading" },
    "TRAVEL": { ru: "Путешествия", en: "Travel" },
    "PROGRAMMING": { ru: "Программирование", en: "Programming" },
    "LANGUAGES": { ru: "Языки", en: "Languages" },
    "SCIENCE": { ru: "Наука", en: "Science" },
    "BUSINESS": { ru: "Бизнес", en: "Business" },
    "MOVIES": { ru: "Фильмы", en: "Movies" },
    "GAMING": { ru: "Гейминг", en: "Gaming" },
    "SOCIAL_MEDIA": { ru: "Соцсети", en: "Social Media" },
    "OTHER": { ru: "Другое", en: "Other" }
};

/**
 * Резервный список интересов, если сервер не ответил
 */
function fallbackToHardcodedLabels() {
    window.interestLabels = Object.values(INTERESTS_TRANSLATIONS).map(t => t.ru);
}

/**
 * Загрузка меток интересов с сервера
 */
async function loadInterestLabels() {
    try {
        const response = await fetch('/sparkle/users/interests/all', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (response.ok) {
            window.interestLabels = await response.json();
            console.log('✅ Метки интересов загружены:', window.interestLabels);
        } else {
            console.warn('⚠️ Не удалось загрузить метки интересов:', response.status);
            fallbackToHardcodedLabels();
        }
    } catch (error) {
        console.error('❌ Ошибка при загрузке меток интересов:', error);
        fallbackToHardcodedLabels();
    }
}

/**
 * Инициализация при загрузке страницы
 */
document.addEventListener('DOMContentLoaded', async () => {
    if (!currentUserId) {
        showError('Пользователь не авторизован');
        return;
    }

    // Сохраняем CSRF-токен в глобальной переменной
    window.csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');

    window.currentPhotoIndex = 0;
    window.interests = [];

    // Загружаем метки интересов
    await loadInterestLabels();

    // Загружаем данные профиля
    await loadUserData();

    // Настраиваем обработчики событий
    setupEventListeners();
});

/**
 * Загрузка данных пользователя с сервера
 */
async function loadUserData() {
    try {
        const response = await fetch(`/sparkle/users/${currentUserId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'X-XSRF-TOKEN': window.csrfToken
            },
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Не удалось загрузить данные');
        const user = await response.json();
        renderProfile(user);
    } catch (error) {
        console.error('Ошибка при загрузке профиля:', error);
        showError('Не удалось загрузить профиль');
    }
}

/**
 * Отображение данных профиля
 */
function renderProfile(user) {
    window.photos = user.photos || [];
    window.interests = Array.from(new Set(user.interests || [])); // Уникальные интересы
    const aboutMe = document.getElementById('aboutMe');

    // Фото
    if (window.photos.length > 0) {
        showCurrentPhoto();
        renderThumbnails();
        updatePhotoIndicator();
    } else {
        resetPhotoDisplay();
    }

    // Интересы
    renderInterestList();
    populateInterestSelect();

    // О себе
    if (aboutMe) {
        aboutMe.value = user.aboutMe || '';
    }
}

/**
 * Сброс отображения фото
 */
function resetPhotoDisplay() {
    const currentPhoto = document.getElementById('currentPhoto');
    currentPhoto.style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+фото)';
    document.getElementById('deleteBtn').disabled = true;
    document.getElementById('thumbnails').innerHTML = '';
    document.getElementById('photoIndicator').innerHTML = '';
}

/**
 * Показ текущего фото
 */
function showCurrentPhoto() {
    if (window.photos.length === 0) return;
    const photo = window.photos[window.currentPhotoIndex];
    const currentPhoto = document.getElementById('currentPhoto');
    currentPhoto.style.backgroundImage = `url('${photo.url}')`;
    document.getElementById('deleteBtn').disabled = false;
}

/**
 * Рендер миниатюр
 */
function renderThumbnails() {
    const thumbnails = document.getElementById('thumbnails');
    thumbnails.innerHTML = '';
    window.photos.forEach((photo, index) => {
        const img = document.createElement('img');
        img.src = photo.url;
        img.alt = 'Миниатюра';
        img.className = 'thumbnail';
        img.dataset.index = index;
        img.addEventListener('click', () => {
            window.currentPhotoIndex = index;
            showCurrentPhoto();
        });
        thumbnails.appendChild(img);
    });
}

/**
 * Обновление индикатора слайдера
 */
function updatePhotoIndicator() {
    const indicator = document.getElementById('photoIndicator');
    if (!indicator) return;
    indicator.innerHTML = '';
    window.photos.forEach((_, i) => {
        const dot = document.createElement('div');
        dot.className = i === window.currentPhotoIndex ? 'photo-dot active' : 'photo-dot';
        indicator.appendChild(dot);
    });
}

/**
 * Рендер списка интересов с крестиками
 */
function renderInterestList() {
    const container = document.getElementById('interestList');

    // ✅ Очищаем контейнер — это важно
    container.innerHTML = '';

    if (!window.interests.length) {
        const span = document.createElement('span');
        span.textContent = 'Нет интересов';
        span.style.color = '#999';
        container.appendChild(span);
        return;
    }

    // Убираем дубликаты на всякий случай (хотя не должны быть)
    const uniqueInterests = [...new Set(window.interests)];

    uniqueInterests.forEach(interestKey => {
        const label = window.interestLabels?.[interestKey] ||
            INTERESTS_TRANSLATIONS[interestKey]?.ru ||
            interestKey;

        const tag = document.createElement('div');
        tag.className = 'interest-tag';
        tag.textContent = label;

        const remove = document.createElement('span');
        remove.className = 'remove-interest';
        remove.textContent = '×';
        remove.onclick = () => removeInterest(interestKey);
        tag.appendChild(remove);

        container.appendChild(tag);
    });
}

/**
 * Заполнение выпадающего списка интересов
 */
function populateInterestSelect() {
    const select = document.getElementById('interestSelect');

    // ✅ Очищаем выпадающий список
    select.innerHTML = '<option value="">Выбрать интересы</option>';

    if (!window.interestLabels || typeof window.interestLabels !== 'object' || Array.isArray(window.interestLabels)) {
        console.warn('window.interestLabels не является объектом');
        return;
    }

    // Фильтруем только те, которых ещё нет
    Object.keys(window.interestLabels).forEach(key => {
        if (!window.interests.includes(key)) {
            const option = document.createElement('option');
            option.value = key;
            option.textContent = window.interestLabels[key];
            select.appendChild(option);
        }
    });
}


/**
 * Удаление интереса — без confirm()
 */
async function removeInterest(interestKey) {
    // Удаляем без подтверждения
    try {
        const response = await fetch(`/sparkle/users/interests/delete/${interestKey}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'X-XSRF-TOKEN': window.csrfToken
            },
            credentials: 'include'
        });

        if (response.ok) {
            window.interests = window.interests.filter(i => i !== interestKey);
            renderInterestList();
            populateInterestSelect();
            showMessage('Интерес удалён', 'success');
        } else {
            throw new Error('Не удалось удалить');
        }
    } catch (error) {
        showMessage('Ошибка при удалении', 'error');
    }
}

/**
 * Настройка обработчиков событий
 */
function setupEventListeners() {
    document.getElementById('backBtn')?.addEventListener('click', () => {
        window.location.href = '/main';
    });

    document.getElementById('prevPhoto')?.addEventListener('click', () => {
        if (window.photos.length <= 1) return;
        window.currentPhotoIndex = (window.currentPhotoIndex - 1 + window.photos.length) % window.photos.length;
        showCurrentPhoto();
        updatePhotoIndicator();
    });

    document.getElementById('nextPhoto')?.addEventListener('click', () => {
        if (window.photos.length <= 1) return;
        window.currentPhotoIndex = (window.currentPhotoIndex + 1) % window.photos.length;
        showCurrentPhoto();
        updatePhotoIndicator();
    });

    document.getElementById('uploadBtn')?.addEventListener('click', () => {
        document.getElementById('photoInput').click();
    });

    document.getElementById('photoInput')?.addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        await handlePhotoUpload(file);
    });

    document.getElementById('deleteBtn')?.addEventListener('click', async () => {
        await removePhoto();
    });

    document.getElementById('addInterestBtn')?.addEventListener('click', async () => {
        const selected = Array.from(document.getElementById('interestSelect').selectedOptions)
            .map(o => o.value);
        if (selected.length === 0) {
            showMessage('Выберите интересы', 'error');
            return;
        }
        await addInterests(selected);
    });

    document.getElementById('saveAboutBtn')?.addEventListener('click', async () => {
        const text = document.getElementById('aboutMe').value.trim();
        const data = text === '' ? null : text;
        await updateAboutMe(data);
    });
}

/**
 * Загрузка фото
 */
async function handlePhotoUpload(file) {
    if (file.size > 5 * 1024 * 1024) {
        return showMessage('Файл слишком большой (макс 5 МБ)', 'error');
    }
    if (window.photos.length >= 5) {
        return showMessage('Можно загрузить не более 5 фото', 'error');
    }

    const fd = new FormData();
    fd.append('file', file);

    try {
        const res = await fetch('/sparkle/users/photo/upload-photo', {
            method: 'POST',
            body: fd,
            headers: { 'X-XSRF-TOKEN': window.csrfToken }
        });

        if (res.ok) {
            const photo = await res.json();
            window.photos.push(photo);
            showCurrentPhoto();
            renderThumbnails();
            updatePhotoIndicator();
            showMessage('Фото загружено', 'success');
        } else {
            throw new Error();
        }
    } catch (e) {
        showMessage('Ошибка загрузки фото', 'error');
    }
}

/**
 * Удаление фото
 */
async function removePhoto() {
    const photoId = window.photos[window.currentPhotoIndex].id;
    try {
        const res = await fetch(`/sparkle/users/photo/remove-photo/photos/${photoId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': window.csrfToken
            },
            body: JSON.stringify({ userId: currentUserId })
        });

        if (res.ok) {
            window.photos.splice(window.currentPhotoIndex, 1);
            if (window.photos.length === 0) {
                resetPhotoDisplay();
            } else {
                window.currentPhotoIndex = Math.max(0, window.currentPhotoIndex - 1);
                showCurrentPhoto();
                updatePhotoIndicator();
            }
            renderThumbnails();
            showMessage('Фото удалено', 'success');
        } else {
            throw new Error();
        }
    } catch (e) {
        showMessage('Ошибка удаления', 'error');
    }
}

/**
 * Добавление интересов — правильная версия
 */
async function addInterests(keys) {
    try {
        const res = await fetch('/sparkle/users/interests/create-all', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': window.csrfToken
            },
            body: JSON.stringify(keys.map(key => ({ interest: key, userId: currentUserId })))
        });

        if (res.ok) {
            // ✅ Перезагружаем весь профиль — это безопасно и надёжно
            await loadUserData();
            showMessage('Интересы добавлены', 'success');
        } else {
            throw new Error('Сервер вернул ошибку');
        }
    } catch (e) {
        console.error('Ошибка при добавлении интересов:', e);
        showMessage('Не удалось добавить интересы', 'error');
    }
}

/**
 * Обновление "О себе"
 */
async function updateAboutMe(text) {
    try {
        const res = await fetch(`/sparkle/users/update-profile`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': window.csrfToken
            },
            body: JSON.stringify({ aboutMe: text })
        });

        if (res.ok) {
            showMessage('Обновлено', 'success');
        } else {
            throw new Error();
        }
    } catch (e) {
        showMessage('Ошибка сохранения', 'error');
    }
}

/**
 * Показ сообщений
 */
function showMessage(text, type) {
    const el = document.getElementById('messages');
    el.innerHTML = `<div class="alert alert-${type}">${text}</div>`;
    setTimeout(() => el.innerHTML = '', 3000);
}

/**
 * Показ ошибки
 */
function showError(message) {
    document.querySelector('.main-content').innerHTML = `
        <div class="error" style="text-align:center; padding:2rem;">
            <p>${message}</p>
            <button onclick="window.location.href='/main'" class="action-btn">На главную</button>
        </div>
    `;
}