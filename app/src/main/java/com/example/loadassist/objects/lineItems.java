package com.example.loadassist.objects;

import java.util.Objects;

public class lineItems extends Object implements Comparable<lineItems> {

    protected String lineItem;
    protected String category;
    protected int lineItemNumber;
    protected int quantity;

    public lineItems() {
        this.quantity = 1;
    }

    public lineItems(String lineItem, String category, int lineItemNumber) {
        this.lineItem = lineItem;
        this.category = category;
        this.lineItemNumber = lineItemNumber;
        this.quantity = 1;
    }

    public String getlineItem() {
        return lineItem;
    }

    public void setlineItem(String lineItem) {

        this.lineItem = lineItem;
    }

    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
    }

    public int getLineItemNumber() {
        return lineItemNumber;
    }

    public void setLineItemNumber(int lineItemNumber) {
        this.lineItemNumber = lineItemNumber;

    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format(
                "Item: %s \n"
                        + "Category: %s\n"
                        + "Line Item Number:  %d\n"
                        + "Quantity: %d\n",
                this.getlineItem(), this.getCategory(), this.getLineItemNumber(), this.getQuantity());
    }

    @Override
    public int compareTo(lineItems otherItem) {
        return this.lineItem.compareTo(otherItem.lineItem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        lineItems that = (lineItems) o;
        return Objects.equals(lineItem, that.lineItem) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineItem, category);
    }
}
