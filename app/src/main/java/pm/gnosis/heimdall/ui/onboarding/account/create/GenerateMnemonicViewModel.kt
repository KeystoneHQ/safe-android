package pm.gnosis.heimdall.ui.onboarding.account.create

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import pm.gnosis.heimdall.accounts.base.repositories.AccountsRepository
import pm.gnosis.heimdall.common.utils.Result
import pm.gnosis.heimdall.common.utils.mapToResult
import javax.inject.Inject


class GenerateMnemonicViewModel @Inject constructor(private val accountsRepository: AccountsRepository) : GenerateMnemonicContract() {
    override fun generateMnemonic(): Single<Result<String>> =
            accountsRepository.generateMnemonic()
                    .mapToResult()
                    .subscribeOn(Schedulers.io())

    override fun saveAccountWithMnemonic(mnemonic: String): Single<Result<Unit>> =
            accountsRepository.saveAccountFromMnemonic(mnemonic)
                    .andThen(accountsRepository.saveMnemonic(mnemonic))
                    .andThen(Single.just(Unit))
                    .mapToResult()
                    .subscribeOn(Schedulers.io())
}