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
import { fetchAllQuestions, createQuestion, addPoints, likeQuestion, dislikeQuestion, fetchQuestionLikesCount, fetchComments } from '../utils/api.js';

export default function HomePage() {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [questionTitle, setQuestionTitle] = useState('');
  const [questionContent, setQuestionContent] = useState('');
  const [showForm, setShowForm] = useState(false);
  const contentRef = useRef(null);
  const [userReactions, setUserReactions] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('askmate_home_q_react') || '{}');
    } catch {
      return {};
    }
  });
  const [pending, setPending] = useState({});
  const [likesByQuestionId, setLikesByQuestionId] = useState({});
  const [commentsCountByQuestionId, setCommentsCountByQuestionId] = useState({});

  const location = useLocation();
  const fromState = location.state || {};
  const [currentUserName, setCurrentUserName] = useState(fromState.userName ?? null);
  const [currentUserId, setCurrentUserId] = useState(fromState.userId ?? null);

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
            const count = await fetchQuestionLikesCount(q.id);
            return [q.id, Number(count) || 0];
          } catch {
            return [q.id, 0];
          }
        })
      );
      const map = Object.fromEntries(entries);
      setLikesByQuestionId(map);

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

  useEffect(() => {
    try {
      localStorage.setItem('askmate_home_q_react', JSON.stringify(userReactions));
    } catch {
      // ignore
    }
  }, [userReactions]);

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
        const meRes = await fetch('/api/user/me', { headers: { Authorization: 'Bearer ' + token } });
        if (!meRes.ok) return;
        const me = await meRes.json();
        setCurrentUserName(me.userName ?? me.username ?? null);
        setCurrentUserId(me.userId ?? me.userid ?? me.id ?? null);
      } catch {
        // ignore
      }
    })();
  }, [currentUserId]);

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
    console.log('Title:', questionTitle);
    console.log('Content:', questionContent);
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

  async function toggleQuestionReaction(questionId, reaction) {
    const current = userReactions[questionId] || null;
    if (pending[questionId]) return;
    // First-time reaction: call backend once
    if (current === null) {
      try {
        setPending((prev) => ({ ...prev, [questionId]: true }));
        if (reaction === 'like') {
          await likeQuestion(questionId);
          setUserReactions((prev) => ({ ...prev, [questionId]: 'like' }));
        } else if (reaction === 'dislike') {
          await dislikeQuestion(questionId);
          setUserReactions((prev) => ({ ...prev, [questionId]: 'dislike' }));
        }
        try {
          const refreshed = await fetchQuestionLikesCount(questionId);
          setLikesByQuestionId((prev) => ({ ...prev, [questionId]: Number(refreshed) || 0 }));
        } catch {
          // ignore
        }
      } catch (err) {
        console.error('Failed to react to question:', err);
      } finally {
        setPending((prev) => {
          const copy = { ...prev };
          delete copy[questionId];
          return copy;
        });
      }
      return;
    }
    // Toggle or switch locally without backend increments
    if (current === reaction) {
      // Unreact
      setUserReactions((prev) => ({ ...prev, [questionId]: null }));
    } else {
      // Switch dislike<->like
      setUserReactions((prev) => ({ ...prev, [questionId]: reaction }));
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
        <Box sx={{ flex: '0 0 720px' }}>
          <PostList
            loading={loading}
            questions={sortedQuestions}
            likesByQuestionId={likesByQuestionId}
            commentsCountByQuestionId={commentsCountByQuestionId}
            userReactions={userReactions}
            pending={pending}
            onToggleReaction={toggleQuestionReaction}
            onShareQuestion={handleShareQuestion}
            currentUserName={currentUserName}
            currentUserId={currentUserId}
          />
        </Box>
        <Box sx={{ flex: '0 0 360px', alignSelf: 'normal', position: 'relative', top: 88, left: 120 }}>
          <TopicsList />
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
