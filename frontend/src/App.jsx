import { Navigate, Route, Routes } from 'react-router-dom';
import { useSelector } from 'react-redux';

import CustomerLayout from './components/CustomerLayout.jsx';
import EmployeeLayout from './components/EmployeeLayout.jsx';
import ProtectedRoute from './routes/ProtectedRoute.jsx';

import CustomerLoginPage from './pages/customer/CustomerLoginPage.jsx';
import CustomerRegisterPage from './pages/customer/CustomerRegisterPage.jsx';
import ForgotPasswordPage from './pages/customer/ForgotPasswordPage.jsx';
import EmployeeLoginPage from './pages/employee/EmployeeLoginPage.jsx';
import EmployeeForgotPasswordPage from './pages/employee/EmployeeForgotPasswordPage.jsx';

import CustomerDashboardPage from './pages/customer/CustomerDashboardPage.jsx';
import ServicesPage from './pages/customer/ServicesPage.jsx';
import CustomerProfilePage from './pages/customer/CustomerProfilePage.jsx';
import ChangePasswordPage from './pages/customer/ChangePasswordPage.jsx';
import TransferPage from './pages/customer/TransferPage.jsx';
import TransactionsPage from './pages/customer/TransactionsPage.jsx';
import MiniStatementPage from './pages/customer/MiniStatementPage.jsx';
import BeneficiariesPage from './pages/customer/BeneficiariesPage.jsx';
import NomineesPage from './pages/customer/NomineesPage.jsx';
import CardsPage from './pages/customer/CardsPage.jsx';
import LoansPage from './pages/customer/LoansPage.jsx';
import FixedDepositPage from './pages/customer/FixedDepositPage.jsx';
import RecurringDepositPage from './pages/customer/RecurringDepositPage.jsx';
import TicketsPage from './pages/customer/TicketsPage.jsx';
import NotificationsPage from './pages/customer/NotificationsPage.jsx';

import EmployeeDashboardPage from './pages/employee/EmployeeDashboardPage.jsx';
import PendingCustomersPage from './pages/employee/PendingCustomersPage.jsx';
import EmployeesPage from './pages/employee/EmployeesPage.jsx';
import CardQueuePage from './pages/employee/CardQueuePage.jsx';
import LoanQueuePage from './pages/employee/LoanQueuePage.jsx';
import CashierPage from './pages/employee/CashierPage.jsx';
import TicketQueuePage from './pages/employee/TicketQueuePage.jsx';
import AuditPage from './pages/employee/AuditPage.jsx';

import {
  selectAuth,
  selectIsCustomer,
  selectIsEmployee,
} from './features/auth/authSlice.js';

function HomeRedirect() {
  const { isAuthenticated } = useSelector(selectAuth);
  const isCustomer = useSelector(selectIsCustomer);
  const isEmployee = useSelector(selectIsEmployee);
  if (!isAuthenticated) {
    return <Navigate to="/customer/login" replace />;
  }
  if (isCustomer) return <Navigate to="/customer/dashboard" replace />;
  if (isEmployee) return <Navigate to="/employee/dashboard" replace />;
  return <Navigate to="/customer/login" replace />;
}

export default function App() {
  return (
    <Routes>
      {/* Public auth */}
      <Route path="/customer/login" element={<CustomerLoginPage />} />
      <Route path="/customer/register" element={<CustomerRegisterPage />} />
      <Route path="/customer/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/employee/login" element={<EmployeeLoginPage />} />
      <Route
        path="/employee/forgot-password"
        element={<EmployeeForgotPasswordPage />}
      />

      {/* Customer area */}
      <Route element={<ProtectedRoute customerOnly />}>
        <Route element={<CustomerLayout />}>
          <Route path="/customer/dashboard" element={<CustomerDashboardPage />} />
          <Route path="/customer/services" element={<ServicesPage />} />
          <Route path="/customer/services/profile" element={<CustomerProfilePage />} />
          <Route
            path="/customer/services/change-password"
            element={<ChangePasswordPage />}
          />
          <Route path="/customer/services/transfer" element={<TransferPage />} />
          <Route
            path="/customer/services/transactions"
            element={<TransactionsPage />}
          />
          <Route
            path="/customer/services/mini-statement"
            element={<MiniStatementPage />}
          />
          <Route
            path="/customer/services/beneficiaries"
            element={<BeneficiariesPage />}
          />
          <Route path="/customer/services/nominees" element={<NomineesPage />} />
          <Route path="/customer/services/cards" element={<CardsPage />} />
          <Route path="/customer/services/loans" element={<LoansPage />} />
          <Route path="/customer/services/fd" element={<FixedDepositPage />} />
          <Route path="/customer/services/rd" element={<RecurringDepositPage />} />
          <Route path="/customer/services/tickets" element={<TicketsPage />} />
          <Route path="/customer/notifications" element={<NotificationsPage />} />
        </Route>
      </Route>

      {/* Employee area */}
      <Route element={<ProtectedRoute employeeOnly />}>
        <Route element={<EmployeeLayout />}>
          <Route path="/employee/dashboard" element={<EmployeeDashboardPage />} />
          <Route
            path="/employee/customers/pending"
            element={
              <ProtectedRoute
                employeeOnly
                requireAnyRole={['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT']}
              >
                <PendingCustomersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/employee/employees"
            element={
              <ProtectedRoute
                employeeOnly
                requireAnyRole={['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']}
              >
                <EmployeesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/employee/cards/queue"
            element={
              <ProtectedRoute
                employeeOnly
                requireAnyRole={[
                  'ROLE_CARD_OFFICER',
                  'ROLE_SUPER_ADMIN',
                  'ROLE_ADMIN',
                ]}
              >
                <CardQueuePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/employee/loans/queue"
            element={
              <ProtectedRoute
                employeeOnly
                requireAnyRole={[
                  'ROLE_LOAN_OFFICER',
                  'ROLE_SUPER_ADMIN',
                  'ROLE_ADMIN',
                ]}
              >
                <LoanQueuePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/employee/cashier"
            element={
              <ProtectedRoute
                employeeOnly
                requireAnyRole={['ROLE_CASHIER', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN']}
              >
                <CashierPage />
              </ProtectedRoute>
            }
          />
          <Route path="/employee/tickets" element={<TicketQueuePage />} />
          <Route
            path="/employee/audit"
            element={
              <ProtectedRoute
                employeeOnly
                requireAnyRole={['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']}
              >
                <AuditPage />
              </ProtectedRoute>
            }
          />
        </Route>
      </Route>

      <Route path="/" element={<HomeRedirect />} />
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  );
}
