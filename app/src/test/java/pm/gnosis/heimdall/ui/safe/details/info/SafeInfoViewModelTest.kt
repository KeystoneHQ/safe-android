package pm.gnosis.heimdall.ui.safe.details.info

import android.content.Context
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import pm.gnosis.heimdall.common.utils.DataResult
import pm.gnosis.heimdall.common.utils.ErrorResult
import pm.gnosis.heimdall.common.utils.Result
import pm.gnosis.heimdall.data.repositories.GnosisSafeRepository
import pm.gnosis.heimdall.data.repositories.models.SafeInfo
import pm.gnosis.heimdall.test.utils.ImmediateSchedulersRule
import pm.gnosis.heimdall.test.utils.MockUtils
import pm.gnosis.models.Wei
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class SafeInfoViewModelTest {
    @JvmField
    @Rule
    val rule = ImmediateSchedulersRule()

    @Mock
    lateinit var contextMock: Context

    @Mock
    lateinit var repositoryMock: GnosisSafeRepository

    lateinit var viewModel: SafeInfoViewModel

    @Before
    fun setup() {
        viewModel = SafeInfoViewModel(contextMock, repositoryMock)
    }

    private fun callSetupAndCheck(address: BigInteger, info: SafeInfo,
                                  repositoryInvocations: Int = 1, totalInvocations: Int = repositoryInvocations,
                                  ignoreCached: Boolean = false) {
        // Setup with address
        viewModel.setup(address)

        // Verify that the repositoryMock is called and expected version is returned
        val observer = TestObserver.create<Result<SafeInfo>>()
        viewModel.loadSafeInfo(ignoreCached).subscribe(observer)
        observer.assertNoErrors().assertValueCount(1).assertValue(DataResult(info))
        Mockito.verify(repositoryMock, Mockito.times(repositoryInvocations)).loadInfo(address)
        Mockito.verify(repositoryMock, Mockito.times(totalInvocations)).loadInfo(MockUtils.any())
    }

    @Test
    fun setupViewModelClearCache() {
        val address1 = BigInteger.ZERO
        val info1 = SafeInfo("Test1", Wei(BigInteger.ONE), 0, emptyList())
        given(repositoryMock.loadInfo(address1)).willReturn(Observable.just(info1))

        val address2 = BigInteger.ONE
        val info2 = SafeInfo("Test2", Wei(BigInteger.ONE), 0, emptyList())
        given(repositoryMock.loadInfo(address2)).willReturn(Observable.just(info2))

        callSetupAndCheck(address1, info1)

        callSetupAndCheck(address2, info2, totalInvocations = 2)
    }

    @Test
    fun setupViewModelKeepCache() {
        val address = BigInteger.ZERO
        val info = SafeInfo("Test", Wei(BigInteger.ONE), 0, emptyList())
        given(repositoryMock.loadInfo(MockUtils.any())).willReturn(Observable.just(info))

        callSetupAndCheck(address, info)

        callSetupAndCheck(address, info)
    }

    @Test
    fun loadSafeInfoIgnoreCache() {
        val address = BigInteger.ZERO
        val info = SafeInfo("Test", Wei(BigInteger.ONE), 0, emptyList())
        given(repositoryMock.loadInfo(address)).willReturn(Observable.just(info))

        callSetupAndCheck(address, info)

        callSetupAndCheck(address, info, 2, ignoreCached = true)
    }

    @Test
    fun loadSafeInfoError() {
        viewModel.setup(BigInteger.ZERO)

        val exception = IllegalStateException("test")
        given(repositoryMock.loadInfo(MockUtils.any())).willReturn(Observable.error(exception))

        val observer = TestObserver.create<Result<SafeInfo>>()
        viewModel.loadSafeInfo(true).subscribe(observer)
        observer.assertNoErrors().assertValueCount(1).assertValue(ErrorResult(exception))
    }
}