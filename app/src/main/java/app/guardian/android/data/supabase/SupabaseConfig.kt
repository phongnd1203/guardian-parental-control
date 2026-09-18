package app.guardian.android.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Supabase configuration and client instance.
 *
 * To connect to your Supabase project:
 * 1. Replace [SUPABASE_URL] with your project URL (e.g. "https://xyzcompany.supabase.co").
 * 2. Replace [SUPABASE_PUBLIC_KEY] with your project anon/public key.
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
        }
    }
}
