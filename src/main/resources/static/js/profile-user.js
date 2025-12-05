/**
 * Исправленный скрипт для страницы профиля пользователя
 * ✅ Загрузка данных
 * ✅ Слайдер фото
 * ✅ Кнопка "Назад"
 * ✅ Кнопка "Написать" → переход на /chat/{id}
 */
document.addEventListener('DOMContentLoaded', async () => {
    console.log('✅ DOM загружен, инициализация профиля');

    // === Элементы ===
    const bottomBackBtn = document.getElementById('bottomBackBtn');
    const sendMessageBtn = document.getElementById('sendMessageBtn');

    const usernameEl = document.getElementById('username');
    const ageEl = document.getElementById('age');
    const genderEl = document.getElementById('gender');
    const cityEl = document.getElementById('city');
    const aboutMeText = document.getElementById('aboutMe')?.querySelector('p');
    const interestsList = document.getElementById('interests')?.querySelector('ul');

    const photoSlider = document.getElementById('photoSlider');
    const currentPhoto = document.getElementById('currentPhoto');
    const photoIndicator = document.getElementById('photoIndicator');
    const prevBtn = document.getElementById('prevPhoto');
    const nextBtn = document.getElementById('nextPhoto');

    // === Переменные ===
    let photos = [];
    let currentPhotoIndex = 0;
    let interestLabels = {};
    const userId = window.userProfileId; // Убедимся, что имя совпадает

    if (!userId || isNaN(Number(userId))) {
        showError('Неверный ID пользователя');
        return;
    }

    // === Кнопка "Назад" ===
    bottomBackBtn?.addEventListener('click', () => {
        window.history.back();
    });

    // === Загрузка данных ===
    await loadInterestLabels();
    await loadUserProfile(userId);

    // === Функции ===

    async function loadInterestLabels() {
        try {
            const response = await fetch('/sparkle/users/interests/all', {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });
            if (response.ok) {
                interestLabels = await response.json();
            } else {
                fallbackToHardcodedLabels();
            }
        } catch (error) {
            console.error('❌ Ошибка при загрузке меток интересов:', error);
            fallbackToHardcodedLabels();
        }
    }

    function fallbackToHardcodedLabels() {
        interestLabels = {
            'FOOTBALL': 'Футбол', 'LITRBALL': 'Пьянство', 'BASKETBALL': 'Баскетбол',
            'TENNIS': 'Теннис', 'SWIMMING': 'Плавание', 'GYM': 'Фитнес и спортзал',
            'PAINTING': 'Рисование', 'MUSIC': 'Музыка', 'DANCE': 'Танцы',
            'WRITING': 'Писательство', 'COOKING': 'Кулинария', 'PHOTOGRAPHY': 'Фотография',
            'READING': 'Чтение', 'TRAVEL': 'Путешествия', 'PROGRAMMING': 'Программирование',
            'LANGUAGES': 'Изучение языков', 'SCIENCE': 'Наука и технологии',
            'BUSINESS': 'Бизнес и предпринимательство', 'MOVIES': 'Кино',
            'GAMING': 'Видеоигры', 'SOCIAL_MEDIA': 'Социальные сети', 'OTHER': 'Другое'
        };
    }

    async function loadUserProfile(id) {
        try {
            const response = await fetch(`/sparkle/users/${id}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response.status === 404) {
                showError('Пользователь не найден');
                return;
            }

            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

            const user = await response.json();
            renderProfile(user);
        } catch (error) {
            console.error('❌ Ошибка загрузки профиля:', error);
            showError('Не удалось загрузить профиль');
        }
    }

    function renderProfile(user) {
        if (usernameEl) usernameEl.textContent = user.username || 'Аноним';
        if (ageEl) ageEl.textContent = calculateAge(user.birthDate) || '—';
        if (genderEl) genderEl.textContent = formatGender(user.gender) || '—';
        if (cityEl) cityEl.textContent = user.city?.cityName || '—';

        if (aboutMeText && user.aboutMe?.trim()) {
            aboutMeText.textContent = user.aboutMe;
            aboutMeText.style.color = 'var(--text-dark)';
            aboutMeText.style.fontStyle = 'normal';
        }

        if (interestsList) {
            interestsList.innerHTML = '';
            if (Array.isArray(user.interests) && user.interests.length > 0) {
                user.interests.forEach(interest => {
                    const li = document.createElement('li');
                    li.textContent = interestLabels[interest] || interest;
                    interestsList.appendChild(li);
                });
            } else {
                const li = document.createElement('li');
                li.textContent = 'Интересы не указаны';
                li.style.color = 'var(--gray-400)';
                interestsList.appendChild(li);
            }
        }

        photos = Array.isArray(user.photos) ? user.photos : [];
        if (photos.length === 0) {
            if (currentPhoto) {
                currentPhoto.style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+фото)';
            }
        } else {
            updatePhoto();
            updatePhotoIndicator();
        }

        // === Кнопка "Написать" ===
        if (sendMessageBtn) {
            sendMessageBtn.onclick = async () => {
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                try {
                    const response = await fetch(`/sparkle/chats/${user.userId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-XSRF-TOKEN': csrfToken
                        },
                        credentials: 'include'
                    });

                    if (!response.ok) {
                        if (response.status === 403) alert('Ошибка доступа.');
                        else if (response.status === 404) alert('Пользователь не существует.');
                        else alert('Не удалось начать чат.');
                        return;
                    }

                    const chatData = await response.json();
                    // Переход на отдельную страницу чата
                    window.location.href = `/chat/${chatData.chatId}`;
                } catch (error) {
                    console.error('❌ Ошибка сети:', error);
                    alert('Не удалось подключиться к серверу');
                }
            };
        }
    }

    // === Фото слайдер ===
    function updatePhoto() {
        if (!currentPhoto || photos.length === 0) return;
        const photoUrl = photos[currentPhotoIndex]?.url || 'https://placehold.co/400x400/CCCCCC/FFFFFF?text=Ошибка';
        currentPhoto.style.backgroundImage = `url('${photoUrl}')`;
    }

    function updatePhotoIndicator() {
        if (!photoIndicator) return;
        photoIndicator.innerHTML = '';
        photos.forEach((_, i) => {
            const dot = document.createElement('div');
            dot.className = i === currentPhotoIndex ? 'photo-dot active' : 'photo-dot';
            photoIndicator.appendChild(dot);
        });
    }

    prevBtn?.addEventListener('click', () => {
        if (photos.length <= 1) return;
        currentPhotoIndex = (currentPhotoIndex - 1 + photos.length) % photos.length;
        updatePhoto();
        updatePhotoIndicator();
    });

    nextBtn?.addEventListener('click', () => {
        if (photos.length <= 1) return;
        currentPhotoIndex = (currentPhotoIndex + 1) % photos.length;
        updatePhoto();
        updatePhotoIndicator();
    });

    // === Вспомогательные функции ===
    function calculateAge(birthDate) {
        if (!birthDate) return null;
        const dob = new Date(birthDate);
        const ageDiff = Date.now() - dob.getTime();
        const ageDate = new Date(ageDiff);
        return Math.abs(ageDate.getUTCFullYear() - 1970);
    }

    function formatGender(gender) {
        if (!gender) return null;
        return { 'MALE': 'Мужской', 'FEMALE': 'Женский', 'WOMEN': 'Женский' }[gender] || gender;
    }

    function showError(message) {
        const main = document.querySelector('.main-content');
        if (main) {
            main.innerHTML = `
                <div class="error" style="text-align:center; padding:2rem; color:#b91c1c;">
                    <p>${message}</p>
                    <button onclick="window.history.back()" class="action-btn" style="margin-top:1rem;">Назад</button>
                </div>
            `;
        }
    }
});