package app.guardian.android.feature.family.data.mapper

import app.guardian.android.core.common.DateTimeUtils
import app.guardian.android.core.model.ChildStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.InvitationStatus
import app.guardian.android.core.model.ProtectionStatus
import app.guardian.android.feature.family.data.local.entity.ChildEntity
import app.guardian.android.feature.family.data.local.entity.DeviceEntity
import app.guardian.android.feature.family.data.local.entity.DevicePermissionStatusEntity
import app.guardian.android.feature.family.data.local.entity.FamilyEntity
import app.guardian.android.feature.family.data.local.entity.FamilyMemberEntity
import app.guardian.android.feature.family.data.local.entity.InvitationEntity
import app.guardian.android.feature.family.data.remote.dto.ChildDto
import app.guardian.android.feature.family.data.remote.dto.CreatePairingResponse
import app.guardian.android.feature.family.data.remote.dto.DeviceDto
import app.guardian.android.feature.family.data.remote.dto.DevicePermissionStatusDto
import app.guardian.android.feature.family.data.remote.dto.FamilyDto
import app.guardian.android.feature.family.data.remote.dto.FamilyMemberDto
import app.guardian.android.feature.family.data.remote.dto.InvitationDto
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.model.DevicePermissionStatus
import app.guardian.android.feature.family.domain.model.Family
import app.guardian.android.feature.family.domain.model.FamilyInvitation
import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.model.PairingSession
import java.time.Instant

// --- Family Mappers ---
fun FamilyDto.toEntity(): FamilyEntity = FamilyEntity(
    id = id,
    name = name,
    ownerUserId = ownerUserId,
    createdAt = DateTimeUtils.parseInstant(createdAt),
    updatedAt = DateTimeUtils.parseInstant(updatedAt),
    deletedAt = DateTimeUtils.parseInstant(deletedAt)
)

