import React from 'react';
import { formatDate } from '../../utils/helpers';
import { Link } from 'react-router-dom';
import './NotificationItem.css';

const NotificationItem = ({ notification, onMarkAsRead }) => {
  return (
    <li className={`list-group-item notification-item ${notification.read ? 'read' : 'unread'}`}>
      <div className="notification-content">
        <span className="notification-type">[{notification.type.replace('_', ' ').toLowerCase()}]</span>
        <p className="notification-message">{notification.message}</p>
        <small className="notification-date">{formatDate(notification.createdAt, 'MMM dd, yyyy HH:mm')}</small>
      </div>
      <div className="notification-actions">
        {!notification.read && (
          <button onClick={() => onMarkAsRead(notification.id)} className="btn btn-sm btn-outline-success mr-2">
            Mark as Read
          </button>
        )}
        {notification.relatedSubscriptionId && (
          <Link to={`/subscriptions/${notification.relatedSubscriptionId}`} className="btn btn-sm btn-outline-info">
            View Subscription
          </Link>
        )}
      </div>
    </li>
  );
};

export default NotificationItem;