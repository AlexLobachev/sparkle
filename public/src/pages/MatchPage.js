import React, { useState, useEffect } from 'react';
import { Container, Grid, List, ListItem, Divider, Typography } from '@mui/material';
import { fetchMatches } from '../utils/api';

const MatchPage = () => {
    const [matches, setMatches] = useState([]);

    useEffect(() => {
        const loadMatches = async () => {
            const fetchedMatches = await fetchMatches();
            setMatches(fetchedMatches);
        };
        loadMatches();
    }, []);

    return (
        <Container maxWidth="md">
            <Typography variant="h4" component="h1" gutterBottom>Совпадения</Typography>
            <List>
                {matches.map(match => (
                    <>
                        <ListItem button key={match.id}>
                            <Typography variant="subtitle1">{match.name}, {match.age}</Typography>
                        </ListItem>
                        <Divider/>
                    </>
                ))}
            </List>
        </Container>
    );
};

export default MatchPage;