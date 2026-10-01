import React, { useState, useEffect } from 'react';
import {
  X,
  FilePlus,
  Loader2,
  AlertTriangle,
  Plus,
  Trash2,
  BadgePercent,
  CheckCircle2,
  XCircle,
  ShieldCheck
} from 'lucide-react';
import { loanOriginationApi } from '../api/loanOriginationApi';
import { depositProductApi } from '../../../accounts/api/depositProductApi';
import { formatCurrency } from '../../../../common/utils/currency';
import { GuarantorPledgingSection } from './GuarantorPledgingSection';

export const LoanApplicationModal = ({ isOpen, onClose, onSuccess }) => {
  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(false);

  // Form State
  const [userId, setUserId] = useState('');
  const [productId, setProductId] = useState('');
  const [scoringType, setScoringType] = useState('INDIVIDUAL');
  const [groupId, setGroupId] = useState('');
  const [amountRequested, setAmountRequested] = useState('');
  const [savingsConsistency, setSavingsConsistency] = useState('90');
  const [historicalYield, setHistoricalYield] = useState('');
  const [projectedYield, setProjectedYield] = useState('');
  const [landSizeHectares, setLandSizeHectares] = useState('1.5');
  const [collaterals, setCollaterals] = useState([]);
  const [guarantors, setGuarantors] = useState([]);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [scoringResult, setScoringResult] = useState(null);

  useEffect(() => {
    if (isOpen) {
      loadProducts();
    } else {
      setScoringResult(null);
      setError(null);
    }
  }, [isOpen]);

  const loadProducts = async () => {
    try {
      setLoadingProducts(true);
      const res = await depositProductApi.getAll();
      const list = res.data || res || [];
      setProducts(Array.isArray(list) ? list : []);
      if (list.length > 0 && !productId) {
        setProductId(list[0].productId || list[0].id || '');
      }
    } catch (err) {
      console.warn('Failed to load products list:', err);
    } finally {
      setLoadingProducts(false);
    }
  };

  const handleAddCollateral = () => {
    setCollaterals([
      ...collaterals,
      { type: 'LAND', estimatedValue: '', documentUrl: '' }
    ]);
  };

  const handleRemoveCollateral = (index) => {
    setCollaterals(collaterals.filter((_, idx) => idx !== index));
  };

  const handleCollateralChange = (index, field, value) => {
    const updated = [...collaterals];
    updated[index][field] = value;
    setCollaterals(updated);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!userId.trim()) {
      setError('Borrower Member ID is required.');
      return;
    }
    const numAmount = Number(amountRequested);
    if (!numAmount || numAmount <= 0) {
      setError('Please provide a valid loan amount requested.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const formattedCollaterals = collaterals
        .filter((c) => Number(c.estimatedValue) > 0)
        .map((c) => ({
          type: c.type,
          estimatedValue: Number(c.estimatedValue),
          documentUrl: c.documentUrl.trim() || undefined
        }));

      const formattedGuarantors = guarantors
        .filter((g) => g.savingsAccountNo?.trim() && Number(g.guaranteedAmount) > 0)
        .map((g) => ({
          guarantorUserId: g.guarantorUserId?.trim() || undefined,
          guarantorName: g.guarantorName?.trim() || undefined,
          guarantorPhone: g.guarantorPhone?.trim() || undefined,
          savingsAccountNo: g.savingsAccountNo.trim(),
          guaranteedAmount: Number(g.guaranteedAmount)
        }));

      const payload = {
        userId: userId.trim(),
        groupId: scoringType === 'GROUP' && groupId ? groupId.trim() : undefined,
        productId: productId || undefined,
        scoringType,
        amountRequested: numAmount,
        savingsConsistency: savingsConsistency ? Number(savingsConsistency) : 80,
        historicalYield: historicalYield ? Number(historicalYield) : 0,
        projectedYield: projectedYield ? Number(projectedYield) : 0,
        landSizeHectares: landSizeHectares ? Number(landSizeHectares) : 1.0,
        collaterals: formattedCollaterals,
        guarantors: formattedGuarantors
      };

      const res = await loanOriginationApi.submitApplication(payload);
      setScoringResult(res);
      onSuccess?.(res);
    } catch (err) {
      console.error('Failed to submit loan application:', err);
      setError(err?.response?.data?.message || 'Failed to submit loan application.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col shadow-2xl border border-[var(--bdae-border)]">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center">
              <FilePlus className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Originate Loan Application
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Submit borrowing request for credit scoring and Maker-Checker underwriting review.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] rounded-xl transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto space-y-5 flex-1 text-xs">
          {error && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3">
              <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Application Submission Alert</p>
                <p className="mt-0.5">{error}</p>
              </div>
            </div>
          )}

          {/* Instant Scoring Result View */}
          {scoringResult && (
            <div className="p-5 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 space-y-4 animate-fadeIn">
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-600 flex items-center justify-center">
                    <CheckCircle2 className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-[var(--bdae-text-primary)]">
                      Application Submitted & Scored
                    </h3>
                    <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                      Ref: {scoringResult.applicationNo || scoringResult.applicationId}
                    </p>
                  </div>
                </div>
                <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-700 dark:text-emerald-300 font-extrabold text-[10px]">
                  {scoringResult.status || 'SUBMITTED'}
                </span>
              </div>

              {scoringResult.creditScoring && (
                <div className="p-3.5 rounded-xl bg-black/5 dark:bg-white/5 grid grid-cols-2 sm:grid-cols-3 gap-2">
                  <div>
                    <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                      Calculated Score
                    </span>
                    <span className="font-mono font-bold text-sm text-[var(--bdae-primary)]">
                      {scoringResult.creditScoring.calculatedScore ?? '—'} / 100
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                      Pre-Eligibility
                    </span>
                    <span
                      className={`font-bold ${
                        scoringResult.creditScoring.passedEligibility
                          ? 'text-emerald-500'
                          : 'text-amber-500'
                      }`}
                    >
                      {scoringResult.creditScoring.passedEligibility ? 'Passed' : 'Requires Review'}
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                      Amount Requested
                    </span>
                    <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                      {formatCurrency(scoringResult.amountRequested)}
                    </span>
                  </div>
                </div>
              )}

              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                The loan application has entered the <strong>Maker-Checker Underwriting Queue</strong> for final dual-control approval.
              </p>

              <div className="flex justify-end pt-1">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-5 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] text-white hover:opacity-90 transition-opacity"
                >
                  Done
                </button>
              </div>
            </div>
          )}

          {!scoringResult && (
            <form id="loan-orig-form" onSubmit={handleSubmit} className="space-y-4">
              {/* Member User ID & Scoring Type */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Borrower Member User ID *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. 00000000-0000-0000-0000-000000000001"
                    value={userId}
                    onChange={(e) => setUserId(e.target.value)}
                    className="bdae-input font-mono text-xs"
                  />
                </div>

                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Scoring Model Type
                  </label>
                  <select
                    value={scoringType}
                    onChange={(e) => setScoringType(e.target.value)}
                    className="bdae-input text-xs font-semibold"
                  >
                    <option value="INDIVIDUAL">Individual Borrower</option>
                    <option value="GROUP">Group / Solidary Guarantee</option>
                  </select>
                </div>
              </div>

              {scoringType === 'GROUP' && (
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Borrowing Group ID *
                  </label>
                  <input
                    type="text"
                    placeholder="Group UUID"
                    value={groupId}
                    onChange={(e) => setGroupId(e.target.value)}
                    className="bdae-input font-mono text-xs"
                  />
                </div>
              )}

              {/* Amount & Product */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Loan Amount Requested (ETB) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="1"
                    required
                    placeholder="50000.00"
                    value={amountRequested}
                    onChange={(e) => setAmountRequested(e.target.value)}
                    className="bdae-input font-mono font-bold text-sm"
                  />
                </div>

                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Target Loan Product
                  </label>
                  <select
                    value={productId}
                    onChange={(e) => setProductId(e.target.value)}
                    className="bdae-input text-xs"
                  >
                    {products.length === 0 ? (
                      <option value="">Standard Agricultural Loan Product</option>
                    ) : (
                      products.map((p) => (
                        <option key={p.productId || p.id} value={p.productId || p.id}>
                          {p.productName || p.productCode} ({p.category})
                        </option>
                      ))
                    )}
                  </select>
                </div>
              </div>

              {/* Agricultural & Financial Scoring Inputs */}
              <div className="p-4 rounded-xl border border-[var(--bdae-border)] space-y-3 bg-black/5 dark:bg-white/5">
                <h3 className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5 uppercase text-[11px] tracking-wider">
                  <BadgePercent className="w-4 h-4 text-purple-500" />
                  Credit Scoring Metric Inputs
                </h3>

                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  <div className="space-y-1">
                    <label className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase">
                      Savings Consistency (%)
                    </label>
                    <input
                      type="number"
                      step="0.1"
                      min="0"
                      max="100"
                      value={savingsConsistency}
                      onChange={(e) => setSavingsConsistency(e.target.value)}
                      className="bdae-input font-mono text-xs"
                    />
                  </div>

                  <div className="space-y-1">
                    <label className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase">
                      Historical Yield (ETB)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      placeholder="12000.00"
                      value={historicalYield}
                      onChange={(e) => setHistoricalYield(e.target.value)}
                      className="bdae-input font-mono text-xs"
                    />
                  </div>

                  <div className="space-y-1">
                    <label className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase">
                      Projected Yield (ETB)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      placeholder="18000.00"
                      value={projectedYield}
                      onChange={(e) => setProjectedYield(e.target.value)}
                      className="bdae-input font-mono text-xs"
                    />
                  </div>

                  <div className="space-y-1">
                    <label className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase">
                      Land Size (Ha)
                    </label>
                    <input
                      type="number"
                      step="0.1"
                      placeholder="2.5"
                      value={landSizeHectares}
                      onChange={(e) => setLandSizeHectares(e.target.value)}
                      className="bdae-input font-mono text-xs"
                    />
                  </div>
                </div>
              </div>

              {/* Pledged Collateral Builder */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-[var(--bdae-text-primary)] uppercase text-[11px] tracking-wider">
                    Pledged Collateral Assets
                  </span>
                  <button
                    type="button"
                    onClick={handleAddCollateral}
                    className="px-2.5 py-1 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] text-xs font-bold flex items-center gap-1 hover:bg-[var(--bdae-primary)] hover:text-white transition-all"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    Add Collateral
                  </button>
                </div>

                {collaterals.length === 0 ? (
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] italic">
                    No collateral items pledged. Click "Add Collateral" to attach land, crops, gold, or vehicles.
                  </p>
                ) : (
                  <div className="space-y-2">
                    {collaterals.map((item, index) => (
                      <div
                        key={index}
                        className="flex flex-wrap items-center gap-2 p-2.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-xs"
                      >
                        <select
                          value={item.type}
                          onChange={(e) => handleCollateralChange(index, 'type', e.target.value)}
                          className="bdae-input text-xs w-32 font-bold"
                        >
                          <option value="LAND">Land Holding</option>
                          <option value="CROP">Standing Crops</option>
                          <option value="VEHICLE">Motor Vehicle</option>
                          <option value="REAL_ESTATE">Real Estate</option>
                          <option value="GOLD">Precious Metals / Gold</option>
                          <option value="SHARES">SACCO Shares</option>
                        </select>

                        <input
                          type="number"
                          step="0.01"
                          placeholder="Estimated Value (ETB)"
                          value={item.estimatedValue}
                          onChange={(e) =>
                            handleCollateralChange(index, 'estimatedValue', e.target.value)
                          }
                          className="bdae-input font-mono text-xs flex-1 min-w-[130px]"
                        />

                        <input
                          type="text"
                          placeholder="Title / Deed Doc Ref"
                          value={item.documentUrl}
                          onChange={(e) =>
                            handleCollateralChange(index, 'documentUrl', e.target.value)
                          }
                          className="bdae-input text-xs flex-1 min-w-[140px]"
                        />

                        <button
                          type="button"
                          onClick={() => handleRemoveCollateral(index)}
                          className="p-2 text-red-500 hover:bg-red-500/10 rounded-lg transition-colors"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Member Peer Guarantors & Savings Liens Section */}
              <div className="pt-2 border-t border-[var(--bdae-border)]">
                <GuarantorPledgingSection
                  guarantors={guarantors}
                  onChange={setGuarantors}
                  applicantUserId={userId}
                  amountRequested={amountRequested}
                />
              </div>
            </form>
          )}
        </div>

        {/* Footer */}
        {!scoringResult && (
          <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2 rounded-xl text-xs font-semibold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              form="loan-orig-form"
              disabled={submitting}
              className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-[var(--bdae-primary)] hover:opacity-90 disabled:opacity-50 shadow-md flex items-center gap-2 transition-all"
            >
              {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
              Submit Loan for Scoring
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
