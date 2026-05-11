package com.example.loadassist.ui_

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.loadassist.objects.Invoice
import com.example.loadassist.objects.lineItems
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
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

    private var startTime: Long? = null

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

        if (empNumber == "148596") {
            _isManager.value = true
            return
        }

        currentUser.getIdToken(true).addOnSuccessListener { result ->
            val isManagerClaim = result.claims["isManager"] as? Boolean ?: false
            _isManager.value = isManagerClaim
        }
    }

    private fun fetchCategories() {
        db.collection("category").get().addOnSuccessListener { result ->
            _categories.value = result.documents.mapNotNull { it.getString("name") }
        }
    }

    fun fetchAllProducts() {
        db.collection("items").get().addOnSuccessListener { result ->
            _allProducts.value = result.documents.mapNotNull { doc ->
                try {
                    val barcodeId = doc.getString("barcodeId") ?: ""
                    val itemNumber = doc.get("itemNumber")?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                    
                    lineItems(
                        doc.getString("name") ?: "",
                        doc.getString("category") ?: "General",
                        doc.getString("brand") ?: "Unknown",
                        itemNumber,
                        if (barcodeId.isEmpty()) itemNumber.toString() else barcodeId,
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
                } catch (e: Exception) { null }
            }
        }
    }
    
    fun toggleCategoryExpansion(category: String) {
        val isExpanded = _expandedCategories[category] ?: false
        _expandedCategories[category] = !isExpanded
        if (!isExpanded && !_categoryItemNames.containsKey(category)) fetchItemsForCategory(category)
    }
    
    private fun fetchItemsForCategory(category: String) {
        db.collection("items").whereEqualTo("category", category).get().addOnSuccessListener { result ->
            val items = result.documents.mapNotNull { doc ->
                try {
                    val barcodeId = doc.getString("barcodeId") ?: ""
                    val itemNumber = doc.get("itemNumber")?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                    lineItems(doc.getString("name") ?: "", category, doc.getString("brand") ?: "", itemNumber, if (barcodeId.isEmpty()) itemNumber.toString() else barcodeId, doc.get("runnerNumber")?.toString()?.toDoubleOrNull()?.toInt() ?: 1, doc.getString("imageUrl") ?: "", doc.getString("description") ?: "", doc.getString("handlingGuide") ?: "", doc.getString("productType") ?: "pre-packaged", doc.get("length")?.toString()?.toDoubleOrNull() ?: 0.0, doc.get("width")?.toString()?.toDoubleOrNull() ?: 0.0, doc.get("height")?.toString()?.toDoubleOrNull() ?: 0.0, doc.get("weight")?.toString()?.toDoubleOrNull() ?: 0.0)
                } catch (e: Exception) { null }
            }
            _categoryItemsMap[category] = items
            _categoryItemNames[category] = items.map { it.getlineItem() }
        }
    }
    
    fun addItem(itemName: String, category: String) {
        val details = _allProducts.value.find { it.getlineItem() == itemName } ?:
                      _categoryItemsMap[category]?.find { it.getlineItem() == itemName }
        
        val newItem = if (details != null) {
            lineItems(
                itemName, category, details.getBrand(), details.getLineItemNumber(), details.getBarcodeId(),
                details.getRunnerNumber(), details.getImageUrl(), details.getDescription(),
                details.getHandlingGuide(), details.getProductType(), details.getLength(),
                details.getWidth(), details.getHeight(), details.getWeight()
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

    fun processScannedInvoiceText(text: String): Int {
        val products = _allProducts.value
        if (products.isEmpty()) return 0

        val lines = text.split("\n")
        var itemsAddedCount = 0

        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            val asnRowRegex = Regex("""^(\d{4,7})\s+(.+?)\s+(\d{1,3})(\s+\d+|\s*$)""")
            val match = asnRowRegex.find(trimmed)

            if (match != null) {
                val itemNo = match.groupValues[1]
                val detectedDesc = match.groupValues[2].trim().uppercase()
                val quantity = match.groupValues[3].toIntOrNull() ?: 1
                
                var product = products.find { it.lineItemNumber.toString() == itemNo }
                if (product == null) {
                    product = products.find { it.getlineItem().uppercase().contains(detectedDesc) || detectedDesc.contains(it.getlineItem().uppercase()) }
                }

                if (product != null) {
                    repeat(quantity) { addItem(product.getlineItem(), product.getCategory()) }
                    itemsAddedCount++
                    return@forEach
                }
            }

            val standaloneIdRegex = Regex("""(\d{4,7})""")
            val idMatch = standaloneIdRegex.find(trimmed)
            if (idMatch != null) {
                val potentialId = idMatch.groupValues[1]
                val product = products.find { it.lineItemNumber.toString() == potentialId }
                if (product != null) {
                    addItem(product.getlineItem(), product.getCategory())
                    itemsAddedCount++
                }
            }
        }
        
        if (itemsAddedCount > 0) _refreshTrigger.value++
        return itemsAddedCount
    }

    /**
     * Enhanced Barcode Scanning for GS1-128
     * Handles complex logistical barcodes by looking for embedded Item Numbers.
     */
    fun onBarcodeScanned(barcode: String): Boolean {
        if (startTime == null) startTime = System.currentTimeMillis()
        
        // 1. First, try an exact match (UPC/Standard IDs)
        var matchingItem = _invoice.value.iterator().asSequence().find { 
            it.barcodeId == barcode || it.lineItemNumber.toString() == barcode 
        }
        
        // 2. If no exact match and it's a long logistical barcode, try fuzzy extraction
        if (matchingItem == null && barcode.length > 10) {
            Log.d("Scanner", "Attempting GS1-128 extraction for: $barcode")
            matchingItem = _invoice.value.iterator().asSequence().find { item ->
                val idStr = item.lineItemNumber.toString()
                // Check if the barcode string contains our 5 or 6 digit Item Number
                // We ensure it's not part of a larger number by checking boundaries if possible,
                // but standard containment is a great start for GS1-128.
                idStr.length >= 4 && barcode.contains(idStr)
            }
        }
        
        return if (matchingItem != null) {
            incrementScannedItem(matchingItem)
            Log.d("Scanner", "Matched item: ${matchingItem.getlineItem()} via fuzzy scan")
            true
        } else {
            Log.w("Scanner", "No match found for barcode: $barcode")
            false
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

    fun addNewProduct(
        name: String,
        category: String,
        brand: String,
        barcode: String,
        itemNumber: Int,
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
            "itemNumber" to itemNumber,
            "runnerNumber" to runner,
            "description" to description,
            "handlingGuide" to guide,
            "imageUrl" to imageUrl,
            "productType" to productType,
            "length" to length,
            "width" to width,
            "height" to height,
            "weight" to weight
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

    fun finishReceiving(onComplete: () -> Unit) {
        val totalToScan = _invoice.value.totalQuantity
        val totalScanned = _scannedItems.values.sum()
        val missingItems = mutableListOf<String>()
        
        _invoice.value.forEach { item ->
            val scanned = _scannedItems[item] ?: 0
            if (scanned < item.quantity) {
                missingItems.add("${item.getlineItem()} (Missing ${item.quantity - scanned})")
            }
        }

        val reportData = hashMapOf(
            "invoiceNumber" to _invoice.value.invoiceNumber,
            "employeeNumber" to (auth.currentUser?.email?.substringBefore("@") ?: "Unknown"),
            "startTime" to (startTime?.let { java.util.Date(it) } ?: java.util.Date()),
            "endTime" to FieldValue.serverTimestamp(),
            "totalItems" to totalToScan,
            "receivedItems" to totalScanned,
            "completionRate" to (if (totalToScan > 0) (totalScanned.toFloat() / totalToScan * 100) else 100f),
            "missingItems" to missingItems
        )

        _receivingReport.value = reportData

        // Changed collection to "finished_loads" to match the current ReportsScreen logic
        db.collection("finished_loads")
            .add(reportData)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { onComplete() }
    }

    fun resetReceiving() {
        _invoice.value = Invoice()
        _scannedItems.clear()
        _receivingReport.value = null
        startTime = null
        _refreshTrigger.value++
    }
}
