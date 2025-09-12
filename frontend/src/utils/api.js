export const fetchComments = async (id) => {
  try {
    const response = await fetch(`/api/answer/${id}`, { credentials: 'include' });
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
    const response = await fetch(`/api/question/${id}`, { credentials: 'include' });
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
    const response = await fetch(`/api/answer/${questionId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({ content, userId }),
    });
    if (!response.ok) {
      throw new Error(`Failed to post answer: ${response.status}`);
    }
    return await response.json();
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
      },
      credentials: 'include',
      body: JSON.stringify({ userId, points }),
    });
    if (!response.ok) {
      throw new Error(`Failed to add points: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Error adding points:', error);
    throw error;
  }
}

export const fetchAllQuestions = async () => {
  try {
    const response = await fetch('/api/question/all', { credentials: 'include' });
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
      },
      credentials: 'include',
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
