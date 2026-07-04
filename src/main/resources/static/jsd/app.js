const dropzone = document.querySelector('#dropzone');
const fileInput = document.querySelector('#videoFiles');
const fileList = document.querySelector('#fileList');
const cutNote = document.querySelector('#cutNote');
const form = document.querySelector('#uploadForm');
const processButton = document.querySelector('#processButton');
const refreshButton = document.querySelector('#refreshButton');
const health = document.querySelector('#health');
const state = document.querySelector('#state');
const ring = document.querySelector('#ring');
const percent = document.querySelector('#percent');
const barFill = document.querySelector('#barFill');
const historyBody = document.querySelector('#historyBody');
const historySummary = document.querySelector('#historySummary');
const pager = document.querySelector('#pager');
const deleteSelectedButton = document.querySelector('#deleteSelectedButton');
const selectAllHistory = document.querySelector('#selectAllHistory');
const workspaceHint = document.querySelector('#workspaceHint');
const localSourceUpload = document.querySelector('#localSourceUpload');
const localSourceNetwork = document.querySelector('#localSourceNetwork');
const uploadSourcePanel = document.querySelector('#uploadSourcePanel');
const networkSourcePanel = document.querySelector('#networkSourcePanel');
const videoUrl = document.querySelector('#videoUrl');
const cookiesFilePath = document.querySelector('#cookiesFilePath');
const aspectRatio = document.querySelector('#aspectRatio');
const splitDropzone = document.querySelector('#splitDropzone');
const splitFileInput = document.querySelector('#splitVideoFiles');
const splitFileList = document.querySelector('#splitFileList');
const splitCutNote = document.querySelector('#splitCutNote');
const splitForm = document.querySelector('#splitUploadForm');
const splitProcessButton = document.querySelector('#splitProcessButton');
const splitRefreshButton = document.querySelector('#splitRefreshButton');
const splitState = document.querySelector('#splitState');
const splitRing = document.querySelector('#splitRing');
const splitPercent = document.querySelector('#splitPercent');
const splitBarFill = document.querySelector('#splitBarFill');
const splitHistoryBody = document.querySelector('#splitHistoryBody');
const splitHistorySummary = document.querySelector('#splitHistorySummary');
const splitPager = document.querySelector('#splitPager');
const splitDeleteSelectedButton = document.querySelector('#splitDeleteSelectedButton');
const splitSelectAllHistory = document.querySelector('#splitSelectAllHistory');
const splitSourceUpload = document.querySelector('#splitSourceUpload');
const splitSourceNetwork = document.querySelector('#splitSourceNetwork');
const splitUploadSourcePanel = document.querySelector('#splitUploadSourcePanel');
const splitNetworkSourcePanel = document.querySelector('#splitNetworkSourcePanel');
const splitVideoUrl = document.querySelector('#splitVideoUrl');
const splitCookiesFilePath = document.querySelector('#splitCookiesFilePath');
const splitAspectRatio = document.querySelector('#splitAspectRatio');
const previewModal = document.querySelector('#previewModal');
const previewBackdrop = document.querySelector('#previewBackdrop');
const previewClose = document.querySelector('#previewClose');
const previewTitle = document.querySelector('#previewTitle');
const previewVideo = document.querySelector('#previewVideo');
const editorModal = document.querySelector('#editorModal');
const editorBackdrop = document.querySelector('#editorBackdrop');
const editorClose = document.querySelector('#editorClose');
const editorTitle = document.querySelector('#editorTitle');
const editorStage = document.querySelector('#editorStage');
const editorVideoLayer = document.querySelector('#editorVideoLayer');
const editorVideo = document.querySelector('#editorVideo');
const editorFrameGuide = document.querySelector('#editorFrameGuide');
const editorTextOverlay = document.querySelector('#editorTextOverlay');
const editorTextContent = document.querySelector('#editorTextContent');
const editorTextResize = document.querySelector('#editorTextResize');
const editorAudioPreview = document.querySelector('#editorAudioPreview');
const editorForm = document.querySelector('#editorForm');
const editorSourceType = document.querySelector('#editorSourceType');
const editorSaveMode = document.querySelector('#editorSaveMode');
const editorStart = document.querySelector('#editorStart');
const editorEnd = document.querySelector('#editorEnd');
const editorRotation = document.querySelector('#editorRotation');
const editorRotationValue = document.querySelector('#editorRotationValue');
const editorAspectRatio = document.querySelector('#editorAspectRatio');
const editorOutputResolution = document.querySelector('#editorOutputResolution');
const editorTextHorizontal = document.querySelector('#editorTextHorizontal');
const editorTextPosition = document.querySelector('#editorTextPosition');
const editorTextSize = document.querySelector('#editorTextSize');
const editorTextColor = document.querySelector('#editorTextColor');
const editorTextFont = document.querySelector('#editorTextFont');
const editorTextBackground = document.querySelector('#editorTextBackground');
const editorOverlayText = document.querySelector('#editorOverlayText');
const editorAudioMode = document.querySelector('#editorAudioMode');
const editorMuteOriginal = document.querySelector('#editorMuteOriginal');
const editorTitleInput = document.querySelector('#editorTitleInput');
const editorMusic = document.querySelector('#editorMusic');
const editorStatus = document.querySelector('#editorStatus');
const editorExportNew = document.querySelector('#editorExportNew');
const editorSave = document.querySelector('#editorSave');
const editorRenderOverlay = document.querySelector('#editorRenderOverlay');
const editorRenderTitle = document.querySelector('#editorRenderTitle');
const editorRenderMessage = document.querySelector('#editorRenderMessage');
const editorRenderPercent = document.querySelector('#editorRenderPercent');
const editorRenderFill = document.querySelector('#editorRenderFill');
const editorTrimTrack = document.querySelector('#editorTrimTrack');
const editorSegmentsLayer = document.querySelector('#editorSegmentsLayer');
const editorTrimSelection = document.querySelector('#editorTrimSelection');
const editorTrimPlayhead = document.querySelector('#editorTrimPlayhead');
const editorTrimStartHandle = document.querySelector('#editorTrimStartHandle');
const editorTrimEndHandle = document.querySelector('#editorTrimEndHandle');
const editorTrimStartLabel = document.querySelector('#editorTrimStartLabel');
const editorTrimEndLabel = document.querySelector('#editorTrimEndLabel');
const editorTrimDurationLabel = document.querySelector('#editorTrimDurationLabel');
const editorPlayPause = document.querySelector('#editorPlayPause');
const editorPlayPauseIcon = document.querySelector('#editorPlayPauseIcon');
const editorUndo = document.querySelector('#editorUndo');
const editorZoomOut = document.querySelector('#editorZoomOut');
const editorZoomReset = document.querySelector('#editorZoomReset');
const editorZoomIn = document.querySelector('#editorZoomIn');
const editorZoomValue = document.querySelector('#editorZoomValue');
const editorTimelineZoomOut = document.querySelector('#editorTimelineZoomOut');
const editorTimelineZoomReset = document.querySelector('#editorTimelineZoomReset');
const editorTimelineZoomIn = document.querySelector('#editorTimelineZoomIn');
const editorTimelineZoomValue = document.querySelector('#editorTimelineZoomValue');
const editorSetStartAtPlayhead = document.querySelector('#editorSetStartAtPlayhead');
const editorSetEndAtPlayhead = document.querySelector('#editorSetEndAtPlayhead');
const editorSplitAtPlayhead = document.querySelector('#editorSplitAtPlayhead');
const editorDeleteSegment = document.querySelector('#editorDeleteSegment');
const editorResetSegments = document.querySelector('#editorResetSegments');
const editorSegmentOrderList = document.querySelector('#editorSegmentOrderList');

let selectedFiles = [];
let previewUrls = [];
let pollTimer = null;
let currentPage = 1;
let currentHistoryItems = [];
let selectedHistoryIds = new Set();
let splitSelectedFiles = [];
let splitPreviewUrls = [];
let splitPollTimer = null;
let splitCurrentPage = 1;
let splitCurrentHistoryItems = [];
let splitSelectedClipKeys = new Set();
let currentEditor = null;
let editorTrim = { duration: 0, start: 0, end: 0, dragging: null, dragOffset: 0 };
let editorSegments = [];
let selectedEditorSegmentIndex = 0;
let editorTextState = { x: 50, y: 82, dragging: false, resizing: false, dragOffsetX: 0, dragOffsetY: 0, resizeStartX: 0, resizeStartY: 0, resizeStartSize: 42 };
let editorAudioObjectUrl = '';
let editorZoom = 1;
let editorUndoStack = [];
let editorNativeSize = { width: 0, height: 0 };
let draggedEditorSegmentIndex = null;
let editorTimelineZoom = 1;
let editorRenderTimer = null;
let editorRenderProgress = 0;
let editorRenderActive = false;
const pageSize = 10;
const HistoryListComponent = window.ToolComponents && window.ToolComponents.historyList;
const EditorTimelineComponent = window.ToolComponents && window.ToolComponents.editorTimeline;
const PreviewModalComponent = window.ToolComponents && window.ToolComponents.previewModal;
const previewModalComponent = PreviewModalComponent
  ? PreviewModalComponent.create({ modal: previewModal, title: previewTitle, video: previewVideo })
  : null;

function initMenu() {
  document.querySelectorAll('.menu-item').forEach((button) => {
    button.addEventListener('click', () => {
      document.querySelectorAll('.menu-item').forEach((item) => item.classList.remove('active'));
      document.querySelectorAll('.view').forEach((view) => view.classList.remove('active'));
      button.classList.add('active');
      document.querySelector(`#${button.dataset.view}`).classList.add('active');
    });
  });
}

function localUsesNetworkSource() {
  return localSourceNetwork.checked;
}

function splitUsesNetworkSource() {
  return splitSourceNetwork.checked;
}

function updateSourcePanels() {
  uploadSourcePanel.classList.toggle('hidden', localUsesNetworkSource());
  networkSourcePanel.classList.toggle('hidden', !localUsesNetworkSource());
  splitUploadSourcePanel.classList.toggle('hidden', splitUsesNetworkSource());
  splitNetworkSourcePanel.classList.toggle('hidden', !splitUsesNetworkSource());
}

function setProgress(value, message, type = '') {
  const safe = Math.max(0, Math.min(100, Math.round(value || 0)));
  ring.style.setProperty('--angle', `${safe * 3.6}deg`);
  percent.textContent = `${safe}%`;
  barFill.style.width = `${safe}%`;
  state.className = `state ${type}`;
  state.textContent = message;
  ring.classList.toggle('spin', type !== 'ok' && type !== 'error' && safe > 0 && safe < 100);
}

function splitSetProgress(value, message, type = '') {
  const safe = Math.max(0, Math.min(100, Math.round(value || 0)));
  splitRing.style.setProperty('--angle', `${safe * 3.6}deg`);
  splitPercent.textContent = `${safe}%`;
  splitBarFill.style.width = `${safe}%`;
  splitState.className = `state ${type}`;
  splitState.textContent = message;
  splitRing.classList.toggle('spin', type !== 'ok' && type !== 'error' && safe > 0 && safe < 100);
}

function fileKey(file) {
  return `${file.name}-${file.size}-${file.lastModified}`;
}

function syncFileInput() {
  const transfer = new DataTransfer();
  selectedFiles.forEach((file) => transfer.items.add(file));
  fileInput.files = transfer.files;
}

function addFiles(files) {
  const incoming = Array.from(files || []).filter((file) => file.type.startsWith('video/'));
  if (!incoming.length) {
    return;
  }
  const existingKeys = new Set(selectedFiles.map(fileKey));
  incoming.forEach((file) => {
    const key = fileKey(file);
    if (!existingKeys.has(key)) {
      selectedFiles.push(file);
      existingKeys.add(key);
    }
  });
  renderFiles();
}

function removeFile(index) {
  selectedFiles.splice(index, 1);
  renderFiles();
}

