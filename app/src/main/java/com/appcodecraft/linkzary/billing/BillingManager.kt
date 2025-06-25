package com.appcodecraft.linkzary.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager

@Singleton
class BillingManager @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : PurchasesUpdatedListener {
    
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
                    queryExistingPurchases()
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
                // Filter out products that don't have proper price details
                val validProducts = productDetailsList.filter { product ->
                    product.oneTimePurchaseOfferDetails != null &&
                    product.oneTimePurchaseOfferDetails?.formattedPrice != null
                }
                _availableProducts.value = validProducts
                
                // Debug logging to help identify issues
                android.util.Log.d("BillingManager", "Total products queried: ${productDetailsList.size}")
                android.util.Log.d("BillingManager", "Valid products with prices: ${validProducts.size}")
                productDetailsList.forEach { product ->
                    android.util.Log.d("BillingManager", "Product: ${product.productId}, " +
                        "Name: ${product.name}, " +
                        "Price: ${product.oneTimePurchaseOfferDetails?.formattedPrice ?: "NULL"}")
                }
            } else {
                android.util.Log.e("BillingManager", "Failed to query products: ${billingResult.debugMessage}")
                _availableProducts.value = emptyList()
            }
        }
    }
    
    private fun queryExistingPurchases() {
        billingClient?.queryPurchasesAsync(
            BillingClient.ProductType.INAPP
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasDonations = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    donationSkus.contains(purchase.products.firstOrNull())
                }
                userPreferencesManager.setHasDonated(hasDonations)
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
            
            // Check if this is a donation and update preference
            if (donationSkus.contains(purchase.products.firstOrNull())) {
                userPreferencesManager.setHasDonated(true)
            }
            
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