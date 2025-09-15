import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext.jsx';

function LoginPage() {
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isNewUser, setIsNewUser] = useState(false);
  const [successfulRegistration, setSuccessfulRegistration] = useState(false);
  const [userExists] = useState(false);
  const { login } = useUser();

  const navigate = useNavigate();

  useEffect(() => {
    // Future: hydrate from persisted auth here if needed
  }, []);

  async function postLogin() {
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
      // Extract JWT from response body or Authorization header
      const headerToken = response.headers && response.headers.get && response.headers.get('Authorization');
      let jwt = data && (data.jwt || data.token || data.accessToken) || null;
      if (!jwt && headerToken && headerToken.startsWith('Bearer ')) {
        jwt = headerToken.slice('Bearer '.length);
      }
      if (!jwt) {
        throw new Error('Login failed: missing token');
      }
      try {
        localStorage.setItem('jwtToken', jwt);
      } catch (error) {
        console.error('Error setting JWT token:', error);
      }

      // Fetch current user using JWT
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
      setIsLoggedIn(true);
      setError(null);
      navigate('/home', { state: { userName: normalizedUserName, userId: normalizedUserId } });
    } catch (error) {
      setError(error.message);
    }
  }

  function handleLogin(e) {
    e.preventDefault();
    postLogin();
  }

  function handleNewUser(e) {
    e.preventDefault();
    setIsNewUser(true);
  }

  async function postRegistration() {
    try {
      const response = await fetch('/api/user/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: userName, password, email }),
      });
      if (!response.ok) {
        throw new Error('Registration failed');
      }
      setSuccessfulRegistration(true);
      await postLogin();
    } catch (e) {
      setError(e.message);
    }
  }

  function handleRegistration(e) {
    e.preventDefault();
    postRegistration();
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col items-center justify-center px-6 py-8">
      {!isLoggedIn && !isNewUser && (
        <div className="w-full max-w-md bg-white rounded-lg shadow dark:bg-gray-800 dark:border dark:border-gray-700 p-8 space-y-6">
          <h1 className="text-2xl font-bold text-center text-gray-900 dark:text-white">Welcome to AskMate!</h1>
          <h2 className="text-lg font-medium text-center text-gray-600 dark:text-gray-300">Please log in</h2>
          <form onSubmit={handleLogin} className="space-y-4">
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
          <div className="text-center">
            <h4 className="text-sm text-gray-600 dark:text-gray-300">Don’t have an account yet?</h4>
            <button onClick={handleNewUser} className="text-blue-600 hover:underline dark:text-blue-400 mt-1">
              Register
            </button>
          </div>
          {error && (
            <div className="text-center text-red-500 text-sm">
              <h2>{error}</h2>
            </div>
          )}
        </div>
      )}

      {isNewUser && (
        <div className="w-full max-w-md bg-white rounded-lg shadow dark:bg-gray-800 dark:border dark:border-gray-700 p-8 space-y-6 mt-4">
          <h2 className="text-xl font-bold text-center text-gray-900 dark:text-white">Register to our site</h2>
          <form onSubmit={handleRegistration} className="space-y-4">
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
            <input
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
          {userExists && (
            <div className="text-center text-red-500 text-sm">
              <h2>This username already exists. Try another one!</h2>
            </div>
          )}
        </div>
      )}

      {successfulRegistration && !isLoggedIn && (
        <div className="mt-4 text-center text-green-500">
          <h2>Successful registration! Please login!</h2>
        </div>
      )}
    </div>
  );
}

export default LoginPage;
