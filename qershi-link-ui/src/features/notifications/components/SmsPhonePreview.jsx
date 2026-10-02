import React from 'react';
import { Smartphone, Signal, Wifi, Battery, Send } from 'lucide-react';

/**
 * Realistic Smartphone Mockup displaying a live rendered SMS preview
 * with sample placeholder values.
 */
export const SmsPhonePreview = ({ content = '', senderId = 'QERSHI' }) => {
  // Replace template tokens with realistic demo data
  const renderSampleText = (raw) => {
    if (!raw) return 'No message content...';
    return raw
      .replace(/{memberName}/g, 'Abebe Bikila')
      .replace(/{amount}/g, '5,000.00')
      .replace(/{accountNo}/g, '100010042')
      .replace(/{balance}/g, '18,450.00')
      .replace(/{productName}/g, 'Regular Savings')
      .replace(/{receiverName}/g, 'Chaltu Tadesse')
      .replace(/{receiverAccountNo}/g, '100010088')
      .replace(/{loanId}/g, 'LN-202610-A19F')
      .replace(/{remainingBalance}/g, '25,000.00')
      .replace(/{saccoName}/g, 'Awash SACCO')
      .replace(/{otpCode}/g, '849201');
  };

  const messageText = renderSampleText(content);
  const characterCount = messageText.length;
  const smsSegments = Math.ceil(characterCount / 160) || 1;

  return (
    <div className="w-full max-w-[280px] mx-auto bg-slate-900 rounded-[36px] p-3 shadow-2xl border-4 border-slate-700/60 relative">
      {/* Dynamic Island / Speaker Notch */}
      <div className="absolute top-4 left-1/2 -translate-x-1/2 w-20 h-4 bg-slate-800 rounded-full flex items-center justify-center gap-2 z-10">
        <div className="w-2.5 h-2.5 rounded-full bg-slate-950 border border-slate-700" />
        <div className="w-1.5 h-1.5 rounded-full bg-blue-500/80" />
      </div>

      {/* Screen Frame */}
      <div className="bg-slate-950 rounded-[28px] overflow-hidden pt-6 pb-4 px-3 flex flex-col h-[420px] text-white">
        {/* Status Bar */}
        <div className="flex items-center justify-between text-[10px] text-slate-400 px-1 pt-1 pb-2">
          <span>09:41</span>
          <div className="flex items-center gap-1.5">
            <Signal className="w-2.5 h-2.5" />
            <Wifi className="w-2.5 h-2.5" />
            <Battery className="w-3 h-3 text-emerald-400" />
          </div>
        </div>

        {/* Sender Header */}
        <div className="border-b border-slate-800/80 pb-2 text-center">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-cyan-600 to-blue-500 text-white font-black text-xs mx-auto flex items-center justify-center shadow-md shadow-cyan-500/20">
            {senderId.slice(0, 2).toUpperCase()}
          </div>
          <div className="text-[11px] font-bold text-slate-200 mt-1">{senderId || 'SMS'}</div>
          <div className="text-[9px] text-slate-500">Text Message • Today</div>
        </div>

        {/* Message Bubble Container */}
        <div className="flex-1 overflow-y-auto py-3 space-y-2">
          <div className="max-w-[90%] bg-gradient-to-b from-slate-800 to-slate-800/90 text-slate-100 text-[11px] leading-relaxed rounded-2xl rounded-tl-sm px-3.5 py-2.5 shadow-sm border border-slate-700/50">
            {messageText}
            <div className="text-[8px] text-slate-400 text-right mt-1.5">09:41 AM</div>
          </div>
        </div>

        {/* Segment Counter */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-xl px-2.5 py-1.5 text-[9px] text-slate-400 flex items-center justify-between">
          <span>{characterCount} chars</span>
          <span className="text-cyan-400 font-semibold">{smsSegments} SMS {smsSegments > 1 ? 'parts' : 'part'}</span>
        </div>
      </div>
    </div>
  );
};
