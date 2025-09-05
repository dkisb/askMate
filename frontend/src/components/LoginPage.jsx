import { useState } from 'react';
import { Link } from 'react-router-dom';

function LoginPage() {
  const [userName, setUserName] = useState(null);
  const [password, setPassword] = useState(null);
  const [email, setEmail] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isNewUser, setIsNewUser] = useState(false);
  const [userExists, setUserExists] = useState(false);
  const [successfulRegistration, setSuccessfulregistration] = useState(false);
  const [invalidLogin, setInvalidLogin] = useState(false);
  const [userId, setUserId] = useState(null);

  function handleNewUser() {
    setIsNewUser(true);
  }

  async function postRegistration(user) {
    const response = await fetch('/api/user/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user),
    });
    const isUserNameExists = await response.json();
    if (isUserNameExists) {
      setUserExists(true);
    } else {
      setUserExists(false);
      setIsNewUser(false);
      setSuccessfulregistration(true);
      setPassword(null);
      setEmail(null);
      setUserName(null);
    }
  }

  async function postLogin(user) {
    const response = await fetch('/api/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user),
    });
    const loggedInUser = await response.json();
    if (loggedInUser.userId == 0) {
      setInvalidLogin(true);
    } else {
      setInvalidLogin(false);
      setIsLoggedIn(true);
      setUserId(loggedInUser.userId);
      setPassword(null);
    }
  }

  function handleRegistration(e) {
    e.preventDefault();
    const user = { username: userName, password: password, email: email };
    postRegistration(user);
  }

  function handleLogin(e) {
    e.preventDefault();
    const user = { username: userName, password: password };
    postLogin(user);
  }

  function handleLogout() {
    setIsLoggedIn(false);
    setUserName(null);
    setUserId(null);
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
          {invalidLogin && (
            <div className="text-center text-red-500 text-sm">
              <h2>Wrong username or password. Please try again!</h2>
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

      {isLoggedIn && (
        <div className="w-full max-w-md bg-white rounded-lg shadow dark:bg-gray-800 dark:border dark:border-gray-700 p-8 mt-6 space-y-6 text-center">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Welcome, {userName}!</h2>
          <Link
            to={`/home`}
            state={{ userName, userId }}
            className="w-full inline-block text-center bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 rounded-lg"
          >
            Go to Homepage
          </Link>
          <button
            onClick={handleLogout}
            className="w-full bg-red-600 hover:bg-red-700 text-white font-medium py-2.5 rounded-lg"
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
}

export default LoginPage;
