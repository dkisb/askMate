import { useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import TopicsList from './TopicsList.jsx';
import PostList from './PostList.jsx';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import SpeedDial from '@mui/material/SpeedDial';
import SpeedDialIcon from '@mui/material/SpeedDialIcon';
import SpeedDialAction from '@mui/material/SpeedDialAction';
import AddIcon from '@mui/icons-material/Add';
import { fetchAllQuestions, createQuestion, addPoints, likeQuestion, dislikeQuestion, fetchQuestionLikesCount, fetchQuestionDislikesCount, fetchComments } from '../utils/api.js';

export default function HomePage() {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [questionTitle, setQuestionTitle] = useState('');
  const [questionContent, setQuestionContent] = useState('');
  const [showForm, setShowForm] = useState(false);
  const contentRef = useRef(null);
  const [likesByQuestionId, setLikesByQuestionId] = useState({});
  const [dislikesByQuestionId, setDislikesByQuestionId] = useState({});
  const [commentsCountByQuestionId, setCommentsCountByQuestionId] = useState({});
  const location = useLocation();
  const fromState = location.state || {};
  const [currentUserName, setCurrentUserName] = useState(fromState.userName ?? null);
  const [currentUserId, setCurrentUserId] = useState(fromState.userId ?? null);
  const [userReviews, setUserReviews] = useState({});
  const API_URL = import.meta.env.VITE_API_URL;

  const fetchData = async () => {
    setLoading(true);
    try {
      const data = await fetchAllQuestions();
      const list = Array.isArray(data) ? data : [];
      setQuestions(list);
      // Fetch like counts for all questions
      const entries = await Promise.all(
        list.map(async (q) => {
          try {
            const likesCount = await fetchQuestionLikesCount(q.id);
            const dislikesCount = await fetchQuestionDislikesCount(q.id);
            const likeResponse = await fetch(`/api/question/like/user/${q.id}`, { headers: { 'Content-Type': 'application/json', ...(localStorage.getItem('jwtToken'  ) ? { Authorization: 'Bearer ' + localStorage.getItem('jwtToken') } : {}) } });
            const dislikeResponse = await fetch(`/api/question/dislike/user/${q.id}`, { headers: { 'Content-Type': 'application/json', ...(localStorage.getItem('jwtToken'  ) ? { Authorization: 'Bearer ' + localStorage.getItem('jwtToken') } : {}) } });
            if (likeResponse.ok) {
              const data = await likeResponse.json();
              if (data === true) {
                setUserReviews((prev) => ({ ...prev, [q.id]: {'like': true, 'dislike': false} }));
              } else if (data === false) {
                setUserReviews((prev) => ({ ...prev, [q.id]: {'like': false, 'dislike': false} }));
              }
            }
            if (dislikeResponse.ok) {
              const data = await dislikeResponse.json();
              if (data === true) {
                setUserReviews((prev) => ({ ...prev, [q.id]: {...(prev[q.id] || {}), 'dislike': true} }));
              } else if (data === false) {
                setUserReviews((prev) => ({ ...prev, [q.id]: {...(prev[q.id] || {}), 'dislike': false} }));
              }
            }
            return [q.id, Number(likesCount) || 0, Number(dislikesCount) || 0];
          } catch {
            setUserReviews((prev) => ({ ...prev, [q.id]: {'like': false} }));
            return [q.id, 0, 0];
          }
        })
      );
      const likesMap = {};
      const dislikesMap = {};
      entries.forEach(([id, likes, dislikes]) => {
        likesMap[id] = likes;
        dislikesMap[id] = dislikes;
      });
      setLikesByQuestionId(likesMap);
      setDislikesByQuestionId(dislikesMap);

      // Fetch comments count for all questions
      const commentEntries = await Promise.all(
        list.map(async (q) => {
          try {
            const answers = await fetchComments(q.id);
            return [q.id, Array.isArray(answers) ? answers.length : 0];
          } catch {
            return [q.id, 0];
          }
        })
      );
      setCommentsCountByQuestionId(Object.fromEntries(commentEntries));
    } catch (error) {
      console.error('Error fetching questions:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);
/*
  useEffect(() => {
    try {
      localStorage.setItem('askmate_home_q_react', JSON.stringify(userReactions));
    } catch {
      // ignore
    }
  }, [userReactions]);

  */
  // Auto-resize the question content textarea based on input length
  useEffect(() => {
    if (contentRef.current) {
      const el = contentRef.current;
      el.style.height = 'auto';
      el.style.height = el.scrollHeight + 'px';
    }
  }, [questionContent, showForm]);

  // Ensure user from JWT if not provided via navigation state
  useEffect(() => {
    if (currentUserId) return;
    const token = (() => { try { return localStorage.getItem('jwtToken'); } catch { return null; } })();
    if (!token) return;
    (async () => {
      try {
        const meRes = await fetch(`${API_URL}/api/user/me`, { headers: { Authorization: 'Bearer ' + token } });
        if (!meRes.ok) return;
        const me = await meRes.json();
        setCurrentUserName(me.userName ?? me.username ?? null);
        setCurrentUserId(me.userId ?? me.userid ?? me.id ?? null);
      } catch {
        // ignore
      }
    })();
  }, [currentUserId, API_URL]);

  async function awardQuestionPoints() {
    try {
      if (!currentUserId) return;
      await addPoints(currentUserId, 5);
    } catch (error) {
      console.error('Error adding points:', error);
    }
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      if (!currentUserId) {
        alert('You are not logged in. Please log in again.');
        return;
      }
      await createQuestion(questionTitle, questionContent, currentUserId);
      setQuestionTitle('');
      setQuestionContent('');
      fetchData();
      awardQuestionPoints();
      setShowForm(false);
    } catch (error) {
      console.error('Error creating question:', error);
      alert('Failed to create question. Please try again.');
    }
  };

  const query = new URLSearchParams(location.search).get('q') || '';
  const filteredQuestions = query
    ? questions.filter((q) => (q.title || '').toLowerCase().includes(query.toLowerCase()))
    : questions;
  const sortedQuestions = [...filteredQuestions].sort(
    (a, b) => new Date(b.created || b.createdAt) - new Date(a.created || a.createdAt)
  );

  async function handleLikeClick(questionId) {
    const data = await likeQuestion(questionId);
    setUserReviews((prev) => ({ ...prev, [questionId]: {'like': data} }));
    try {
      const refreshed = await fetchQuestionLikesCount(questionId);
      setLikesByQuestionId((prev) => ({ ...prev, [questionId]: Number(refreshed) || 0 }));
    } catch {
      // ignore
    }
  }

  async function handleDislikeClick(questionId) {
    const data = await dislikeQuestion(questionId);
    setUserReviews((prev) => ({ ...prev, [questionId]: {'dislike': data} }));
    try {
      const refreshed = await fetchQuestionDislikesCount(questionId);
      setDislikesByQuestionId((prev) => ({ ...prev, [questionId]: Number(refreshed) || 0 }));
    } catch {
      // ignore
    }
  }

  async function handleShareQuestion(questionId) {
    const url = `${window.location.origin}/question/${questionId}`;
    const shareData = { title: 'AskMate Question', text: 'Check out this question on AskMate', url };
    try {
      if (navigator.share) {
        await navigator.share(shareData);
      } else if (navigator.clipboard) {
        await navigator.clipboard.writeText(url);
        alert('Link copied to clipboard');
      } else {
        // Fallback
        const textarea = document.createElement('textarea');
        textarea.value = url;
        document.body.appendChild(textarea);
        textarea.select();
        document.copy();
        document.body.removeChild(textarea);
        alert('Link copied to clipboard');
      }
    } catch (err) {
      console.error('Share failed:', err);
    }
  }

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    setShowForm(params.get('new') === '1');
  }, [location.search]);

  const closeForm = () => {
    const params = new URLSearchParams(location.search);
    if (params.has('new')) {
      params.delete('new');
      const next = `${location.pathname}${params.toString() ? `?${params.toString()}` : ''}`;
      window.history.replaceState(null, '', next);
    }
    setShowForm(false);
  };

  return (
    <div className="container mx-auto p-4">
      <Dialog open={showForm} onClose={closeForm} fullWidth maxWidth="sm">
        <DialogTitle>Post a new question</DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent dividers>
            <div className="mb-4">
              <label htmlFor="question-title" className="block text-gray-700 text-sm font-bold mb-2">
                Question Title:
              </label>
              <input
                type="text"
                id="question-title"
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                placeholder="Enter question title"
                value={questionTitle}
                onChange={(e) => setQuestionTitle(e.target.value)}
                required
              />
            </div>
            <div className="mb-4">
              <label htmlFor="question-content" className="block text-gray-700 text-sm font-bold mb-2">
                Question Content:
              </label>
              <textarea
                id="question-content"
                rows="3"
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                placeholder="Enter question content"
                value={questionContent}
                onChange={(e) => setQuestionContent(e.target.value)}
                ref={contentRef}
                style={{ overflow: 'hidden' }}
                required
              ></textarea>
            </div>
          </DialogContent>
          <DialogActions>
            <Button onClick={closeForm}>Cancel</Button>
            <Button type="submit" variant="contained">Post</Button>
          </DialogActions>
        </form>
      </Dialog>

      <Box sx={{ display: { xs: 'block', md: 'flex' }, gap: 2 }}>
        <Box sx={{ flex: { xs: '1 1 100%', md: '1 1 0' }, mt: 11 }}>
          <PostList
            loading={loading}
            questions={sortedQuestions}
            likesByQuestionId={likesByQuestionId}
            dislikesByQuestionId={dislikesByQuestionId}
            commentsCountByQuestionId={commentsCountByQuestionId}
            onLikeClick={handleLikeClick}
            onDislikeClick={handleDislikeClick}
            userReviews={userReviews}
            onShareQuestion={handleShareQuestion}
            currentUserName={currentUserName}
            currentUserId={currentUserId}
          />
        </Box>
        <Box
          sx={{
            flex: { xs: '1 1 100%', md: '0 0 360px' },
            width: { md: 360 },
            display: { xs: 'block', md: 'block' },
            position: { xs: 'static', md: 'sticky' },
            top: { md: 0 },
            pl: { md: 6 }
          }}
        >
          <TopicsList topPostTitle={sortedQuestions[0]?.title || ''} />
        </Box>
      </Box>

      <Box sx={{ position: 'fixed', bottom: 16, left: 16 }}>
        <SpeedDial ariaLabel="actions" icon={<SpeedDialIcon />} direction="up">
          <SpeedDialAction
            key="new"
            icon={<AddIcon />}
            title="New Post"
            onClick={() => setShowForm(true)}
          />
        </SpeedDial>
      </Box>
    </div>
  );
}
