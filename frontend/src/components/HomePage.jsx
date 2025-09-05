import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';

export default function HomePage() {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [questionTitle, setQuestionTitle] = useState('');
  const [questionContent, setQuestionContent] = useState('');

  const location = useLocation();
  const { userName, userId } = location.state || {};

  const fetchData = async () => {
    try {
      const response = await fetch('/api/question/all');
      const data = await response.json();
      console.log(data);
      setQuestions(data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching data:', error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  async function addPoints() {
    const response = await fetch('/api/user/', {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ userId: userId, points: 5}),
    });
    const data = await response.json();
    console.log(data);
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    console.log('Title:', questionTitle);
    console.log('Content:', questionContent);
    try {
      const response = await fetch('/api/question/', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ title: questionTitle, content: questionContent, userId: userId}),
      });
      const data = await response.json();
      console.log('New question created:', data);
      setQuestionTitle('');
      setQuestionContent('');
      fetchData();
      addPoints();
    } catch (error) {
      console.error('Error creating question:', error);
    }
  };

  const sortedQuestions = [...questions].sort(
    (a, b) => new Date(b.created || b.createdAt) - new Date(a.created || a.createdAt)
  );

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
