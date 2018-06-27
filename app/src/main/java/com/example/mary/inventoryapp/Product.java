package com.example.mary.inventoryapp;

public class Product {

    /**
     * member of the class to hold the name of the product
     */
    private String productName;

    /**
     * member of the class to hold the price of the product
     */
    private float mProductPrice;

    /**
     * member of the class to hold the quantity
     */
    private int productQuantity;

    /**
     * member of the class to hold the product's producer's name
     */
    private String supplierName;

    /**
     * member of the class to hold the product's producer's phone
     */
    private String supplierPhone;

    /**
     * The Product object constructor.
     * @param productName The name of the product.
     * @param price The product price.
     * @param quantity How many products we have.
     * @param supplierName The name of the supplier.
     * @param supplierPhone The supplier's phone number.
     */
    public Product(String productName, float price, int quantity, String supplierName, String supplierPhone) {
        this.productName = productName;
        mProductPrice = price;
        productQuantity = quantity;
        this.supplierName = supplierName;
        this.supplierPhone = supplierPhone;
    }

    /**
     * @return the name of the product
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return the price of the product
     */
    public float getProductPrice() {
        return mProductPrice;
    }

    /**
     * @return the quantity of the product
     */
    public int getProductQuantity() {
        return productQuantity;
    }

    /**
     * @return the supplier name
     */
    public String getSupplierName() {
        return supplierName;
    }

    /**
     * @return the supplier's phone
     */
    public String getSupplierPhone() {
        return supplierPhone;
    }

//    /**
//     * Overrides the toString method for debugging purposes.
//     * @return Returns a concatenated string with all the fields contents.
//     */
//    @Override
//    public String toString() {
//        return "Product {" +
//                "productName='" + productName + "', " +
//                "mProductPrice=" + mProductPrice + ", " +
//                "productQuantity=" + productQuantity + ", " +
//                "supplierName='" + supplierName + "', " +
//                "supplierPhone='" + supplierPhone + "'}";
//    }
}
