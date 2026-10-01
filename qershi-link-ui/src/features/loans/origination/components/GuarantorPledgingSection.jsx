import React, { useState } from 'react';
import {
  Users,
  Plus,
  Trash2,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Search,
  ShieldCheck,
  Lock,
  Wallet
} from 'lucide-react';
import { accountLedgerApi } from '../../../accounts/api/accountLedgerApi';
import { formatCurrency } from '../../../../common/utils/currency';

/**
 * Component for adding and validating member peer guarantors pledging savings liens.
 * Provides real-time account balance lookups and self-guarantee security checks.
 */
export const GuarantorPledgingSection = ({
  guarantors,
  onChange,
  applicantUserId,
  amountRequested
}) => {
  const [verifyingIndex, setVerifyingIndex] = useState(null);
  const [verificationResults, setVerificationResults] = useState({});

  const handleAdd = () => {
    onChange([
      ...guarantors,
      {
        guarantorUserId: '',
        guarantorName: '',
        guarantorPhone: '',
        savingsAccountNo: '',
        guaranteedAmount: ''
      }
    ]);
  };

  const handleRemove = (index) => {
    const updated = guarantors.filter((_, idx) => idx !== index);
    onChange(updated);
    const newResults = { ...verificationResults };
    delete newResults[index];
    setVerificationResults(newResults);
  };

  const handleChange = (index, field, value) => {
    const updated = [...guarantors];
    updated[index][field] = value;
    onChange(updated);

    // Invalidate previous verification if account or amount changed
    if (field === 'savingsAccountNo' || field === 'guaranteedAmount' || field === 'guarantorUserId') {
      if (verificationResults[index]) {
        const newResults = { ...verificationResults };
        delete newResults[index];
        setVerificationResults(newResults);
      }
    }
  };

  const verifyGuarantorAccount = async (index) => {
    const item = guarantors[index];
    if (!item.savingsAccountNo?.trim()) return;

    setVerifyingIndex(index);
    try {
      const res = await accountLedgerApi.getAccountByNo(item.savingsAccountNo.trim());
      const account = res.data || res;

      if (!account || !account.accountNo) {
        setVerificationResults((prev) => ({
          ...prev,
          [index]: { status: 'NOT_FOUND', message: 'Account not found in core registry' }
        }));
        return;
      }

      const available = Number(account.availableBalance || 0);
      const pledged = Number(item.guaranteedAmount || 0);
      const isSelfPledge = applicantUserId && account.userId && String(applicantUserId).trim() === String(account.userId).trim();

      // Auto-fill guarantor info if available
      if (account.fullName && !item.guarantorName) {
        const updated = [...guarantors];
        updated[index].guarantorName = account.fullName;
        if (account.phoneNumber && !item.guarantorPhone) {
          updated[index].guarantorPhone = account.phoneNumber;
        }
        if (account.userId && !item.guarantorUserId) {
          updated[index].guarantorUserId = account.userId;
        }
        onChange(updated);
      }

      if (isSelfPledge) {
        setVerificationResults((prev) => ({
          ...prev,
          [index]: {
            status: 'SELF_PLEDGE',
            available,
            message: 'Borrower cannot act as their own guarantor'
          }
        }));
      } else if (account.status !== 'ACTIVE') {
        setVerificationResults((prev) => ({
          ...prev,
          [index]: {
            status: 'INACTIVE',
            available,
            message: `Account is ${account.status} (must be ACTIVE)`
          }
        }));
      } else if (pledged > 0 && available < pledged) {
        setVerificationResults((prev) => ({
          ...prev,
          [index]: {
            status: 'INSUFFICIENT',
            available,
            message: `Insufficient unencumbered balance (${formatCurrency(available)} available vs ${formatCurrency(pledged)} pledged)`
          }
        }));
      } else {
        setVerificationResults((prev) => ({
          ...prev,
          [index]: {
            status: 'VERIFIED',
            available,
            message: `Verified! Available: ${formatCurrency(available)}`
          }
        }));
      }
    } catch (err) {
      setVerificationResults((prev) => ({
        ...prev,
        [index]: {
          status: 'ERROR',
          message: err.response?.data?.message || 'Failed to verify account'
        }
      }));
    } finally {
      setVerifyingIndex(null);
    }
  };

  const totalPledged = guarantors.reduce(
    (sum, g) => sum + (Number(g.guaranteedAmount) || 0),
    0
  );
  const loanReq = Number(amountRequested) || 0;
  const coveragePct = loanReq > 0 ? Math.min(Math.round((totalPledged / loanReq) * 100), 100) : 0;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Users className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-sm font-semibold text-slate-200">
              Member Peer Guarantors & Savings Liens
            </h4>
            <p className="text-xs text-slate-400">
              Pledge peer members' unencumbered savings as legal collateral liens.
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={handleAdd}
          className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-emerald-600/20 text-emerald-300 border border-emerald-500/30 hover:bg-emerald-600/30 transition-colors"
        >
          <Plus className="w-3.5 h-3.5" />
          Add Guarantor
        </button>
      </div>

      {/* Coverage Summary Bar */}
      {loanReq > 0 && (
        <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between text-xs">
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 text-slate-300 font-medium">
              <Lock className="w-3.5 h-3.5 text-amber-400" />
              Total Pledged Lien:
              <span className="text-emerald-400 font-bold ml-1">
                {formatCurrency(totalPledged)}
              </span>
            </div>
            <span className="text-slate-500">•</span>
            <span className="text-slate-400">
              Requested: {formatCurrency(loanReq)}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <div className="w-24 bg-slate-800 rounded-full h-2 overflow-hidden">
              <div
                className={`h-full transition-all duration-300 ${
                  coveragePct >= 100
                    ? 'bg-emerald-500'
                    : coveragePct >= 50
                    ? 'bg-amber-500'
                    : 'bg-rose-500'
                }`}
                style={{ width: `${coveragePct}%` }}
              />
            </div>
            <span
              className={`font-semibold ${
                coveragePct >= 100
                  ? 'text-emerald-400'
                  : coveragePct >= 50
                  ? 'text-amber-400'
                  : 'text-rose-400'
              }`}
            >
              {coveragePct}%
            </span>
          </div>
        </div>
      )}

      {/* Guarantors List */}
      {guarantors.length === 0 ? (
        <div className="text-center py-6 border border-dashed border-slate-800 rounded-xl bg-slate-900/30">
          <ShieldCheck className="w-8 h-8 text-slate-600 mx-auto mb-1.5" />
          <p className="text-xs text-slate-400 font-medium">
            No peer guarantors pledged yet.
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5">
            Add members with active savings to satisfy Tier-1 peer-lending collateral requirements.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {guarantors.map((item, index) => {
            const verification = verificationResults[index];
            const isVerifying = verifyingIndex === index;

            return (
              <div
                key={index}
                className="p-4 bg-slate-900/80 border border-slate-800 rounded-xl space-y-3 relative group"
              >
                <div className="flex items-center justify-between pb-2 border-b border-slate-800/60">
                  <div className="flex items-center gap-2">
                    <span className="w-5 h-5 rounded-full bg-slate-800 text-slate-300 text-xs flex items-center justify-center font-bold">
                      {index + 1}
                    </span>
                    <span className="text-xs font-semibold text-slate-300">
                      Guarantor #{index + 1} {item.guarantorName && `(${item.guarantorName})`}
                    </span>
                  </div>

                  <button
                    type="button"
                    onClick={() => handleRemove(index)}
                    className="text-slate-500 hover:text-rose-400 transition-colors p-1"
                    title="Remove Guarantor"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[11px] font-medium text-slate-400 mb-1">
                      Savings Account No *
                    </label>
                    <div className="relative">
                      <input
                        type="text"
                        value={item.savingsAccountNo}
                        onChange={(e) =>
                          handleChange(index, 'savingsAccountNo', e.target.value)
                        }
                        placeholder="e.g. 1001-SAV-000123"
                        className="w-full bg-slate-950/80 border border-slate-800 rounded-lg pl-3 pr-20 py-2 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 font-mono"
                        required
                      />
                      <button
                        type="button"
                        onClick={() => verifyGuarantorAccount(index)}
                        disabled={!item.savingsAccountNo?.trim() || isVerifying}
                        className="absolute right-1 top-1 bottom-1 px-2.5 bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-400 border border-emerald-500/30 rounded-md text-[11px] font-medium flex items-center gap-1 disabled:opacity-40 transition-colors"
                      >
                        <Search className="w-3 h-3" />
                        {isVerifying ? 'Checking...' : 'Verify'}
                      </button>
                    </div>
                  </div>

                  <div>
                    <label className="block text-[11px] font-medium text-slate-400 mb-1">
                      Guaranteed Pledge Amount (ETB) *
                    </label>
                    <div className="relative">
                      <input
                        type="number"
                        min="1"
                        step="any"
                        value={item.guaranteedAmount}
                        onChange={(e) =>
                          handleChange(index, 'guaranteedAmount', e.target.value)
                        }
                        placeholder="e.g. 10000"
                        className="w-full bg-slate-950/80 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 font-mono"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[11px] font-medium text-slate-400 mb-1">
                      Guarantor Full Name
                    </label>
                    <input
                      type="text"
                      value={item.guarantorName}
                      onChange={(e) =>
                        handleChange(index, 'guarantorName', e.target.value)
                      }
                      placeholder="Auto-populated or enter name"
                      className="w-full bg-slate-950/80 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-medium text-slate-400 mb-1">
                      Guarantor Phone
                    </label>
                    <input
                      type="text"
                      value={item.guarantorPhone}
                      onChange={(e) =>
                        handleChange(index, 'guarantorPhone', e.target.value)
                      }
                      placeholder="e.g. +251911223344"
                      className="w-full bg-slate-950/80 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 font-mono"
                    />
                  </div>
                </div>

                {/* Verification Feedback Banner */}
                {verification && (
                  <div
                    className={`p-2.5 rounded-lg border text-xs flex items-center justify-between ${
                      verification.status === 'VERIFIED'
                        ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-300'
                        : verification.status === 'SELF_PLEDGE' || verification.status === 'INSUFFICIENT'
                        ? 'bg-rose-500/10 border-rose-500/20 text-rose-300'
                        : 'bg-amber-500/10 border-amber-500/20 text-amber-300'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      {verification.status === 'VERIFIED' ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400 flex-shrink-0" />
                      ) : verification.status === 'SELF_PLEDGE' || verification.status === 'INSUFFICIENT' ? (
                        <XCircle className="w-4 h-4 text-rose-400 flex-shrink-0" />
                      ) : (
                        <AlertTriangle className="w-4 h-4 text-amber-400 flex-shrink-0" />
                      )}
                      <span>{verification.message}</span>
                    </div>

                    {verification.available !== undefined && (
                      <span className="font-mono font-semibold text-[11px] opacity-90">
                        Avail: {formatCurrency(verification.available)}
                      </span>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
