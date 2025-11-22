import React from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Avatar from '@mui/material/Avatar';
import Typography from '@mui/material/Typography';

const ProfileCard = ({ user }) => {
    return (
        <Card elevation={8}>
            <div style={{ display: 'flex', alignItems: 'center' }}>
                <Avatar alt={user.name} src={user.avatarUrl} sx={{ width: 100, height: 100 }} />
                <CardContent>
                    <Typography gutterBottom variant="h5">
                        {user.name}, {user.age}
                    </Typography>
                    <Typography>{user.bio}</Typography>
                </CardContent>
            </div>
        </Card>
    );
};

export default ProfileCard;