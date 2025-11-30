document.addEventListener('DOMContentLoaded', () => {
    const setupForm = document.getElementById('profileForm');
    const messages = document.getElementById('messages');
    const profilePhotoInput = document.getElementById('profilePhoto');
    const currentPhoto = document.getElementById('currentPhoto');
    const uploadBtn = document.getElementById('uploadBtn');
    const deleteBtn = document.getElementById('deleteBtn');
    const addInterestBtn = document.getElementById('addInterestBtn');
    const interestSelect = document.getElementById('interestSelect');
    const interestList = document.getElementById('interestList');
    const logoutBtn = document.getElementById('logoutBtn');
    const cityInput = document.getElementById('city');
    // === Список интересов ===
    const INTERESTS = {
        "FOOTBALL": "Футбол",
        "BASKETBALL": "Баскетбол",
        "TENNIS": "Теннис",
        "SWIMMING": "Плавание",
        "GYM": "Фитнес и спортзал",
        "PAINTING": "Рисование",
        "MUSIC": "Музыка",
        "DANCE": "Танцы",
        "WRITING": "Писательство",
        "COOKING": "Кулинария",
        "PHOTOGRAPHY": "Фотография",
        "READING": "Чтение",
        "TRAVEL": "Путешествия",
        "PROGRAMMING": "Программирование",
        "LANGUAGES": "Изучение языков",
        "SCIENCE": "Наука и технологии",
        "BUSINESS": "Бизнес",
        "MOVIES": "Фильмы",
        "GAMING": "Видеоигры",
        "SOCIAL_MEDIA": "Соцсети",
        "OTHER": "Другое"
    };

    // === Элемент для подсказок (добавим вручную, если ещё нет) ===
    let suggestionsList = document.getElementById('city-suggestions');
    if (!suggestionsList) {
        suggestionsList = document.createElement('ul');
        suggestionsList.id = 'city-suggestions';
        suggestionsList.className = 'suggestions';
        cityInput.parentNode.insertBefore(suggestionsList, cityInput.nextSibling);
    }

    let selectedPhoto = null;
    const selectedInterests = new Set();
    const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
    let CITIES = []; // Будет: ["Москва", "Екатеринбург", ...]
    let filteredCities = [];
    let activeSuggestionIndex = -1;

// === 1. Загрузка городов с сервера ===
    async function loadCities() {
        try {
            const response = await fetch('/sparkle/city/get-all-cities');
            if (response.ok) {
                const data = await response.json(); // ← Получаем: [{ cityName: "Москва" }, ...]
                if (Array.isArray(data)) {
                    CITIES = data.map(item => item.cityName); // ← Извлекаем только имена
                } else {
                    console.error('Ответ — не массив', data);
                    CITIES = [];
                }
            } else {
                showMessage('Не удалось загрузить список городов', 'error');
            }
        } catch (err) {
            console.error('Ошибка загрузки городов:', err);
            showMessage('Ошибка подключения к серверу', 'error');
        }
    }

    // === 2. Фильтрация при вводе ===
    function filterCities(query) {
        if (query.length < 2) return [];
        return CITIES.filter(city =>
            city.toLowerCase().includes(query.toLowerCase())
        ).sort((a, b) => {
            const aStarts = a.toLowerCase().startsWith(query.toLowerCase());
            const bStarts = b.toLowerCase().startsWith(query.toLowerCase());
            if (aStarts && !bStarts) return -1;
            if (!aStarts && bStarts) return 1;
            return a.localeCompare(b);
        }).slice(0, 10);
    }

    // === 3. Показать подсказки ===
    function showSuggestions() {
        suggestionsList.innerHTML = '';
        if (filteredCities.length === 0) {
            suggestionsList.classList.remove('show');
            return;
        }
        filteredCities.forEach((city, index) => {
            const li = document.createElement('li');
            li.textContent = city;
            if (index === activeSuggestionIndex) li.classList.add('active');
            li.addEventListener('click', () => {
                cityInput.value = city;
                suggestionsList.classList.remove('show');
            });
            suggestionsList.appendChild(li);
        });
        suggestionsList.classList.add('show');
    }

    // === 4. Навигация стрелками ===
    function setActive(index) {
        if (index < 0) index = filteredCities.length - 1;
        if (index >= filteredCities.length) index = 0;
        activeSuggestionIndex = index;
        showSuggestions();
    }

    // === 5. Ввод текста ===
    cityInput.addEventListener('input', () => {
        const query = cityInput.value.trim();
        if (query === '') return;

        filteredCities = filterCities(query);
        activeSuggestionIndex = -1;
        showSuggestions();
    });

    // === 6. Клавиатура: стрелки, Enter, Escape ===
    cityInput.addEventListener('keydown', e => {
        if (!suggestionsList.classList.contains('show')) return;

        const items = suggestionsList.querySelectorAll('li');
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActive(activeSuggestionIndex + 1);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActive(activeSuggestionIndex - 1);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (activeSuggestionIndex >= 0 && items[activeSuggestionIndex]) {
                cityInput.value = items[activeSuggestionIndex].textContent;
                suggestionsList.classList.remove('show');
            }
        } else if (e.key === 'Escape') {
            suggestionsList.classList.remove('show');
        }
    });

    // === 7. Скрыть подсказки при клике вне ===
    document.addEventListener('click', (e) => {
        if (!cityInput.contains(e.target) && !suggestionsList.contains(e.target)) {
            suggestionsList.classList.remove('show');
        }
    });

    // === 8. Проверка при отправке формы ===
    setupForm.addEventListener('submit', async e => {
        e.preventDefault();

        // === Проверка пола ===
        const gender = document.querySelector('input[name="gender"]:checked');
        if (!gender) {
            showMessage('Выберите ваш пол', 'error');
            return;
        }

        const preferredGender = document.querySelector('input[name="preferredGender"]:checked');
        if (!preferredGender) {
            showMessage('Выберите предпочтительный пол', 'error');
            return;
        }

        // === Проверка города ===
        const cityValue = cityInput.value.trim();
        if (!CITIES.includes(cityValue)) {
            showMessage('Такой город пока отсутствует. Введите ближайший к вам город.', 'error');
            return;
        }

        // === Сбор данных ===
        const formData = new FormData(setupForm);
        const data = {};

        for (let [key, value] of formData.entries()) {
            if (key !== 'profilePhoto' && value.trim() !== '') {
                data[key] = value;
            }
        }

        data.gender = gender.value;
        data.preferredGender = preferredGender.value;
        data.aboutMe = document.getElementById('aboutMe')?.value.trim() || null;
        data.interests = Array.from(selectedInterests).map(interest => ({ interest }));

        const email = data.email?.trim();
        if (email && !validateEmail(email)) {
            showMessage('Некорректный email', 'error');
            return;
        }

        // === Отправка данных ===
        try {
            showMessage('Сохраняем...', 'info');
            const resProfile = await fetch('/sparkle/users/setup-profile', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(data),
                credentials: 'include'
            });

            if (!resProfile.ok) throw new Error('Ошибка сохранения профиля');

            if (selectedPhoto) {
                const fd = new FormData();
                fd.append('file', selectedPhoto);
                await fetch('/sparkle/users/photo/upload-photo', {
                    method: 'POST',
                    headers: { 'X-XSRF-TOKEN': csrfToken },
                    body: fd,
                    credentials: 'include'
                });
            }

            if (data.interests.length > 0) {
                await fetch('/sparkle/users/interests/create-all', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-XSRF-TOKEN': csrfToken
                    },
                    body: JSON.stringify(data.interests),
                    credentials: 'include'
                });
            }

            showMessage('Успешно!', 'success');
            setTimeout(() => window.location.href = '/main', 1500);
        } catch (err) {
            showMessage('Ошибка: ' + err.message, 'error');
        }
    });

    // === Остальные обработчики (фото, интересы, выход) ===
    uploadBtn?.addEventListener('click', () => profilePhotoInput.click());
    profilePhotoInput?.addEventListener('change', e => {
        const file = e.target.files[0];
        if (file) showPhotoPreview(file);
    });

    deleteBtn?.addEventListener('click', resetPhoto);

    addInterestBtn?.addEventListener('click', () => {
        const value = interestSelect.value;
        if (value && !selectedInterests.has(value)) {
            selectedInterests.add(value);
            renderInterestList();
            populateInterestSelect();
        }
    });

    logoutBtn?.addEventListener('click', () => {
        if (confirm('Выйти без сохранения?')) {
            fetch('/logout', {
                method: 'POST',
                headers: { 'X-XSRF-TOKEN': csrfToken },
                credentials: 'include'
            }).then(() => window.location.href = '/');
        }
    });

    // === Загрузка городов при старте ===
    loadCities();

    // === Инициализация остального ===
    populateInterestSelect();
    renderInterestList();
    resetPhoto();

    // === Валидация email ===
    function validateEmail(email) {
        if (!email) return true;
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    function populateInterestSelect() {
        interestSelect.innerHTML = '<option value="">Выбрать</option>';
        Object.entries(INTERESTS).forEach(([key, label]) => {
            if (!selectedInterests.has(key)) {
                const option = document.createElement('option');
                option.value = key;
                option.textContent = label;
                interestSelect.appendChild(option);
            }
        });
    }

    function renderInterestList() {
        interestList.innerHTML = '';
        if (selectedInterests.size === 0) {
            const span = document.createElement('span');
            span.textContent = 'Нет интересов';
            span.style.color = '#999';
            interestList.appendChild(span);
            return;
        }
        Array.from(selectedInterests).forEach(key => {
            const tag = document.createElement('div');
            tag.className = 'interest-tag';
            tag.textContent = INTERESTS[key];
            const remove = document.createElement('span');
            remove.className = 'remove-interest';
            remove.textContent = '×';
            remove.onclick = () => {
                selectedInterests.delete(key);
                renderInterestList();
                populateInterestSelect();
            };
            tag.appendChild(remove);
            interestList.appendChild(tag);
        });
    }

    function showPhotoPreview(file) {
        if (!file) return;
        const reader = new FileReader();
        reader.onload = e => {
            currentPhoto.style.backgroundImage = `url('${e.target.result}')`;
            selectedPhoto = file;
            deleteBtn.disabled = false;
        };
        reader.readAsDataURL(file);
    }

    function resetPhoto() {
        currentPhoto.style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+фото)';
        selectedPhoto = null;
        deleteBtn.disabled = true;
        if (profilePhotoInput) profilePhotoInput.value = '';
    }

    function showMessage(text, type) {
        if (!messages) return;
        messages.innerHTML = `<div class="alert alert-${type}">${text}</div>`;
        setTimeout(() => messages.innerHTML = '', 3000);
    }


});