package com.example.loadassist.objects;

public class lineItems extends Object implements Comparable<lineItems> {

    protected String lineItem;
    protected String category;
    protected int lineItemNumber;


    public lineItems() {
    }

    public lineItems(String lineItem, String category, int lineItemNumber) {
        this.lineItem = lineItem;
        this.category = category;
        this.lineItemNumber = lineItemNumber;

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

    @Override
    public String toString() {
        return String.format(
                "Item: %s \n"
                        + "Category: %s\n"
                        + "Line Item Number:  %d\n",
                this.getlineItem(), this.getCategory(), this.getLineItemNumber());
    }

    @Override
    public int compareTo(lineItems otherItem) {
        //return this.lastName.compareTo(o.lastName)
        if (this.lineItem.length() < otherItem.lineItem.length()) {
            return -1;
        }


        return 0;
    }

}
