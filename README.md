COFFEE SHOP MANAGEMENT SYSTEM - BACKEND
📋 Giới thiệu
Hệ thống quản lý quán cà phê được xây dựng với kiến trúc microservices sử dụng Java Spring Boot. Dự án cung cấp các tính năng quản lý toàn diện cho việc vận hành quán cà phê, từ quản lý sản phẩm, đơn hàng đến thanh toán và báo cáo.
🏗️ Kiến trúc hệ thống
Dự án được thiết kế theo mô hình Microservices Architecture với các service độc lập:

Categories Service: Quản lý danh mục sản phẩm
Products Service: Quản lý sản phẩm và khuyến mãi
Orders Service: Xử lý đơn hàng và trạng thái
Users Service: Quản lý người dùng và phân quyền
Promotions Service: Quản lý chương trình khuyến mãi
Bills Service: Xử lý hóa đơn và thanh toán

⚙️ Công nghệ sử dụng

Framework: Spring Boot
Database: MySQL
ORM: Spring Data JPA/Hibernate
Security: Spring Security + JWT
Real-time Communication: WebSocket
Build Tool: Maven
API Documentation: Swagger/OpenAPI

👥 Phân quyền hệ thống
🔐 Admin

✅ Quản lý sản phẩm (CRUD): Thêm, xóa, sửa sản phẩm, danh mục, kèm ảnh
✅ Quản lý khuyến mãi: Tạo, áp dụng khuyến mãi cho sản phẩm/đơn hàng
✅ Quản lý nhân viên: CRUD thông tin nhân viên (bao gồm ảnh)
✅ Xem báo cáo tổng quan: Doanh thu, đơn hàng, hóa đơn

👨‍💼 Nhân viên

✅ Tìm kiếm, chọn sản phẩm để tạo/sửa đơn hàng
✅ Quản lý bàn: Chọn bàn, cập nhật trạng thái
✅ Xem đơn hàng realtime từ khách hàng qua WebSocket
✅ Xử lý đơn hàng: Xác nhận, chuẩn bị, hoàn thành, thanh toán
✅ Xem/xuất hóa đơn
✅ Lưu thông tin thanh toán (tổng tiền, phương thức, trạng thái), liên kết với đơn hàng

👤 Khách hàng

✅ Chọn sản phẩm từ menu, gắn với bàn
✅ Gửi đơn hàng trực tiếp, tự động hiển thị trên màn hình nhân viên
✅ Đồng bộ đơn hàng từ khách hàng đến nhân viên qua WebSocket

📂 Cấu trúc thư mục
# File Tree: cafe
**Generated:** 11/12/2025, 7:30:03 PM
**Root Path:** `h:\HeThongQuanLyCoffeeShop(Backend)\cafe`

