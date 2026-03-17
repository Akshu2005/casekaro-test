package com.gocomet.assignment;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import java.nio.file.Paths;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class App {
    public static void main(String[] args) {
        System.out.println("\uD83D\uDE80 Launching Playwright...");

        Playwright playwright = Playwright.create();
        Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        // 1. Navigate to CaseKaro
        System.out.println("\uD83C\uDF10 Navigating to https://casekaro.com/");
        page.navigate("https://casekaro.com/");
        page.waitForTimeout(3000);

        // 2. Click on 'Mobile Covers'
        System.out.println("\uD83D\uDCE6 Clicking on 'Mobile Covers'...");
        Locator mobileCover = page.locator("a#HeaderMenu-mobile-covers");
        mobileCover.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        mobileCover.click();
        page.waitForTimeout(3000); 

        // 3. Search for "Apple"
        System.out.println("\uD83D\uDD0D Clicking search & typing 'Apple'...");
        Locator searchBtn = page.locator("button[aria-label='Search']");
        if (searchBtn.isVisible()) {
            searchBtn.click();
        } else {
            page.evaluate("document.querySelector(\"button[aria-label='Search']\").click()");
        }

        Locator searchInput = page.locator("input#search-bar-cover-page");
        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchInput.fill("Apple");
        searchInput.press("Enter");
        page.waitForTimeout(2000);

        // 4. Validate absence of banned brands
        System.out.println("\u274C Validating absence of banned brands...");
        List<String> bannedBrands = Arrays.asList("samsung", "vivo", "oneplus", "realme", "mi");
        List<String> titles = page.locator(".product-title").allInnerTexts();
        for (String text : titles) {
            String lower = text.toLowerCase();
            for (String brand : bannedBrands) {
                assertFalse(lower.contains(brand), "❌ Brand '" + brand + "' found in: " + text);
            }
        }

        // 5. Click on Apple brand
        System.out.println("\uD83C\uDF4E Clicking on 'Apple' brand button...");
        Locator appleButton = page.locator("button.brand-name-container:has(a:text('Apple'))");
        appleButton.waitFor(new Locator.WaitForOptions().setTimeout(10000).setState(WaitForSelectorState.VISIBLE));
        Page popupPage = page.waitForPopup(() -> appleButton.click());
        popupPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
        popupPage.waitForTimeout(3000);

        // 6. Click on 'iPhone 16 Pro'
        System.out.println("\uD83D\uDCF1 Clicking on 'iPhone 16 Pro'...");
        Locator iphone16Pro = popupPage.locator("a:has(div.brand-name-main:has-text('iPhone 16 Pro'))").first();
        iphone16Pro.waitFor(new Locator.WaitForOptions().setTimeout(10000).setState(WaitForSelectorState.VISIBLE));
        iphone16Pro.click();
        popupPage.waitForTimeout(3000);

        // 7. Apply In Stock Filter
        System.out.println("\u2705 Expanding 'Availability' filter...");
        Locator availabilityFilter = popupPage.locator("summary[aria-label^='Availability']");
        availabilityFilter.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        availabilityFilter.click();

        System.out.println("\u2705 Clicking 'In Stock' label to toggle checkbox...");
        Locator inStockLabel = popupPage.locator("label[for='Filter-filter.v.availability-1']");
        inStockLabel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        inStockLabel.click();

        popupPage.waitForFunction("() => document.querySelectorAll('li.grid__item').length > 0");
        popupPage.waitForTimeout(2000);

        // 8. Scrape product details from 2 pages
        System.out.println("\uD83D\uDCCB Scraping product details...");
        List<Product> products = new ArrayList<>();

        for (int pageNum = 0; pageNum < 2; pageNum++) {
            Locator cards = popupPage.locator("li.grid__item");
            int total = cards.count();

            for (int i = 0; i < total; i++) {
                Locator item = cards.nth(i);

                String title = safeText(item.locator("h3.card__heading a"));
                String discounted = safeText(item.locator("span.price-item--sale"));
                String original = item.locator("s.price-item--regular").count() > 0
                        ? safeText(item.locator("s.price-item--regular"))
                        : "N/A";
                String imageUrl = item.locator("img").getAttribute("src");

                int discountedPrice = extractPrice(discounted);
                products.add(new Product(title, original, discountedPrice, imageUrl));
            }

            // Next page
            Locator nextBtn = popupPage.locator("a[aria-label='Next']");
            if (nextBtn.count() > 0 && nextBtn.isVisible()) {
                nextBtn.click();
                popupPage.waitForFunction("() => document.querySelectorAll('li.grid__item').length > 0");
                popupPage.waitForTimeout(2000);
            } else {
                break;
            }
        }

        // 9. Sort and print
        System.out.println("\n\uD83D\uDCC8 Sorted Products by Discounted Price:");
        products.sort(Comparator.comparingInt(p -> p.discountedPrice));
        for (Product p : products) {
            System.out.println("\uD83C\uDFCD️ " + p.title);
            System.out.println("   \uD83D\uDCB0 Discounted: ₹" + p.discountedPrice);
            System.out.println("   \uD83D\uDD16 Original: " + p.originalPrice);
            System.out.println("   \uD83D\uDDBC️ Image: " + p.imageUrl + "\n");
        }

        // 🔄 Write to CSV
        try (PrintWriter writer = new PrintWriter("products.csv")) {
            writer.println("Title,Original Price,Discounted Price,Image URL");
            for (Product p : products) {
                writer.printf("\"%s\",\"%s\",%d,\"%s\"%n", p.title, p.originalPrice, p.discountedPrice, p.imageUrl);
            }
            System.out.println("\uD83D\uDCC1 Saved results to products.csv");
        } catch (Exception e) {
            System.out.println("\u26A0\uFE0F Failed to save CSV: " + e.getMessage());
        }

        System.out.println("\u2705 Task complete. Closing browser.");
        browser.close();
        playwright.close();
    }

    private static String safeText(Locator locator) {
        return locator.count() > 0 ? locator.first().innerText().trim() : "N/A";
    }

    private static int extractPrice(String price) {
        try {
            return Integer.parseInt(price.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    static class Product {
        String title;
        String originalPrice;
        int discountedPrice;
        String imageUrl;

        Product(String title, String originalPrice, int discountedPrice, String imageUrl) {
            this.title = title;
            this.originalPrice = originalPrice;
            this.discountedPrice = discountedPrice;
            this.imageUrl = imageUrl;
        }
    }
}
