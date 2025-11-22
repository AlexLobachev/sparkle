let users = [];
let currentUserIndex = 0;
let currentPhotoIndex = 0;
let isAnimating = false;

const csrfMeta = document.querySelector('meta[name="csrf-token"]');
const csrfToken = csrfMeta?.getAttribute('content');




console.log('CSRF Meta:', csrfMeta); // Должен быть элемент
console.log('CSRF Token:', csrfToken); // Должен быть не null
console.log('X-XSRF-TOKEN header:', {
    'X-XSRF-TOKEN': csrfToken,
    'Content-Type': 'application/json'
});



// Получение следующего кандидата (единственный метод загрузки)
async function loadNextCandidate() {
    try {
        const url = `http://localhost:8080/sparkle/users/match/next-candidate/users?distance=70&page=0&size=1`;
        const response = await fetch(url);

        // Обрабатываем 204 No Content
        if (response.status === 204) {
            showNoCandidates();
            return;
        }

        // Для других ошибок (4xx, 5xx)
        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status} ${response.statusText}`);
        }

        // Только если статус 200 OK — парсим JSON
        const candidate = await response.json();

        // Добавляем кандидата в список
        users.push(candidate);
        currentUserIndex = users.length - 1;
        currentPhotoIndex = 0;

        renderProfile();
    } catch (error) {
        console.error('Ошибка при загрузке следующего кандидата:', error.message);
        showError();
    }
}

function showNoCandidates() {
    const profileInfo = document.getElementById('profileInfo');
    const currentPhoto = document.getElementById('currentPhoto');

    if (!profileInfo || !currentPhoto) return;

    // Очищаем фото (устанавливаем серый фон или заглушку)
    currentPhoto.style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+кандидатов)';
    // Или просто: currentPhoto.style.backgroundImage = 'none';
    currentPhoto.style.backgroundColor = '#f5f5f5'; // Светло‑серый фон

    // Текст сообщения
    profileInfo.innerHTML = '<p>Нет доступных кандидатов</p>';

    // Скрываем элементы управления
    const controls = document.getElementById('controls');
    const indicator = document.getElementById('indicator');
    const photoIndicator = document.getElementById('photoIndicator');

    if (controls) controls.style.display = 'none';
    if (indicator) indicator.style.display = 'none';
    if (photoIndicator) photoIndicator.style.display = 'none';

    // Добавляем кнопку «Попробовать снова»
    const retryBtn = document.createElement('button');
    retryBtn.className = 'action-btn retry';
    retryBtn.textContent = 'Попробовать снова';
    retryBtn.style.margin = '1rem auto';
    retryBtn.style.display = 'block';

    retryBtn.addEventListener('click', () => {
        loadNextCandidate();
        retryBtn.remove();
    });

    profileInfo.appendChild(retryBtn);
}





// Индикатор профилей (точки внизу)
function updateProfileIndicator() {
    const indicator = document.getElementById('indicator');
    indicator.innerHTML = '';

    users.forEach((_, i) => {
        const dot = document.createElement('div');
        dot.className = i === currentUserIndex ? 'dot active' : 'dot';
        indicator.appendChild(dot);
    });
}

// Индикатор фото (если их несколько)
function updatePhotoIndicator(totalPhotos) {
    const indicator = document.getElementById('photoIndicator');
    indicator.innerHTML = '';

    if (totalPhotos <= 1) return; // Не показываем точки, если фото 0–1

    for (let i = 0; i < totalPhotos; i++) {
        const dot = document.createElement('div');
        dot.className = i === currentPhotoIndex ? 'photo-dot active' : 'photo-dot';
        indicator.appendChild(dot);
    }
}

// Листать фото внутри профиля
function showNextPhoto() {
    const user = users[currentUserIndex];
    if (user.photos.length === 0) return;

    currentPhotoIndex = (currentPhotoIndex + 1) % user.photos.length;
    renderProfile();
}

function showPrevPhoto() {
    const user = users[currentUserIndex];
    if (user.photos.length === 0) return;

    currentPhotoIndex = (currentPhotoIndex - 1 + user.photos.length) % user.photos.length;
    renderProfile();
}






// Отправить лайк
async function sendLike(userId) {

    try {
        const response = await fetch(`/sparkle/users/match/like/users/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken // Убедимся, что тут не undefined
            },
            credentials: 'include' // Обязательно для отправки куки
        });

        if (!response.ok) {
            throw new Error(`Ошибка лайка: ${response.statusText}`);
        }

        console.log(`Лайк отправлен для пользователя ${userId}`);
    } catch (error) {
        console.error('Ошибка отправки лайка:', error.message);
// Можно показать уведомление пользователю
        alert('Не удалось поставить лайк. Проверьте подключение.');
    }
}


