import React, { useState, useEffect } from 'react';
import { Container, List, ListItem, Typography, Divider, TextField, Button } from '@mui/material';
import { fetchChats, sendMessage } from '../utils/api';

const ChatPage = () => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');

    useEffect(() => {
        const loadChats = async () => {
            const chats = await fetchChats();
            setMessages(chats.messages);
        };
        loadChats();
    }, []);

    const handleSendMessage = async () => {
        await sendMessage(newMessage);
        setNewMessage('');
    };

    return (
        <Container maxWidth="sm">
            <Typography variant="h4" component="h1" gutterBottom>Сообщения</Typography>
            <List>
                {messages.map(message => (
                    <ListItem key={message.id}>{message.text}</ListItem>
                ))}
            </List>
            <TextField
                placeholder="Напишите сообщение..."
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                fullWidth
                margin="dense"
            />
            <Button variant="contained" color="primary" onClick={handleSendMessage}>Отправить</Button>
        </Container>
    );
};

export default ChatPage;