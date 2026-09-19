package app.guardian.android.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Supabase configuration and client instance.
 *
 * Configured with:
 * - Auth: User & Child device session management
 * - Postgrest: Direct database querying via RLS
 * - Realtime: Live data synchronization
 * - Storage: Family and child avatar upload/download
 * - Functions: Calling Supabase Edge Functions
 */
object SupabaseConfig {

    // Replace with your actual Supabase project URL and anon public key
    const val SUPABASE_URL: String = "https://zqxaevwkbougyjjugqcq.supabase.co"
    const val SUPABASE_PUBLIC_KEY: String = "sb_publishable_Z35jDfKiXjOuwg0GHtPGHQ_MRKAEky9"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_PUBLIC_KEY
        ) {
            httpEngine = OkHttp.create()
            install(Auth) {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
            install(Functions)
        }
    }
}
