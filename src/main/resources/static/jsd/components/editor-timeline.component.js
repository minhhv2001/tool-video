(function () {
  const components = window.ToolComponents = window.ToolComponents || {};

  function createTimelineSegmentButton({ segment, index, duration, active, formatTime }) {
    const startRatio = Math.max(0, Math.min(1, segment.start / duration));
    const endRatio = Math.max(0, Math.min(1, segment.end / duration));
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `trim-segment${active ? ' active' : ''}`;
    button.dataset.index = String(index);
    button.draggable = true;
    button.style.left = `${startRatio * 100}%`;
    button.style.width = `${Math.max(0.6, (endRatio - startRatio) * 100)}%`;
    button.innerHTML = `
      <span class="segment-number">Xuất ${index + 1}</span>
      <span class="segment-time">${formatTime(segment.start)} - ${formatTime(segment.end)}</span>
    `;
    button.title = `Xuất ${index + 1}: ${formatTime(segment.start)} - ${formatTime(segment.end)}`;
    button.setAttribute('aria-label', `Chọn đoạn xuất ${index + 1}`);
    return button;
  }

  function createSegmentOrderButton({ segment, index, active, formatTime }) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `segment-order-item${active ? ' active' : ''}`;
    button.draggable = true;
    button.dataset.index = String(index);
    button.innerHTML = `
      <span class="segment-order-index">Khúc ${index + 1}</span>
      <span class="segment-order-time">${formatTime(segment.start)} - ${formatTime(segment.end)}</span>
    `;
    return button;
  }

  components.editorTimeline = {
    createTimelineSegmentButton,
    createSegmentOrderButton
  };
}());
