(function () {
    'use strict';

    const storageKey = 'usedtrade.auth.v1';
    const roles = ['seller', 'buyer'];
    const listeners = new Set();

    function emptyState() {
        return { activeRole: 'seller', sessions: { seller: null, buyer: null } };
    }

    // JWT의 exp는 화면 표시용으로만 읽는다. 유효성 검사는 서버가 수행한다.
    function tokenExpiry(token) {
        try {
            const payload = token.split('.')[1];
            const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
            const claims = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')));
            return Number.isFinite(claims.exp) ? claims.exp * 1000 : null;
        } catch (_) {
            return null;
        }
    }

    function normalize(raw) {
        const state = emptyState();
        if (!raw || typeof raw !== 'object') return state;
        if (roles.includes(raw.activeRole)) state.activeRole = raw.activeRole;

        for (const role of roles) {
            const session = raw.sessions && raw.sessions[role];
            if (!session || typeof session.accessToken !== 'string') continue;
            const expiresAt = tokenExpiry(session.accessToken);
            if (!expiresAt || expiresAt <= Date.now()) continue;
            if (!Number.isInteger(session.userId) || typeof session.nickname !== 'string') continue;
            state.sessions[role] = {
                accessToken: session.accessToken,
                userId: session.userId,
                nickname: session.nickname,
                expiresAt
            };
        }
        return state;
    }

    function load() {
        try {
            return normalize(JSON.parse(localStorage.getItem(storageKey)));
        } catch (_) {
            return emptyState();
        }
    }

    let state = load();

    function persist() {
        try {
            localStorage.setItem(storageKey, JSON.stringify(state));
            return true;
        } catch (_) {
            return false;
        }
    }

    function notify() {
        for (const listener of listeners) listener();
    }

    function getActiveRole() {
        return state.activeRole;
    }

    function getSession(role = state.activeRole) {
        if (!roles.includes(role)) return null;
        const session = state.sessions[role];
        if (session && session.expiresAt <= Date.now()) {
            state.sessions[role] = null;
            persist();
            notify();
            return null;
        }
        return session;
    }

    function setActiveRole(role) {
        if (!roles.includes(role)) throw new Error('알 수 없는 계정입니다.');
        state.activeRole = role;
        const saved = persist();
        notify();
        return saved;
    }

    function saveSession(role, response) {
        if (!roles.includes(role)) throw new Error('알 수 없는 계정입니다.');
        const expiresAt = response && tokenExpiry(response.accessToken);
        if (!expiresAt || expiresAt <= Date.now() || !Number.isInteger(response.userId)) {
            throw new Error('로그인 응답에 유효한 접근 토큰이 없습니다.');
        }
        state.sessions[role] = {
            accessToken: response.accessToken,
            userId: response.userId,
            nickname: String(response.nickname || ''),
            expiresAt
        };
        state.activeRole = role;
        const saved = persist();
        notify();
        return saved;
    }

    function clearSession(role = state.activeRole) {
        if (!roles.includes(role)) return;
        state.sessions[role] = null;
        persist();
        notify();
    }

    function subscribe(listener) {
        listeners.add(listener);
        return () => listeners.delete(listener);
    }

    window.addEventListener('storage', event => {
        if (event.key !== storageKey) return;
        state = load();
        notify();
    });

    window.UsedTradeAuth = {
        roles, getActiveRole, getSession, setActiveRole,
        saveSession, clearSession, subscribe
    };
})();
