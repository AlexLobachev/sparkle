import React, { useState, useEffect } from 'react';
import { Container, Grid, Paper } from '@mui/material';
import LoginForm from '../components/LoginForm';
import SwipeContainer from '../components/SwipeContainer';
import { fetchUsers } from '../utils/api';

const HomePage = () => {
    const [users, setUsers] = useState([]);

    useEffect(() => {
        const loadUsers = async () => {
            const fetchedUsers = await fetchUsers();
            setUsers(fetchedUsers);
        };
        loadUsers();
    }, []);

    return (
        <Container maxWidth="lg">
            <Paper elevation={3} style={{ padding: '2rem' }}>
                <Grid container justifyContent="space-between">
                    <Grid item md={6}>
                        <LoginForm />
                    </Grid>
                    <Grid item md={6}>
                        <SwipeContainer users={users} />
                    </Grid>
                </Grid>
            </Paper>
        </Container>
    );
};

export default HomePage;