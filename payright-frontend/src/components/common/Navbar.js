import React from 'react';
import { Link, NavLink as RouterNavLink, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/auth/login');
  };

  return (
    <nav className="navbar">
      <div className="container">
        <Link to="/" className="navbar-brand">PayRight</Link>
        <ul className="navbar-nav">
          {isAuthenticated ? (
            <>
              <li className="nav-item">
                <RouterNavLink to="/dashboard" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>Dashboard</RouterNavLink>
              </li>
              <li className="nav-item">
                <RouterNavLink to="/subscriptions" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>Subscriptions</RouterNavLink>
              </li>
              <li className="nav-item">
                <RouterNavLink to="/wallet" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>Wallet</RouterNavLink>
              </li>
              <li className="nav-item">
                <RouterNavLink to="/notifications" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>Notifications</RouterNavLink>
              </li>
              <li className="nav-item">
                <span className="nav-link" style={{color: 'rgba(255,255,255,.5)'}}>Hi, {user?.username}</span>
              </li>
              <li className="nav-item">
                <button onClick={handleLogout} className="nav-link">Logout</button>
              </li>
            </>
          ) : (
            <>
              <li className="nav-item">
                <RouterNavLink to="/auth/login" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>Login</RouterNavLink>
              </li>
              <li className="nav-item">
                <RouterNavLink to="/auth/register" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>Register</RouterNavLink>
              </li>
            </>
          )}
        </ul>
      </div>
    </nav>
  );
};

export default Navbar;