package com.example.loadassist.ui_

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.loadassist.objects.Invoice
import com.example.loadassist.objects.lineItems
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManualInputViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _invoice = MutableStateFlow(Invoice())
    val invoice: StateFlow<Invoice> = _invoice.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _categoryItemsMap = mutableStateMapOf<String, List<lineItems>>()
    
    private val _categoryItemNames = mutableStateMapOf<String, List<String>>()
    val categoryItems: Map<String, List<String>> = _categoryItemNames

    private val _allProducts = MutableStateFlow<List<lineItems>>(emptyList())
    val allProducts: StateFlow<List<lineItems>> = _allProducts.asStateFlow()

    private val _expandedCategories = mutableStateMapOf<String, Boolean>()
    val expandedCategories: Map<String, Boolean> = _expandedCategories

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    private val _scannedItems = mutableStateMapOf<lineItems, Int>()
    val scannedItems: Map<lineItems, Int> = _scannedItems

    // Manager role state
    private val _isManager = MutableStateFlow(false)
    val isManager: StateFlow<Boolean> = _isManager.asStateFlow()

    // Receiving Report State
    private val _receivingReport = MutableStateFlow<Map<String, Any>?>(null)
    val receivingReport: StateFlow<Map<String, Any>?> = _receivingReport.asStateFlow()

    init {
        fetchCategories()
        fetchAllProducts()
        checkUserRole()
    }
    
    private fun checkUserRole() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        val email = currentUser.email ?: ""
        val empNumber = email.substringBefore("@")

        // 1. Hardcoded Master Admin Bypass (for you)
        if (empNumber == "148596") {
            _isManager.value = true
            Log.d("ManualInputViewModel", "Master Admin $empNumber detected")
            return
        }

        // 2. Standard Token Check (for everyone else)
        currentUser.getIdToken(true).addOnSuccessListener { result ->
            val isManagerClaim = result.claims["isManager"] as? Boolean ?: false
            _isManager.value = isManagerClaim
            Log.d("ManualInputViewModel", "Token Manager Status: $isManagerClaim")
        }
    }

    private fun fetchCategories() {
        db.collection("category")
            .get()
            .addOnSuccessListener { result ->
                _categories.value = result.documents.mapNotNull { it.getString("name") }
            }
    }

    fun fetchAllProducts() {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                _allProducts.value = result.documents.mapNotNull { doc ->
                    try {
                        lineItems(
                            doc.getString("name") ?: "",
                            doc.getString("category") ?: "General",
                            doc.getString("brand") ?: "Unknown",
                            doc.get("itemNumber")?.toString()?.toDoubleOrNull()?.toInt() ?: 0,
                            doc.getString("barcodeId") ?: "",
                            doc.get("runnerNumber")?.toString()?.toDoubleOrNull()?.toInt() ?: 1,
                            doc.getString("imageUrl") ?: "",
                            doc.getString("description") ?: "No description provided.",
                            doc.getString("handlingGuide") ?: "No special handling instructions.",
                            doc.getString("productType") ?: "pre-packaged",
                            doc.get("length")?.toString()?.toDoubleOrNull() ?: 0.0,
                            doc.get("width")?.toString()?.toDoubleOrNull() ?: 0.0,
                            doc.get("height")?.toString()?.toDoubleOrNull() ?: 0.0,
                            doc.get("weight")?.toString()?.toDoubleOrNull() ?: 0.0
                        )
                    } catch (e: Exception) {
                        Log.e("ManualInputViewModel", "Error parsing item: ${doc.id}", e)
                        null
                    }
                }
            }
    }
    
    fun toggleCategoryExpansion(category: String) {
        val isExpanded = _expandedCategories[category] ?: false
        _expandedCategories[category] = !isExpanded
        
        if (!isExpanded && !_categoryItemNames.containsKey(category)) {
            fetchItemsForCategory(category)
        }
    }
    
    private fun fetchItemsForCategory(category: String) {
        db.collection("items")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { doc ->
                    try {
                        lineItems(
                            doc.getString("name") ?: "",
                            category,
                            doc.getString("brand") ?: "Unknown",
                            doc.get("itemNumber")?.toString()?.toDoubleOrNull()?.toInt() ?: 0,
                            doc.getString("barcodeId") ?: "",
                            doc.get("runnerNumber")?.toString()?.toDoubleOrNull()?.toInt() ?: 1,
                            doc.getString("imageUrl") ?: "",
                            doc.getString("description") ?: "",
                            doc.getString("handlingGuide") ?: "",
                            doc.getString("productType") ?: "pre-packaged",
                            doc.get("length")?.toString()?.toDoubleOrNull() ?: 0.0,
                            doc.get("width")?.toString()?.toDoubleOrNull() ?: 0.0,
                            doc.get("height")?.toString()?.toDoubleOrNull() ?: 0.0,
                            doc.get("weight")?.toString()?.toDoubleOrNull() ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _categoryItemsMap[category] = items
                _categoryItemNames[category] = items.map { it.getlineItem() }
            }
    }
    
    fun addItem(itemName: String, category: String) {
        val details = _categoryItemsMap[category]?.find { it.getlineItem() == itemName }
        val newItem = if (details != null) {
            lineItems(
                itemName, 
                category, 
                details.getBrand(), 
                details.getLineItemNumber(), 
                details.getBarcodeId(),
                details.getRunnerNumber(),
                details.getImageUrl(),
                details.getDescription(),
                details.getHandlingGuide(),
                details.getProductType(),
                details.getLength(),
                details.getWidth(),
                details.getHeight(),
                details.getWeight()
            )
        } else {
            lineItems(itemName, category, 0)
        }
        _invoice.value.addItem(newItem)
        _refreshTrigger.value++
    }

    fun removeItem(itemName: String, category: String) {
        val list = _invoice.value.getItemsByCategory(category)
        val target = list?.iterator()?.asSequence()?.find { it.getlineItem() == itemName }
        
        if (target != null) {
            _invoice.value.removeItem(target)
            _refreshTrigger.value++
        }
    }

    fun getItemQuantity(itemName: String, category: String): Int {
        val list = _invoice.value.getItemsByCategory(category)
        val item = list?.iterator()?.asSequence()?.find { it.getlineItem() == itemName }
        return item?.quantity ?: 0
    }

    fun addNewProduct(
        name: String,
        category: String,
        brand: String,
        barcode: String,
        runner: Int,
        description: String,
        guide: String,
        imageUrl: String,
        productType: String,
        length: Double,
        width: Double,
        height: Double,
        weight: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val itemData = hashMapOf(
            "name" to name,
            "category" to category,
            "brand" to brand,
            "barcodeId" to barcode,
            "runnerNumber" to runner,
            "description" to description,
            "handlingGuide" to guide,
            "imageUrl" to imageUrl,
            "productType" to productType,
            "length" to length,
            "width" to width,
            "height" to height,
            "weight" to weight,
            "itemNumber" to System.currentTimeMillis().toInt()
        )

        db.collection("items")
            .add(itemData)
            .addOnSuccessListener {
                fetchAllProducts()
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to add item")
            }
    }

    fun incrementScannedItem(item: lineItems) {
        val currentCount = _scannedItems[item] ?: 0
        if (currentCount < item.quantity) {
            _scannedItems[item] = currentCount + 1
            _refreshTrigger.value++
        }
    }
    
    fun decrementScannedItem(item: lineItems) {
        val currentCount = _scannedItems[item] ?: 0
        if (currentCount > 0) {
            _scannedItems[item] = currentCount - 1
            _refreshTrigger.value++
        }
    }
    
    fun onBarcodeScanned(barcode: String): Boolean {
        val matchingItem = _invoice.value.iterator().asSequence().find { 
            it.barcodeId == barcode || (it.barcodeId.isEmpty() && it.lineItemNumber.toString() == barcode)
        }
        
        return if (matchingItem != null) {
            incrementScannedItem(matchingItem)
            true
        } else {
            false
        }
    }

    fun finishReceiving(onComplete: () -> Unit) {
        val totalToScan = _invoice.value.totalQuantity
        val totalScanned = _scannedItems.values.sum()
        val completionRate = if (totalToScan > 0) (totalScanned.toFloat() / totalToScan * 100) else 0f
        
        val missingItems = mutableListOf<Map<String, Any>>()
        _invoice.value.forEach { item ->
            val scanned = _scannedItems[item] ?: 0
            if (scanned < item.quantity) {
                missingItems.add(hashMapOf(
                    "name" to item.getlineItem(),
                    "expected" to item.quantity,
                    "received" to scanned,
                    "missing" to (item.quantity - scanned)
                ))
            }
        }

        val reportData = hashMapOf(
            "invoiceNumber" to _invoice.value.invoiceNumber,
            "timestamp" to System.currentTimeMillis(),
            "formattedDate" to _invoice.value.formattedDate,
            "totalExpected" to totalToScan,
            "totalReceived" to totalScanned,
            "completionRate" to completionRate,
            "missingItems" to missingItems,
            "receivedBy" to (auth.currentUser?.email?.substringBefore("@") ?: "Unknown")
        )

        _receivingReport.value = reportData

        db.collection("receiving_reports")
            .add(reportData)
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { e ->
                onComplete()
            }
    }

    fun resetReceiving() {
        _invoice.value = Invoice()
        _scannedItems.clear()
        _receivingReport.value = null
        _refreshTrigger.value++
    }
}
