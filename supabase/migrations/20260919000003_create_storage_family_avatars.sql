-- Migration: 20260919000003_create_storage_family_avatars.sql
-- Description: Create private storage bucket 'family-avatars' and access policies

-- 1. Create storage bucket if not exists
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'family-avatars',
    'family-avatars',
    false,
    5242880, -- 5MB limit
    ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO NOTHING;

-- 2. Storage RLS Policies for family-avatars
-- Path format: {familyId}/children/{childId}/avatar.webp

CREATE POLICY "family_avatars_select" ON storage.objects
FOR SELECT USING (
    bucket_id = 'family-avatars' AND
    public.is_parent_user() AND
    public.is_family_member((storage.foldername(name))[1]::uuid)
);

CREATE POLICY "family_avatars_insert" ON storage.objects
FOR INSERT WITH CHECK (
    bucket_id = 'family-avatars' AND
    public.is_parent_user() AND
    public.has_family_role((storage.foldername(name))[1]::uuid, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "family_avatars_update" ON storage.objects
FOR UPDATE USING (
    bucket_id = 'family-avatars' AND
    public.is_parent_user() AND
    public.has_family_role((storage.foldername(name))[1]::uuid, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "family_avatars_delete" ON storage.objects
FOR DELETE USING (
    bucket_id = 'family-avatars' AND
    public.is_parent_user() AND
    public.has_family_role((storage.foldername(name))[1]::uuid, ARRAY['OWNER', 'PARENT'])
);
