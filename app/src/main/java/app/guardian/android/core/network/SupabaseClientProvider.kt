package app.guardian.android.core.network

import app.guardian.android.data.supabase.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage

/**
 * Accessor for the shared SupabaseClient instance and its sub-modules.
 */
object SupabaseClientProvider {
    val client: SupabaseClient
        get() = SupabaseConfig.client

    val auth get() = client.auth
    val postgrest get() = client.postgrest
    val realtime get() = client.realtime
    val storage get() = client.storage
    val functions get() = client.functions
}
