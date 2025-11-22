

const INTERESTS_TRANSLATIONS = {
    "FOOTBALL": {
        en: "Football",
        ru: "Футбол"
    },
    "LITRBALL": {
        en: "drunkenness",
        ru: "Пьянство"
    },
    "BASKETBALL": {
        en: "Basketball",
        ru: "Баскетбол"
    },
    "TENNIS": {
        en: "Tennis",
        ru: "Теннис"
    },
    "SWIMMING": {
        en: "Swimming",
        ru: "Плавание"
    },
    "GYM": {
        en: "Gym",
        ru: "Тренажёрный зал"
    },
    "PAINTING": {
        en: "Painting",
        ru: "Рисование"
    },
    "MUSIC": {  // Вероятно, опечатка: должно быть "MUSIC" → "MUSIC"?
        en: "Music",
        ru: "Музыка"
    },
    "DANCE": {
        en: "Dance",
        ru: "Танцы"
    },
    "WRITING": {
        en: "Writing",
        ru: "Писательство"
    },
    "COOKING": {
        en: "Cooking",
        ru: "Кулинария"
    },
    "PHOTOGRAPHY": {
        en: "Photography",
        ru: "Фотография"
    },
    "READING": {
        en: "Reading",
        ru: "Чтение"
    },
    "TRAVEL": {
        en: "Travel",
        ru: "Путешествия"
    },
    "PROGRAMMING": {  // Опечатка: "PROGRAMMING" → "PROGRAMMING"?
        en: "Programming",
        ru: "Программирование"
    },
    "LANGUAGES": {
        en: "Languages",
        ru: "Языки"
    },
    "SCIENCE": {
        en: "Science",
        ru: "Наука"
    },
    "BUSINESS": {
        en: "Business",
        ru: "Бизнес"
    },
    "MOVIES": {
        en: "Movies",
        ru: "Фильмы"
    },
    "GAMING": {
        en: "Gaming",
        ru: "Гейминг"
    },
    "SOCIAL_MEDIA": {
        en: "Social Media",
        ru: "Соцсети"
    },
    "OTHER": {
        en: "Other",
        ru: "Другое"
    }
};

// Далее идут функции...


document.addEventListener('DOMContentLoaded', async () => {
    try {
        // 1. Валидация userId
        const userId = window.currentUserId;
        if (typeof userId !== 'number' || isNaN(userId) || userId <= 0) {
            throw new Error('Некорректный ID пользователя ' + userId);
        }

        // 2. Получение CSRF-токена
        const csrfToken = getCsrfToken();
        if (!csrfToken) {
            throw new Error('CSRF-токен не найден');
        }

        // 3. Загрузка ВСЕХ данных пользователя одним запросом
        const user = await loadProfileData(userId, csrfToken);

        // 4. Заполнение интерфейса данными из user
        populateProfile(user);

        // 5. Настройка обработчиков событий
        setupEventListeners(userId, csrfToken);

    } catch (error) {
        console.error('Инициализация профиля не удалась:', error);
        showError(error.message || 'Произошла ошибка при загрузке профиля');
    }
});

// Получение CSRF-токена
function getCsrfToken() {
    const meta = document.querySelector('meta[name="csrf-token"]');
    if (!meta) return null;
    const token = meta.getAttribute('content');
    return token && token.trim().length > 0 ? token.trim() : null;
}

