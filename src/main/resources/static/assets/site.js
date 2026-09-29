(function () {
    'use strict';

    const auth = window.UsedTradeAuth;
    const byId = id => document.getElementById(id);
    const roleNames = { seller: '판매자', buyer: '구매자' };
    const money = new Intl.NumberFormat('ko-KR');
    const roleSelect = byId('site-role');
    const notice = byId('page-notice');
    const grid = byId('product-grid');
    const detailPanel = byId('detail-panel');
    const dealPanel = byId('deal-panel');
    const dealList = byId('deal-list');
    const recentList = byId('recent-list');
    let products = [];
    let selectedProduct = null;
    let detailSequence = 0;
    let dealsSequence = 0;

    function showNotice(message, kind = 'info') {
        notice.textContent = message;
        notice.className = 'page-notice is-visible is-' + kind;
    }

    function showFeedback(element, message, kind = '') {
        element.textContent = message;
        element.className = 'form-feedback' + (kind ? ' is-' + kind : '');
    }

    function errorMessage(status, data) {
        const message = data && typeof data === 'object'
            ? data.message || data.error
            : typeof data === 'string' ? data : null;
        return 'HTTP ' + status + (message ? ' · ' + String(message).slice(0, 180) : ' · 요청에 실패했습니다.');
    }

    async function request(method, path, options = {}) {
        const headers = {};
        const requestRole = auth.getActiveRole();
        if (options.auth) {
            const session = auth.getSession(requestRole);
            if (!session) throw new Error(roleNames[requestRole] + ' 계정으로 먼저 로그인하세요.');
            headers.Authorization = 'Bearer ' + session.accessToken;
        }

        let body;
        if (options.body instanceof FormData) {
            body = options.body;
        } else if (options.body !== undefined) {
            headers['Content-Type'] = 'application/json';
            body = JSON.stringify(options.body);
        }

        let response;
        try {
            response = await fetch(path, { method, headers, body });
        } catch (_) {
            throw new Error('서버에 연결할 수 없습니다. 잠시 후 다시 시도하세요.');
        }
        const raw = await response.text();
        let data = raw || null;
        if (raw) {
            try { data = JSON.parse(raw); } catch (_) { /* 일반 텍스트 응답 유지 */ }
        }
        if (response.status === 401 && options.auth) auth.clearSession(requestRole);
        if (!response.ok) throw new Error(errorMessage(response.status, data));
        return { status: response.status, data };
    }

    function statusInfo(status) {
        if (status === 'SELLING') return { label: '판매 중', className: 'selling' };
        if (status === 'SOLD') return { label: '판매 완료', className: 'sold' };
        if (status === 'RESERVED') return { label: '예약 중', className: 'other' };
        return { label: String(status || '상태 미확인'), className: 'other' };
    }

    function statusBadge(status) {
        const info = statusInfo(status);
        const badge = document.createElement('span');
        badge.className = 'status ' + info.className;
        badge.textContent = info.label;
        return badge;
    }

    function priceText(price) {
        return Number.isFinite(Number(price)) ? money.format(Number(price)) + '원' : '가격 정보 없음';
    }

    function renderImage(container, path, title) {
        container.replaceChildren();
        if (typeof path !== 'string' || !path.startsWith('/uploads/') || path.includes('..')) {
            container.textContent = '사진 없음';
            return;
        }
        const image = document.createElement('img');
        image.src = path;
        image.alt = title + ' 사진';
        image.loading = 'lazy';
        image.addEventListener('error', () => { container.textContent = '사진을 불러올 수 없습니다.'; });
        container.append(image);
    }

    function renderAccount() {
        roleSelect.value = auth.getActiveRole();
        for (const role of auth.roles) {
            const session = auth.getSession(role);
            const chip = byId('site-' + role + '-session');
            chip.textContent = session
                ? roleNames[role] + ' · ' + session.nickname + ' (#' + session.userId + ')'
                : roleNames[role] + ' · 로그인 전';
            chip.classList.toggle('is-ready', Boolean(session));
        }
        byId('logout-button').disabled = !auth.getSession();
    }

    function renderProducts() {
        grid.replaceChildren();
        if (products.length === 0) {
            const empty = document.createElement('p');
            empty.className = 'empty';
            empty.textContent = '등록된 상품이 없습니다. 첫 번째 물건을 올려보세요.';
            grid.append(empty);
            return;
        }
        for (const product of products) {
            const card = document.createElement('button');
            card.type = 'button';
            card.className = 'product-card';
            card.classList.toggle('is-selected', selectedProduct && selectedProduct.id === product.id);
            card.setAttribute('aria-label', product.title + ' 상세 보기');
            card.addEventListener('click', () => openDetail(product.id));

            const image = document.createElement('span');
            image.className = 'product-image';
            renderImage(image, product.imagePath, product.title);
            const body = document.createElement('span');
            body.className = 'product-card-body';
            const title = document.createElement('span');
            title.className = 'product-card-title';
            title.textContent = product.title;
            const price = document.createElement('span');
            price.className = 'product-card-price';
            price.textContent = priceText(product.price);
            const meta = document.createElement('span');
            meta.className = 'product-card-meta';
            meta.textContent = product.category + ' · 판매자 #' + product.sellerId;
            body.append(statusBadge(product.status), title, price, meta);
            card.append(image, body);
            grid.append(card);
        }
    }

    async function loadProducts() {
        try {
            const result = await request('GET', '/api/products');
            if (!Array.isArray(result.data)) throw new Error('상품 목록 응답 형식이 올바르지 않습니다.');
            products = result.data;
            renderProducts();
            byId('total-count').textContent = String(products.length);
            byId('selling-count').textContent = String(products.filter(p => p.status === 'SELLING').length);
            byId('list-feedback').textContent = 'HTTP ' + result.status + ' · 상품 ' + products.length + '개';
        } catch (error) {
            byId('list-feedback').textContent = error.message;
            showNotice(error.message, 'error');
        }
    }

    function actionButton(label, primary, handler) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = primary ? 'primary-button' : 'secondary-button';
        button.textContent = label;
        button.addEventListener('click', async () => {
            button.disabled = true;
            try { await handler(); } catch (error) { showNotice(error.message, 'error'); }
            finally { button.disabled = false; }
        });
        return button;
    }

    function renderDetail() {
        if (!selectedProduct) return;
        const product = selectedProduct;
        detailPanel.hidden = false;
        byId('detail-title').textContent = product.title;
        byId('detail-price').textContent = priceText(product.price);
        byId('detail-meta').textContent = product.category + ' · 판매자 #' + product.sellerId + ' · 상품 #' + product.id;
        byId('detail-description').textContent = product.content;
        renderImage(byId('detail-image'), product.imagePath, product.title);
        const info = statusInfo(product.status);
        const status = byId('detail-status');
        status.textContent = info.label;
        status.className = 'status ' + info.className;

        const actions = byId('detail-actions');
        actions.replaceChildren();
        const session = auth.getSession();
        if (session && session.userId === product.sellerId) {
            actions.append(actionButton('거래 신청 목록 보기', false, loadDeals));
        } else if (product.status === 'SELLING' && session) {
            actions.append(actionButton('거래 신청하기', true, async () => {
                const result = await request('POST', '/api/products/' + product.id + '/application', { auth: true });
                showNotice('HTTP ' + result.status + ' · 거래 신청 완료 (거래 #' + result.data.dealId + ')', 'success');
            }));
        } else {
            const message = document.createElement('p');
            message.textContent = product.status === 'SELLING'
                ? '거래를 신청하려면 위에서 로그인하세요.'
                : '판매가 종료된 상품입니다.';
            actions.append(message);
        }
    }

    async function openDetail(productId) {
        const sequence = ++detailSequence;
        try {
            const result = await request('GET', '/api/products/' + productId,
                { auth: Boolean(auth.getSession()) });
            if (sequence !== detailSequence) return;
            selectedProduct = result.data;
            dealPanel.hidden = true;
            dealList.replaceChildren();
            renderDetail();
            renderProducts();
            detailPanel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        } catch (error) {
            if (sequence === detailSequence) showNotice(error.message, 'error');
        }
    }

    async function loadRecent() {
        const role = auth.getActiveRole();
        const button = byId('load-recent');
        button.disabled = true;
        try {
            const result = await request('GET', '/api/products/recent', { auth: true });
            if (auth.getActiveRole() !== role) return;
            if (!Array.isArray(result.data)) throw new Error('최근 본 상품 응답 형식이 올바르지 않습니다.');
            recentList.replaceChildren();
            if (result.data.length === 0) {
                recentList.textContent = '아직 본 상품이 없습니다.';
            }
            for (const product of result.data) {
                const item = document.createElement('button');
                item.type = 'button';
                item.className = 'recent-item';
                const title = document.createElement('strong');
                title.textContent = product.title;
                const meta = document.createElement('small');
                meta.textContent = statusInfo(product.status).label + ' · 상품 #' + product.id;
                item.append(title, meta);
                item.addEventListener('click', () => openDetail(product.id));
                recentList.append(item);
            }
            showFeedback(byId('recent-feedback'), 'HTTP ' + result.status + ' · ' + result.data.length + '개', 'success');
        } catch (error) {
            showFeedback(byId('recent-feedback'), error.message, 'error');
        } finally {
            button.disabled = false;
        }
    }

    function renderDeals(deals) {
        dealList.replaceChildren();
        if (deals.length === 0) {
            const empty = document.createElement('p');
            empty.className = 'form-feedback';
            empty.textContent = '아직 거래 신청이 없습니다.';
            dealList.append(empty);
            return;
        }
        for (const deal of deals) {
            const row = document.createElement('div');
            row.className = 'deal-row';
            const description = document.createElement('p');
            description.textContent = '구매자 #' + deal.buyerId + ' · 거래 #' + deal.dealId;
            const state = document.createElement('small');
            state.textContent = deal.status === 'REQUESTED' ? '승인 대기' : '승인 완료';
            const text = document.createElement('div');
            text.append(description, state);
            row.append(text);
            if (deal.status === 'REQUESTED' && selectedProduct.status === 'SELLING') {
                row.append(actionButton('승인', true, () => approveDeal(deal.dealId)));
            }
            dealList.append(row);
        }
    }

    async function loadDeals() {
        if (!selectedProduct) return;
        const productId = selectedProduct.id;
        const role = auth.getActiveRole();
        const sequence = ++dealsSequence;
        const result = await request('GET', '/api/products/' + productId + '/deals', { auth: true });
        if (sequence !== dealsSequence || !selectedProduct || selectedProduct.id !== productId || auth.getActiveRole() !== role) return;
        if (!Array.isArray(result.data)) throw new Error('신청 목록 응답 형식이 올바르지 않습니다.');
        dealPanel.hidden = false;
        renderDeals(result.data);
        showNotice('HTTP ' + result.status + ' · 거래 신청 ' + result.data.length + '건', 'info');
    }

    async function approveDeal(dealId) {
        const productId = selectedProduct.id;
        const result = await request('POST', '/api/deals/' + dealId + '/approve', { auth: true });
        showNotice('HTTP ' + result.status + ' · 거래 승인 완료', 'success');
        await loadProducts();
        await openDetail(productId);
        await loadDeals();
    }

    function bindForm(formId, feedbackId, handler) {
        const form = byId(formId);
        const feedback = byId(feedbackId);
        form.addEventListener('submit', async event => {
            event.preventDefault();
            const button = form.querySelector('button[type="submit"]');
            button.disabled = true;
            showFeedback(feedback, '요청 중입니다.');
            try { await handler(form, feedback); }
            catch (error) { showFeedback(feedback, error.message, 'error'); }
            finally { button.disabled = false; }
        });
    }

    bindForm('signup-form', 'signup-feedback', async (form, feedback) => {
        const email = form.elements.email.value.trim();
        const result = await request('POST', '/api/users', {
            body: { email, password: form.elements.password.value, nickname: form.elements.nickname.value.trim() }
        });
        byId('login-email').value = email;
        form.reset();
        showFeedback(feedback, 'HTTP ' + result.status + ' · 계정 생성 완료. 위에서 로그인하세요.', 'success');
    });

    bindForm('login-form', 'login-feedback', async (form, feedback) => {
        const role = auth.getActiveRole();
        const result = await request('POST', '/api/users/login', {
            body: { email: form.elements.email.value.trim(), password: form.elements.password.value }
        });
        const saved = auth.saveSession(role, result.data);
        form.elements.password.value = '';
        showFeedback(feedback, 'HTTP ' + result.status + ' · ' + roleNames[role] + ' 계정 로그인 완료' +
            (saved ? '' : ' · 브라우저 저장에 실패해 페이지 이동 후 다시 로그인해야 합니다.'), 'success');
    });

    bindForm('product-form', 'product-feedback', async (form, feedback) => {
        const result = await request('POST', '/api/products', { auth: true, body: new FormData(form) });
        form.reset();
        showFeedback(feedback, 'HTTP ' + result.status + ' · 상품 등록 완료', 'success');
        showNotice('새 상품이 등록됐습니다. 상품 #' + result.data.id, 'success');
        await loadProducts();
        await openDetail(result.data.id);
    });

    roleSelect.addEventListener('change', () => auth.setActiveRole(roleSelect.value));
    byId('logout-button').addEventListener('click', async () => {
        const role = auth.getActiveRole();
        const button = byId('logout-button');
        button.disabled = true;
        try {
            const result = await request('POST', '/api/users/logout', { auth: true });
            auth.clearSession(role);
            showNotice('HTTP ' + result.status + ' · ' + roleNames[role] + ' 계정 로그아웃 완료', 'success');
        } catch (error) {
            showNotice(error.message, 'error');
        } finally {
            renderAccount();
        }
    });
    byId('refresh-products').addEventListener('click', loadProducts);
    byId('load-recent').addEventListener('click', loadRecent);
    auth.subscribe(() => {
        renderAccount();
        dealPanel.hidden = true;
        dealList.replaceChildren();
        dealsSequence++;
        recentList.replaceChildren();
        showFeedback(byId('recent-feedback'), '');
        renderDetail();
    });

    renderAccount();
    loadProducts();
    setInterval(renderAccount, 60_000);
})();
