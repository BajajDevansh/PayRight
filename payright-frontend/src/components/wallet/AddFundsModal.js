import React, { useState } from 'react';
import Modal from '../common/Modal';
import AlertMessage from '../common/AlertMessage';

const AddFundsModal = ({ isOpen, onClose, onAddFunds, loading, error, currentCurrency = 'USD_DEMO' }) => {
  const [amount, setAmount] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!amount || parseFloat(amount) <= 0) {
      // Basic validation, more can be added
      alert('Please enter a valid positive amount.');
      return;
    }
    onAddFunds({ amount: parseFloat(amount), currency: currentCurrency });
    // Optionally close modal on successful submission initiation or let parent handle
    // if (!error) setAmount(''); // Clear amount if no immediate error
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Funds to Demo Wallet">
      <form onSubmit={handleSubmit}>
        {error && <AlertMessage type="danger" message={typeof error === 'string' ? error : 'Failed to add funds.'} />}
        <div className="form-group">
          <label htmlFor="fund-amount">Amount ({currentCurrency.replace('_DEMO','')})</label>
          <input
            type="number"
            id="fund-amount"
            className="form-control"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="e.g., 50.00"
            step="0.01"
            min="0.01"
            required
          />
        </div>
        <div className="modal-footer d-flex justify-content-end">
          <button type="button" className="btn btn-secondary mr-2" onClick={onClose} disabled={loading}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Adding...' : 'Add Funds'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default AddFundsModal;