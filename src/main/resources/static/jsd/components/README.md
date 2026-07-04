# Frontend components

Frontend hiện tại vẫn là Spring MVC static HTML/CSS/JS, chưa dùng React build tool.
Thư mục này tách UI theo kiểu component để dễ tái sử dụng dần:

- `history-list.component.js`: render bảng danh sách Video Cắt ghép, Video Tách, empty row và phân trang.
- `editor-timeline.component.js`: render các khúc timeline và danh sách thứ tự xuất trong modal chỉnh sửa video.
- `preview-modal.component.js`: controller đóng/mở modal xem trước video.

Quy ước:

- Component chỉ dựng UI/DOM.
- `app.js` giữ phần gọi API, state chính, bind action và điều phối luồng.
- Khi thêm màn mới, ưu tiên tạo component mới ở đây rồi gọi từ `app.js`, tránh nhét HTML dài trực tiếp trong `app.js`.
