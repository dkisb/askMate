import { useState, useEffect } from 'react';
import { ThumbsUp, ThumbsDown, Share } from 'lucide-react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { fetchQuestion, fetchComments, postAnswer, addPoints, likeQuestion, dislikeQuestion, likeAnswer, dislikeAnswer, fetchQuestionLikesCount, fetchAnswerLikesCount } from '../utils/api.js';
import { formatRelativeTime, formatExactTime } from '../utils/transformDate.jsx';

export default function QuestionPage() {
  const [question, setQuestion] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');  
  const { id } = useParams();
  const [postReaction, setPostReaction] = useState(null);
  const [commentReactions, setCommentReactions] = useState({});
  const [pending, setPending] = useState({ post: false, comments: {} });
  const [likeCounts, setLikeCounts] = useState({ question: 0, comments: {} });

  const location = useLocation();
  const navigate = useNavigate();
  const fromState = location.state || {};
  const [currentUserId, setCurrentUserId] = useState(fromState.userId || null);

  useEffect(() => {
    async function ensureUser() {
      if (currentUserId) return;
      const token = (() => { try { return localStorage.getItem('jwtToken'); } catch { return null; } })();
      if (!token) {
        navigate('/', { replace: true });
        return;
      }
      try {
        const meRes = await fetch('/api/user/me', { headers: { Authorization: 'Bearer ' + token } });
        if (!meRes.ok) {
          navigate('/', { replace: true });
          return;
        }
        const me = await meRes.json();
        const idFromMe = me.userId ?? me.id ?? null;
        if (idFromMe) setCurrentUserId(idFromMe);
        else navigate('/', { replace: true });
      } catch (error) {
        console.error('Failed to resolve current user:', error);
        navigate('/', { replace: true });
      }
    }
    ensureUser();
    let isCancelled = false;
    async function loadQuestion() {
      if (!id) return;
      setLoading(true);
      try {
        const data = await fetchQuestion(id);
        const count = await fetchQuestionLikesCount(id);
        if (!isCancelled) {
          setQuestion(data);
          setLikeCounts((prev) => ({ ...prev, question: Number(count) || 0 }));
        }
      } catch (error) {
        console.error('Error loading question:', error);
      } finally {
        if (!isCancelled) {
          setLoading(false);
        }
      }
    }
    loadQuestion();
    return () => {
      isCancelled = true;
    };
  }, [id, currentUserId, navigate]);

 

  useEffect(() => {
    let isCancelled = false;
    async function loadComments() {
      if (!id) return;
      try {
        const data = await fetchComments(id);
        if (!isCancelled) {
          const list = Array.isArray(data) ? data : [];
          setComments(list);
          const entries = await Promise.all(
            list.filter((c) => c.id != null).map(async (c) => {
              try {
                const cnt = await fetchAnswerLikesCount(c.id);
                return [c.id, Number(cnt) || 0];
              } catch {
                return [c.id, 0];
              }
            })
          );
          setLikeCounts((prev) => ({ ...prev, comments: Object.fromEntries(entries) }));
        }
      } catch (error) {
        console.error('Error loading comments:', error);
      }
    }
    loadComments();
    return () => {
      isCancelled = true;
    };
  }, [id]);

  // Poll for real-time-like updates for question and comments
  useEffect(() => {
    if (!id) return;
    const intervalId = setInterval(async () => {
      try {
        const [q, c] = await Promise.all([
          fetchQuestion(id),
          fetchComments(id),
        ]);
        setQuestion(q);
        setComments(Array.isArray(c) ? c : []);
      } catch {
        // Ignore transient polling errors
      }
    }, 5000);
    return () => clearInterval(intervalId);
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;

    try {
      await postAnswer(id, commentText, currentUserId);
    } catch (error) {
      console.error('Error posting comment:', error);
      alert('Failed to post comment. Please try again.');
      return;
    }

    const newComment = {
      id: null,
      content: commentText,
      userId: currentUserId,
      createdAt: new Date().toISOString(),
    };
    setComments([...comments, newComment]);
    setCommentText('');

    try {
      await addPoints(currentUserId);
    } catch (err) {
      console.warn('Failed to add points:', err);
    }
  };

  const sortedComments = [...comments].sort(
    (a, b) => new Date(b.created || b.createdAt) - new Date(a.created || a.createdAt)
  );

  async function togglePostReaction(reaction) {
    if (pending.post) return;
    const current = postReaction;
    // First-time reaction -> call backend once
    if (current === null) {
      try {
        setPending((p) => ({ ...p, post: true }));
        if (reaction === 'like') {
          await likeQuestion(id);
          setPostReaction('like');
        } else if (reaction === 'dislike') {
          await dislikeQuestion(id);
          setPostReaction('dislike');
        }
        const refreshed = await fetchQuestionLikesCount(id);
        setLikeCounts((prev) => ({ ...prev, question: Number(refreshed) || 0 }));
      } catch (err) {
        console.error('Failed to react on question:', err);
      } finally {
        setPending((p) => ({ ...p, post: false }));
      }
      return;
    }
    // Toggle locally without extra backend increments
    if (current === reaction) {
      setPostReaction(null);
    } else {
      setPostReaction(reaction);
    }
  }

  async function toggleCommentReaction(commentId, reaction) {
    if (!commentId) return;
    if (pending.comments[commentId]) return;
    const current = commentReactions[commentId] || null;
    if (current === null) {
      try {
        setPending((p) => ({ ...p, comments: { ...p.comments, [commentId]: true } }));
        if (reaction === 'like') {
          await likeAnswer(commentId);
          setCommentReactions((r) => ({ ...r, [commentId]: 'like' }));
        } else if (reaction === 'dislike') {
          await dislikeAnswer(commentId);
          setCommentReactions((r) => ({ ...r, [commentId]: 'dislike' }));
        }
        const refreshed = await fetchAnswerLikesCount(commentId);
        setLikeCounts((prev) => ({ ...prev, comments: { ...prev.comments, [commentId]: Number(refreshed) || 0 } }));
      } catch (err) {
        console.error('Failed to react on answer:', err);
      } finally {
        setPending((p) => {
          const copy = { ...p.comments };
          delete copy[commentId];
          return { ...p, comments: copy };
        });
      }
      return;
    }
    if (current === reaction) {
      setCommentReactions((r) => ({ ...r, [commentId]: null }));
    } else {
      setCommentReactions((r) => ({ ...r, [commentId]: reaction }));
    }
  }

  async function handleShareQuestion() {
    const url = window.location.href;
    const shareData = { title: 'AskMate Question', text: 'Check out this question on AskMate', url };
    try {
      if (navigator.share) {
        await navigator.share(shareData);
      } else if (navigator.clipboard) {
        await navigator.clipboard.writeText(url);
        alert('Link copied to clipboard');
      } else {
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
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Posted by {question?.author}</h1>
      {loading || !question ? (
        <p>Loading question...</p>
      ) : (
        <div className="mb-6">
          <h2 className="text-xl font-semibold">{question.title}</h2>
          <p className="text-gray-500">{question.content}</p>
          <small className="text-gray-600" title={formatExactTime(question.createdAt)}>
             {formatRelativeTime(question.createdAt)}
          </small>
          <div className="text-xs text-gray-600 mt-1">
            {likeCounts.question} likes
          </div>
          <div className="mt-2 flex items-center gap-2">
            <button
              type="button"
              className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${postReaction === 'like' ? 'text-blue-600' : ''}`}
              onClick={() => togglePostReaction('like')}
              disabled={pending.post}
              aria-pressed={postReaction === 'like'}
              aria-label="Like post"
            >
              <ThumbsUp size={16} />
            </button>
            <button
              type="button"
              className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${postReaction === 'dislike' ? 'text-red-600' : ''}`}
              onClick={() => togglePostReaction('dislike')}
              disabled={pending.post}
              aria-pressed={postReaction === 'dislike'}
              aria-label="Dislike post"
            >
              <ThumbsDown size={16} />
            </button>
            <button
              type="button"
              className="btn btn-ghost btn-xs inline-flex items-center gap-1"
              onClick={handleShareQuestion}
              aria-label="Share post"
            >
              <Share size={16} />
            </button>
          </div>
        </div>
      )}
      <form className="mb-6" onSubmit={handleSubmit}>
        <div className="py-2 px-4 mb-4 bg-white rounded-lg border border-gray-200">
          <textarea
            id="comment"
            rows="6"
            className="px-0 w-full text-sm text-gray-900 border-0 focus:ring-0 focus:outline-none"
            placeholder="Write a comment..."
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            required
          />
        </div>
        <button
          type="submit"
          className="inline-flex items-center py-2.5 px-4 text-xs font-medium text-white bg-blue-700 rounded-lg hover:bg-blue-800"
        >
          Post comment
        </button>
      </form>

      <h3 className="text-lg font-bold mb-4">Comments</h3>
      {sortedComments.length > 0 ? (
        sortedComments.map((comment) => (
          <div key={comment.id ?? Math.random()} className="p-6 mb-3 bg-white rounded-lg border-t border-gray-200">
            <p className="text-lg font-bold mb-1">{comment.content}</p>
            <div className="text-xs text-gray-700 mb-2">By {comment.author}</div>
            <small className="text-gray-600" title={formatExactTime(comment.created || comment.createdAt)}>
              {formatRelativeTime(comment.created || comment.createdAt)}
            </small>
            <div className="text-xs text-gray-600 mt-1">
              {(likeCounts.comments[comment.id] ?? 0)} likes
            </div>
            <div className="mt-2 flex items-center gap-2">
              <button
                type="button"
                className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${commentReactions[comment.id] === 'like' ? 'text-blue-600' : ''}`}
                onClick={() => toggleCommentReaction(comment.id, 'like')}
                disabled={!!pending.comments[comment.id]}
                aria-pressed={commentReactions[comment.id] === 'like'}
                aria-label="Like comment"
              >
                <ThumbsUp size={16} />
              </button>
              <button
                type="button"
                className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${commentReactions[comment.id] === 'dislike' ? 'text-red-600' : ''}`}
                onClick={() => toggleCommentReaction(comment.id, 'dislike')}
                disabled={!!pending.comments[comment.id]}
                aria-pressed={commentReactions[comment.id] === 'dislike'}
                aria-label="Dislike comment"
              >
                <ThumbsDown size={16} />
              </button>
              <button
                type="button"
                className="btn btn-ghost btn-xs inline-flex items-center gap-1"
                onClick={handleShareQuestion}
                aria-label="Share comment"
              >
                <Share size={16} />
              </button>
            </div>
          </div>
        ))
      ) : (
        <p>No comments yet.</p>
      )}
    </div>
  );
}
