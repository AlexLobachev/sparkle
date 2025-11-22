import React, { useState } from 'react';
import { TextField, Button, Typography } from '@mui/material';

const SignUpForm = ({ onSubmit }) => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = async e => {
        e.preventDefault();
        await onSubmit({ name, email, password });
    };

    return (
        <form onSubmit={handleSubmit}>
            <TextField label="Имя" value={name} onChange={(e) => setName(e.target.value)} fullWidth margin="normal"/>
            <TextField label="Email" value={email} onChange={(e) => setEmail(e.target.value)} fullWidth margin="normal"/>
            <TextField type="password" label="Пароль" value={password} onChange={(e) => setPassword(e.target.value)} fullWidth margin="normal"/>
            <Button variant="contained" color="primary" type="submit">Зарегистрироваться</Button>
        </form>
    );
};

export default SignUpForm;