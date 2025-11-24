/**
 * Скрипт для формы настроек профиля
 */
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('profileForm');
    const deleteBtn = document.getElementById('deleteProfileBtn');
    const backBtn = document.getElementById('backBtn');
    const messages = document.getElementById('messages');

    // Кнопка "Назад"
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            window.location.href = '/main';
        });
    }

    // CSRF токен
    const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');

    // Загрузка текущих данных
    loadProfileData();

    async function loadProfileData() {
        try {
            const response = await fetch(`/sparkle/users/${window.currentUserId}`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (!response.ok) throw new Error('Не удалось загрузить данные');

            const user = await response.json();
            fillForm(user);
        } catch (error) {
            console.error('Ошибка при загрузке профиля:', error);
            showMessage('Не удалось загрузить данные профиля', 'error');
        }
    }

    function fillForm(user) {
        document.getElementById('gender').value = user.gender || '';
        document.getElementById('preferredGender').value = user.preferredGender || '';
        document.getElementById('email').value = user.email || '';
        if (user.birthDate) {
            document.getElementById('birthDate').value = user.birthDate.split('T')[0];
        }
    }


    // Сохранение формы
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const data = {};

        // Обрабатываем каждое поле вручную, чтобы контролировать null
        for (let [key, value] of formData.entries()) {
            // Если значение — строка и состоит только из пробелов — отправляем null
            if (typeof value === 'string') {
                value = value.trim();
            }

            data[key] = value === '' ? null : value;
        }

        data.userId = window.currentUserId;

        try {
            const response = await fetch('/sparkle/users/update-profile', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(data),
                credentials: 'include'
            });

            if (response.ok) {
                showMessage('Данные успешно обновлены', 'success');
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            console.error('Ошибка при сохранении:', error);
            showMessage('Не удалось сохранить изменения', 'error');
        }
    });

    // Удаление профиля
    deleteBtn?.addEventListener('click', async () => {
        if (!confirm('Вы уверены, что хотите удалить профиль? Это действие нельзя отменить.')) {
            return;
        }

        try {
            const response = await fetch(`/sparkle/users/delete/${window.currentUserId}`, {
                method: 'DELETE',
                headers: {
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (response.ok) {
                alert('Профиль удалён');
                window.location.href = '/logout';
            } else {
                throw new Error('Не удалось удалить профиль');
            }
        } catch (error) {
            console.error('Ошибка при удалении:', error);
            showMessage('Не удалось удалить профиль', 'error');
        }
    });

    function showMessage(text, type) {
        messages.innerHTML = '';
        const msg = document.createElement('div');
        msg.className = `alert alert-${type}`;
        msg.textContent = text;
        messages.appendChild(msg);

        setTimeout(() => {
            if (msg.parentNode === messages) {
                msg.remove();
            }
        }, 3000);
    }
});