import React, { useState, useEffect, useCallback } from 'react';
import {
  BadgeCheck, Plus, RefreshCw, ArrowRightLeft, Award, TrendingUp,
  CheckCircle2, AlertCircle, Clock, FileText, ShieldCheck, Hash, Phone,
} from 'lucide-react';
import { shareCapitalApi } from '../api/shareCapitalApi';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { formatCurrency } from '../../../common/utils/currency';

/**
 * Share Capital Management Dashboard — Day 5
 * Properly modularized: API calls via shareCapitalApi, auth via PermissionGuard.
 */
export const ShareCapitalPage = () => {
  const user = useAuthStore((s) => s.user);
  const memberId = user?.id || user?.userId;

  const [summary, setSummary] = useState(null);
  const [certificates, setCertificates] = useState([]);
  const [pendingTransfers, setPendingTransfers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Phone lookup
  const [phoneSearch, setPhoneSearch] = useState('');
  const [phoneResult, setPhoneResult] = useState(null);
  const [isSearching, setIsSearching] = useState(false);

  // Purchase modal
  const [showPurchaseModal, setShowPurchaseModal] = useState(false);
  const [purchaseForm, setPurchaseForm] = useState({ shareCount: 5, sourceAccountNo: '' });
  const [isPurchasing, setIsPurchasing] = useState(false);

  // Transfer modal
  const [showTransferModal, setShowTransferModal] = useState(false);
  const [transferForm, setTransferForm] = useState({
    fromMemberId: '', toMemberId: '', certificateId: '', shareCount: 1, transferPrice: '',
  });
  const [isTransferring, setIsTransferring] = useState(false);

  const fetchData = useCallback(async () => {
    if (!memberId) return;
    setIsLoading(true);
    setError(null);
    try {
      const [summaryData, pendingData] = await Promise.allSettled([
        shareCapitalApi.getMemberSummary(memberId),
        shareCapitalApi.getPendingTransfers(),
      ]);
      if (summaryData.status === 'fulfilled') {
        setSummary(summaryData.value);
        setCertificates(summaryData.value?.certificates || []);
      }
      if (pendingData.status === 'fulfilled') {
        setPendingTransfers(Array.isArray(pendingData.value) ? pendingData.value : []);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load share capital data.');
    } finally {
      setIsLoading(false);
    }
  }, [memberId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handlePhoneSearch = async (e) => {
    e.preventDefault();
    if (!phoneSearch.trim()) return;
    setIsSearching(true);
    setPhoneResult(null);
    setError(null);
    try {
      const result = await shareCapitalApi.lookupByPhone(phoneSearch.trim());
      setPhoneResult(result);
    } catch (err) {
      setError(err.response?.data?.message || 'No share account found for that phone number.');
    } finally {
      setIsSearching(false);
    }
  };

  const handlePurchase = async (e) => {
    e.preventDefault();
    setIsPurchasing(true);
    setError(null);
    try {
      const res = await shareCapitalApi.purchaseShares(memberId, {
        shareCount: Number(purchaseForm.shareCount),
        sourceAccountNo: purchaseForm.sourceAccountNo || null,
        saccoCode: user?.saccoCode || 'DEFAULT',
        branchCode: user?.branchCode || null,
      });
      setSuccess(`✅ ${purchaseForm.shareCount} shares purchased! Certificate: ${res.certificateNumber}`);
      setShowPurchaseModal(false);
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Share purchase failed.');
    } finally {
      setIsPurchasing(false);
    }
  };

  const handleTransfer = async (e) => {
    e.preventDefault();
    setIsTransferring(true);
    setError(null);
    try {
      await shareCapitalApi.initiateTransfer({ ...transferForm, shareCount: Number(transferForm.shareCount), autoApprove: false });
      setSuccess('✅ Share transfer request submitted for approval.');
      setShowTransferModal(false);
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer request failed.');
    } finally {
      setIsTransferring(false);
    }
  };

  const handleApproveTransfer = async (transferId) => {
    try {
      await shareCapitalApi.approveTransfer(transferId);
      setSuccess('✅ Share transfer approved and executed.');
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer approval failed.');
    }
  };

  const mandatoryMet = summary?.isMandatoryRequirementMet;
  const minimumShares = summary?.minimumRequiredShares || 5;
  const totalShares = summary?.totalShares || 0;
  const progressPct = Math.min((totalShares / minimumShares) * 100, 100);

  return (
    <div className="p-6 space-y-6 animate-fadeIn">
      {/* ── Header ── */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-extrabold text-[var(--bdae-text-primary)] flex items-center gap-2">
            <Award className="w-7 h-7 text-[#00CDDB]" />
            Share Capital Register
          </h1>
          <p className="text-sm text-[var(--bdae-text-secondary)] mt-0.5">
            Mandatory equity, certificate serials & peer-to-peer transfer management
          </p>
        </div>
        <div className="flex gap-2">
          <button onClick={fetchData} className="p-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[#00CDDB] transition-colors">
            <RefreshCw className="w-4 h-4" />
          </button>
          {/* Transfer — requires SHARE_CAPITAL_MANAGE or SHARE_TRANSFER_APPROVE */}
          <PermissionGuard
            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
            permissions={[PERMISSIONS.SHARE_CAPITAL_MANAGE, PERMISSIONS.SHARE_TRANSFER_APPROVE]}
          >
            <button
              id="share-transfer-btn"
              onClick={() => setShowTransferModal(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-xl border border-[#00CDDB]/40 text-[#00CDDB] text-xs font-bold hover:bg-[#00CDDB]/10 transition-all"
            >
              <ArrowRightLeft className="w-3.5 h-3.5" /> Transfer Shares
            </button>
          </PermissionGuard>
          {/* Purchase — requires SHARE_CAPITAL_MANAGE or ACCOUNT_CREATE */}
          <PermissionGuard
            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER']}
            permissions={[PERMISSIONS.SHARE_CAPITAL_MANAGE, PERMISSIONS.ACCOUNT_CREATE]}
          >
            <button
              id="purchase-shares-btn"
              onClick={() => setShowPurchaseModal(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#00CDDB] text-white text-xs font-bold hover:bg-[#00CDDB]/90 shadow-sm shadow-[#00CDDB]/30 transition-all"
            >
              <Plus className="w-3.5 h-3.5" /> Purchase Shares
            </button>
          </PermissionGuard>
        </div>
      </div>

      {/* ── Alerts ── */}
      {error && (
        <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
          <AlertCircle className="w-4 h-4 shrink-0" /> {error}
          <button onClick={() => setError(null)} className="ml-auto text-xs opacity-60 hover:opacity-100">✕</button>
        </div>
      )}
      {success && (
        <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-sm">
          <CheckCircle2 className="w-4 h-4 shrink-0" /> {success}
          <button onClick={() => setSuccess(null)} className="ml-auto text-xs opacity-60 hover:opacity-100">✕</button>
        </div>
      )}

      {/* ── Phone Lookup ── */}
      <PermissionGuard
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER']}
        permissions={[PERMISSIONS.SHARE_CAPITAL_VIEW, PERMISSIONS.ACCOUNT_VIEW]}
      >
        <form onSubmit={handlePhoneSearch} className="bdae-surface rounded-2xl border border-[var(--bdae-border)] p-4 flex items-center gap-3">
          <Phone className="w-4 h-4 text-[var(--bdae-text-secondary)] shrink-0" />
          <input
            id="share-phone-lookup"
            type="text"
            placeholder="Lookup share account by phone number…"
            value={phoneSearch}
            onChange={(e) => setPhoneSearch(e.target.value)}
            className="flex-1 bg-transparent text-sm text-[var(--bdae-text-primary)] focus:outline-none placeholder:text-[var(--bdae-text-secondary)]"
          />
          <button
            type="submit"
            id="share-phone-lookup-btn"
            disabled={isSearching}
            className="px-4 py-1.5 rounded-lg bg-[#00CDDB]/10 text-[#00CDDB] text-xs font-bold hover:bg-[#00CDDB]/20 disabled:opacity-50 transition-colors"
          >
            {isSearching ? 'Searching…' : 'Lookup'}
          </button>
        </form>
        {phoneResult && (
          <div className="bdae-surface rounded-xl border border-[#00CDDB]/30 p-4 text-sm space-y-1">
            <p className="font-bold text-[var(--bdae-text-primary)]">📱 Result for {phoneSearch}</p>
            <p className="text-[var(--bdae-text-secondary)]">Account: <span className="font-mono font-bold">{phoneResult.accountNumber}</span></p>
            <p className="text-[var(--bdae-text-secondary)]">Total Shares: <span className="font-bold text-[#00CDDB]">{phoneResult.totalShares}</span> · Status: {phoneResult.status}</p>
          </div>
        )}
      </PermissionGuard>

      {isLoading ? (
        <div className="flex items-center justify-center py-20"><RefreshCw className="w-6 h-6 text-[#00CDDB] animate-spin" /></div>
      ) : (
        <>
          {/* ── Summary Cards ── */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className={`bdae-surface rounded-2xl p-5 border ${mandatoryMet ? 'border-emerald-500/30' : 'border-amber-500/40'} space-y-3`}>
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-widest">Mandatory Compliance</span>
                {mandatoryMet ? <BadgeCheck className="w-5 h-5 text-emerald-400" /> : <AlertCircle className="w-5 h-5 text-amber-400" />}
              </div>
              <div>
                <div className="w-full h-2 rounded-full bg-[var(--bdae-border)]">
                  <div className={`h-2 rounded-full transition-all duration-700 ${mandatoryMet ? 'bg-emerald-400' : 'bg-amber-400'}`} style={{ width: `${progressPct}%` }} />
                </div>
                <p className="text-xs text-[var(--bdae-text-secondary)] mt-1.5">{totalShares} / {minimumShares} mandatory shares</p>
              </div>
              <p className={`text-sm font-bold ${mandatoryMet ? 'text-emerald-400' : 'text-amber-400'}`}>
                {mandatoryMet ? '✓ Requirement Met' : `⚠ ${minimumShares - totalShares} more shares required`}
              </p>
            </div>
            <div className="bdae-surface rounded-2xl p-5 border border-[var(--bdae-border)] space-y-2">
              <span className="text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-widest">Total Holdings</span>
              <p className="text-3xl font-black text-[#00CDDB]">{totalShares.toLocaleString()}</p>
              <p className="text-xs text-[var(--bdae-text-secondary)]">shares @ {formatCurrency(summary?.nominalValue || 1000)} each</p>
              <p className="text-sm font-bold text-[var(--bdae-text-primary)]">{formatCurrency(summary?.totalAmount || 0)} ETB total equity</p>
            </div>
            <div className="bdae-surface rounded-2xl p-5 border border-[var(--bdae-border)] space-y-2">
              <span className="text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-widest">Account Info</span>
              <div className={`mt-2 px-2 py-0.5 rounded-full text-[10px] font-bold uppercase inline-block ${summary?.status === 'ACTIVE' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-gray-500/20 text-gray-400'}`}>
                {summary?.status || 'NOT OPENED'}
              </div>
              <p className="text-xs font-mono text-[var(--bdae-text-secondary)] break-all">{summary?.accountNumber || '—'}</p>
              <p className="text-xs text-[var(--bdae-text-secondary)]">{certificates.length} active certificate(s)</p>
            </div>
          </div>

          {/* ── Certificates Table ── */}
          <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] overflow-hidden">
            <div className="px-5 py-3.5 border-b border-[var(--bdae-border)] flex items-center gap-2">
              <FileText className="w-4 h-4 text-[#00CDDB]" />
              <h2 className="text-sm font-bold text-[var(--bdae-text-primary)]">Share Certificates</h2>
            </div>
            {certificates.length === 0 ? (
              <div className="py-12 text-center text-[var(--bdae-text-secondary)] text-sm">No certificates issued yet.</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02]">
                      {['Certificate #', 'Serial Range', 'Shares', 'Issue Date', 'Status'].map((h) => (
                        <th key={h} className="text-left px-4 py-3 font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[var(--bdae-border)]">
                    {certificates.map((cert) => (
                      <tr key={cert.id} className="hover:bg-black/[0.01] dark:hover:bg-white/[0.01] transition-colors">
                        <td className="px-4 py-3 font-mono font-bold text-[var(--bdae-text-primary)]">{cert.certificateNumber}</td>
                        <td className="px-4 py-3 font-mono text-[var(--bdae-text-secondary)]">
                          <span className="flex items-center gap-1"><Hash className="w-3 h-3" />{cert.startSerial?.toLocaleString()} – {cert.endSerial?.toLocaleString()}</span>
                        </td>
                        <td className="px-4 py-3 font-bold text-[var(--bdae-text-primary)]">{cert.shareCount}</td>
                        <td className="px-4 py-3 text-[var(--bdae-text-secondary)]">{cert.issueDate || '—'}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${cert.status === 'ACTIVE' ? 'bg-emerald-500/20 text-emerald-400' : cert.status === 'TRANSFERRED' ? 'bg-blue-500/20 text-blue-400' : 'bg-gray-500/20 text-gray-400'}`}>{cert.status}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* ── Pending Transfers — only visible to approvers ── */}
          <PermissionGuard
            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
            permissions={[PERMISSIONS.SHARE_TRANSFER_APPROVE, PERMISSIONS.SHARE_CAPITAL_MANAGE]}
          >
            {pendingTransfers.length > 0 && (
              <div className="bdae-surface rounded-2xl border border-amber-500/30 overflow-hidden">
                <div className="px-5 py-3.5 border-b border-amber-500/20 flex items-center gap-2">
                  <Clock className="w-4 h-4 text-amber-400" />
                  <h2 className="text-sm font-bold text-[var(--bdae-text-primary)]">Pending Transfer Approvals ({pendingTransfers.length})</h2>
                </div>
                <div className="divide-y divide-[var(--bdae-border)]">
                  {pendingTransfers.map((t) => (
                    <div key={t.id} className="px-5 py-3 flex items-center justify-between">
                      <div>
                        <p className="text-xs font-bold text-[var(--bdae-text-primary)]">{t.shareCount} shares</p>
                        <p className="text-[11px] text-[var(--bdae-text-secondary)]">From {t.fromMemberId} → {t.toMemberId}</p>
                      </div>
                      <button
                        id={`approve-transfer-${t.id}`}
                        onClick={() => handleApproveTransfer(t.id)}
                        className="px-3 py-1.5 rounded-lg bg-emerald-500/20 text-emerald-400 text-xs font-bold hover:bg-emerald-500/30 transition-colors"
                      >
                        <ShieldCheck className="w-3.5 h-3.5 inline mr-1" />Approve
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </PermissionGuard>
        </>
      )}

      {/* ── Purchase Modal ── */}
      {showPurchaseModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] w-full max-w-md shadow-2xl">
            <div className="px-6 py-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
              <h3 className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-2"><TrendingUp className="w-4 h-4 text-[#00CDDB]" /> Purchase Shares</h3>
              <button onClick={() => setShowPurchaseModal(false)} className="text-[var(--bdae-text-secondary)] text-lg leading-none">✕</button>
            </div>
            <form onSubmit={handlePurchase} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Number of Shares *</label>
                <input id="purchase-share-count" type="number" min="1" value={purchaseForm.shareCount}
                  onChange={(e) => setPurchaseForm({ ...purchaseForm, shareCount: e.target.value })}
                  className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">Cost: {formatCurrency((purchaseForm.shareCount || 0) * 1000)} ETB</p>
              </div>
              <div>
                <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Source Savings Account (optional)</label>
                <input id="purchase-source-account" type="text" placeholder="Leave blank for cash"
                  value={purchaseForm.sourceAccountNo} onChange={(e) => setPurchaseForm({ ...purchaseForm, sourceAccountNo: e.target.value })}
                  className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowPurchaseModal(false)} className="flex-1 py-2.5 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] text-sm font-bold">Cancel</button>
                <button type="submit" id="confirm-purchase-btn" disabled={isPurchasing} className="flex-1 py-2.5 rounded-xl bg-[#00CDDB] text-white text-sm font-bold disabled:opacity-50">
                  {isPurchasing ? 'Processing…' : 'Purchase Shares'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Transfer Modal ── */}
      {showTransferModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] w-full max-w-md shadow-2xl">
            <div className="px-6 py-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
              <h3 className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-2"><ArrowRightLeft className="w-4 h-4 text-[#00CDDB]" /> Transfer Shares</h3>
              <button onClick={() => setShowTransferModal(false)} className="text-[var(--bdae-text-secondary)] text-lg leading-none">✕</button>
            </div>
            <form onSubmit={handleTransfer} className="p-6 space-y-4">
              {[
                { id: 'transfer-from', label: 'From Member ID *', key: 'fromMemberId', placeholder: 'UUID of seller' },
                { id: 'transfer-to', label: 'To Member ID *', key: 'toMemberId', placeholder: 'UUID of buyer' },
                { id: 'transfer-cert', label: 'Certificate ID *', key: 'certificateId', placeholder: 'UUID of certificate' },
              ].map(({ id, label, key, placeholder }) => (
                <div key={key}>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">{label}</label>
                  <input id={id} type="text" placeholder={placeholder} value={transferForm[key]}
                    onChange={(e) => setTransferForm({ ...transferForm, [key]: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
                </div>
              ))}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Shares *</label>
                  <input id="transfer-share-count" type="number" min="1" value={transferForm.shareCount}
                    onChange={(e) => setTransferForm({ ...transferForm, shareCount: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Price (ETB)</label>
                  <input id="transfer-price" type="number" placeholder="Nominal if blank" value={transferForm.transferPrice}
                    onChange={(e) => setTransferForm({ ...transferForm, transferPrice: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" />
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowTransferModal(false)} className="flex-1 py-2.5 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] text-sm font-bold">Cancel</button>
                <button type="submit" id="confirm-transfer-btn" disabled={isTransferring} className="flex-1 py-2.5 rounded-xl bg-[#00CDDB] text-white text-sm font-bold disabled:opacity-50">
                  {isTransferring ? 'Submitting…' : 'Submit Transfer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
