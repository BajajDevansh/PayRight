import React from 'react';

const PageLayout = ({ title, children, actions }) => {
  return (
    <div className="container mt-3">
      <div className="page-header">
        <h1>{title}</h1>
        {actions && <div className="page-actions">{actions}</div>}
      </div>
      <div className="page-content">
        {children}
      </div>
    </div>
  );
};

export default PageLayout;