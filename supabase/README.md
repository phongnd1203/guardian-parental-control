# Supabase Backend Configuration & Deployment Guide: Guardian Family Module

Tài liệu này hướng dẫn cách thiết lập, cấu hình và quản trị toàn bộ hệ thống Supabase (Database Schema, RLS, Storage, Realtime, Edge Functions) cho Guardian Family Module.

---

## 1. Kết quả thực hiện tự động qua Supabase MCP

Toàn bộ các thành phần đã được triển khai trực tiếp vào Supabase project thông qua Supabase MCP:

- **Database Tables & Constraints (8 tables):** `families`, `family_members`, `children`, `devices`, `device_permission_status`, `family_invitations`, `device_pairing_sessions`, `family_audit_logs`.
- **Row Level Security (RLS):** Bật trên 100% các bảng với các hàm kiểm tra quyền độc lập (`is_parent_user`, `is_child_device`, `is_family_member`, `has_family_role`).
- **Realtime Publication:** Đã kích hoạt trên 5 bảng nghiệp vụ (`family_members`, `children`, `devices`, `device_permission_status`, `family_invitations`).
- **Storage Bucket:** `family-avatars` (Private, 5MB limit, WebP/JPEG/PNG) kèm Storage RLS theo đường dẫn `{familyId}/children/{childId}/avatar.webp`.
- **Edge Functions (12 functions ACTIVE):**
  1. `create-family`
  2. `invite-family-member`
  3. `accept-family-invite`
  4. `cancel-family-invite`
  5. `change-member-role`
  6. `remove-family-member`
  7. `transfer-family-ownership`
  8. `create-device-pairing`
  9. `claim-device-pairing`
  10. `unpair-device`
  11. `delete-child`
  12. `delete-family`

---

## 2. Hướng dẫn thiết lập thủ công (Manual Setup Fallback)

Nếu cần tái tạo trên môi trường mới hoặc triển khai qua Supabase CLI / Web Dashboard, thực hiện theo các bước sau:

### Cách 1: Sử dụng Supabase CLI (Khuyến nghị cho CI/CD & Local)

1. **Cài đặt Supabase CLI:**
   ```bash
   brew install supabase/tap/supabase
   ```

2. **Đăng nhập và liên kết dự án:**
   ```bash
   supabase login
   supabase link --project-ref <PROJECT_REF>
   ```

3. **Áp dụng các Migration SQL:**
   ```bash
   supabase db push
   ```
   *(Các file migration nằm trong thư mục `supabase/migrations/`)*

4. **Triển khai Edge Functions:**
   ```bash
   supabase functions deploy create-family
   supabase functions deploy invite-family-member
   supabase functions deploy accept-family-invite
   supabase functions deploy cancel-family-invite
   supabase functions deploy change-member-role
   supabase functions deploy remove-family-member
   supabase functions deploy transfer-family-ownership
   supabase functions deploy create-device-pairing
   supabase functions deploy claim-device-pairing
   supabase functions deploy unpair-device
   supabase functions deploy delete-child
   supabase functions deploy delete-family
   ```

---

### Cách 2: Sử dụng Supabase Web Dashboard (Thực hiện thủ công)

#### Bước 2.1: Chạy SQL Migrations
1. Mở Supabase Dashboard -> chọn Project -> vào mục **SQL Editor**.
2. Mở file `supabase/migrations/20260919000001_create_family_schema.sql`, copy toàn bộ nội dung dán vào SQL Editor và bấm **Run**.
3. Mở file `supabase/migrations/20260919000002_create_family_rls_and_realtime.sql`, dán vào và bấm **Run**.
4. Mở file `supabase/migrations/20260919000003_create_storage_family_avatars.sql`, dán vào và bấm **Run**.

#### Bước 2.2: Kiểm tra Storage Bucket
1. Vào mục **Storage** trên thanh menu trái.
2. Kiểm tra bucket `family-avatars`:
   - Visibility: `Private`
   - File size limit: `5MB`
   - Allowed MIME types: `image/jpeg, image/png, image/webp`
3. Vào tab **Policies** của Storage, xác nhận có 4 policy: `family_avatars_select`, `family_avatars_insert`, `family_avatars_update`, `family_avatars_delete`.

#### Bước 2.3: Kiểm tra Realtime Replication
1. Vào mục **Database** -> **Replication**.
2. Xác nhận publication `supabase_realtime` có 5 bảng được bật:
   - `family_members`
   - `children`
   - `devices`
   - `device_permission_status`
   - `family_invitations`

#### Bước 2.4: Cấu hình biến môi trường (Secrets) cho Edge Functions
Mỗi Edge Function sử dụng các secrets mặc định của Supabase:
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`

Supabase tự động inject các secrets này vào Edge Functions runtime.
