import React, { useState, useEffect } from 'react';
import PageLayout from '../components/layout/PageLayout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import AlertMessage from '../components/common/AlertMessage';
import SubscriptionCard from '../components/subscriptions/SubscriptionCard';
import WalletSummary from '../components/wallet/WalletSummary'; // Will create this later
import * as api from '../services/api';
import { Link } from 'react-router-dom';
import './DashboardPage.css'; // For specific dashboard layout

const DashboardPage = () => {
  const [subscriptions, setSubscriptions] = useState([]);
  const [wallet, setWallet] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError('');
      try {
        const [subsRes, walletRes, notifRes] = await Promise.all([
          api.fetchActiveSubscriptions(),
          api.fetchMyWallet(),
          api.fetchUnreadNotifications(), // Fetch only unread for dashboard quick view
        ]);
        setSubscriptions(subsRes.data.filter(s => s.active).slice(0, 3)); // Show first 3 active
        setWallet(walletRes.data);
        setNotifications(notifRes.data.slice(0, 3)); // Show first 3 unread
      } catch (err) {
        console.error("Dashboard fetch error:", err.response || err);
        setError('Failed to load dashboard data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleSync = async () => {
    setLoading(true);
    setError('');
    try {
        await api.syncMockTransactions();
        // Refetch data after sync to show immediate updates
        const subsRes = await api.fetchActiveSubscriptions();
        setSubscriptions(subsRes.data.filter(s => s.active).slice(0, 3));
        alert('Mock transactions synced and re-analyzed!');
    } catch (err) {
        console.error("Sync error:", err);
        setError('Failed to sync mock transactions.');
    } finally {
        setLoading(false);
    }
  };


  if (loading) return <div className="container mt-3 text-center"><LoadingSpinner /></div>;

  return (
    <PageLayout title="Dashboard" actions={<button onClick={handleSync} className="btn btn-info">Sync Mock Data</button>}>
      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      <div className="dashboard-grid">
        <section className="dashboard-section wallet-summary-section">
          <h3>Wallet Overview</h3>
          {wallet ? <WalletSummary wallet={wallet} /> : <p>Loading wallet...</p>}
        </section>

        <section className="dashboard-section recent-subscriptions-section">
          <div className="section-header">
            <h3>Active Subscriptions ({subscriptions.length > 0 ? 'Showing few' : 'None'})</h3>
            {subscriptions.length > 0 && <Link to="/subscriptions" className="btn btn-sm btn-outline-primary">View All</Link>}
          </div>
          {subscriptions.length > 0 ? (
            <div className="subscriptions-grid">
              {subscriptions.map(sub => <SubscriptionCard key={sub.id} subscription={sub} />)}
            </div>
          ) : (
            <p>No active subscriptions found. <Link to="/subscriptions">Manage Subscriptions</Link></p>
          )}
        </section>

        <section className="dashboard-section recent-notifications-section">
           <div className="section-header">
            <h3>Recent Notifications ({notifications.length > 0 ? 'Unread' : 'None'})</h3>
             {notifications.length > 0 && <Link to="/notifications" className="btn btn-sm btn-outline-primary">View All</Link>}
          </div>
          {notifications.length > 0 ? (
            <ul className="list-group">
              {notifications.map(notif => (
                <li key={notif.id} className="list-group-item">
                  <small>({notif.type.replace('_', ' ')})</small> {notif.message}
                </li>
              ))}
            </ul>
          ) : (
            <p>No new notifications.</p>
          )}
        </section>
      </div>
    </PageLayout>
  );
};

export default DashboardPage;