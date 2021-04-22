package io.gnosis.safe.ui.settings.owner

import io.gnosis.data.repositories.CredentialsRepository
import io.gnosis.safe.*
import io.gnosis.safe.notifications.NotificationRepository
import io.gnosis.safe.ui.base.BaseStateViewModel.ViewAction.*
import io.gnosis.safe.ui.settings.app.SettingsHandler
import io.mockk.*
import org.junit.Rule
import org.junit.Test
import pm.gnosis.model.Solidity
import java.math.BigInteger

class OwnerEnterNameViewModelTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val instantExecutorRule = TestLifecycleRule()

    private val credentialsRepository = mockk<CredentialsRepository>()
    private val notificationRepository = mockk<NotificationRepository>()
    private val settingsHandler = mockk<SettingsHandler>()
    private val tracker = mockk<Tracker>()

    private lateinit var viewModel: OwnerEnterNameViewModel

    @Test
    fun `importOwner (passcode was not setup) - should import owner and proceed to passcode setup`() {

        val ownerAddress = Solidity.Address(BigInteger.ZERO)
        val ownerKey = BigInteger.ONE
        val ownerName = "owner1"

        coEvery { credentialsRepository.saveOwner(any(), any(), any()) } just Runs
        coEvery { credentialsRepository.ownerCount() } returns 1
        coEvery { settingsHandler.showOwnerBanner = false } just Runs
        coEvery { settingsHandler.showOwnerScreen = false } just Runs
        coEvery { tracker.logKeyImported(any()) } just Runs
        coEvery { tracker.setNumKeysImported(any()) } just Runs
        coEvery { notificationRepository.registerOwners() } just Runs

        coEvery { settingsHandler.usePasscode } returns false

        viewModel = OwnerEnterNameViewModel(credentialsRepository, notificationRepository, settingsHandler, tracker, appDispatchers)
        val testObserver = TestLiveDataObserver<OwnerEnterNameState>()
        viewModel.state.observeForever(testObserver)

        viewModel.importOwner(ownerAddress, ownerName, ownerKey, true)

        testObserver.assertValues(
            OwnerEnterNameState(None),
            OwnerEnterNameState(NavigateTo(OwnerEnterNameFragmentDirections.actionOwnerEnterNameFragmentToCreatePasscodeFragment(true)))
        )

        coVerifySequence {
            credentialsRepository.saveOwner(any(), any(), any())
            settingsHandler.showOwnerBanner = false
            settingsHandler.showOwnerScreen = false
            tracker.logKeyImported(any())
            credentialsRepository.ownerCount()
            tracker.setNumKeysImported(any())
            notificationRepository.registerOwners()
            settingsHandler.usePasscode
        }
    }

    @Test
    fun `importOwner (passcode was setup) - should import owner and finish import owner flow`() {
        val ownerAddress = Solidity.Address(BigInteger.ZERO)
        val ownerKey = BigInteger.ONE
        val ownerName = "owner1"

        coEvery { credentialsRepository.saveOwner(any(), any(), any()) } just Runs
        coEvery { credentialsRepository.ownerCount() } returns 3
        coEvery { settingsHandler.showOwnerBanner = false } just Runs
        coEvery { settingsHandler.showOwnerScreen = false } just Runs
        coEvery { tracker.logKeyImported(any()) } just Runs
        coEvery { tracker.setNumKeysImported(any()) } just Runs
        coEvery { notificationRepository.registerOwners() } just Runs

        coEvery { settingsHandler.usePasscode } returns true

        viewModel = OwnerEnterNameViewModel(credentialsRepository, notificationRepository, settingsHandler, tracker, appDispatchers)
        val testObserver = TestLiveDataObserver<OwnerEnterNameState>()
        viewModel.state.observeForever(testObserver)

        viewModel.importOwner(ownerAddress, ownerName, ownerKey, true)

        testObserver.assertValues(
            OwnerEnterNameState(None),
            OwnerEnterNameState(CloseScreen)
        )

        coVerifySequence {
            credentialsRepository.saveOwner(any(), any(), any())
            settingsHandler.showOwnerBanner = false
            settingsHandler.showOwnerScreen = false
            tracker.logKeyImported(any())
            credentialsRepository.ownerCount()
            tracker.setNumKeysImported(3)
            notificationRepository.registerOwners()
            settingsHandler.usePasscode
        }
    }
}
