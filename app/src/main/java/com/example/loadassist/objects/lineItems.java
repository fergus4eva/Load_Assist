package com.example.loadassist.objects;

public class lineItems {

    protected String lineItem;
    protected String category;
    protected int lineItemNumber;


    public lineItems() {}

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



    @Override
    public String toString(){
        return String.format(
                "Item: %s \n"
                        + "Category: %s\n"
                        + "Trail Length In Feet:  %d\n",
                this.getlineItem(), this.getCategory());
    }
    @Override
    public int compareTo(lineItems otherItem) {
        //return this.lastName.compareTo(o.lastName);
        //return this.vacationMiles -
        if(this.lineItem < otherItem.l) {
            return -1;
        }
        else if(this.trailLengthMiles == t.trailLengthMiles) {
            return 0;}
        else return 1;

    }
}


}
