document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('profileForm');
    const deleteBtn = document.getElementById('deleteProfileBtn');
    const backBtn = document.getElementById('backBtn');
    const messages = document.getElementById('messages');

    const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');

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
            showMessage('Не удалось загрузить данные', 'error');
        }
    }

    function fillForm(user) {
        if (user.gender) {
            document.querySelector(`input[name="gender"][value="${user.gender}"]`)?.click();
        }
        if (user.preferredGender) {
            document.querySelector(`input[name="preferredGender"][value="${user.preferredGender}"]`)?.click();
        }
        document.getElementById('email').value = user.email || '';
        if (user.birthDate) {
            document.getElementById('birthDate').value = user.birthDate.split('T')[0];
        }
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const data = {};

        for (let [key, value] of formData.entries()) {
            if (typeof value === 'string') value = value.trim();
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
                showMessage('Данные сохранены', 'success');
            } else {
                throw new Error('Ошибка сервера');
            }
        } catch (error) {
            showMessage('Не удалось сохранить', 'error');
        }
    });

    deleteBtn?.addEventListener('click', async () => {
        if (!confirm('Удалить профиль? Это нельзя отменить.')) return;

        try {
            const response = await fetch(`/sparkle/users/delete/${window.currentUserId}`, {
                method: 'DELETE',
                headers: { 'X-XSRF-TOKEN': csrfToken },
                credentials: 'include'
            });

            if (response.ok) {
                alert('Профиль удалён');
                window.location.href = '/logout';
            } else {
                showMessage('Ошибка при удалении', 'error');
            }
        } catch (error) {
            showMessage('Не удалось удалить', 'error');
        }
    });

    backBtn?.addEventListener('click', () => {
        window.location.href = '/main';
    });

    function showMessage(text, type) {
        messages.innerHTML = '';
        const msg = document.createElement('div');
        msg.className = `alert alert-${type}`;
        msg.textContent = text;
        messages.appendChild(msg);

        setTimeout(() => msg.remove(), 3000);
    }
});