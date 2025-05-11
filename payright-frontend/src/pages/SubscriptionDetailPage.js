import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import PageLayout from '../components/layout/PageLayout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import AlertMessage from '../components/common/AlertMessage';
import Modal from '../components/common/Modal';
import SubscriptionForm from '../components/subscriptions/SubscriptionForm';
import * as api from '../services/api';
import { formatDate, formatCurrency } from '../utils/helpers';
import './SubscriptionDetailPage.css'; // Create for styling

const SubscriptionDetailPage = () => {
  const { id: subscriptionId } = useParams();
  const navigate = useNavigate();
  const [subscription, setSubscription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState(''); // For errors within modals/actions
  const [actionLoading, setActionLoading] = useState(false);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isCancelConfirmModalOpen, setIsCancelConfirmModalOpen] = useState(false);
  const [cancellationLinkInfo, setCancellationLinkInfo] = useState(null);
  const [isDeleteConfirmModalOpen, setIsDeleteConfirmModalOpen] = useState(false);
  const [alternativeSuggestions, setAlternativeSuggestions] = useState([]);
  const [loadingSuggestions, setLoadingSuggestions] = useState(false);


  const fetchSubscription = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await api.fetchSubscriptionById(subscriptionId);
      setSubscription(response.data); // Assuming response.data is SubscriptionResponseDTO
    } catch (err) {
      console.error("Fetch subscription detail error:", err.response || err);
      setError('Failed to load subscription details. It might have been deleted or you may not have access.');
    } finally {
      setLoading(false);
    }
  }, [subscriptionId]);

  useEffect(() => {
    fetchSubscription();
  }, [fetchSubscription]);

  const handleUpdateSubscription = async (formData) => {
    setActionLoading(true);
    setActionError('');
    try {
      const dataToSubmit = {
          ...subscription, // retain fields like id, userId, active, autoRenews, payFromWallet
          ...formData // override with form data
      };
      // Ensure boolean fields are actually boolean if coming from form elements
      dataToSubmit.active = subscription.active; // Keep original active status unless specifically changed by another toggle
      dataToSubmit.autoRenews = subscription.autoRenews; // Same for autoRenews
      dataToSubmit.payFromWallet = subscription.payFromWallet; // Same for payFromWallet

      const response = await api.updateSubscription(subscriptionId, dataToSubmit);
      setSubscription(response.data);
      setIsEditModalOpen(false);
      alert('Subscription updated successfully!');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Failed to update subscription.';
      setActionError(msg);
      console.error("Update subscription error:", err.response || err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleGetCancellationLink = async () => {
    setActionLoading(true);
    setActionError('');
    try {
      const response = await api.getSubscriptionCancellationLink(subscriptionId);
      setCancellationLinkInfo(response.data); // { url, message, subscriptionName }
      if (response.data.url) {
        // Open in new tab immediately if URL is present
        window.open(response.data.url, '_blank', 'noopener,noreferrer');
      }
      setIsCancelConfirmModalOpen(true); // Always open modal to show message / confirm
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to get cancellation link.';
      setActionError(msg);
      console.error("Get cancellation link error:", err.response || err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleMarkAsCancelled = async () => {
    setActionLoading(true);
    setActionError('');
    try {
      const response = await api.markSubscriptionAsCancelledApi(subscriptionId);
      setSubscription(response.data);
      setIsCancelConfirmModalOpen(false);
      alert('Subscription marked as cancelled.');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to mark as cancelled.';
      setActionError(msg);
      console.error("Mark as cancelled error:", err.response || err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteSubscription = async () => {
    setActionLoading(true);
    setActionError('');
    try {
      await api.deleteSubscriptionApi(subscriptionId);
      alert('Subscription deleted successfully.');
      navigate('/subscriptions');
    } catch (err) {
        const msg = err.response?.data?.message || 'Failed to delete subscription.';
        setActionError(msg);
        console.error("Delete subscription error:", err.response || err);
        // Keep modal open if delete fails
    } finally {
        setActionLoading(false);
        if (!actionError) setIsDeleteConfirmModalOpen(false); // Close only if successful
    }
  };

  const handleToggleWalletPayment = async () => {
    if (!subscription) return;
    setActionLoading(true);
    setActionError('');
    try {
      const response = await api.toggleSubscriptionWalletPayment(subscriptionId, { enable: !subscription.payFromWallet });
      setSubscription(response.data); // Update with new state
      alert(`Payment from wallet ${response.data.payFromWallet ? 'enabled' : 'disabled'}.`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to toggle wallet payment.';
      setActionError(msg);
      console.error("Toggle wallet payment error:", err.response || err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleFetchSuggestions = async () => {
    if (!subscription) return;
    setLoadingSuggestions(true);
    setAlternativeSuggestions([]);
    setActionError('');
    try {
        const response = await api.fetchAlternativeSuggestions(subscription.name, subscription.category);
        setAlternativeSuggestions(response.data || []);
        if (!response.data || response.data.length === 0) {
            setActionError('No alternative suggestions found for this subscription.');
        }
    } catch (err) {
        const msg = err.response?.data?.message || 'Failed to fetch AI suggestions.';
        setActionError(msg);
        console.error("Fetch suggestions error:", err.response || err);
    } finally {
        setLoadingSuggestions(false);
    }
  };


  if (loading) return <div className="container mt-3 text-center"><LoadingSpinner /></div>;
  if (error) return <PageLayout title="Error"><AlertMessage type="danger" message={error} /></PageLayout>;
  if (!subscription) return <PageLayout title="Subscription Not Found"><AlertMessage type="info" message="The subscription could not be found." /></PageLayout>;

  return (
    <PageLayout title={subscription.name} actions={
        <button onClick={() => setIsEditModalOpen(true)} className="btn btn-primary btn-sm">Edit</button>
    }>
      <div className="subscription-detail-grid">
        {/* Main Info Column */}
        <div className="detail-card main-info card">
          <div className="card-body">
            <h4 className="card-title">{subscription.name}
                <span className={`status-badge ml-2 ${subscription.active ? 'active' : 'inactive'}`}>
                    {subscription.active ? 'Active' : 'Cancelled'}
                </span>
            </h4>
            <p><strong>Amount:</strong> {formatCurrency(subscription.amount, subscription.currency)} / {subscription.frequency}</p>
            <p><strong>Next Billing:</strong> {subscription.active ? formatDate(subscription.nextBillingDate) : 'N/A'}</p>
            <p><strong>Start Date:</strong> {formatDate(subscription.startDate)}</p>
            {subscription.category && <p><strong>Category:</strong> {subscription.category}</p>}
            {subscription.notes && <p><strong>Notes:</strong> {subscription.notes}</p>}
            {subscription.cancellationUrl && <p><strong>Official Cancel URL:</strong> <a href={subscription.cancellationUrl} target="_blank" rel="noopener noreferrer">Link</a></p>}
            {!subscription.active && subscription.cancellationDate && <p><strong>Cancelled On:</strong> {formatDate(subscription.cancellationDate)}</p>}
          </div>
        </div>

        {/* Actions Column */}
        <div className="detail-card actions-card card">
            <div className="card-body">
                <h5 className="card-title">Manage Subscription</h5>
                <button
                    onClick={handleToggleWalletPayment}
                    className={`btn btn-block mb-2 ${subscription.payFromWallet ? 'btn-outline-warning' : 'btn-outline-success'}`}
                    disabled={actionLoading || !subscription.active}
                    title={!subscription.active ? "Cannot change wallet payment for inactive subscription" : ""}
                >
                    {actionLoading ? 'Updating...' : (subscription.payFromWallet ? 'Disable Wallet Payment' : 'Enable Wallet Payment')}
                </button>
                {subscription.active && (
                    <button onClick={handleGetCancellationLink} className="btn btn-warning btn-block mb-2" disabled={actionLoading}>
                        {actionLoading ? 'Processing...' : 'Cancel Subscription'}
                    </button>
                )}
                 <button onClick={handleFetchSuggestions} className="btn btn-info btn-block mb-2" disabled={loadingSuggestions}>
                    {loadingSuggestions ? 'Getting Suggestions...' : 'Get AI Alternatives'}
                </button>
                <button onClick={() => setIsDeleteConfirmModalOpen(true)} className="btn btn-danger btn-block" disabled={actionLoading}>
                    Delete Subscription
                </button>
                 {actionError && !isEditModalOpen && !isCancelConfirmModalOpen && !isDeleteConfirmModalOpen &&  ( // Show general action error if no modal is open
                    <AlertMessage type="danger" message={actionError} onClose={() => setActionError('')} />
                )}
            </div>
        </div>

         {/* AI Suggestions Column */}
        {(alternativeSuggestions.length > 0 || loadingSuggestions) && (
            <div className="detail-card suggestions-card card">
                <div className="card-body">
                    <h5 className="card-title">AI Alternative Suggestions</h5>
                    {loadingSuggestions && <LoadingSpinner />}
                    {!loadingSuggestions && alternativeSuggestions.length === 0 && !actionError && <p>No suggestions available.</p>}
                    {/* Display error specific to suggestions if it occurred */}
                    {!loadingSuggestions && alternativeSuggestions.length === 0 && actionError.includes('AI suggestions') &&
                        <AlertMessage type="info" message={actionError} onClose={() => setActionError('')} />
                    }
                    {!loadingSuggestions && alternativeSuggestions.length > 0 && (
                        <ul className="list-group">
                            {alternativeSuggestions.map((s, index) => (
                                <li key={index} className="list-group-item suggestion-item">
                                    <strong>{s.name}</strong> ({s.type}) - {s.price ? formatCurrency(s.price, subscription.currency) : 'Free'}
                                    <p><small>{s.description}</small></p>
                                    {s.link && <a href={s.link} target="_blank" rel="noopener noreferrer" className="btn btn-sm btn-outline-info">Visit</a>}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        )}

      </div>


      {/* Edit Modal */}
      <Modal isOpen={isEditModalOpen} onClose={() => setIsEditModalOpen(false)} title="Edit Subscription">
        <SubscriptionForm
          initialData={subscription}
          onSubmit={handleUpdateSubscription}
          onCancel={() => setIsEditModalOpen(false)}
          loading={actionLoading}
          error={actionError}
        />
      </Modal>

      {/* Cancellation Info/Confirm Modal */}
      <Modal
        isOpen={isCancelConfirmModalOpen}
        onClose={() => setIsCancelConfirmModalOpen(false)}
        title={cancellationLinkInfo?.subscriptionName ? `Cancel ${cancellationLinkInfo.subscriptionName}` : "Cancel Subscription"}
      >
        {actionError && <AlertMessage type="danger" message={actionError} onClose={() => setActionError('')}/>}
        <p>{cancellationLinkInfo?.message}</p>
        {cancellationLinkInfo?.url && (
          <p>If the page didn't open, you can use this link: <a href={cancellationLinkInfo.url} target="_blank" rel="noopener noreferrer">{cancellationLinkInfo.url}</a></p>
        )}
        <p className="mt-3">Once you have completed the cancellation on the provider's website, please confirm below.</p>
        <div className="modal-footer d-flex justify-content-between">
          <button className="btn btn-secondary" onClick={() => setIsCancelConfirmModalOpen(false)} disabled={actionLoading}>Close</button>
          <button className="btn btn-success" onClick={handleMarkAsCancelled} disabled={actionLoading}>
            {actionLoading ? 'Confirming...' : "I've Cancelled It"}
          </button>
        </div>
      </Modal>

      {/* Delete Confirmation Modal */}
       <Modal
        isOpen={isDeleteConfirmModalOpen}
        onClose={() => setIsDeleteConfirmModalOpen(false)}
        title="Confirm Deletion"
      >
        {actionError && <AlertMessage type="danger" message={actionError} onClose={() => setActionError('')}/>}
        <p>Are you sure you want to delete the subscription "<strong>{subscription.name}</strong>"? This action cannot be undone.</p>
        <div className="modal-footer d-flex justify-content-between">
          <button className="btn btn-secondary" onClick={() => setIsDeleteConfirmModalOpen(false)} disabled={actionLoading}>Cancel</button>
          <button className="btn btn-danger" onClick={handleDeleteSubscription} disabled={actionLoading}>
            {actionLoading ? 'Deleting...' : "Yes, Delete"}
          </button>
        </div>
      </Modal>

    </PageLayout>
  );
};

export default SubscriptionDetailPage;