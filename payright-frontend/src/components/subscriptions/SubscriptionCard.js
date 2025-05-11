import React from 'react';
import { Link } from 'react-router-dom';
import { formatDate, formatCurrency } from '../../utils/helpers';
import './SubscriptionCard.css'; // Create this for styling

const SubscriptionCard = ({ subscription }) => {
  return (
    <div className={`card subscription-card ${!subscription.active ? 'inactive' : ''}`}>
      <div className="card-body">
        <div className="subscription-card-header">
          <h5 className="card-title">{subscription.name}</h5>
          <span className={`status-badge ${subscription.active ? 'active' : 'inactive'}`}>
            {subscription.active ? 'Active' : 'Cancelled'}
          </span>
        </div>
        <p className="card-text amount-frequency">
          {formatCurrency(subscription.amount, subscription.currency)} / {subscription.frequency}
        </p>
        <p className="card-text">
          Next Billing: {subscription.active ? formatDate(subscription.nextBillingDate) : 'N/A'}
        </p>
        {subscription.category && <p className="card-text category-tag">Category: {subscription.category}</p>}
        {!subscription.active && subscription.cancellationDate && (
            <p className="card-text cancelled-on">Cancelled on: {formatDate(subscription.cancellationDate)}</p>
        )}

        <Link to={`/subscriptions/${subscription.id}`} className="btn btn-sm btn-outline-primary mt-2">
          View Details
        </Link>
      </div>
    </div>
  );
};

export default SubscriptionCard;