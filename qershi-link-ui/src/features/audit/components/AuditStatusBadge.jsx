import React from 'react';
import { CheckCircle2, ShieldAlert, AlertTriangle } from 'lucide-react';

export const AuditStatusBadge = ({ status }) => {
  const normalized = (status || 'UNKNOWN').toUpperCase();

  if (normalized === 'SUCCESS' || normalized === 'COMPLETED' || normalized === '200') {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 border border-emerald-500/30">
        <CheckCircle2 className="w-3 h-3" />
        <span>SUCCESS</span>
      </span>
    );
  }

  if (normalized === 'FAILED' || normalized === 'ERROR' || normalized === '401' || normalized === '403') {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 border border-red-500/30">
        <ShieldAlert className="w-3 h-3" />
        <span>{normalized}</span>
      </span>
    );
  }

  return (
    <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-500/10 text-slate-600 border border-slate-500/30">
      <AlertTriangle className="w-3 h-3" />
      <span>{normalized}</span>
    </span>
  );
};
