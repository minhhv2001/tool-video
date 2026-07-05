(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const markup = `<section class="view" id="manualEditView">
      <div class="layout">
        <section class="panel upload-panel card border-0 shadow-sm">
          <h2>Edit video có sẵn</h2>
          <form id="manualEditForm">
            <div class="source-toggle" role="group" aria-label="Nguồn video">
              <label><input id="manualEditSourceUpload" name="manualEditSourceMode" type="radio" value="upload" checked> Video upload lên</label>
              <label><input id="manualEditSourceNetwork" name="manualEditSourceMode" type="radio" value="network"> Nguồn video từ mạng</label>
            </div>

            <div class="source-panel" id="manualEditUploadSourcePanel">
              <label class="dropzone manual-edit-dropzone" id="manualEditDropzone">
                <input id="manualEditVideoFile" name="video" type="file" accept="video/*">
                <span class="drop-title">Kéo video muốn chỉnh sửa vào đây</span>
                <span class="drop-subtitle">Hoặc bấm để chọn một file video từ máy tính</span>
              </label>
              <div class="file-list" id="manualEditFileList"></div>
            </div>

            <div class="source-panel hidden" id="manualEditNetworkSourcePanel">
              <label class="note-control">Link video
                <input id="manualEditVideoUrl" name="videoUrl" type="url" placeholder="Dán link video cần tải tại đây">
                <span class="hint">Server sẽ tải bản nét nhất có thể, tối đa 2K, rồi mở bằng trình chỉnh sửa.</span>
              </label>
              <label class="note-control">Cookies.txt nếu link cần đăng nhập
                <input id="manualEditCookiesFilePath" name="cookiesFilePath" type="text" placeholder="Ví dụ: %USERPROFILE%\\Downloads\\instagram-cookies.txt">
              </label>
            </div>

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

        <section class="panel history-panel card border-0 shadow-sm">
          <div class="table-header">
            <div>
              <h2>Danh sách video đã edit</h2>
              <p id="manualEditHistorySummary">Đang tải lịch sử...</p>
            </div>
            <div class="table-actions">
              <button class="btn btn-outline-danger danger" id="manualEditDeleteSelectedButton" type="button" disabled>Xóa đã chọn</button>
            </div>
          </div>

          <div class="table-wrap">
            <table class="table table-hover align-middle">
              <thead>
              <tr>
                <th class="action-cell">Thao tác</th>
                <th class="select-cell"><input id="manualEditSelectAllHistory" type="checkbox" aria-label="Chọn tất cả video edit trên trang"></th>
                <th class="time-cell">Thời gian</th>
                <th class="file-cell">Video gốc</th>
                <th class="folder-cell">Thư mục gốc</th>
                <th class="note-cell">Ghi chú</th>
                <th class="status-cell">Trạng thái</th>
                <th class="clips-cell">Số đoạn</th>
                <th class="duration-cell">Thời lượng</th>
                <th class="folder-cell">Thư mục kết quả</th>
              </tr>
              </thead>
              <tbody id="manualEditHistoryBody"></tbody>
            </table>
          </div>
          <div class="pager" id="manualEditPager"></div>
        </section>
      </div>
    </section>`;

  function render() {
    return markup;
  }

  components.manualEditView = { render };
}());