```
├── 📁 .mvn
│   └── 📁 wrapper
│       └── 📄 maven-wrapper.properties
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 example
│   │   │           └── 📁 cafe
│   │   │               ├── 📁 config
│   │   │               │   ├── ☕ MoMoConfig.java
│   │   │               │   └── ☕ VNPayConfig.java
│   │   │               ├── 📁 controllers
│   │   │               │   ├── ☕ AuthController.java
│   │   │               │   ├── ☕ BillController.java
│   │   │               │   ├── ☕ CategoryController.java
│   │   │               │   ├── ☕ MoMoPaymentController.java
│   │   │               │   ├── ☕ OrderController.java
│   │   │               │   ├── ☕ OrderItemController.java
│   │   │               │   ├── ☕ PaymentController.java
│   │   │               │   ├── ☕ ProductController.java
│   │   │               │   ├── ☕ PromotionController.java
│   │   │               │   ├── ☕ TableController.java
│   │   │               │   └── ☕ UserController.java
│   │   │               ├── 📁 dto
│   │   │               │   ├── ☕ BillDTO.java
│   │   │               │   ├── ☕ LoginDto.java
│   │   │               │   ├── ☕ MoMoIPNRequest.java
│   │   │               │   ├── ☕ MoMoPaymentRequest.java
│   │   │               │   ├── ☕ MoMoPaymentResponse.java
│   │   │               │   ├── ☕ OrderItemDTO.java
│   │   │               │   ├── ☕ PaymentApiResponse.java
│   │   │               │   ├── ☕ PaymentRequest.java
│   │   │               │   └── ☕ PaymentResponse.java
│   │   │               ├── 📁 entity
│   │   │               │   ├── 📁 enums
│   │   │               │   │   ├── ☕ OrderStatus.java
│   │   │               │   │   ├── ☕ PaymentMethod.java
│   │   │               │   │   ├── ☕ PaymentStatus.java
│   │   │               │   │   ├── ☕ Role.java
│   │   │               │   │   └── ☕ Status.java
│   │   │               │   ├── ☕ Bill.java
│   │   │               │   ├── ☕ Category.java
│   │   │               │   ├── ☕ Order.java
│   │   │               │   ├── ☕ OrderItem.java
│   │   │               │   ├── ☕ Product.java
│   │   │               │   ├── ☕ Promotion.java
│   │   │               │   ├── ☕ TableEntity.java
│   │   │               │   └── ☕ User.java
│   │   │               ├── 📁 repository
│   │   │               │   ├── ☕ BillRepository.java
│   │   │               │   ├── ☕ CategoryRepository.java
│   │   │               │   ├── ☕ OrderItemRepository.java
│   │   │               │   ├── ☕ OrderRepository.java
│   │   │               │   ├── ☕ ProductRepository.java
│   │   │               │   ├── ☕ PromotionRepository.java
│   │   │               │   ├── ☕ TableRepository.java
│   │   │               │   └── ☕ UserRepository.java
│   │   │               ├── 📁 scheduler
│   │   │               │   └── ☕ OrderStatusScheduler.java
│   │   │               ├── 📁 security
│   │   │               │   ├── 📁 jwt
│   │   │               │   │   ├── ☕ JwtAuthenticationFilter.java
│   │   │               │   │   └── ☕ JwtFilter.java
│   │   │               │   ├── 📁 services
│   │   │               │   │   ├── 📁 impl
│   │   │               │   │   │   ├── ☕ BillServiceImpl.java
│   │   │               │   │   │   ├── ☕ CategoryServiceImpl.java
│   │   │               │   │   │   ├── ☕ OrderItemServiceImpl.java
│   │   │               │   │   │   ├── ☕ OrderServiceImpl.java
│   │   │               │   │   │   ├── ☕ ProductServiceImpl.java
│   │   │               │   │   │   ├── ☕ PromotionServiceImpl.java
│   │   │               │   │   │   ├── ☕ TableServiceImpl.java
│   │   │               │   │   │   └── ☕ UserServiceImpl.java
│   │   │               │   │   ├── ☕ BillService.java
│   │   │               │   │   ├── ☕ CategoryService.java
│   │   │               │   │   ├── ☕ CustomUserDetailsService.java
│   │   │               │   │   ├── ☕ JwtService.java
│   │   │               │   │   ├── ☕ OrderItemService.java
│   │   │               │   │   ├── ☕ OrderService.java
│   │   │               │   │   ├── ☕ ProductService.java
│   │   │               │   │   ├── ☕ PromotionService.java
│   │   │               │   │   ├── ☕ TableService.java
│   │   │               │   │   └── ☕ UserService.java
│   │   │               │   └── ☕ SecurityConfig.java
│   │   │               ├── 📁 services
│   │   │               │   ├── ☕ MoMoService.java
│   │   │               │   └── ☕ VNPayService.java
│   │   │               └── ☕ CafeApplication.java
│   │   └── 📁 resources
│   │       ├── 📁 static
│   │       ├── 📁 templates
│   │       └── 📄 application.properties
│   └── 📁 test
│       └── 📁 java
│           └── 📁 com
│               └── 📁 example
│                   └── 📁 cafe
│                       └── ☕ CafeApplicationTests.java
├── 📁 uploads
│   └── 📁 images
│       ├── 🖼️ 03feaf05-c980-4b5e-8d95-99fbcb1fb1e4.png
│       ├── 🖼️ 043b7bc0-8f04-41af-aa68-6e20a5a9972c.png
│       ├── 🖼️ 06a0b0bc-47a6-4528-b712-5148cfc8a3c4.jpg
│       ├── 🖼️ 0800419c-152d-43ac-aceb-cf50260c70bf.webp
│       ├── 🖼️ 0b5e642c-df2a-40b8-8f23-f239e494d360.jpg
│       ├── 🖼️ 0c902e8e-155e-44b7-a3e7-3938b7567265.jpg
│       ├── 🖼️ 0d46bbd6-980e-4f18-b081-7aa36aca9ab9.jpg
│       ├── 🖼️ 21d9a172-0d48-4e4f-8016-c02d2e1ea23f.jpg
│       ├── 🖼️ 221dcc5e-4ef1-4488-bc72-8195fc1c8235.jpg
│       ├── 🖼️ 22476fc4-0bcf-43ce-aa25-5d080a4461d1.jpg
│       ├── 🖼️ 2bf32534-83c7-420e-964a-29e3e9b88e87.jpg
│       ├── 🖼️ 303547a3-a712-46b7-ab24-88550a45d36b.webp
│       ├── 🖼️ 30d9b8cf-f632-4612-8347-9be00b629595.jpg
│       ├── 🖼️ 32a2fa05-9e37-4eea-abb8-43363b2ead9b.jpg
│       ├── 🖼️ 34f35764-21b7-4ab1-af9d-383c5744038d.webp
│       ├── 🖼️ 3b56d2dc-3bc0-47f7-871c-6f48656cdc22.jpg
│       ├── 🖼️ 3be5140b-312e-46d9-bd93-4c858458a3e1.jpg
│       ├── 🖼️ 3c17358b-8c88-4556-9555-223850e9c8e5.jpg
│       ├── 🖼️ 40981077-0b72-4491-94be-6dc5df46fd64.jpg
│       ├── 🖼️ 41fd3e10-7ff1-4bb5-83fa-3781c052fd55.webp
│       ├── 🖼️ 43d6e9c4-ee2a-4fd4-9ab4-eacd96b2de2b.webp
│       ├── 🖼️ 459426fb-3042-46e2-8e5a-f53323ca6816.jpg
│       ├── 🖼️ 48f696a3-c1e1-4449-b424-7e2a0b935784.jpg
│       ├── 🖼️ 4a3b9112-1616-41ef-b244-d93d951c2fd2.jpg
│       ├── 🖼️ 4a8ec108-5bbe-4393-b87e-3ed0e795713a.webp
│       ├── 🖼️ 56b0539b-edbe-4b7f-a4e7-45354771eb75.jpg
│       ├── 🖼️ 5817c56f-08b2-4cf3-bd60-6e6c37e9369f.png
│       ├── 🖼️ 58770157-f82d-42c0-af30-6b9ab7bf1907.jpg
│       ├── 🖼️ 5b901edb-3951-4d06-be20-74585a6e2e2e.png
│       ├── 🖼️ 5bac1d45-b4bd-4194-9263-2b27fe6b8fe5.png
│       ├── 🖼️ 607cc6ba-10b7-46dd-8f70-0c456d662e0d.jpg
│       ├── 🖼️ 6977941a-ccac-482d-b2b8-8c7dc2ce9481.jpg
│       ├── 🖼️ 6d2fb178-d00c-4bfb-a9e4-d6eb05a09520.jpg
│       ├── 🖼️ 760c0e7f-7e19-45de-84b0-10292de9d789.jpg
│       ├── 🖼️ 789eae10-2ec2-460c-86d3-a9fa5faf996d.jpg
│       ├── 🖼️ 7e995cdc-c17e-45d7-a5a9-051fb71c6da1.png
│       ├── 🖼️ 8926fdb9-22b9-458d-913f-cc9113102974.jpg
│       ├── 🖼️ 8c3ecca7-22c5-41d2-bce9-eae5e2bf1d70.jpg
│       ├── 🖼️ 8e4054f4-00ac-4e24-b1e2-033d2fb65412.webp
│       ├── 🖼️ 976765c7-750c-4905-86ae-5277c081a604.jpg
│       ├── 🖼️ 9b3eb447-4dbb-4572-8f63-c9cfe4f9fe82.webp
│       ├── 🖼️ 9be7dd70-37e5-4c21-a2e7-c0b06b1c9a49.jpg
│       ├── 🖼️ 9cde48e5-d36c-45e2-b535-88392ad2ad38.webp
│       ├── 🖼️ aa4feca1-564e-4352-8b98-35774f7f2e30.jpg
│       ├── 🖼️ ab643751-26a7-486f-9b14-166e0b0f6ea5.png
│       ├── 🖼️ ad56a09e-19da-405f-848b-e08dcf469d88.jpg
│       ├── 🖼️ af66982c-3941-45d3-a7fd-b7151c716259.jpg
│       ├── 🖼️ b02c012b-4cd1-4c99-b952-d80edd142f06.webp
│       ├── 🖼️ b4754aef-6b91-4a55-ac99-a441af4f763e.png
│       ├── 🖼️ b4dc759c-e175-47b2-87d9-2ae48f8c6a32.jpg
│       ├── 🖼️ c481f2e8-54d7-49d1-825a-6672f3846bd7.jpg
│       ├── 🖼️ c7506314-eb88-4d58-a36a-de4d39057699.jpg
│       ├── 🖼️ c8cb908d-754b-4449-a398-1e1f756df302.png
│       ├── 🖼️ d1314cc3-1a90-4a81-8254-ab77ae377a67.jpg
│       ├── 🖼️ d3d54e10-4f32-488e-859c-d9e2fd1e3cd5.jpg
│       ├── 🖼️ d3ec1a6f-c094-4e39-b8b6-56c6b55d3fd9.webp
│       ├── 🖼️ d47e9879-8c95-4c77-9cc6-56eaffd6fea1.png
│       ├── 🖼️ dc73dee3-c8d3-440c-be8a-f663e3251d7f.png
│       ├── 🖼️ dc9d1f3f-f7fa-444a-9635-1c9296c018ec.png
│       ├── 🖼️ df57b44d-9f32-46e8-a70a-a6a4d418b3dd.jpg
│       ├── 🖼️ e05c430c-6111-4903-972b-e275e8c1262b.jpg
│       ├── 🖼️ e1bb5206-6445-45f6-80b1-3246e6a68148.png
│       ├── 🖼️ e6dbb7a3-8693-4b6b-8e43-0c09fe87dc41.jpg
│       ├── 🖼️ e9baf228-06ee-453d-8855-8c3f0d2e84dc.jpg
│       ├── 🖼️ ec6005d1-b660-4822-acc4-9b0fe3ebfced.jpg
│       ├── 🖼️ ef3f8b1a-5179-4ece-bccb-5baf7a96b04a.jpg
│       ├── 🖼️ f1eb8801-eeda-4890-b1fb-b1aca42de8d8.jpg
│       ├── 🖼️ f36e7f15-a72d-4f2f-85f8-d0cba151da41.jpg
│       ├── 🖼️ f634f538-ce99-4306-a3e0-a436586589f6.jpg
│       ├── 🖼️ f645814a-3fa3-40a3-8382-e2afcb395f0e.png
│       ├── 🖼️ f6e973f4-036b-4c3d-93ff-e3d6526eac8c.jpg
│       ├── 🖼️ fc029d1a-547e-47ad-9016-8c07a5768891.jpg
│       ├── 🖼️ fe301c3a-ed45-4c72-9954-c57cd0ecd7fd.png
│       └── 🖼️ ffa15bd0-e125-418a-80f2-4e3d46c9fd1a.jpg
├── ⚙️ .gitattributes
├── ⚙️ .gitignore
├── 📝 README.md
├── 📄 mvnw
├── 📄 mvnw.cmd
└── ⚙️ pom.xml
```

