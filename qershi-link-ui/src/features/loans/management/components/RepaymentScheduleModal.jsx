import {
  X,
  Calendar,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Loader2,
  BadgePercent,
  FileSpreadsheet,
  Download,
  Users,
  Lock,
  Unlock,
  ShieldCheck
} from 'lucide-react';
import { loanManagementApi } from '../api/loanManagementApi';
import { formatCurrency, formatDate } from '../../../../common/utils/currency';

export const RepaymentScheduleModal = ({ accountId, accountNo, isOpen, onClose }) => {
  const [activeTab, setActiveTab] = useState('schedule');
  const [schedules, setSchedules] = useState([]);
  const [guarantors, setGuarantors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && accountId) {
      loadData();
    } else {
      setSchedules([]);
      setGuarantors([]);
      setActiveTab('schedule');
      setError(null);
    }
  }, [isOpen, accountId]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [schedRes, guarRes] = await Promise.allSettled([
        loanManagementApi.getAccountSchedule(accountId),
        loanManagementApi.getAccountGuarantors(accountId)
      ]);

      if (schedRes.status === 'fulfilled') {
        const data = schedRes.value.data || schedRes.value || [];
        setSchedules(Array.isArray(data) ? data : []);
      }

      if (guarRes.status === 'fulfilled') {
        const gData = guarRes.value.data || guarRes.value || [];
        setGuarantors(Array.isArray(gData) ? gData : []);
      }
    } catch (err) {
      console.error('Failed to load schedule or guarantors:', err);
      setError(err?.response?.data?.message || 'Could not retrieve loan account details.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  // Calculate totals
  const totalPrincipal = schedules.reduce((acc, s) => acc + Number(s.principalDue || 0), 0);
  const totalInterest = schedules.reduce((acc, s) => acc + Number(s.interestDue || 0), 0);
  const totalDue = schedules.reduce((acc, s) => acc + Number(s.totalDue || 0), 0);
  const totalPaid = schedules.reduce((acc, s) => acc + Number(s.amountPaid || 0), 0);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PAID':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-3 h-3" />
            Paid
          </span>
        );
      case 'PARTIALLY_PAID':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <Clock className="w-3 h-3" />
            Partial
          </span>
        );
      case 'OVERDUE':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 dark:text-red-400">
            <AlertTriangle className="w-3 h-3" />
            Overdue
          </span>
        );
      case 'PENDING':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-gray-500/10 text-gray-600">
            Pending
          </span>
        );
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col shadow-2xl border border-[var(--bdae-border)]">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Amortization Repayment Schedule
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                Loan Account: {accountNo || accountId?.slice(0, 13)}
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

        {/* Summary Metric Ribbon */}
        <div className="p-4 bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Total Principal
            </span>
            <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
              {formatCurrency(totalPrincipal)}
            </span>
          </div>
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Total Interest
            </span>
            <span className="font-mono font-bold text-amber-600 dark:text-amber-400">
              {formatCurrency(totalInterest)}
            </span>
          </div>
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Cumulative Paid
            </span>
            <span className="font-mono font-bold text-emerald-600 dark:text-emerald-400">
              {formatCurrency(totalPaid)}
            </span>
          </div>
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Remaining Balance
            </span>
            <span className="font-mono font-black text-[var(--bdae-primary)]">
              {formatCurrency(Math.max(0, totalDue - totalPaid))}
            </span>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="flex border-b border-[var(--bdae-border)] px-6 bg-black/5 dark:bg-white/5">
          <button
            type="button"
            onClick={() => setActiveTab('schedule')}
            className={`py-3 px-4 text-xs font-bold border-b-2 flex items-center gap-2 transition-all ${
              activeTab === 'schedule'
                ? 'border-[var(--bdae-primary)] text-[var(--bdae-primary)]'
                : 'border-transparent text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            <Calendar className="w-4 h-4" />
            Amortization Schedule ({schedules.length})
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('guarantors')}
            className={`py-3 px-4 text-xs font-bold border-b-2 flex items-center gap-2 transition-all ${
              activeTab === 'guarantors'
                ? 'border-[var(--bdae-primary)] text-[var(--bdae-primary)]'
                : 'border-transparent text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            <Users className="w-4 h-4" />
            Peer Guarantor Liens ({guarantors.length})
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-4 flex-1">
          {loading && (
            <div className="flex flex-col items-center justify-center py-16 space-y-3">
              <Loader2 className="w-8 h-8 animate-spin text-[var(--bdae-primary)]" />
              <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
                Loading loan account and lien hold details...
              </p>
            </div>
          )}

          {error && !loading && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
              <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Retrieval Alert</p>
                <p className="mt-0.5 opacity-90">{error}</p>
              </div>
            </div>
          )}

          {/* Tab 1: Amortization Schedule */}
          {activeTab === 'schedule' && !loading && (
            <>
              {schedules.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
                  <FileSpreadsheet className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
                  <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
                    No Schedule Installments Found
                  </p>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                    The repayment schedule will be generated once loan disbursement is approved.
                  </p>
                </div>
              ) : (
                <div className="overflow-x-auto border border-[var(--bdae-border)] rounded-xl">
                  <table className="w-full text-left border-collapse text-xs">
                    <thead>
                      <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] font-semibold">
                        <th className="py-2.5 px-3 text-center">#</th>
                        <th className="py-2.5 px-3">Due Date</th>
                        <th className="py-2.5 px-3 text-right">Principal Due</th>
                        <th className="py-2.5 px-3 text-right">Interest Due</th>
                        <th className="py-2.5 px-3 text-right">Total Installment</th>
                        <th className="py-2.5 px-3 text-right">Amount Paid</th>
                        <th className="py-2.5 px-3 text-center">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--bdae-border)]">
                      {schedules.map((item) => (
                        <tr key={item.scheduleId || item.installmentNo} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                          <td className="py-2.5 px-3 font-mono font-bold text-center text-[var(--bdae-text-secondary)]">
                            {item.installmentNo}
                          </td>
                          <td className="py-2.5 px-3 font-medium text-[var(--bdae-text-primary)]">
                            {formatDate(item.dueDate)}
                          </td>
                          <td className="py-2.5 px-3 text-right font-mono font-semibold text-[var(--bdae-text-primary)]">
                            {formatCurrency(item.principalDue)}
                          </td>
                          <td className="py-2.5 px-3 text-right font-mono text-amber-600 dark:text-amber-400">
                            {formatCurrency(item.interestDue)}
                          </td>
                          <td className="py-2.5 px-3 text-right font-mono font-black text-[var(--bdae-primary)]">
                            {formatCurrency(item.totalDue)}
                          </td>
                          <td className="py-2.5 px-3 text-right font-mono font-bold text-emerald-600 dark:text-emerald-400">
                            {formatCurrency(item.amountPaid || 0)}
                          </td>
                          <td className="py-2.5 px-3 text-center">
                            {getStatusBadge(item.status)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr className="bg-black/5 dark:bg-white/5 font-bold border-t-2 border-[var(--bdae-border)] text-xs">
                        <td className="py-2.5 px-3 text-center" colSpan={2}>
                          Total Amortization
                        </td>
                        <td className="py-2.5 px-3 text-right font-mono text-[var(--bdae-text-primary)]">
                          {formatCurrency(totalPrincipal)}
                        </td>
                        <td className="py-2.5 px-3 text-right font-mono text-amber-600 dark:text-amber-400">
                          {formatCurrency(totalInterest)}
                        </td>
                        <td className="py-2.5 px-3 text-right font-mono text-[var(--bdae-primary)]">
                          {formatCurrency(totalDue)}
                        </td>
                        <td className="py-2.5 px-3 text-right font-mono text-emerald-600 dark:text-emerald-400">
                          {formatCurrency(totalPaid)}
                        </td>
                        <td></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              )}
            </>
          )}

          {/* Tab 2: Peer Guarantors & Savings Liens */}
          {activeTab === 'guarantors' && !loading && (
            <div className="space-y-4">
              <div className="p-4 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                    <ShieldCheck className="w-4 h-4 text-emerald-500" />
                    Tier-1 Peer Guarantor Collateral Holds
                  </h4>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-0.5">
                    Savings liens placed on member accounts are automatically held upon disbursement and systematically released upon total loan repayment.
                  </p>
                </div>
                <div className="text-right">
                  <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
                    Total Encumbered Lien
                  </span>
                  <span className="font-mono text-sm font-black text-emerald-500">
                    {formatCurrency(
                      guarantors.reduce((sum, g) => sum + (Number(g.guaranteedAmount) || 0), 0)
                    )}
                  </span>
                </div>
              </div>

              {guarantors.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
                  <Users className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
                  <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
                    No Peer Guarantors Attached
                  </p>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                    This loan account does not have savings lien encumbrances pledged by other SACCO members.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {guarantors.map((g, idx) => (
                    <div
                      key={g.guarantorId || idx}
                      className="p-4 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] space-y-3 shadow-sm hover:border-[var(--bdae-primary)]/50 transition-all"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <div className="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-500 flex items-center justify-center font-bold text-xs">
                            #{idx + 1}
                          </div>
                          <div>
                            <h5 className="font-bold text-xs text-[var(--bdae-text-primary)]">
                              {g.guarantorName || 'Guarantor Member'}
                            </h5>
                            <span className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">
                              {g.savingsAccountNo}
                            </span>
                          </div>
                        </div>

                        <span
                          className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold ${
                            g.status === 'HELD'
                              ? 'bg-amber-500/10 text-amber-500 border border-amber-500/20'
                              : g.status === 'RELEASED'
                              ? 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20'
                              : 'bg-slate-500/10 text-slate-400'
                          }`}
                        >
                          {g.status === 'HELD' ? (
                            <>
                              <Lock className="w-3 h-3" />
                              Active Lien Hold
                            </>
                          ) : g.status === 'RELEASED' ? (
                            <>
                              <Unlock className="w-3 h-3" />
                              Lien Released
                            </>
                          ) : (
                            g.status || 'PENDING'
                          )}
                        </span>
                      </div>

                      <div className="grid grid-cols-2 gap-2 pt-2 border-t border-[var(--bdae-border)]/60 text-xs">
                        <div>
                          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                            Pledged Amount
                          </span>
                          <span className="font-mono font-bold text-emerald-500">
                            {formatCurrency(g.guaranteedAmount)}
                          </span>
                        </div>

                        <div>
                          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                            Lien Hold ID
                          </span>
                          <span className="font-mono text-[10px] text-[var(--bdae-text-secondary)] truncate block">
                            {g.lienId ? g.lienId.slice(0, 13) + '...' : 'Not Recorded'}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-4 py-2 rounded-xl text-xs font-bold border border-[var(--bdae-border)] bg-[var(--bdae-surface)] hover:bg-black/5 dark:hover:bg-white/5 flex items-center gap-2 transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            Print Schedule
          </button>
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] text-white hover:opacity-90 transition-opacity"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
