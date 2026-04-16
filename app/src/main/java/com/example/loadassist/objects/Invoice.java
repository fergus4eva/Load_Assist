package com.example.loadassist.objects;

import androidx.annotation.NonNull;

import com.example.loadassist.adts.InventoryList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Represents an Invoice that groups items by their category using a HashMap.
 * Implements Iterable to allow iterating through all items across all categories.
 */
public class Invoice implements Iterable<lineItems> {
    private String invoiceNumber;
    private long timestamp;
    // Maps Category Name -> InventoryList of items in that category
    private Map<String, InventoryList<lineItems>> itemsByCategory;
    
    protected int totalQuantity;

    public Invoice() {
        this.invoiceNumber = generateInvoiceNumber();
        this.timestamp = System.currentTimeMillis();
        this.itemsByCategory = new HashMap<>();
        this.totalQuantity = 0;
    }

    /**
     * Adds a line item to the invoice, automatically grouping it by its category.
     * If the item already exists in the invoice, increments its quantity.
     * @param item The line item to add.
     */
    public void addItem(lineItems item) {
        if (item == null || item.getCategory() == null) return;

        String category = item.getCategory();

        // If the category doesn't exist in our map yet, create a new list for it
        if (!itemsByCategory.containsKey(category)) {
            itemsByCategory.put(category, new InventoryList<lineItems>());
        }

        InventoryList<lineItems> list = itemsByCategory.get(category);
        assert list != null;
        
        // Check if item already exists in this category
        lineItems existingItem = list.get(item);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
        } else {
            list.add(item);
        }
        
        totalQuantity++;
    }
    
    /**
     * Removes an item from the invoice. 
     * If quantity > 1, decrements quantity. If quantity == 1, removes the item entirely.
     * @param item The item to remove.
     */
    public void removeItem(lineItems item) {
        if (item == null || item.getCategory() == null) return;

        String category = item.getCategory();
        if (itemsByCategory.containsKey(category)) {
            InventoryList<lineItems> list = itemsByCategory.get(category);
            assert list != null;
            //check to see if line item is already in the list Invoice
            lineItems existingItem = list.get(item);
            if (existingItem != null) {
                if (existingItem.getQuantity() > 1) {
                    existingItem.setQuantity(existingItem.getQuantity() - 1);
                } else {
                    list.remove(existingItem);
                    // Clean up empty categories
                    if (list.isEmpty()) {
                        itemsByCategory.remove(category);
                    }
                }
                totalQuantity--;
            }
        }
    }

    /**
     * Groups all items in the invoice by their runner number.
     * @return A map where key is Runner Number and value is a list of items for that runner.
     */
    public Map<Integer, InventoryList<lineItems>> getItemsByRunner() {
        Map<Integer, InventoryList<lineItems>> runnerMap = new TreeMap<>(); // Sorted by runner number
        for (lineItems item : this) {
            int runner = item.getRunnerNumber();
            if (!runnerMap.containsKey(runner)) {
                runnerMap.put(runner, new InventoryList<lineItems>());
            }
            runnerMap.get(runner).add(item);
        }
        return runnerMap;
    }

    /**
     * Calculates and returns the name and size of the largest category by item count.
     * @return A string like "CategoryName (Count)" or "N/A" if empty.
     */
    public String getLargestCategoryInfo() {
        if (itemsByCategory.isEmpty()) return "N/A";
        
        String largestCat = "";
        int maxCount = -1;
        
        for (Map.Entry<String, InventoryList<lineItems>> entry : itemsByCategory.entrySet()) {
            int currentCount = 0;
            for (lineItems item : entry.getValue()) {
                currentCount += item.getQuantity();
            }
            
            if (currentCount > maxCount) {
                maxCount = currentCount;
                largestCat = entry.getKey();
            }
        }
        
        return largestCat + " (" + maxCount + ")";
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

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
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

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a formatted date string for the invoice creation time.
     */
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String generateInvoiceNumber() {
        return UUID.randomUUID().toString();
    }

    @NonNull
    @Override
    public Iterator<lineItems> iterator() {
        return new InvoiceIterator();
    }

    /**
     * Custom Iterator that flattens the Map of lists into a single stream of items.
     */
    private class InvoiceIterator implements Iterator<lineItems> {
        private final Iterator<InventoryList<lineItems>> categoryListsIterator;
        private Iterator<lineItems> currentItemIterator;

        public InvoiceIterator() {
            categoryListsIterator = itemsByCategory.values().iterator();
            prepareNextIterator();
        }

        private void prepareNextIterator() {
            if (categoryListsIterator.hasNext()) {
                currentItemIterator = categoryListsIterator.next().iterator();
            } else {
                currentItemIterator = null;
            }
        }

        @Override
        public boolean hasNext() {
            while (currentItemIterator != null && !currentItemIterator.hasNext()) {
                prepareNextIterator();
            }
            return currentItemIterator != null && currentItemIterator.hasNext();
        }

        @Override
        public lineItems next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return currentItemIterator.next();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceNumber='" + invoiceNumber + '\'' +
                ", date='" + getFormattedDate() + '\'' +
                ", itemsByCategory=" + itemsByCategory +
                ", totalQuantity=" + totalQuantity +
                '}';
    }
}
