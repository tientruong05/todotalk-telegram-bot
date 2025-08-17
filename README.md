# TodoTalk Bot

Telegram bot quản lý công việc cá nhân hiệu quả với tính năng thông minh.

## 📋 Dự án là gì?

TodoTalk Bot là một trợ lý ảo trên Telegram giúp bạn quản lý công việc hàng ngày một cách thông minh và dễ dàng. Bot được phát triển bằng **Spring Boot 3** + **Java 21**.

## 🌟 Tính năng chính

- **Tạo task thông minh**: Chỉ cần gõ `/addtask` kèm mô tả, bot sẽ tự động nhận diện thời gian deadline (tiếng Việt) và danh sách subtasks
- **Quản lý subtasks**: Chia nhỏ công việc thành các subtask để theo dõi tiến độ chi tiết
- **Đánh dấu hoàn thành**: Sử dụng `/donetask` để đánh dấu hoàn thành từng subtask hoặc cả task
- **Hoàn tác linh hoạt**: `/undotask` để hoàn tác khi đánh dấu nhầm
- **Theo dõi tiến độ**: Xem thanh tiến độ trực quan với emoji và phần trăm hoàn thành
- **Thông báo thông minh**: Tự động nhắc nhở khi task quá hạn
- **Cá nhân hóa**: Gọi tên bằng @username để tăng tính thân thiện

## 💡 Cách sử dụng đơn giản

### Tạo task:

```
/addtask Hoàn thành báo cáo vào lúc 17h:
- Thu thập dữ liệu
- Phân tích kết quả
- Viết báo cáo
- Review và gửi
```

### Hoàn thành từng bước:

```
/donetask thu thập dữ liệu
/donetask phân tích kết quả
```

### Xem tiến độ:

```
/progress
📊 Tiến độ công việc của bạn, @username:
�🟩🟩🟩🟩⬜⬜⬜⬜⬜ 50%
```

## 🎯 Điểm đặc biệt

- **Thông minh**: Tự động nhận diện thời gian bằng tiếng Việt ("vào lúc 17h", "đến 6h")
- **Linh hoạt**: Hỗ trợ nhiều định dạng danh sách (-, •, \*, +, 1., 2.)
- **Thân thiện**: Gọi tên người dùng bằng @username
- **Tự động**: Nhắc nhở khi quá deadline
- **Trực quan**: Thanh tiến độ bằng emoji dễ nhìn

## � Danh sách lệnh

| Lệnh           | Mô tả                   |
| -------------- | ----------------------- |
| `/addtask`     | Tạo task mới            |
| `/donetask`    | Hoàn thành subtask/task |
| `/undotask`    | Hoàn tác                |
| `/donealltask` | Hoàn thành tất cả       |
| `/progress`    | Xem tiến độ             |
| `/listtasks`   | Danh sách task          |
| `/cleartasks`  | Xóa tất cả              |

---

**TodoTalk Bot** - Trợ lý quản lý công việc thông minh trên Telegram ✅
