package com.example.loadassist.objects;

import com.example.loadassist.adts.InventoryList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an Invoice that groups items by their category using a HashMap.
 */
public class Invoice {
    private String invoiceNumber;
    // Maps Category Name -> InventoryList of items in that category
    private Map<String, InventoryList<lineItems>> itemsByCategory;
    protected int totalQuantity;

    public Invoice() {
        this.invoiceNumber = generateInvoiceNumber();
        this.itemsByCategory = new HashMap<>();
        this.totalQuantity = 0;
    }

    /**
     * Adds a line item to the invoice, automatically grouping it by its category.
     * @param item The line item to add.
     */
    public void addItem(lineItems item) {
        if (item == null || item.getCategory() == null) return;

        String category = item.getCategory();

        // If the category doesn't exist in our map yet, create a new list for it
        if (!itemsByCategory.containsKey(category)) {
            itemsByCategory.put(category, new InventoryList<lineItems>());
        }

        // Add the item to the specific list for its category
        itemsByCategory.get(category).add(item);
        totalQuantity++;
    }

    /**
     * Returns the list of items for a specific category.
     * @param category The category name to look up.
     * @return The InventoryList for that category, or null if it doesn't exist.
     */
    public InventoryList<lineItems> getItemsByCategory(String category) {
        return itemsByCategory.get(category);
    }

    /**
     * Returns the full map of categories and their items.
     */
    public Map<String, InventoryList<lineItems>> getItemsMap() {
        return itemsByCategory;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * Returns the number of unique categories in this invoice.
     */
    public int getCategoryCount() {
        return itemsByCategory.size();
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    private String generateInvoiceNumber() {
        return UUID.randomUUID().toString();
    }
}
