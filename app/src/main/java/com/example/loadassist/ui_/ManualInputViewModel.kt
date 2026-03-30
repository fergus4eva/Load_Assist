package com.example.loadassist.ui_

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.loadassist.objects.Invoice
import com.example.loadassist.objects.lineItems
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManualInputViewModel : ViewModel() {

    //database instance
    private val db = Firebase.firestore

    // Model = Invoice() object that will change with addition of items
    //stateflow to hold the invoice object, observed by ui
    private val _invoice = MutableStateFlow(Invoice())
    val invoice: StateFlow<Invoice> = _invoice.asStateFlow()

    // UI States - categories from database and items for each category
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Map of Category Name -> List of item names in that category
    private val _categoryItems = mutableStateMapOf<String, List<String>>()
    val categoryItems: Map<String, List<String>> = _categoryItems

    // Tracking which categories are expanded in the UI
    private val _expandedCategories = mutableStateMapOf<String, Boolean>()
    val expandedCategories: Map<String, Boolean> = _expandedCategories

    // Tracking UI Refresh
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // initializer block to fetch categories - so it will fetch our data
    //from database at loading of function
    init {
        fetchCategories()
    }
    //fetch categories from database and update UI
    private fun fetchCategories() {
        db.collection("category")
            .get()
            .addOnSuccessListener { result ->
                _categories.value = result.documents.mapNotNull { it.getString("name") }
            }
    }
    //toggle category expansion logic (expand or collapse)
    fun toggleCategoryExpansion(category: String) {
        val isExpanded = _expandedCategories[category] ?: false
        _expandedCategories[category] = !isExpanded
        
        // If expanding and we don't have items, fetch them
        if (!isExpanded && !_categoryItems.containsKey(category)) {
            fetchItemsForCategory(category)
        }
    }
    //fetch items for a specific category from database and update UI
    private fun fetchItemsForCategory(category: String) {
        db.collection("items")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { result ->
                _categoryItems[category] = result.documents.mapNotNull { it.getString("name") }
            }
    }
    //add item to invoice and update UI
    fun addItem(itemName: String, category: String) {
        val newItem = lineItems(itemName, category, 0)
        _invoice.value.addItem(newItem)
        _refreshTrigger.value++
    }

    fun removeItem(itemName: String, category: String) {
        val list = _invoice.value.getItemsByCategory(category)
        val target = list?.iterator()?.asSequence()?.find { it.getlineItem() == itemName }
        
        if (target != null) {
            // Invoice.removeItem handles the list removal AND quantity update
            _invoice.value.removeItem(target)
            _refreshTrigger.value++
        }
    }

    fun getItemQuantity(itemName: String, category: String): Int {
        val list = _invoice.value.getItemsByCategory(category)
        val item = list?.iterator()?.asSequence()?.find { it.getlineItem() == itemName }
        return item?.quantity ?: 0
    }
}
