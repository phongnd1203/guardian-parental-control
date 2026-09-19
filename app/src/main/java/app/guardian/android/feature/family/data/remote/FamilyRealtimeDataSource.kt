package app.guardian.android.feature.family.data.remote

import app.guardian.android.core.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface FamilyRealtimeDataSource {
    suspend fun subscribeFamilyChanges(familyId: String): Flow<PostgresAction>
}

class SupabaseFamilyRealtimeDataSource : FamilyRealtimeDataSource {

    private val realtime = SupabaseClientProvider.realtime

    override suspend fun subscribeFamilyChanges(familyId: String): Flow<PostgresAction> {
        return runCatching {
            val channel = realtime.channel("family-$familyId")
            val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                filter("family_id", FilterOperator.EQ, familyId)
            }
            channel.subscribe()
            flow
        }.getOrDefault(emptyFlow())
    }
}
