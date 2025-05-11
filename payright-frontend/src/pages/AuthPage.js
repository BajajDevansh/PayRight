import React from 'react';
import { useParams, Link, Navigate } from 'react-router-dom';
import LoginForm from '../components/auth/LoginForm';
import RegisterForm from '../components/auth/RegisterForm';
import useAuth from '../hooks/useAuth';
import './AuthPage.css'; // Create this for styling

const AuthPage = () => {
  const { mode } = useParams(); // 'login' or 'register'
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="auth-page-container">
      <div className="auth-wrapper">
        {mode === 'login' ? <LoginForm /> : <RegisterForm />}
        <div className="auth-switch-link text-center mt-3">
          {mode === 'login' ? (
            <p>Don't have an account? <Link to="/auth/register">Register here</Link></p>
          ) : (
            <p>Already have an account? <Link to="/auth/login">Login here</Link></p>
          )}
        </div>
      </div>
    </div>
  );
};

export default AuthPage;