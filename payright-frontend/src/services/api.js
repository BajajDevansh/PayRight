import axios from 'axios';
import { API_BASE_URL, TOKEN_KEY } from '../utils/constants';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Function to set the auth token for all subsequent requests
export const setAuthToken = (token) => {
  if (token) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

// --- Auth Endpoints ---
export const registerUser = (userData) => apiClient.post('/auth/register', userData);
export const loginUser = (credentials) => apiClient.post('/auth/authenticate', credentials);

// --- Transaction Endpoints ---
export const fetchUserTransactions = () => apiClient.get('/transactions');
export const syncMockTransactions = () => apiClient.post('/transactions/sync-mock');

// --- Subscription Endpoints ---
export const fetchActiveSubscriptions = () => apiClient.get('/subscriptions');
export const fetchSubscriptionById = (id) => apiClient.get(`/subscriptions/${id}`);
export const updateSubscription = (id, subscriptionData) => apiClient.put(`/subscriptions/${id}`, subscriptionData);
export const deleteSubscriptionApi = (id) => apiClient.delete(`/subscriptions/${id}`); // Renamed to avoid conflict
export const checkSubscriptionRenewals = () => apiClient.post('/subscriptions/check-renewals'); // Admin/Test
export const getSubscriptionCancellationLink = (id) => apiClient.get(`/subscriptions/${id}/cancellation-link`);
export const markSubscriptionAsCancelledApi = (id) => apiClient.post(`/subscriptions/${id}/mark-cancelled`); // Renamed
export const toggleSubscriptionWalletPayment = (id, payload) => apiClient.post(`/subscriptions/${id}/toggle-wallet-payment`, payload);
export const processDueSubscriptionPayments = () => apiClient.post('/subscriptions/process-due-payments'); // Admin/Test

// --- Notification Endpoints ---
export const fetchUnreadNotifications = () => apiClient.get('/notifications/unread');
export const fetchAllNotifications = () => apiClient.get('/notifications');
export const markNotificationAsReadApi = (id) => apiClient.post(`/notifications/${id}/read`); // Renamed

// --- Suggestion Endpoints ---
export const fetchAlternativeSuggestions = (appName, category) => {
  let params = { appName };
  if (category) {
    params.category = category;
  }
  return apiClient.get('/suggestions/alternatives', { params });
};

// --- Wallet Endpoints ---
export const fetchMyWallet = () => apiClient.get('/wallet');
export const addFundsToWallet = (payload) => apiClient.post('/wallet/add-funds', payload);
export const fetchMyWalletTransactions = (page = 0, size = 20) => {
  return apiClient.get('/wallet/transactions', { params: { page, size } });
};

// Initialize token if present from localStorage on app load
const token = localStorage.getItem(TOKEN_KEY);
if (token) {
  setAuthToken(token);
}

export default apiClient; // Default export for convenience if needed, but named exports are used above