// Обработчики кнопок

document.getElementById('likeBtn').addEventListener('click', async (e) => {
    e.stopPropagation();
    const user = users[currentUserIndex];
    if (!user || !user.id) return;

    try {
        await sendLike(user.id);
        loadNextCandidate();
    } catch (error) {
        console.error('Ошибка лайка:', error);
        alert('Не удалось поставить лайк.');
    }
});






document.getElementById('dislikeBtn').addEventListener('click', (e) => {
    e.stopPropagation();
    const user = users[currentUserIndex];
    if (!user) return;

    console.log('Дизлайк для:', user.username);
    loadNextCandidate();
});




document.getElementById('nextPhoto').addEventListener('click', showNextPhoto);
document.getElementById('prevPhoto').addEventListener('click', showPrevPhoto);

// Обработка свайпов (touch)
let touchStartX = 0;
let touchEndX = 0;

const profileViewer = document.getElementById('profileViewer');

profileViewer.addEventListener('touchstart', (e) => {
    touchStartX = e.touches[0].clientX;
});

profileViewer.addEventListener('touchmove', (e) => {
    touchEndX = e.touches[0].clientX;
});

profileViewer.addEventListener('touchend', (e) => {

    if (e.target.closest('#likeBtn') || e.target.closest('#dislikeBtn')) {
        return; // игнорируем, если клик был по кнопке
    }

    const swipeThreshold = 80;

    // Свайп влево → лайк + следующий
    if (touchStartX - touchEndX > swipeThreshold) {
        console.log('[Свайп] Влево → лайк');
        const user = users[currentUserIndex];
        if (user && user.id) {
            console.log('НЕ РЕАЛИЗОВАНО В ЭТОЙ ВЕРСИИ');
            //sendLike(user.id);
            //loadNextCandidate();
        }
    }

    // Свайп вправо → только следующий
    if (touchEndX - touchStartX > swipeThreshold) {
        console.log('НЕ РЕАЛИЗОВАНО В ЭТОЙ ВЕРСИИ');
        //console.log('[Свайп] Вправо → следующий');
        //loadNextCandidate();
    }
});

// Обработка свайпов (мышь)
let mouseStartX = 0;
let mouseEndX = 0;

profileViewer.addEventListener('mousedown', (e) => {
    mouseStartX = e.clientX;
    profileViewer.style.cursor = 'grabbing';
});

profileViewer.addEventListener('mousemove', (e) => {
    if (mouseStartX !== 0) mouseEndX = e.clientX;
});

profileViewer.addEventListener('mouseup', () => {
    profileViewer.style.cursor = 'grab';
    const swipeThreshold = 80;

    if (mouseStartX - mouseEndX > swipeThreshold) {
        console.log('НЕ РЕАЛИЗОВАНО В ЭТОЙ ВЕРСИИ');
        //const user = users[currentUserIndex];
        //sendLike(user.id);
        //loadNextCandidate();
    }

    if (mouseEndX - mouseStartX > swipeThreshold) {
        console.log('НЕ РЕАЛИЗОВАНО В ЭТОЙ ВЕРСИИ');
        //loadNextCandidate();
    }

    mouseStartX = 0;
    mouseEndX = 0;
});

// Предотвращение стандартного drag
profileViewer.addEventListener('dragstart', (e) => e.preventDefault());

// Сообщения об ошибках
function showError() {
    const profileInfo = document.getElementById('profileInfo');
    if (!profileInfo) return;

    profileInfo.innerHTML = '<p>Произошла ошибка. Попробуйте позже.</p>';
    const controls = document.getElementById('controls');
    const indicator = document.getElementById('indicator');
    const photoIndicator = document.getElementById('photoIndicator');

    if (controls) controls.style.display = 'none';
    if (indicator) indicator.style.display = 'none';
    if (photoIndicator) photoIndicator.style.display = 'none';

    document.getElementById('controls').style.display = 'none';
    document.getElementById('indicator').style.display = 'none';
    document.getElementById('photoIndicator').style.display = 'none';

// Кнопка повтора
    const retryBtn = document.createElement('button');
    retryBtn.className = 'action-btn retry';
    retryBtn.textContent = 'Повторить попытку';
    retryBtn.style.margin = '1rem auto';
    retryBtn.style.display = 'block';

    retryBtn.addEventListener('click', () => {
        loadNextCandidate(); // Пытаемся загрузить кандидата снова
        retryBtn.remove();
    });

    profileInfo.appendChild(retryBtn);
}

