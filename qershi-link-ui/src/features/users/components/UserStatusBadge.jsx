import React from 'react';
import { CheckCircle2, Clock, Lock, AlertTriangle } from 'lucide-react';

export const UserStatusBadge = ({ status }) => {
  const normalized = (status || 'PENDING').toUpperCase();

  if (normalized === 'ACTIVE') {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 border border-emerald-500/30">
        <CheckCircle2 className="w-3 h-3" />
        <span>ACTIVE</span>
      </span>
    );
  }

  if (normalized === 'PENDING' || normalized === 'PASSWORD_CHANGE_REQUIRED') {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 border border-amber-500/30">
        <Clock className="w-3 h-3" />
        <span>{normalized === 'PASSWORD_CHANGE_REQUIRED' ? 'PIN CHANGE REQ' : 'PENDING'}</span>
      </span>
    );
  }

  if (normalized === 'LOCKED' || normalized === 'FROZEN' || normalized === 'DELETED') {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 border border-red-500/30">
        <Lock className="w-3 h-3" />
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
