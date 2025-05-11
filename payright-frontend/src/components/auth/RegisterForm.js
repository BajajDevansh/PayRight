import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as api from '../../services/api';
import AlertMessage from '../common/AlertMessage';

const RegisterForm = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await api.registerUser({ username, password, fullName });
      setSuccess('Registration successful! Please login.');
      // Optionally redirect to login after a short delay
      setTimeout(() => {
        navigate('/auth/login');
      }, 2000);
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.response?.data || 'Registration failed. Please try again.';
      setError(errorMessage);
      console.error('Registration error:', err.response || err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="auth-form card">
      <div className="card-body">
        <h3 className="card-title text-center">Register</h3>
        {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
        {success && <AlertMessage type="success" message={success} />}
        <div className="form-group">
          <label htmlFor="register-fullname">Full Name</label>
          <input
            type="text"
            id="register-fullname"
            className="form-control"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="register-username">Username (Email)</label>
          <input
            type="email"
            id="register-username"
            className="form-control"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            autoComplete="username"
          />
        </div>
        <div className="form-group">
          <label htmlFor="register-password">Password</label>
          <input
            type="password"
            id="register-password"
            className="form-control"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength="6"
            autoComplete="new-password"
          />
        </div>
        <button type="submit" className="btn btn-primary btn-block w-100 mt-3" disabled={loading}>
          {loading ? 'Registering...' : 'Register'}
        </button>
      </div>
    </form>
  );
};

export default RegisterForm;