import axios from 'axios';

// Конфигурация сервера
const baseURL = process.env.REACT_APP_API_URL || '/api';

export const login = async credentials => {
    try {
        const response = await axios.post(`${baseURL}/auth/login`, credentials);
        return response.data.token;
    } catch (err) {
        throw new Error(err.response?.data.message || err.message);
    }
};

export const register = async data => {
    try {
        const response = await axios.post(`${baseURL}/auth/register`, data);
        return response.data.userId;
    } catch (err) {
        throw new Error(err.response?.data.message || err.message);
    }
};

export const fetchUsers = async () => {
    try {
        const response = await axios.get(`${baseURL}/users`);
        return response.data.users;
    } catch (err) {
        console.error("Ошибка загрузки пользователей:", err);
    }
};

export const fetchMatches = async () => {
    try {
        const response = await axios.get(`${baseURL}/matches`);
        return response.data.matches;
    } catch (err) {
        console.error("Ошибка загрузки совпадений:", err);
    }
};

export const fetchChats = async () => {
    try {
        const response = await axios.get(`${baseURL}/chats`);
        return response.data.chats;
    } catch (err) {
        console.error("Ошибка загрузки сообщений:", err);
    }
};

export const sendMessage = async message => {
    try {
        await axios.post(`${baseURL}/send-message`, { message });
    } catch (err) {
        console.error("Ошибка отправки сообщения:", err);
    }
};