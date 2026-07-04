(function () {
  const components = window.ToolComponents = window.ToolComponents || {};

  function renderEmptyRow(message, colspan) {
    const row = document.createElement('tr');
    row.innerHTML = `<td colspan="${colspan}">${message}</td>`;
    return row;
  }

  function renderHighlightRow(item, helpers) {
    const tr = document.createElement('tr');
    const files = (item.inputFileNames || []).join(', ');
    const jobId = encodeURIComponent(item.jobId);
    const previewUrl = item.downloadUrl ? `/api/highlights/${jobId}/preview` : '';
    const sourceLocationUrl = `/api/highlights/${jobId}/open-location?target=source`;
    const outputLocationUrl = `/api/highlights/${jobId}/open-location?target=output`;
    const title = files || 'Video highlight';

    tr.innerHTML = `
      <td class="action-cell">
        <div class="row-actions">
          ${previewUrl ? `<button class="preview-row btn btn-sm btn-outline-info" type="button" data-preview-url="${previewUrl}" data-preview-title="${helpers.escapeHtml(title)}" title="Xem trước">Xem</button>` : ''}
          ${item.downloadUrl ? `<a class="download btn btn-sm btn-success" href="${item.downloadUrl}" title="Tải xuống">Tải</a>` : ''}
          ${previewUrl ? `<button class="edit-row btn btn-sm btn-outline-primary" type="button" data-edit-url="/api/highlights/${jobId}/edit" data-preview-url="${previewUrl}" data-edit-title="${helpers.escapeHtml(title)}" title="Chỉnh sửa">Sửa</button>` : ''}
          <button class="delete-row btn btn-sm btn-outline-danger" type="button" data-job-id="${helpers.escapeHtml(item.jobId)}">Xóa</button>
        </div>
      </td>
      <td class="select-cell"><input class="history-check" type="checkbox" data-job-id="${helpers.escapeHtml(item.jobId)}" aria-label="Chọn job này"></td>
      <td class="time-cell">${helpers.formatDate(item.createdAt)}</td>
      <td class="file-cell">${helpers.renderCompactText(files)}</td>
      <td class="folder-cell"><button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${sourceLocationUrl}" title="Mở thư mục video gốc">${helpers.folderIcon()}<span>Gốc</span></button></td>
      <td class="note-cell">${helpers.renderCompactText(item.cutNote || '-')}</td>
      <td class="status-cell"><span class="status ${item.status}">${helpers.statusText(item.status)}</span></td>
      <td class="clips-cell">${item.clipsUsed || '-'}</td>
      <td class="duration-cell">${item.totalDurationSeconds ? `${Number(item.totalDurationSeconds).toFixed(1)} giây` : '-'}</td>
      <td class="folder-cell">${item.downloadUrl ? `<button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${outputLocationUrl}" title="Mở thư mục video đã cắt ghép">${helpers.folderIcon()}<span>Kết quả</span></button>` : '-'}</td>
    `;
    return tr;
  }

  function renderSplitRow(item, helpers) {
    const tr = document.createElement('tr');
    const jobId = encodeURIComponent(item.jobId);
    const clipIndex = encodeURIComponent(item.clipIndex);
    const previewUrl = item.downloadUrl ? `/api/split-highlights/${jobId}/clips/${clipIndex}/preview` : '';
    const previewName = `${item.outputFileName || `clip-${item.clipIndex}`} - ${item.originalFileName || 'Video gốc'}`;
    const sourceLocationUrl = `/api/split-highlights/${jobId}/clips/${clipIndex}/open-location?target=source`;
    const outputLocationUrl = `/api/split-highlights/${jobId}/clips/${clipIndex}/open-location?target=output`;

    tr.innerHTML = `
      <td class="action-cell">
        <div class="row-actions">
          ${previewUrl ? `<button class="preview-row btn btn-sm btn-outline-info" type="button" data-preview-url="${previewUrl}" data-preview-title="${helpers.escapeHtml(previewName)}" title="Xem trước">Xem</button>` : ''}
          ${item.downloadUrl ? `<a class="download btn btn-sm btn-success" href="${item.downloadUrl}" title="Tải xuống">Tải</a>` : ''}
          ${previewUrl ? `<button class="edit-row btn btn-sm btn-outline-primary" type="button" data-edit-url="/api/split-highlights/${jobId}/clips/${clipIndex}/edit" data-preview-url="${previewUrl}" data-edit-title="${helpers.escapeHtml(previewName)}" title="Chỉnh sửa">Sửa</button>` : ''}
          <button class="delete-row split-delete-row btn btn-sm btn-outline-danger" type="button" data-job-id="${helpers.escapeHtml(item.jobId)}" data-clip-index="${helpers.escapeHtml(item.clipIndex)}">Xóa</button>
        </div>
      </td>
      <td class="select-cell"><input class="split-history-check" type="checkbox" data-job-id="${helpers.escapeHtml(item.jobId)}" data-clip-index="${helpers.escapeHtml(item.clipIndex)}" aria-label="Chọn clip này"></td>
      <td class="time-cell">${helpers.formatDate(item.createdAt)}</td>
      <td class="file-cell">${helpers.renderCompactText(item.originalFileName || '-')}</td>
      <td class="folder-cell"><button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${sourceLocationUrl}" title="Mở thư mục video gốc">${helpers.folderIcon()}<span>Gốc</span></button></td>
      <td class="file-cell clip-name-cell">${helpers.renderCompactText(item.outputFileName || `clip-${item.clipIndex}`)}</td>
      <td class="folder-cell">${item.downloadUrl ? `<button class="open-location-row folder-action btn btn-sm btn-outline-success" type="button" data-open-url="${outputLocationUrl}" title="Mở thư mục clip đã tách">${helpers.folderIcon()}<span>Clip</span></button>` : '-'}</td>
      <td class="range-cell">${helpers.formatRange(item.startSeconds, item.durationSeconds)}</td>
      <td class="duration-cell">${helpers.formatSeconds(item.durationSeconds)}</td>
      <td class="status-cell"><span class="status ${item.status}">${helpers.statusText(item.status)}</span></td>
    `;
    return tr;
  }

  function renderPager(container, page, totalPages, onPage) {
    container.innerHTML = '';
    if (!totalPages || totalPages <= 1) {
      return;
    }
    addPageButton(container, 'Trước', Math.max(1, page - 1), page === 1, false, onPage);
    for (let i = 1; i <= totalPages; i += 1) {
      addPageButton(container, String(i), i, false, i === page, onPage);
    }
    addPageButton(container, 'Next', Math.min(totalPages, page + 1), page === totalPages, false, onPage);
  }

  function addPageButton(container, label, page, disabled, active, onPage) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `page-btn btn btn-sm ${active ? 'active btn-primary' : 'btn-light'}`;
    button.textContent = label;
    button.disabled = disabled;
    button.addEventListener('click', () => onPage(page));
    container.appendChild(button);
  }

  components.historyList = {
    renderEmptyRow,
    renderHighlightRow,
    renderSplitRow,
    renderPager
  };
}());
