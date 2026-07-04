(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const layoutStart = `<div class="app-shell container-fluid">
  <header class="app-header card border-0 shadow-sm">
    <div class="brand">
      <div class="brand-mark">VC</div>
      <div>
        <h1>Quản lý cắt video</h1>
        <p>Upload hoặc dán link video, phân tích đoạn nổi bật và quản lý lịch sử render.</p>
      </div>
    </div>
    <div class="health" id="health">Đang kiểm tra FFmpeg...</div>
  </header>

  <nav class="top-menu nav nav-pills" aria-label="Menu chính">
    <button class="menu-item nav-link active" type="button" data-view="localVideoView">Video Cắt ghép</button>
    <button class="menu-item nav-link" type="button" data-view="splitVideoView">Video Tách</button>
    <button class="menu-item nav-link" type="button" data-view="manualEditView">Edit video có sẵn</button>
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
  </main>
${layoutEnd}
${components.previewModalShell.render()}
${components.editorModalShell.render()}`;
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
