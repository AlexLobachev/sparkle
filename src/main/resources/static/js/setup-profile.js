document.addEventListener('DOMContentLoaded', () => {
    const interestsContainer = document.getElementById('interestsContainer');
    const setupForm = document.getElementById('setupForm');
    const messages = document.getElementById('messages');
    const profilePhotoInput = document.getElementById('profilePhoto');
    const photoPreview = document.getElementById('photoPreview');

    // Список интересов из enum (можно передать через Thymeleaf или API)
    const INTERESTS = [
        'FOOTBALL', 'BASKETBALL', 'TENNIS', 'SWIMMING', 'GYM',
        'PAINTING', 'MUSIC', 'DANCE', 'WRITING',
        'COOKING', 'PHOTOGRAPHY', 'READING', 'TRAVEL',
        'PROGRAMMING', 'LANGUAGES', 'SCIENCE', 'BUSINESS',
        'MOVIES', 'GAMING', 'SOCIAL_MEDIA', 'OTHER'
    ];

    // Отображение названия интереса (можно улучшить через API/словарь)
    const interestLabels = {
        'FOOTBALL': 'Футбол',
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

    // Генерируем чекбоксы интересов (как было)
    if (interestsContainer) {
        INTERESTS.forEach(interestKey => {
            const div = document.createElement('div');
            div.className = 'interest-item';

            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = `interest_${interestKey}`;
            checkbox.name = 'interests';
            checkbox.value = interestKey;

            const label = document.createElement('label');
            label.htmlFor = `interest_${interestKey}`;
            label.textContent = interestLabels[interestKey];

            div.appendChild(checkbox);
            div.appendChild(label);
            interestsContainer.appendChild(div);
        });
    }

    // Предварительный просмотр фото (как было)
    if (profilePhotoInput && photoPreview) {
        profilePhotoInput.addEventListener('change', (e) => {
            if (e.target.files && e.target.files[0]) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    photoPreview.innerHTML = '';
                    photoPreview.appendChild(img);
                };
                reader.readAsDataURL(e.target.files[0]);
            }
        });
    }

    async function checkUserStatus() {
        try {
            const response = await fetch('/main');
            const user = await response.json();

            if (user.status === 'DRAFT') {
                window.location.href = '/profile/setup';
                return false;
            }
            return true;
        } catch (error) {
            console.error('Ошибка при проверке статуса пользователя:', error);
            return false;
        }
    }

    function validateForm() {
        const emailInput = document.getElementById('email');
        if (!emailInput) return true; // Если поля нет — пропускаем


        const emailValue = emailInput.value.trim();

        // Если email пуст — разрешаем (будет null на сервере)
        if (emailValue !== '') {
            // Проверяем формат email
            const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            if (!emailRegex.test(emailValue)) {
                showMessage('Некорректный email', 'error');
                return false;
            }
        }

        return true; // Форма валидна
    }

    // Собираем только основные данные формы (без фото и интересов)
    function getBasicUserData() {
        const formData = new FormData(setupForm);
        const data = {};

        for (let [key, value] of formData.entries()) {
            // Исключаем интересы и поле файла
            if (key === 'interests' || key === 'profilePhoto') continue;

            if (typeof value === 'string' && value.trim() !== '') {
                data[key] = value;
            } else {
                data[key] = null;
            }
        }

        return data;
    }

    // Отправляем основные данные профиля
    async function saveBasicProfile(data) {
        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');

        const response = await fetch('/sparkle/users/setup-profile', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(data),
            credentials: 'same-origin'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ошибка сохранения профиля: ${response.status} - ${errorText}`);
        }

        return await response.json(); // Можно вернуть ID пользователя или др. данные
    }

    // Загружаем фото
    async function uploadPhoto(file) {
        if (!file) return; // Если фото не выбрано — пропускаем

        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/sparkle/users/photo/upload-photo', {
            method: 'POST',
            headers: {
                'X-XSRF-TOKEN': csrfToken
            },
            body: formData,
            credentials: 'same-origin'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ошибка загрузки фото: ${response.status} - ${errorText}`);
        }

        return await response.json();
    }

    // Сохраняем интересы
    async function saveInterests(interestsArray) {
        if (interestsArray.length === 0) return; // Если интересов нет — пропускаем

        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');

        const response = await fetch('/sparkle/users/interests/create-all', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(interestsArray),
            credentials: 'same-origin'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ошибка сохранения интересов: ${response.status} - ${errorText}`);
        }

        return await response.json();
    }

    // Обработка отправки формы
    if (setupForm) {
        setupForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!validateForm()) return;

            try {
                // 1. Собираем основные данные
                const basicData = getBasicUserData();

                // 2. Сохраняем профиль
                showMessage('Сохраняем профиль...', 'info');
                await saveBasicProfile(basicData);
                showMessage('Профиль сохранён!', 'success');

                // 3. Загружаем фото (если есть)
                const photoFile = profilePhotoInput?.files[0];
                if (photoFile) {
                    showMessage('Загружаем фото...', 'info');
                    await uploadPhoto(photoFile);
                    showMessage('Фото загружено!', 'success');
                }


                // 4. Сохраняем интересы
                const selectedInterests = Array.from(
                    setupForm.querySelectorAll('input[name="interests"]:checked')
                ).map(cb => ({interest: cb.value}));

                if (selectedInterests.length > 0) {
                    showMessage('Сохраняем интересы...', 'info');
                    await saveInterests(selectedInterests);
                    showMessage('Интересы сохранены!', 'success');
                }

                // Всё успешно — переходим на главную
                showMessage('Профиль успешно создан! Переходим на главную...', 'success');
                setTimeout(() => {
                    window.location.href = '/main';
                }, 1500);

            } catch (error) {
                console.error('Ошибка при сохранении:', error);
                showMessage(`Произошла ошибка: ${error.message}`, 'error');
            }
        });
    }

    // Выход
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function () {
            fetch('/logout', {
                method: 'POST',
                headers: {
                    'X-XSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').getAttribute('content')
                },
                credentials: 'same-origin'
            })
                .then(response => {
                    if (response.ok) {
                        window.location.href = '/';
                    } else {
                        showMessage('Ошибка при выходе. Попробуйте ещё раз.', 'error');
                    }
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    showMessage('Не удалось выйти. Проверьте подключение к сети.', 'error');
                });
        });
    }

    // Функция для отображения сообщений (как было, но можно улучшить)
    function showMessage(text, type) {
        if (!messages) return;

        messages.innerHTML = '';
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type}`;
        alertDiv.textContent = text;
        messages.appendChild(alertDiv);

        setTimeout(() => alertDiv.remove(), 5000);
    }

    // Инициализация: проверка статуса пользователя при загрузке страницы
    checkUserStatus().catch(error => {
        console.error('Ошибка при инициализации:', error);
        showMessage('Не удалось загрузить данные пользователя. Попробуйте обновить страницу.', 'error');
    });
});
