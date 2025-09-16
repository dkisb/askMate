import { useState, useEffect } from 'react';
import LoginForm from './LoginForm.jsx';
import RegisterForm from './RegisterForm.jsx';

function LoginPage() {
  const [isNewUser, setIsNewUser] = useState(false);
  const [successfulRegistration, setSuccessfulRegistration] = useState(false);

  useEffect(() => {
    // Future: hydrate from persisted auth here if needed
  }, []);

  function handleNewUser(e) {
    e.preventDefault();
    setIsNewUser(true);
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col items-center justify-center px-6 py-8">
      {!isNewUser && (
        <div className="w-full max-w-md bg-white rounded-lg shadow dark:bg-gray-800 dark:border dark:border-gray-700 p-8 space-y-6">
          <h1 className="text-2xl font-bold text-center text-gray-900 dark:text-white">Welcome to AskMate!</h1>
          <h2 className="text-lg font-medium text-center text-gray-600 dark:text-gray-300">Please log in</h2>
          <LoginForm />
          <div className="text-center">
            <h4 className="text-sm text-gray-600 dark:text-gray-300">Don’t have an account yet?</h4>
            <button onClick={handleNewUser} className="text-blue-600 hover:underline dark:text-blue-400 mt-1">
              Register
            </button>
          </div>
        </div>
      )}

      {isNewUser && (
        <div className="w-full max-w-md bg-white rounded-lg shadow dark:bg-gray-800 dark:border dark:border-gray-700 p-8 space-y-6 mt-4">
          <h2 className="text-xl font-bold text-center text-gray-900 dark:text-white">Register to our site</h2>
          <RegisterForm onRegistered={() => setSuccessfulRegistration(true)} />
        </div>
      )}

      {successfulRegistration && (
        <div className="mt-4 text-center text-green-500">
          <h2>Successful registration! Please login!</h2>
        </div>
      )}
    </div>
  );
}

export default LoginPage;
