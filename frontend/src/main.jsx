import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import './index.css';
import HomePage from './components/HomePage.jsx';
import LoginPage from './components/LoginPage.jsx';
import QuestionPage from './components/QuestionPage.jsx';
import ProfilePage from './components/ProfilePage.jsx';
import ErrorPage from './ErrorPage.jsx';
import Layout from './Layout.jsx';
import { UserProvider } from './context/UserContext.jsx';
import MyQuestions from './MyQuestions.jsx';

const router = createBrowserRouter([
  {
    path: '/',
    element: <LoginPage />,
    errorElement: <ErrorPage />,
  },
  {
    element: <Layout />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: '/home',
        element: <HomePage />,
      },
      {
        path: '/profile',
        element: <ProfilePage />,
      },
      {
        path: '/question/:id',
        element: <QuestionPage />,
      },
      {
        path: '/myquestions',
        element: <MyQuestions />,
      },
    ],
  },
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <UserProvider>
      <RouterProvider router={router} />  
    </UserProvider>
  </StrictMode>
);