fun FamilyEntity.toDomain(): Family = Family(
    id = id,
    name = name,
    ownerUserId = ownerUserId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FamilyDto.toDomain(): Family = Family(
    id = id,
    name = name,
    ownerUserId = ownerUserId,
    createdAt = DateTimeUtils.parseInstant(createdAt),
    updatedAt = DateTimeUtils.parseInstant(updatedAt)
)

// --- Child Mappers ---
fun ChildDto.toEntity(): ChildEntity = ChildEntity(
    id = id,
    familyId = familyId,
    name = name,
    nickname = nickname,
    dateOfBirth = DateTimeUtils.parseLocalDate(dateOfBirth),
    avatarPath = avatarPath,
    status = runCatching { ChildStatus.valueOf(status) }.getOrDefault(ChildStatus.ACTIVE),
    createdBy = createdBy,
    createdAt = DateTimeUtils.parseInstant(createdAt),
    updatedAt = DateTimeUtils.parseInstant(updatedAt),
    archivedAt = DateTimeUtils.parseInstant(archivedAt),
    deletedAt = DateTimeUtils.parseInstant(deletedAt)
)

fun ChildEntity.toDomain(): Child = Child(
    id = id,
    familyId = familyId,
    name = name,
    nickname = nickname,
    dateOfBirth = dateOfBirth,
    avatarPath = avatarPath,
    status = status,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ChildDto.toDomain(): Child = Child(
    id = id,
    familyId = familyId,
    name = name,
    nickname = nickname,
    dateOfBirth = DateTimeUtils.parseLocalDate(dateOfBirth),
    avatarPath = avatarPath,
    status = runCatching { ChildStatus.valueOf(status) }.getOrDefault(ChildStatus.ACTIVE),
    createdBy = createdBy,
    createdAt = DateTimeUtils.parseInstant(createdAt),
    updatedAt = DateTimeUtils.parseInstant(updatedAt)
)

// --- Family Member Mappers ---
fun FamilyMemberDto.toEntity(): FamilyMemberEntity = FamilyMemberEntity(
    id = id,
    familyId = familyId,
    userId = userId,
    role = runCatching { FamilyRole.valueOf(role) }.getOrDefault(FamilyRole.VIEWER),
    email = email,
    joinedAt = DateTimeUtils.parseInstant(joinedAt),
    createdAt = DateTimeUtils.parseInstant(createdAt)
)

fun FamilyMemberEntity.toDomain(): FamilyMember = FamilyMember(
    id = id,
    familyId = familyId,
    userId = userId,
    role = role,
    email = email,
    joinedAt = joinedAt,
    createdAt = createdAt
)

fun FamilyMemberDto.toDomain(): FamilyMember = FamilyMember(
    id = id,
    familyId = familyId,
    userId = userId,
    role = runCatching { FamilyRole.valueOf(role) }.getOrDefault(FamilyRole.VIEWER),
    email = email,
    joinedAt = DateTimeUtils.parseInstant(joinedAt),
    createdAt = DateTimeUtils.parseInstant(createdAt)
)

// --- Device Mappers ---
fun DeviceDto.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    familyId = familyId,
    childId = childId,
    authUserId = authUserId,
    name = name,
    manufacturer = manufacturer,
    model = model,
    androidVersion = androidVersion,
    apiLevel = apiLevel,
    appVersion = appVersion,
    appBuild = appBuild,
    batteryLevel = batteryLevel,
    charging = charging,
    protectionStatus = runCatching { ProtectionStatus.valueOf(protectionStatus) }.getOrDefault(ProtectionStatus.ACTIVE),
    lastSeenAt = DateTimeUtils.parseInstant(lastSeenAt),
    pairedAt = DateTimeUtils.parseInstant(pairedAt),
    unpairedAt = DateTimeUtils.parseInstant(unpairedAt),
    createdAt = DateTimeUtils.parseInstant(createdAt),
    updatedAt = DateTimeUtils.parseInstant(updatedAt)
)

fun DeviceEntity.toDomain(permissionStatus: DevicePermissionStatus? = null): Device = Device(
    id = id,
    familyId = familyId,
    childId = childId,
    authUserId = authUserId,
    name = name,
    manufacturer = manufacturer,
    model = model,
    androidVersion = androidVersion,
    apiLevel = apiLevel,
    appVersion = appVersion,
    appBuild = appBuild,
    batteryLevel = batteryLevel,
    charging = charging,
    protectionStatus = protectionStatus,
    lastSeenAt = lastSeenAt,
    pairedAt = pairedAt,
    permissionStatus = permissionStatus
)

fun DevicePermissionStatusDto.toEntity(): DevicePermissionStatusEntity = DevicePermissionStatusEntity(
    deviceId = deviceId,
    usageAccess = usageAccess,
    accessibilityService = accessibilityService,
    notificationPermission = notificationPermission,
    vpnActive = vpnActive,
    locationPermission = locationPermission,
    deviceAdmin = deviceAdmin,
    updatedAt = DateTimeUtils.parseInstant(updatedAt)
)

fun DevicePermissionStatusEntity.toDomain(): DevicePermissionStatus = DevicePermissionStatus(
    deviceId = deviceId,
    usageAccess = usageAccess,
    accessibilityService = accessibilityService,
    notificationPermission = notificationPermission,
    vpnActive = vpnActive,
    locationPermission = locationPermission,
    deviceAdmin = deviceAdmin,
    updatedAt = updatedAt
)

// --- Invitation Mappers ---
fun InvitationDto.toEntity(): InvitationEntity = InvitationEntity(
    id = id,
    familyId = familyId,
    email = email,
    role = runCatching { FamilyRole.valueOf(role) }.getOrDefault(FamilyRole.VIEWER),
    tokenHash = tokenHash,
    status = runCatching { InvitationStatus.valueOf(status) }.getOrDefault(InvitationStatus.PENDING),
    expiresAt = DateTimeUtils.parseInstant(expiresAt) ?: Instant.now(),
    invitedBy = invitedBy,
    createdAt = DateTimeUtils.parseInstant(createdAt) ?: Instant.now(),
    acceptedBy = acceptedBy,
    acceptedAt = DateTimeUtils.parseInstant(acceptedAt),
    cancelledAt = DateTimeUtils.parseInstant(cancelledAt)
)

fun InvitationEntity.toDomain(): FamilyInvitation = FamilyInvitation(
    id = id,
    familyId = familyId,
    email = email,
    role = role,
    status = status,
    expiresAt = expiresAt,
    invitedBy = invitedBy,
    createdAt = createdAt,
    acceptedBy = acceptedBy,
    acceptedAt = acceptedAt
)

// --- Pairing Mappers ---
fun CreatePairingResponse.toDomain(childId: String): PairingSession = PairingSession(
    sessionId = pairingSessionId,
    childId = childId,
    pairingToken = pairingToken,
    manualCode = manualCode,
    expiresAt = DateTimeUtils.parseInstant(expiresAt) ?: Instant.now()
)