function renderFiles() {
  syncFileInput();
  previewUrls.forEach((url) => URL.revokeObjectURL(url));
  previewUrls = [];
  fileList.innerHTML = '';
  selectedFiles.forEach((file, index) => {
    const url = URL.createObjectURL(file);
    previewUrls.push(url);
    const row = document.createElement('div');
    row.className = 'file-row';
    row.innerHTML = `
      <video src="${url}" muted playsinline></video>
      <div>
        <div class="file-name">${escapeHtml(file.name)}</div>
        <div class="file-size">${(file.size / 1024 / 1024).toFixed(1)} MB</div>
      </div>
      <button class="file-remove" type="button" aria-label="Xóa video này">×</button>
    `;
    row.querySelector('.file-remove').addEventListener('click', () => removeFile(index));
    fileList.appendChild(row);
  });
  setProgress(0, selectedFiles.length ? `Đã chọn ${selectedFiles.length} video. Bạn có thể kéo thêm video vào danh sách.` : 'Sẵn sàng nhận video.');
}

function syncSplitFileInput() {
  const transfer = new DataTransfer();
  splitSelectedFiles.forEach((file) => transfer.items.add(file));
  splitFileInput.files = transfer.files;
}

function addSplitFiles(files) {
  const incoming = Array.from(files || []).filter((file) => file.type.startsWith('video/'));
  if (!incoming.length) {
    return;
  }
  const existingKeys = new Set(splitSelectedFiles.map(fileKey));
  incoming.forEach((file) => {
    const key = fileKey(file);
    if (!existingKeys.has(key)) {
      splitSelectedFiles.push(file);
      existingKeys.add(key);
    }
  });
  renderSplitFiles();
}

function removeSplitFile(index) {
  splitSelectedFiles.splice(index, 1);
  renderSplitFiles();
}

function renderSplitFiles() {
  syncSplitFileInput();
  splitPreviewUrls.forEach((url) => URL.revokeObjectURL(url));
  splitPreviewUrls = [];
  splitFileList.innerHTML = '';
  splitSelectedFiles.forEach((file, index) => {
    const url = URL.createObjectURL(file);
    splitPreviewUrls.push(url);
    const row = document.createElement('div');
    row.className = 'file-row';
    row.innerHTML = `
      <video src="${url}" muted playsinline></video>
      <div>
        <div class="file-name">${escapeHtml(file.name)}</div>
        <div class="file-size">${(file.size / 1024 / 1024).toFixed(1)} MB</div>
      </div>
      <button class="file-remove" type="button" aria-label="Xóa video này">×</button>
    `;
    row.querySelector('.file-remove').addEventListener('click', () => removeSplitFile(index));
    splitFileList.appendChild(row);
  });
  splitSetProgress(0, splitSelectedFiles.length ? `Đã chọn ${splitSelectedFiles.length} video. Bạn có thể kéo thêm video vào danh sách.` : 'Sẵn sàng nhận video.');
}

async function loadHealth() {
  try {
    const response = await fetch('/api/health');
    const data = await response.json();
    health.innerHTML = `
      <span class="health-title">${data.message === 'Ready' ? 'Sẵn sàng' : data.message}</span>
      <span class="health-pill">FFmpeg: <b>${data.ffmpegAvailable ? 'OK' : 'thiếu'}</b></span>
      <span class="health-pill">FFprobe: <b>${data.ffprobeAvailable ? 'OK' : 'thiếu'}</b></span>
    `;
    workspaceHint.textContent = data.workspace || 'Local workspace';
  } catch (error) {
    health.textContent = 'Chưa kết nối được server.';
  }
}

function historyListHelpers() {
  return {
    escapeHtml,
    renderCompactText,
    folderIcon,
    formatDate,
    statusText,
    formatSeconds,
    formatRange
  };
}

