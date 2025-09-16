import * as React from 'react';
import { styled, alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Typography from '@mui/material/Typography';
import InputBase from '@mui/material/InputBase';
import IconButton from '@mui/material/IconButton';
import SearchIcon from '@mui/icons-material/Search';
import AccountCircle from '@mui/icons-material/AccountCircle';
import LogoutIcon from '@mui/icons-material/Logout';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useNavigate, Link } from 'react-router-dom';
import { useUser } from '../context/UserContext.jsx';

const Search = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: theme.shape.borderRadius,
  backgroundColor: alpha(theme.palette.common.white, 0.15),
  '&:hover': {
    backgroundColor: alpha(theme.palette.common.white, 0.25),
  },
  marginRight: theme.spacing(2),
  marginLeft: 0,
  width: '100%',
  [theme.breakpoints.up('sm')]: {
    marginLeft: theme.spacing(3),
    width: 'auto',
  },
}));

const SearchIconWrapper = styled('div')(({ theme }) => ({
  padding: theme.spacing(0, 2),
  height: '100%',
  position: 'absolute',
  pointerEvents: 'none',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}));

const StyledInputBase = styled(InputBase)(({ theme }) => ({
  color: 'inherit',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(4)})`,
    transition: theme.transitions.create('width'),
    width: '100%',
    [theme.breakpoints.up('md')]: {
      width: '20ch',
    },
  },
}));

export default function PrimarySearchAppBar({ open = true, onToggle }) {
  const [searchText, setSearchText] = React.useState('');
  const navigate = useNavigate();
  const { logout } = useUser();
  const drawerWidth = 280;
  const miniWidth = 72;

  const handleSearchKeyDown = (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      const q = (searchText || '').trim();
      navigate(q ? `/home?q=${encodeURIComponent(q)}` : '/home');
    }
  };

  const handleSearchChange = (event) => {
    const value = event.target.value;
    setSearchText(value);
    if (value.trim() === '') {
      navigate('/home');
    }
  };

  const handleGoToProfile = () => {
    navigate('/profile');
  };

  const handleLogout = () => {
    try {
      localStorage.removeItem('jwtToken');
    } catch {
      // ignore storage errors
    }
    logout && logout();
    navigate('/', { replace: true });
  };
  const effectiveWidth = open ? drawerWidth : miniWidth;

  return (
    <Box sx={{ width: effectiveWidth, flexShrink: 0 }}>
      <Drawer
        variant="permanent"
        anchor="left"
        sx={{
          '& .MuiDrawer-paper': {
            width: effectiveWidth,
            transition: 'width 200ms ease',
            overflowX: 'hidden',
            boxSizing: 'border-box',
            bgcolor: '#1e3a8a',
            color: 'white',
          },
        }}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
          <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1, justifyContent: open ? 'flex-start' : 'center' }}>
            <IconButton aria-label={open ? 'Collapse sidebar' : 'Expand sidebar'} onClick={onToggle} sx={{ color: 'inherit' }}>
              {open ? <ChevronLeftIcon /> : <ChevronRightIcon />}
            </IconButton>
            {open && (
              <Typography
                variant="h6"
                noWrap
                component={Link}
                to="/home"
                sx={{ color: 'inherit', textDecoration: 'none', cursor: 'pointer' }}
              >
                AskMate
              </Typography>
            )}
          </Box>
          {open ? (
            <Box sx={{ px: 2 }}>
              <Search>
                <SearchIconWrapper>
                  <SearchIcon />
                </SearchIconWrapper>
                <StyledInputBase
                  placeholder="Search…"
                  inputProps={{ 'aria-label': 'search' }}
                  value={searchText}
                  onChange={handleSearchChange}
                  onKeyDown={handleSearchKeyDown}
                />
              </Search>
            </Box>
          ) : (
            <Box sx={{ display: 'flex', justifyContent: 'center', px: 1 }}>
              <IconButton aria-label="Search" sx={{ color: 'inherit' }} onClick={onToggle}>
                <SearchIcon />
              </IconButton>
            </Box>
          )}
          <Box sx={{ flexGrow: 1 }} />
          
          <Divider sx={{ borderColor: 'rgba(255,255,255,0.2)' }} />
          <List>
            <ListItem disablePadding>
              <ListItemButton onClick={handleGoToProfile} sx={{ justifyContent: open ? 'initial' : 'center' }}>
                <ListItemIcon sx={{ color: 'inherit', minWidth: open ? 56 : 'auto' }}>
                  <AccountCircle />
                </ListItemIcon>
                {open && <ListItemText primary="Profile" />}
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton onClick={handleLogout} sx={{ justifyContent: open ? 'initial' : 'center' }}>
                <ListItemIcon sx={{ color: 'inherit', minWidth: open ? 56 : 'auto' }}>
                  <LogoutIcon />
                </ListItemIcon>
                {open && <ListItemText primary="Logout" />}
              </ListItemButton>
            </ListItem>
          </List>
        </Box>
      </Drawer>
    </Box>
  );
}
