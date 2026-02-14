/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState } from 'react';

const UserContext = createContext();

export function UserProvider({ children }) {
  const [user, setUserRaw] = useState(null);

  const setUser = (userData) => setUserRaw(userData ? { ...userData } : null);
  const login = (userData) => setUserRaw(userData ? { ...userData } : null);
  const logout = () => setUserRaw(null);

  return <UserContext.Provider value={{ user, setUser, login, logout }}>{children}</UserContext.Provider>;
}

export function useUser() {
  return useContext(UserContext);
}
