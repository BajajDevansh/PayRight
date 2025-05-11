import React, { useState, useEffect, useCallback } from 'react';
import PageLayout from '../components/layout/PageLayout';
import NotificationItem from '../components/notifications/NotificationItem';
import LoadingSpinner from '../components/common/LoadingSpinner';
import AlertMessage from '../components/common/AlertMessage';
import * as api from '../services/api';

const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('all'); // 'all', 'unread'

  const fetchNotificationsData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      let response;
      if (filter === 'unread') {
        response = await api.fetchUnreadNotifications();
      } else {
        response = await api.fetchAllNotifications();
      }
      setNotifications(response.data);
    } catch (err) {
      setError('Failed to load notifications.');
      console.error("Fetch notifications error:", err.response || err);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    fetchNotificationsData();
  }, [fetchNotificationsData]);

  const handleMarkAsRead = async (notificationId) => {
    try {
      await api.markNotificationAsReadApi(notificationId);
      // Re-fetch or update local state
      // For simplicity, re-fetch based on current filter
      setNotifications(prev => prev.map(n => n.id === notificationId ? {...n, read: true} : n));
      // Or if you want to refetch to ensure data consistency:
      // fetchNotificationsData();
    } catch (err) {
      console.error("Mark as read error:", err.response || err);
      alert('Failed to mark notification as read.'); // Simple alert for user
    }
  };

  if (loading) return <div className="container mt-3 text-center"><LoadingSpinner /></div>;

  return (
    <PageLayout title="Notifications">
      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      <div className="filter-controls mb-3">
        <button onClick={() => setFilter('all')} className={`btn btn-sm ${filter === 'all' ? 'btn-primary' : 'btn-outline-secondary'} mr-1`}>All</button>
        <button onClick={() => setFilter('unread')} className={`btn btn-sm ${filter === 'unread' ? 'btn-primary' : 'btn-outline-secondary'}`}>Unread</button>
      </div>

      {notifications.length > 0 ? (
        <ul className="list-group list-group-flush">
          {notifications.map(notif => (
            <NotificationItem
              key={notif.id}
              notification={notif}
              onMarkAsRead={handleMarkAsRead}
            />
          ))}
        </ul>
      ) : (
        <p>No notifications found {filter === 'unread' ? 'that are unread' : ''}.</p>
      )}
    </PageLayout>
  );
};

export default NotificationsPage;