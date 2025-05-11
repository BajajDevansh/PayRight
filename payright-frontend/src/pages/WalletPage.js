import React, { useState, useEffect, useCallback } from 'react';
import PageLayout from '../components/layout/PageLayout';
import WalletSummary from '../components/wallet/WalletSummary';
import WalletTransactionItem from '../components/wallet/WalletTransactionItem';
import AddFundsModal from '../components/wallet/AddFundsModal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import AlertMessage from '../components/common/AlertMessage';
import * as api from '../services/api';

const WalletPage = () => {
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loadingWallet, setLoadingWallet] = useState(true);
  const [loadingTransactions, setLoadingTransactions] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const [isAddFundsModalOpen, setIsAddFundsModalOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 10; // Or make this configurable

  const fetchWalletData = useCallback(async () => {
    setLoadingWallet(true);
    try {
      const response = await api.fetchMyWallet();
      setWallet(response.data);
    } catch (err) {
      setError('Failed to load wallet details.');
      console.error("Fetch wallet error:", err.response || err);
    } finally {
      setLoadingWallet(false);
    }
  }, []);

  const fetchTransactionsData = useCallback(async (page) => {
    setLoadingTransactions(true);
    try {
      const response = await api.fetchMyWalletTransactions(page, pageSize);
      setTransactions(response.data.content); // Assuming backend returns Spring Page object
      setTotalPages(response.data.totalPages);
      setCurrentPage(response.data.number);
    } catch (err) {
      setError('Failed to load wallet transactions.');
      console.error("Fetch wallet transactions error:", err.response || err);
    } finally {
      setLoadingTransactions(false);
    }
  }, [pageSize]);

  useEffect(() => {
    fetchWalletData();
    fetchTransactionsData(currentPage);
  }, [fetchWalletData, fetchTransactionsData, currentPage]);

  const handleAddFunds = async (fundData) => {
    setActionLoading(true);
    setActionError('');
    try {
      const response = await api.addFundsToWallet(fundData);
      setWallet(response.data); // Update wallet with new balance
      setIsAddFundsModalOpen(false);
      fetchTransactionsData(0); // Refresh transactions to show the credit
      alert('Funds added successfully!');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to add funds.';
      setActionError(msg);
      console.error("Add funds error:", err.response || err);
    } finally {
      setActionLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
        setCurrentPage(newPage); // This will trigger useEffect to fetch new data
    }
  };

  if (loadingWallet) return <div className="container mt-3 text-center"><LoadingSpinner /></div>;

  return (
    <PageLayout title="My Demo Wallet">
      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      {wallet && (
        <WalletSummary wallet={wallet} onAddFundsClick={() => setIsAddFundsModalOpen(true)} />
      )}

      <div className="card mt-3">
        <div className="card-header">
          <h4>Transaction History</h4>
        </div>
        <div className="card-body">
          {loadingTransactions ? (
            <LoadingSpinner />
          ) : transactions.length > 0 ? (
            <>
              <ul className="list-group list-group-flush">
                {transactions.map(tx => (
                  <WalletTransactionItem key={tx.id} transaction={tx} />
                ))}
              </ul>
              {/* Pagination Controls */}
              {totalPages > 1 && (
                <nav aria-label="Page navigation" className="mt-3 d-flex justify-content-center">
                  <ul className="pagination">
                    <li className={`page-item ${currentPage === 0 ? 'disabled' : ''}`}>
                      <button className="page-link" onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 0}>
                        Previous
                      </button>
                    </li>
                    {[...Array(totalPages).keys()].map(pageNumber => (
                       <li key={pageNumber} className={`page-item ${pageNumber === currentPage ? 'active' : ''}`}>
                         <button className="page-link" onClick={() => handlePageChange(pageNumber)}>
                           {pageNumber + 1}
                         </button>
                       </li>
                    ))}
                    <li className={`page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}`}>
                      <button className="page-link" onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage >= totalPages - 1}>
                        Next
                      </button>
                    </li>
                  </ul>
                </nav>
              )}
            </>
          ) : (
            <p>No transactions yet.</p>
          )}
        </div>
      </div>

      {wallet && (
          <AddFundsModal
            isOpen={isAddFundsModalOpen}
            onClose={() => { setIsAddFundsModalOpen(false); setActionError('');}}
            onAddFunds={handleAddFunds}
            loading={actionLoading}
            error={actionError}
            currentCurrency={wallet.currency}
          />
      )}
    </PageLayout>
  );
};

export default WalletPage;