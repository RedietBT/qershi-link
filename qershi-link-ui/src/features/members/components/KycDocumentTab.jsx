import React, { useState, useEffect } from 'react';
import { kycApi } from '../api/kycApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { RefreshCw, AlertCircle, CheckCircle, FileText, Upload, Plus } from 'lucide-react';

export const KycDocumentTab = ({ userId }) => {
    const [documents, setDocuments] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [showForm, setShowForm] = useState(false);

    const [formData, setFormData] = useState({
        idType: 'NATIONAL_ID',
        idNumber: '',
        issueDate: '',
        expiryDate: '',
        issuingAuthority: ''
    });

    const fetchDocuments = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await kycApi.getIdentificationsByUserId(userId);
            setDocuments(res.data || []);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch KYC documents');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchDocuments();
    }, [userId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);
        setSuccessMsg(null);

        try {
            await kycApi.submitIdentification(userId, {
                ...formData,
                issueDate: formData.issueDate || null,
                expiryDate: formData.expiryDate || null,
            });
            setSuccessMsg('KYC Document submitted successfully!');
            setShowForm(false);
            setFormData({
                idType: 'NATIONAL_ID',
                idNumber: '',
                issueDate: '',
                expiryDate: '',
                issuingAuthority: ''
            });
            fetchDocuments();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to submit document');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="space-y-6">
            {error && (
                <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs flex gap-2">
                    <AlertCircle className="w-4 h-4 shrink-0" /><span>{error}</span>
                </div>
            )}
            {successMsg && (
                <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 text-xs flex gap-2">
                    <CheckCircle className="w-4 h-4 shrink-0" /><span>{successMsg}</span>
                </div>
            )}

            <div className="flex justify-between items-center bg-black/5 dark:bg-white/5 p-3 rounded-xl border border-[var(--bdae-border)]">
                <div className="text-xs font-bold text-[var(--bdae-text-secondary)]">Registered Identifications</div>
                <PermissionGuard authorities={['KYC_SUBMIT']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
                    <button
                        type="button"
                        onClick={() => setShowForm(!showForm)}
                        className="px-3 py-1.5 bg-[var(--bdae-primary)] text-white text-[11px] font-bold rounded-lg flex items-center gap-1"
                    >
                        {showForm ? <X className="w-3 h-3" /> : <Plus className="w-3 h-3" />}
                        {showForm ? 'Cancel Submission' : 'Submit New ID'}
                    </button>
                </PermissionGuard>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="p-4 border border-[var(--bdae-border)] rounded-xl space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">ID Type</label>
                            <select
                                value={formData.idType}
                                onChange={e => setFormData({ ...formData, idType: e.target.value })}
                                className="w-full px-3 py-2 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl"
                            >
                                <option value="NATIONAL_ID">National ID</option>
                                <option value="PASSPORT">Passport</option>
                                <option value="DRIVING_LICENSE">Driving License</option>
                                <option value="KEBELE_ID">Kebele ID</option>
                                <option value="TAX_ID">Tax ID (TIN)</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">ID Number</label>
                            <input type="text" required value={formData.idNumber} onChange={e => setFormData({ ...formData, idNumber: e.target.value })} className="w-full px-3 py-2 text-xs font-mono bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                        </div>
                        <div>
                            <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Issue Date</label>
                            <input type="date" value={formData.issueDate} onChange={e => setFormData({ ...formData, issueDate: e.target.value })} className="w-full px-3 py-2 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                        </div>
                        <div>
                            <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Expiry Date</label>
                            <input type="date" value={formData.expiryDate} onChange={e => setFormData({ ...formData, expiryDate: e.target.value })} className="w-full px-3 py-2 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                        </div>
                        <div className="col-span-2">
                            <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Issuing Authority</label>
                            <input type="text" value={formData.issuingAuthority} onChange={e => setFormData({ ...formData, issuingAuthority: e.target.value })} placeholder="e.g. Immigration Office" className="w-full px-3 py-2 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                        </div>
                    </div>
                    <div className="flex justify-end">
                        <button type="submit" disabled={isSubmitting} className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-lg flex items-center gap-2">
                            {isSubmitting ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Upload className="w-3.5 h-3.5" />}
                            Submit Document
                        </button>
                    </div>
                </form>
            )}

            {isLoading ? (
                <div className="text-center py-6">
                    <RefreshCw className="w-5 h-5 animate-spin mx-auto text-[var(--bdae-secondary)]" />
                </div>
            ) : documents.length === 0 ? (
                <div className="text-center py-6 text-xs text-[var(--bdae-text-secondary)]">
                    <FileText className="w-8 h-8 opacity-20 mx-auto mb-2" />
                    No KYC documents have been uploaded for this member.
                </div>
            ) : (
                <div className="space-y-3">
                    {documents.map((doc, idx) => (
                        <div key={doc.identificationId || idx} className="p-4 border border-[var(--bdae-border)] rounded-xl bg-black/5 dark:bg-white/5 flex flex-col md:flex-row md:items-center justify-between gap-4">
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-xl bg-cyan-500/10 text-cyan-500 flex items-center justify-center shrink-0">
                                    <FileText className="w-5 h-5" />
                                </div>
                                <div>
                                    <div className="text-xs font-bold text-[var(--bdae-text-primary)]">
                                        {doc.idType.replace('_', ' ')}
                                    </div>
                                    <div className="text-[10px] text-[var(--bdae-text-secondary)] font-mono mt-0.5">
                                        ID: {doc.idNumber}
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center gap-4 text-[10px]">
                                {doc.expiryDate && (
                                    <div className="text-[var(--bdae-text-secondary)] whitespace-nowrap">
                                        Exp: <strong>{doc.expiryDate}</strong>
                                    </div>
                                )}
                                <div className={`px-2 py-1 rounded font-bold border ${doc.status === 'VERIFIED' ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20' : doc.status === 'REJECTED' ? 'bg-red-500/10 text-red-600 border-red-500/20' : 'bg-amber-500/10 text-amber-600 border-amber-500/20'}`}>
                                    {doc.status}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
