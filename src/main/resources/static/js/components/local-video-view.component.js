(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const markup = `<section class="view active" id="localVideoView">
      <div class="layout">
        <section class="panel upload-panel card border-0 shadow-sm">
          <h2>Video Cắt ghép</h2>
          <form id="uploadForm">
            <div class="source-toggle" role="group" aria-label="Nguồn video">
              <label><input id="localSourceUpload" name="localSourceMode" type="radio" value="upload" checked> Video upload lên</label>
              <label><input id="localSourceNetwork" name="localSourceMode" type="radio" value="network"> Nguồn video từ mạng</label>
            </div>

            <div class="source-panel" id="uploadSourcePanel">
              <label class="dropzone" id="dropzone">
                <input id="videoFiles" name="videos" type="file" accept="video/*" multiple>
                <span class="drop-title">Kéo một hoặc nhiều video vào đây</span>
                <span class="drop-subtitle">Hoặc bấm để chọn file từ máy tính</span>
              </label>
              <div class="file-list" id="fileList"></div>
            </div>

            <div class="source-panel hidden" id="networkSourcePanel">
              <label class="note-control">Link video
                <input id="videoUrl" name="videoUrl" type="url" placeholder="Dán link video cần tải tại đây">
                <span class="hint">Server sẽ tải bản nét nhất có thể, giới hạn tối đa 2K rồi dùng video đó để cắt ghép.</span>
              </label>
              <label class="note-control">Cookies.txt nếu link cần đăng nhập
                <input id="cookiesFilePath" name="cookiesFilePath" type="text" placeholder="Ví dụ: C:\\Users\\minhm\\Downloads\\instagram-cookies.txt">
                <span class="hint">Dùng khi Instagram báo lỗi cookie/DPAPI. Có thể để trống nếu link tải công khai.</span>
              </label>
            </div>

            <div class="controls">
              <label class="control">Số đoạn muốn cắt
                <input id="clipCount" name="clipCount" type="number" min="1" max="30" value="6">
              </label>
              <label class="control">Độ dài mỗi đoạn
                <input id="clipSeconds" name="clipSeconds" type="number" min="1" max="60" step="0.5" value="5">
              </label>
              <label class="control">Tỉ lệ xuất
                <select id="aspectRatio" name="aspectRatio">
                  <option value="9:16" selected>9:16 dọc</option>
                  <option value="16:9">16:9 ngang</option>
                </select>
              </label>
            </div>

            <label class="note-control">Ghi chú khi cắt
              <textarea id="cutNote" name="cutNote" maxlength="500" placeholder="Ví dụ: cắt 10-25, 00:40-00:55; video 2: 1:20-1:35; bỏ intro, ưu tiên đoạn nói to..."></textarea>
              <span class="hint">Có thể ghi mốc cụ thể như 10-25, 10s đến 25s, 00:10-00:25 hoặc video 2: 1:20-1:35.</span>
            </label>

            <div class="actions">
              <button class="btn btn-primary" id="processButton" type="submit">Phân tích và cắt ghép</button>
              <button class="btn btn-light secondary" id="refreshButton" type="button">Làm mới danh sách</button>
            </div>
          </form>

          <div class="progress-card">
            <div class="ring" id="ring" style="--angle: 0deg">
              <div class="percent" id="percent">0%</div>
            </div>
            <div>
              <div class="state" id="state">Sẵn sàng nhận video.</div>
              <div class="bar"><div class="bar-fill" id="barFill"></div></div>
            </div>
          </div>
        </section>

        <section class="panel history-panel card border-0 shadow-sm">
          <div class="table-header">
            <div>
              <h2>Danh sách video đã cắt</h2>
              <p id="historySummary">Đang tải lịch sử...</p>
            </div>
            <div class="table-actions">
              <button class="btn btn-outline-danger danger" id="deleteSelectedButton" type="button" disabled>Xóa đã chọn</button>
            </div>
          </div>

          <div class="table-wrap">
            <table class="table table-hover align-middle">
              <thead>
              <tr>
                <th class="action-cell">Thao tác</th>
                <th class="select-cell"><input id="selectAllHistory" type="checkbox" aria-label="Chọn tất cả trên trang"></th>
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
              <tbody id="historyBody"></tbody>
            </table>
          </div>
          <div class="pager" id="pager"></div>
        </section>
      </div>
    </section>`;

  function render() {
    return markup;
  }

  components.localVideoView = { render };
}());