---
*Generated by FileTree Pro Extension*

---

Hệ thống sử dụng các bảng chính:

categories: Danh mục sản phẩm
products: Thông tin sản phẩm
orders: Đơn hàng
order_items: Chi tiết đơn hàng
users: Người dùng (Admin, Nhân viên, Khách hàng)
bills: Hóa đơn thanh toán
promotions: Chương trình khuyến mãi
promotion_products: Liên kết khuyến mãi với sản phẩm
tables: Bàn trong quán

🚀 Cài đặt và chạy dự án
Yêu cầu hệ thống

Java 17 hoặc cao hơn
Maven 3.8+
MySQL 8.0+ hoặc PostgreSQL 13+
IDE: IntelliJ IDEA, Eclipse, hoặc VS Code

Các bước cài đặt

Clone repository

bashgit clone <repository-url>
cd CAFE

Cấu hình database

Tạo database mới và cập nhật file application.properties:
propertiesspring.datasource.url=jdbc:mysql://localhost:3306/cafe_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

Build project

bash./mvnw clean install

Chạy ứng dụng

bash./mvnw spring-boot:run
```

Hoặc sử dụng IDE để run `CafeApplication.java`

5. **Truy cập ứng dụng**
- API Base URL: `http://localhost:8080`

## 📡 API Endpoints

