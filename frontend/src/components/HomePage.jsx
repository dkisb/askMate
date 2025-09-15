import { useState, useEffect } from 'react';
import { ThumbsUp, ThumbsDown, Share } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { fetchAllQuestions, createQuestion, addPoints, likeQuestion, dislikeQuestion, fetchQuestionLikesCount } from '../utils/api.js';
import { formatRelativeTime, formatExactTime } from '../utils/transformDate.jsx';

export default function HomePage() {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [questionTitle, setQuestionTitle] = useState('');
  const [questionContent, setQuestionContent] = useState('');
  const [userReactions, setUserReactions] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('askmate_home_q_react') || '{}');
    } catch {
      return {};
    }
  });
  const [pending, setPending] = useState({});
  const [likesByQuestionId, setLikesByQuestionId] = useState({});

  const location = useLocation();
  const fromState = location.state || {};
  const authUser = []
  const userName = fromState.userName || (authUser && authUser.userName);
  const userId = fromState.userId || (authUser && authUser.userId);

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
    } catch (error) {
      console.error('Error fetching questions:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Lightweight polling to keep counts in sync with backend
  useEffect(() => {
    const intervalId = setInterval(() => {
      fetchData();
    }, 5000);
    return () => clearInterval(intervalId);
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem('askmate_home_q_react', JSON.stringify(userReactions));
    } catch {
      // ignore
    }
  }, [userReactions]);

  async function awardQuestionPoints() {
    try {
      await addPoints(userId, 5);
    } catch (error) {
      console.error('Error adding points:', error);
    }
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    console.log('Title:', questionTitle);
    console.log('Content:', questionContent);
    try {
      await createQuestion(questionTitle, questionContent, userId);
      setQuestionTitle('');
      setQuestionContent('');
      fetchData();
      awardQuestionPoints();
    } catch (error) {
      console.error('Error creating question:', error);
    }
  };

  const sortedQuestions = [...questions].sort(
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
        await fetchData();
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
        document.execCommand('copy');
        document.body.removeChild(textarea);
        alert('Link copied to clipboard');
      }
    } catch (err) {
      console.error('Share failed:', err);
    }
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Questions</h1>

      <form className="mb-6" onSubmit={handleSubmit}>
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
            rows="6"
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            placeholder="Enter question content"
            value={questionContent}
            onChange={(e) => setQuestionContent(e.target.value)}
            required
          ></textarea>
        </div>
        <button
          type="submit"
          className="inline-flex items-center py-2.5 px-4 text-xs font-medium text-white bg-blue-700 rounded-lg hover:bg-blue-800"
        >
          Ask Question
        </button>
      </form>

      {loading ? (
        <p>Loading...</p>
      ) : questions ? (
        sortedQuestions.map((question) => {
          return (
            <div key={question.id} className="collapse collapse-arrow bg-base-100 border border-base-300 mb-2">
              <input type="checkbox" name={`question-${question.id}`} />
              <div className="collapse-title font-semibold">{question.title}</div>
              <div className="collapse-content text-sm">
                <div>{question.content}</div>
                <div className="text-xs text-gray-500 mt-1" title={formatExactTime(question.created || question.createdAt)}>
                  {formatRelativeTime(question.created || question.createdAt)}
                </div>
                <div className="text-xs text-gray-600 mt-1">
                  {(likesByQuestionId[question.id] ?? 0)} likes
                </div>
                <div className="mt-2 flex items-center gap-2">
                  <button
                    type="button"
                    className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${userReactions[question.id] === 'like' ? 'text-blue-600' : ''}`}
                    onClick={() => toggleQuestionReaction(question.id, 'like')}
                    disabled={!!pending[question.id]}
                    aria-pressed={userReactions[question.id] === 'like'}
                    aria-label="Like question"
                  >
                    <ThumbsUp size={16} />
                  </button>
                  <button
                    type="button"
                    className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${userReactions[question.id] === 'dislike' ? 'text-red-600' : ''}`}
                    onClick={() => toggleQuestionReaction(question.id, 'dislike')}
                    disabled={!!pending[question.id]}
                    aria-pressed={userReactions[question.id] === 'dislike'}
                    aria-label="Dislike question"
                  >
                    <ThumbsDown size={16} />
                  </button>
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs inline-flex items-center gap-1"
                    onClick={() => handleShareQuestion(question.id)}
                    aria-label="Share question"
                  >
                    <Share size={16} />
                  </button>
                </div>
                <Link to={`/question/${question.id}`} state={{userName, userId, questionUserId: question.userId}} className="btn mt-2">
                  See Comments
                </Link>
              </div>
            </div>
          );
        })
      ) : (
        <p>No questions found</p>
      )}
    </div>
  );
}
