(function () {
  const components = window.ToolComponents = window.ToolComponents || {};

  function create({ modal, title, video }) {
    return {
      open(url, displayTitle) {
        title.textContent = displayTitle || 'Xem trước video';
        video.src = url;
        modal.classList.add('active');
        modal.setAttribute('aria-hidden', 'false');
        video.load();
        video.play().catch(() => {
          // Browser may block autoplay; controls are visible so the user can press play.
        });
      },

      close() {
        video.pause();
        video.removeAttribute('src');
        video.load();
        modal.classList.remove('active');
        modal.setAttribute('aria-hidden', 'true');
      }
    };
  }

  components.previewModal = { create };
}());
