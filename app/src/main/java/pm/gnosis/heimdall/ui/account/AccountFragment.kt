package pm.gnosis.heimdall.ui.account

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.dialog_show_qr_code.view.*
import kotlinx.android.synthetic.main.layout_account.*
import pm.gnosis.heimdall.R
import pm.gnosis.heimdall.common.di.component.ApplicationComponent
import pm.gnosis.heimdall.common.di.component.DaggerViewComponent
import pm.gnosis.heimdall.common.di.module.ViewModule
import pm.gnosis.heimdall.common.util.copyToClipboard
import pm.gnosis.heimdall.common.util.shareExternalText
import pm.gnosis.heimdall.common.util.snackbar
import pm.gnosis.heimdall.common.util.subscribeForResult
import pm.gnosis.heimdall.data.model.Wei
import pm.gnosis.heimdall.ui.base.BaseFragment
import pm.gnosis.heimdall.utils.errorSnackbar
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AccountFragment : BaseFragment() {
    @Inject
    lateinit var viewModel: AccountContract

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.layout_account, container, false)
    }

    override fun onStart() {
        super.onStart()
        disposables += accountAddress()

        layout_account_clipboard.setOnClickListener {
            context.copyToClipboard("address", layout_account_address.text.toString())
            snackbar(layout_account_coordinator_layout, "Address copied to clipboard")
        }

        layout_account_share.setOnClickListener {
            context.shareExternalText(layout_account_address.text.toString(), "Share your address")
        }

        // We delay the initial value, else the progress bar is not visible
        disposables += layout_account_refresh_layout.refreshes()
                .subscribeOn(AndroidSchedulers.mainThread())
                .startWith(Observable.just(Unit).delay(100, TimeUnit.MILLISECONDS))
                // We need to switch the observing scheduler here because `delay` forces the
                // computation scheduler and we need the ui thread scheduler when subscribing in the
                // flap map, else the loading spinner is triggered on the wrong thread
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { accountBalance() }
                .subscribeForResult(this::onAccountBalance, this::handleError)

        disposables += layout_account_qrcode.clicks()
                .flatMap { generateQrCode(layout_account_address.text.toString()) }
                .subscribeForResult(this::onQrCode, this::handleError)
    }

    private fun accountAddress() = viewModel.getAccountAddress()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeForResult({
                layout_account_address.text = it.address
            }, this::handleError)

    private fun accountBalance() = viewModel.getAccountBalance()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onAccountBalanceLoading(true) }
            .doOnTerminate { onAccountBalanceLoading(false) }

    private fun generateQrCode(contents: String) = viewModel.getQrCode(contents)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onQrCodeLoading(true) }
            .doAfterTerminate { onQrCodeLoading(false) }

    private fun onAccountBalance(wei: Wei) {
        val etherBalance = wei.toEther()
        layout_account_balance.text = if (etherBalance.compareTo(BigDecimal.ZERO) == 0) "0 Ξ" else etherBalance.stripTrailingZeros().toPlainString() + " Ξ"
    }

    private fun onAccountBalanceLoading(isLoading: Boolean) {
        layout_account_refresh_layout.isRefreshing = isLoading
    }

    private fun onQrCode(bitmap: Bitmap) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_show_qr_code, null)
        dialogView.dialog_qr_code_image.setImageBitmap(bitmap)
        AlertDialog.Builder(context)
                .setView(dialogView)
                .show()
    }

    private fun onQrCodeLoading(isLoading: Boolean) {
        layout_account_progress_bar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }

    private fun handleError(throwable: Throwable) {
        errorSnackbar(layout_account_coordinator_layout, throwable)
    }

    override fun inject(component: ApplicationComponent) {
        DaggerViewComponent.builder()
                .applicationComponent(component)
                .viewModule(ViewModule(this.context))
                .build()
                .inject(this)
    }
}