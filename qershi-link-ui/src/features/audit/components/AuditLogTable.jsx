import React, { useState } from 'react';
import { AuditStatusBadge } from './AuditStatusBadge';
import { ShieldCheck, RefreshCw, AlertCircle, Calendar, Globe, ChevronDown, ChevronUp, Terminal } from 'lucide-react';

export const AuditLogTable = ({ logs = [], isLoading, error, onRefresh }) => {
  const [expandedLogId, setExpandedLogId] = useState(null);

  const toggleExpand = (logId) => {
    setExpandedLogId(expandedLogId === logId ? null : logId);
  };

  if (isLoading) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <RefreshCw className="w-8 h-8 text-[var(--bdae-secondary)] animate-spin mx-auto" />
        <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">
          Fetching Platform Security Audit Logs...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bdae-card p-8 border border-red-500/30 bg-red-500/5 text-center space-y-3">
        <AlertCircle className="w-8 h-8 text-red-500 mx-auto" />
        <p className="text-xs font-bold text-red-600 dark:text-red-400">{error}</p>
        <button
          onClick={onRefresh}
          className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl inline-flex items-center gap-1.5"
        >
          <RefreshCw className="w-3.5 h-3.5" /> Retry Load
        </button>
      </div>
    );
  }

  if (logs.length === 0) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <ShieldCheck className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto opacity-50" />
        <p className="text-sm font-bold text-[var(--bdae-text-primary)]">No Security Audit Log Events Found</p>
        <p className="text-xs text-[var(--bdae-text-secondary)]">System actions and login events will be logged here.</p>
      </div>
    );
  }

  return (
    <div className="bdae-card border border-[var(--bdae-border)] shadow-xl overflow-hidden rounded-2xl">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
              <th className="py-3.5 px-4">Timestamp</th>
              <th className="py-3.5 px-4">Action Event</th>
              <th className="py-3.5 px-4">Resource Affected</th>
              <th className="py-3.5 px-4">IP Address</th>
              <th className="py-3.5 px-4">Status</th>
              <th className="py-3.5 px-4 text-right">Details</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {logs.map((log) => {
              const isExpanded = expandedLogId === (log.logId || log.timestamp);
              const formattedDate = log.timestamp
                ? new Date(log.timestamp).toLocaleString('en-US', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit'
                  })
                : 'N/A';

              return (
                <React.Fragment key={log.logId || log.timestamp || Math.random()}>
                  <tr 
                    onClick={() => toggleExpand(log.logId || log.timestamp)}
                    className="hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition-colors group"
                  >
                    {/* Timestamp */}
                    <td className="py-3.5 px-4 font-mono text-[11px] text-[var(--bdae-text-secondary)] whitespace-nowrap">
                      <div className="flex items-center space-x-1.5">
                        <Calendar className="w-3.5 h-3.5 opacity-60" />
                        <span>{formattedDate}</span>
                      </div>
                    </td>

                    {/* Action Event */}
                    <td className="py-3.5 px-4">
                      <span className="font-mono font-bold text-xs text-[var(--bdae-primary)] dark:text-[var(--bdae-secondary)]">
                        {log.action}
                      </span>
                    </td>

                    {/* Resource Affected */}
                    <td className="py-3.5 px-4 font-semibold text-[var(--bdae-text-primary)]">
                      {log.resourceAffected || 'System Global'}
                    </td>

                    {/* IP Address */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center space-x-1 font-mono text-[11px] text-[var(--bdae-text-secondary)]">
                        <Globe className="w-3 h-3 opacity-60" />
                        <span>{log.ipAddress || '127.0.0.1'}</span>
                      </div>
                    </td>

                    {/* Status Badge */}
                    <td className="py-3.5 px-4">
                      <AuditStatusBadge status={log.status} />
                    </td>

                    {/* Expand Details */}
                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleExpand(log.logId || log.timestamp);
                        }}
                        className="p-1.5 rounded-lg border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] transition-all"
                      >
                        {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                      </button>
                    </td>
                  </tr>

                  {/* Expanded Log Details */}
                  {isExpanded && (
                    <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)]">
                      <td colSpan="6" className="p-4 font-mono text-xs">
                        <div className="p-4 rounded-xl bdae-surface border border-[var(--bdae-border)] space-y-2">
                          <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-2 text-[11px]">
                            <span className="font-bold flex items-center gap-1.5 text-[var(--bdae-secondary)]">
                              <Terminal className="w-4 h-4" /> Action Event Log Trace
                            </span>
                            <span>Status: {log.status}</span>
                          </div>

                          <div className="space-y-1">
                            <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
                              Event Details & Audit Log Message:
                            </p>
                            <pre className="p-3 rounded-lg bg-black/10 dark:bg-black/40 text-[11px] leading-relaxed text-[var(--bdae-text-primary)] overflow-x-auto whitespace-pre-wrap font-mono">
                              {log.details ? log.details : JSON.stringify({ action: log.action, resource: log.resourceAffected, ip: log.ipAddress }, null, 2)}
                            </pre>
                          </div>
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
