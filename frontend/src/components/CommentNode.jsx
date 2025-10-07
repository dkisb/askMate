import { useState, useRef, useEffect } from 'react';
import { ArrowBigUp, ArrowBigDown, Share, ChevronRight, ChevronDown } from 'lucide-react';
import { formatRelativeTime } from '../utils/transformDate.jsx';
import { postCommentReply } from '../utils/api.js';

export default function CommentNode({
  comment,
  questionId,
  childrenByParent, 
  likeCountsComments,
  dislikeCountsComments,
  onLikeAnswerClick,
  onDislikeAnswerClick,
  commentReactions,
  onToggleReaction,
  pendingComments,
  onShareComment,
  onReplyPosted,         
  currentUserId,      
}) {
  const commentId = comment.id;
  const [replyOpen, setReplyOpen] = useState(false);
  const [replyText, setReplyText] = useState('');
  const [expanded, setExpanded] = useState(false);
  const [isContentExpanded, setIsContentExpanded] = useState(false);
  const [isClamped, setIsClamped] = useState(false);
  const contentRef = useRef(null);

  useEffect(() => {
    const el = contentRef.current;
    if (!el || isContentExpanded) return;
    const clamped = el.scrollHeight > el.clientHeight + 1;
    setIsClamped(clamped);
  }, [comment.content, isContentExpanded]);

  const childReplies = childrenByParent?.[commentId] || [];

  async function submitReply() {
    const text = replyText.trim();
    if (!text || !commentId) return;
    try {
      await postCommentReply(commentId, currentUserId, text);
      setReplyText('');
      setReplyOpen(false);
      onReplyPosted();
    } catch (error) {
      console.error('Error posting reply:', error);
      alert('Failed to post reply. Please try again.');
    }
  }

  return (
    <div id={commentId ? `answer-${commentId}` : undefined} className="p-3 mb-2 bg-white rounded-md border border-gray-200">
      <div className="text-xs text-gray-600 mb-1">
        Posted by <span className="font-medium text-gray-700">{comment.author ?? comment.userName ?? 'Unknown'}</span>,
        {' '}{formatRelativeTime(comment.created || comment.createdAt)}
      </div>
      <p
        ref={contentRef}
        className={`text-[13px] leading-snug text-gray-900 mb-1 whitespace-pre-wrap ${isContentExpanded ? '' : 'line-clamp-4'}`}
        style={isContentExpanded ? undefined : { display: '-webkit-box', WebkitBoxOrient: 'vertical', overflow: 'hidden' }}
      >
        {comment.content}
      </p>
      {(isClamped || isContentExpanded) && (
        <button
          type="button"
          className="text-xs text-blue-600 hover:underline mt-1"
          onClick={() => setIsContentExpanded((v) => !v)}
        >
          {isContentExpanded ? 'Show less' : 'Read more'}
        </button>
      )}
      <div className="text-[11px] text-gray-600 mt-1">{(likeCountsComments[commentId] ?? 0)} upvotes {(dislikeCountsComments[commentId] ?? 0)} downvotes</div>

      <div className="mt-2 flex items-center gap-2">
        <button
          type="button"
          className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${commentReactions[commentId] === 'like' ? 'text-blue-600' : ''}`}
          onClick={() => onLikeAnswerClick(commentId)}
          disabled={!!pendingComments[commentId]}
          aria-pressed={commentReactions[commentId] === 'like'}
          aria-label="Upvote comment"
        >
          <ArrowBigUp size={16} />
        </button>
        <button
          type="button"
          className={`btn btn-ghost btn-xs inline-flex items-center gap-1 ${commentReactions[commentId] === 'dislike' ? 'text-red-600' : ''}`}
          onClick={() => onDislikeAnswerClick(commentId)}
          disabled={!!pendingComments[commentId]}
          aria-pressed={commentReactions[commentId] === 'dislike'}
          aria-label="Downvote comment"
        >
          <ArrowBigDown size={16} />
        </button>
        <button
          type="button"
          className="btn btn-ghost btn-xs inline-flex items-center gap-1"
          onClick={() => onShareComment(commentId)}
          aria-label="Share comment"
        >
          <Share size={16} />
        </button>
        {commentId && (
          <button
            type="button"
            className="btn btn-ghost btn-xs inline-flex items-center gap-1"
            onClick={() => setReplyOpen((v) => !v)}
            aria-expanded={replyOpen}
          >
            Reply
          </button>
        )}

        {childReplies.length > 0 && (
          <button
            type="button"
            className="btn btn-ghost btn-xs inline-flex items-center gap-1 ml-1"
            onClick={() => setExpanded((v) => !v)}
            aria-expanded={expanded}
            aria-controls={commentId ? `replies-${commentId}` : undefined}
          >
            {expanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
            <span className="text-[11px]">{expanded ? 'Hide' : 'Show'} replies ({childReplies.length})</span>
          </button>
        )}
      </div>

      {replyOpen && (
        <div className="mt-2">
          <div className="py-2 px-3 mb-2 bg-white rounded-md border border-gray-200">
            <textarea
              rows="1"
              className="px-0 w-full text-sm text-gray-900 border-0 focus:ring-0 focus:outline-none"
              placeholder="Write a reply..."
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              style={{ overflow: 'hidden' }}
            />
          </div>
          <button
            type="button"
            className="inline-flex items-center py-1 px-2 text-[11px] font-medium text-white bg-blue-700 rounded hover:bg-blue-800"
            onClick={submitReply}
          >
            Post reply
          </button>
        </div>
      )}

      {childReplies.length > 0 && expanded && (
        <div id={commentId ? `replies-${commentId}` : undefined} className="mt-3 ml-3 border-l pl-3 space-y-2">
          {childReplies.map((reply) => (
            <CommentNode
              key={reply.id}
              comment={reply}
              questionId={questionId}
              childrenByParent={childrenByParent}
              likeCountsComments={likeCountsComments}
              dislikeCountsComments={dislikeCountsComments}
              onLikeAnswerClick={onLikeAnswerClick}
              onDislikeAnswerClick={onDislikeAnswerClick}
              commentReactions={commentReactions}
              pendingComments={pendingComments}
              onShareComment={onShareComment}
              onReplyPosted={onReplyPosted}
              currentUserId={currentUserId}
              onToggleReaction={onToggleReaction}
            />
          ))}
        </div>
      )}
    </div>
  );
}
