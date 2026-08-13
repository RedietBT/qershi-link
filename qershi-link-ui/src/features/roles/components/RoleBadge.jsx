import React from 'react';
import { ShieldCheck, Shield } from 'lucide-react';

export const RoleBadge = ({ isSystemDefined }) => {
  if (isSystemDefined) {
    return (
      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-cyan-500/10 text-cyan-600 border border-cyan-500/30">
        <ShieldCheck className="w-3 h-3" />
        <span>SYSTEM DEFINED</span>
      </span>
    );
  }

  return (
    <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 border border-emerald-500/30">
      <Shield className="w-3 h-3" />
      <span>CUSTOM ROLE</span>
    </span>
  );
};
