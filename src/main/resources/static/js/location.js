

// Элементы
const locationModal = document.getElementById('locationModal');
const getLocationBtn = document.getElementById('getLocationBtn');
const skipLocationBtn = document.getElementById('skipLocationBtn');
const manualInput = document.getElementById('manualInput');
const cityInput = document.getElementById('cityInput');
const confirmCityBtn = document.getElementById('confirmCityBtn');

// Поле "Город" из основной формы
const cityField = document.getElementById('city'); // ← это твоё поле ввода
const form = document.getElementById('profileForm');

// Показать окно автоматически при загрузке
window.addEventListener('DOMContentLoaded', () => {
    if (locationModal && !cityField.value) {
        setTimeout(() => {
            locationModal.style.display = 'flex';
        }, 1000); // Показать через 1 секунду
    }
});

// Пропустить — скрыть модалку
skipLocationBtn?.addEventListener('click', () => {
    locationModal.style.display = 'none';
});

// Определить местоположение
getLocationBtn?.addEventListener('click', () => {
    if (!navigator.geolocation) {
        showManualInput("Геолокация не поддерживается.");
        return;
    }

    navigator.geolocation.getCurrentPosition(
        (position) => {
            const { latitude, longitude } = position.coords;
            if (position.coords.accuracy > 1000) {
                showManualInput("Точность низкая. Введите город вручную.");
                return;
            }

            fetchCityByCoordinates(latitude, longitude)
                .then(cityName => {
                    if (cityName) {
                        cityField.value = cityName;
                        alert(`Авто-заполнено: ${cityName}`);
                        locationModal.style.display = 'none';
                    } else {
                        showManualInput("Не удалось определить город.");
                    }
                });
        },
        (error) => {
            if (error.code === error.PERMISSION_DENIED) {
                showManualInput("Доступ к местоположению запрещён.");
            } else {
                showManualInput(`Ошибка: ${error.message}`);
            }
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
});

// Показать ручной ввод
function showManualInput(message) {
    const msgEl = document.querySelector('#manualInput .alert');
    if (!msgEl && message) {
        const alert = document.createElement('div');
        alert.className = 'alert alert-info';
        alert.textContent = message;
        manualInput.insertAdjacentElement('afterbegin', alert);
    }
    manualInput.style.display = 'block';
}

// Подтвердить ввод вручную
confirmCityBtn?.addEventListener('click', () => {
    if (cityInput.value.trim()) {
        cityField.value = cityInput.value.trim();
        locationModal.style.display = 'none';
    } else {
        alert('Введите город');
    }
});

// Обратное геокодирование
async function fetchCityByCoordinates(lat, lng) {
    try {
        const response = await fetch('/sparkle/city/location', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': document.querySelector('meta[name="csrf-token"]')?.getAttribute('content')
            },
            credentials: 'include',
            body: JSON.stringify({ latitude: lat, longitude: lng, accuracy: 10 })
        });

        if (!response.ok) throw new Error('Не удалось определить город');

        const data = await response.json();
        return data.cityName;
    } catch (error) {
        console.error('Ошибка:', error);
        return null;
    }
}