async function loadHistory(page = currentPage) {
  currentPage = page;
  const response = await fetch(`/api/highlights?page=${page}&size=${pageSize}`);
  const data = await response.json();
  if (data.items.length === 0 && data.totalItems > 0 && page > 1) {
    return loadHistory(page - 1);
  }
  selectedHistoryIds.clear();
  currentHistoryItems = data.items || [];
  if (HistoryListComponent) {
    historySummary.textContent = `${data.totalItems} bản ghi, trang ${data.page}/${Math.max(data.totalPages, 1)}`;
    historyBody.innerHTML = '';
    if (!data.items.length) {
      historyBody.appendChild(HistoryListComponent.renderEmptyRow('Chưa có video nào được cắt.', 10));
    }
    data.items.forEach((item) => {
      historyBody.appendChild(HistoryListComponent.renderHighlightRow(item, historyListHelpers()));
    });
    bindHistoryActions();
    updateHistorySelection();
    renderPager(data.page, data.totalPages);
    return;
  }
  historySummary.textContent = `${data.totalItems} bản ghi, trang ${data.page}/${Math.max(data.totalPages, 1)}`;
  historyBody.innerHTML = data.items.length ? '' : '<tr><td colspan="10">Chưa có video nào được cắt.</td></tr>';
  data.items.forEach((item) => {
    const tr = document.createElement('tr');
    const files = (item.inputFileNames || []).join(', ');
    const previewUrl = item.downloadUrl ? `/api/highlights/${encodeURIComponent(item.jobId)}/preview` : '';
    const sourceLocationUrl = `/api/highlights/${encodeURIComponent(item.jobId)}/open-location?target=source`;
    const outputLocationUrl = `/api/highlights/${encodeURIComponent(item.jobId)}/open-location?target=output`;
    tr.innerHTML = `
      <td class="action-cell">
        <div class="row-actions">
          ${item.downloadUrl ? `<a class="download btn btn-sm btn-success" href="${item.downloadUrl}" title="Tải xuống">Tải</a>` : ''}
          ${previewUrl ? `<button class="edit-row btn btn-sm btn-outline-primary" type="button" data-edit-url="/api/highlights/${encodeURIComponent(item.jobId)}/edit" data-preview-url="${previewUrl}" data-edit-title="${escapeHtml(files || 'Video highlight')}" title="Chỉnh sửa">Sửa</button>` : ''}
          <button class="delete-row btn btn-sm btn-outline-danger" type="button" data-job-id="${escapeHtml(item.jobId)}">Xóa</button>
        </div>
      </td>
      <td class="select-cell"><input class="history-check" type="checkbox" data-job-id="${escapeHtml(item.jobId)}" aria-label="Chọn job này"></td>
      <td class="time-cell">${formatDate(item.createdAt)}</td>
      <td class="file-cell">${renderCompactText(files)}</td>
      <td class="folder-cell"><button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${sourceLocationUrl}" title="Mở thư mục video gốc">${folderIcon()}<span>Gốc</span></button></td>
      <td class="note-cell">${renderCompactText(item.cutNote || '-')}</td>
      <td class="status-cell"><span class="status ${item.status}">${statusText(item.status)}</span></td>
      <td class="clips-cell">${item.clipsUsed || '-'}</td>
      <td class="duration-cell">${item.totalDurationSeconds ? `${Number(item.totalDurationSeconds).toFixed(1)} giây` : '-'}</td>
      <td class="folder-cell">${item.downloadUrl ? `<button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${outputLocationUrl}" title="Mở thư mục video đã cắt ghép">${folderIcon()}<span>Kết quả</span></button>` : '-'}</td>
    `;
    if (previewUrl) {
      prependPreviewButton(tr, previewUrl, files || 'Video highlight');
    }
    historyBody.appendChild(tr);
  });
  bindHistoryActions();
  updateHistorySelection();
  renderPager(data.page, data.totalPages);
}

async function loadSplitHistory(page = splitCurrentPage) {
  splitCurrentPage = page;
  const response = await fetch(`/api/split-highlights?page=${page}&size=${pageSize}`);
  const data = await response.json();
  if (data.items.length === 0 && data.totalItems > 0 && page > 1) {
    return loadSplitHistory(page - 1);
  }
  splitSelectedClipKeys.clear();
  splitCurrentHistoryItems = data.items || [];
  if (HistoryListComponent) {
    splitHistorySummary.textContent = `${data.totalItems} clip, trang ${data.page}/${Math.max(data.totalPages, 1)}`;
    splitHistoryBody.innerHTML = '';
    if (!data.items.length) {
      splitHistoryBody.appendChild(HistoryListComponent.renderEmptyRow('Chưa có clip nào được tách.', 10));
    }
    data.items.forEach((item) => {
      splitHistoryBody.appendChild(HistoryListComponent.renderSplitRow(item, historyListHelpers()));
    });
    bindSplitHistoryActions();
    updateSplitHistorySelection();
    renderSplitPager(data.page, data.totalPages);
    return;
  }
  splitHistorySummary.textContent = `${data.totalItems} clip, trang ${data.page}/${Math.max(data.totalPages, 1)}`;
  splitHistoryBody.innerHTML = data.items.length ? '' : '<tr><td colspan="10">Chưa có clip nào được tách.</td></tr>';
  if (!data.items.length) {
    const emptyCell = splitHistoryBody.querySelector('td');
    if (emptyCell) {
      emptyCell.colSpan = 10;
    }
  }
  data.items.forEach((item) => {
    const tr = document.createElement('tr');
    const previewUrl = item.downloadUrl ? `/api/split-highlights/${encodeURIComponent(item.jobId)}/clips/${encodeURIComponent(item.clipIndex)}/preview` : '';
    const previewName = `${item.outputFileName || `clip-${item.clipIndex}`} - ${item.originalFileName || 'Video gốc'}`;
    const sourceLocationUrl = `/api/split-highlights/${encodeURIComponent(item.jobId)}/clips/${encodeURIComponent(item.clipIndex)}/open-location?target=source`;
    const outputLocationUrl = `/api/split-highlights/${encodeURIComponent(item.jobId)}/clips/${encodeURIComponent(item.clipIndex)}/open-location?target=output`;
    tr.innerHTML = `
      <td class="action-cell">
        <div class="row-actions">
          ${item.downloadUrl ? `<a class="download btn btn-sm btn-success" href="${item.downloadUrl}" title="Tải xuống">Tải</a>` : ''}
          ${previewUrl ? `<button class="edit-row btn btn-sm btn-outline-primary" type="button" data-edit-url="/api/split-highlights/${encodeURIComponent(item.jobId)}/clips/${encodeURIComponent(item.clipIndex)}/edit" data-preview-url="${previewUrl}" data-edit-title="${escapeHtml(previewName)}" title="Chỉnh sửa">Sửa</button>` : ''}
        </div>
      </td>
      <td class="time-cell">${formatDate(item.createdAt)}</td>
      <td class="file-cell">${renderCompactText(item.originalFileName || '-')}</td>
      <td class="folder-cell"><button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${sourceLocationUrl}" title="Mở thư mục video gốc">${folderIcon()}<span>Gốc</span></button></td>
      <td class="file-cell clip-name-cell">${renderCompactText(item.outputFileName || `clip-${item.clipIndex}`)}</td>
      <td class="folder-cell">${item.downloadUrl ? `<button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${outputLocationUrl}" title="Mở thư mục clip đã tách">${folderIcon()}<span>Clip</span></button>` : '-'}</td>
      <td class="range-cell">${formatRange(item.startSeconds, item.durationSeconds)}</td>
      <td class="duration-cell">${formatSeconds(item.durationSeconds)}</td>
      <td class="status-cell"><span class="status ${item.status}">${statusText(item.status)}</span></td>
    `;
    prependSplitSelectionCell(tr, item);
    if (previewUrl) {
      prependPreviewButton(tr, previewUrl, previewName);
    }
    appendSplitDeleteButton(tr, item);
    splitHistoryBody.appendChild(tr);
  });
  bindSplitHistoryActions();
  updateSplitHistorySelection();
  renderSplitPager(data.page, data.totalPages);
}

function bindHistoryActions() {
  historyBody.querySelectorAll('.history-check').forEach((checkbox) => {
    checkbox.addEventListener('change', () => {
      const jobId = checkbox.dataset.jobId;
      if (checkbox.checked) {
        selectedHistoryIds.add(jobId);
      } else {
        selectedHistoryIds.delete(jobId);
      }
      updateHistorySelection();
    });
  });
  historyBody.querySelectorAll('.delete-row').forEach((button) => {
    button.addEventListener('click', () => deleteHistoryJobs([button.dataset.jobId]));
  });
  historyBody.querySelectorAll('.open-location-row').forEach((button) => {
    button.addEventListener('click', () => openFileLocation(button.dataset.openUrl, setProgress));
  });
  historyBody.querySelectorAll('.preview-row').forEach((button) => {
    button.addEventListener('click', () => openPreview(button.dataset.previewUrl, button.dataset.previewTitle));
  });
  historyBody.querySelectorAll('.edit-row').forEach((button) => {
    button.addEventListener('click', () => openEditor(button.dataset.editUrl, button.dataset.previewUrl, button.dataset.editTitle, 'highlight'));
  });
  historyBody.querySelectorAll('.text-toggle').forEach((button) => {
    button.addEventListener('click', () => toggleCompactText(button));
  });
}

function prependPreviewButton(row, previewUrl, title) {
  const actions = row.querySelector('.row-actions');
  if (!actions) {
    return;
  }
  const button = document.createElement('button');
  button.className = 'preview-row btn btn-sm btn-outline-info';
  button.type = 'button';
  button.title = 'Xem trước';
  button.textContent = 'Xem';
  button.addEventListener('click', () => openPreview(previewUrl, title));
  actions.prepend(button);
}

function prependSplitSelectionCell(row, item) {
  const cell = document.createElement('td');
  cell.className = 'select-cell';
  cell.innerHTML = `<input class="split-history-check" type="checkbox" data-job-id="${escapeHtml(item.jobId)}" data-clip-index="${escapeHtml(item.clipIndex)}" aria-label="Chọn clip này">`;
  const actionCell = row.querySelector('.action-cell');
  if (actionCell) {
    actionCell.after(cell);
  } else {
    row.prepend(cell);
  }
}

function appendSplitDeleteButton(row, item) {
  const actions = row.querySelector('.row-actions');
  if (!actions) {
    return;
  }
  const button = document.createElement('button');
  button.className = 'delete-row split-delete-row btn btn-sm btn-outline-danger';
  button.type = 'button';
  button.dataset.jobId = item.jobId;
  button.dataset.clipIndex = item.clipIndex;
  button.textContent = 'Xóa';
  actions.appendChild(button);
}

function splitClipKey(jobId, clipIndex) {
  return `${jobId}:${clipIndex}`;
}

function splitClipRefFromKey(key) {
  const [jobId, clipIndex] = String(key || '').split(':');
  return { jobId, clipIndex: Number(clipIndex) };
}

function folderIcon() {
  return '<span class="folder-icon" aria-hidden="true"></span>';
}

function bindSplitHistoryActions() {
  splitHistoryBody.querySelectorAll('.split-history-check').forEach((checkbox) => {
    checkbox.addEventListener('change', () => {
      const key = splitClipKey(checkbox.dataset.jobId, checkbox.dataset.clipIndex);
      if (checkbox.checked) {
        splitSelectedClipKeys.add(key);
      } else {
        splitSelectedClipKeys.delete(key);
      }
      updateSplitHistorySelection();
    });
  });
  splitHistoryBody.querySelectorAll('.split-delete-row').forEach((button) => {
    button.addEventListener('click', () => deleteSplitClips([{ jobId: button.dataset.jobId, clipIndex: Number(button.dataset.clipIndex) }]));
  });
  splitHistoryBody.querySelectorAll('.open-location-row').forEach((button) => {
    button.addEventListener('click', () => openFileLocation(button.dataset.openUrl, splitSetProgress));
  });
  splitHistoryBody.querySelectorAll('.preview-row').forEach((button) => {
    button.addEventListener('click', () => openPreview(button.dataset.previewUrl, button.dataset.previewTitle));
  });
  splitHistoryBody.querySelectorAll('.edit-row').forEach((button) => {
    button.addEventListener('click', () => openEditor(button.dataset.editUrl, button.dataset.previewUrl, button.dataset.editTitle, 'split'));
  });
  splitHistoryBody.querySelectorAll('.text-toggle').forEach((button) => {
    button.addEventListener('click', () => toggleCompactText(button));
  });
}

async function openFileLocation(url, progressSetter) {
  if (!url) {
    return;
  }
  try {
    const response = await fetch(url, { method: 'POST' });
    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.error || 'Không mở được thư mục lưu video.');
    }
    progressSetter(0, result.message || 'Đã mở vị trí lưu video.');
  } catch (error) {
    progressSetter(100, error.message, 'error');
  }
}

function renderCompactText(value) {
  const raw = String(value || '-');
  const needsToggle = raw.length > 70 || raw.includes(',');
  return `
    <div class="clamped-text">${escapeHtml(raw)}</div>
    ${needsToggle ? '<button class="text-toggle" type="button" aria-expanded="false">Xem thêm</button>' : ''}
  `;
}

function toggleCompactText(button) {
  const cell = button.closest('.file-cell, .note-cell');
  if (!cell) {
    return;
  }
  const expanded = cell.classList.toggle('expanded');
  button.textContent = expanded ? 'Thu gọn' : 'Xem thêm';
  button.setAttribute('aria-expanded', String(expanded));
}

function updateHistorySelection() {
  const pageIds = currentHistoryItems.map((item) => item.jobId).filter(Boolean);
  const checkedCount = pageIds.filter((jobId) => selectedHistoryIds.has(jobId)).length;
  selectAllHistory.checked = pageIds.length > 0 && checkedCount === pageIds.length;
  selectAllHistory.indeterminate = checkedCount > 0 && checkedCount < pageIds.length;
  deleteSelectedButton.disabled = selectedHistoryIds.size === 0;
}

function updateSplitHistorySelection() {
  const pageKeys = splitCurrentHistoryItems
    .map((item) => splitClipKey(item.jobId, item.clipIndex))
    .filter(Boolean);
  const checkedCount = pageKeys.filter((key) => splitSelectedClipKeys.has(key)).length;
  splitSelectAllHistory.checked = pageKeys.length > 0 && checkedCount === pageKeys.length;
  splitSelectAllHistory.indeterminate = checkedCount > 0 && checkedCount < pageKeys.length;
  splitDeleteSelectedButton.disabled = splitSelectedClipKeys.size === 0;
}

async function deleteHistoryJobs(jobIds) {
  const safeIds = Array.from(new Set((jobIds || []).filter(Boolean)));
  if (!safeIds.length) {
    return;
  }
  const ok = confirm(`Xóa ${safeIds.length} job khỏi ổ cứng? Video gốc, video tạm và output sẽ bị xóa sạch.`);
  if (!ok) {
    return;
  }
  deleteSelectedButton.disabled = true;
  try {
    const response = await fetch('/api/highlights/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ jobIds: safeIds })
    });
    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.error || 'Không xóa được lịch sử.');
    }
    selectedHistoryIds.clear();
    setProgress(0, `Đã xóa ${result.deletedCount} job khỏi ổ cứng.`);
    await loadHistory(currentPage);
  } catch (error) {
    setProgress(100, error.message, 'error');
    updateHistorySelection();
  }
}

async function deleteSplitClips(clips) {
  const safeClips = (clips || [])
    .filter((clip) => clip && clip.jobId && Number(clip.clipIndex) > 0)
    .filter((clip, index, list) => list.findIndex((item) => splitClipKey(item.jobId, item.clipIndex) === splitClipKey(clip.jobId, clip.clipIndex)) === index);
  if (!safeClips.length) {
    return;
  }
  const ok = confirm(`Xóa ${safeClips.length} clip khỏi ổ cứng? File clip sẽ bị xóa sạch khỏi server local.`);
  if (!ok) {
    return;
  }
  splitDeleteSelectedButton.disabled = true;
  try {
    const response = await fetch('/api/split-highlights/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ clips: safeClips })
    });
    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.error || 'Không xóa được clip.');
    }
    splitSelectedClipKeys.clear();
    splitSetProgress(0, `Đã xóa ${result.deletedCount} clip khỏi ổ cứng.`);
    await loadSplitHistory(splitCurrentPage);
  } catch (error) {
    splitSetProgress(100, error.message, 'error');
    updateSplitHistorySelection();
  }
}

function renderPager(page, totalPages) {
  if (HistoryListComponent) {
    HistoryListComponent.renderPager(pager, page, totalPages, loadHistory);
    return;
  }
  pager.innerHTML = '';
  if (!totalPages || totalPages <= 1) {
    return;
  }
  addPageButton('Trước', Math.max(1, page - 1), page === 1);
  for (let i = 1; i <= totalPages; i++) {
    addPageButton(String(i), i, false, i === page);
  }
  addPageButton('Next', Math.min(totalPages, page + 1), page === totalPages);
}

function addPageButton(label, page, disabled, active = false) {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = `page-btn btn btn-sm ${active ? 'active btn-primary' : 'btn-light'}`;
  button.textContent = label;
  button.disabled = disabled;
  button.addEventListener('click', () => loadHistory(page));
  pager.appendChild(button);
}

function renderSplitPager(page, totalPages) {
  if (HistoryListComponent) {
    HistoryListComponent.renderPager(splitPager, page, totalPages, loadSplitHistory);
    return;
  }
  splitPager.innerHTML = '';
  if (!totalPages || totalPages <= 1) {
    return;
  }
  addSplitPageButton('Trước', Math.max(1, page - 1), page === 1);
  for (let i = 1; i <= totalPages; i++) {
    addSplitPageButton(String(i), i, false, i === page);
  }
  addSplitPageButton('Next', Math.min(totalPages, page + 1), page === totalPages);
}

function addSplitPageButton(label, page, disabled, active = false) {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = `page-btn btn btn-sm ${active ? 'active btn-primary' : 'btn-light'}`;
  button.textContent = label;
  button.disabled = disabled;
  button.addEventListener('click', () => loadSplitHistory(page));
  splitPager.appendChild(button);
}

function uploadJob(data) {
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open('POST', '/api/highlights');
    request.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const uploadPercent = Math.min(12, Math.round((event.loaded / event.total) * 12));
        setProgress(uploadPercent, `Đang tải video lên server... ${Math.round((event.loaded / event.total) * 100)}%`);
      }
    };
    request.onload = () => {
      try {
        const result = JSON.parse(request.responseText || '{}');
        if (request.status >= 200 && request.status < 300) {
          resolve(result);
        } else {
          reject(new Error(result.error || 'Upload thất bại.'));
        }
      } catch (error) {
        reject(new Error('Server trả về dữ liệu không hợp lệ.'));
      }
    };
    request.onerror = () => reject(new Error('Không thể upload video lên server.'));
    request.send(data);
  });
}

async function uploadNetworkJob(payload) {
  const response = await fetch('/api/highlights/from-url', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const result = await response.json();
  if (!response.ok) {
    throw new Error(result.error || 'Không tải được video từ mạng.');
  }
  return result;
}

function uploadSplitJob(data) {
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open('POST', '/api/split-highlights');
    request.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const uploadPercent = Math.min(12, Math.round((event.loaded / event.total) * 12));
        splitSetProgress(uploadPercent, `Đang tải video lên server... ${Math.round((event.loaded / event.total) * 100)}%`);
      }
    };
    request.onload = () => {
      try {
        const result = JSON.parse(request.responseText || '{}');
        if (request.status >= 200 && request.status < 300) {
          resolve(result);
        } else {
          reject(new Error(result.error || 'Upload thất bại.'));
        }
      } catch (error) {
        reject(new Error('Server trả về dữ liệu không hợp lệ.'));
      }
    };
    request.onerror = () => reject(new Error('Không thể upload video lên server.'));
    request.send(data);
  });
}

async function uploadSplitNetworkJob(payload) {
  const response = await fetch('/api/split-highlights/from-url', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const result = await response.json();
  if (!response.ok) {
    throw new Error(result.error || 'Không tải được video từ mạng.');
  }
  return result;
}

async function pollJob(jobId) {
  try {
    const response = await fetch(`/api/highlights/${jobId}`);
    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.error || 'Không đọc được tiến trình.');
    }
    if (result.status === 'ready') {
      clearInterval(pollTimer);
      pollTimer = null;
      setProgress(100, result.phase, 'ok');
      processButton.disabled = false;
      await loadHistory(1);
      return;
    }
    if (result.status === 'error') {
      clearInterval(pollTimer);
      pollTimer = null;
      setProgress(100, result.error || 'Xử lý thất bại.', 'error');
      processButton.disabled = false;
      await loadHistory(1);
      return;
    }
    setProgress(result.progress, result.phase || 'Đang xử lý video...');
  } catch (error) {
    clearInterval(pollTimer);
    pollTimer = null;
    setProgress(100, error.message, 'error');
    processButton.disabled = false;
  }
}

async function pollSplitJob(jobId) {
  try {
    const response = await fetch(`/api/split-highlights/${jobId}`);
    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.error || 'Không đọc được tiến trình.');
    }
    if (result.status === 'ready') {
      clearInterval(splitPollTimer);
      splitPollTimer = null;
      splitSetProgress(100, result.phase, 'ok');
      splitProcessButton.disabled = false;
      await loadSplitHistory(1);
      return;
    }
    if (result.status === 'error') {
      clearInterval(splitPollTimer);
      splitPollTimer = null;
      splitSetProgress(100, result.error || 'Xử lý thất bại.', 'error');
      splitProcessButton.disabled = false;
      await loadSplitHistory(1);
      return;
    }
    splitSetProgress(result.progress, result.phase || 'Đang tách clip...');
  } catch (error) {
    clearInterval(splitPollTimer);
    splitPollTimer = null;
    splitSetProgress(100, error.message, 'error');
    splitProcessButton.disabled = false;
  }
}

function initUploadEvents() {
  ['dragenter', 'dragover'].forEach((eventName) => {
    dropzone.addEventListener(eventName, (event) => {
      event.preventDefault();
      dropzone.classList.add('active');
    });
  });

  ['dragleave', 'drop'].forEach((eventName) => {
    dropzone.addEventListener(eventName, (event) => {
      event.preventDefault();
      dropzone.classList.remove('active');
    });
  });

  dropzone.addEventListener('drop', (event) => {
    const files = Array.from(event.dataTransfer.files).filter((file) => file.type.startsWith('video/'));
    if (files.length) {
      addFiles(files);
    }
  });

  fileInput.addEventListener('change', () => addFiles(fileInput.files));
}

function initSplitUploadEvents() {
  ['dragenter', 'dragover'].forEach((eventName) => {
    splitDropzone.addEventListener(eventName, (event) => {
      event.preventDefault();
      splitDropzone.classList.add('active');
    });
  });

  ['dragleave', 'drop'].forEach((eventName) => {
    splitDropzone.addEventListener(eventName, (event) => {
      event.preventDefault();
      splitDropzone.classList.remove('active');
    });
  });

  splitDropzone.addEventListener('drop', (event) => {
    const files = Array.from(event.dataTransfer.files).filter((file) => file.type.startsWith('video/'));
    if (files.length) {
      addSplitFiles(files);
    }
  });

  splitFileInput.addEventListener('change', () => addSplitFiles(splitFileInput.files));
}

function initFormEvents() {
  [localSourceUpload, localSourceNetwork, splitSourceUpload, splitSourceNetwork].forEach((input) => {
    input.addEventListener('change', updateSourcePanels);
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (localUsesNetworkSource()) {
      const url = videoUrl.value.trim();
      if (!url) {
        setProgress(100, 'Bạn chưa nhập link video.', 'error');
        return;
      }
      const payload = {
        videoUrl: url,
        clipCount: document.querySelector('#clipCount').value,
        clipSeconds: document.querySelector('#clipSeconds').value,
        aspectRatio: aspectRatio.value,
        cutNote: cutNote.value.trim(),
        cookiesFilePath: cookiesFilePath.value.trim()
      };
      processButton.disabled = true;
      setProgress(1, 'Đang gửi link video cho server...');
      try {
        const job = await uploadNetworkJob(payload);
        setProgress(job.progress || 3, job.phase || 'Đã nhận link video.');
        pollTimer = setInterval(() => pollJob(job.jobId), 900);
        pollJob(job.jobId);
        await loadHistory(1);
      } catch (error) {
        setProgress(100, error.message, 'error');
        processButton.disabled = false;
      }
      return;
    }

    const files = selectedFiles.slice();
    if (!files.length) {
      setProgress(100, 'Bạn chưa chọn video.', 'error');
      return;
    }
    const data = new FormData();
    files.forEach((file) => data.append('videos', file));
    data.append('clipCount', document.querySelector('#clipCount').value);
    data.append('clipSeconds', document.querySelector('#clipSeconds').value);
    data.append('aspectRatio', aspectRatio.value);
    data.append('cutNote', cutNote.value.trim());

    processButton.disabled = true;
    setProgress(1, 'Đang chuẩn bị upload video...');
    try {
      const job = await uploadJob(data);
      setProgress(job.progress || 12, job.phase || 'Đã nhận job xử lý.');
      pollTimer = setInterval(() => pollJob(job.jobId), 900);
      pollJob(job.jobId);
      await loadHistory(1);
    } catch (error) {
      setProgress(100, error.message, 'error');
      processButton.disabled = false;
    }
  });

  refreshButton.addEventListener('click', () => loadHistory(currentPage));

  selectAllHistory.addEventListener('change', () => {
    const pageIds = currentHistoryItems.map((item) => item.jobId).filter(Boolean);
    if (selectAllHistory.checked) {
      pageIds.forEach((jobId) => selectedHistoryIds.add(jobId));
    } else {
      pageIds.forEach((jobId) => selectedHistoryIds.delete(jobId));
    }
    historyBody.querySelectorAll('.history-check').forEach((checkbox) => {
      checkbox.checked = selectAllHistory.checked;
    });
    updateHistorySelection();
  });

  deleteSelectedButton.addEventListener('click', () => deleteHistoryJobs(Array.from(selectedHistoryIds)));
}

function initSplitFormEvents() {
  splitForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (splitUsesNetworkSource()) {
      const url = splitVideoUrl.value.trim();
      if (!url) {
        splitSetProgress(100, 'Bạn chưa nhập link video.', 'error');
        return;
      }
      const payload = {
        videoUrl: url,
        clipCount: document.querySelector('#splitClipCount').value,
        clipSeconds: document.querySelector('#splitClipSeconds').value,
        aspectRatio: splitAspectRatio.value,
        cutNote: splitCutNote.value.trim(),
        cookiesFilePath: splitCookiesFilePath.value.trim()
      };
      splitProcessButton.disabled = true;
      splitSetProgress(1, 'Đang gửi link video cho server...');
      try {
        const job = await uploadSplitNetworkJob(payload);
        splitSetProgress(job.progress || 3, job.phase || 'Đã nhận link video.');
        splitPollTimer = setInterval(() => pollSplitJob(job.jobId), 900);
        pollSplitJob(job.jobId);
        await loadSplitHistory(1);
      } catch (error) {
        splitSetProgress(100, error.message, 'error');
        splitProcessButton.disabled = false;
      }
      return;
    }

    const files = splitSelectedFiles.slice();
    if (!files.length) {
      splitSetProgress(100, 'Bạn chưa chọn video.', 'error');
      return;
    }
    const data = new FormData();
    files.forEach((file) => data.append('videos', file));
    data.append('clipCount', document.querySelector('#splitClipCount').value);
    data.append('clipSeconds', document.querySelector('#splitClipSeconds').value);
    data.append('aspectRatio', splitAspectRatio.value);
    data.append('cutNote', splitCutNote.value.trim());

    splitProcessButton.disabled = true;
    splitSetProgress(1, 'Đang chuẩn bị upload video...');
    try {
      const job = await uploadSplitJob(data);
      splitSetProgress(job.progress || 12, job.phase || 'Đã nhận job tách clip.');
      splitPollTimer = setInterval(() => pollSplitJob(job.jobId), 900);
      pollSplitJob(job.jobId);
      await loadSplitHistory(1);
    } catch (error) {
      splitSetProgress(100, error.message, 'error');
      splitProcessButton.disabled = false;
    }
  });

  splitRefreshButton.addEventListener('click', () => loadSplitHistory(splitCurrentPage));

  splitSelectAllHistory.addEventListener('change', () => {
    const pageKeys = splitCurrentHistoryItems.map((item) => splitClipKey(item.jobId, item.clipIndex)).filter(Boolean);
    if (splitSelectAllHistory.checked) {
      pageKeys.forEach((key) => splitSelectedClipKeys.add(key));
    } else {
      pageKeys.forEach((key) => splitSelectedClipKeys.delete(key));
    }
    splitHistoryBody.querySelectorAll('.split-history-check').forEach((checkbox) => {
      checkbox.checked = splitSelectAllHistory.checked;
    });
    updateSplitHistorySelection();
  });

  splitDeleteSelectedButton.addEventListener('click', () => {
    const selectedClips = Array.from(splitSelectedClipKeys).map(splitClipRefFromKey);
    deleteSplitClips(selectedClips);
  });
}

function initEditorEvents() {
  editorClose.addEventListener('click', closeEditor);
  editorBackdrop.addEventListener('click', closeEditor);
  editorVideo.addEventListener('loadedmetadata', initializeEditorTrim);
  editorVideo.addEventListener('loadeddata', drawEditorFrame);
  editorVideo.addEventListener('timeupdate', () => {
    updateEditorPlayhead();
    syncEditorAudioPreview(false);
  });
  editorVideo.addEventListener('seeked', () => {
    updateEditorPlayhead();
    syncEditorAudioPreview(true);
  });
  editorVideo.addEventListener('play', playEditorAudioPreview);
  editorVideo.addEventListener('play', updateEditorPlayPause);
  editorVideo.addEventListener('pause', () => {
    pauseEditorAudioPreview();
    updateEditorPlayPause();
  });
  editorVideo.addEventListener('ended', () => {
    pauseEditorAudioPreview();
    updateEditorPlayPause();
  });
  editorVideo.addEventListener('ratechange', () => {
    editorAudioPreview.playbackRate = editorVideo.playbackRate || 1;
  });
  editorStart.addEventListener('input', syncEditorTrimFromFields);
  editorEnd.addEventListener('input', syncEditorTrimFromFields);
  editorRotation.addEventListener('input', updateEditorRotationPreview);
  editorAspectRatio.addEventListener('change', () => {
    pushEditorUndo();
    updateEditorResolutionOptions();
    updateEditorFrameGuide();
  });
  editorOutputResolution.addEventListener('change', updateEditorFrameGuide);
  editorTextHorizontal.addEventListener('change', applyEditorTextPreset);
  editorTextPosition.addEventListener('change', applyEditorTextPreset);
  editorTextSize.addEventListener('input', updateEditorTextOverlay);
  editorTextColor.addEventListener('input', updateEditorTextOverlay);
  editorTextFont.addEventListener('change', updateEditorTextOverlay);
  editorTextBackground.addEventListener('change', updateEditorTextOverlay);
  editorOverlayText.addEventListener('input', updateEditorTextOverlay);
  editorAudioMode.addEventListener('change', updateEditorAudioPreview);
  editorMuteOriginal.addEventListener('change', updateEditorAudioPreview);
  editorMusic.addEventListener('change', loadEditorAudioPreview);
  editorTextOverlay.addEventListener('pointerdown', startEditorTextDrag);
  editorTextResize.addEventListener('pointerdown', startEditorTextResize);
  editorTrimStartHandle.addEventListener('pointerdown', (event) => startTrimDrag(event, 'start'));
  editorTrimEndHandle.addEventListener('pointerdown', (event) => startTrimDrag(event, 'end'));
  editorTrimSelection.addEventListener('pointerdown', (event) => startTrimDrag(event, 'range'));
  editorTrimTrack.addEventListener('pointerdown', seekEditorFromTimeline);
  editorPlayPause.addEventListener('click', toggleEditorPlayback);
  editorUndo.addEventListener('click', undoEditorStep);
  editorZoomOut.addEventListener('click', () => {
    pushEditorUndo();
    setEditorZoom(editorZoom - 0.15);
  });
  editorZoomReset.addEventListener('click', () => {
    pushEditorUndo();
    setEditorZoom(1);
  });
  editorZoomIn.addEventListener('click', () => {
    pushEditorUndo();
    setEditorZoom(editorZoom + 0.15);
  });
  editorTimelineZoomOut.addEventListener('click', () => setEditorTimelineZoom(editorTimelineZoom - 0.5));
  editorTimelineZoomReset.addEventListener('click', () => setEditorTimelineZoom(1));
  editorTimelineZoomIn.addEventListener('click', () => setEditorTimelineZoom(editorTimelineZoom + 0.5));
  editorSetStartAtPlayhead.addEventListener('click', () => {
    pushEditorUndo();
    selectSegmentAtPlayhead(false);
    setEditorTrim(editorVideo.currentTime || 0, editorTrim.end, true);
  });
  editorSetEndAtPlayhead.addEventListener('click', () => {
    pushEditorUndo();
    selectSegmentAtPlayhead(false);
    setEditorTrim(editorTrim.start, editorVideo.currentTime || editorTrim.end, true);
  });
  editorSplitAtPlayhead.addEventListener('click', splitEditorAtPlayhead);
  editorDeleteSegment.addEventListener('click', deleteSelectedEditorSegment);
  editorResetSegments.addEventListener('click', resetEditorSegments);
  editorExportNew.addEventListener('click', () => {
    editorSaveMode.value = 'new';
    submitVideoEdit();
  });
  editorForm.addEventListener('submit', (event) => {
    event.preventDefault();
    submitVideoEdit();
  });
  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && editorModal.classList.contains('active')) {
      closeEditor();
    }
  });
  window.addEventListener('resize', updateEditorFrameGuide);
}

function initPreviewEvents() {
  previewClose.addEventListener('click', closePreview);
  previewBackdrop.addEventListener('click', closePreview);
  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && previewModal.classList.contains('active')) {
      closePreview();
    }
  });
}

function openEditor(editUrl, previewUrl, title, kind) {
  currentEditor = { editUrl, previewUrl, title, kind };
  editorTitle.textContent = `Chỉnh sửa - ${title || 'video'}`;
  editorVideo.src = `${previewUrl}${previewUrl.includes('?') ? '&' : '?'}t=${Date.now()}`;
  resetEditorTrim();
  resetEditorUndo();
  editorSourceType.value = 'output';
  editorSaveMode.value = 'new';
  editorStart.value = '';
  editorEnd.value = '';
  editorRotation.value = '0';
  editorAspectRatio.value = 'keep';
  setEditorZoom(1);
  setEditorTimelineZoom(1);
  updateEditorRotationPreview();
  editorTextHorizontal.value = 'center';
  editorTextPosition.value = 'center';
  editorTextSize.value = '42';
  editorTextColor.value = '#ffffff';
  editorTextFont.value = 'arial';
  editorTextBackground.value = 'dark';
  editorOverlayText.value = '';
  resetEditorTextOverlay();
  editorAudioMode.value = 'keep';
  editorMuteOriginal.checked = false;
  editorTitleInput.value = '';
  editorMusic.value = '';
  resetEditorAudioPreview();
  setEditorStatus('Sẵn sàng chỉnh sửa.');
  editorModal.classList.add('active');
  editorModal.setAttribute('aria-hidden', 'false');
  editorVideo.load();
  updateEditorPlayPause();
}

function closeEditor() {
  if (editorRenderActive) {
    setEditorRenderProgress(editorRenderProgress, 'Video đang được render, vui lòng chờ hoàn tất.');
    return;
  }
  editorVideo.pause();
  resetEditorAudioPreview();
  editorVideo.removeAttribute('src');
  editorVideo.load();
  editorModal.classList.remove('active');
  editorModal.setAttribute('aria-hidden', 'true');
  currentEditor = null;
  resetEditorTrim();
  updateEditorPlayPause();
}

function resetEditorTrim() {
  editorTrim = { duration: 0, start: 0, end: 0, dragging: null, dragOffset: 0 };
  editorSegments = [];
  selectedEditorSegmentIndex = 0;
  editorStart.value = '';
  editorEnd.value = '';
  updateEditorTrimUI();
}

function initializeEditorTrim() {
  const duration = Number(editorVideo.duration || 0);
  if (!Number.isFinite(duration) || duration <= 0) {
    resetEditorTrim();
    return;
  }
  editorNativeSize = {
    width: Number(editorVideo.videoWidth || 0),
    height: Number(editorVideo.videoHeight || 0)
  };
  updateEditorResolutionOptions();
  updateEditorFrameGuide();
  editorTrim.duration = duration;
  editorTrim.start = 0;
  editorTrim.end = duration;
  editorSegments = [{ start: 0, end: roundTrimTime(duration) }];
  selectedEditorSegmentIndex = 0;
  selectEditorSegment(0, 0);
}

function syncEditorTrimFromFields() {
  if (!editorTrim.duration) {
    return;
  }
  const start = parseTrimNumber(editorStart.value, 0);
  const end = parseTrimNumber(editorEnd.value, editorTrim.duration);
  setEditorTrim(start, end, false);
}

function startTrimDrag(event, mode) {
  if (!editorTrim.duration) {
    return;
  }
  pushEditorUndo();
  event.preventDefault();
  event.stopPropagation();
  const time = trimTimeFromPointer(event);
  editorTrim.dragging = mode;
  editorTrim.dragOffset = mode === 'range' ? time - editorTrim.start : 0;
  editorTrimTrack.setPointerCapture(event.pointerId);
  editorTrimTrack.addEventListener('pointermove', onTrimDrag);
  editorTrimTrack.addEventListener('pointerup', stopTrimDrag, { once: true });
  editorTrimTrack.addEventListener('pointercancel', stopTrimDrag, { once: true });
}

function onTrimDrag(event) {
  if (!editorTrim.duration || !editorTrim.dragging) {
    return;
  }
  const time = trimTimeFromPointer(event);
  if (editorTrim.dragging === 'start') {
    setEditorTrim(time, editorTrim.end, true);
    return;
  }
  if (editorTrim.dragging === 'end') {
    setEditorTrim(editorTrim.start, time, true);
    return;
  }
  const length = editorTrim.end - editorTrim.start;
  const bounds = selectedEditorSegmentBounds();
  let start = time - editorTrim.dragOffset;
  start = Math.max(bounds.min, Math.min(bounds.max - length, start));
  setEditorTrim(start, start + length, true);
}

function stopTrimDrag() {
  editorTrim.dragging = null;
  editorTrimTrack.removeEventListener('pointermove', onTrimDrag);
}

function seekEditorFromTimeline(event) {
  if (!editorTrim.duration) {
    return;
  }
  const segmentButton = event.target.closest ? event.target.closest('.trim-segment') : null;
  if (segmentButton) {
    const index = Number(segmentButton.dataset.index);
    const time = trimTimeFromPointer(event);
    selectEditorSegment(index, time);
    return;
  }
  if (event.target === editorTrimSelection || event.target === editorTrimStartHandle || event.target === editorTrimEndHandle) {
    return;
  }
  const time = trimTimeFromPointer(event);
  editorVideo.currentTime = time;
  updateEditorPlayhead();
}

function setEditorTrim(start, end, seekToStart) {
  if (!editorTrim.duration) {
    return;
  }
  const bounds = selectedEditorSegmentBounds();
  const minLength = Math.min(0.2, editorTrim.duration);
  let safeStart = Math.max(bounds.min, Math.min(bounds.max, Number(start || 0)));
  let safeEnd = Math.max(bounds.min, Math.min(bounds.max, Number(end || editorTrim.duration)));
  if (safeEnd < safeStart + minLength) {
    if (editorTrim.dragging === 'start') {
      safeStart = Math.max(bounds.min, safeEnd - minLength);
    } else {
      safeEnd = Math.min(bounds.max, safeStart + minLength);
    }
  }
  editorTrim.start = roundTrimTime(safeStart);
  editorTrim.end = roundTrimTime(safeEnd);
  if (editorSegments[selectedEditorSegmentIndex]) {
    editorSegments[selectedEditorSegmentIndex] = { start: editorTrim.start, end: editorTrim.end };
  }
  editorStart.value = formatTrimInput(editorTrim.start);
  editorEnd.value = formatTrimInput(editorTrim.end);
  if (seekToStart) {
    editorVideo.currentTime = editorTrim.dragging === 'end' ? editorTrim.end : editorTrim.start;
  }
  updateEditorTrimUI();
}

function updateEditorTrimUI() {
  const duration = editorTrim.duration || 0;
  const startRatio = duration ? editorTrim.start / duration : 0;
  const endRatio = duration ? editorTrim.end / duration : 0;
  const widthRatio = Math.max(0, endRatio - startRatio);
  renderEditorSegments();
  renderEditorSegmentOrder();
  editorTrimSelection.style.left = `${startRatio * 100}%`;
  editorTrimSelection.style.width = `${widthRatio * 100}%`;
  editorTrimStartHandle.style.left = `${startRatio * 100}%`;
  editorTrimEndHandle.style.left = `${endRatio * 100}%`;
  editorTrimStartLabel.textContent = formatTimelineTime(editorTrim.start);
  editorTrimEndLabel.textContent = formatTimelineTime(editorTrim.end);
  const segmentLabel = editorSegments.length ? `Đoạn ${selectedEditorSegmentIndex + 1}/${editorSegments.length}` : 'Đoạn đã chọn';
  editorTrimDurationLabel.textContent = `${segmentLabel}: ${formatTimelineTime(Math.max(0, editorTrim.end - editorTrim.start))}`;
  updateEditorPlayhead();
}

function renderEditorSegments() {
  if (!editorSegmentsLayer) {
    return;
  }
  editorSegmentsLayer.innerHTML = '';
  const duration = editorTrim.duration || 0;
  if (!duration) {
    return;
  }
  editorSegments.forEach((segment, index) => {
    const button = EditorTimelineComponent
      ? EditorTimelineComponent.createTimelineSegmentButton({
          segment,
          index,
          duration,
          active: index === selectedEditorSegmentIndex,
          formatTime: formatTimelineTime
        })
      : document.createElement('button');
    button.addEventListener('click', (event) => {
      event.stopPropagation();
      selectEditorSegment(index, trimTimeFromPointer(event));
    });
    wireEditorSegmentDrag(button, index);
    editorSegmentsLayer.appendChild(button);
  });
}

function renderEditorSegmentOrder() {
  if (!editorSegmentOrderList) {
    return;
  }
  editorSegmentOrderList.innerHTML = '';
  if (!editorSegments.length) {
    return;
  }
  editorSegments.forEach((segment, index) => {
    const button = EditorTimelineComponent
      ? EditorTimelineComponent.createSegmentOrderButton({
          segment,
          index,
          active: index === selectedEditorSegmentIndex,
          formatTime: formatTimelineTime
        })
      : document.createElement('button');
    button.addEventListener('click', () => selectEditorSegment(index, segment.start));
    wireEditorSegmentDrag(button, index);
    editorSegmentOrderList.appendChild(button);
  });
}

function wireEditorSegmentDrag(element, index) {
  element.addEventListener('dragstart', (event) => {
    draggedEditorSegmentIndex = index;
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', String(index));
  });
  element.addEventListener('dragover', (event) => {
    event.preventDefault();
    element.classList.add('drag-over');
    event.dataTransfer.dropEffect = 'move';
  });
  element.addEventListener('dragleave', () => element.classList.remove('drag-over'));
  element.addEventListener('drop', (event) => {
    event.preventDefault();
    element.classList.remove('drag-over');
    const from = draggedEditorSegmentIndex ?? Number(event.dataTransfer.getData('text/plain'));
    const to = Number(element.dataset.index);
    draggedEditorSegmentIndex = null;
    reorderEditorSegment(from, to);
  });
  element.addEventListener('dragend', () => {
    draggedEditorSegmentIndex = null;
    element.classList.remove('drag-over');
  });
}

function reorderEditorSegment(from, to) {
  if (!Number.isInteger(from) || !Number.isInteger(to) || from === to || from < 0 || to < 0
      || from >= editorSegments.length || to >= editorSegments.length) {
    return;
  }
  pushEditorUndo();
  const [segment] = editorSegments.splice(from, 1);
  editorSegments.splice(to, 0, segment);
  selectedEditorSegmentIndex = to;
  selectEditorSegment(to, segment.start, true);
  setEditorStatus(`Đã đổi thứ tự xuất: khúc ${from + 1} sang vị trí ${to + 1}.`, 'ok');
}

function segmentIndexAtTime(time) {
  const safeTime = Number(time || 0);
  const tolerance = 0.08;
  const insideIndex = editorSegments.findIndex((segment) => safeTime > segment.start + tolerance && safeTime < segment.end - tolerance);
  if (insideIndex >= 0) {
    return insideIndex;
  }
  const leftBoundaryIndex = editorSegments.findIndex((segment) => Math.abs(safeTime - segment.end) <= tolerance);
  if (leftBoundaryIndex >= 0) {
    return leftBoundaryIndex;
  }
  return editorSegments.findIndex((segment) => safeTime >= segment.start - tolerance && safeTime <= segment.end + tolerance);
}

function selectSegmentAtPlayhead(shouldSeek = false) {
  const time = roundTrimTime(editorVideo.currentTime || 0);
  const index = segmentIndexAtTime(time);
  if (index >= 0) {
    selectEditorSegment(index, time, shouldSeek);
  }
  return index;
}

function selectedEditorSegmentBounds() {
  const current = editorSegments[selectedEditorSegmentIndex];
  if (!current) {
    return { min: 0, max: editorTrim.duration };
  }
  let min = 0;
  let max = editorTrim.duration;
  editorSegments.forEach((segment, index) => {
    if (index === selectedEditorSegmentIndex) {
      return;
    }
    if (segment.end <= current.start) {
      min = Math.max(min, segment.end);
    }
    if (segment.start >= current.end) {
      max = Math.min(max, segment.start);
    }
  });
  return { min, max };
}

function selectEditorSegment(index, seekTime, shouldSeek = true) {
  if (!editorSegments.length) {
    updateEditorTrimUI();
    return;
  }
  selectedEditorSegmentIndex = Math.max(0, Math.min(editorSegments.length - 1, Number(index || 0)));
  const segment = editorSegments[selectedEditorSegmentIndex];
  editorTrim.start = roundTrimTime(segment.start);
  editorTrim.end = roundTrimTime(segment.end);
  editorStart.value = formatTrimInput(editorTrim.start);
  editorEnd.value = formatTrimInput(editorTrim.end);
  if (shouldSeek && Number.isFinite(seekTime)) {
    editorVideo.currentTime = Math.max(editorTrim.start, Math.min(editorTrim.end, seekTime));
  } else if (shouldSeek && (editorVideo.currentTime < editorTrim.start || editorVideo.currentTime > editorTrim.end)) {
    editorVideo.currentTime = editorTrim.start;
  }
  updateEditorTrimUI();
}

function splitEditorAtPlayhead() {
  if (!editorTrim.duration) {
    return;
  }
  pushEditorUndo();
  const time = roundTrimTime(editorVideo.currentTime || 0);
  const segmentIndex = segmentIndexAtTime(time);
  if (segmentIndex < 0) {
    setEditorStatus('Đầu phát đang nằm ở khoảng đã xóa. Hãy tua vào một đoạn còn giữ lại rồi tách.', 'error');
    return;
  }
  selectedEditorSegmentIndex = segmentIndex;
  const segment = editorSegments[selectedEditorSegmentIndex];
  const minGap = Math.min(0.2, editorTrim.duration);
  if (time <= segment.start + minGap || time >= segment.end - minGap) {
    setEditorStatus('Đưa đầu phát vào giữa đoạn cần tách rồi bấm tách.', 'error');
    return;
  }
  editorSegments.splice(
    selectedEditorSegmentIndex,
    1,
    { start: roundTrimTime(segment.start), end: time },
    { start: time, end: roundTrimTime(segment.end) }
  );
  selectEditorSegment(selectedEditorSegmentIndex, time, false);
  setEditorStatus(`Đã tách thành ${editorSegments.length} khúc. Nếu vừa tách cuối đoạn cần bỏ, bấm xóa sẽ xóa khúc bên trái đầu phát.`, 'ok');
}

function deleteSelectedEditorSegment() {
  if (editorSegments.length <= 1) {
    setEditorStatus('Timeline cần giữ lại ít nhất một đoạn.', 'error');
    return;
  }
  pushEditorUndo();
  const playheadIndex = segmentIndexAtTime(editorVideo.currentTime || 0);
  if (playheadIndex >= 0) {
    selectedEditorSegmentIndex = playheadIndex;
  }
  editorSegments.splice(selectedEditorSegmentIndex, 1);
  selectedEditorSegmentIndex = Math.min(selectedEditorSegmentIndex, editorSegments.length - 1);
  selectEditorSegment(selectedEditorSegmentIndex);
  setEditorStatus(`Đã xóa khúc khỏi bản xuất. Còn ${editorSegments.length} khúc sẽ được ghép khi lưu.`, 'ok');
}

function resetEditorSegments() {
  if (!editorTrim.duration) {
    return;
  }
  pushEditorUndo();
  editorSegments = [{ start: 0, end: roundTrimTime(editorTrim.duration) }];
  selectedEditorSegmentIndex = 0;
  selectEditorSegment(0, 0);
  setEditorStatus('Đã khôi phục timeline ban đầu.', 'ok');
}

function editorSnapshot() {
  return {
    segments: editorSegments.map((segment) => ({ start: segment.start, end: segment.end })),
    selectedIndex: selectedEditorSegmentIndex,
    currentTime: Number(editorVideo.currentTime || 0),
    trim: { start: editorTrim.start, end: editorTrim.end },
    zoom: editorZoom,
    rotation: editorRotation.value,
    aspectRatio: editorAspectRatio.value,
    outputResolution: editorOutputResolution.value,
    textHorizontal: editorTextHorizontal.value,
    textPosition: editorTextPosition.value,
    textState: { ...editorTextState },
    textSize: editorTextSize.value,
    textColor: editorTextColor.value,
    textFont: editorTextFont.value,
    textBackground: editorTextBackground.value,
    overlayText: editorOverlayText.value,
    audioMode: editorAudioMode.value,
    muteOriginal: editorMuteOriginal.checked,
    timelineZoom: editorTimelineZoom
  };
}

function pushEditorUndo() {
  if (!editorModal.classList.contains('active')) {
    return;
  }
  editorUndoStack.push(editorSnapshot());
  if (editorUndoStack.length > 40) {
    editorUndoStack.shift();
  }
  updateEditorUndoButton();
}

function resetEditorUndo() {
  editorUndoStack = [];
  updateEditorUndoButton();
}

function undoEditorStep() {
  const snapshot = editorUndoStack.pop();
  if (!snapshot) {
    updateEditorUndoButton();
    return;
  }
  editorSegments = snapshot.segments.map((segment) => ({ start: segment.start, end: segment.end }));
  selectedEditorSegmentIndex = Math.max(0, Math.min(editorSegments.length - 1, snapshot.selectedIndex || 0));
  editorTextState = { ...snapshot.textState };
  editorTextHorizontal.value = snapshot.textHorizontal || 'center';
  editorTextPosition.value = snapshot.textPosition || 'center';
  editorTextSize.value = snapshot.textSize;
  editorTextColor.value = snapshot.textColor;
  editorTextFont.value = snapshot.textFont || 'arial';
  editorTextBackground.value = snapshot.textBackground || 'dark';
  editorOverlayText.value = snapshot.overlayText || '';
  editorRotation.value = snapshot.rotation || '0';
  editorAspectRatio.value = snapshot.aspectRatio || 'keep';
  updateEditorResolutionOptions(snapshot.outputResolution || '');
  editorAudioMode.value = snapshot.audioMode || 'keep';
  editorMuteOriginal.checked = Boolean(snapshot.muteOriginal);
  editorZoom = snapshot.zoom || 1;
  editorTimelineZoom = snapshot.timelineZoom || 1;
  if (editorSegments.length) {
    const segment = editorSegments[selectedEditorSegmentIndex];
    editorTrim.start = segment.start;
    editorTrim.end = segment.end;
    editorStart.value = formatTrimInput(editorTrim.start);
    editorEnd.value = formatTrimInput(editorTrim.end);
    editorVideo.currentTime = Math.max(segment.start, Math.min(segment.end, snapshot.currentTime || segment.start));
  }
  updateEditorVideoTransform();
  setEditorTimelineZoom(editorTimelineZoom);
  updateEditorTextOverlay();
  updateEditorAudioPreview();
  updateEditorTrimUI();
  updateEditorUndoButton();
  setEditorStatus('Đã hoàn tác một bước.', 'ok');
}

function updateEditorUndoButton() {
  if (editorUndo) {
    editorUndo.disabled = editorUndoStack.length === 0;
  }
}

function updateEditorPlayhead() {
  const duration = editorTrim.duration || Number(editorVideo.duration || 0);
  if (!duration || !Number.isFinite(duration)) {
    editorTrimPlayhead.style.left = '0%';
    return;
  }
  const ratio = Math.max(0, Math.min(1, Number(editorVideo.currentTime || 0) / duration));
  editorTrimPlayhead.style.left = `${ratio * 100}%`;
}

function trimTimeFromPointer(event) {
  const rect = editorTrimTrack.getBoundingClientRect();
  const ratio = rect.width ? Math.max(0, Math.min(1, (event.clientX - rect.left) / rect.width)) : 0;
  return ratio * (editorTrim.duration || 0);
}

function parseTrimNumber(value, fallback) {
  const number = Number(value);
  return Number.isFinite(number) ? number : fallback;
}

function roundTrimTime(value) {
  return Math.round(value * 1000) / 1000;
}

function formatTrimInput(value) {
  return roundTrimTime(value).toFixed(3).replace(/\.?0+$/, '');
}

function formatTimelineTime(value) {
  const safe = Math.max(0, Number(value || 0));
  const minutes = Math.floor(safe / 60);
  const seconds = safe - minutes * 60;
  return `${String(minutes).padStart(2, '0')}:${seconds.toFixed(3).padStart(6, '0')}`;
}

function updateEditorResolutionOptions(preferredValue = editorOutputResolution.value) {
  const width = editorNativeSize.width;
  const height = editorNativeSize.height;
  const aspectMode = editorAspectRatio.value || 'keep';
  editorOutputResolution.innerHTML = '';
  const appendOption = (value, label) => {
    const option = document.createElement('option');
    option.value = value;
    option.textContent = label;
    editorOutputResolution.appendChild(option);
    return option;
  };
  if (aspectMode === 'keep') {
    appendOption('', width && height ? `Giữ nguyên (${width}x${height})` : 'Giữ nguyên');
  }
  if (!width || !height) {
    return;
  }
  if (aspectMode !== 'keep') {
    const candidates = aspectMode === '16:9'
      ? [[3840, 2160], [2560, 1440], [1920, 1080], [1280, 720], [854, 480], [640, 360]]
      : [[2160, 3840], [1440, 2560], [1080, 1920], [720, 1280], [480, 854], [360, 640]];
    const maxSide = Math.max(width, height);
    const labelPrefix = aspectMode === '16:9' ? '16:9 ngang' : '9:16 dọc';
    let added = 0;
    candidates.forEach(([outputWidth, outputHeight]) => {
      if (Math.max(outputWidth, outputHeight) > maxSide) {
        return;
      }
      appendOption(`${outputWidth}x${outputHeight}`, `${labelPrefix} - ${outputWidth}x${outputHeight}`);
      added += 1;
    });
    if (!added) {
      const aspect = aspectMode === '16:9' ? 16 / 9 : 9 / 16;
      const outputWidth = aspectMode === '16:9' ? roundEven(maxSide) : roundEven(maxSide * aspect);
      const outputHeight = aspectMode === '16:9' ? roundEven(maxSide / aspect) : roundEven(maxSide);
      appendOption(`${outputWidth}x${outputHeight}`, `${labelPrefix} - ${outputWidth}x${outputHeight}`);
    }
    editorOutputResolution.value = [...editorOutputResolution.options].some((option) => option.value === preferredValue)
      ? preferredValue
      : editorOutputResolution.options[0].value;
    return;
  }
  const portrait = height > width;
  const aspect = width / height;
  const levels = [480, 720, 1080, 1440, 2160];
  const seen = new Set();
  levels.forEach((level) => {
    let outputWidth;
    let outputHeight;
    if (portrait) {
      outputWidth = level;
      outputHeight = roundEven(level / aspect);
      if (outputWidth > width || outputHeight > height) {
        return;
      }
    } else {
      outputHeight = level;
      outputWidth = roundEven(level * aspect);
      if (outputWidth > width || outputHeight > height) {
        return;
      }
    }
    outputWidth = roundEven(outputWidth);
    outputHeight = roundEven(outputHeight);
    const value = `${outputWidth}x${outputHeight}`;
    if (seen.has(value) || (outputWidth === width && outputHeight === height)) {
      return;
    }
    seen.add(value);
    appendOption(value, value);
  });
  if ([...editorOutputResolution.options].some((option) => option.value === preferredValue)) {
    editorOutputResolution.value = preferredValue;
  }
}

function roundEven(value) {
  const rounded = Math.max(2, Math.round(Number(value || 0)));
  return rounded % 2 === 0 ? rounded : rounded + 1;
}

function updateEditorRotationPreview() {
  updateEditorVideoTransform();
}

function setEditorZoom(value) {
  editorZoom = Math.max(0.5, Math.min(2.5, Number(value || 1)));
  updateEditorVideoTransform();
}

function setEditorTimelineZoom(value) {
  editorTimelineZoom = Math.max(1, Math.min(12, Number(value || 1)));
  editorTrimTrack.style.setProperty('--timeline-zoom', editorTimelineZoom);
  editorTimelineZoomValue.textContent = `${Math.round(editorTimelineZoom * 100)}%`;
}

function updateEditorVideoTransform() {
  const rotation = Number(editorRotation.value || 0);
  const safeRotation = Number.isFinite(rotation) ? rotation : 0;
  editorRotationValue.textContent = `${safeRotation} độ`;
  editorZoomValue.textContent = `${Math.round(editorZoom * 100)}%`;
  editorVideoLayer.style.transform = `rotate(${safeRotation}deg) scale(${editorZoom})`;
  updateEditorFrameGuide();
}

function updateEditorFrameGuide() {
  if (!editorFrameGuide || !editorStage) {
    return;
  }
  const size = parseEditorOutputResolution() || editorNativeSize;
  if (!size.width || !size.height) {
    editorFrameGuide.style.display = 'none';
    updateEditorTextOverlay();
    return;
  }
  const stageWidth = editorStage.clientWidth || 0;
  const stageHeight = editorStage.clientHeight || 0;
  if (!stageWidth || !stageHeight) {
    return;
  }
  const aspect = size.width / size.height;
  let width = stageWidth * 0.88;
  let height = width / aspect;
  if (height > stageHeight * 0.88) {
    height = stageHeight * 0.88;
    width = height * aspect;
  }
  editorFrameGuide.style.display = 'block';
  editorFrameGuide.style.width = `${width}px`;
  editorFrameGuide.style.height = `${height}px`;
  editorFrameGuide.style.left = `${(stageWidth - width) / 2}px`;
  editorFrameGuide.style.top = `${(stageHeight - height) / 2}px`;
  editorFrameGuide.dataset.label = `${size.width}x${size.height}`;
  updateEditorTextOverlay();
}

function resetEditorTextOverlay() {
  editorTextState = { x: 50, y: 82, dragging: false, resizing: false, dragOffsetX: 0, dragOffsetY: 0, resizeStartX: 0, resizeStartY: 0, resizeStartSize: 42 };
  updateEditorTextOverlay();
}

function updateEditorTextOverlay() {
  const text = editorOverlayText.value.trim();
  editorTextContent.textContent = text;
  editorTextOverlay.classList.toggle('active', Boolean(text));
  editorTextOverlay.classList.toggle('dragging', editorTextState.dragging || editorTextState.resizing);
  editorTextOverlay.classList.remove('text-bg-none', 'text-bg-outline', 'text-bg-light', 'text-bg-highlight');
  const background = editorTextBackground.value || 'dark';
  if (background !== 'dark') {
    editorTextOverlay.classList.add(`text-bg-${background}`);
  }
  const metrics = editorFrameMetrics();
  const left = metrics.left + (editorTextState.x / 100) * metrics.width;
  const top = metrics.top + (editorTextState.y / 100) * metrics.height;
  editorTextOverlay.style.left = `${left}px`;
  editorTextOverlay.style.top = `${top}px`;
  editorTextOverlay.style.setProperty('--text-x', editorTextState.x);
  editorTextOverlay.style.setProperty('--text-y', editorTextState.y);
  editorTextOverlay.style.fontFamily = editorFontFamily(editorTextFont.value);
  editorTextOverlay.style.fontSize = `${Math.max(18, Math.min(160, Number(editorTextSize.value || 42)))}px`;
  editorTextOverlay.style.color = editorTextColor.value || '#ffffff';
  editorTextOverlay.style.textAlign = editorTextHorizontal.value || 'center';
  editorTextOverlay.style.justifyContent = textVerticalJustify(editorTextPosition.value);
}

function editorFontFamily(value) {
  const fonts = {
    arial: 'Arial, Helvetica, sans-serif',
    segoe: '"Segoe UI", Arial, sans-serif',
    tahoma: 'Tahoma, Arial, sans-serif',
    verdana: 'Verdana, Arial, sans-serif',
    impact: 'Impact, Haettenschweiler, sans-serif',
    georgia: 'Georgia, serif',
    times: '"Times New Roman", Times, serif',
    comic: '"Comic Sans MS", cursive'
  };
  return fonts[value] || fonts.arial;
}

function textVerticalJustify(value) {
  if (value === 'top') {
    return 'flex-start';
  }
  if (value === 'bottom') {
    return 'flex-end';
  }
  return 'center';
}

function applyEditorTextPreset() {
  updateEditorTextOverlay();
}

function startEditorTextDrag(event) {
  if (!editorOverlayText.value.trim()) {
    return;
  }
  pushEditorUndo();
  event.preventDefault();
  event.stopPropagation();
  const stageRect = editorStage.getBoundingClientRect();
  const metrics = editorFrameMetrics();
  const anchorX = stageRect.left + metrics.left + (editorTextState.x / 100) * metrics.width;
  const anchorY = stageRect.top + metrics.top + (editorTextState.y / 100) * metrics.height;
  editorTextState.dragOffsetX = event.clientX - anchorX;
  editorTextState.dragOffsetY = event.clientY - anchorY;
  editorTextState.dragging = true;
  editorTextOverlay.setPointerCapture(event.pointerId);
  editorTextOverlay.addEventListener('pointermove', dragEditorText);
  editorTextOverlay.addEventListener('pointerup', stopEditorTextDrag, { once: true });
  editorTextOverlay.addEventListener('pointercancel', stopEditorTextDrag, { once: true });
  dragEditorText(event);
}

function dragEditorText(event) {
  if (!editorTextState.dragging) {
    return;
  }
  const stageRect = editorStage.getBoundingClientRect();
  const metrics = editorFrameMetrics();
  if (!metrics.width || !metrics.height) {
    return;
  }
  const anchorX = event.clientX - editorTextState.dragOffsetX;
  const anchorY = event.clientY - editorTextState.dragOffsetY;
  editorTextState.x = Math.max(0, Math.min(100, ((anchorX - stageRect.left - metrics.left) / metrics.width) * 100));
  editorTextState.y = Math.max(0, Math.min(100, ((anchorY - stageRect.top - metrics.top) / metrics.height) * 100));
  updateEditorTextOverlay();
}

function stopEditorTextDrag() {
  editorTextState.dragging = false;
  editorTextOverlay.removeEventListener('pointermove', dragEditorText);
  updateEditorTextOverlay();
}

function startEditorTextResize(event) {
  if (!editorOverlayText.value.trim()) {
    return;
  }
  pushEditorUndo();
  event.preventDefault();
  event.stopPropagation();
  editorTextState.resizing = true;
  editorTextState.resizeStartX = event.clientX;
  editorTextState.resizeStartY = event.clientY;
  editorTextState.resizeStartSize = Math.max(18, Math.min(160, Number(editorTextSize.value || 42)));
  editorTextResize.setPointerCapture(event.pointerId);
  editorTextResize.addEventListener('pointermove', resizeEditorText);
  editorTextResize.addEventListener('pointerup', stopEditorTextResize, { once: true });
  editorTextResize.addEventListener('pointercancel', stopEditorTextResize, { once: true });
  updateEditorTextOverlay();
}

function resizeEditorText(event) {
  if (!editorTextState.resizing) {
    return;
  }
  const delta = Math.max(event.clientX - editorTextState.resizeStartX, event.clientY - editorTextState.resizeStartY);
  const nextSize = Math.max(18, Math.min(160, Math.round(editorTextState.resizeStartSize + delta * 0.45)));
  editorTextSize.value = String(nextSize);
  updateEditorTextOverlay();
}

function stopEditorTextResize() {
  editorTextState.resizing = false;
  editorTextResize.removeEventListener('pointermove', resizeEditorText);
  updateEditorTextOverlay();
}

function drawEditorFrame() {
  updateEditorRotationPreview();
  updateEditorTextOverlay();
}

function toggleEditorPlayback() {
  if (!editorVideo.src) {
    return;
  }
  if (editorVideo.paused) {
    editorVideo.play().catch(() => {});
  } else {
    editorVideo.pause();
  }
}

function updateEditorPlayPause() {
  if (!editorPlayPauseIcon) {
    return;
  }
  editorPlayPauseIcon.textContent = editorVideo.paused ? '▶' : 'Ⅱ';
}

function loadEditorAudioPreview() {
  if (editorAudioObjectUrl) {
    URL.revokeObjectURL(editorAudioObjectUrl);
    editorAudioObjectUrl = '';
  }
  editorAudioPreview.pause();
  editorAudioPreview.removeAttribute('src');
  if (editorMusic.files && editorMusic.files[0]) {
    editorAudioObjectUrl = URL.createObjectURL(editorMusic.files[0]);
    editorAudioPreview.src = editorAudioObjectUrl;
    editorAudioPreview.load();
  }
  updateEditorAudioPreview();
}

function resetEditorAudioPreview() {
  editorAudioPreview.pause();
  editorAudioPreview.removeAttribute('src');
  editorAudioPreview.load();
  editorVideo.muted = false;
  if (editorAudioObjectUrl) {
    URL.revokeObjectURL(editorAudioObjectUrl);
    editorAudioObjectUrl = '';
  }
}

function updateEditorAudioPreview() {
  const shouldUseMusic = editorAudioPreview.src && editorAudioMode.value !== 'keep';
  editorVideo.muted = Boolean(editorMuteOriginal.checked || (shouldUseMusic && editorAudioMode.value === 'replace'));
  if (!shouldUseMusic) {
    editorAudioPreview.pause();
    return;
  }
  syncEditorAudioPreview(true);
  if (!editorVideo.paused) {
    playEditorAudioPreview();
  }
}

function playEditorAudioPreview() {
  if (!editorAudioPreview.src || editorAudioMode.value === 'keep') {
    return;
  }
  syncEditorAudioPreview(true);
  editorAudioPreview.playbackRate = editorVideo.playbackRate || 1;
  editorAudioPreview.play().catch(() => {});
}

function pauseEditorAudioPreview() {
  editorAudioPreview.pause();
}

function syncEditorAudioPreview(force) {
  if (!editorAudioPreview.src || editorAudioMode.value === 'keep') {
    return;
  }
  const audioDuration = Number(editorAudioPreview.duration || 0);
  let target = Number(editorVideo.currentTime || 0);
  if (Number.isFinite(audioDuration) && audioDuration > 0) {
    target = Math.min(target, Math.max(0, audioDuration - 0.05));
  }
  if (force || Math.abs((editorAudioPreview.currentTime || 0) - target) > 0.35) {
    try {
      editorAudioPreview.currentTime = target;
    } catch (error) {
      // Browser may reject seeking before audio metadata is ready.
    }
  }
}

async function submitVideoEdit() {
  if (!currentEditor || !currentEditor.editUrl) {
    return;
  }
  if (editorRenderActive) {
    return;
  }
  if (editorSaveMode.value === 'overwrite') {
    const ok = confirm('Ghi đè dòng hiện tại? File video kết quả hiện tại sẽ bị thay bằng bản chỉnh sửa.');
    if (!ok) {
      return;
    }
  }
  const saveMode = editorSaveMode.value;
  startEditorRenderProgress(saveMode);
  setEditorStatus(saveMode === 'overwrite' ? 'Đang lưu video đã chỉnh sửa...' : 'Đang xuất bản mới...');
  try {
    const data = new FormData();
    appendEditField(data, 'sourceType', editorSourceType.value);
    appendEditField(data, 'saveMode', saveMode);
    appendEditField(data, 'startSeconds', editorStart.value);
    appendEditField(data, 'endSeconds', editorEnd.value);
    appendEditField(data, 'rotationDegrees', editorRotation.value);
    appendEditField(data, 'videoZoom', editorZoom.toFixed(3));
    const outputSize = parseEditorOutputResolution();
    if (outputSize) {
      appendEditField(data, 'outputWidth', outputSize.width);
      appendEditField(data, 'outputHeight', outputSize.height);
    }
    appendEditField(data, 'textPosition', editorTextPosition.value);
    const textPosition = editorTextOutputPosition();
    appendEditField(data, 'textXPercent', textPosition.x.toFixed(2));
    appendEditField(data, 'textYPercent', textPosition.y.toFixed(2));
    appendEditField(data, 'textSize', editorTextSize.value);
    appendEditField(data, 'textColor', editorTextColor.value);
    appendEditField(data, 'textFont', editorTextFont.value);
    appendEditField(data, 'textBackground', editorTextBackground.value);
    appendEditField(data, 'overlayText', editorOverlayText.value.trim());
    appendEditField(data, 'audioMode', editorAudioMode.value);
    appendEditField(data, 'muteOriginalAudio', editorMuteOriginal.checked ? 'true' : 'false');
    appendEditField(data, 'title', editorTitleInput.value.trim());
    setEditorRenderProgress(10, 'Đang dựng text, vị trí, zoom và khung xuất...');
    const textOverlay = await createEditorTextOverlayBlob();
    if (textOverlay) {
      data.append('textOverlay', textOverlay, 'text-overlay.png');
    }
    if (editorSegments.length) {
      data.append('segmentsJson', JSON.stringify(editorSegments.map((segment) => ({
        startSeconds: roundTrimTime(segment.start),
        endSeconds: roundTrimTime(segment.end)
      }))));
    }
    if (editorMusic.files && editorMusic.files[0]) {
      setEditorRenderProgress(14, 'Đang đính kèm file nhạc vào bản render...');
      data.append('music', editorMusic.files[0]);
    }

    setEditorRenderProgress(18, 'Đang gửi dữ liệu và bắt đầu render bằng FFmpeg...');
    const response = await fetch(currentEditor.editUrl, {
      method: 'POST',
      body: data
    });
    const result = await readJsonResponse(response, 'Không xuất được video đã chỉnh sửa.');
    if (!response.ok) {
      throw new Error(result.error || 'Không xuất được video đã chỉnh sửa.');
    }
    setEditorRenderProgress(96, 'Render xong, đang cập nhật danh sách...');
    const progressSetter = currentEditor.kind === 'split' ? splitSetProgress : setProgress;
    progressSetter(100, result.message || 'Đã xuất video đã chỉnh sửa.', 'ok');
    setEditorStatus(result.message || 'Đã xuất video đã chỉnh sửa.', 'ok');
    await loadHistory(1);
    if (currentEditor.kind === 'split') {
      await loadSplitHistory(splitCurrentPage);
    }
    setEditorRenderProgress(100, result.message || 'Đã xuất video đã chỉnh sửa.', 'Hoàn tất');
    await delay(350);
    stopEditorRenderProgress();
    closeEditor();
  } catch (error) {
    stopEditorRenderProgress();
    setEditorStatus(error.message, 'error');
  } finally {
    editorSave.disabled = false;
    editorExportNew.disabled = false;
  }
}

function createEditorTextOverlayBlob() {
  const text = editorOverlayText.value.trim();
  if (!text) {
    return Promise.resolve(null);
  }
  const metrics = editorFrameMetrics();
  const outputSize = parseEditorOutputResolution() || editorNativeSize;
  const scale = metrics.width && outputSize.width ? outputSize.width / metrics.width : 1;
  const fontSize = Math.max(18, Math.min(420, Math.round(Number(editorTextSize.value || 42) * scale)));
  const background = editorTextBackground.value || 'dark';
  const padding = ['dark', 'light', 'highlight'].includes(background) ? Math.round(14 * scale) : 0;
  const lineHeight = Math.ceil(fontSize * 1.15);
  const lines = text.split(/\r?\n/);
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');
  context.font = `900 ${fontSize}px ${editorFontFamily(editorTextFont.value)}`;
  const textWidth = Math.ceil(Math.max(...lines.map((line) => context.measureText(line || ' ').width)));
  const previewBox = editorTextOverlay.getBoundingClientRect();
  const width = Math.ceil(Math.max(textWidth + padding * 2, previewBox.width * scale));
  const textBlockHeight = lines.length * lineHeight;
  const height = Math.ceil(Math.max(textBlockHeight + padding * 2, previewBox.height * scale));
  canvas.width = Math.max(1, width);
  canvas.height = Math.max(1, height);
  context.font = `900 ${fontSize}px ${editorFontFamily(editorTextFont.value)}`;
  context.textBaseline = 'top';
  context.textAlign = editorTextHorizontal.value === 'left' ? 'left' : (editorTextHorizontal.value === 'right' ? 'right' : 'center');
  context.lineJoin = 'round';
  if (background === 'dark' || background === 'light' || background === 'highlight') {
    context.fillStyle = background === 'light' ? 'rgba(255,255,255,.76)' : (background === 'highlight' ? 'rgba(250,204,21,.82)' : 'rgba(0,0,0,.45)');
    roundedRect(context, 0, 0, canvas.width, canvas.height, 8);
    context.fill();
  }
  const horizontal = editorTextHorizontal.value || 'center';
  const vertical = editorTextPosition.value || 'center';
  const x = horizontal === 'left' ? padding : (horizontal === 'right' ? canvas.width - padding : canvas.width / 2);
  let startY = padding;
  if (vertical === 'center') {
    startY = Math.max(padding, (canvas.height - textBlockHeight) / 2);
  } else if (vertical === 'bottom') {
    startY = Math.max(padding, canvas.height - padding - textBlockHeight);
  }
  lines.forEach((line, index) => {
    const y = startY + index * lineHeight;
    if (background === 'outline') {
      context.strokeStyle = 'rgba(0,0,0,.92)';
      context.lineWidth = 6;
      context.strokeText(line, x, y);
    }
    context.fillStyle = editorTextColor.value || '#ffffff';
    context.fillText(line, x, y);
  });
  return new Promise((resolve) => canvas.toBlob(resolve, 'image/png'));
}

function editorFrameMetrics() {
  const stageRect = editorStage.getBoundingClientRect();
  const guideRect = editorFrameGuide && editorFrameGuide.style.display !== 'none'
    ? editorFrameGuide.getBoundingClientRect()
    : stageRect;
  return {
    left: guideRect.left - stageRect.left,
    top: guideRect.top - stageRect.top,
    width: guideRect.width || stageRect.width,
    height: guideRect.height || stageRect.height,
    stageWidth: stageRect.width,
    stageHeight: stageRect.height
  };
}

function editorTextOutputPosition() {
  return {
    x: Math.max(0, Math.min(100, Number(editorTextState.x || 0))),
    y: Math.max(0, Math.min(100, Number(editorTextState.y || 0)))
  };
}

function roundedRect(context, x, y, width, height, radius) {
  const safeRadius = Math.min(radius, width / 2, height / 2);
  context.beginPath();
  context.moveTo(x + safeRadius, y);
  context.lineTo(x + width - safeRadius, y);
  context.quadraticCurveTo(x + width, y, x + width, y + safeRadius);
  context.lineTo(x + width, y + height - safeRadius);
  context.quadraticCurveTo(x + width, y + height, x + width - safeRadius, y + height);
  context.lineTo(x + safeRadius, y + height);
  context.quadraticCurveTo(x, y + height, x, y + height - safeRadius);
  context.lineTo(x, y + safeRadius);
  context.quadraticCurveTo(x, y, x + safeRadius, y);
  context.closePath();
}

function parseEditorOutputResolution() {
  const value = editorOutputResolution.value;
  if (!value) {
    return null;
  }
  const match = value.match(/^(\d+)x(\d+)$/);
  if (!match) {
    return null;
  }
  return {
    width: Number(match[1]),
    height: Number(match[2])
  };
}

function appendEditField(data, key, value) {
  if (value !== undefined && value !== null && String(value).trim() !== '') {
    data.append(key, value);
  }
}

function startEditorRenderProgress(saveMode) {
  const isOverwrite = saveMode === 'overwrite';
  editorRenderActive = true;
  editorRenderProgress = 4;
  editorModal.classList.add('rendering');
  editorRenderOverlay.classList.add('active');
  editorRenderOverlay.setAttribute('aria-hidden', 'false');
  editorClose.disabled = true;
  editorSave.disabled = true;
  editorExportNew.disabled = true;
  editorForm.setAttribute('aria-busy', 'true');
  setEditorRenderProgress(editorRenderProgress, 'Đang gom dữ liệu chỉnh sửa...', isOverwrite ? 'Đang lưu video' : 'Đang xuất bản mới');
  clearInterval(editorRenderTimer);
  editorRenderTimer = setInterval(() => {
    if (!editorRenderActive) {
      return;
    }
    const step = editorRenderProgress < 45 ? 3 : editorRenderProgress < 78 ? 1.6 : editorRenderProgress < 94 ? 0.55 : 0;
    if (step > 0) {
      setEditorRenderProgress(editorRenderProgress + step, 'FFmpeg đang render video, vui lòng chờ...');
    }
  }, 900);
}

function setEditorRenderProgress(value, message, title) {
  editorRenderProgress = Math.max(0, Math.min(100, Math.round(value || 0)));
  if (title) {
    editorRenderTitle.textContent = title;
  }
  editorRenderMessage.textContent = message;
  editorRenderPercent.textContent = `${editorRenderProgress}%`;
  editorRenderFill.style.width = `${editorRenderProgress}%`;
}

function stopEditorRenderProgress() {
  clearInterval(editorRenderTimer);
  editorRenderTimer = null;
  editorRenderActive = false;
  editorModal.classList.remove('rendering');
  editorRenderOverlay.classList.remove('active');
  editorRenderOverlay.setAttribute('aria-hidden', 'true');
  editorForm.removeAttribute('aria-busy');
  editorClose.disabled = false;
}

function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function readJsonResponse(response, fallbackMessage) {
  const text = await response.text();
  let result = {};
  if (text) {
    try {
      result = JSON.parse(text);
    } catch (error) {
      result = { error: text.trim().slice(0, 500) };
    }
  }
  if (!response.ok) {
    throw new Error(result.error || result.message || fallbackMessage);
  }
  return result;
}

function setEditorStatus(message, type = '') {
  editorStatus.className = `editor-status ${type}`;
  editorStatus.textContent = message;
}

function openPreview(url, title) {
  if (previewModalComponent) {
    previewModalComponent.open(url, title);
    return;
  }
  previewTitle.textContent = title || 'Xem trước video';
  previewVideo.src = url;
  previewModal.classList.add('active');
  previewModal.setAttribute('aria-hidden', 'false');
  previewVideo.load();
  previewVideo.play().catch(() => {
    // Browser may block autoplay; controls are visible so the user can press play.
  });
}

function closePreview() {
  if (previewModalComponent) {
    previewModalComponent.close();
    return;
  }
  previewVideo.pause();
  previewVideo.removeAttribute('src');
  previewVideo.load();
  previewModal.classList.remove('active');
  previewModal.setAttribute('aria-hidden', 'true');
}

function statusText(status) {
  if (status === 'ready') return 'Hoàn tất';
  if (status === 'error') return 'Lỗi';
  return 'Đang xử lý';
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('vi-VN');
}

function formatSeconds(value) {
  const number = Number(value || 0);
  if (!Number.isFinite(number) || number <= 0) {
    return '-';
  }
  return `${number.toFixed(1)} giây`;
}

function formatRange(start, duration) {
  const begin = Number(start || 0);
  const length = Number(duration || 0);
  if (!Number.isFinite(begin) || !Number.isFinite(length) || length <= 0) {
    return '-';
  }
  return `${begin.toFixed(1)}s - ${(begin + length).toFixed(1)}s`;
}

function escapeHtml(value) {
  return String(value || '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }[char]));
}

initMenu();
updateSourcePanels();
initUploadEvents();
initSplitUploadEvents();
initFormEvents();
initSplitFormEvents();
initEditorEvents();
initPreviewEvents();
loadHealth();
loadHistory(1);
loadSplitHistory(1);
