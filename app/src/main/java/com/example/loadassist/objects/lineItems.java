package com.example.loadassist.objects;

import java.util.Objects;

public class lineItems extends Object implements Comparable<lineItems> {

    protected String lineItem;
    protected String category;
    protected String brand;
    protected int lineItemNumber;
    protected int quantity;
    protected String barcodeId;
    protected int runnerNumber; // ID for the runner/route
    protected String imageUrl;   // URL for the reference photo
    protected String description; // Item description
    protected String handlingGuide; // Guide for handling/placement
    protected String productType; // bulk, pre-sliced, pre-packaged
    
    // Physical Dimensions (in inches/lbs)
    protected double length;
    protected double width;
    protected double height;
    protected double weight;

    public lineItems() {
        this.quantity = 1;
        this.brand = "Unknown";
        this.barcodeId = "";
        this.runnerNumber = 1;
        this.imageUrl = "";
        this.description = "";
        this.handlingGuide = "";
        this.productType = "pre-packaged";
        this.length = 0.0;
        this.width = 0.0;
        this.height = 0.0;
        this.weight = 0.0;
    }

    public lineItems(String lineItem, String category, int lineItemNumber) {
        this(lineItem, category, "Unknown", lineItemNumber, "", 1, "", "", "", "pre-packaged", 0, 0, 0, 0);
    }

    // Constructor for items without dimensions
    public lineItems(String lineItem, String category, String brand, int lineItemNumber, String barcodeId, int runnerNumber, String imageUrl, String description, String handlingGuide, String productType) {
        this(lineItem, category, brand, lineItemNumber, barcodeId, runnerNumber, imageUrl, description, handlingGuide, productType, 0.0, 0.0, 0.0, 0.0);
    }

    // Full constructor
    public lineItems(String lineItem, String category, String brand, int lineItemNumber, String barcodeId, int runnerNumber, String imageUrl, String description, String handlingGuide, String productType, double length, double width, double height, double weight) {
        this.lineItem = lineItem;
        this.category = category;
        this.brand = brand;
        this.lineItemNumber = lineItemNumber;
        this.quantity = 1;
        this.barcodeId = barcodeId;
        this.runnerNumber = runnerNumber;
        this.imageUrl = imageUrl;
        this.description = description;
        this.handlingGuide = handlingGuide;
        this.productType = productType;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
    }

    public String getlineItem() { return lineItem; }
    public void setlineItem(String lineItem) { this.lineItem = lineItem; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getLineItemNumber() { return lineItemNumber; }
    public void setLineItemNumber(int lineItemNumber) { this.lineItemNumber = lineItemNumber; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getBarcodeId() { return barcodeId; }
    public void setBarcodeId(String barcodeId) { this.barcodeId = barcodeId; }
    public int getRunnerNumber() { return runnerNumber; }
    public void setRunnerNumber(int runnerNumber) { this.runnerNumber = runnerNumber; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHandlingGuide() { return handlingGuide; }
    public void setHandlingGuide(String handlingGuide) { this.handlingGuide = handlingGuide; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    /**
     * Calculates the volume of the box in cubic inches.
     */
    public double getVolume() {
        return length * width * height;
    }

    @Override
    public String toString() {
        return String.format(
                "Item: %s \n"
                        + "Category: %s\n"
                        + "Type: %s\n"
                        + "Dimensions: %.1fx%.1fx%.1f\n"
                        + "Weight: %.1f lbs\n",
                this.getlineItem(), this.getCategory(), productType, length, width, height, weight);
    }

    @Override
    public int compareTo(lineItems otherItem) {
        if (this.runnerNumber != otherItem.runnerNumber) {
            return Integer.compare(this.runnerNumber, otherItem.runnerNumber);
        }
        // Heavy items first (best practice for loading)
        int weightCompare = Double.compare(otherItem.weight, this.weight);
        if (weightCompare != 0) return weightCompare;
        
        return this.lineItem.compareTo(otherItem.lineItem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        lineItems that = (lineItems) o;
        return Objects.equals(lineItem, that.lineItem) &&
                Objects.equals(category, that.category) &&
                Objects.equals(brand, that.brand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineItem, category, brand);
    }
}
