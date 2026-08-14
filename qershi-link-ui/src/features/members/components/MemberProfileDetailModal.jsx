import React, { useState } from 'react';
import {
    X, UserCircle, MapPin, Briefcase, ShieldCheck, RefreshCw, AlertCircle, Save, CheckCircle, FileText
} from 'lucide-react';
import { memberProfileApi } from '../api/memberProfileApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { KycDocumentTab } from './KycDocumentTab';

export const MemberProfileDetailModal = ({ profile, onClose, onSuccess }) => {
    const [activeTab, setActiveTab] = useState('demographics');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);

    // States
    const [demoData, setDemoData] = useState({
        firstName: profile.firstName || '',
        middleName: profile.middleName || '',
        lastName: profile.lastName || '',
        gender: profile.gender || 'MALE',
        dateOfBirth: profile.dateOfBirth || '',
        maritalStatus: profile.maritalStatus || 'SINGLE'
    });

    const [addressData, setAddressData] = useState({
        primaryPhone: profile.address?.primaryPhone || '',
        secondaryPhone: profile.address?.secondaryPhone || '',
        email: profile.address?.email || '',
        region: profile.address?.region || '',
        zoneSubcity: profile.address?.zoneSubcity || '',
        woreda: profile.address?.woreda || '',
        houseNumber: profile.address?.houseNumber || ''
    });

    const [empData, setEmpData] = useState({
        occupationSector: profile.employment?.occupationSector || '',
        employerName: profile.employment?.employerName || '',
        monthlyIncome: profile.employment?.monthlyIncome || 0,
        tinNumber: profile.employment?.tinNumber || '',
        employeeId: profile.employment?.employeeId || '',
        externalEmployeeId: profile.employment?.externalEmployeeId || ''
    });

    const [remarks, setRemarks] = useState('');

    const clearMessages = () => { setError(null); setSuccessMsg(null); };

    const handleUpdateDemographics = async (e) => {
        e.preventDefault();
        setIsSubmitting(true); clearMessages();
        try {
            await memberProfileApi.updateDemographics(profile.userId, demoData);
            setSuccessMsg("Demographics updated successfully");
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to update demographics');
        } finally { setIsSubmitting(false); }
    };

    const handleSaveAddress = async (e) => {
        e.preventDefault();
        setIsSubmitting(true); clearMessages();
        try {
            await memberProfileApi.saveAddress(profile.userId, addressData);
            setSuccessMsg("Address details saved successfully");
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to save address');
        } finally { setIsSubmitting(false); }
    };

    const handleSaveEmployment = async (e) => {
        e.preventDefault();
        setIsSubmitting(true); clearMessages();
        try {
            await memberProfileApi.saveEmployment(profile.userId, empData);
            setSuccessMsg("Employment details saved successfully");
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to save employment');
        } finally { setIsSubmitting(false); }
    };

    const handleApprove = async () => {
        if (!window.confirm("Approve this member onboarding?")) return;
        setIsSubmitting(true); clearMessages();
        try {
            await memberProfileApi.approveOnboarding(profile.userId, remarks);
            setSuccessMsg("Member successfully approved!");
            onSuccess();
            setTimeout(onClose, 1500); // close after success
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to approve onboarding');
        } finally { setIsSubmitting(false); }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card w-full max-w-2xl rounded-3xl shadow-2xl border border-[var(--bdae-border)] flex flex-col max-h-[95vh]">

                {/* Header */}
                <div className="flex items-center justify-between p-5 border-b border-[var(--bdae-border)] shrink-0 bg-black/5 dark:bg-white/5">
                    <div>
                        <h2 className="text-lg font-extrabold text-[var(--bdae-text-primary)]">
                            {profile.firstName} {profile.lastName}
                        </h2>
                        <div className="flex items-center gap-3 mt-1">
                            <span className="font-mono text-[10px] bg-[var(--bdae-primary)] px-2 py-0.5 rounded text-white font-bold">
                                {profile.memberNo || 'PENDING MEMBER NO'}
                            </span>
                            <span className={`text-[10px] px-2 py-0.5 rounded font-bold border ${profile.status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20' : 'bg-amber-500/10 text-amber-600 border-amber-500/20'}`}>
                                {profile.status}
                            </span>
                        </div>
                    </div>
                    <button onClick={onClose} className="p-2 rounded-lg hover:bg-black/10 dark:hover:bg-white/10 text-[var(--bdae-text-secondary)] transition-colors">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Tabs */}
                <div className="flex border-b border-[var(--bdae-border)] px-5 sticky top-0 bg-[var(--bdae-bg)] shrink-0 z-10">
                    <button onClick={() => { setActiveTab('demographics'); clearMessages(); }} className={`flex items-center gap-2 py-3 px-4 text-xs font-bold transition-all relative ${activeTab === 'demographics' ? 'text-[var(--bdae-primary)]' : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'}`}>
                        <UserCircle className="w-4 h-4" /> Demographics
                        {activeTab === 'demographics' && <div className="absolute bottom-0 left-0 w-full h-0.5 bg-[var(--bdae-primary)] rounded-t" />}
                    </button>
                    <button onClick={() => { setActiveTab('address'); clearMessages(); }} className={`flex items-center gap-2 py-3 px-4 text-xs font-bold transition-all relative ${activeTab === 'address' ? 'text-[var(--bdae-primary)]' : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'}`}>
                        <MapPin className="w-4 h-4" /> Contact Address
                        {activeTab === 'address' && <div className="absolute bottom-0 left-0 w-full h-0.5 bg-[var(--bdae-primary)] rounded-t" />}
                    </button>
                    <button onClick={() => { setActiveTab('employment'); clearMessages(); }} className={`flex items-center gap-2 py-3 px-4 text-xs font-bold transition-all relative ${activeTab === 'employment' ? 'text-[var(--bdae-primary)]' : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'}`}>
                        <Briefcase className="w-4 h-4" /> Employment
                        {activeTab === 'employment' && <div className="absolute bottom-0 left-0 w-full h-0.5 bg-[var(--bdae-primary)] rounded-t" />}
                    </button>
                    <button onClick={() => { setActiveTab('kyc'); clearMessages(); }} className={`flex items-center gap-2 py-3 px-4 text-xs font-bold transition-all relative ${activeTab === 'kyc' ? 'text-[var(--bdae-primary)]' : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'}`}>
                        <FileText className="w-4 h-4" /> KYC Identifications
                        {activeTab === 'kyc' && <div className="absolute bottom-0 left-0 w-full h-0.5 bg-[var(--bdae-primary)] rounded-t" />}
                    </button>
                    <button onClick={() => { setActiveTab('governance'); clearMessages(); }} className={`flex items-center gap-2 py-3 px-4 text-xs font-bold transition-all relative ${activeTab === 'governance' ? 'text-[var(--bdae-primary)]' : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'}`}>
                        <ShieldCheck className="w-4 h-4" /> Governance
                        {activeTab === 'governance' && <div className="absolute bottom-0 left-0 w-full h-0.5 bg-[var(--bdae-primary)] rounded-t" />}
                    </button>
                </div>

                {/* scrollable Body */}
                <div className="p-5 overflow-y-auto grow">
                    {error && (
                        <div className="p-3 mb-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs flex gap-2">
                            <AlertCircle className="w-4 h-4 shrink-0" /><span>{error}</span>
                        </div>
                    )}
                    {successMsg && (
                        <div className="p-3 mb-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 text-xs flex gap-2">
                            <CheckCircle className="w-4 h-4 shrink-0" /><span>{successMsg}</span>
                        </div>
                    )}

                    {activeTab === 'demographics' && (
                        <form id="demoForm" onSubmit={handleUpdateDemographics} className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">First Name</label>
                                    <input type="text" required value={demoData.firstName} onChange={(e) => setDemoData({ ...demoData, firstName: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Middle Name</label>
                                    <input type="text" required value={demoData.middleName} onChange={(e) => setDemoData({ ...demoData, middleName: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Last Name</label>
                                    <input type="text" required value={demoData.lastName} onChange={(e) => setDemoData({ ...demoData, lastName: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">DOB</label>
                                    <input type="date" required value={demoData.dateOfBirth} onChange={(e) => setDemoData({ ...demoData, dateOfBirth: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Gender</label>
                                    <select value={demoData.gender} onChange={(e) => setDemoData({ ...demoData, gender: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl">
                                        <option value="MALE">Male</option><option value="FEMALE">Female</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Marital Status</label>
                                    <select value={demoData.maritalStatus} onChange={(e) => setDemoData({ ...demoData, maritalStatus: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl">
                                        <option value="SINGLE">Single</option><option value="MARRIED">Married</option>
                                        <option value="DIVORCED">Divorced</option><option value="WIDOWED">Widowed</option>
                                    </select>
                                </div>
                            </div>
                        </form>
                    )}

                    {activeTab === 'address' && (
                        <form id="addressForm" onSubmit={handleSaveAddress} className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Primary Phone</label>
                                    <input type="text" value={addressData.primaryPhone} onChange={(e) => setAddressData({ ...addressData, primaryPhone: e.target.value })} placeholder="+251911223344" className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl font-mono" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Secondary Phone</label>
                                    <input type="text" value={addressData.secondaryPhone} onChange={(e) => setAddressData({ ...addressData, secondaryPhone: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl font-mono" />
                                </div>
                                <div className="col-span-2 grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Region</label>
                                        <input type="text" required value={addressData.region} onChange={(e) => setAddressData({ ...addressData, region: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                    </div>
                                    <div>
                                        <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Zone / Subcity</label>
                                        <input type="text" required value={addressData.zoneSubcity} onChange={(e) => setAddressData({ ...addressData, zoneSubcity: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                    </div>
                                    <div>
                                        <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Woreda</label>
                                        <input type="text" required value={addressData.woreda} onChange={(e) => setAddressData({ ...addressData, woreda: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                    </div>
                                    <div>
                                        <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">House No</label>
                                        <input type="text" value={addressData.houseNumber} onChange={(e) => setAddressData({ ...addressData, houseNumber: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                    </div>
                                </div>
                            </div>
                        </form>
                    )}

                    {activeTab === 'employment' && (
                        <form id="empForm" onSubmit={handleSaveEmployment} className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Sector</label>
                                    <input type="text" required value={empData.occupationSector} onChange={(e) => setEmpData({ ...empData, occupationSector: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Employer</label>
                                    <input type="text" required value={empData.employerName} onChange={(e) => setEmpData({ ...empData, employerName: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Monthly Income</label>
                                    <input type="number" min="0" required value={empData.monthlyIncome} onChange={(e) => setEmpData({ ...empData, monthlyIncome: e.target.value })} className="w-full px-4 py-2.5 text-xs font-mono bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">TIN Number</label>
                                    <input type="text" value={empData.tinNumber} onChange={(e) => setEmpData({ ...empData, tinNumber: e.target.value })} className="w-full px-4 py-2.5 text-xs font-mono bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                </div>
                                <div className="col-span-2 grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Internal Employee ID</label>
                                        <input type="text" value={empData.employeeId} onChange={(e) => setEmpData({ ...empData, employeeId: e.target.value })} className="w-full px-4 py-2.5 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                                    </div>
                                </div>
                            </div>
                        </form>
                    )}

                    {activeTab === 'kyc' && (
                        <KycDocumentTab userId={profile.userId} />
                    )}

                    {activeTab === 'governance' && (
                        <div className="space-y-6">
                            <div className="bdae-card p-5 border border-[var(--bdae-border)] bg-black/5 rounded-xl">
                                <h3 className="text-sm font-bold text-[var(--bdae-text-primary)] flex items-center gap-2 mb-2"><ShieldCheck className="text-[var(--bdae-primary)] w-4 h-4" /> Maker-Checker Verification</h3>
                                <p className="text-xs text-[var(--bdae-text-secondary)] mb-4">You are acting as the Checker (Supervisor). Submitting this will officially approve onboarding for {profile.firstName} resulting in full member activation.</p>
                                <textarea
                                    placeholder="Supervisor verification remarks (optional)"
                                    value={remarks}
                                    onChange={e => setRemarks(e.target.value)}
                                    className="w-full px-4 py-3 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl focus:border-[var(--bdae-primary)] h-20 outline-none mb-3"
                                />
                                <PermissionGuard authorities={['MEMBER_APPROVE']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']} fallback={<p className="text-xs text-red-500 font-bold">You lack MEMBER_APPROVE permissions.</p>}>
                                    <button
                                        onClick={handleApprove}
                                        disabled={isSubmitting || profile.status === 'ACTIVE'}
                                        className="bdae-btn-primary px-5 py-2.5 text-xs font-bold rounded-xl flex items-center gap-2"
                                    >
                                        {isSubmitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4" />}
                                        {profile.status === 'ACTIVE' ? 'Already Approved' : 'Approve Onboarding'}
                                    </button>
                                </PermissionGuard>
                            </div>

                            <div className="bdae-card p-5 border border-red-500/20 bg-red-500/5 rounded-xl">
                                <h3 className="text-sm font-bold text-red-600 flex items-center gap-2 mb-2"><AlertCircle className="w-4 h-4" /> Danger Actions</h3>
                                <p className="text-xs text-[var(--bdae-text-secondary)] mb-4">Suspend or forcefully close member accounts. Warning: Closed accounts are irreversible and immediately block transacting.</p>

                                <PermissionGuard authorities={['MEMBER_UPDATE']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
                                    <div className="flex gap-3">
                                        <button
                                            onClick={async () => {
                                                if (!window.confirm("Suspend Member?")) return;
                                                try { await memberProfileApi.changeStatus(profile.userId, 'SUSPENDED'); onSuccess(); setSuccessMsg("Suspended!"); }
                                                catch (err) { setError(err.response?.data?.message || "Failed to suspend"); }
                                            }}
                                            disabled={profile.status === 'SUSPENDED'}
                                            className="px-4 py-2 bg-amber-500/10 text-amber-600 border border-amber-500/20 hover:bg-amber-500/20 text-xs font-bold rounded-xl"
                                        >Suspend Profile</button>
                                        <button
                                            onClick={async () => {
                                                if (!window.confirm("Close profile? IRREVERSIBLE.")) return;
                                                try { await memberProfileApi.changeStatus(profile.userId, 'CLOSED'); onSuccess(); setSuccessMsg("Closed!"); }
                                                catch (err) { setError(err.response?.data?.message || "Failed to close"); }
                                            }}
                                            disabled={profile.status === 'CLOSED'}
                                            className="px-4 py-2 bg-red-500/10 text-red-600 border border-red-500/20 hover:bg-red-500/20 text-xs font-bold rounded-xl"
                                        >Close Profile</button>
                                    </div>
                                </PermissionGuard>
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer */}
                {(activeTab !== 'governance' && activeTab !== 'kyc') && (
                    <div className="p-5 border-t border-[var(--bdae-border)] shrink-0 flex justify-end gap-3 bg-black/5 dark:bg-white/5">
                        <PermissionGuard authorities={['MEMBER_UPDATE']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
                            <button
                                type="submit"
                                form={activeTab === 'demographics' ? 'demoForm' : activeTab === 'address' ? 'addressForm' : 'empForm'}
                                disabled={isSubmitting}
                                className="bdae-btn-primary px-6 py-2.5 rounded-xl text-xs font-bold flex items-center justify-center gap-2 shadow-lg"
                            >
                                {isSubmitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                                <span>Save {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}</span>
                            </button>
                        </PermissionGuard>
                    </div>
                )}
            </div>
        </div>
    );
};
