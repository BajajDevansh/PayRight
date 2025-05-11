import React from 'react';
import { formatCurrency } from '../../utils/helpers';
import './WalletSummary.css';

const WalletSummary = ({ wallet, onAddFundsClick }) => {
  if (!wallet) {
    return <p>Loading wallet information...</p>;
  }

  return (
    <div className="wallet-summary-card card">
      <div className="card-body">
        <h5 className="card-title">Demo Wallet Balance</h5>
        <p className="wallet-balance">{formatCurrency(wallet.balance, wallet.currency)}</p>
        <p className="wallet-currency-note">Currency: {wallet.currency}</p>
        {onAddFundsClick && (
          <button onClick={onAddFundsClick} className="btn btn-success btn-sm mt-2">
            Add Funds
          </button>
        )}
      </div>
    </div>
  );
};

export default WalletSummary;