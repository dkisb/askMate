import { useEffect, useState } from 'react';
import { useUser } from '../context/UserContext.jsx';

export default function ProfilePage() {
  const { user } = useUser();
  const [userName, setUserName] = useState('');
  const [email, setEmail] = useState('');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    setUserName(user?.userName || '');
    setEmail(user?.email || '');
  }, [user]);

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    setMessage('');
    try {
      const token = (() => { try { return localStorage.getItem('jwtToken'); } catch { return null; } })();
      const res = await fetch('/api/user/me', {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: 'Bearer ' + token } : {}),
        },
        body: JSON.stringify({ userName, email }),
      });
      if (!res.ok) throw new Error('Failed to save profile');
      setMessage('Profile updated');
    } catch (err) {
      setMessage(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Your Profile</h1>
      <form onSubmit={handleSave} className="space-y-4 max-w-md">
        <div>
          <label className="block text-sm font-medium mb-1">Username</label>
          <input
            type="text"
            className="w-full p-2.5 rounded-lg border"
            value={userName}
            onChange={(e) => setUserName(e.target.value)}
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Email</label>
          <input
            type="email"
            className="w-full p-2.5 rounded-lg border"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 rounded-lg px-4" disabled={saving}>
          {saving ? 'Saving…' : 'Save'}
        </button>
      </form>
      {message && <div className="mt-3 text-sm">{message}</div>}
    </div>
  );
}

