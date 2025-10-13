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
  fetchQuestionDislikesCount,
  fetchAnswerLikesCount,
  fetchAnswerDislikesCount,
  alreadyLikedAnswer,
  alreadyDislikedAnswer,
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
  const [questionLikeCounts, setQuestionLikeCounts] = useState(0);
  const [questionDislikeCounts, setQuestionDislikeCounts] = useState(0);
  const [answerLikeCounts, setAnswerLikeCounts] = useState({'comments': {}});
  const [answerDislikeCounts, setAnswerDislikeCounts] = useState({'comments': {}});
  const [isQuestionExpanded, setIsQuestionExpanded] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const fromState = location.state || {};
  const [currentUserId, setCurrentUserId] = useState(fromState.userId || null);
  const API_URL = import.meta.env.VITE_API_URL;

  useEffect(() => {
    async function ensureUser() {
      if (currentUserId) return;
      const token = (() => { try { return localStorage.getItem('jwtToken'); } catch { return null; } })();
      if (!token) { navigate('/', { replace: true }); return; }
      try {
        const meRes = await fetch(`${API_URL}/api/user/me`, { headers: { Authorization: 'Bearer ' + token } });
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
        const likesNumber = await fetchQuestionLikesCount(id);
        const dislikesNumber = await fetchQuestionDislikesCount(id);
        const likeResponse = await fetch(`${API_URL}/api/question/like/user/${id}`, { headers: { 'Content-Type': 'application/json', ...(localStorage.getItem('jwtToken'  ) ? { Authorization: 'Bearer ' + localStorage.getItem('jwtToken') } : {}) } });
        if (likeResponse.ok) {
          const liked = await likeResponse.json();
          if (liked === true) setPostReaction('like');
          else {
            const dislikeResponse = await fetch(`${API_URL}/api/question/dislike/user/${id}`, { headers: { 'Content-Type': 'application/json', ...(localStorage.getItem('jwtToken'  ) ? { Authorization: 'Bearer ' + localStorage.getItem('jwtToken') } : {}) } });
            if (dislikeResponse.ok) {
              const disliked = await dislikeResponse.json();
              if (disliked === true) setPostReaction('dislike');
            }
          }
        } else { console.error('Failed to fetch user review status'); }
        if (!isCancelled) {
          setQuestion(data);
          setQuestionLikeCounts(Number(likesNumber) || 0);
          setQuestionDislikeCounts(Number(dislikesNumber) || 0);
        }
      } finally {
        if (!isCancelled) setLoading(false);
      }
    }
    loadQuestion();
    return () => { isCancelled = true; };
  }, [id, currentUserId, navigate, API_URL]);

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

        const newLikedComments = {};
        const newDislikedComments = {};
        const reactions = {};
        const promises = [];

        for (const key in byParent) {
          for (const comment of byParent[key]) {
            newLikedComments[comment.id] = Number(comment.likes) || 0;
            newDislikedComments[comment.id] = Number(comment.dislikes) || 0;
            promises.push((async () => {
              if (await alreadyLikedAnswer(comment.id)) return [comment.id, 'like'];
              if (await alreadyDislikedAnswer(comment.id)) return [comment.id, 'dislike'];
              return [comment.id, null];
            })());
          }
        }

        const results = await Promise.all(promises);
        for (const [id, reaction] of results) {
          reactions[id] = reaction;
        }

        setAnswerLikeCounts(prev => ({
          ...prev,
          comments: {
            ...prev.comments,
            ...newLikedComments,
          },
        }));
        setAnswerDislikeCounts(prev => ({
          ...prev,
          comments: {
            ...prev.comments,
            ...newDislikedComments,
          },
        }));
        console.log('reactions:', reactions);
        setCommentReactions(reactions);
        setComments(byParent[null] || []);
        setChildrenByParent(byParent);
      } catch (e) {
        console.error('Error loading comments:', e);
      }
    }
    loadComments();
    return () => { isCancelled = true; };
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
      const newLikedComments = {};
      const newDislikedComments = {};
      for (const key in byParent) {
        for (const comment of byParent[key]) {
          newLikedComments[comment.id] = Number(comment.likes) || 0;
          newDislikedComments[comment.id] = Number(comment.dislikes) || 0;
        }
      }
      setAnswerLikeCounts(prev => ({
        ...prev,
        comments: {
          ...prev.comments,
          ...newLikedComments,
        },
      }));
      setAnswerDislikeCounts(prev => ({
        ...prev,
        comments: {
          ...prev.comments,
          ...newDislikedComments,
        },
      }));
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
      await postAnswer(id, commentText, currentUserId);
      setCommentText('');
      refreshAllComments();
      if (currentUserId) await addPoints(currentUserId);
    } catch (error) {
      console.error('Error posting comment:', error);
      alert('Failed to post comment. Please try again.');
    }
  };

  const sortedComments = comments; // already sorted desc

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

  async function handleLikeQuestionClick(questionId) {
    const data = await likeQuestion(questionId);
    if (data === true) setPostReaction('like');
    else if (data === false) setPostReaction(null);
    try {
      const refreshed = await fetchQuestionLikesCount(questionId);
      setQuestionLikeCounts(Number(refreshed) || 0 );
    } catch(err) {
      console.err("Failed to fetch likescount of question", err)
    }
  }

  async function handleDislikeQuestionClick(questionId) {
    const data = await dislikeQuestion(questionId);
    if (data === true) setPostReaction('dislike');
    else if (data === false) setPostReaction(null);
    try {
      const refreshed = await fetchQuestionDislikesCount(questionId);
      setQuestionDislikeCounts(Number(refreshed) || 0);
    } catch(err) {
      console.err("Failed to fetch dislikecount of question", err)
    }
  }

  async function handleLikeAnswerClick(answerId) {
    const data = await likeAnswer(answerId);
    if (data === true) setCommentReactions((r) => ({ ...r, [answerId]: 'like' }));
    else if (data === false) setCommentReactions((r) => ({ ...r, [answerId]: null }));
    try {
      const refreshed = await fetchAnswerLikesCount(answerId);
      console.log('refreshed like count for', answerId, 'is', refreshed);
      setAnswerLikeCounts((prev) => ({ ...prev, comments: { ...prev.comments, [answerId]: Number(refreshed) || 0 } }));
    } catch(err) {
      console.err("Failed to fetch likecounts of answer", err)
    }
  }

  async function handleDislikeAnswerClick(answerId) {
    const data = await dislikeAnswer(answerId);
    if (data === true) setCommentReactions((r) => ({ ...r, [answerId]: 'dislike' }));
    else if (data === false) setCommentReactions((r) => ({ ...r, [answerId]: null }));
    try {
      const refreshed = await fetchAnswerDislikesCount(answerId);
      setAnswerDislikeCounts((prev) => ({ ...prev, comments: { ...prev.comments, [answerId]: Number(refreshed) || 0 } }));
    } catch(err) {
      console.err("Failed to fetch dislikecount of answer", err)
    }
  }

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
          <div className="text-xs text-gray-600 mt-1">{questionLikeCounts} upvotes {questionDislikeCounts} downvotes</div>

          <div className="mt-2 flex items-center gap-2">
            <button type="button" className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${postReaction === 'like' ? 'text-blue-600' : ''}`}
              onClick={() => handleLikeQuestionClick(id)} 
              disabled={pending.post} aria-pressed={postReaction === 'like'} aria-label="Upvote post">
              <ArrowBigUp size={16} />
            </button>
            <button type="button" className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${postReaction === 'dislike' ? 'text-red-600' : ''}`}
              onClick={() => handleDislikeQuestionClick(id)} 
              disabled={pending.post} aria-pressed={postReaction === 'dislike'} aria-label="Downvote post">
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
              likeCountsComments={answerLikeCounts.comments}
              dislikeCountsComments={answerDislikeCounts.comments}
              commentReactions={commentReactions}
              pendingComments={pending.comments}
              onLikeAnswerClick={handleLikeAnswerClick}
              onDislikeAnswerClick={handleDislikeAnswerClick}
              onShareComment={handleShareComment}
              onReplyPosted={refreshAllComments}
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
