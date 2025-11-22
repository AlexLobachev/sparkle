import React, { useState } from 'react';
import { TextField, Button, Typography } from '@mui/material';

const LoginForm = ({ onSubmit }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = async e => {
        e.preventDefault();
        await onSubmit({ email, password });
    };

    return (
        <form onSubmit={handleSubmit}>
            <TextField label="Email" value={email} onChange={(e) => setEmail(e.target.value)} fullWidth margin="normal"/>
            <TextField type="password" label="Пароль" value={password} onChange={(e) => setPassword(e.target.value)} fullWidth margin="normal"/>
            <Button variant="contained" color="primary" type="submit">Войти</Button>
        </form>
    );
};

export default LoginForm;