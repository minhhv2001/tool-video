(function () {
  const components = window.ToolComponents = window.ToolComponents || {};
  const markup = `<div class="editor-modal" id="editorModal" aria-hidden="true">
  <div class="editor-backdrop" id="editorBackdrop"></div>
  <section class="editor-dialog" role="dialog" aria-modal="true" aria-labelledby="editorTitle">
    <div class="editor-header">
      <h2 id="editorTitle">Chỉnh sửa video</h2>
      <button class="editor-close" id="editorClose" type="button">Đóng</button>
    </div>
    <div class="editor-render-overlay" id="editorRenderOverlay" aria-hidden="true" aria-live="assertive">
      <div class="editor-render-card">
        <div class="editor-render-main">
          <div class="editor-render-spinner" aria-hidden="true"></div>
          <div>
            <strong id="editorRenderTitle">Đang xuất video</strong>
            <span id="editorRenderMessage">Đang chuẩn bị dữ liệu chỉnh sửa...</span>
          </div>
          <b id="editorRenderPercent">0%</b>
        </div>
        <div class="editor-render-bar">
          <span id="editorRenderFill"></span>
        </div>
        <p>Vui lòng chờ render xong, không đóng cửa sổ hoặc bấm thao tác khác.</p>
      </div>
    </div>
    <div class="editor-body">
      <div class="editor-preview">
        <div class="editor-stage" id="editorStage">
          <div class="editor-video-layer" id="editorVideoLayer">
            <video id="editorVideo" playsinline></video>
          </div>
          <div class="editor-frame-guide" id="editorFrameGuide" aria-hidden="true"></div>
          <div class="editor-text-layer-canvas" id="editorTextLayerCanvas"></div>
          <div class="editor-text-overlay" id="editorTextOverlay" role="button" tabindex="0" aria-label="Kéo để đặt vị trí text">
            <span id="editorTextContent"></span>
            <button class="editor-text-resize" id="editorTextResize" type="button" aria-label="Kéo để phóng to hoặc thu nhỏ text"></button>
          </div>
        </div>
        <audio id="editorAudioPreview"></audio>
        <div class="trim-panel">
          <div class="trim-meta">
            <span id="editorTrimStartLabel">00:00.0</span>
            <strong id="editorTrimDurationLabel">Đoạn đã chọn: 00:00.0</strong>
            <span id="editorTrimEndLabel">00:00.0</span>
          </div>
          <div class="trim-track-scroll" id="editorTrimTrackScroll" title="L&#259;n chu&#7897;t &#273;&#7875; ph&#243;ng to ho&#7863;c thu nh&#7887; timeline">
            <div class="trim-track" id="editorTrimTrack">
              <div class="trim-thumbnails" id="editorTimelineThumbs"></div>
              <div class="trim-segments" id="editorSegmentsLayer"></div>
              <div class="trim-selection" id="editorTrimSelection"></div>
              <div class="trim-playhead" id="editorTrimPlayhead"></div>
              <button class="trim-handle trim-handle-start" id="editorTrimStartHandle" type="button" aria-label="Kéo điểm bắt đầu"></button>
              <button class="trim-handle trim-handle-end" id="editorTrimEndHandle" type="button" aria-label="Kéo điểm kết thúc"></button>
            </div>
          </div>
          <div class="trim-actions">
            <button class="icon-tool" id="editorPlayPause" type="button" title="Phát hoặc tạm dừng" aria-label="Phát hoặc tạm dừng">
              <span class="tool-icon" id="editorPlayPauseIcon">▶</span>
            </button>
            <button class="icon-tool" id="editorUndo" type="button" title="Hoàn tác bước vừa làm" aria-label="Hoàn tác bước vừa làm" disabled>
              <span class="tool-icon">↶</span>
            </button>
            <button class="icon-tool" id="editorZoomOut" type="button" title="Thu nhỏ preview" aria-label="Thu nhỏ preview">
              <span class="tool-icon">−</span>
            </button>
            <button class="icon-tool text-tool" id="editorZoomReset" type="button" title="Đưa zoom về 100%" aria-label="Đưa zoom về 100%">
              <span id="editorZoomValue">100%</span>
            </button>
            <button class="icon-tool" id="editorZoomIn" type="button" title="Phóng to preview" aria-label="Phóng to preview">
              <span class="tool-icon">+</span>
            </button>
            <button class="icon-tool" id="editorTimelineZoomOut" type="button" title="Thu nhỏ timeline" aria-label="Thu nhỏ timeline" hidden>
              <span class="tool-icon">⇤</span>
            </button>
            <button class="icon-tool text-tool" id="editorTimelineZoomReset" type="button" title="L&#259;n chu&#7897;t tr&#234;n timeline &#273;&#7875; ph&#243;ng to ho&#7863;c thu nh&#7887;, b&#7845;m &#273;&#7875; v&#7873; 100%" aria-label="Timeline 100%">
              <span id="editorTimelineZoomValue">100%</span>
            </button>
            <button class="icon-tool" id="editorTimelineZoomIn" type="button" title="Phóng to timeline để cắt chính xác" aria-label="Phóng to timeline để cắt chính xác" hidden>
              <span class="tool-icon">⇥</span>
            </button>
            <button class="icon-tool" id="editorSetStartAtPlayhead" type="button" title="Đặt điểm bắt đầu tại đầu phát" aria-label="Đặt điểm bắt đầu tại đầu phát">
              <span class="tool-icon">⟦</span>
            </button>
            <button class="icon-tool" id="editorSetEndAtPlayhead" type="button" title="Đặt điểm kết thúc tại đầu phát" aria-label="Đặt điểm kết thúc tại đầu phát">
              <span class="tool-icon">⟧</span>
            </button>
            <button class="icon-tool" id="editorSplitAtPlayhead" type="button" title="Tách đoạn tại đầu phát" aria-label="Tách đoạn tại đầu phát">
              <span class="tool-icon">✂</span>
            </button>
            <button class="icon-tool danger" id="editorDeleteSegment" type="button" title="Xóa đoạn đang chọn" aria-label="Xóa đoạn đang chọn">
              <span class="tool-icon">⌫</span>
            </button>
            <button class="icon-tool" id="editorResetSegments" type="button" title="Khôi phục timeline" aria-label="Khôi phục timeline">
              <span class="tool-icon">↺</span>
            </button>
          </div>
          <div class="segment-order-panel">
            <div class="segment-order-head">
              <strong>Th&#7913; t&#7921; xu&#7845;t</strong>
              <span>K&#233;o t&#7915;ng kh&#250;c &#273;&#7875; &#273;&#7893;i th&#7913; t&#7921;. Khi x&#243;a kh&#250;c gi&#7919;a, c&#225;c kh&#250;c c&#242;n l&#7841;i s&#7869; n&#7889;i s&#225;t nhau khi l&#432;u.</span>
            </div>
            <div class="segment-order-list" id="editorSegmentOrderList"></div>
          </div>
            <div class="text-layer-panel">
              <div class="text-layer-head">
                <strong>D&#242;ng th&#7901;i gian text</strong>
                <span>K&#233;o thanh text &#273;&#7875; &#273;&#7863;t th&#7901;i gian hi&#7875;n th&#7883;.</span>
              </div>
            <div class="text-layer-scroll" id="editorTextLayerTimelineScroll">
              <div class="text-layer-timeline" id="editorTextLayerTimeline"></div>
            </div>
          </div>
        </div>
      </div>
      <form class="editor-form" id="editorForm">
        <section class="editor-control-section">
          <div class="editor-section-heading">Xuất video</div>
          <div class="editor-grid">
            <label class="control">Chỉnh từ
              <select id="editorSourceType" name="sourceType">
                <option value="output" selected>Video kết quả</option>
                <option value="source">Video gốc</option>
              </select>
            </label>
            <label class="control">Lưu kiểu
              <select id="editorSaveMode" name="saveMode">
                <option value="new" selected>Tạo bản ghi mới</option>
                <option value="overwrite">Ghi đè dòng hiện tại</option>
              </select>
            </label>
            <label class="control">Tinh chỉnh từ giây
              <input id="editorStart" name="startSeconds" type="number" min="0" step="0.001" placeholder="0">
            </label>
            <label class="control">Tinh chỉnh đến giây
              <input id="editorEnd" name="endSeconds" type="number" min="0" step="0.001" placeholder="Để trống nếu lấy tới cuối">
            </label>
            <label class="control">Xoay
              <div class="range-control">
                <input id="editorRotation" name="rotationDegrees" type="range" min="-180" max="180" step="1" value="0">
                <span id="editorRotationValue">0 độ</span>
              </div>
            </label>
            <label class="control">Tỉ lệ khung
              <select id="editorAspectRatio" name="editorAspectRatio">
                <option value="keep" selected>Giữ nguyên</option>
                <option value="16:9">16:9 ngang</option>
                <option value="9:16">9:16 dọc</option>
              </select>
            </label>
            <label class="control">Độ phân giải xuất
              <select id="editorOutputResolution" name="outputResolution">
                <option value="">Giữ nguyên</option>
              </select>
            </label>
          </div>
        </section>

        <section class="editor-control-section">
          <div class="editor-section-heading editor-section-heading-actions">
            <span>Text</span>
            <span class="editor-mini-actions">
              <button class="btn btn-sm btn-outline-primary" id="editorAddTextLayer" type="button">Th&#234;m text</button>
              <button class="btn btn-sm btn-outline-danger" id="editorDeleteTextLayer" type="button">X&#243;a text</button>
            </span>
          </div>
          <div class="editor-grid">
            <label class="control">Căn chữ ngang
              <select id="editorTextHorizontal" name="textHorizontal">
                <option value="left">Trái</option>
                <option value="center" selected>Giữa</option>
                <option value="right">Phải</option>
              </select>
            </label>
            <label class="control">Căn chữ dọc
              <select id="editorTextPosition" name="textPosition">
                <option value="top">Trên</option>
                <option value="center" selected>Giữa</option>
                <option value="bottom">Dưới</option>
              </select>
            </label>
            <label class="control">Cỡ chữ
              <input id="editorTextSize" name="textSize" type="number" min="18" max="160" value="42">
            </label>
            <label class="control">Màu chữ
              <input id="editorTextColor" name="textColor" type="color" value="#ffffff">
            </label>
            <label class="control">Kiểu chữ
              <select id="editorTextFont" name="textFont">
                <option value="arial" selected>Arial</option>
                <option value="segoe">Segoe UI</option>
                <option value="tahoma">Tahoma</option>
                <option value="verdana">Verdana</option>
                <option value="impact">Impact</option>
                <option value="georgia">Georgia</option>
                <option value="times">Times New Roman</option>
                <option value="comic">Comic Sans</option>
              </select>
            </label>
            <label class="control">Nền chữ
              <select id="editorTextBackground" name="textBackground">
                <option value="dark" selected>Nền đen mờ</option>
                <option value="none">Không nền</option>
                <option value="outline">Viền chữ</option>
                <option value="light">Nền trắng mờ</option>
                <option value="highlight">Nền vàng</option>
              </select>
            </label>
          </div>
          <label class="note-control">Text chèn vào video
            <textarea id="editorOverlayText" name="overlayText" maxlength="200" placeholder="Nhập chữ muốn chèn lên video"></textarea>
          </label>
        </section>

        <section class="editor-control-section">
          <div class="editor-section-heading">Âm thanh</div>
          <div class="editor-grid">
            <label class="control">Âm thanh
              <select id="editorAudioMode" name="audioMode">
                <option value="keep" selected>Giữ âm thanh gốc</option>
                <option value="replace">Thay bằng nhạc</option>
                <option value="mix">Trộn với nhạc</option>
              </select>
            </label>
            <label class="control checkbox-control">
              <input id="editorMuteOriginal" name="muteOriginalAudio" type="checkbox">
              <span>Tắt tiếng video gốc</span>
            </label>
            <label class="control">Tên hiển thị
              <input id="editorTitleInput" name="title" type="text" maxlength="180" placeholder="Để trống để tự đặt tên">
            </label>
          </div>
          <label class="note-control">File nhạc
            <input class="editor-file-input" id="editorMusic" name="music" type="file" accept="audio/*">
            <span class="hint">Chọn file nhạc nếu muốn thay hoặc trộn âm thanh.</span>
          </label>
        </section>

        <div class="editor-status" id="editorStatus">Sẵn sàng chỉnh sửa.</div>
        <div class="editor-actions">
          <button class="btn btn-outline-primary" id="editorExportNew" type="button">Xuất bản mới</button>
          <button class="btn btn-primary" id="editorSave" type="submit">Lưu video</button>
        </div>
      </form>
    </div>
  </section>
</div>`;

  function render() {
    return markup;
  }

  components.editorModalShell = { render };
}());
