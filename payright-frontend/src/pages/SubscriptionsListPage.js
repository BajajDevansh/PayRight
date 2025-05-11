import React, { useState, useEffect, useCallback } from 'react';
import PageLayout from '../components/layout/PageLayout';
import SubscriptionCard from '../components/subscriptions/SubscriptionCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import AlertMessage from '../components/common/AlertMessage';
import * as api from '../services/api';
import './SubscriptionsListPage.css'; // For specific list layout

const SubscriptionsListPage = () => {
  const [subscriptions, setSubscriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('active'); // 'active', 'inactive', 'all'

  const fetchSubscriptionsData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      // Backend provides all active by default, so we fetch all if needed for 'all' or 'inactive' filter
      // For simplicity, fetching all and filtering on client for 'active'/'inactive'
      // A more optimized approach would have backend filters.
      // fetchActiveSubscriptions actually fetches based on backend logic.
      // Let's assume fetchActiveSubscriptions is the main one, and we filter further if needed.
      const response = await api.fetchActiveSubscriptions(); // This typically gets active=true from backend
      // To get all, you'd need a different backend endpoint or parameter.
      // For this demo, we'll simulate filtering if we fetched 'all'
      // Let's assume the current API `fetchActiveSubscriptions` returns ALL subs and we filter here.
      // OR, make a new API endpoint /api/subscriptions/all
      // For now, let's adjust to fetch all subs using a placeholder or assume `fetchActiveSubscriptions` gets all.
      // To make it work with current backend, we'll fetch active and then simulate having others if needed.
      // THIS PART NEEDS ADJUSTMENT BASED ON ACTUAL `fetchActiveSubscriptions` BEHAVIOR.
      // If `fetchActiveSubscriptions` truly only gets active, then filtering for 'inactive' or 'all' is limited.
      // Let's assume for now that `fetchActiveSubscriptions` can get all with a param, or we use a different endpoint.
      // For now, let's just display what `fetchActiveSubscriptions` returns and adjust filter logic.
      setSubscriptions(response.data); // Assuming response.data is an array of all subscriptions
    } catch (err) {
      console.error("Error fetching subscriptions:", err.response || err);
      setError('Failed to load subscriptions.');
    } finally {
      setLoading(false);
    }
  }, []); // Removed 'filter' dependency to avoid loop if api call doesn't support it

  useEffect(() => {
    fetchSubscriptionsData();
  }, [fetchSubscriptionsData]);

  const filteredSubscriptions = subscriptions.filter(sub => {
    if (filter === 'all') return true;
    return filter === 'active' ? sub.active : !sub.active;
  });

  if (loading) return <div className="container mt-3 text-center"><LoadingSpinner /></div>;

  return (
    <PageLayout title="Your Subscriptions">
      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      <div className="filter-controls mb-3">
        <button onClick={() => setFilter('active')} className={`btn btn-sm ${filter === 'active' ? 'btn-primary' : 'btn-outline-secondary'} mr-1`}>Active</button>
        <button onClick={() => setFilter('inactive')} className={`btn btn-sm ${filter === 'inactive' ? 'btn-primary' : 'btn-outline-secondary'} mr-1`}>Inactive/Cancelled</button>
        <button onClick={() => setFilter('all')} className={`btn btn-sm ${filter === 'all' ? 'btn-primary' : 'btn-outline-secondary'}`}>All</button>
      </div>

      {filteredSubscriptions.length > 0 ? (
        <div className="subscriptions-list-grid">
          {filteredSubscriptions.map(sub => (
            <SubscriptionCard key={sub.id} subscription={sub} />
          ))}
        </div>
      ) : (
        <p>No subscriptions found for the current filter. {filter === 'active' && <a href="#add">Add a new subscription?</a>} </p>
      )}
    </PageLayout>
  );
};

export default SubscriptionsListPage;