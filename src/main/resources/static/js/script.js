document.addEventListener('DOMContentLoaded', () => {

    // Получаем CSRF‑токен
    const csrfMeta = document.querySelector('meta[name="csrf-token"]');
    if (!csrfMeta) {
        console.error('❌ Метатег [name="csrf-token"] не найден');
        return;
    }
    const csrfToken = csrfMeta.getAttribute('content');
    console.log('✅ CSRF Token найден:', csrfToken);
    let startX = 0;
    // Проверка элементов
    const likeBtn = document.getElementById('likeBtn');
    const dislikeBtn = document.getElementById('dislikeBtn');
    const profileViewer = document.getElementById('profileViewer');

    if (!likeBtn) console.error('❌ #likeBtn не найден');
    if (!dislikeBtn) console.error('❌ #dislikeBtn не найден');
    if (!profileViewer) console.error('❌ #profileViewer не найден');

    // Глобальные переменные
    let users = [];
    let currentUserIndex = 0;
    let currentPhotoIndex = 0;
    let isAnimating = false;

    // === Отправка лайка ===
    async function sendLike(userId) {
        if (!userId || isAnimating) {
            console.warn('⚠️ Невозможно отправить лайк', { userId, isAnimating });
            return;
        }
        try {
            const response = await fetch(`/sparkle/users/match/like/${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (!response.ok) {
                const text = await response.text();
                console.warn('❌ Лайк не удался:', text);
                return;
            }

            console.log('✅ Лайк отправлен:', userId);
        } catch (error) {
            console.error('❌ Ошибка лайка:', error);
        }
    }

    // === Отправка дизлайка ===
    async function sendDislike(userId) {
        if (!userId || isAnimating) {
            console.warn('⚠️ Невозможно отправить дизлайк', { userId, isAnimating });
            return;
        }
        try {
            const response = await fetch(`/sparkle/users/match/dislike/${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (!response.ok) {
                const text = await response.text();
                console.warn('❌ Дизлайк не удался:', text);
                return;
            }
            console.log('✅ Дизлайк отправлен:', userId);
        } catch (error) {
            console.error('❌ Ошибка дизлайка:', error);
        }
    }

    // === Загрузка следующего кандидата ===
    // === Загрузка следующего кандидата ===
    async function loadNextCandidate() {
        if (isAnimating) {
            console.warn('⚠️ Анимация уже идёт, выход');
            return;
        }

        isAnimating = true;
        console.log('🔄 Загрузка кандидата...');

        try {
            showLoader();
            updateUIState(true);

            const response = await fetch('/sparkle/users/match/next-candidate', {
                method: 'GET',
                headers: {
                    'X-XSRF-TOKEN': csrfToken
                },
                credentials: 'include'
            });

            if (response.status === 204) {
                showNoCandidates();
                return;
            }

            if (!response.ok) throw new Error(`Ошибка: ${response.status}`);

            const candidate = await response.json();
            console.log('🔁 Кандидат получен:', candidate);

            // ✅ Исправлено: проверяем наличие userId, а не id
            if (candidate && candidate.userId) {
                users = [candidate];
                currentUserIndex = 0;
                currentPhotoIndex = 0;
                renderProfileForced();
            } else {
                console.warn('❌ Кандидат не содержит userId');
                showNoCandidates();
            }
        } catch (error) {
            console.error('❌ Ошибка загрузки:', error);
            showError();
        } finally {
            hideLoader();
            updateUIState(false);
            isAnimating = false;
        }
    }

    // === Рендер профиля ===
    function renderProfileForced() {
        console.log('🔥 Рендер профиля');

        if (users.length === 0 || currentUserIndex >= users.length) {
            console.warn('❌ Нет пользователя для отображения');
            return;
        }

        const user = users[currentUserIndex];

        const photoContainer = document.getElementById('currentPhoto');
        if (photoContainer) {
            let photoUrl = '/images/placeholder.jpg';

            if (user.photos && user.photos.length > 0) {
                const firstPhoto = user.photos[0];
                if (firstPhoto.url) {
                    photoUrl = firstPhoto.url;
                } else if (firstPhoto.photoUrl) {
                    photoUrl = firstPhoto.photoUrl;
                } else if (firstPhoto.path) {
                    photoUrl = `/uploads/${firstPhoto.path}`;
                }
            }

            photoContainer.style.backgroundImage = `url('${photoUrl}')`;
            photoContainer.style.display = 'block';
            // Обработка ошибки загрузки фото
            photoContainer.onload = () => console.log('Фото загружено:', photoUrl);
            photoContainer.onerror = () => {
                console.error('❌ Не удалось загрузить фото:', photoUrl);
                photoContainer.style.backgroundImage = 'url(/images/placeholder.jpg)';
            };
        }

        const infoContainer = document.getElementById('profileInfo');
        if (infoContainer) {
            const birthDate = user.birthDate ? new Date(user.birthDate) : null;
            const age = birthDate ? new Date().getFullYear() - birthDate.getFullYear() : 'Не указан';
            const cityName = user.city?.cityName || 'Не указан';
            const aboutMe = user.aboutMe || 'О себе не рассказано';

            infoContainer.innerHTML = `
                <h3>${user.username || 'Аноним'}, ${age}</h3>
                <p><strong>Город:</strong> ${cityName}</p>
                <p>${aboutMe}</p>
            `;
        }

        updateIndicators(user);
    }

    // === Утилиты ===
    function updateIndicators(user) {
        const profileIndicator = document.getElementById('indicator');
        if (profileIndicator) {
            profileIndicator.innerHTML = '<div class="dot active"></div>';
        }

        const photoIndicator = document.getElementById('photoIndicator');
        if (photoIndicator && user?.photos?.length > 1) {
            photoIndicator.innerHTML = '';
            user.photos.forEach((_, i) => {
                const dot = document.createElement('div');
                dot.className = i === currentPhotoIndex ? 'photo-dot active' : 'photo-dot';
                photoIndicator.appendChild(dot);
            });
        }
    }

    function showLoader() {
        const loader = document.createElement('div');
        loader.className = 'loader';
        loader.textContent = 'Загрузка...';
        loader.style.cssText = `
            position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
            background: rgba(255,255,255,0.9); padding: 20px; border-radius: 12px; z-index: 100;
            text-align: center; font-size: 16px; color: #333;
        `;
        const container = document.getElementById('profileViewer');
        if (container && !document.querySelector('.loader')) {
            container.appendChild(loader);
        }
    }

    function hideLoader() {
        const loader = document.querySelector('.loader');
        if (loader) loader.remove();
    }

    function showNoCandidates() {
        const currentPhoto = document.getElementById('currentPhoto');
        const profileInfo = document.getElementById('profileInfo');
        const controls = document.getElementById('controls');
        const indicator = document.getElementById('indicator');
        const photoIndicator = document.getElementById('photoIndicator');

        if (currentPhoto) {
            currentPhoto.style.backgroundImage = 'url(https://placehold.co/400x400/CCCCCC/FFFFFF?text=Нет+кандидатов)';
        }

        if (profileInfo) {
            profileInfo.innerHTML = '<p>Нет доступных кандидатов</p>';
        }

        [controls, indicator, photoIndicator].forEach(el => {
            if (el) el.style.display = 'none';
        });

        const retryBtn = document.createElement('button');
        retryBtn.className = 'action-btn retry';
        retryBtn.textContent = 'Попробовать снова';
        retryBtn.style.cssText = 'display: block; margin: 1rem auto;';

        retryBtn.addEventListener('click', async () => {
            try {
                showLoader();

                // Отправляем GET‑запрос на перезагрузку кандидатов
                const response = await fetch('/sparkle/users/match/reload', {
                    method: 'GET',
                    headers: {
                        'X-XSRF-TOKEN': csrfToken
                    },
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error(`Ошибка сервера: ${response.status}`);
                }

                // Восстанавливаем видимость элементов управления
                [controls, indicator, photoIndicator].forEach(el => {
                    if (el) el.style.display = '';
                });

                // Удаляем кнопку повтора
                retryBtn.remove();

                // Загружаем следующего кандидата
                await loadNextCandidate();

            } catch (err) {
                console.error('❌ Ошибка при попытке перезагрузки:', err);

                // Если произошла ошибка, оставляем сообщение об ошибке
                if (profileInfo) {
                    profileInfo.innerHTML = '<p>Ошибка загрузки. Попробуйте позже.</p>';
                }

                // Добавляем кнопку повторной попытки
                const errorRetryBtn = document.createElement('button');
                errorRetryBtn.className = 'action-btn retry';
                errorRetryBtn.textContent = 'Повторить';
                errorRetryBtn.style.cssText = 'display: block; margin: 1rem auto;';

                errorRetryBtn.addEventListener('click', async () => {
                    try {
                        showLoader();
                        await loadNextCandidate();
                    } catch (innerErr) {
                        console.error('❌ Повторная попытка загрузки не удалась:', innerErr);
                    } finally {
                        hideLoader();
                    }
                });

                if (profileInfo) profileInfo.appendChild(errorRetryBtn);
            } finally {
                hideLoader();
            }
        });

        if (profileInfo) profileInfo.appendChild(retryBtn);
    }

    function showError() {
        const profileInfo = document.getElementById('profileInfo');
        if (profileInfo) {
            profileInfo.innerHTML = '<p>Ошибка загрузки. Попробуйте позже.</p>';
        }

        const retryBtn = document.createElement('button');
        retryBtn.className = 'action-btn retry';
        retryBtn.textContent = 'Повторить';
        retryBtn.style.cssText = 'display: block; margin: 1rem auto;';

        retryBtn.onclick = async () => {
            try {
                showLoader();
                await loadNextCandidate();
            } catch (err) {
                console.error('❌ Ошибка повторной загрузки:', err);
            } finally {
                hideLoader();
            }
        };

        if (profileInfo) profileInfo.appendChild(retryBtn);
    }

    function updateUIState(loading) {
        ['controls', 'indicator', 'photoIndicator'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.style.opacity = loading ? '0.6' : '1';
        });
    }

// === Обработчики кнопок ===
    likeBtn?.addEventListener('click', e => {
        e.stopPropagation();

        if (users.length === 0 || currentUserIndex >= users.length) {
            console.warn('❌ Нет доступного пользователя для лайка');
            return;
        }

        const user = users[currentUserIndex];
        if (!user?.userId) {
            console.warn('❌ У пользователя нет userId');
            return;
        }

        console.log('❤️ Ставим лайк:', user.userId);
        sendLike(user.userId);
        setTimeout(loadNextCandidate, 0);
    });

    dislikeBtn?.addEventListener('click', e => {
        e.stopPropagation();

        if (users.length === 0 || currentUserIndex >= users.length) {
            console.warn('❌ Нет доступного пользователя для дизлайка');
            return;
        }

        const user = users[currentUserIndex];
        if (!user?.userId) {
            console.warn('❌ У пользователя нет userId');
            return;
        }

        console.log('👎 Ставим дизлайк:', user.userId);
        sendDislike(user.userId);
        setTimeout(loadNextCandidate, 0);
    });

    dislikeBtn?.addEventListener('click', e => {
        e.stopPropagation();

        // Проверяем наличие валидного пользователя
        if (users.length === 0 || currentUserIndex >= users.length) {
            console.warn('❌ Нет доступного пользователя для дизлайка');
            return;
        }

        const user = users[currentUserIndex];
        if (!user?.id) {
            console.warn('❌ У пользователя нет ID');
            return;
        }

        console.log('👎 Ставим дизлайк:',user.userId);
        sendDislike(user.userId);
        setTimeout(loadNextCandidate, 0);
    });

// === Свайпы ===
    profileViewer?.addEventListener('mouseup', e => {
        if (!startX) return;
        const diff = startX - e.clientX;

        if (Math.abs(diff) > 50) {
            if (users.length === 0 || currentUserIndex >= users.length) {
                console.warn('❌ Нет пользователя для свайпа');
                return;
            }

            const user = users[currentUserIndex];
            if (diff > 0 && user?.userId) sendLike(user.userId);
            setTimeout(loadNextCandidate, 0);
        }
        startX = 0;
    });

    profileViewer?.addEventListener('touchend', e => {
        if (!startX) return;
        const diff = startX - e.changedTouches[0].clientX;

        if (Math.abs(diff) > 50) {
            if (users.length === 0 || currentUserIndex >= users.length) {
                console.warn('❌ Нет пользователя для свайпа');
                return;
            }

            const user = users[currentUserIndex];
            if (diff > 0 && user?.userId) sendLike(user.userId);
            setTimeout(loadNextCandidate, 0);
        }
        startX = 0;
    });

    profileViewer?.addEventListener('touchstart', e => startX = e.touches[0].clientX);
    profileViewer?.addEventListener('touchend', e => {
        if (!startX) return;
        const diff = startX - e.changedTouches[0].clientX;

        if (Math.abs(diff) > 50) {
            // Проверяем данные перед отправкой
            if (users.length === 0 || currentUserIndex >= users.length) {
                console.warn('❌ Нет пользователя для свайпа');
                return;
            }

            const user = users[currentUserIndex];
            if (diff > 0 && user?.id) sendLike(user.userId);
            setTimeout(loadNextCandidate, 0);
        }
        startX = 0;
    });

// === Управление фото ===
    document.getElementById('nextPhoto')?.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (!user?.photos?.length || user.photos.length <= 1) return;

        currentPhotoIndex = (currentPhotoIndex + 1) % user.photos.length;
        renderProfileForced();
    });

    document.getElementById('prevPhoto')?.addEventListener('click', () => {
        const user = users[currentUserIndex];
        if (!user?.photos?.length || user.photos.length <= 1) return;

        currentPhotoIndex = (currentPhotoIndex - 1 + user.photos.length) % user.photos.length;
        renderProfileForced();
    });

// === Навигация ===
    document.getElementById('profileBtn')?.addEventListener('click', () => {
        window.location.href = '/main/profile';
    });

    document.getElementById('editProfileBtn')?.addEventListener('click', () => {
        window.location.href = '/main/settings/profile';
    });

    document.getElementById('chatsMatchesBtn')?.addEventListener('click', () => {
        window.location.href = '/chats-matches';
    });

    document.getElementById('logoutBtn')?.addEventListener('click', async () => {
        await fetch('/logout', {
            method: 'POST',
            headers: { 'X-XSRF-TOKEN': csrfToken },
            credentials: 'include'
        });
        window.location.href = '/';
    });

// Инициализация
    loadNextCandidate();
});
