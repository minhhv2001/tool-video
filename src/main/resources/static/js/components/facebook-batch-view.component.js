(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const markup = `<section class="view" id="facebookBatchView">
      <div class="layout facebook-batch-layout">
        <section class="panel upload-panel card border-0 shadow-sm">
          <h2>Tải hàng loạt Facebook</h2>
          <form id="facebookBatchForm">
            <label class="note-control">Link danh sách video reels
              <textarea id="facebookBatchUrl" name="reelsUrl" placeholder="Ví dụ: https://www.facebook.com/tên-page/reels/&#10;Hoặc dán nhiều link reel, mỗi dòng một link"></textarea>
              <span class="hint">Có thể dán link trang reels hoặc nhiều link reel cụ thể. Với Facebook, dán nhiều link reel cụ thể sẽ ổn định hơn.</span>
            </label>

            <div class="controls batch-range-controls">
              <label class="control">Từ video số
                <input id="facebookBatchStart" name="startIndex" type="number" min="0" max="999" value="1">
              </label>
              <label class="control">Đến video số
                <input id="facebookBatchEnd" name="endIndex" type="number" min="1" max="999" value="10">
              </label>
            </div>

            <label class="note-control">Cookies.txt nếu Facebook cần đăng nhập
              <input id="facebookBatchCookiesFilePath" name="cookiesFilePath" type="text" placeholder="Ví dụ: %USERPROFILE%\\Downloads\\facebook-cookies.txt">
              <span class="hint">Nếu link công khai thì để trống. Nếu Facebook chặn quyền xem, export cookies rồi dán đường dẫn file vào đây.</span>
            </label>

            <div class="actions">
              <button class="btn btn-primary" id="facebookBatchDownloadButton" type="submit">Tải hàng loạt</button>
              <button class="btn btn-light secondary" id="facebookBatchRefreshButton" type="button">Làm mới danh sách</button>
            </div>
          </form>

          <div class="progress-card">
            <div class="ring" id="facebookBatchRing" style="--angle: 0deg">
              <div class="percent" id="facebookBatchPercent">0%</div>
            </div>
            <div>
              <div class="state" id="facebookBatchState">Sẵn sàng tải danh sách reels Facebook.</div>
              <div class="bar"><div class="bar-fill" id="facebookBatchBarFill"></div></div>
            </div>
          </div>
        </section>

        <section class="panel history-panel card border-0 shadow-sm">
          <div class="table-header">
            <div>
              <h2>Danh sách batch Facebook đã tải</h2>
              <p id="facebookBatchHistorySummary">Đang tải lịch sử...</p>
            </div>
            <div class="table-actions">
              <button class="btn btn-outline-danger danger" id="facebookBatchDeleteSelectedButton" type="button" disabled>Xóa đã chọn</button>
            </div>
          </div>

          <div class="table-wrap">
            <table class="table table-hover align-middle facebook-batch-table">
              <thead>
              <tr>
                <th class="action-cell">Thao tác</th>
                <th class="select-cell"><input id="facebookBatchSelectAllHistory" type="checkbox" aria-label="Chọn tất cả batch trên trang"></th>
                <th class="time-cell">Thời gian</th>
                <th class="file-cell">Video đã tải</th>
                <th class="folder-cell">Thư mục</th>
                <th class="note-cell">Nguồn</th>
                <th class="status-cell">Trạng thái</th>
                <th class="clips-cell">Số video</th>
                <th class="duration-cell">Tổng thời lượng</th>
              </tr>
              </thead>
              <tbody id="facebookBatchHistoryBody"></tbody>
            </table>
          </div>
          <div class="pager" id="facebookBatchPager"></div>
        </section>
      </div>
    </section>`;

  function render() {
    return markup;
  }

  components.facebookBatchView = { render };
}());
