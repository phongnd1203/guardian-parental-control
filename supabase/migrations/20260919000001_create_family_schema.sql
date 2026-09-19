-- Migration: 20260919000001_create_family_schema.sql
-- Description: Create core schema, tables, constraints, triggers, and indexes for Guardian Family Module v1.0

-- 1. Helper function for updated_at timestamps
CREATE OR REPLACE FUNCTION update_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Table: families
CREATE TABLE IF NOT EXISTS public.families (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL CHECK (char_length(trim(name)) >= 2 AND char_length(trim(name)) <= 50),
    owner_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_families_owner_user_id ON public.families(owner_user_id);

CREATE OR REPLACE TRIGGER trigger_families_updated_at
BEFORE UPDATE ON public.families
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

-- 3. Table: family_members
CREATE TABLE IF NOT EXISTS public.family_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES public.families(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('OWNER', 'PARENT', 'VIEWER')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_family_members UNIQUE (family_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_family_members_family_id ON public.family_members(family_id);
CREATE INDEX IF NOT EXISTS idx_family_members_user_id ON public.family_members(user_id);
CREATE INDEX IF NOT EXISTS idx_family_members_family_role ON public.family_members(family_id, role);

-- 4. Table: children
CREATE TABLE IF NOT EXISTS public.children (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES public.families(id) ON DELETE CASCADE,
    name TEXT NOT NULL CHECK (char_length(trim(name)) >= 1 AND char_length(trim(name)) <= 50),
    nickname TEXT NULL CHECK (nickname IS NULL OR char_length(trim(nickname)) <= 30),
    date_of_birth DATE NULL CHECK (date_of_birth IS NULL OR date_of_birth <= CURRENT_DATE),
    avatar_path TEXT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_by UUID NULL REFERENCES auth.users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    archived_at TIMESTAMPTZ NULL,
    deleted_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_children_family_id ON public.children(family_id);
CREATE INDEX IF NOT EXISTS idx_children_family_status ON public.children(family_id, status);

CREATE OR REPLACE TRIGGER trigger_children_updated_at
BEFORE UPDATE ON public.children
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

-- 5. Table: devices
CREATE TABLE IF NOT EXISTS public.devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES public.families(id) ON DELETE CASCADE,
    child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
    auth_user_id UUID UNIQUE REFERENCES auth.users(id) ON DELETE SET NULL,
    name TEXT NOT NULL DEFAULT 'Child Device',
    manufacturer TEXT NULL,
    model TEXT NULL,
    android_version TEXT NULL,
    api_level INT NULL,
    app_version TEXT NULL,
    app_build INT NULL,
    battery_level INT NULL CHECK (battery_level IS NULL OR (battery_level >= 0 AND battery_level <= 100)),
    charging BOOLEAN NULL DEFAULT false,
    protection_status TEXT NOT NULL DEFAULT 'ACTIVE',
    last_seen_at TIMESTAMPTZ NULL,
    paired_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    unpaired_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_devices_child_id ON public.devices(child_id);
CREATE INDEX IF NOT EXISTS idx_devices_family_id ON public.devices(family_id);
CREATE INDEX IF NOT EXISTS idx_devices_auth_user_id ON public.devices(auth_user_id);
CREATE INDEX IF NOT EXISTS idx_devices_last_seen_at ON public.devices(last_seen_at);

CREATE OR REPLACE TRIGGER trigger_devices_updated_at
BEFORE UPDATE ON public.devices
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

-- 6. Table: device_permission_status
CREATE TABLE IF NOT EXISTS public.device_permission_status (
    device_id UUID PRIMARY KEY REFERENCES public.devices(id) ON DELETE CASCADE,
    usage_access BOOLEAN NOT NULL DEFAULT false,
    accessibility_service BOOLEAN NOT NULL DEFAULT false,
    notification_permission BOOLEAN NOT NULL DEFAULT false,
    vpn_active BOOLEAN NOT NULL DEFAULT false,
    location_permission BOOLEAN NOT NULL DEFAULT false,
    device_admin BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE OR REPLACE TRIGGER trigger_device_permission_status_updated_at
BEFORE UPDATE ON public.device_permission_status
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

-- 7. Table: family_invitations
CREATE TABLE IF NOT EXISTS public.family_invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES public.families(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('PARENT', 'VIEWER')),
    token_hash TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED')),
    expires_at TIMESTAMPTZ NOT NULL,
    invited_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    accepted_by UUID NULL REFERENCES auth.users(id) ON DELETE SET NULL,
    accepted_at TIMESTAMPTZ NULL,
    cancelled_at TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_family_invitations_pending_email
ON public.family_invitations(family_id, lower(email))
WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_family_invitations_family_id ON public.family_invitations(family_id);

-- 8. Table: device_pairing_sessions
CREATE TABLE IF NOT EXISTS public.device_pairing_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES public.families(id) ON DELETE CASCADE,
    child_id UUID NOT NULL REFERENCES public.children(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL,
    manual_code_hash TEXT NOT NULL,
    created_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    claimed_at TIMESTAMPTZ NULL,
    claimed_device_id UUID NULL REFERENCES public.devices(id) ON DELETE SET NULL,
    cancelled_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_device_pairing_token_hash ON public.device_pairing_sessions(token_hash);
CREATE INDEX IF NOT EXISTS idx_device_pairing_manual_code_hash ON public.device_pairing_sessions(manual_code_hash);

-- 9. Table: family_audit_logs
CREATE TABLE IF NOT EXISTS public.family_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES public.families(id) ON DELETE CASCADE,
    actor_type TEXT NOT NULL CHECK (actor_type IN ('USER', 'SYSTEM', 'DEVICE')),
    actor_id UUID NULL,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id UUID NULL,
    metadata JSONB NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_family_audit_logs_family_id ON public.family_audit_logs(family_id, created_at DESC);
