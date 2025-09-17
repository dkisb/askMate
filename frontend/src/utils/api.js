function getAuthHeader() {
  const token = (() => { try { return localStorage.getItem('jwtToken'); } catch { return null; } })();
  return token ? { Authorization: 'Bearer ' + token } : {};
}

export const fetchComments = async (questionId) => {
  const res = await fetch(`/api/answer/${questionId}`, { headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to fetch comments: ${res.status}`);
  const data = await res.json();
  const list = Array.isArray(data) ? data : [];
  return list.map((c) => {
    const authorRaw = c && (c.author ?? c.user ?? c.userDto);
    let userId = null, userName = null;
    if (authorRaw && typeof authorRaw === 'object') {
      userId = authorRaw.id ?? authorRaw.userId ?? null;
      userName = authorRaw.userName ?? authorRaw.username ?? null;
    } else if (typeof authorRaw === 'string') {
      userName = authorRaw;
    } else {
      userId = c.userId ?? null;
      userName = c.userName ?? c.username ?? null;
    }
    return {
      ...c,
      userId,
      userName,
      createdAt: c.createdAt ?? c.created ?? null,
      created: c.created ?? c.createdAt ?? null,
      parentId: c.parentId ?? null,
      questionId: c.questionId ?? null,
      likes: c.likes ?? 0,
      dislikes: c.dislikes ?? 0,
    };
  });
};

export const fetchTopLevelComments = async (questionId) => {
  const list = await fetchComments(questionId);
  return list.filter((c) => c.parentId == null);
};

// Create top-level answer
export async function postAnswer(questionId, content) {
  const res = await fetch(`/api/answer/`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
    body: JSON.stringify({ content, questionId: Number(questionId) }),
  });
  if (!res.ok) {
    const details = await res.text().catch(() => '');
    throw new Error(`Failed to post answer: ${res.status} ${details}`);
  }
  return true;
}

// Reply (NewReplyDTO shape per your backend: content, userId, parentId)
export async function postCommentReply(parentAnswerId, userId, content) {
  const res = await fetch(`/api/answer/a/${parentAnswerId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
    body: JSON.stringify({ content, userId, parentId: parentAnswerId }),
  });
  if (!res.ok) {
    const details = await res.text().catch(() => '');
    throw new Error(`Failed to post reply: ${res.status} ${details}`);
  }
  return true;
}

// Likes/dislikes, questions, etc. (unchanged)
export const fetchQuestion = async (id) => {
  const res = await fetch(`/api/question/${id}`, { headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to fetch question: ${res.status}`);
  const data = await res.json();
  const author = data && data.author !== undefined ? data.author : {};
  const userId = typeof author === 'object' && author !== null ? (author.id ?? author.userId ?? null) : (data.userId ?? null);
  const userName = typeof author === 'object' && author !== null ? (author.userName ?? author.username ?? null)
                  : (typeof author === 'string' ? author : (data.userName ?? data.username ?? null));
  return { ...data, userId, userName, createdAt: data.createdAt ?? data.created ?? null, created: data.created ?? data.createdAt ?? null };
};

export async function addPoints(userId, points = 10) {
  const res = await fetch('/api/user/', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
    body: JSON.stringify({ userId, points }),
  });
  if (!res.ok) throw new Error(`Failed to add points: ${res.status}`);
  return true;
}


export const fetchAllQuestions = async () => {
  try {
    const response = await fetch('/api/question/all', {
      headers: {
        ...getAuthHeader(),
      },
    });
    if (!response.ok) {
      throw new Error(`Failed to fetch questions: ${response.status}`);
    }
    const list = await response.json();
    const normalized = Array.isArray(list)
      ? list.map((q) => {
          const author = q && q.author !== undefined ? q.author : {};
          const userId = typeof author === 'object' && author !== null
            ? (author.id ?? author.userId ?? null)
            : (q.userId ?? null);
          const userName = typeof author === 'object' && author !== null
            ? (author.userName ?? author.username ?? null)
            : (typeof author === 'string' ? author : (q.userName ?? q.username ?? null));
          return {
            ...q,
            userId,
            userName,
            createdAt: q.createdAt ?? q.created ?? null,
            created: q.created ?? q.createdAt ?? null,
          };
        })
      : [];
    return normalized;
  } catch (error) {
    console.error('Error fetching all questions:', error);
    throw error;
  }
};


export const createQuestion = async (title, content, userId) => {
  try {
    const response = await fetch('/api/question/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
      },
      body: JSON.stringify({ title, content, userId }),
    });
    if (!response.ok) {
      throw new Error(`Failed to create question: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Error creating question:', error);
    throw error;
  }
};

export async function likeQuestion(questionId) {
  const res = await fetch(`/api/question/like/${questionId}`, { method: 'PATCH', headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to like question: ${res.status}`);
  return true;
}
export async function dislikeQuestion(questionId) {
  const res = await fetch(`/api/question/dislike/${questionId}`, { method: 'PATCH', headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to dislike question: ${res.status}`);
  return true;
}
export async function fetchQuestionLikesCount(questionId) {
  const res = await fetch(`/api/question/like/${questionId}`, { headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to fetch question likes: ${res.status}`);
  return await res.json();
}

export async function likeAnswer(answerId) {
  const res = await fetch(`/api/answer/like/${answerId}`, { method: 'PATCH', headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to like answer: ${res.status}`);
  return true;
}
export async function dislikeAnswer(answerId) {
  const res = await fetch(`/api/answer/dislike/${answerId}`, { method: 'PATCH', headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to dislike answer: ${res.status}`);
  return true;
}
export async function fetchAnswerLikesCount(answerId) {
  const res = await fetch(`/api/answer/like/${answerId}`, { headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to fetch answer likes: ${res.status}`);
  return await res.json();
}
export async function fetchAnswerDislikesCount(answerId) {
  const res = await fetch(`/api/answer/dislike/${answerId}`, { headers: { ...getAuthHeader() } });
  if (!res.ok) throw new Error(`Failed to fetch answer dislikes: ${res.status}`);
  return await res.json();
}