### Authentication
```
POST /api/auth/login       - Đăng nhập
POST /api/auth/register    - Đăng ký
POST /api/auth/refresh     - Làm mới token
```

### Products
```
GET    /api/products              - Lấy danh sách sản phẩm
GET    /api/products/{id}         - Chi tiết sản phẩm
POST   /api/products              - Tạo sản phẩm (Admin)
PUT    /api/products/{id}         - Cập nhật sản phẩm (Admin)
DELETE /api/products/{id}         - Xóa sản phẩm (Admin)
```

### Orders
```
GET    /api/orders                - Danh sách đơn hàng
GET    /api/orders/{id}           - Chi tiết đơn hàng
POST   /api/orders                - Tạo đơn hàng
PUT    /api/orders/{id}/status    - Cập nhật trạng thái
DELETE /api/orders/{id}           - Hủy đơn hàng
```

### Bills
```
GET    /api/bills                 - Danh sách hóa đơn
GET    /api/bills/{id}            - Chi tiết hóa đơn
POST   /api/bills                 - Tạo hóa đơn
PUT    /api/bills/{id}/payment    - Thanh toán
🔌 WebSocket Integration
Hệ thống sử dụng WebSocket để cập nhật đơn hàng realtime:
Connect endpoint: /ws
Subscribe topics:

/topic/orders - Nhận thông báo đơn hàng mới
/topic/orders/{orderId} - Theo dõi trạng thái đơn hàng cụ thể

🔒 Security

JWT Authentication: Token-based authentication với access token và refresh token
Role-based Access Control: Phân quyền theo vai trò (Admin, Nhân viên, Khách hàng)
Password Encryption: Mã hóa mật khẩu sử dụng BCrypt
CORS Configuration: Cấu hình CORS cho phép truy cập từ frontend

📊 Features
Quản lý sản phẩm

CRUD sản phẩm với hình ảnh
Phân loại theo danh mục
Quản lý giá và tồn kho

Quản lý đơn hàng

Tạo đơn hàng từ menu
Cập nhật trạng thái realtime
Gán đơn hàng với bàn
Xác nhận và xử lý đơn

Quản lý thanh toán

Tạo hóa đơn tự động
Nhiều phương thức thanh toán
Lưu lịch sử giao dịch

Báo cáo

Doanh thu theo thời gian
Thống kê đơn hàng
Top sản phẩm bán chạy

🧪 Testing
Chạy unit tests:
bash./mvnw test
📝 Environment Variables
Tạo file .env hoặc cấu hình trong application.properties:
properties# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cafe_db
DB_USER=root
DB_PASSWORD=password

# JWT
JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000

# Upload
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10MB
🤝 Contributing

Fork project
Tạo branch mới (git checkout -b feature/AmazingFeature)
Commit changes (git commit -m 'Add some AmazingFeature')
Push to branch (git push origin feature/AmazingFeature)
Tạo Pull Request

📄 License
Dự án được phân phối dưới giấy phép MIT. Xem file LICENSE để biết thêm chi tiết.
👨‍💻 Contact
---Hoàng Đạt---
Email: dat147714@gmail.com

🙏 Acknowledgments

Spring Boot Documentation
Spring Security
WebSocket Protocol
JWT Implementation


Made with ☕ and ❤️