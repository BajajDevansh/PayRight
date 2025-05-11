import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/layout/ProtectedRoute';
import AuthPage from './pages/AuthPage';
import DashboardPage from './pages/DashboardPage';
import SubscriptionsListPage from './pages/SubscriptionsListPage';
import SubscriptionDetailPage from './pages/SubscriptionDetailPage';
// import AddSubscriptionPage from './pages/AddSubscriptionPage'; // We'll add later if needed
import WalletPage from './pages/WalletPage';
import NotificationsPage from './pages/NotificationsPage';
import SettingsPage from './pages/SettingsPage';
import NotFoundPage from './pages/NotFoundPage';
import useAuth from './hooks/useAuth';
import LoadingSpinner from './components/common/LoadingSpinner';

function App() {
  const { loading } = useAuth();

  if (loading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}><LoadingSpinner /></div>;
  }

  return (
    <>
      <Navbar />
      <main>
        <Routes>
          {/* Public Routes */}
          <Route path="/auth/:mode" element={<AuthPage />} />
          <Route path="/auth" element={<Navigate to="/auth/login" />} />

          {/* Protected Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<Navigate to="/dashboard" />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/subscriptions" element={<SubscriptionsListPage />} />
            <Route path="/subscriptions/:id" element={<SubscriptionDetailPage />} />
            {/* <Route path="/subscriptions/add" element={<AddSubscriptionPage />} /> */}
            <Route path="/wallet" element={<WalletPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>

          {/* Not Found */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </>
  );
}

export default App;