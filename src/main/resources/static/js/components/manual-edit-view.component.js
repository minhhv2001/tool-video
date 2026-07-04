(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const markup = `<section class="view" id="manualEditView">
      <div class="layout manual-edit-layout">
        <section class="panel upload-panel card border-0 shadow-sm">
          <h2>Edit video có sẵn</h2>
          <form id="manualEditForm">
            <label class="dropzone manual-edit-dropzone" id="manualEditDropzone">
              <input id="manualEditVideoFile" name="video" type="file" accept="video/*">
              <span class="drop-title">Kéo video muốn chỉnh sửa vào đây</span>
              <span class="drop-subtitle">Hoặc bấm để chọn một file video từ máy tính</span>
            </label>
            <div class="file-list" id="manualEditFileList"></div>
            <div class="actions">
              <button class="btn btn-primary" id="manualEditOpenButton" type="submit" disabled>Mở trình chỉnh sửa</button>
              <button class="btn btn-light secondary" id="manualEditClearButton" type="button" disabled>Xóa file đã chọn</button>
            </div>
          </form>
          <div class="progress-card">
            <div class="ring" id="manualEditRing" style="--angle: 0deg">
              <div class="percent" id="manualEditPercent">0%</div>
            </div>
            <div>
              <div class="state" id="manualEditState">Sẵn sàng nhận video để chỉnh sửa.</div>
              <div class="bar"><div class="bar-fill" id="manualEditBarFill"></div></div>
            </div>
          </div>
        </section>

        <section class="panel history-panel manual-edit-help card border-0 shadow-sm">
          <div class="table-header">
            <div>
              <h2>Quy trình chỉnh sửa</h2>
              <p>File upload sẽ được lưu thành một bản nháp local, sau đó mở bằng bộ chỉnh sửa video hiện có.</p>
            </div>
          </div>
          <div class="manual-edit-steps">
            <div><b>1</b><span>Kéo hoặc chọn một video từ máy.</span></div>
            <div><b>2</b><span>Bấm mở trình chỉnh sửa để cắt, tách, thêm text, thêm nhạc, zoom, xoay.</span></div>
            <div><b>3</b><span>Lưu đè bản nháp hoặc xuất thành bản ghi mới trong danh sách Video Cắt ghép.</span></div>
          </div>
        </section>
      </div>
    </section>`;

  function render() {
    return markup;
  }

  components.manualEditView = { render };
}());
