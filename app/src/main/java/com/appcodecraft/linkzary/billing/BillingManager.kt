package com.appcodecraft.linkzary.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor() : PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    
    private val _billingConnectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    // Donation SKUs from $1 to $30
    private val donationSkus = listOf(
        "donation_1_dollar",
        "donation_2_dollar", 
        "donation_3_dollar",
        "donation_5_dollar",
        "donation_10_dollar",
        "donation_15_dollar",
        "donation_20_dollar",
        "donation_25_dollar",
        "donation_30_dollar"
    )
    
    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()
    
    fun initializeBilling(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        connectToBillingService()
    }
    
    private fun connectToBillingService() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingConnectionState.value = BillingConnectionState.CONNECTED
                    queryAvailableProducts()
                } else {
                    _billingConnectionState.value = BillingConnectionState.ERROR
                }
            }
            
            override fun onBillingServiceDisconnected() {
                _billingConnectionState.value = BillingConnectionState.DISCONNECTED
            }
        })
    }
    
    private fun queryAvailableProducts() {
        val productList = donationSkus.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _availableProducts.value = productDetailsList
            }
        }
    }
    
    fun launchDonationFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        _purchaseState.value = PurchaseState.Loading
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _purchaseState.value = PurchaseState.Success
            
            // Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    // Handle acknowledgment result if needed
                }
            }
        }
    }
    
    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }
    
    fun endConnection() {
        billingClient?.endConnection()
        _billingConnectionState.value = BillingConnectionState.DISCONNECTED
    }
}

enum class BillingConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}