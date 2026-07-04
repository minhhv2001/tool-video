(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const markup = `<div class="preview-modal" id="previewModal" aria-hidden="true">
  <div class="preview-backdrop" id="previewBackdrop"></div>
  <section class="preview-dialog" role="dialog" aria-modal="true" aria-labelledby="previewTitle">
    <div class="preview-header">
      <h2 id="previewTitle">Xem trước video</h2>
      <button class="preview-close" id="previewClose" type="button">Đóng</button>
    </div>
    <video id="previewVideo" controls playsinline></video>
  </section>
</div>`;

  function render() {
    return markup;
  }

  components.previewModalShell = { render };
}());