(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const markup = `<section class="view" id="splitVideoView">
      <div class="layout">
        <section class="panel upload-panel card border-0 shadow-sm">
          <h2>Video Tách</h2>
          <form id="splitUploadForm">
            <div class="source-toggle" role="group" aria-label="Nguồn video">
              <label><input id="splitSourceUpload" name="splitSourceMode" type="radio" value="upload" checked> Video upload lên</label>
              <label><input id="splitSourceNetwork" name="splitSourceMode" type="radio" value="network"> Nguồn video từ mạng</label>
            </div>

            <div class="source-panel" id="splitUploadSourcePanel">
              <label class="dropzone" id="splitDropzone">
                <input id="splitVideoFiles" name="videos" type="file" accept="video/*" multiple>
                <span class="drop-title">Kéo một hoặc nhiều video vào đây</span>
                <span class="drop-subtitle">Tool sẽ tìm đoạn hay và xuất thành nhiều clip riêng</span>
              </label>
              <div class="file-list" id="splitFileList"></div>
            </div>

            <div class="source-panel hidden" id="splitNetworkSourcePanel">
              <label class="note-control">Link video
                <input id="splitVideoUrl" name="videoUrl" type="url" placeholder="Dán link video cần tải tại đây">
                <span class="hint">Server sẽ tải bản nét nhất có thể, giới hạn tối đa 2K rồi dùng video đó để tách clip.</span>
              </label>
              <label class="note-control">Cookies.txt nếu link cần đăng nhập
                <input id="splitCookiesFilePath" name="cookiesFilePath" type="text" placeholder="Ví dụ: %USERPROFILE%\\Downloads\\instagram-cookies.txt">
                <span class="hint">Dùng khi Instagram báo lỗi cookie/DPAPI. Có thể để trống nếu link tải công khai.</span>
              </label>
            </div>

            <div class="controls">
              <label class="control">Số đoạn muốn cắt
                <input id="splitClipCount" name="clipCount" type="number" min="1" max="30" value="8">
              </label>
              <label class="control">Độ dài mỗi đoạn
                <input id="splitClipSeconds" name="clipSeconds" type="number" min="1" max="60" step="0.5" value="5">
              </label>
              <label class="control">Tỉ lệ xuất
                <select id="splitAspectRatio" name="aspectRatio">
                  <option value="9:16" selected>9:16 dọc</option>
                  <option value="16:9">16:9 ngang</option>
                </select>
              </label>
            </div>

            <label class="note-control">Ghi chú khi cắt
              <textarea id="splitCutNote" name="cutNote" maxlength="500" placeholder="Ví dụ: lấy 8 đoạn hay, mỗi đoạn 5 giây; ưu tiên đoạn có lời nói; hoặc ghi rõ 10-25, video 2: 1:20-1:35..."></textarea>
              <span class="hint">Có thể ghi mốc cụ thể như 10-25, 00:10-00:25 hoặc video 2: 1:20-1:35.</span>
            </label>

            <div class="actions">
              <button class="btn btn-primary" id="splitProcessButton" type="submit">Phân tích và tách clip</button>
              <button class="btn btn-light secondary" id="splitRefreshButton" type="button">Làm mới danh sách</button>
            </div>
          </form>

          <div class="progress-card">
            <div class="ring" id="splitRing" style="--angle: 0deg">
              <div class="percent" id="splitPercent">0%</div>
            </div>
            <div>
              <div class="state" id="splitState">Sẵn sàng nhận video.</div>
              <div class="bar"><div class="bar-fill" id="splitBarFill"></div></div>
            </div>
          </div>
        </section>

        <section class="panel history-panel card border-0 shadow-sm">
          <div class="table-header">
            <div>
              <h2>Danh sách clip đã tách</h2>
              <p id="splitHistorySummary">Đang tải lịch sử...</p>
            </div>
            <div class="table-actions">
              <button class="btn btn-outline-danger danger" id="splitDeleteSelectedButton" type="button" disabled>Xóa đã chọn</button>
            </div>
          </div>

          <div class="table-wrap">
            <table class="table table-hover align-middle split-table">
              <thead>
              <tr>
                <th class="action-cell">Thao tác</th>
                <th class="select-cell"><input id="splitSelectAllHistory" type="checkbox" aria-label="Chọn tất cả clip trên trang"></th>
                <th class="time-cell">Thời gian</th>
                <th class="file-cell">Video gốc</th>
                <th class="folder-cell">Thư mục gốc</th>
                <th class="file-cell clip-name-cell">Tên clip</th>
                <th class="folder-cell">Thư mục clip</th>
                <th class="range-cell">Mốc cắt</th>
                <th class="duration-cell">Độ dài</th>
                <th class="status-cell">Trạng thái</th>
              </tr>
              </thead>
              <tbody id="splitHistoryBody"></tbody>
            </table>
          </div>
          <div class="pager" id="splitPager"></div>
        </section>
      </div>
    </section>`;

  function render() {
    return markup;
  }

  components.splitVideoView = { render };
}());
