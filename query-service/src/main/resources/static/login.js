/*
 * 登录页逻辑：提交账号密码 -> POST /api/auth/login
 * 成功后将 token 存入 localStorage，跳转 dashboard 首页。
 */
(function () {
    const $msg = document.getElementById('msg');
    const $btn = document.getElementById('loginBtn');
    const $user = document.getElementById('username');
    const $pass = document.getElementById('password');

    function show(msg, type) {
        $msg.textContent = msg;
        $msg.className = 'login-msg ' + (type || '');
    }

    // 已登录则直接进 dashboard
    if (localStorage.getItem('cc_token')) {
        fetch('/api/auth/check', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('cc_token') }
        }).then(r => r.json()).then(d => {
            if (d.success) {
                location.href = '/';
            } else {
                localStorage.removeItem('cc_token');
            }
        }).catch(() => localStorage.removeItem('cc_token'));
    }

    $btn.addEventListener('click', doLogin);
    $pass.addEventListener('keydown', e => { if (e.key === 'Enter') doLogin(); });

    async function doLogin() {
        const username = $user.value.trim();
        const password = $pass.value;
        if (!username || !password) {
            show('请输入用户名和密码', 'err');
            return;
        }
        $btn.disabled = true;
        show('登录中…');
        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: username, password: password })
            });
            const data = await res.json();
            if (data.success) {
                localStorage.setItem('cc_token', data.token);
                localStorage.setItem('cc_user', JSON.stringify(data.user));
                show('登录成功，正在跳转…', 'ok');
                setTimeout(() => { location.href = '/'; }, 500);
            } else {
                show(data.message || '登录失败', 'err');
                $btn.disabled = false;
            }
        } catch (err) {
            show('网络错误：' + err.message, '  err');
            $btn.disabled = false;
        }
    }
})();
