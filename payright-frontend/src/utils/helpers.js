import { format, parseISO } from 'date-fns';

export const formatDate = (dateString, formatStr = 'MMM dd, yyyy') => {
  if (!dateString) return 'N/A';
  try {
    const date = typeof dateString === 'string' ? parseISO(dateString) : dateString;
    return format(date, formatStr);
  } catch (error) {
    console.error("Error formatting date:", dateString, error);
    return dateString; // Return original if parsing fails
  }
};

export const formatCurrency = (amount, currency = 'USD') => {
  if (amount === null || amount === undefined) return 'N/A';
  const numericAmount = typeof amount === 'string' ? parseFloat(amount) : amount;

  // Simple currency formatting, consider Intl.NumberFormat for production
  const options = { style: 'currency', currency: currency.replace('_DEMO', '') }; // Remove _DEMO for display
  try {
    return new Intl.NumberFormat('en-US', options).format(numericAmount);
  } catch (e) { // Fallback for unknown currency codes to Intl.NumberFormat
    return `${currency.replace('_DEMO', '')} ${numericAmount.toFixed(2)}`;
  }
};

// Example: Get first letter for an avatar placeholder
export const getInitials = (name = '') => {
  if (!name) return '?';
  const parts = name.split(' ');
  if (parts.length > 1) {
    return parts[0][0].toUpperCase() + parts[parts.length - 1][0].toUpperCase();
  }
  return name[0].toUpperCase();
};