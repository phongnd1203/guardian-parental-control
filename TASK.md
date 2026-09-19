# Kế hoạch & Danh sách Task Triển khai: Guardian — Family Module

Tài liệu này chi tiết hóa toàn bộ các hạng mục triển khai cho **Family Module (v1.0)** dựa trên đặc tả kỹ thuật tại [REQUREMENTS.md](file:///Users/phong/AndroidStudioProjects/GuardianAndroid/REQUREMENTS.md).

---

## Bảng trạng thái tổng quan

- **Tiến độ tổng thể:** 40% hoàn thành (Hoàn thành 100% Giai đoạn 1 & Giai đoạn 2)
- **Quy ước trạng thái:**
  - `[ ]` Chưa bắt đầu (Pending)
  - `[/]` Đang thực hiện (In Progress)
  - `[x]` Đã hoàn thành (Done)
  - `[!]` Bị chặn / Cần xem xét (Blocked/Review)

---

## Giai đoạn 1: Cơ sở dữ liệu & Backend (Supabase)

### 1.1. Thiết kế Schema & Migration Database
- [x] **Tạo DDL bảng `families`**:
  - `id` (UUID PK), `name` (TEXT NOT NULL, 2-50 chars), `owner_user_id` (UUID NOT NULL FK `auth.users`), `created_at`, `updated_at`, `deleted_at`.
  - Trigger cập nhật `updated_at`.
  - Index trên `owner_user_id`.
- [x] **Tạo DDL bảng `family_members`**:
  - `id` (UUID PK), `family_id` (UUID FK `families`), `user_id` (UUID FK `auth.users`), `role` (`OWNER`, `PARENT`, `VIEWER`), `joined_at`, `created_at`.
  - Ràng buộc duy nhất `UNIQUE(family_id, user_id)`.
  - Indexes: `family_id`, `user_id`, `(family_id, role)`.
- [x] **Tạo DDL bảng `children`**:
  - `id` (UUID PK), `family_id` (UUID FK `families`), `name` (TEXT 1-50 chars), `nickname` (TEXT nullable), `date_of_birth` (DATE nullable), `avatar_path` (TEXT nullable), `status` (`ACTIVE`, `ARCHIVED`), `created_by`, `created_at`, `updated_at`, `archived_at`, `deleted_at`.
  - Index: `family_id`, `(family_id, status)`.
- [x] **Tạo DDL bảng `devices`**:
  - `id` (UUID PK), `family_id` (UUID FK `families`), `child_id` (UUID FK `children`), `auth_user_id` (UUID FK `auth.users`), `name`, `manufacturer`, `model`, `android_version`, `api_level`, `app_version`, `app_build`, `battery_level`, `charging`, `protection_status`, `last_seen_at`, `paired_at`, `unpaired_at`, `created_at`, `updated_at`.
  - Indexes: `child_id`, `family_id`, `auth_user_id`, `last_seen_at`.
- [x] **Tạo DDL bảng `device_permission_status`**:
  - `device_id` (UUID PK FK `devices`), `usage_access` (BOOL), `accessibility_service` (BOOL), `notification_permission` (BOOL), `vpn_active` (BOOL), `location_permission` (BOOL), `device_admin` (BOOL), `updated_at`.
- [x] **Tạo DDL bảng `family_invitations`**:
  - `id` (UUID PK), `family_id` (UUID FK `families`), `email` (TEXT NOT NULL), `role` (`PARENT`, `VIEWER`), `token_hash` (TEXT NOT NULL), `status` (`PENDING`, `ACCEPTED`, `EXPIRED`, `CANCELLED`), `expires_at`, `invited_by`, `created_at`, `accepted_by`, `accepted_at`, `cancelled_at`.
  - Ràng buộc Partial Unique Index: `UNIQUE(family_id, lower(email))` với điều kiện `status = 'PENDING'`.
- [x] **Tạo DDL bảng `device_pairing_sessions`**:
  - `id` (UUID PK), `family_id` (UUID FK), `child_id` (UUID FK), `token_hash` (TEXT NOT NULL), `manual_code_hash` (TEXT NOT NULL), `created_by`, `expires_at` (10 phút), `claimed_at`, `claimed_device_id`, `cancelled_at`, `created_at`.
- [x] **Tạo DDL bảng `family_audit_logs`**:
  - `id` (UUID PK), `family_id` (UUID FK), `actor_type` (`USER`, `SYSTEM`, `DEVICE`), `actor_id` (UUID), `action` (TEXT), `entity_type` (TEXT), `entity_id` (UUID), `metadata` (JSONB), `created_at`.
  - Chặn sửa/xóa bảng audit (Immutable).

### 1.2. Row Level Security (RLS) & Helper Functions
- [x] **Viết Function kiểm tra quyền**:
  - `is_family_member(f_id UUID)`: kiểm tra `auth.uid()` có nằm trong `family_members` của family không.
  - `has_family_role(f_id UUID, allowed_roles TEXT[])`: kiểm tra role của user trong family.
  - Helper xác thực Parent Auth: `(auth.jwt()->>'is_anonymous')::boolean = false`.
  - Helper xác thực Child Device Auth: `(auth.jwt()->>'is_anonymous')::boolean = true` và mapping đúng `devices.auth_user_id`.
- [x] **Thiết lập RLS Policies**:
  - Bảng `families`: Chỉ member mới đọc được; chỉ Owner mới có thể update/delete.
  - Bảng `family_members`: Member được xem danh sách thành viên trong gia đình; Owner mới được đổi role hoặc xóa member.
  - Bảng `children`: Member (Owner/Parent/Viewer) được xem; Owner/Parent được insert/update; không cho Child Device xem.
  - Bảng `devices`: Parent đọc toàn bộ device trong gia đình; Child device chỉ đọc và update device của chính nó (`auth.uid() = devices.auth_user_id`).
  - Bảng `device_permission_status`: Parent đọc; Device update bản ghi của chính nó.
  - Bảng `family_invitations`: Owner/Parent đọc & quản lý; Viewer không có quyền.
  - Bảng `device_pairing_sessions`: Chỉ Owner/Parent quản lý.
  - Bảng `family_audit_logs`: Chỉ member được SELECT; cấm INSERT/UPDATE/DELETE từ client trực tiếp.

### 1.3. Supabase Storage Bucket Setup
- [x] Tạo private bucket `family-avatars`.
- [x] Thiết lập Storage RLS Policies theo path:
  - `family-avatars/{familyId}/children/{childId}/avatar.webp`
  - Đảm bảo chỉ user thuộc `familyId` mới được xem và Owner/Parent mới có quyền tải lên/cập nhật.

### 1.4. Supabase Realtime Setup
- [x] Bật Realtime replication cho các bảng:
  - `family_members`, `children`, `devices`, `device_permission_status`, `family_invitations`.
- [x] Cấu hình filter phía client theo `family_id`.

### 1.5. Supabase Edge Functions (API Gateway & Sensitive Ops)
- [x] **Hàm `create-family`**:
  - Nhận `familyName`, tạo bản ghi `families`, tạo bản ghi `family_members` với role `OWNER`, ghi audit log.
- [x] **Hàm `invite-family-member`**:
  - Kiểm tra quyền (Owner/Parent), hash token SHA-256, lưu `family_invitations` (hạn 72h), kích hoạt gửi email qua mail service.
- [x] **Hàm `accept-family-invite`**:
  - Xác thực token hash, kiểm tra hạn/trạng thái, gán user vào `family_members`, chuyển trạng thái `ACCEPTED`.
- [x] **Hàm `cancel-family-invite`**:
  - Cho phép Owner/Parent hủy lời mời đang chờ.
- [x] **Hàm `change-member-role`**:
  - Chỉ Owner được thực hiện, không cho phép hạ quyền Owner cuối cùng.
- [x] **Hàm `remove-family-member`**:
  - Chỉ Owner thực hiện, kiểm tra ràng buộc không xóa Owner cuối cùng.
- [x] **Hàm `transfer-family-ownership`**:
  - Chỉ Owner hiện tại chuyển quyền sang Parent khác.
- [x] **Hàm `create-device-pairing`**:
  - Tạo pairing session với token độ phức tạp cao (QR) + mã 6 số (manual), hạn 10 phút.
- [x] **Hàm `claim-device-pairing`**:
  - Xác thực anonymous JWT của child app, kiểm tra mã ghép đôi, gán `auth_user_id` vào `devices`, đánh dấu session đã dùng.
- [x] **Hàm `unpair-device`**:
  - Ngắt kết nối thiết bị của con, cập nhật `unpaired_at`, thu hồi liên kết.
- [x] **Hàm `delete-child`**:
  - Hủy liên kết toàn bộ thiết bị liên quan, lưu trữ chính sách, soft-delete con (`deleted_at`).
- [x] **Hàm `delete-family`**:
  - Chỉ Owner được thực hiện, dọn dẹp các ràng buộc.
- [x] Chuẩn hóa định dạng lỗi Standard Error Response (`code`, `message`, `requestId`).

---

## Giai đoạn 2: Cấu hình Android Core & Data Infrastructure

### 2.1. Cập nhật Thư viện & Gradle
- [x] Thêm các module Supabase vào `libs.versions.toml` & `app/build.gradle.kts`:
  - `postgrest-kt`, `realtime-kt`, `storage-kt`, `functions-kt`.
- [x] Cấu hình Room Database (`androidx.room:room-runtime`, `androidx.room:room-ktx`, `androidx.room:room-compiler` qua KSP, phiên bản 2.7.0 tương thích KSP2).
- [x] Thêm Coil Compose (`io.coil-kt:coil-compose`) để load avatar mượt mà.
- [x] Thêm thư viện Barcode Scanning/Generating (`com.google.zxing:core:3.5.3` và tiện ích `QrCodeGenerator`).
- [x] Cấu hình `minSdk = 26` (dự án đang sử dụng minSdk 33 >= 26).

### 2.2. Tổ chức Cấu trúc Thư mục theo Kiến trúc Khuyến nghị
- [x] Thiết lập package `core/`:
  - `core/model/`: Các entity/enum dùng chung (`FamilyRole`, `DeviceOnlineStatus`, `ProtectionStatus`, `ChildStatus`, `InvitationStatus`).
  - `core/database/`: Room DB configuration (`GuardianDatabase`), TypeConverters (`Converters`).
  - `core/network/`: Supabase client instance (`SupabaseConfig`, `SupabaseClientProvider`), Network monitor (`NetworkMonitor`, `ConnectivityManagerNetworkMonitor`).
  - `core/designsystem/`: Components, icons, dialogs, status tags (`AvatarImage`, `OnlineStatusBadge`, `RoleBadge`, `ProtectionStatusBadge`).
  - `core/common/`: Result wrapper (`Resource`), UiText (`UiText`), Dispatchers (`AppDispatchers`), DateTimeUtils (`DateTimeUtils`), QrCodeGenerator (`QrCodeGenerator`).
- [x] Thiết lập package `feature/family/`:
  - `data/local/`: DAOs & Entities (`FamilyEntity`, `ChildEntity`, `DeviceEntity`, `FamilyMemberEntity`, `DevicePermissionStatusEntity`, `InvitationEntity`, `FamilyDao`, `ChildDao`, `DeviceDao`, `MemberDao`, `InvitationDao`).
  - `data/remote/`: DTOs (`FamilyDto`, `ChildDto`, `DeviceDto`, `PairingDtos`, `ErrorResponseDto`), Supabase DataSources (`FamilyRemoteDataSource`, `FamilyFunctionDataSource`, `FamilyRealtimeDataSource`).
  - `data/mapper/`: `FamilyMappers` ánh xạ 2 chiều DTO <-> Entity <-> Domain.
  - `domain/model/`: Domain models (`Family`, `Child`, `Device`, `FamilyMember`, `DevicePermissionStatus`, `FamilyInvitation`, `PairingSession`).
  - `domain/repository/`: `FamilyRepository` interface.
  - `domain/usecase/`: Use cases độc lập sẵn sàng triển khai tiếp ở Giai đoạn 3.

### 2.3. Cài đặt Room Local Database (Offline-First Cache)
- [x] Định nghĩa Room Entities:
  - `FamilyEntity`, `ChildEntity`, `FamilyMemberEntity`, `DeviceEntity`, `DevicePermissionStatusEntity`, `InvitationEntity`.
- [x] Định nghĩa Room DAOs:
  - `FamilyDao`, `ChildDao`, `DeviceDao`, `MemberDao`, `InvitationDao` hỗ trợ trả về `Flow<T>`.
- [x] Định nghĩa `GuardianDatabase` với migration strategy (`fallbackToDestructiveMigration(true)`).
- [x] Tạo TypeConverters cho Date/Instant, LocalDate, Enums.

### 2.4. Khởi tạo Supabase Client Đầy Đủ
- [x] Cập nhật `SupabaseConfig.kt` bổ sung các plugin:
  - `install(Auth)`
  - `install(Postgrest)`
  - `install(Realtime)`
  - `install(Storage)`
  - `install(Functions)`

### 2.5. Xử lý Lỗi & Mapping Domain
- [x] Xây dựng sealed class `DomainError` và `UiText` (`UiText.DynamicString`, `UiText.StringResource`).
- [x] Tạo mapper ánh xạ mã lỗi từ Edge Function (`PAIRING_CODE_EXPIRED`, `FORBIDDEN`, `LAST_OWNER_CANNOT_LEAVE`, ...) sang `DomainError` và `UiText`.

---

## Giai đoạn 3: Tầng Domain & Repository (Offline-First & Realtime)

### 3.1. Domain Models
- [ ] Tạo các domain model thuần túy (không phụ thuộc Room/Supabase annotations):
  - `Family`, `Child` (tính tuổi tự động qua `date_of_birth`), `Device`, `FamilyMember` (`FamilyRole`: `OWNER`, `PARENT`, `VIEWER`), `DevicePermissionStatus`, `FamilyInvitation`, `PairingSession`.
  - Enum `DeviceOnlineStatus`: `ONLINE` (<= 2 phút), `RECENTLY_ONLINE` (2-15 phút), `OFFLINE` (> 15 phút).

### 3.2. Data Sources & Mappers
- [ ] Viết Data DTOs tuần tự hóa với `@Serializable`.
- [ ] Viết Data Mappers:
  - DTO <-> Entity (Network -> Local Cache).
  - Entity <-> Domain Model (Local Cache -> Domain).
  - DTO <-> Domain Model (Direct API -> Domain).
- [ ] Xây dựng `FamilyRemoteDataSource`:
  - Thực hiện các truy vấn PostgREST trực tiếp với RLS.
- [ ] Xây dựng `FamilyFunctionDataSource`:
  - Gọi các Supabase Edge Functions cho các tác vụ quan trọng.
- [ ] Xây dựng `FamilyRealtimeDataSource`:
  - Đăng ký nhận sự kiện realtime từ channel `family_id`.

### 3.3. Tầng Repository (`FamilyRepository`)
- [ ] Định nghĩa interface `FamilyRepository`:
  - `fun observeFamily(): Flow<Family?>`
  - `fun observeChildren(familyId: String): Flow<List<Child>>`
  - `fun observeMembers(familyId: String): Flow<List<FamilyMember>>`
  - `fun observeDevices(childId: String): Flow<List<Device>>`
  - `fun observeInvitations(familyId: String): Flow<List<FamilyInvitation>>`
  - `suspend fun refreshFamily()`
  - `suspend fun createFamily(name: String): Family`
  - `suspend fun createChild(name: String, dob: LocalDate?, avatarFile: ByteArray?): Child`
  - `suspend fun updateChild(childId: String, name: String, nickname: String?, dob: LocalDate?, avatarFile: ByteArray?)`
  - `suspend fun deleteChild(childId: String)`
  - `suspend fun createPairingSession(childId: String): PairingSession`
  - `suspend fun unpairDevice(deviceId: String)`
  - `suspend fun renameDevice(deviceId: String, newName: String)`
  - `suspend fun inviteMember(email: String, role: FamilyRole)`
  - `suspend fun cancelInvitation(invitationId: String)`
  - `suspend fun acceptInvitation(token: String)`
  - `suspend fun updateMemberRole(memberId: String, newRole: FamilyRole)`
  - `suspend fun removeMember(memberId: String)`
- [ ] Triển khai `FamilyRepositoryImpl`:
  - Room là **Single Source of Truth** cho UI đọc dữ liệu.
  - Khi mở ứng dụng: đọc từ Room ngay lập tức -> gọi refresh API Supabase -> ghi đè Room -> Room phát ra data mới.
  - Kết nối Realtime: cập nhật tức thời vào Room khi có thay đổi từ thiết bị khác hoặc child app.
  - Thao tác ghi: kiểm tra mạng, không xếp hàng offline (offline queue) với các hành động bảo mật (xóa con, hủy ghép đôi, đổi quyền).

### 3.4. Các Use Case Tương Ứng
- [ ] `ObserveFamilyUseCase`, `RefreshFamilyUseCase`, `CreateFamilyUseCase`.
- [ ] `ObserveChildrenUseCase`, `GetChildDetailUseCase`, `CreateChildUseCase`, `UpdateChildUseCase`, `DeleteChildUseCase`.
- [ ] `ObserveMembersUseCase`, `InviteMemberUseCase`, `CancelInvitationUseCase`, `AcceptInvitationUseCase`, `UpdateMemberRoleUseCase`, `RemoveMemberUseCase`.
- [ ] `ObserveDevicesUseCase`, `GetDeviceDetailUseCase`, `CreatePairingSessionUseCase`, `UnpairDeviceUseCase`, `RenameDeviceUseCase`.

---

## Giai đoạn 4: Thành phần Giao diện & Design System (UI Components)

- [ ] **Badge trạng thái thiết bị (`DeviceStatusBadge`)**:
  - Hỗ trợ semantic/accessibility: Không dùng màu đơn lẻ, kết hợp icon + text (`● Online`, `● Recently Online`, `● Offline`).
- [ ] **Card trẻ em (`ChildCard`)**:
  - Hiển thị avatar, tên, tuổi (tính từ DOB), tóm tắt thiết bị và trạng thái bảo vệ (Protection status).
- [ ] **Card thành viên (`MemberCard`)**:
  - Hiển thị avatar, tên, nhãn quyền (`Owner`, `Parent`, `Viewer`), huy hiệu `You`.
- [ ] **Card thiết bị (`DeviceCard`)**:
  - Model, battery level (%) kèm icon pin/sạc, trạng thái bảo vệ, thời gian last seen.
- [ ] **Thành phần Skeleton Loading (`FamilySkeletonLoader`)**:
  - Shimmer placeholders cho header, danh sách trẻ em và thành viên, tránh full-screen spinner đột ngột.
- [ ] **Banner Offline (`OfflineNoticeBanner`)**:
  - Thông báo rõ ràng: *"Bạn đang ngoại tuyến. Dữ liệu cập nhật lúc 08:30"*.
- [ ] **Hộp thoại xác nhận phá hủy (`GuardianConfirmDialog`)**:
  - Theo chuẩn Material 3 AlertDialog cho các hành động xóa trẻ, hủy ghép nối thiết bị, xóa thành viên. Nút hành động nguy hiểm đặt đúng quy chuẩn.
- [ ] **Bộ chọn & nén ảnh đại diện (`AvatarPickerHelper`)**:
  - Tích hợp Android Photo Picker, nén WebP/JPEG, kiểm tra kích thước tối ưu trước khi tải lên Supabase Storage.

---

## Giai đoạn 5: Phát triển Giao diện & Màn hình Family (Parent Side)

### 5.1. Định tuyến Navigation Compose
- [ ] Định nghĩa `FamilyDestinations`:
  - `family` (Home)
  - `family/create` (Tạo gia đình nếu chưa có)
  - `family/child/add`
  - `family/child/{childId}`
  - `family/child/{childId}/edit`
  - `family/child/{childId}/pair-device`
  - `family/device/{deviceId}`
  - `family/device/{deviceId}/permissions`
  - `family/member/{memberId}`
  - `family/invite`
  - `family/settings`
- [ ] Thiết lập Deep Link cho lời mời: `https://guardian.example.com/invite/{token}` mở thẳng màn hình xác nhận lời mời.
- [ ] Đảm bảo chỉ truyền tham số ID đơn giản giữa các route; ViewModel tự nạp entity từ Repository.

### 5.2. Màn hình Family Home (`FamilyHomeScreen`)
- [ ] Xây dựng `FamilyUiState`:
  - `Loading`, `Content` (family, children, members, pendingInvitations, isRefreshing, isOffline), `Empty` (canCreateFamily), `Error`.
- [ ] Xây dựng `FamilyViewModel` & `FamilyAction`:
  - Tách bạch UDF, thu thập State qua `collectAsStateWithLifecycle()`.
- [ ] Giao diện Family Home:
  - Header gia đình (Tên, số lượng thành viên, số trẻ).
  - Section Children kèm nút `+ Add child`.
  - Section Parents & Guardians kèm danh sách pending invitations và nút `+ Invite parent`.
  - Liên kết điều hướng Family Settings.
  - Hỗ trợ Pull-to-refresh.

### 5.3. Màn hình Tạo Gia đình (`CreateFamilyScreen`)
- [ ] Form nhập tên gia đình (`family_name`).
- [ ] Validation: bắt buộc, trim khoảng trắng, từ 2–50 ký tự.
- [ ] Gọi `create-family` qua ViewModel, xử lý loading và điều hướng sang Family Home.

### 5.4. Màn hình Thêm & Sửa Trẻ (`AddChildScreen` / `EditChildScreen`)
- [ ] Các trường: Tên (bắt buộc, 1-50 chars), Nickname, Ngày sinh (DatePicker, <= ngày hiện tại), Ảnh đại diện.
- [ ] Chọn ảnh qua Android Photo Picker, preview và tải lên.
- [ ] Không cho phép nhập tuổi thủ công (tuổi sinh động theo DOB).

### 5.5. Màn hình Chi tiết Trẻ (`ChildDetailScreen`)
- [ ] Header: Avatar lớn, tên, tuổi.
- [ ] Section Guardian Devices:
  - Danh sách thiết bị của trẻ, pin, trạng thái hoạt động, nút `+ Add device`.
- [ ] Section Protection Shortcuts:
  - Lối tắt sang Tab Rules (số rules đang bật) và Tab Activity (thời gian hoạt động gần nhất).
- [ ] Section Profile:
  - Personal info, Child settings.
- [ ] Menu tùy chọn: Sửa thông tin, Lưu trữ, Xóa trẻ (hiển thị Dialog cảnh báo ngắt kết nối toàn bộ thiết bị liên quan).

### 5.6. Màn hình Ghép đôi Thiết bị (`PairDeviceScreen`)
- [ ] Sinh mã QR (chứa JSON object với high-entropy secret token).
- [ ] Hiển thị mã 6 số phụ trợ để nhập thủ công.
- [ ] Đếm ngược thời gian hết hạn (10 phút) kèm nút tạo lại mã mới khi hết hạn.
- [ ] Lắng nghe Realtime: Tự động chuyển màn hình thành công khi con quét/ghép đôi hoàn tất.

### 5.7. Màn hình Chi tiết Thiết bị (`DeviceDetailScreen`)
- [ ] Thông tin thiết bị: Model, Hệ điều hành Android, Phiên bản Guardian, Mức pin & trạng thái sạc, Last seen.
- [ ] Snapshot Quyền thiết bị (`DevicePermissionSnapshot`):
  - Usage access, Accessibility, VPN active, Notifications, Location, Device admin.
- [ ] Tùy chọn quản lý: Đổi tên thiết bị, Hủy ghép đôi (`Unpair Device`) kèm Confirm Dialog.

### 5.8. Màn hình Quản lý Thành viên & Lời mời (`InviteParentScreen` / `MemberDetailScreen`)
- [ ] Màn hình mời phụ huynh: Nhập Email, chọn Role (`Parent` hoặc `Viewer`), hiển thị mô tả quyền hạn tương ứng.
- [ ] Hiển thị trạng thái lời mời (Đang chờ, Ngày hết hạn 72h), nút Gửi lại (Resend) và Hủy (Cancel).
- [ ] Màn hình chi tiết thành viên: Đổi vai trò (Chỉ Owner), Xóa thành viên (Chỉ Owner).
- [ ] Màn hình Nhận lời mời (`AcceptInviteScreen` qua App Link): Xem thông tin gia đình được mời, chấp thuận hoặc từ chối.

---

## Giai đoạn 6: Tích hợp Thiết bị Con (Child Device Companion Flow)

- [ ] Anonymous Auth Sign-in với Supabase Auth khi cài đặt app con.
- [ ] Màn hình Quét QR hoặc Nhập mã 6 chữ số ghép đôi.
- [ ] Gọi Edge Function `claim-device-pairing` gửi metadata thiết bị (Model, SDK, Version).
- [ ] Service thu thập trạng thái quyền (`usage_access`, `accessibility_service`, `vpn_active`, `notification_permission`, `location_permission`, `device_admin`).
- [ ] Định kỳ gửi heartbeat cập nhật `last_seen_at`, pin và trạng thái bảo vệ lên Supabase.
- [ ] Đảm bảo RLS chặn hoàn toàn app con truy vấn dữ liệu nhạy cảm của gia đình hoặc trẻ khác.

---

## Giai đoạn 7: Kiểm thử, Tối ưu & Đảm bảo Chất lượng

### 7.1. Unit & Integration Testing
- [ ] Viết Unit Tests cho ViewModel (`FamilyViewModel`, `ChildDetailViewModel`, `PairDeviceViewModel`).
- [ ] Viết DAO & Migration Tests cho Room Database.
- [ ] Viết Repository Tests xác thực logic Offline-First & Realtime sync.
- [ ] Kiểm thử Mapping lỗi Edge Function sang `UiText`.

### 7.2. Kiểm thử Bảo mật & RLS Policies
- [ ] Kiểm thử tài khoản Parent không thể truy cập gia đình khác (Isolation test).
- [ ] Kiểm thử Viewer không thể thực hiện các thao tác ghi (Write restriction).
- [ ] Kiểm thử Anonymous Child device chỉ có thể truy cập tài nguyên của chính nó, không đọc được `family_members` hay `children`.
- [ ] Kiểm thử không thể xóa hoặc hạ quyền Owner cuối cùng.
- [ ] Kiểm thử token hết hạn hoặc tái sử dụng sẽ bị từ chối.

### 7.3. UI/UX, Khả năng Tiếp cận (Accessibility) & Thiết bị Màn hình Lớn
- [ ] Kiểm tra chuẩn Accessibility: Touch target tối thiểu 48dp, TalkBack semantics cho icon/avatar/status badge.
- [ ] Hỗ trợ Dynamic Font Scaling không bị vỡ bố cục.
- [ ] Tối ưu giao diện thích ứng (Adaptive Layout): Chuẩn bị bố cục List-Detail cho máy tính bảng / Foldable.
