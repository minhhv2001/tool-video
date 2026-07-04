(function () {
  const components = window.ToolComponents = window.ToolComponents || {};

  function escapeHtml(value) {
    return String(value || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function thumbStyle(thumbnailUrl) {
    return thumbnailUrl ? ` style="background-image: url('${thumbnailUrl}')"` : '';
  }

  function segmentLabel(segment, fallback) {
    return segment && segment.label ? segment.label : fallback;
  }

  function createTimelineSegmentButton({ segment, index, duration, active, formatTime, thumbnailUrl }) {
    const startRatio = Math.max(0, Math.min(1, segment.start / duration));
    const endRatio = Math.max(0, Math.min(1, segment.end / duration));
    const label = segmentLabel(segment, `Khúc ${index + 1}`);
    const segmentDuration = Math.max(0, segment.end - segment.start);
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `trim-segment${active ? ' active' : ''}`;
    button.dataset.index = String(index);
    button.dataset.segmentId = segment.id || '';
    button.draggable = true;
    button.style.left = `${startRatio * 100}%`;
    button.style.width = `${Math.max(0.6, (endRatio - startRatio) * 100)}%`;
    button.innerHTML = `
      <span class="segment-thumb"${thumbStyle(thumbnailUrl)}></span>
      <span class="segment-shade"></span>
      <span class="segment-number">${escapeHtml(label)}</span>
      <span class="segment-time">${formatTime(segment.start)} - ${formatTime(segment.end)}</span>
      <span class="segment-duration">${formatTime(segmentDuration)}</span>
    `;
    button.title = `${label}: ${formatTime(segment.start)} - ${formatTime(segment.end)} (${formatTime(segmentDuration)})`;
    button.setAttribute('aria-label', `Chọn ${label}`);
    return button;
  }

  function createSegmentOrderButton({ segment, index, active, formatTime, thumbnailUrl }) {
    const label = segmentLabel(segment, `Khúc ${index + 1}`);
    const segmentDuration = Math.max(0, segment.end - segment.start);
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `segment-order-item${active ? ' active' : ''}`;
    button.draggable = true;
    button.dataset.index = String(index);
    button.dataset.segmentId = segment.id || '';
    button.innerHTML = `
      <span class="segment-order-thumb"${thumbStyle(thumbnailUrl)}></span>
      <span class="segment-order-copy">
        <b>Xuất ${index + 1}</b>
        <strong>${escapeHtml(label)}</strong>
        <small>${formatTime(segment.start)} - ${formatTime(segment.end)} | ${formatTime(segmentDuration)}</small>
      </span>
    `;
    button.title = `Kéo ${label} để đổi thứ tự xuất`;
    return button;
  }

  components.editorTimeline = {
    createTimelineSegmentButton,
    createSegmentOrderButton
  };
}());
