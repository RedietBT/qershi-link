import React, { useState } from 'react';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { Layout } from '../../../common/components/Layout';
import { ChangePinModal } from '../../auth/components/ChangePinModal';
import { DashboardWelcomeHeader } from '../components/DashboardWelcomeHeader';
import { AdminGovernanceDeck } from '../components/AdminGovernanceDeck';
import { MemberOpsDeck } from '../components/MemberOpsDeck';
import { AccountManagementDeck } from '../components/AccountManagementDeck';
import { BankingOperationsDeck } from '../components/BankingOperationsDeck';
import { LendingCreditDeck } from '../components/LendingCreditDeck';
import { AccountingLedgerDeck } from '../components/AccountingLedgerDeck';
import { BatchOperationsDeck } from '../components/BatchOperationsDeck';

/**
 * Modular Core Banking Dashboard Page
 */
export const DashboardPage = () => {
  const user = useAuthStore((state) => state.user);
  const [isChangePinOpen, setIsChangePinOpen] = useState(false);

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn pb-12">
        {/* Welcome Header & Immediate User Actions */}
        <DashboardWelcomeHeader
          user={user}
          onOpenChangePin={() => setIsChangePinOpen(true)}
        />

        {/* 1. Super Admin & Platform Governance Matrix */}
        <AdminGovernanceDeck />

        {/* 2. Member Services & KYC Queue Matrix */}
        <MemberOpsDeck />

        {/* 3. Core Accounts, Branches & Product Factory Matrix */}
        <AccountManagementDeck />

        {/* 4. Banking Operations, Cash Desk & Drawer Matrix */}
        <BankingOperationsDeck />

        {/* 5. Credit, Underwriting & Loan Management Matrix */}
        <LendingCreditDeck />

        {/* 6. General Ledger & Financial Accounting Matrix */}
        <AccountingLedgerDeck />

        {/* 7. Core Batch & EOD Operations Matrix */}
        <BatchOperationsDeck />

        {/* Change Initial PIN Modal */}
        {isChangePinOpen && (
          <ChangePinModal
            initialMsisdn={user?.msisdn || ''}
            onClose={() => setIsChangePinOpen(false)}
          />
        )}
      </div>
    </Layout>
  );
};
