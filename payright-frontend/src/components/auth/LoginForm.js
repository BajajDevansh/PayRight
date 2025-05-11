import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import * as api from '../../services/api';
import AlertMessage from '../common/AlertMessage';

const LoginForm = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await api.loginUser({ username, password });
      login(response.data); // response.data should be { jwtToken, userId, username }
      navigate('/dashboard');
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.response?.data || 'Login failed. Please check your credentials.';
      setError(errorMessage);
      console.error('Login error:', err.response || err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="auth-form card">
      <div className="card-body">
        <h3 className="card-title text-center">Login</h3>
        {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
        <div className="form-group">
          <label htmlFor="login-username">Username (Email)</label>
          <input
            type="email"
            id="login-username"
            className="form-control"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            autoComplete="username"
          />
        </div>
        <div className="form-group">
          <label htmlFor="login-password">Password</label>
          <input
            type="password"
            id="login-password"
            className="form-control"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </div>
        <button type="submit" className="btn btn-primary btn-block w-100 mt-3" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </div>
    </form>
  );
};

export default LoginForm;