document.addEventListener('DOMContentLoaded', () => {
    const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');
    console.log('CSRF Token:', csrfToken);
    loadNextCandidate();
    // Если токен устарел или отсутствует, получите свежий токен
    if (!csrfToken) {
        fetch('/refresh-csrf-token', {
            method: 'GET',
            credentials: 'same-origin'
        }).then(response => {
            if (response.ok) {
                console.log('Новый CSRF-токен получен');
            } else {
                console.error('Ошибка при получении CSRF-токена');
            }
        });
    }
});


// Инициализация: при загрузке страницы загружаем первого кандидата
//document.addEventListener('DOMContentLoaded', () => {
//    loadNextCandidate();
//});

// Обработчики навигации
document.getElementById('profileBtn').addEventListener('click', () => {
    window.location.href = '/main/profile';
});

document.getElementById('editProfileBtn').addEventListener('click', () => {
    window.location.href = '/main/settings/profile';
});

document.getElementById('logoutBtn').addEventListener('click', function() {
// Отправляем POST‑запрос на сервер для завершения сессии
    fetch('/logout', {
        method: 'POST',
        headers: {
            'X-XSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').getAttribute('content')
        },
        credentials: 'same-origin' // Обязательно для отправки куки JSESSIONID
    })
        .then(response => {
            if (response.ok) {
// Успешно → перенаправляем на главную
                window.location.href = '/';
            } else {
                alert('Ошибка при выходе. Попробуйте ещё раз.');
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось выйти. Проверьте подключение к сети.');
        });
});

// Дополнительные стили для кнопки повтора (можно добавить в styles.css)
const style = document.createElement('style');
style.textContent = `
.retry {
background-color: #ff9800;
color: white;
}
.retry:hover {
background-color: #e68a00;
}
`;
document.head.appendChild(style);

// Доп. функции для улучшения UX

/**
 * Показывает лоадер во время загрузки кандидата
 */
function showLoader() {
    const loader = document.createElement('div');
    loader.className = 'loader';
    loader.innerHTML = 'Загружаем...';
    document.getElementById('profileViewer').appendChild(loader);
}

/**
 * Скрывает лоадер
 */
function hideLoader() {
    const loader = document.querySelector('.loader');
    if (loader) loader.remove();
}

/**
 * Обновляет состояние UI при загрузке
 */
function updateUIState(loading = false) {
    const controls = document.getElementById('controls');
    const indicator = document.getElementById('indicator');

    if (loading) {
        controls.style.opacity = '0.5';
        indicator.style.opacity = '0.5';
    } else {
        controls.style.opacity = '1';
        indicator.style.opacity = '1';
    }
}



// Модифицируем renderProfile для обработки крайних случаев
function renderProfile() {
    if (isAnimating || currentUserIndex >= users.length || users.length === 0) return;

    isAnimating = true;

    const user = users[currentUserIndex];

// Фото: если нет фото — показываем заглушку
    const photoUrl = user.photos && user.photos.length > 0
        ? user.photos[currentPhotoIndex].url
        : 'https://placehold.co/400x400/CCCCCC/FFFFFF?text=Фото+нет';

    const currentPhoto = document.getElementById('currentPhoto');
    currentPhoto.style.backgroundImage = `url(${photoUrl})`;

// Возраст: вычисляем из birthDate
    const birthDate = new Date(user.birthDate);
    const today = new Date();
    const age = today.getFullYear() - birthDate.getFullYear();

// Город: берём из cityDto
    const cityName = user.city?.cityName || 'Не указан';

// О себе: если null — заглушка
    const aboutMe = user.aboutMe || 'О себе не рассказано';

// Обновляем информацию о пользователе
    const profileInfo = document.getElementById('profileInfo');
    profileInfo.innerHTML = `
<h3>${user.username}, ${age}</h3>
<p><strong>Город:</strong> ${cityName}</p>
<p>${aboutMe}</p>
`;

// Обновляем индикаторы
    updateProfileIndicator();
    updatePhotoIndicator(user.photos?.length || 0);

    setTimeout(() => {
        isAnimating = false;
    }, 500);


    function updateUIState(loading = false) {
        const controls = document.getElementById('controls');
        const indicator = document.getElementById('indicator');

// Проверяем наличие элементов
        if (controls) {
            controls.style.opacity = loading ? '0.5' : '1';
        }
        if (indicator) {
            indicator.style.opacity = loading ? '0.5' : '1';
        }
    }
}