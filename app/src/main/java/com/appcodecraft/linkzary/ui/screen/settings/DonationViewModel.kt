package com.appcodecraft.linkzary.ui.screen.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.appcodecraft.linkzary.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DonationViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {
    
    val billingConnectionState: StateFlow<com.appcodecraft.linkzary.billing.BillingConnectionState> = 
        billingManager.billingConnectionState
    
    val availableProducts: StateFlow<List<ProductDetails>> = 
        billingManager.availableProducts
    
    val purchaseState: StateFlow<com.appcodecraft.linkzary.billing.PurchaseState> = 
        billingManager.purchaseState
    
    fun initializeBilling(context: Context) {
        billingManager.initializeBilling(context)
    }
    
    fun launchDonationFlow(context: Context, productDetails: ProductDetails) {
        if (context is Activity) {
            billingManager.launchDonationFlow(context, productDetails)
        }
    }
    
    fun resetPurchaseState() {
        billingManager.resetPurchaseState()
    }
    
    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}