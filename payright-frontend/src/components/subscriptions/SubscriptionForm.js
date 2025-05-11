import React, { useState, useEffect } from 'react';
import AlertMessage from '../common/AlertMessage';

const SubscriptionForm = ({ initialData = {}, onSubmit, onCancel, loading, error }) => {
  const [formData, setFormData] = useState({
    name: '',
    amount: '',
    currency: 'USD_DEMO', // Default
    frequency: 'monthly', // Default
    nextBillingDate: '',
    category: '',
    notes: '',
    cancellationUrl: '',
    // active and autoRenews are typically toggles, not direct form fields here
    // payFromWallet is also a toggle handled separately
    ...initialData, // Spread initialData to pre-fill
  });

  useEffect(() => {
    // Format nextBillingDate for input type="date" if it exists
    const formattedInitialData = { ...initialData };
    if (initialData.nextBillingDate) {
      try {
        formattedInitialData.nextBillingDate = new Date(initialData.nextBillingDate).toISOString().split('T')[0];
      } catch (e) {
        console.error("Error formatting initial nextBillingDate: ", e);
        formattedInitialData.nextBillingDate = ''; // Fallback
      }
    } else {
        formattedInitialData.nextBillingDate = ''; // Ensure it's an empty string if null/undefined
    }
    setFormData(prev => ({ ...prev, ...formattedInitialData }));
  }, [initialData]);


  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const dataToSubmit = { ...formData };
    // Ensure amount is a number
    if (dataToSubmit.amount) {
        dataToSubmit.amount = parseFloat(dataToSubmit.amount);
    }
    onSubmit(dataToSubmit);
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <AlertMessage type="danger" message={typeof error === 'string' ? error : 'An error occurred.'} />}
      <div className="form-group">
        <label htmlFor="sub-name">Subscription Name</label>
        <input type="text" id="sub-name" name="name" className="form-control" value={formData.name} onChange={handleChange} required />
      </div>
      <div className="row">
        <div className="col-md-6">
          <div className="form-group">
            <label htmlFor="sub-amount">Amount</label>
            <input type="number" id="sub-amount" name="amount" className="form-control" value={formData.amount} onChange={handleChange} step="0.01" required />
          </div>
        </div>
        <div className="col-md-6">
          <div className="form-group">
            <label htmlFor="sub-currency">Currency</label>
            <select id="sub-currency" name="currency" className="form-control" value={formData.currency} onChange={handleChange}>
              <option value="USD_DEMO">USD (Demo)</option>
              <option value="EUR_DEMO">EUR (Demo)</option>
              {/* Add more as needed */}
            </select>
          </div>
        </div>
      </div>
      <div className="form-group">
        <label htmlFor="sub-frequency">Frequency</label>
        <select id="sub-frequency" name="frequency" className="form-control" value={formData.frequency} onChange={handleChange}>
          <option value="weekly">Weekly</option>
          <option value="monthly">Monthly</option>
          <option value="yearly">Yearly</option>
          <option value="one-time">One-time</option>
          <option value="other">Other</option>
        </select>
      </div>
      <div className="form-group">
        <label htmlFor="sub-nextBillingDate">Next Billing Date</label>
        <input type="date" id="sub-nextBillingDate" name="nextBillingDate" className="form-control" value={formData.nextBillingDate} onChange={handleChange} />
      </div>
      <div className="form-group">
        <label htmlFor="sub-category">Category</label>
        <input type="text" id="sub-category" name="category" className="form-control" value={formData.category} onChange={handleChange} placeholder="e.g., Entertainment, Productivity" />
      </div>
      <div className="form-group">
        <label htmlFor="sub-cancellationUrl">Cancellation URL (Optional)</label>
        <input type="url" id="sub-cancellationUrl" name="cancellationUrl" className="form-control" value={formData.cancellationUrl || ''} onChange={handleChange} placeholder="https://service.com/cancel" />
      </div>
      <div className="form-group">
        <label htmlFor="sub-notes">Notes (Optional)</label>
        <textarea id="sub-notes" name="notes" className="form-control" value={formData.notes || ''} onChange={handleChange} rows="3"></textarea>
      </div>
      <div className="form-actions d-flex justify-content-end mt-3">
        <button type="button" className="btn btn-secondary mr-2" onClick={onCancel} disabled={loading}>Cancel</button>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </form>
  );
};

export default SubscriptionForm;