// Загрузка данных профиля (один запрос)
async function loadProfileData(userId, csrfToken) {
    showLoading(true);
    try {
        const response = await fetch(`/sparkle/users/${userId}`, {
            method: 'GET',
            headers: {
                'X-XSRF-TOKEN': csrfToken,
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(`HTTP ${response.status}: ${text}`);
        }

        const user = await response.json();
        return user;

    } catch (error) {
        throw error;
    } finally {
        showLoading(false);
    }
}

// Заполнение интерфейса данными пользователя
function populateProfile(user, language = 'ru') {
    console.log('Полученные интересы:', user.interests);

    // Гарантируем, что interests — массив
    const rawInterests = Array.isArray(user.interests) ? user.interests : [];
    window.interests = normalizeInterests(rawInterests, language);

    console.log('Инициализированные интересы:', window.interests);


    renderInterests();
    populateInterestOptions(language);
    // 1. Фото
    window.photos = user.photos || [];
    window.currentPhotoIndex = 0;

    if (window.photos.length > 0) {
        showCurrentPhoto();
        renderThumbnails();
    } else {
        resetPhotoDisplay();
    }

    // 2. Интересы
    window.interests = normalizeInterests(user.interests || [], language);
    renderInterests();
    populateInterestOptions(language); // Обновляем список доступных интересов

    // 3. «О себе»
    const aboutMe = document.getElementById('aboutMe');
    if (aboutMe) {
        aboutMe.value = user.aboutMe || '';
    }

    // 4. Дополнительно: можно заполнить другие поля (имя, email и т.п.)
    // Например:
    // document.getElementById('username').textContent = user.username;
    // document.getElementById('email').textContent = user.email;
}

// Остальные функции (без изменений)
function showLoading(isLoading) {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.style.display = isLoading ? 'block' : 'none';
    }
}

function showError(message) {
    const messages = document.getElementById('messages');
    messages.innerHTML = `
        <div class="alert alert-error">${message}</div>
    `;
}

function showMessage(text, type) {
    const messages = document.getElementById('messages');
    messages.innerHTML = '';
    const msg = document.createElement('div');
    msg.className = `alert alert-${type}`;
    msg.textContent = text;
    messages.appendChild(msg);

    setTimeout(() => {
        msg.remove();
    }, 3000);
}

// Функции для работы с фото (без изменений)
function resetPhotoDisplay() {
    const currentPhoto = document.getElementById('currentPhoto');
    const likesCount = document.querySelector('.likes-count');
    const thumbnails = document.querySelector('.thumbnails');
    const deleteBtn = document.getElementById('deleteBtn');

    currentPhoto.src = '';
    currentPhoto.alt = 'Фото не загружено';
    likesCount.textContent = '0 лайков';
    thumbnails.innerHTML = '';
    deleteBtn.disabled = true;
}

function showCurrentPhoto() {
    if (window.photos.length === 0) return;

    const photo = window.photos[window.currentPhotoIndex];
    const currentPhoto = document.getElementById('currentPhoto');
    const likesCount = document.querySelector('.likes-count');

    currentPhoto.src = photo.url;
    currentPhoto.alt = photo.fileName || 'Фото';
    likesCount.textContent = `${photo.likes || 0} лайков`;

    document.getElementById('deleteBtn').disabled = false;
}

function renderThumbnails() {
    const thumbnails = document.querySelector('.thumbnails');
    thumbnails.innerHTML = '';

    window.photos.forEach((photo, index) => {
        const img = document.createElement('img');
        img.src = photo.url;
        img.alt = photo.fileName || 'Миниатюра';
        img.className = 'thumbnail';
        img.dataset.index = index;
        img.addEventListener('click', () => {
            window.currentPhotoIndex = index;
            showCurrentPhoto();
        });
        thumbnails.appendChild(img);
    });
}

// Функции для работы с интересами (без изменений)
// В функции renderInterests()
function renderInterests() {
    const interestList = document.querySelector('.interest-list');
    interestList.innerHTML = '';

    window.interests.forEach(interest => {
        const div = document.createElement('div');
        div.className = 'interest-tag';
        div.textContent = interest.name;

        // Передаём interest.key (например, "FOOTBALL")
        const removeBtn = document.createElement('span');
        removeBtn.className = 'remove-interest';
        removeBtn.textContent = '×';
        removeBtn.addEventListener('click', () => removeInterest(interest.key));

        div.appendChild(removeBtn);
        interestList.appendChild(div);
    });
}



async function populateInterestOptions() {
    const interestSelect = document.getElementById('interestSelect');

    try {
        const response = await fetch('/sparkle/users/interests/all', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken()
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        // Получаем массив строк ["Футбол", "Баскетбол", ...]
        const allInterests = await response.json();

        if (!Array.isArray(allInterests)) {
            throw new Error('Сервер вернул не массив');
        }

        interestSelect.innerHTML = '<option value="">Выбрать интересы (можно несколько)</option>';

        allInterests.forEach(interestLabel => {
            // Находим ключ enum по названию интереса
            const interestKey = Object.keys(INTERESTS_TRANSLATIONS)
                .find(key => INTERESTS_TRANSLATIONS[key].ru === interestLabel);

            // Проверяем, есть ли интерес уже у пользователя
            const isAlreadyAdded = window.interests.some(userInterest =>
                userInterest.key === interestKey
            );

            if (!isAlreadyAdded && interestKey) {
                const option = document.createElement('option');
                option.value = interestKey;
                option.textContent = interestLabel;
                interestSelect.appendChild(option);
            }
        });

    } catch (error) {
        console.error('Ошибка при загрузке списка интересов:', error);
        showMessage(`Не удалось загрузить список интересов: ${error.message}`, 'error');
    }
}

function updateInterestSelect(normalizedInterests) {
    const interestSelect = document.getElementById('interestSelect');
    interestSelect.innerHTML = '<option value="">Выбрать интерес</option>';

    normalizedInterests.forEach(interest => {
        const isAlreadyAdded = window.interests.some(userInterest =>
            userInterest.normalized === interest.normalized
        );

        if (!isAlreadyAdded) {
            const option = document.createElement('option');
            option.value = interest.key;
            option.textContent = interest.name;
            interestSelect.appendChild(option);
        }
    });
}



function normalizeInterests(rawInterests, language = 'ru') {
    if (!Array.isArray(rawInterests)) return [];


    return rawInterests.map((interestKey, index) => {
        const translation = INTERESTS_TRANSLATIONS[interestKey] || {en: interestKey, ru: interestKey};
        return {
            id: index,
            key: interestKey,
            originalId: interestKey, // Сохраняем исходный идентификатор
            name: translation[language] || translation.en,
            normalized: interestKey.toLowerCase()
        };
    });
}


// Обработчики событий (без изменений)
async function setupEventListeners(userId, csrfToken) {
    const uploadBtn = document.getElementById('uploadBtn');
    const deleteBtn = document.getElementById('deleteBtn');
    const addInterestBtn = document.getElementById('addInterestBtn');
    const saveAboutBtn = document.getElementById('saveAboutBtn');
    const backBtn = document.getElementById('backBtn');
    const interestSelect = document.getElementById('interestSelect');

    // Загрузка нового фото
    uploadBtn.addEventListener('click', () => {
        const photoInput = document.getElementById('photoInput');
        photoInput.click();
    });

    // Обработка выбора файла
    document.getElementById('photoInput').addEventListener('change', async (e) => {
        await handlePhotoUpload(e, userId, csrfToken);
    });

    // Удаление фото
    deleteBtn.addEventListener('click', async () => {
        await removePhoto(userId, csrfToken);
    });

    // Добавление интереса

    addInterestBtn.addEventListener('click', async () => {
        const selectedOptions = document.querySelectorAll('#interestSelect option:checked');

        if (selectedOptions.length === 0) {
            showMessage('Выберите хотя бы один интерес', 'error');
            return;
        }

        const interestKeys = Array.from(selectedOptions).map(opt => opt.value);
        await addInterests(userId, interestKeys, csrfToken);
    });



    async function addInterests(userId, interestKeys, csrfToken) {
        try {
            const requestBody = interestKeys.map(key => ({
                interest: key,
                userId: userId
            }));

            const response = await fetch('/sparkle/users/interests/create-all', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const newInterests = await response.json();

            // 1. Нормализуем ответ сервера (в зависимости от формата)
            let interestNames = [];
            if (Array.isArray(newInterests)) {
                if (typeof newInterests[0] === 'string') {
                    interestNames = newInterests; // ["Футбол", "Баскетбол"]
                } else if (newInterests[0].name) {
                    interestNames = newInterests.map(item => item.name); // из объектов
                }
            } else if (newInterests?.interests) {
                interestNames = newInterests.interests; // из объекта { interests: [...] }
            }

            if (!interestNames.length) {
                throw new Error('Сервер не вернул интересы');
            }

            // 2. Добавляем только уникальные интересы
            interestNames.forEach(interestName => {
                // Проверяем, есть ли уже такой интерес
                const isDuplicate = window.interests.some(existing =>
                    existing.name === interestName
                );

                if (!isDuplicate) {
                    const interestKey = Object.keys(INTERESTS_TRANSLATIONS)
                        .find(key => INTERESTS_TRANSLATIONS[key].ru === interestName);

                    window.interests.push({
                        key: interestKey || interestName,
                        name: interestName,
                        normalized: interestName.toLowerCase()
                    });
                }
            });

            renderInterests();
            populateInterestOptions();
            showMessage(`Добавлено ${interestNames.length} новых интересов`, 'success');

        } catch (error) {
            console.error('Ошибка при добавлении интересов:', error);
            showMessage(`Не удалось добавить интересы: ${error.message}`, 'error');
        }
    }







    // Сохранение информации «О себе»
    saveAboutBtn.addEventListener('click', async () => {
        const aboutText = document.getElementById('aboutMe').value.trim();
        await updateAboutMe(userId, aboutText, csrfToken);
    });

    // Кнопка «Назад»
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            window.location.href = '/main';
        });
    }
}

