-- Migration: 20260919000002_create_family_rls_and_realtime.sql
-- Description: Helper functions, Row Level Security (RLS) policies, and Realtime publications for Guardian Family Module v1.0

-- 1. Helper functions for authorization
CREATE OR REPLACE FUNCTION public.is_parent_user()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
  SELECT coalesce((auth.jwt()->>'is_anonymous')::boolean, false) = false;
$$;

CREATE OR REPLACE FUNCTION public.is_child_device()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
  SELECT coalesce((auth.jwt()->>'is_anonymous')::boolean, false) = true;
$$;

CREATE OR REPLACE FUNCTION public.is_family_member(f_id UUID)
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
STABLE
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.family_members
    WHERE family_id = f_id
      AND user_id = auth.uid()
  );
$$;

CREATE OR REPLACE FUNCTION public.has_family_role(f_id UUID, allowed_roles TEXT[])
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
STABLE
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.family_members
    WHERE family_id = f_id
      AND user_id = auth.uid()
      AND role = ANY(allowed_roles)
  );
$$;

-- 2. Enable Row Level Security on all tables
ALTER TABLE public.families ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.family_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.children ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_permission_status ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.family_invitations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_pairing_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.family_audit_logs ENABLE ROW LEVEL SECURITY;

-- 3. RLS Policies: families
CREATE POLICY "families_select_members" ON public.families
FOR SELECT USING (
    public.is_parent_user() AND (public.is_family_member(id) OR owner_user_id = auth.uid())
);

CREATE POLICY "families_insert_authenticated" ON public.families
FOR INSERT WITH CHECK (
    public.is_parent_user() AND owner_user_id = auth.uid()
);

CREATE POLICY "families_update_owner" ON public.families
FOR UPDATE USING (
    public.is_parent_user() AND public.has_family_role(id, ARRAY['OWNER'])
);

CREATE POLICY "families_delete_owner" ON public.families
FOR DELETE USING (
    public.is_parent_user() AND public.has_family_role(id, ARRAY['OWNER'])
);

-- 4. RLS Policies: family_members
CREATE POLICY "family_members_select" ON public.family_members
FOR SELECT USING (
    public.is_parent_user() AND public.is_family_member(family_id)
);

CREATE POLICY "family_members_insert_owner" ON public.family_members
FOR INSERT WITH CHECK (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER'])
);

CREATE POLICY "family_members_update_owner" ON public.family_members
FOR UPDATE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER'])
);

CREATE POLICY "family_members_delete_owner" ON public.family_members
FOR DELETE USING (
    public.is_parent_user() AND (
        public.has_family_role(family_id, ARRAY['OWNER']) OR
        (user_id = auth.uid() AND role != 'OWNER')
    )
);

-- 5. RLS Policies: children
CREATE POLICY "children_select_members" ON public.children
FOR SELECT USING (
    public.is_parent_user() AND public.is_family_member(family_id)
);

CREATE POLICY "children_insert_parents" ON public.children
FOR INSERT WITH CHECK (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "children_update_parents" ON public.children
FOR UPDATE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "children_delete_owner" ON public.children
FOR DELETE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER'])
);

-- 6. RLS Policies: devices
CREATE POLICY "devices_select" ON public.devices
FOR SELECT USING (
    (public.is_parent_user() AND public.is_family_member(family_id)) OR
    (public.is_child_device() AND auth_user_id = auth.uid())
);

CREATE POLICY "devices_insert_parents" ON public.devices
FOR INSERT WITH CHECK (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "devices_update" ON public.devices
FOR UPDATE USING (
    (public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])) OR
    (public.is_child_device() AND auth_user_id = auth.uid())
);

CREATE POLICY "devices_delete_parents" ON public.devices
FOR DELETE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

-- 7. RLS Policies: device_permission_status
CREATE POLICY "device_permission_select" ON public.device_permission_status
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = device_id AND (
            (public.is_parent_user() AND public.is_family_member(d.family_id)) OR
            (public.is_child_device() AND d.auth_user_id = auth.uid())
        )
    )
);

CREATE POLICY "device_permission_insert" ON public.device_permission_status
FOR INSERT WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = device_id AND (
            (public.is_parent_user() AND public.has_family_role(d.family_id, ARRAY['OWNER', 'PARENT'])) OR
            (public.is_child_device() AND d.auth_user_id = auth.uid())
        )
    )
);

CREATE POLICY "device_permission_update" ON public.device_permission_status
FOR UPDATE USING (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = device_id AND (
            (public.is_parent_user() AND public.has_family_role(d.family_id, ARRAY['OWNER', 'PARENT'])) OR
            (public.is_child_device() AND d.auth_user_id = auth.uid())
        )
    )
);

CREATE POLICY "device_permission_delete" ON public.device_permission_status
FOR DELETE USING (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = device_id AND
            public.is_parent_user() AND public.has_family_role(d.family_id, ARRAY['OWNER', 'PARENT'])
    )
);

-- 8. RLS Policies: family_invitations
CREATE POLICY "invitations_select" ON public.family_invitations
FOR SELECT USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "invitations_insert" ON public.family_invitations
FOR INSERT WITH CHECK (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "invitations_update" ON public.family_invitations
FOR UPDATE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "invitations_delete" ON public.family_invitations
FOR DELETE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

-- 9. RLS Policies: device_pairing_sessions
CREATE POLICY "pairing_sessions_select" ON public.device_pairing_sessions
FOR SELECT USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "pairing_sessions_insert" ON public.device_pairing_sessions
FOR INSERT WITH CHECK (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "pairing_sessions_update" ON public.device_pairing_sessions
FOR UPDATE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

CREATE POLICY "pairing_sessions_delete" ON public.device_pairing_sessions
FOR DELETE USING (
    public.is_parent_user() AND public.has_family_role(family_id, ARRAY['OWNER', 'PARENT'])
);

-- 10. RLS Policies: family_audit_logs
CREATE POLICY "audit_logs_select" ON public.family_audit_logs
FOR SELECT USING (
    public.is_parent_user() AND public.is_family_member(family_id)
);
-- Note: Audit logs INSERT/UPDATE/DELETE are strictly forbidden for clients. Only service_role / Edge Functions can write.

-- 11. Realtime Publication
-- Add tables to realtime publication
DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE public.family_members;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE public.children;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE public.devices;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE public.device_permission_status;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE public.family_invitations;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
