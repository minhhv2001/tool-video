(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const layoutStart = `<div class="app-shell container-fluid">
  <header class="app-header card border-0 shadow-sm">
    <div class="brand">
      <div class="brand-mark">VC</div>
      <div>
        <h1>Quản lý cắt video</h1>
        <p>Upload hoặc dán link video, phân tích đoạn nổi bật và quản lý lịch sử render.</p>
        <p>Anh em nên thường xuyên xóa video đã sử dụng rồi để tránh đầy ổ trên máy.</p>
      </div>
    </div>
    <div class="header-actions"><div class="auth-user" id="authUser" hidden></div><button class="btn btn-sm btn-outline-secondary" type="button" id="logoutButton" hidden>Dang xuat</button><div class="health" id="health">Dang kiem tra FFmpeg...</div></div>
  </header>

  <nav class="top-menu nav nav-pills" aria-label="Menu chính">
    <button class="menu-item nav-link active" type="button" data-view="localVideoView">Video Cắt ghép</button>
    <button class="menu-item nav-link" type="button" data-view="splitVideoView">Video Tách</button>
    <button class="menu-item nav-link" type="button" data-view="manualEditView">Edit video có sẵn</button>
    <button class="menu-item nav-link" type="button" data-view="facebookBatchView">Tải hàng loạt Facebook</button>
  </nav>`;
  const layoutEnd = `<footer class="app-footer">
    <span>Video Highlight Cutter</span>
    <span id="workspaceHint">Local workspace</span>
  </footer>
</div>`;

  function render() {
    return `${layoutStart}
  <main>
    ${components.localVideoView.render()}
    ${components.splitVideoView.render()}
    ${components.manualEditView.render()}
    ${components.facebookBatchView.render()}
  </main>
${layoutEnd}
${components.previewModalShell.render()}
${components.editorModalShell.render()}\n<div class="auth-gate" id="authGate" hidden>\n  <form class="auth-card" id="loginForm">\n    <h2>Đăng nhập</h2>\n    <p>Mỗi tài khoản chỉ thấy video của chính tài khoản đó.</p>\n    <label>Tên đăng nhập<input class="form-control" id="loginUsername" autocomplete="username" required></label>\n    <label>Mật khẩu<input class="form-control" id="loginPassword" type="password" autocomplete="current-password" required></label>\n    <button class="btn btn-primary w-100" type="submit" id="loginButton">Đăng nhập</button>\n    <div class="auth-error" id="loginError" hidden></div>\n  </form>\n</div>`;
  }

  function mount(target) {
    const root = typeof target === 'string' ? document.querySelector(target) : target;
    if (!root) {
      return;
    }
    root.innerHTML = render();
  }

  components.appShell = { mount, render };
}());
