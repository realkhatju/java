package com.example.productapi.config;

import com.example.productapi.product.Product;
import com.example.productapi.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ProductRepository productRepository;
    private final boolean enabled;

    public DataSeeder(ProductRepository productRepository,
                      @Value("${app.seed.enabled:true}") boolean enabled) {
        this.productRepository = productRepository;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || productRepository.count() > 0) {
            return;
        }

        List<Product> products = List.of(
                new Product("ELEC-1001", "Wireless Mouse", "Ergonomic 2.4 GHz wireless mouse", "Electronics", new BigDecimal("29.99"), 120, true),
                new Product("ELEC-1002", "Mechanical Keyboard", "Hot-swappable RGB mechanical keyboard", "Electronics", new BigDecimal("89.50"), 45, true),
                new Product("ELEC-1003", "USB-C Hub", "7-in-1 USB-C hub with HDMI and card reader", "Electronics", new BigDecimal("49.00"), 80, true),
                new Product("HOME-2001", "Ceramic Coffee Mug", "350 ml matte ceramic mug", "Home", new BigDecimal("12.75"), 300, true),
                new Product("HOME-2002", "Desk Lamp", "Dimmable LED desk lamp with USB port", "Home", new BigDecimal("39.90"), 60, true),
                new Product("OFFC-3001", "Notebook A5", "Dotted 160-page hardcover notebook", "Office", new BigDecimal("14.25"), 200, true),
                new Product("OFFC-3002", "Gel Pen Pack", "Pack of 12 black 0.5 mm gel pens", "Office", new BigDecimal("8.99"), 500, true),
                new Product("SPRT-4001", "Yoga Mat", "Non-slip 6 mm yoga mat", "Sports", new BigDecimal("24.50"), 75, true),
                new Product("SPRT-4002", "Water Bottle", "1 litre insulated stainless steel bottle", "Sports", new BigDecimal("21.00"), 0, false),
                new Product("ACCS-5001", "Laptop Sleeve", "14-inch padded laptop sleeve", "Accessories", new BigDecimal("19.99"), 90, true)
        );

        productRepository.saveAll(products);
        log.info("Seeded {} products", products.size());
    }
}
