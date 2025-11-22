// Получаем userId из data‑атрибута (см. ниже)
const userIdElement = document.getElementById('user-id');
window.currentUserId = userIdElement ? parseInt(userIdElement.value) : null;