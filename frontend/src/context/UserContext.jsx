import { createContext, useContext, useState } from 'react';

const UserContext = createContext();

function normalizeUser(userData) {
  return {
    ...userData,
  };
}

export function UserProvider({ children }) {
  const [user, setUserRaw] = useState(null);

  const setUser = (userData) => setUserRaw(normalizeUser(userData));
  const login = (userData) => setUserRaw(normalizeUser(userData));
  const logout = () => setUserRaw(null);

  return <UserContext.Provider value={{ user, setUser, login, logout }}>{children}</UserContext.Provider>;
}

export function useUser() {
  return useContext(UserContext);
}
