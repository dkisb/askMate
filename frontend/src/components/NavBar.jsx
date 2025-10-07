import * as React from 'react';
import { styled, alpha } from '@mui/material/styles';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import InputBase from '@mui/material/InputBase';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import SearchIcon from '@mui/icons-material/Search';
import AccountCircle from '@mui/icons-material/AccountCircle';
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

export default function PrimarySearchAppBar() {
  const [searchText, setSearchText] = React.useState('');
  const [anchorEl, setAnchorEl] = React.useState(null);
  const navigate = useNavigate();
  const { user, setUser, logout } = useUser();
  const API_URL = import.meta.env.VITE_API_URL;

  console.log('anchorEl:', anchorEl);

  const isMenuOpen = Boolean(anchorEl);

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
    handleMenuClose();
    navigate('/profile');
  };

  const handleGoToMyQuestions = () => {
    handleMenuClose();
    navigate('/my-questions');
  }

  const handleLogout = () => {
    try {
      localStorage.removeItem('jwtToken');
    } catch {
      // ignore storage errors
    }
    logout && logout();
    handleMenuClose();
    navigate('/', { replace: true });
  };

  const handleProfileMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };
  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  // Hydrate user from JWT if missing
  React.useEffect(() => {
    if (user && (user.userName || user.username)) return;
    const token = (() => {
      try { return localStorage.getItem('jwtToken'); } catch { return null; }
    })();
    if (!token) return;
    (async () => {
      try {
        const meRes = await fetch(`${API_URL}/api/user/me`, { headers: { Authorization: 'Bearer ' + token } });
        if (!meRes.ok) return;
        const me = await meRes.json();
        const normalizedUserName = me.userName ?? me.username ?? null;
        const normalizedUserId = me.userId ?? me.userid ?? me.id ?? null;
        const email = me.email ?? null;
        setUser && setUser({ userName: normalizedUserName, userId: normalizedUserId, email });
      } catch {
        // ignore hydration errors
      }
    })();
  }, [user, setUser]);

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static" sx={{ bgcolor: '#1e3a8a' }}>
        <Toolbar>
          <Typography
            variant="h6"
            noWrap
            component={Link}
            to="/home"
            sx={{ color: 'inherit', textDecoration: 'none', cursor: 'pointer', mr: 2 }}
          >
            AskMate
          </Typography>
          <Button color="inherit" component={Link} to="/profile" sx={{ mr: 2 }}>
            My profile
          </Button>

          <Button color="inherit" component={Link} to="/myquestions" sx={{ mr: 2 }}>
            My Questions
          </Button>

          <Box sx={{ flexGrow: 1, display: 'flex', justifyContent: 'center' }}>
            <Box sx={{ maxWidth: 600, flex: 1 }}>
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
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Typography
              variant="h6"
              sx={{ display: { xs: 'none', sm: 'block' }, fontWeight: 600 }}
            >
              Welcome back{user?.userName ? `, ${user.userName}` : ''}
            </Typography>
            <IconButton
              size="large"
              edge="end"
              aria-label="account of current user"
              aria-haspopup="true"
              onClick={handleProfileMenuOpen}
              color="inherit"
            >
              <AccountCircle fontSize="large" />
            </IconButton>
          </Box>
        </Toolbar>
      </AppBar>

      <Menu
        anchorEl={anchorEl}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        open={isMenuOpen}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleGoToProfile}>Profile</MenuItem>
        <MenuItem onClick={handleLogout}>Logout</MenuItem>
        <MenuItem onClick={handleGoToMyQuestions}>My Questions</MenuItem>
      </Menu>
    </Box>
  );
}
