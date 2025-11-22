// Единый обработчик DOMContentLoaded
document.addEventListener('DOMContentLoaded', async () => {
    try {
        // 1. Валидация userId
        const userId = window.currentUserId;
        if (typeof userId !== 'number' || isNaN(userId) || userId <= 0) {
            throw new Error('Некорректный ID пользователя');
        }

        // 2. Получение CSRF-токена
        const csrfToken = getCsrfToken();
        if (!csrfToken) {
            throw new Error('CSRF-токен не найден');
        }

        // 3. Загрузка данных профиля
        await loadProfileData(userId, csrfToken);

        // 4. Настройка обработчиков
        setupEventListeners(userId, csrfToken);

    } catch (error) {
        console.error('Инициализация профиля не удалась:', error);
        showError(error.message || 'Произошла ошибка при загрузке профиля');
    }
});

// Получение CSRF-токена с проверкой
function getCsrfToken() {
    const meta = document.querySelector('meta[name="csrf-token"]');
    if (!meta) return null;
    const token = meta.getAttribute('content');
    return token && token.trim().length > 0 ? token.trim() : null;
}

// Загрузка данных профиля
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
        populateForm(user);

    } catch (error) {
        throw error; // Передаём дальше для обработки в верхнем блоке
    } finally {
        showLoading(false);
    }
}

// Заполнение формы
function populateForm(user) {
    const fields = {
        'username': 'username',
        'gender': 'gender',
        'preferredGender': 'preferredGender',
        'email': 'email',
        'birthDate': 'birthDate'
    };

    for (const [key, id] of Object.entries(fields)) {
        const el = document.getElementById(id);
        if (!el) {
            console.warn(`Поле ${id} не найдено в DOM`);
            continue;
        }

        if (key === 'birthDate' && user[key]) {
            // Безопасное преобразование даты
            const date = new Date(user[key]);
            if (!isNaN(date.getTime())) {
                el.value = date.toISOString().split('T')[0];
            }
        } else {
            // Для email: если значение null, оставляем поле пустым
            el.value = (key === 'email' && user[key] === null) ? '' : (user[key] || '');
        }
    }
}

// Настройка обработчиков
function setupEventListeners(userId, csrfToken) {
    const form = document.getElementById('profileForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await saveProfile(userId, csrfToken, form);
        });
    }

    const deleteBtn = document.getElementById('deleteProfileBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', async () => {
            if (confirm('Вы уверены, что хотите удалить профиль? Это действие необратимо!')) {
                await deleteProfile(userId, csrfToken);
            }
        });
    }

    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            window.location.href = '/main';
        });
    }
}

// Сохранение профиля
async function saveProfile(userId, csrfToken, form) {
    showLoading(true);

    try {
        // Валидация и фильтрация данных
        const validData = validateFormData(new FormData(form));
        if (!validData) {
            showError('Проверьте заполненные данные');
            return;
        }

        const response = await fetch(`/sparkle/users/update-profile/${userId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(validData),
            credentials: 'same-origin'
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(`HTTP ${response.status}: ${text}`);
        }

        showSuccess('Профиль успешно обновлён!');
        setTimeout(() => window.location.href = '/main', 1500);

    } catch (error) {
        console.error('Ошибка сохранения:', error);
        showError(error.message || 'Не удалось сохранить изменения');
    } finally {
        showLoading(false);
    }
}

// Удаление профиля
async function deleteProfile(userId, csrfToken) {
    showLoading(true);

    try {
        const response = await fetch(`/sparkle/users/${userId}`, {
            method: 'DELETE',
            headers: { 'X-XSRF-TOKEN': csrfToken },
            credentials: 'same-origin'
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(`HTTP ${response.status}: ${text}`);
        }

        showSuccess('Профиль удалён!');
        setTimeout(() => window.location.href = '/logout', 1500);

    } catch (error) {
        console.error('Ошибка удаления:', error);
        showError(error.message || 'Не удалось удалить профиль');
    } finally {
        showLoading(false);
    }
}

// Валидация данных формы
function validateFormData(formData) {
    const data = Object.fromEntries(formData.entries());

    // Обработка email
    if (data.email && data.email.trim() !== '') {
        // Если email заполнен — проверяем корректность
        if (!data.email.includes('@')) {
            return null; // Некорректный email
        }
        data.email = data.email.trim(); // Очищаем от лишних пробелов
    } else {
        // Если email пуст или не заполнен — устанавливаем null
        data.email = null;
    }

    return data;
}

// Индикация загрузки
function showLoading(isLoading) {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.style.display = isLoading ? 'block' : 'none';
    }
}

// Сообщения
function showError(message) {
    const alertBox = createAlert('error', message);
    document.querySelector('.profile-form').prepend(alertBox);
}

function showSuccess(message) {
    const alertBox = createAlert('success', message);
    document.querySelector('.profile-form').prepend(alertBox);
}

function createAlert(type, message) {
    const alertBox = document.createElement('div');
    alertBox.className = `alert alert-${type}`;
    alertBox.textContent = message;

    // Добавляем кнопку закрытия
    const closeBtn = document.createElement('button');
    closeBtn.className = 'alert-close';
    closeBtn.textContent = '×';
    closeBtn.addEventListener('click', () => {
        alertBox.remove();
    });
    alertBox.appendChild(closeBtn);

    // Автоматическое скрытие через время
    setTimeout(() => {
        if (document.body.contains(alertBox)) {
            alertBox.remove();
        }
    }, type === 'success' ? 3000 : 5000);

    return alertBox;
}
