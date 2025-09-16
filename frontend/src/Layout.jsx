import NavBar from './components/NavBar';
import { Outlet } from 'react-router-dom';
import Box from '@mui/material/Box';
import { useState } from 'react';

export default function Layout() {
  const drawerWidth = 280;
  const [open, setOpen] = useState(true);
  const toggleDrawer = () => setOpen((v) => !v);
  return (
    <Box sx={{ display: 'flex' }}>
      <NavBar open={open} onToggle={toggleDrawer} />
      <Box component="main" sx={{ flexGrow: 1, ml: open ? `${drawerWidth}px` : '72px', p: 2, transition: 'margin-left 200ms ease' }}>
        <Outlet />
      </Box>
    </Box>
  );
}
