import React from 'react';
import { CheckCircle2, Clock, AlertTriangle } from 'lucide-react';

export const SaccoStatusBadge = ({ status }) => {
  const normalizedStatus = (status || 'PENDING_SETUP').toUpperCase();

  if (normalizedStatus === 'ACTIVE') {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 border border-emerald-500/30">
        <CheckCircle2 className="w-3 h-3" />
        <span>ACTIVE</span>
      </span>
    );
  }

  if (normalizedStatus === 'PENDING_SETUP') {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 border border-amber-500/30">
        <Clock className="w-3 h-3" />
        <span>PENDING SETUP</span>
      </span>
    );
  }

  return (
    <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-500/10 text-slate-600 border border-slate-500/30">
      <AlertTriangle className="w-3 h-3" />
      <span>{normalizedStatus}</span>
    </span>
  );
};
