import React, { useState } from 'react';
import { X, UserPlus, RefreshCw, AlertCircle } from 'lucide-react';
import { memberProfileApi } from '../api/memberProfileApi';

export const CreateProfileModal = ({ userId, msisdn, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        firstName: '',
        middleName: '',
        lastName: '',
        gender: 'MALE',
        dateOfBirth: '',
        maritalStatus: 'SINGLE'
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);
        try {
            const payload = {
                ...formData,
                userId: userId
            };
            await memberProfileApi.createProfile(payload);
            onSuccess();
            onClose();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create member profile.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card w-full max-w-lg rounded-3xl shadow-2xl border border-[var(--bdae-border)] flex flex-col max-h-[90vh]">

                {/* Header */}
                <div className="flex items-center justify-between p-5 border-b border-[var(--bdae-border)] shrink-0">
                    <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 rounded-xl bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center">
                            <UserPlus className="w-5 h-5" />
                        </div>
                        <div>
                            <h2 className="text-sm font-extrabold text-[var(--bdae-text-primary)]">Register Member Profile</h2>
                            <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                                Linking profile to Phone: <strong className="font-mono text-[var(--bdae-text-primary)]">{msisdn}</strong>
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] transition-colors"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Body */}
                <div className="p-5 overflow-y-auto space-y-4">
                    {error && (
                        <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs flex gap-2">
                            <AlertCircle className="w-4 h-4 shrink-0" />
                            <span>{error}</span>
                        </div>
                    )}

                    <form id="createProfileForm" onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase tracking-wider">
                                    First Name
                                </label>
                                <input
                                    type="text"
                                    required
                                    pattern="^[A-Za-z\s\-']{2,100}$"
                                    title="Alphabetical characters only"
                                    value={formData.firstName}
                                    onChange={(e) => setFormData(prev => ({ ...prev, firstName: e.target.value }))}
                                    className="w-full px-4 py-2.5 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                                />
                            </div>

                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase tracking-wider">
                                    Middle Name (Father)
                                </label>
                                <input
                                    type="text"
                                    required
                                    pattern="^[A-Za-z\s\-']{2,100}$"
                                    title="Alphabetical characters only"
                                    value={formData.middleName}
                                    onChange={(e) => setFormData(prev => ({ ...prev, middleName: e.target.value }))}
                                    className="w-full px-4 py-2.5 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase tracking-wider">
                                    Last Name (Grandfather)
                                </label>
                                <input
                                    type="text"
                                    required
                                    pattern="^[A-Za-z\s\-']{2,100}$"
                                    title="Alphabetical characters only"
                                    value={formData.lastName}
                                    onChange={(e) => setFormData(prev => ({ ...prev, lastName: e.target.value }))}
                                    className="w-full px-4 py-2.5 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                                />
                            </div>

                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase tracking-wider">
                                    Date of Birth
                                </label>
                                <input
                                    type="date"
                                    required
                                    max={new Date().toISOString().split('T')[0]} // Max today
                                    value={formData.dateOfBirth}
                                    onChange={(e) => setFormData(prev => ({ ...prev, dateOfBirth: e.target.value }))}
                                    className="w-full px-4 py-2.5 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase tracking-wider">
                                    Gender
                                </label>
                                <select
                                    value={formData.gender}
                                    onChange={(e) => setFormData(prev => ({ ...prev, gender: e.target.value }))}
                                    className="w-full px-4 py-2.5 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                                >
                                    <option value="MALE">Male</option>
                                    <option value="FEMALE">Female</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase tracking-wider">
                                    Marital Status
                                </label>
                                <select
                                    value={formData.maritalStatus}
                                    onChange={(e) => setFormData(prev => ({ ...prev, maritalStatus: e.target.value }))}
                                    className="w-full px-4 py-2.5 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                                >
                                    <option value="SINGLE">Single</option>
                                    <option value="MARRIED">Married</option>
                                    <option value="DIVORCED">Divorced</option>
                                    <option value="WIDOWED">Widowed</option>
                                </select>
                            </div>
                        </div>
                    </form>
                </div>

                {/* Footer */}
                <div className="p-5 border-t border-[var(--bdae-border)] shrink-0 flex justify-end gap-3 bg-black/5 dark:bg-white/5">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-5 py-2.5 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5 transition-all"
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        form="createProfileForm"
                        disabled={isSubmitting}
                        className="bdae-btn-primary px-6 py-2.5 rounded-xl text-xs font-bold flex items-center justify-center gap-2 shadow-lg"
                    >
                        {isSubmitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <UserPlus className="w-4 h-4" />}
                        <span>Register Profile</span>
                    </button>
                </div>
            </div>
        </div>
    );
};
