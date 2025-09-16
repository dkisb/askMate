import NavBar from './components/NavBar';
import { Outlet } from 'react-router-dom';
import Box from '@mui/material/Box';

export default function Layout() {
  return (
    <>
      <NavBar />
      <Outlet />
    </>
  );
}
