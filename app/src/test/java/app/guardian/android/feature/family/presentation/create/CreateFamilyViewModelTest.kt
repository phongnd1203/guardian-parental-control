package app.guardian.android.feature.family.presentation.create

import android.app.Application
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.model.Family
import app.guardian.android.feature.family.domain.model.FamilyInvitation
import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.model.PairingSession
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import app.guardian.android.feature.family.domain.usecase.AcceptInvitationUseCase
import app.guardian.android.feature.family.domain.usecase.CreateFamilyUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class CreateFamilyViewModelTest {

    private lateinit var fakeRepository: FakeFamilyRepository
    private lateinit var viewModel: CreateFamilyViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFamilyRepository()
        val app = Application()
        viewModel = CreateFamilyViewModel(
            application = app,
            createFamily = CreateFamilyUseCase(fakeRepository),
            acceptInvitation = AcceptInvitationUseCase(fakeRepository),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun `default tab is CREATE and family name validation works`() {
        assertEquals(CreateFamilyTab.CREATE, viewModel.uiState.value.selectedTab)

        viewModel.onFamilyNameChange("A")
        viewModel.onSubmit { }
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.onFamilyNameChange("Nguyen Family")
        assertEquals("Nguyen Family", viewModel.uiState.value.familyName)
    }

    @Test
    fun `switching tab to JOIN and submitting manual code calls acceptInvitation`() {
        viewModel.onTabChange(CreateFamilyTab.JOIN)
        assertEquals(CreateFamilyTab.JOIN, viewModel.uiState.value.selectedTab)

        viewModel.onInviteCodeChange("https://guardian.example.com/invite/inv-token-999")
        var joined = false
        viewModel.onSubmit { joined = true }

        assertTrue(joined)
        assertEquals("inv-token-999", fakeRepository.lastAcceptedToken)
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `onQrCodeScanned extracts token and joins family`() {
        var joined = false
        viewModel.onQrCodeScanned("https://guardian.example.com/invite/qr-secret-123") {
            joined = true
        }

        assertTrue(joined)
        assertEquals("qr-secret-123", fakeRepository.lastAcceptedToken)
        assertEquals("qr-secret-123", viewModel.uiState.value.inviteCode)
    }

    @Test
    fun `onQrCodeScanned with blank token sets error`() {
        var joined = false
        viewModel.onQrCodeScanned("   ") {
            joined = true
        }

        assertFalse(joined)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    private class FakeFamilyRepository : FamilyRepository {
        var lastAcceptedToken: String? = null
        var lastCreatedFamilyName: String? = null

        override fun observeFamily(): Flow<Family?> = flowOf(null)
        override fun observeChildren(familyId: String): Flow<List<Child>> = flowOf(emptyList())
        override fun observeChild(childId: String): Flow<Child?> = flowOf(null)
        override suspend fun getChild(childId: String): Child? = null
        override fun observeMembers(familyId: String): Flow<List<FamilyMember>> = flowOf(emptyList())
        override fun observeInvitations(familyId: String): Flow<List<FamilyInvitation>> = flowOf(emptyList())
        override fun observeDevices(childId: String): Flow<List<Device>> = flowOf(emptyList())
        override fun observeDevice(deviceId: String): Flow<Device?> = flowOf(null)
        override suspend fun getDevice(deviceId: String): Device? = null
        override suspend fun refreshFamily(familyId: String?): Result<Unit> = Result.success(Unit)
        override suspend fun checkHasFamily(): Boolean = false

        override suspend fun createFamily(name: String): Result<String> {
            lastCreatedFamilyName = name
            return Result.success("fam-123")
        }

        override suspend fun createChild(
            familyId: String,
            name: String,
            nickname: String?,
            dob: LocalDate?,
            avatarBytes: ByteArray?
        ): Result<Child> = Result.failure(NotImplementedError())

        override suspend fun updateChild(
            childId: String,
            name: String,
            nickname: String?,
            dob: LocalDate?,
            avatarBytes: ByteArray?
        ): Result<Unit> = Result.success(Unit)

        override suspend fun deleteChild(childId: String): Result<Unit> = Result.success(Unit)
        override suspend fun createPairingSession(childId: String): Result<PairingSession> =
            Result.success(PairingSession("s", "c", "t", "123", Instant.now()))
        override suspend fun unpairDevice(deviceId: String): Result<Unit> = Result.success(Unit)
        override suspend fun renameDevice(deviceId: String, newName: String): Result<Unit> = Result.success(Unit)
        override suspend fun inviteMember(email: String, role: FamilyRole): Result<Unit> = Result.success(Unit)
        override suspend fun cancelInvitation(invitationId: String): Result<Unit> = Result.success(Unit)

        override suspend fun acceptInvitation(token: String): Result<Unit> {
            lastAcceptedToken = token
            return Result.success(Unit)
        }

        override suspend fun updateMemberRole(memberId: String, newRole: FamilyRole): Result<Unit> = Result.success(Unit)
        override suspend fun removeMember(memberId: String): Result<Unit> = Result.success(Unit)
    }
}
