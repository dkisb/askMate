import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Chip from '@mui/material/Chip';
import React from 'react';

const mockTopics = [
  { id: 1, name: 'JavaScript', upvotes: 1240, posts: 87 },
  { id: 2, name: 'React', upvotes: 1033, posts: 72 },
  { id: 3, name: 'Spring Boot', upvotes: 880, posts: 54 },
  { id: 4, name: 'Databases', upvotes: 735, posts: 49 },
  { id: 5, name: 'CSS', upvotes: 610, posts: 41 },
];

export default function TopicsList({ topPostTitle }) {
  return (
    <Card elevation={3} sx={{ width: '100%' }}>
      <CardContent sx={{ p: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 2 }}>
          Active Topics
        </Typography>
        {topPostTitle ? (
          <>
            <Typography variant="subtitle2" color="text.secondary">
              Top post of the week
            </Typography>
            <Typography variant="body1" sx={{ mb: 2 }}>
              {topPostTitle}
            </Typography>
          </>
        ) : null}
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Most upvoted topics and total upvotes
        </Typography>
        <List disablePadding>
          {mockTopics.map((t, idx) => (
            <React.Fragment key={t.id}>
              <ListItem sx={{ py: 2 }} secondaryAction={<Chip size="small" label={`${t.upvotes} upvotes`} /> }>
                <ListItemText
                  primaryTypographyProps={{ variant: 'subtitle1' }}
                  primary={t.name}
                  secondary={
                    <Typography variant="body2" color="text.secondary">{t.posts} posts</Typography>
                  }
                />
              </ListItem>
              {idx < mockTopics.length - 1 && <Divider component="li" />}
            </React.Fragment>
          ))}
        </List>
      </CardContent>
    </Card>
  );
}


