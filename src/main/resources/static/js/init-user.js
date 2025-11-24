// Получаем userId из скрытого input-поля
const userIdElement = document.getElementById('user-id');
const userIdValue = userIdElement ? userIdElement.value : null;

// Проверяем, что значение существует и не "null"
if (userIdValue && userIdValue !== 'null') {
    window.currentUserId = parseInt(userIdValue, 10);
} else {
    window.currentUserId = null;
    console.warn('⚠️ ID пользователя не найден или равен null');
}