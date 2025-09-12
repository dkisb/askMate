export function saveAuthUser(user) {
  try {
    const sanitized = user && typeof user === 'object' ? {
      userId: user.userId,
      userName: user.userName,
    } : null;
    if (sanitized) {
      localStorage.setItem('authUser', JSON.stringify(sanitized));
    }
  } catch (e) {
    console.error('Failed to save auth user:', e);
  }
}

export function loadAuthUser() {
  try {
    const raw = localStorage.getItem('authUser');
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (parsed && typeof parsed === 'object' && 'userId' in parsed) {
      return parsed;
    }
    return null;
  } catch (e) {
    console.error('Failed to load auth user:', e);
    return null;
  }
}

export function clearAuthUser() {
  try {
    localStorage.removeItem('authUser');
  } catch (e) {
    console.error('Failed to clear auth user:', e);
  }
}


