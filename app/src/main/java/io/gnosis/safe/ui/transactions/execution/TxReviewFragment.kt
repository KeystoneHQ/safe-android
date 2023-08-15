package io.gnosis.safe.ui.transactions.execution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewbinding.ViewBinding
import io.gnosis.data.models.transaction.DetailedExecutionInfo
import io.gnosis.data.models.transaction.Param
import io.gnosis.data.models.transaction.TransferInfo
import io.gnosis.data.models.transaction.symbol
import io.gnosis.safe.R
import io.gnosis.safe.ScreenId
import io.gnosis.safe.databinding.FragmentTxReviewBinding
import io.gnosis.safe.databinding.TxReviewCustomBinding
import io.gnosis.safe.databinding.TxReviewRejectionBinding
import io.gnosis.safe.databinding.TxReviewSettingsChangeBinding
import io.gnosis.safe.databinding.TxReviewTransferBinding
import io.gnosis.safe.di.components.ViewComponent
import io.gnosis.safe.ui.base.BaseStateViewModel.ViewAction.Loading
import io.gnosis.safe.ui.base.BaseStateViewModel.ViewAction.ShowError
import io.gnosis.safe.ui.base.SafeOverviewBaseFragment
import io.gnosis.safe.ui.base.fragment.BaseViewBindingFragment
import io.gnosis.safe.ui.transactions.details.SigningMode
import io.gnosis.safe.ui.transactions.details.TransactionDetailsFragmentDirections
import io.gnosis.safe.ui.transactions.details.viewdata.TransactionInfoViewData
import io.gnosis.safe.utils.BalanceFormatter
import io.gnosis.safe.utils.ParamSerializer
import io.gnosis.safe.utils.formattedAmount
import io.gnosis.safe.utils.logoUri
import io.gnosis.safe.utils.setLink
import io.gnosis.safe.utils.toColor
import io.gnosis.safe.utils.txActionInfoItems
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.visible
import pm.gnosis.utils.asEthereumAddress
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class TxReviewFragment : BaseViewBindingFragment<FragmentTxReviewBinding>() {

    override fun screenId() = ScreenId.TRANSACTIONS_EXEC_REVIEW

    private val navArgs by navArgs<TxReviewFragmentArgs>()
    private val chain by lazy { navArgs.chain }
    private val txDetails by lazy { navArgs.txDetails }

    @Inject
    lateinit var paramSerializer: ParamSerializer

    @Inject
    lateinit var balanceFormatter: BalanceFormatter

    @Inject
    lateinit var viewModel: TxReviewViewModel

    private lateinit var contentBinding: ViewBinding

    override fun inject(component: ViewComponent) {
        component.inject(this)
    }
    override fun viewModelProvider() = this

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTxReviewBinding =
        FragmentTxReviewBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
            chainRibbon.text = chain.name
            chainRibbon.setTextColor(
                chain.textColor.toColor(
                    requireContext(),
                    R.color.white
                )
            )
            chainRibbon.setBackgroundColor(
                chain.backgroundColor.toColor(
                    requireContext(),
                    R.color.primary
                )
            )

            when (val txInfo = txDetails!!.txInfo) {

                is TransactionInfoViewData.Transfer -> {
                    val viewStub = binding.stubTransfer
                    if (viewStub.parent != null) {
                        val inflate = viewStub.inflate()
                        contentBinding = TxReviewTransferBinding.bind(inflate)
                    }
                    val transferBinding = contentBinding as TxReviewTransferBinding
                    val amount = txDetails!!.txData?.value ?: BigInteger.ZERO
                    val amountDecimal = amount.toBigDecimal().divide(BigDecimal.TEN.pow(chain.currency.decimals)).toPlainString()
                    with(transferBinding) {
                        when (txInfo.transferInfo) {
                            is TransferInfo.Erc20Transfer -> {
                                transferAmount.setAmount(
                                    amountDecimal = amountDecimal,
                                    txInfo.transferInfo.symbol(chain)!!,
                                    txInfo.transferInfo.logoUri
                                )
                            }
                            is TransferInfo.Erc721Transfer -> {
                                transferAmount.setAmount(
                                    amountDecimal = BigDecimal.ONE.toPlainString(),
                                    txInfo.transferInfo.symbol(chain)!!,
                                    txInfo.transferInfo.logoUri
                                )
                            }
                            is TransferInfo.NativeTransfer -> {
                                transferAmount.setAmount(
                                    amountDecimal = amountDecimal,
                                    chain.currency.symbol,
                                    chain.currency.logoUri
                                )
                            }
                        }
                        fromAddressItem.name = viewModel.activeSafe.localName
                        fromAddressItem.setAddress(
                            chain = chain,
                            value = viewModel.activeSafe.address,
                            showChainPrefix = viewModel.isChainPrefixPrependEnabled(),
                            copyChainPrefix = viewModel.isChainPrefixCopyEnabled()
                        )
                        txInfo.addressName?.let {
                            toAddressItemKnown.visible(true)
                            toAddressItem.visible(false)
                            toAddressItemKnown.name = it
                            toAddressItemKnown.setAddress(
                                chain = chain,
                                value = txInfo.address,
                                showChainPrefix = viewModel.isChainPrefixPrependEnabled(),
                                copyChainPrefix = viewModel.isChainPrefixCopyEnabled()
                            )
                            if (!txInfo.addressUri.isNullOrBlank()) {
                                toAddressItemKnown.loadKnownAddressLogo(txInfo.addressUri, txInfo.address)
                            }
                        } ?: run {
                            toAddressItemKnown.visible(false)
                            toAddressItem.visible(true)
                            toAddressItem.setAddress(
                                chain = chain,
                                value = txInfo.address,
                                showChainPrefix = viewModel.isChainPrefixPrependEnabled(),
                                copyChainPrefix = viewModel.isChainPrefixCopyEnabled()
                            )
                        }
                    }
                }

                is TransactionInfoViewData.SettingsChange -> {
                    val viewStub = binding.stubSettingsChange
                    if (viewStub.parent != null) {
                        val inflate = viewStub.inflate()
                        contentBinding = TxReviewSettingsChangeBinding.bind(inflate)
                    }
                    val settingsChangeBinding = contentBinding as TxReviewSettingsChangeBinding
                    with(settingsChangeBinding) {
                        txAction.setActionInfoItems(
                            chain = chain,
                            showChainPrefix = viewModel.isChainPrefixPrependEnabled(),
                            copyChainPrefix = viewModel.isChainPrefixCopyEnabled(),
                            actionInfoItems = txInfo.txActionInfoItems(requireContext().resources)
                        )
                    }
                }

                is TransactionInfoViewData.Custom -> {
                    val viewStub = binding.stubCustom
                    if (viewStub.parent != null) {
                        val inflate = viewStub.inflate()
                        contentBinding = TxReviewCustomBinding.bind(inflate)
                    }
                    val customBinding = contentBinding as TxReviewCustomBinding
                    with(customBinding) {
                        txAction.setActionInfo(
                            chain = chain,
                            outgoing = true,
                            amount = txInfo.formattedAmount(chain, balanceFormatter),
                            logoUri = txInfo.logoUri(chain) ?: "",
                            address = txInfo.to,
                            showChainPrefix = viewModel.isChainPrefixPrependEnabled(),
                            copyChainPrefix = viewModel.isChainPrefixCopyEnabled(),
                            addressUri = txInfo.actionInfoAddressUri,
                            addressName = txInfo.actionInfoAddressName
                        )
                        val decodedData = txDetails!!.txData?.dataDecoded
                        if (decodedData == null) {
                            txDataDecoded.visible(false)
                            txDataDecodedSeparator.visible(false)
                        } else {

                            if (decodedData.method.lowercase() == "multisend") {

                                val valueDecoded = (decodedData.parameters?.get(0) as Param.Bytes).valueDecoded

                                txDataDecoded.name = getString(R.string.tx_details_action_multisend, valueDecoded?.size ?: 0)
                                txDataDecoded.setOnClickListener {
                                    txDetails!!.txData?.dataDecoded?.parameters?.getOrNull(0)?.let { param ->
                                        if (param is Param.Bytes && param.valueDecoded != null) {
                                            findNavController().navigate(
                                                //TODO adjust direction
                                                TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToTransactionDetailsActionMultisendFragment(
                                                    chain,
                                                    paramSerializer.serializeDecodedValues(param.valueDecoded!!),
                                                    paramSerializer.serializeAddressInfoIndex(txDetails!!.txData?.addressInfoIndex)
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {

                                txDataDecoded.name = getString(R.string.tx_details_action, txDetails!!.txData?.dataDecoded?.method)
                                txDataDecoded.setOnClickListener {
                                    txDetails!!.txData?.let {
                                        findNavController().navigate(
                                            //TODO adjust direction
                                            TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToTransactionDetailsActionFragment(
                                                chain = chain,
                                                action = it.dataDecoded?.method ?: "",
                                                data = it.hexData ?: "",
                                                decodedData = it.dataDecoded?.let { paramSerializer.serializeDecodedData(it) },
                                                addressInfoIndex = paramSerializer.serializeAddressInfoIndex(it.addressInfoIndex)
                                            )
                                        )
                                    }
                                }

                            }
                        }

                        txData.setData(txDetails!!.txData?.hexData, txInfo.dataSize, getString(R.string.tx_details_data))
                    }
                }

                is TransactionInfoViewData.Rejection -> {
                    val viewStub = binding.stubRejection
                    if (viewStub.parent != null) {
                        val inflate = viewStub.inflate()
                        contentBinding = TxReviewRejectionBinding.bind(inflate)
                    }
                    val rejectionBinding = contentBinding as TxReviewRejectionBinding
                    with(rejectionBinding) {
                        when (val executionInfo = txDetails!!.detailedExecutionInfo) {
                            is DetailedExecutionInfo.MultisigExecutionDetails -> {
                                txRejectionInfo.text = getString(R.string.tx_details_rejection_info_queued, executionInfo.nonce)
                                txPaymentReasonLink.setLink(
                                    url = getString(R.string.tx_details_rejection_payment_reason_link),
                                    urlText = getString(R.string.tx_details_rejection_payment_reason),
                                    linkIcon = R.drawable.ic_external_link_green_16dp,
                                    underline = true
                                )
                            }
                            else -> {
                            }
                        }
                    }
                }
            }

            estimatedFee.setOnClickListener {
                //TODO: check tx type, save user input
                viewModel.minNonce?.let {
                    findNavController().navigate(
                        TxReviewFragmentDirections.actionTxReviewFragmentToTxEditFee1559Fragment(
                            chain = chain,
                            nonce = viewModel.minNonce.toString(),
                            minNonce = viewModel.nonce.toString(),
                            gasLimit = viewModel.gasLimit.toString(),
                            maxPriorityFee = viewModel.maxPriorityFeePerGas?.toPlainString() ?: "0",
                            maxFee = viewModel.maxFeePerGas?.toPlainString() ?: "0",
                        )
                    )
                }
            }
            selectKey.setOnClickListener {
                findNavController().navigate(
                    TxReviewFragmentDirections.actionTxReviewFragmentToSigningOwnerSelectionFragment(
                        missingSigners = null,
                        signingMode = SigningMode.EXECUTION,
                        chain = chain
                    )
                )
            }
            reviewAdvanced.setOnClickListener {
                findNavController().navigate(
                    TxReviewFragmentDirections.actionTxReviewFragmentToTxAdvancedParamsFragment(
                        chain = chain,
                        hash = txDetails!!.txHash,
                        data = paramSerializer.serializeData(txDetails!!.txData),
                        executionInfo = paramSerializer.serializeExecutionInfo(txDetails!!.detailedExecutionInfo)
                    )
                )
            }
            refresh.setOnRefreshListener {
                loadEstimation()
            }
        }

        setFragmentResultListener(TxEditFee1559Fragment.REQUEST_EDIT_FEE) { requestKey, bundle ->
            val nonce = bundle.getString(TxEditFee1559Fragment.RESULT_NONCE)!!
            val gasLimit = bundle.getString(TxEditFee1559Fragment.RESULT_GAS_LIMIT)!!
            val maxPriorityFee = bundle.getString(TxEditFee1559Fragment.RESULT_MAX_PRIORITY_FEE)!!
            val maxFee = bundle.getString(TxEditFee1559Fragment.RESULT_MAX_FEE)!!
            viewModel.updateEstimationParams(
                nonce = nonce.toBigInteger(),
                gasLimit = gasLimit.toBigInteger(),
                maxPriorityFeePerGas = maxPriorityFee.toBigDecimal().stripTrailingZeros(),
                maxFeePerGas = maxFee.toBigDecimal().stripTrailingZeros()
            )
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TxReviewState -> {
                    state.viewAction?.let { action ->
                        when (action) {
                            is Loading -> {
                                binding.refresh.isRefreshing = action.isLoading
                            }
                            is DefaultKey -> {
                                with(binding) {
                                    selectKey.setKey(action.key, action.key?.balance)
                                }
                            }
                            is UpdateFee -> {
                                binding.estimatedFee.value = action.fee
                                binding.refresh.isRefreshing = false
                            }
                            is ShowError -> {
                                binding.refresh.isRefreshing = false
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ownerSelected() != null) {
            viewModel.updateDefaultKey(ownerSelected()!!)
            resetOwnerData()
        }
        loadEstimation()
    }

    private fun loadEstimation() {
        if (!viewModel.isLoading()) {
            txDetails?.let {
                viewModel.estimate(it.txData!!, it.detailedExecutionInfo!!)
            }
        }
    }

    private fun ownerSelected(): Solidity.Address? {
        return findNavController().currentBackStackEntry?.savedStateHandle?.get<String>(
            SafeOverviewBaseFragment.OWNER_SELECTED_RESULT
        )
            ?.asEthereumAddress()
    }

    private fun ownerSigned(): String? {
        return findNavController().currentBackStackEntry?.savedStateHandle?.get<String>(
            SafeOverviewBaseFragment.OWNER_SIGNED_RESULT
        )
    }

    private fun resetOwnerData() {
        findNavController().currentBackStackEntry?.savedStateHandle?.set(
            SafeOverviewBaseFragment.OWNER_SELECTED_RESULT,
            null
        )
        findNavController().currentBackStackEntry?.savedStateHandle?.set(
            SafeOverviewBaseFragment.OWNER_SIGNED_RESULT,
            null
        )
    }
}
