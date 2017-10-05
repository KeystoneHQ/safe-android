package pm.gnosis.heimdall.common.di.module

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import dagger.Module
import dagger.Provides
import pm.gnosis.heimdall.common.di.ForView
import pm.gnosis.heimdall.common.di.ViewContext
import pm.gnosis.heimdall.ui.account.AccountContract
import pm.gnosis.heimdall.ui.authenticate.AuthenticateContract
import pm.gnosis.heimdall.ui.onboarding.GenerateMnemonicContract
import pm.gnosis.heimdall.ui.security.SecurityContract

@Module
class ViewModule(val context: Context) {
    @Provides
    @ForView
    @ViewContext
    fun providesContext() = context

    @Provides
    @ForView
    fun providesAuthenticateContract(provider: ViewModelProvider) = provider[AuthenticateContract::class.java]!!

    @Provides
    @ForView
    fun providesAccountContract(provider: ViewModelProvider) = provider[AccountContract::class.java]!!

    @Provides
    @ForView
    fun providesGenerateMnemonicContract(provider: ViewModelProvider) = provider[GenerateMnemonicContract::class.java]!!

    @Provides
    @ForView
    fun providesSecurityContract(provider: ViewModelProvider) = provider[SecurityContract::class.java]!!

    @Provides
    @ForView
    fun providesViewModelProvider(factory: ViewModelProvider.Factory): ViewModelProvider {
        return when (context) {
            is Fragment -> ViewModelProviders.of(context, factory)
            is FragmentActivity -> ViewModelProviders.of(context, factory)
            else -> throw IllegalArgumentException("Unsupported context $context")
        }
    }
}