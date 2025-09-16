import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext.jsx';

export default function LoginForm() {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { login } = useUser();

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      const response = await fetch('/api/user/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: userName, password }),
      });
      if (!response.ok) {
        throw new Error('Invalid login');
      }
      const data = await response.json();
      const headerToken = response.headers && response.headers.get && response.headers.get('Authorization');
      let jwt = (data && (data.jwt || data.token || data.accessToken)) || null;
      if (!jwt && headerToken && headerToken.startsWith('Bearer ')) {
        jwt = headerToken.slice('Bearer '.length);
      }
      if (!jwt) {
        throw new Error('Login failed: missing token');
      }
      try {
        localStorage.setItem('jwtToken', jwt);
      } catch (storageErr) {
        console.error('Error setting JWT token:', storageErr);
      }

      const meRes = await fetch('/api/user/me', {
        headers: { Authorization: 'Bearer ' + jwt },
      });
      if (!meRes.ok) {
        throw new Error('Could not fetch user info');
      }
      const me = await meRes.json();
      const normalizedUserName = me.userName ?? me.username ?? userName;
      const normalizedUserId = me.userId ?? me.userid ?? me.id ?? null;

      login({ userName: normalizedUserName, userId: normalizedUserId });
      setError(null);
      navigate('/home', { state: { userName: normalizedUserName, userId: normalizedUserId } });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          onChange={(e) => setUserName(e.target.value)}
          type="text"
          placeholder="Username"
          className="w-full p-2.5 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white"
          required
        />
        <input
          onChange={(e) => setPassword(e.target.value)}
          type="password"
          placeholder="Password"
          autoComplete="off"
          className="w-full p-2.5 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white"
          required
        />
        <button
          type="submit"
          className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 rounded-lg"
        >
          Login
        </button>
      </form>
      {error && (
        <div className="text-center text-red-500 text-sm">
          <h2>{error}</h2>
        </div>
      )}
    </>
  );
}


