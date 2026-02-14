import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext.jsx';

export default function RegisterForm({ onRegistered }) {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState(null);
  const [successfulRegistration, setSuccessfulRegistration] = useState(false);
  const navigate = useNavigate();
  const { login } = useUser();
  const API_URL = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      const response = await fetch(`${API_URL}/api/user/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: userName, password, email }),
      });
      if (!response.ok) {
        throw new Error('Registration failed');
      }
      setSuccessfulRegistration(true);
      setError(null);

      // Immediately log the user in with the provided credentials
      const loginRes = await fetch(`${API_URL}/api/user/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: userName, password }),
      });
      if (!loginRes.ok) {
        throw new Error('Auto-login failed');
      }
      const loginData = await loginRes.json();
      const headerToken = loginRes.headers && loginRes.headers.get && loginRes.headers.get('Authorization');
      let jwt = (loginData && (loginData.jwt || loginData.token || loginData.accessToken)) || null;
      if (!jwt && headerToken && headerToken.startsWith('Bearer ')) {
        jwt = headerToken.slice('Bearer '.length);
      }
      if (!jwt) {
        throw new Error('Auto-login failed: missing token');
      }
      try {
        localStorage.setItem('jwtToken', jwt);
      } catch (storageErr) {
        console.error('Error setting JWT token:', storageErr);
      }

      const meRes = await fetch(`${API_URL}/api/user/me`, {
        headers: { Authorization: 'Bearer ' + jwt },
      });
      if (!meRes.ok) {
        throw new Error('Could not fetch user info');
      }
      const me = await meRes.json();
      const normalizedUserName = me.userName ?? me.username ?? userName;
      const normalizedUserId = me.userId ?? me.userid ?? me.id ?? null;

      login({ userName: normalizedUserName, userId: normalizedUserId });
      if (onRegistered) onRegistered();
      navigate('/home', { state: { userName: normalizedUserName, userId: normalizedUserId } });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          name="username"
          onChange={(e) => setUserName(e.target.value)}
          type="text"
          placeholder="Username"
          className="w-full p-2.5 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white"
          required
        />
        <input
          name="password"
          onChange={(e) => setPassword(e.target.value)}
          type="password"
          placeholder="Password"
          autoComplete="off"
          className="w-full p-2.5 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white"
          required
        />
        <input
          name="email"
          onChange={(e) => setEmail(e.target.value)}
          type="email"
          placeholder="Email"
          autoComplete="off"
          className="w-full p-2.5 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white"
          required
        />
        <button
          type="submit"
          className="w-full bg-green-600 hover:bg-green-700 text-white font-medium py-2.5 rounded-lg"
        >
          Register
        </button>
      </form>

      {error && (
        <div className="text-center text-red-500 text-sm">
          <h2>{error}</h2>
        </div>
      )}

      {successfulRegistration && (
        <div className="mt-4 text-center text-green-500">
          <h2>Successful registration! Please login!</h2>
        </div>
      )}
    </>
  );
}


