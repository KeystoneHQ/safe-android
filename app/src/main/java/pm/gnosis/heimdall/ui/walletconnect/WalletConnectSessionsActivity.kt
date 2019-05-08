package pm.gnosis.heimdall.ui.walletconnect


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.layout_wallet_connect_sessions.*
import pm.gnosis.heimdall.R
import pm.gnosis.heimdall.di.components.ViewComponent
import pm.gnosis.heimdall.reporting.ScreenId
import pm.gnosis.heimdall.ui.base.ViewModelActivity
import pm.gnosis.heimdall.ui.qrscan.QRCodeScanActivity
import pm.gnosis.heimdall.utils.handleQrCodeActivityResult
import timber.log.Timber
import javax.inject.Inject

class WalletConnectSessionsActivity : ViewModelActivity<WalletConnectSessionsContract>() {

    @Inject
    lateinit var adapter: WalletConnectSessionsAdapter

    override fun screenId() = ScreenId.WALLET_CONNECT_SESSIONS

    override fun layout() = R.layout.layout_wallet_connect_sessions

    override fun inject(component: ViewComponent) = component.inject(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout_wallet_connect_sessions_recycler_view.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        layout_wallet_connect_sessions_recycler_view.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!handleQrCodeActivityResult(requestCode, resultCode, data, {
                disposables += viewModel.createSession(it).subscribeBy(onError = Timber::e)
            })) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStart() {
        super.onStart()

        disposables += layout_wallet_connect_sessions_back_arrow.clicks()
            .subscribeBy { onBackPressed() }

        disposables += viewModel.observeSessions().observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = adapter::updateData, onError = Timber::e)

        disposables += layout_wallet_connect_sessions_add.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { QRCodeScanActivity.startForResult(this) }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, WalletConnectSessionsActivity::class.java)
    }
}