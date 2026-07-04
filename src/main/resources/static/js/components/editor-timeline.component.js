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

  function segmentLabel(segment, fallback) {
    return segment && segment.label ? segment.label : fallback;
  }

  function createTimelineSegmentButton({ segment, index, outputStart, outputDuration, totalDuration, active, formatTime }) {
    const startRatio = Math.max(0, Math.min(1, outputStart / totalDuration));
    const endRatio = Math.max(0, Math.min(1, (outputStart + outputDuration) / totalDuration));
    const label = segmentLabel(segment, `Khúc ${index + 1}`);
    const segmentDuration = Math.max(0, segment.end - segment.start);
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `trim-segment${active ? ' active' : ''}`;
    button.dataset.index = String(index);
    button.dataset.segmentId = segment.id || '';
    button.dataset.outputStart = String(outputStart);
    button.dataset.outputEnd = String(outputStart + outputDuration);
    button.draggable = true;
    button.style.left = `${startRatio * 100}%`;
    button.style.width = `${Math.max(0.6, (endRatio - startRatio) * 100)}%`;
    button.innerHTML = `
      <span class="segment-shade"></span>
      <span class="segment-number">${escapeHtml(label)}</span>
      <span class="segment-time">Xuất ${index + 1} | Gốc ${formatTime(segment.start)} - ${formatTime(segment.end)}</span>
      <span class="segment-duration">${formatTime(segmentDuration)}</span>
    `;
    button.title = `${label}: ${formatTime(segment.start)} - ${formatTime(segment.end)} (${formatTime(segmentDuration)}). Kéo trực tiếp trên timeline để đổi thứ tự xuất.`;
    button.setAttribute('aria-label', `Chọn ${label}`);
    return button;
  }

  components.editorTimeline = {
    createTimelineSegmentButton
  };
}());
