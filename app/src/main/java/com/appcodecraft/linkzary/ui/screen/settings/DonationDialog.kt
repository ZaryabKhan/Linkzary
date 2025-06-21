package com.appcodecraft.linkzary.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.appcodecraft.linkzary.billing.BillingConnectionState
import com.appcodecraft.linkzary.billing.PurchaseState

@Composable
fun DonationDialog(
    onDismiss: () -> Unit,
    viewModel: DonationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val billingConnectionState by viewModel.billingConnectionState.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeBilling(context)
    }
    
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success -> {
                // Show success message and dismiss dialog
                onDismiss()
                viewModel.resetPurchaseState()
            }
            is PurchaseState.Error -> {
                // Handle error - could show a snackbar or toast
                viewModel.resetPurchaseState()
            }
            else -> {}
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Support Linkzary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Your support helps us continue developing and improving Linkzary. Choose any amount you'd like to donate:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (billingConnectionState) {
                    BillingConnectionState.CONNECTED -> {
                        if (availableProducts.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableProducts.sortedBy { getProductPrice(it) }) { product ->
                                    DonationOptionCard(
                                        productDetails = product,
                                        isLoading = purchaseState is PurchaseState.Loading,
                                        onClick = {
                                            viewModel.launchDonationFlow(context, product)
                                        }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Loading donation options...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    BillingConnectionState.ERROR -> {
                        Text(
                            text = "Unable to load donation options. Please try again later.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DonationOptionCard(
    productDetails: ProductDetails,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = productDetails.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = productDetails.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onClick,
                enabled = !isLoading,
                modifier = Modifier.width(80.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: "$1",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

private fun getProductPrice(productDetails: ProductDetails): Long {
    return productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
}