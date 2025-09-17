import { useState, useEffect, useRef } from 'react';
import { ArrowBigUp, ArrowBigDown, Share } from 'lucide-react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import {
  fetchQuestion,
  fetchComments,
  postAnswer,
  addPoints,
  likeQuestion,
  dislikeQuestion,
  likeAnswer,
  dislikeAnswer,
  fetchQuestionLikesCount,
  fetchAnswerLikesCount,
  fetchAnswerDislikesCount,
} from '../utils/api.js';
import { formatRelativeTime } from '../utils/transformDate.jsx';
import CommentNode from './CommentNode.jsx';

export default function QuestionPage() {
  const [question, setQuestion] = useState(null);
  const [comments, setComments] = useState([]);
  const [childrenByParent, setChildrenByParent] = useState({});
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');
  const commentRef = useRef(null);
  const { id } = useParams();
  const [postReaction, setPostReaction] = useState(null);
  const [commentReactions, setCommentReactions] = useState({});
  const [pending, setPending] = useState({ post: false, comments: {} });
  const [likeCounts, setLikeCounts] = useState({ question: 0, comments: {} });
  const [dislikeCounts, setDislikeCounts] = useState({ comments: {} });
  const [isQuestionExpanded, setIsQuestionExpanded] = useState(false);

  const location = useLocation();
  const navigate = useNavigate();
  const fromState = location.state || {};
  const [currentUserId, setCurrentUserId] = useState(fromState.userId || null);

  useEffect(() => {
    async function ensureUser() {
      if (currentUserId) return;
      const token = (() => { try { return localStorage.getItem('jwtToken'); } catch { return null; } })();
      if (!token) { navigate('/', { replace: true }); return; }
      try {
        const meRes = await fetch('/api/user/me', { headers: { Authorization: 'Bearer ' + token } });
        if (!meRes.ok) { navigate('/', { replace: true }); return; }
        const me = await meRes.json();
        const idFromMe = me.userId ?? me.id ?? null;
        if (idFromMe) setCurrentUserId(idFromMe); else navigate('/', { replace: true });
      } catch (e) {
        console.error('Error ensuring user:', e);
      }
       { navigate('/', { replace: true }); }
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
      } finally {
        if (!isCancelled) setLoading(false);
      }
    }
    loadQuestion();
    return () => { isCancelled = true; };
  }, [id, currentUserId, navigate]);

  // build flat list + children map
  useEffect(() => {
    let isCancelled = false;
    async function loadComments() {
      if (!id) return;
      try {
        const flat = await fetchComments(id);
        if (isCancelled) return;

        const byParent = {};
        for (const a of flat) {
          const pid = a.parentId ?? null;
          if (!byParent[pid]) byParent[pid] = [];
          byParent[pid].push(a);
        }
        // stable sort by created desc in each bucket
        Object.values(byParent).forEach(arr =>
          arr.sort((a, b) => new Date(b.created || b.createdAt) - new Date(a.created || a.createdAt))
        );

        setComments(byParent[null] || []);
        setChildrenByParent(byParent);

        // preload like/dislike for visible roots
        const entries = await Promise.all(
          (byParent[null] || []).filter((c) => c.id != null).map(async (c) => {
            try {
              const cnt = await fetchAnswerLikesCount(c.id);
              return [c.id, Number(cnt) || 0];
            } catch { return [c.id, 0]; }
          })
        );
        setLikeCounts((prev) => ({ ...prev, comments: Object.fromEntries(entries) }));

        const dislikeEntries = await Promise.all(
          (byParent[null] || []).filter((c) => c.id != null).map(async (c) => {
            try {
              const cnt = await fetchAnswerDislikesCount(c.id);
              return [c.id, Number(cnt) || 0];
            } catch { return [c.id, 0]; }
          })
        );
        setDislikeCounts((prev) => ({ ...prev, comments: Object.fromEntries(dislikeEntries) }));
      } catch (e) {
        console.error('Error loading comments:', e);
      }
    }
    loadComments();
    return () => { isCancelled = true; };
  }, [id]);

  // lightweight poll (optional)
  useEffect(() => {
    if (!id) return;
    const timer = setInterval(async () => {
      try {
        const flat = await fetchComments(id);
        const byParent = {};
        for (const a of flat) {
          const pid = a.parentId ?? null;
          if (!byParent[pid]) byParent[pid] = [];
          byParent[pid].push(a);
        }
        Object.values(byParent).forEach(arr =>
          arr.sort((a, b) => new Date(b.created || b.createdAt) - new Date(a.created || a.createdAt))
        );
        setComments(byParent[null] || []);
        setChildrenByParent(byParent);
      } catch(e) {
        console.error('Error polling comments:', e);
      }
    }, 5000);
    return () => clearInterval(timer);
  }, [id]);

  function refreshAllComments() {
    // reuse the loader
    (async () => {
      const flat = await fetchComments(id);
      const byParent = {};
      for (const a of flat) {
        const pid = a.parentId ?? null;
        if (!byParent[pid]) byParent[pid] = [];
        byParent[pid].push(a);
      }
      Object.values(byParent).forEach(arr =>
        arr.sort((a, b) => new Date(b.created || b.createdAt) - new Date(a.created || a.createdAt))
      );
      setComments(byParent[null] || []);
      setChildrenByParent(byParent);
    })().catch(() => {});
  }

  // textarea autoresize;
  useEffect(() => {
    if (commentRef.current) {
      const el = commentRef.current;
      el.style.height = 'auto';
      el.style.height = el.scrollHeight + 'px';
    }
  }, [commentText]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    try {
      await postAnswer(id, commentText);
      setCommentText('');
      refreshAllComments();
      if (currentUserId) await addPoints(currentUserId);
    } catch (error) {
      console.error('Error posting comment:', error);
      alert('Failed to post comment. Please try again.');
    }
  };

  const sortedComments = comments; // already sorted desc

  async function togglePostReaction(reaction) {
    if (pending.post) return;
    const current = postReaction;
    if (current === null) {
      try {
        setPending((p) => ({ ...p, post: true }));
        if (reaction === 'like') { await likeQuestion(id); setPostReaction('like'); }
        else { await dislikeQuestion(id); setPostReaction('dislike'); }
        const refreshed = await fetchQuestionLikesCount(id);
        setLikeCounts((prev) => ({ ...prev, question: Number(refreshed) || 0 }));
      } catch (err) {
        console.error('Failed to react on question:', err);
      } finally {
        setPending((p) => ({ ...p, post: false }));
      }
      return;
    }
    setPostReaction(current === reaction ? null : reaction);
  }

  async function toggleCommentReaction(commentId, reaction) {
    if (!commentId) return;
    if (pending.comments[commentId]) return;
    const current = commentReactions[commentId] || null;
    if (current === null) {
      try {
        setPending((p) => ({ ...p, comments: { ...p.comments, [commentId]: true } }));
        if (reaction === 'like') { await likeAnswer(commentId); setCommentReactions((r) => ({ ...r, [commentId]: 'like' })); }
        else { await dislikeAnswer(commentId); setCommentReactions((r) => ({ ...r, [commentId]: 'dislike' })); }
        const [likesRef, dislikesRef] = await Promise.all([
          fetchAnswerLikesCount(commentId), fetchAnswerDislikesCount(commentId),
        ]);
        setLikeCounts((prev) => ({ ...prev, comments: { ...prev.comments, [commentId]: Number(likesRef) || 0 } }));
        setDislikeCounts((prev) => ({ ...prev, comments: { ...prev.comments, [commentId]: Number(dislikesRef) || 0 } }));
      } catch (err) {
        console.error('Failed to react on answer:', err);
      } finally {
        setPending((p) => {
          const copy = { ...p.comments }; delete copy[commentId];
          return { ...p, comments: copy };
        });
      }
      return;
    }
    setCommentReactions((r) => ({ ...r, [commentId]: current === reaction ? null : reaction }));
  }

  function handleShareLink(url) {
    const shareData = { title: 'AskMate', text: 'Check this out', url };
    (async () => {
      try {
        if (navigator.share) { await navigator.share(shareData); }
        else if (navigator.clipboard) { await navigator.clipboard.writeText(url); alert('Link copied to clipboard'); }
        else {
          const textarea = document.createElement('textarea');
          textarea.value = url; document.body.appendChild(textarea); textarea.select();
          document.execCommand('copy'); document.body.removeChild(textarea);
          alert('Link copied to clipboard');
        }
      } catch (err) { console.error('Share failed:', err); }
    })();
  }

  function handleShareComment(commentId) {
    const base = window.location.origin + window.location.pathname + window.location.search;
    handleShareLink(`${base}#answer-${commentId}`);
  }
  function handleShareQuestion() { handleShareLink(window.location.href); }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">{
        `Posted by ${question?.author ?? ''}${question?.createdAt ? `, ${formatRelativeTime(question.createdAt)}` : ''}`
      }</h1>

      {loading || !question ? (
        <p>Loading question...</p>
      ) : (
        <div className="mb-6">
          <h2 className="text-xl font-semibold">{question.title}</h2>
          <p
            className={`text-gray-700 whitespace-pre-wrap ${isQuestionExpanded ? '' : 'line-clamp-6'}`}
            style={isQuestionExpanded ? undefined : { display: '-webkit-box', WebkitBoxOrient: 'vertical', overflow: 'hidden' }}
          >
            {question.content}
          </p>
          {question?.content && question.content.split('\n').join(' ').length > 0 && (
            <button
              type="button"
              className="text-xs text-blue-600 hover:underline mt-1"
              onClick={() => setIsQuestionExpanded((v) => !v)}
            >
              {isQuestionExpanded ? 'Show less' : 'Read more'}
            </button>
          )}
          {/* time shown in header; removed duplicate here */}
          <div className="text-xs text-gray-600 mt-1">{likeCounts.question} upvotes</div>

          <div className="mt-2 flex items-center gap-2">
            <button type="button" className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${postReaction === 'like' ? 'text-blue-600' : ''}`}
              onClick={() => togglePostReaction('like')} disabled={pending.post} aria-pressed={postReaction === 'like'} aria-label="Upvote post">
              <ArrowBigUp size={16} />
            </button>
            <button type="button" className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${postReaction === 'dislike' ? 'text-red-600' : ''}`}
              onClick={() => togglePostReaction('dislike')} disabled={pending.post} aria-pressed={postReaction === 'dislike'} aria-label="Downvote post">
              <ArrowBigDown size={16} />
            </button>
            <button type="button" className="btn btn-ghost btn-xs inline-flex items-center gap-1" onClick={handleShareQuestion} aria-label="Share post">
              <Share size={16} />
            </button>
          </div>
        </div>
      )}

      <form className="mb-6" onSubmit={handleSubmit}>
        <div className="py-2 px-4 mb-4 bg-white rounded-lg border border-gray-200">
          <textarea
            id="comment"
            rows="1"
            className="px-0 w-full text-sm text-gray-900 border-0 focus:ring-0 focus:outline-none"
            placeholder="Write a comment..."
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            ref={commentRef}
            style={{ overflow: 'hidden' }}
            required
          />
        </div>
        <button type="submit" className="inline-flex items-center py-2.5 px-4 text-xs font-medium text-white bg-blue-700 rounded-lg hover:bg-blue-800">
          Post comment
        </button>
      </form>

      <h3 className="text-lg font-bold mb-2">Comments</h3>
      <div className="max-h-[60vh] overflow-y-auto pr-1">
        {sortedComments.length > 0 ? (
          sortedComments.map((comment, idx) => (
            <CommentNode
              key={comment.id ?? idx}
              comment={comment}
              questionId={id}
              childrenByParent={childrenByParent}
              likeCountsComments={likeCounts.comments}
              dislikeCountsComments={dislikeCounts.comments}
              commentReactions={commentReactions}
              pendingComments={pending.comments}
              onToggleReaction={toggleCommentReaction}
              onShareComment={handleShareComment}
              onReplyPosted={(parentId) => refreshAllComments(parentId)}
              currentUserId={currentUserId}
            />
          ))
        ) : (
          <p>No comments yet.</p>
        )}
      </div>
    </div>
  );
}
