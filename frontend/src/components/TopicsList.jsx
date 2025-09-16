import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Chip from '@mui/material/Chip';

const mockTopics = [
  { id: 1, name: 'JavaScript', upvotes: 1240, posts: 87 },
  { id: 2, name: 'React', upvotes: 1033, posts: 72 },
  { id: 3, name: 'Spring Boot', upvotes: 880, posts: 54 },
  { id: 4, name: 'Databases', upvotes: 735, posts: 49 },
  { id: 5, name: 'CSS', upvotes: 610, posts: 41 },
];

export default function TopicsList() {
  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
          Active Topics
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Most upvoted posts and total upvotes
        </Typography>
        <List dense disablePadding>
          {mockTopics.map((t, idx) => (
            <>
              <ListItem key={t.id} sx={{ py: 1 }} secondaryAction={<Chip size="small" label={`${t.upvotes} upvotes`} /> }>
                <ListItemText
                  primary={t.name}
                  secondary={
                    <Typography variant="caption" color="text.secondary">{t.posts} posts</Typography>
                  }
                />
              </ListItem>
              {idx < mockTopics.length - 1 && <Divider component="li" />}
            </>
          ))}
        </List>
      </CardContent>
    </Card>
  );
}


