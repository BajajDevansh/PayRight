import React from 'react';
import { Link } from 'react-router-dom';
import PageLayout from '../components/layout/PageLayout';

const NotFoundPage = () => {
  return (
    <PageLayout title="404 - Page Not Found">
      <div className="text-center">
        <p>Oops! The page you are looking for does not exist.</p>
        <Link to="/dashboard" className="btn btn-primary">Go to Dashboard</Link>
      </div>
    </PageLayout>
  );
};

export default NotFoundPage;