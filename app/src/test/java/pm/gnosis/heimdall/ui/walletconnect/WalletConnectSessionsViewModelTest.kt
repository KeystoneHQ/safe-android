package pm.gnosis.heimdall.ui.walletconnect

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import pm.gnosis.heimdall.data.repositories.BridgeRepository
import pm.gnosis.heimdall.ui.base.Adapter
import pm.gnosis.svalinn.common.utils.WhatTheFuck
import pm.gnosis.tests.utils.ImmediateSchedulersRule
import pm.gnosis.tests.utils.MockUtils
import pm.gnosis.tests.utils.TestCompletable
import java.util.*
import java.util.concurrent.BrokenBarrierException

@RunWith(MockitoJUnitRunner::class)
class WalletConnectSessionsViewModelTest {
    @JvmField
    @Rule
    val rule = ImmediateSchedulersRule()

    @Mock
    private lateinit var bridgeRepoMock: BridgeRepository

    private lateinit var viewModel: WalletConnectSessionsViewModel

    @Before
    fun setUp() {
        viewModel = WalletConnectSessionsViewModel(bridgeRepoMock)
    }

    @Test
    fun observeSession() {
        val eventId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()
        given(bridgeRepoMock.observeSession(MockUtils.any())).willReturn(Observable.just(BridgeRepository.SessionEvent.Closed(eventId)))
        val testObserver = TestObserver<BridgeRepository.SessionEvent>()
        viewModel.observeSession(sessionId).subscribe(testObserver)
        testObserver.assertResult(BridgeRepository.SessionEvent.Closed(eventId))
        then(bridgeRepoMock).should().observeSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun observeSessionError() {
        val sessionId = UUID.randomUUID().toString()
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.observeSession(MockUtils.any())).willReturn(Observable.error(error))
        val testObserver = TestObserver<BridgeRepository.SessionEvent>()
        viewModel.observeSession(sessionId).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().observeSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun approveSession() {
        val sessionId = UUID.randomUUID().toString()
        given(bridgeRepoMock.approveSession(MockUtils.any())).willReturn(Completable.complete())
        val testObserver = TestObserver<Unit>()
        viewModel.approveSession(sessionId).subscribe(testObserver)
        testObserver.assertComplete()
        then(bridgeRepoMock).should().approveSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun approveSessionError() {
        val sessionId = UUID.randomUUID().toString()
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.approveSession(MockUtils.any())).willReturn(Completable.error(error))
        val testObserver = TestObserver<Unit>()
        viewModel.approveSession(sessionId).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().approveSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun denySession() {
        val sessionId = UUID.randomUUID().toString()
        given(bridgeRepoMock.rejectSession(MockUtils.any())).willReturn(Completable.complete())
        val testObserver = TestObserver<Unit>()
        viewModel.denySession(sessionId).subscribe(testObserver)
        testObserver.assertComplete()
        then(bridgeRepoMock).should().rejectSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun denySessionError() {
        val sessionId = UUID.randomUUID().toString()
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.rejectSession(MockUtils.any())).willReturn(Completable.error(error))
        val testObserver = TestObserver<Unit>()
        viewModel.denySession(sessionId).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().rejectSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun killSession() {
        val sessionId = UUID.randomUUID().toString()
        given(bridgeRepoMock.closeSession(MockUtils.any())).willReturn(Completable.complete())
        val testObserver = TestObserver<Unit>()
        viewModel.killSession(sessionId).subscribe(testObserver)
        testObserver.assertComplete()
        then(bridgeRepoMock).should().closeSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun killSessionError() {
        val sessionId = UUID.randomUUID().toString()
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.closeSession(MockUtils.any())).willReturn(Completable.error(error))
        val testObserver = TestObserver<Unit>()
        viewModel.killSession(sessionId).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().closeSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun createSession() {
        val sessionId = UUID.randomUUID().toString()
        given(bridgeRepoMock.createSession(MockUtils.any())).willReturn(sessionId)
        given(bridgeRepoMock.initSession(MockUtils.any())).willReturn(Completable.complete())
        val url = "ws:someconfigparams@kkhfksddfjsgsh"
        val testObserver = TestObserver<Unit>()
        viewModel.createSession(url).subscribe(testObserver)
        testObserver.assertComplete()
        then(bridgeRepoMock).should().createSession(url)
        then(bridgeRepoMock).should().initSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun createSessionCreationError() {
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.createSession(MockUtils.any())).willThrow(error)
        val url = "ws:someconfigparams@kkhfksddfjsgsh"
        val testObserver = TestObserver<Unit>()
        viewModel.createSession(url).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().createSession(url)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun createSessionInitError() {
        val sessionId = UUID.randomUUID().toString()
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.createSession(MockUtils.any())).willReturn(sessionId)
        given(bridgeRepoMock.initSession(MockUtils.any())).willReturn(Completable.error(error))
        val url = "ws:someconfigparams@kkhfksddfjsgsh"
        val testObserver = TestObserver<Unit>()
        viewModel.createSession(url).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().createSession(url)
        then(bridgeRepoMock).should().initSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun activateSession() {
        val sessionId = UUID.randomUUID().toString()
        given(bridgeRepoMock.activateSession(MockUtils.any())).willReturn(Completable.complete())
        given(bridgeRepoMock.initSession(MockUtils.any())).willReturn(Completable.complete())
        val testObserver = TestObserver<Unit>()
        viewModel.activateSession(sessionId).subscribe(testObserver)
        testObserver.assertComplete()
        then(bridgeRepoMock).should().activateSession(sessionId)
        then(bridgeRepoMock).should().initSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun activateSessionActivateError() {
        val testCompletable = TestCompletable()
        val sessionId = UUID.randomUUID().toString()
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.activateSession(MockUtils.any())).willReturn(Completable.error(error))
        given(bridgeRepoMock.initSession(MockUtils.any())).willReturn(testCompletable)
        val testObserver = TestObserver<Unit>()
        viewModel.activateSession(sessionId).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().activateSession(sessionId)
        then(bridgeRepoMock).should().initSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
        Assert.assertEquals("Init Callable should not be triggered", 0, testCompletable.callCount)
    }

    @Test
    fun activateSessionInitError() {
        val sessionId = UUID.randomUUID().toString()
        val error = WhatTheFuck(BrokenBarrierException())
        given(bridgeRepoMock.activateSession(MockUtils.any())).willReturn(Completable.complete())
        given(bridgeRepoMock.initSession(MockUtils.any())).willReturn(Completable.error(error))
        val testObserver = TestObserver<Unit>()
        viewModel.activateSession(sessionId).subscribe(testObserver)
        testObserver.assertSubscribed().assertError(error).assertNoValues()
        then(bridgeRepoMock).should().activateSession(sessionId)
        then(bridgeRepoMock).should().initSession(sessionId)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }

    @Test
    fun observeSessions() {
        val metaSubject = PublishSubject.create<List<BridgeRepository.SessionMeta>>()
        given(bridgeRepoMock.observeSessions()).willReturn(metaSubject)
        val testObserver = TestObserver<Adapter.Data<BridgeRepository.SessionMeta>>()
        viewModel.observeSessions().subscribe(testObserver)
        then(bridgeRepoMock).should().observeSessions()
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
        testObserver
            .assertValueCount(1)
            .assertValueAt(0) {
                it.parentId == null &&
                        it.diff == null &&
                        it.entries == emptyList<BridgeRepository.SessionMeta>()
            }
        metaSubject.onNext(emptyList())
        testObserver
            .assertValueCount(2)
            .assertValueAt(1) {
                it.parentId == testObserver.values()[0].id &&
                        it.diff != null &&
                        it.entries == emptyList<BridgeRepository.SessionMeta>()
            }
        val initialValues = listOf(BridgeRepository.SessionMeta(UUID.randomUUID().toString(), null, null, null, false, null))
        metaSubject.onNext(initialValues)
        testObserver
            .assertValueCount(3)
            .assertValueAt(2) {
                it.parentId == testObserver.values()[1].id &&
                        it.diff != null &&
                        it.entries == initialValues
            }
        val updatedValues = listOf(
            initialValues.first(),
            BridgeRepository.SessionMeta(UUID.randomUUID().toString(), null, null, null, false, null)
        )
        metaSubject.onNext(updatedValues)
        testObserver
            .assertValueCount(4)
            .assertValueAt(3) {
                it.parentId == testObserver.values()[2].id &&
                        it.diff != null &&
                        it.entries == updatedValues
            }
        val error = WhatTheFuck(BrokenBarrierException())
        metaSubject.onError(error)
        testObserver
            .assertValueCount(4)
            .assertError(error)
        then(bridgeRepoMock).shouldHaveNoMoreInteractions()
    }
}