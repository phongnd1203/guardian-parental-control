package app.guardian.android.feature.family.di

import android.content.Context
import app.guardian.android.core.database.GuardianDatabase
import app.guardian.android.core.network.ConnectivityManagerNetworkMonitor
import app.guardian.android.core.network.NetworkMonitor
import app.guardian.android.feature.family.data.remote.SupabaseFamilyFunctionDataSource
import app.guardian.android.feature.family.data.remote.SupabaseFamilyRealtimeDataSource
import app.guardian.android.feature.family.data.remote.SupabaseFamilyRemoteDataSource
import app.guardian.android.feature.family.data.repository.FamilyRepositoryImpl
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import app.guardian.android.feature.family.domain.usecase.AcceptInvitationUseCase
import app.guardian.android.feature.family.domain.usecase.CancelInvitationUseCase
import app.guardian.android.feature.family.domain.usecase.CreateChildUseCase
import app.guardian.android.feature.family.domain.usecase.CreateFamilyUseCase
import app.guardian.android.feature.family.domain.usecase.CreatePairingSessionUseCase
import app.guardian.android.feature.family.domain.usecase.DeleteChildUseCase
import app.guardian.android.feature.family.domain.usecase.GetChildDetailUseCase
import app.guardian.android.feature.family.domain.usecase.GetDeviceDetailUseCase
import app.guardian.android.feature.family.domain.usecase.InviteMemberUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveChildrenUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveDevicesUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveFamilyUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveInvitationsUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveMembersUseCase
import app.guardian.android.feature.family.domain.usecase.RefreshFamilyUseCase
import app.guardian.android.feature.family.domain.usecase.RemoveMemberUseCase
import app.guardian.android.feature.family.domain.usecase.RenameDeviceUseCase
import app.guardian.android.feature.family.domain.usecase.UnpairDeviceUseCase
import app.guardian.android.feature.family.domain.usecase.UpdateChildUseCase
import app.guardian.android.feature.family.domain.usecase.UpdateMemberRoleUseCase

/**
 * Service Locator / Dependency Provider for Family module components.
 */
object FamilyDependencyProvider {

    @Volatile
    private var repositoryInstance: FamilyRepositoryImpl? = null

    @Volatile
    private var networkMonitorInstance: NetworkMonitor? = null

    fun provideRepository(context: Context): FamilyRepositoryImpl {
        return repositoryInstance ?: synchronized(this) {
            repositoryInstance ?: run {
                val db = GuardianDatabase.getInstance(context)
                FamilyRepositoryImpl(
                    familyDao = db.familyDao(),
                    childDao = db.childDao(),
                    deviceDao = db.deviceDao(),
                    memberDao = db.memberDao(),
                    invitationDao = db.invitationDao(),
                    remoteDataSource = SupabaseFamilyRemoteDataSource(),
                    functionDataSource = SupabaseFamilyFunctionDataSource(),
                    realtimeDataSource = SupabaseFamilyRealtimeDataSource()
                ).also { repositoryInstance = it }
            }
        }
    }

    fun provideNetworkMonitor(context: Context): NetworkMonitor {
        return networkMonitorInstance ?: synchronized(this) {
            networkMonitorInstance ?: ConnectivityManagerNetworkMonitor(context.applicationContext)
                .also { networkMonitorInstance = it }
        }
    }

    fun provideObserveFamilyUseCase(context: Context) = ObserveFamilyUseCase(provideRepository(context))
    fun provideRefreshFamilyUseCase(context: Context) = RefreshFamilyUseCase(provideRepository(context))
    fun provideCreateFamilyUseCase(context: Context) = CreateFamilyUseCase(provideRepository(context))

    fun provideObserveChildrenUseCase(context: Context) = ObserveChildrenUseCase(provideRepository(context))
    fun provideGetChildDetailUseCase(context: Context) = GetChildDetailUseCase(provideRepository(context))
    fun provideCreateChildUseCase(context: Context) = CreateChildUseCase(provideRepository(context))
    fun provideUpdateChildUseCase(context: Context) = UpdateChildUseCase(provideRepository(context))
    fun provideDeleteChildUseCase(context: Context) = DeleteChildUseCase(provideRepository(context))

    fun provideObserveMembersUseCase(context: Context) = ObserveMembersUseCase(provideRepository(context))
    fun provideObserveInvitationsUseCase(context: Context) = ObserveInvitationsUseCase(provideRepository(context))
    fun provideInviteMemberUseCase(context: Context) = InviteMemberUseCase(provideRepository(context))
    fun provideCancelInvitationUseCase(context: Context) = CancelInvitationUseCase(provideRepository(context))
    fun provideAcceptInvitationUseCase(context: Context) = AcceptInvitationUseCase(provideRepository(context))
    fun provideUpdateMemberRoleUseCase(context: Context) = UpdateMemberRoleUseCase(provideRepository(context))
    fun provideRemoveMemberUseCase(context: Context) = RemoveMemberUseCase(provideRepository(context))

    fun provideObserveDevicesUseCase(context: Context) = ObserveDevicesUseCase(provideRepository(context))
    fun provideGetDeviceDetailUseCase(context: Context) = GetDeviceDetailUseCase(provideRepository(context))
    fun provideCreatePairingSessionUseCase(context: Context) = CreatePairingSessionUseCase(provideRepository(context))
    fun provideUnpairDeviceUseCase(context: Context) = UnpairDeviceUseCase(provideRepository(context))
    fun provideRenameDeviceUseCase(context: Context) = RenameDeviceUseCase(provideRepository(context))
}