// Обработка загрузки фото
async function handlePhotoUpload(event, userId, csrfToken) {
    const file = event.target.files[0];
    if (!file) return;

    // Проверка размера (5 МБ)
    if (file.size > 5 * 1024 * 1024) {
        showMessage('Файл слишком большой (максимум 5 МБ)', 'error');
        return;
    }

    // Проверка количества фото (максимум 5)
    if (window.photos.length >= 5) {
        showMessage('Можно загрузить не более 5 фотографий', 'error');
        return;
    }

    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/sparkle/users/photo/upload-photo', {
            method: 'POST',
            body: formData,
            headers: {
                'X-XSRF-TOKEN': csrfToken
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const newPhoto = await response.json();
        window.photos.push(newPhoto);
        showCurrentPhoto();
        renderThumbnails();
        showMessage('Фото загружено успешно', 'success');
    } catch (error) {
        console.error('Ошибка при загрузке фото:', error);
        showMessage(`Не удалось загрузить фото: ${error.message}`, 'error');
    }
}

// Удаление фото

async function removePhoto(userId, csrfToken) {
    if (window.currentPhotoIndex >= window.photos.length) return;

    const photoId = window.photos[window.currentPhotoIndex].id;

    try {
        const response = await fetch(`/sparkle/users/photo/remove-photo/photos/${photoId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                userId: userId  // Добавляем userId в тело запроса
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        window.photos.splice(window.currentPhotoIndex, 1);
        if (window.photos.length > 0) {
            window.currentPhotoIndex = Math.max(0, window.currentPhotoIndex - 1);
            showCurrentPhoto();
        } else {
            resetPhotoDisplay();
        }
        renderThumbnails();
        showMessage('Фото удалено', 'success');
    } catch (error) {
        console.error('Ошибка при удалении фото:', error);
        showMessage(`Не удалось удалить фото: ${error.message}`, 'error');
    }
}

// Добавление интереса
async function addInterest(userId, interestId, csrfToken) {
    try {
        const response = await fetch('/sparkle/users/interests/create-all', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                userId: userId,
                interestId: interestId
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const newInterest = await response.json();
        window.interests.push(newInterest);
        renderInterests();
        populateInterestOptions();
        showMessage('Интерес добавлен успешно', 'success');
    } catch (error) {
        console.error('Ошибка при добавлении интереса:', error);
        showMessage(`Не удалось добавить интерес: ${error.message}`, 'error');
    }
}

// Удаление интереса

async function removeInterest(interestKey) { // Изменили имя параметра на interestKey
    const csrfToken = getCsrfToken();

    try {
        const response = await fetch(`/sparkle/users/interests/delete/${interestKey}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        // Удаляем интерес из локального массива по ключу
        window.interests = window.interests.filter(interest => interest.key !== interestKey);

        renderInterests();
        populateInterestOptions();
        showMessage('Интерес удалён успешно', 'success');

    } catch (error) {
        console.error('Ошибка при удалении интереса:', error);
        showMessage(`Не удалось удалить интерес: ${error.message}`, 'error');
    }
}
function setLanguage(lang) {
    const currentUserId = window.currentUserId;
    const csrfToken = getCsrfToken();

    // Перезагружаем профиль с новым языком
    populateProfile(window.userProfileData, lang);

    // Обновляем список выбора
    populateInterestOptions(lang);
}

// Обработчик кнопки смены языка
document.getElementById('lang-toggle').addEventListener('click', () => {
    const currentLang = document.documentElement.lang || 'en';
    const newLang = currentLang === 'ru' ? 'en' : 'ru';

    document.documentElement.lang = newLang;
    setLanguage(newLang);
});


// Обновление информации «О себе»
async function updateAboutMe(userId, aboutText, csrfToken) {
    try {
        const response = await fetch(`/sparkle/users/update-profile/${userId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({aboutMe: aboutText})
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        showMessage('Информация обновлена успешно', 'success');
    } catch (error) {
        console.error('Ошибка при сохранении информации о себе:', error);
        showMessage(`Не удалось сохранить информацию: ${error.message}`, 'error');
    }
}
