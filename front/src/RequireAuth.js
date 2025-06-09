import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

// Component to protect routes that require authentication
const RequireAuth = () => {
  // Check if userId cookie exists
  const getCookie = (name) => {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? match[2] : null;
  };

  const userId = getCookie('userId');
  // If not authenticated, redirect to login
  return userId ? <Outlet /> : <Navigate to="/login" replace />;
};

export default RequireAuth;
