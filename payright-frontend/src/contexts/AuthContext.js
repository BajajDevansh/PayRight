import React, { createContext, useState, useEffect, useCallback } from 'react';
import { TOKEN_KEY, USER_ID_KEY, USERNAME_KEY } from '../utils/constants';
import * as api from '../services/api'; // We will create this next

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null); // { id, username, token }
  const [loading, setLoading] = useState(true);

  const initializeAuth = useCallback(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    const userId = localStorage.getItem(USER_ID_KEY);
    const username = localStorage.getItem(USERNAME_KEY);

    if (token && userId && username) {
      setUser({ id: userId, username, token });
      api.setAuthToken(token); // Set token for future API calls
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  const login = (userData) => { // userData from backend: { jwtToken, userId, username }
    localStorage.setItem(TOKEN_KEY, userData.jwtToken);
    localStorage.setItem(USER_ID_KEY, userData.userId);
    localStorage.setItem(USERNAME_KEY, userData.username);
    setUser({ id: userData.userId, username: userData.username, token: userData.jwtToken });
    api.setAuthToken(userData.jwtToken);
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(USERNAME_KEY);
    setUser(null);
    api.setAuthToken(null);
  };

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthContext;