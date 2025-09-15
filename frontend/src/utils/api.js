function getAuthHeader() {
  const token = (() => {
    try { return localStorage.getItem('jwtToken'); } catch { return null; }
  })();
  return token ? { Authorization: 'Bearer ' + token } : {};
}

export const fetchComments = async (id) => {
  try {
    const response = await fetch(`/api/answer/${id}`, {
      headers: {
        ...getAuthHeader(),
      },
    });
    if (!response.ok) {
      throw new Error(`Failed to fetch comments: ${response.status}`);
    }
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching comments:', error);
    throw error;
  }
};

export const fetchQuestion = async (id) => {
  try {
    const response = await fetch(`/api/question/${id}`, {
      headers: {
        ...getAuthHeader(),
      },
    });
    if (!response.ok) {
      throw new Error(`Failed to fetch question: ${response.status}`);
    }
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching question:', error);
    throw error;
  }
};

export async function postAnswer(questionId, content, userId) {
  try {
    const response = await fetch(`/api/answer/`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
      },
      body: JSON.stringify({ content, questionId: Number(questionId), userId }),
    });
    if (!response.ok) {
      let details = '';
      try {
        details = await response.text();
      } catch {
        // ignore
      }
      throw new Error(`Failed to post answer: ${response.status} ${details}`);
    }
    return true;
  } catch (error) {
    console.error('Error posting answer:', error);
    throw error;
  }
}

export async function addPoints(userId, points = 10) {
  try {
    const response = await fetch('/api/user/', {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
      },
      body: JSON.stringify({ userId, points }),
    });
    if (!response.ok) {
      throw new Error(`Failed to add points: ${response.status}`);
    }
    return true;
  } catch (error) {
    console.error('Error adding points:', error);
    throw error;
  }
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
    return await response.json();
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

// Like/Dislike APIs (backend-only increments)
export async function likeQuestion(questionId) {
  const response = await fetch(`/api/question/like/${questionId}`, {
    method: 'PATCH',
    headers: {
      ...getAuthHeader(),
    },
  });
  if (!response.ok) {
    throw new Error(`Failed to like question: ${response.status}`);
  }
  return true;
}

// Like count fetchers
export async function fetchQuestionLikesCount(questionId) {
  const response = await fetch(`/api/question/like/${questionId}`, {
    headers: { ...getAuthHeader() },
  });
  if (!response.ok) {
    throw new Error(`Failed to fetch question likes: ${response.status}`);
  }
  return await response.json();
}

export async function fetchAnswerLikesCount(answerId) {
  const response = await fetch(`/api/answer/like/${answerId}`, {
    headers: { ...getAuthHeader() },
  });
  if (!response.ok) {
    throw new Error(`Failed to fetch answer likes: ${response.status}`);
  }
  return await response.json();
}

export async function dislikeQuestion(questionId) {
  const response = await fetch(`/api/question/dislike/${questionId}`, {
    method: 'PATCH',
    headers: {
      ...getAuthHeader(),
    },
  });
  if (!response.ok) {
    throw new Error(`Failed to dislike question: ${response.status}`);
  }
  return true;
}

export async function likeAnswer(answerId) {
  const response = await fetch(`/api/answer/like/${answerId}`, {
    method: 'PATCH',
    headers: {
      ...getAuthHeader(),
    },
  });
  if (!response.ok) {
    throw new Error(`Failed to like answer: ${response.status}`);
  }
  return true;
}

export async function dislikeAnswer(answerId) {
  const response = await fetch(`/api/answer/dislike/${answerId}`, {
    method: 'PATCH',
    headers: {
      ...getAuthHeader(),
    },
  });
  if (!response.ok) {
    throw new Error(`Failed to dislike answer: ${response.status}`);
  }
  return true;
}
