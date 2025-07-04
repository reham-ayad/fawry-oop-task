

import java.util.*;

interface Shippable {
    String getName();
    double getWeight();
}

abstract class Product {
    protected String productName;
    protected double productPrice;
    protected int productQty;

    public Product(String productName, double productPrice, int productQty) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQty = productQty;
    }

    public String getName() {
        return productName;
    }

    public double getPrice() {
        return productPrice;
    }

    public int getQuantity() {
        return productQty;
    }

    public void reduceQuantity(int qty) {
        this.productQty -= qty;
    }

    public boolean isExpired() {
        return false;
    }

    public boolean requiresShipping() {
        return false;
    }

    public double getWeight() {
        return 0.0;
    }
}

class ExpirableProduct extends Product {
    private Date expiry;

    public ExpirableProduct(String name, double price, int qty, Date expiry) {
        super(name, price, qty);
        this.expiry = expiry;
    }

    @Override
    public boolean isExpired() {
        return new Date().after(expiry);
    }
}

class ShippableProduct extends Product implements Shippable {
    private double productWeight;

    public ShippableProduct(String name, double price, int qty, double weight) {
        super(name, price, qty);
        this.productWeight = weight;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }

    @Override
    public double getWeight() {
        return productWeight;
    }
}

class ExpirableShippableProduct extends ExpirableProduct implements Shippable {
    private double weight;

    public ExpirableShippableProduct(String name, double price, int qty, Date expiry, double weight) {
        super(name, price, qty, expiry);
        this.weight = weight;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

class NonExpirableNonShippableProduct extends Product {
    public NonExpirableNonShippableProduct(String name, double price, int qty) {
        super(name, price, qty);
    }
}

class CartItem {
    Product item;
    int quantity;

    public CartItem(Product item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }

    public Product getProduct() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }
}

class Cart {
    private List<CartItem> items = new ArrayList<>();

    public void add(Product item, int qty) {
        if (item.getQuantity() < qty) {
            throw new IllegalArgumentException("Sorry, not enough in stock: " + item.getName());
        }
        items.add(new CartItem(item, qty));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

class Customer {
    private String customerName;
    private double wallet;

    public Customer(String customerName, double wallet) {
        this.customerName = customerName;
        this.wallet = wallet;
    }

    public String getName() {
        return customerName;
    }

    public double getBalance() {
        return wallet;
    }

    public void deduct(double amount) {
        if (wallet < amount) {
            System.out.println("Oops! Your balance is not enough for this order.");
            throw new IllegalArgumentException("Insufficient funds.");
        }
        this.wallet -= amount;
    }
}

class ShippingService {
    public static void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0.0;
        for (Shippable item : items) {
            System.out.println("- " + item.getName() + " " + item.getWeight() + "kg");
            totalWeight += item.getWeight();
        }
        System.out.println("Total package weight: " + totalWeight + "kg");
    }
}

class CheckoutService {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty!");
            throw new IllegalArgumentException("Cart is empty.");
        }

        double subtotal = 0.0;
        double shipping = 0.0;
        List<Shippable> toShip = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (product.isExpired()) {
                System.out.println("Oops! Product expired: " + product.getName());
                throw new IllegalArgumentException("Product expired.");
            }

            if (product.getQuantity() < item.getQuantity()) {
                System.out.println("Not enough stock for: " + product.getName());
                throw new IllegalArgumentException("Out of stock.");
            }

            product.reduceQuantity(item.getQuantity());
            subtotal += item.getTotalPrice();

            if (product.requiresShipping()) {
                toShip.add((Shippable) product);
                shipping += 15.0;
            }
        }

        double total = subtotal + shipping;

        customer.deduct(total);

        if (!toShip.isEmpty()) {
            ShippingService.ship(toShip);
        }

        // Final receipt
        System.out.println("\n** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.println(item.getQuantity() + "x " + item.getProduct().getName() + " = " + item.getTotalPrice());
        }
        System.out.println("----------------------");
        System.out.println("Subtotal: " + subtotal);
        System.out.println("Shipping: " + shipping);
        System.out.println("Total: " + total);
        System.out.println("Balance left: " + customer.getBalance());
    }
}

public class Main {
    public static void main(String[] args) {
        // simulate today + 1 day for expiry
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();

        // Define products
        ExpirableShippableProduct cheese = new ExpirableShippableProduct("Cheese", 100, 5, tomorrow, 0.4);
        ExpirableProduct biscuits = new ExpirableProduct("Biscuits", 150, 3, tomorrow);
        ShippableProduct tv = new ShippableProduct("TV", 3000, 2, 5.0);
        NonExpirableNonShippableProduct scratchCard = new NonExpirableNonShippableProduct("Scratch Card", 50, 10);

        // Create customer
        Customer customer = new Customer("Reham", 1000);

        // Fill the cart
        Cart cart = new Cart();
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        // Perform checkout
        CheckoutService.checkout(customer, cart);
    }
}
