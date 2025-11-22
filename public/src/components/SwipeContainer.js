import React, { useEffect, useRef, useState } from 'react';
import { Box, Grid } from '@mui/material';
import ProfileCard from './ProfileCard';

const SwipeContainer = ({ users }) => {
    const swipeAreaRef = useRef(null);
    const [currentUserIndex, setCurrentUserIndex] = useState(0);

    // Обработчик события свайпа
    const handleTouchMove = event => {
        if (!swipeAreaRef.current || currentUserIndex >= users.length) return;

        const touch = event.touches ? event.touches[0] : event.changedTouches[0];
        const deltaX = touch.clientX - swipeAreaRef.current.offsetLeft;

        if (deltaX > 100) nextUser(); // Свайп вправо
        else if (deltaX < -100) previousUser(); // Свайп влево
    };

    const nextUser = () => {
        setCurrentUserIndex(currentUserIndex + 1);
    };

    const previousUser = () => {
        setCurrentUserIndex(Math.max(0, currentUserIndex - 1));
    };

    useEffect(() => {
        window.addEventListener('touchmove', handleTouchMove);
        return () => window.removeEventListener('touchmove', handleTouchMove);
    }, []);

    return (
        <Box ref={swipeAreaRef} style={{ position: 'relative', overflow: 'hidden' }}>
            <Grid container spacing={2}>
                {users.map((user, i) =>
                    <Grid item xs={12} key={i}>
                        <ProfileCard user={user} visible={i === currentUserIndex}/>
                    </Grid>
                )}
            </Grid>
        </Box>
    );
};

export default SwipeContainer;