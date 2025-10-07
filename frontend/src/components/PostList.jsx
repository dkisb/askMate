import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import { ArrowBigUp, ArrowBigDown, Share } from 'lucide-react';
import { Link } from 'react-router-dom';
import { formatRelativeTime } from '../utils/transformDate.jsx';

export default function PostList({
  loading,
  questions,
  likesByQuestionId,
  dislikesByQuestionId,
  commentsCountByQuestionId,
  onLikeClick,
  onDislikeClick,
  userReviews,
  onShareQuestion,
  currentUserName,
  currentUserId,
}) {
  if (loading) {
    return <p>Loading...</p>;
  }

  if (!questions || questions.length === 0) {
    return <p>No questions found</p>;
  }

  // Roughly show ~5 posts before scrolling; each card + margin is ~140px on average
  return (
    <Box sx={{ overflowY: 'auto', maxHeight: 700, pr: 1 }}>
      {questions.map((question) => {
        const commentsCount = commentsCountByQuestionId[question.id] ?? 0;
        const likeCount = likesByQuestionId[question.id] ?? 0;
        const dislikeCount = dislikesByQuestionId[question.id] ?? 0;
        return (
          <Box key={question.id} sx={{ mb: 2 }}>
            <Card variant="outlined">
              <Box sx={{ display: 'flex' }}>
                <Box sx={{ flex: 1, p: 2 }}>
                  <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
                    {question.title}
                  </Typography>
                  <Typography sx={{ color: 'text.secondary', mb: 1 }}>
                    Posted by {question.author} {formatRelativeTime(question.createdAt)}
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{
                      display: '-webkit-box',
                      WebkitLineClamp: 3,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden',
                    }}
                  >
                    {question.content}
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mt: 1.5 }}>
                    <IconButton aria-label="Share question" onClick={() => onShareQuestion(question.id)}>
                      <Share size={18} />
                    </IconButton>
                    <Box
                      component={Link}
                      to={`/question/${question.id}`}
                      state={{ userName: currentUserName, userId: currentUserId, questionUserId: question.userId}}
                      sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'inherit', textDecoration: 'none' }}
                    >
                      <Typography variant="caption">{commentsCount}</Typography>
                      <ChatBubbleOutlineIcon fontSize="small" />
                    </Box>
                  </Box>
                </Box>
                <Box sx={{ width: 72, borderLeft: '1px solid', borderColor: 'divider', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 0.5, p: 1 }}>
                  <IconButton
                    aria-label="Like question"
                    onClick={() => onLikeClick(question.id)}
                    sx={{ color: userReviews[question.id].like === true ? 'primary.main' : 'inherit' }}
                  >
                    <ArrowBigUp size={28} />
                  </IconButton>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>{likeCount}</Typography>
                  <IconButton
                    aria-label="Dislike question"
                    onClick={() => onDislikeClick(question.id)}
                    sx={{ color: userReviews[question.id].dislike === true ? 'error.main' : 'inherit' }}
                  >
                    <ArrowBigDown size={28} />
                  </IconButton>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>{dislikeCount}</Typography>
                </Box>
              </Box>
            </Card>
          </Box>
        );
      })}
    </Box>
  );
}


