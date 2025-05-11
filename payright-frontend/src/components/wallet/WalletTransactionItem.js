import React from 'react';
import { formatDate, formatCurrency } from '../../utils/helpers';
import { Link } from 'react-router-dom';
import './WalletTransactionItem.css';

const WalletTransactionItem = ({ transaction }) => {
  const isCredit = transaction.type === 'CREDIT';
  return (
    <li className={`list-group-item wallet-transaction-item ${isCredit ? 'credit' : 'debit'}`}>
      <div className="transaction-main">
        <span className={`transaction-type-indicator ${isCredit ? 'credit' : 'debit'}`}>
          {isCredit ? '+' : '-'}
        </span>
        <div className="transaction-details">
          <span className="transaction-description">{transaction.description}</span>
          <small className="transaction-date">{formatDate(transaction.transactionDate, 'MMM dd, yyyy, HH:mm')}</small>
        </div>
      </div>
      <div className="transaction-amount">
        {formatCurrency(transaction.amount, transaction.currency)}
      </div>
      {transaction.relatedSubscriptionId && (
         <Link to={`/subscriptions/${transaction.relatedSubscriptionId}`} className="transaction-link btn btn-sm btn-outline-info">View Sub</Link>
      )}
    </li>
  );
};

export default WalletTransactionItem;