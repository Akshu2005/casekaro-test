package com.gocomet.assignment;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.util.*;

public class CaseKaroSteps {
    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;
    private static Page popupPage;
    private final List<Product> products = new ArrayList<>();

    @Given("I launch the browser")
    public void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    @When("I navigate to the CaseKaro website")
    public void navigateToWebsite() {
        page.navigate("https://casekaro.com/");
        page.waitForTimeout(3000);
    }

    @And("I click on Mobile Covers")
    public void clickMobileCovers() {
        Locator mobileCover = page.locator("a#HeaderMenu-mobile-covers");
        mobileCover.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        mobileCover.click();
        page.waitForTimeout(3000);
    }

    @And("I search for {string}")
    public void searchFor(String keyword) {
        Locator searchBtn = page.locator("button[aria-label='Search']");
        if (searchBtn.isVisible()) {
            searchBtn.click();
        } else {
            page.evaluate("document.querySelector(\"button[aria-label='Search']\").click()");
        }

        Locator searchInput = page.locator("input#search-bar-cover-page");
        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchInput.fill(keyword);
        searchInput.press("Enter");
        page.waitForTimeout(2000);
    }

    @Then("I should not see banned brands")
    public void validateBannedBrands() {
        List<String> bannedBrands = Arrays.asList("samsung", "vivo", "oneplus", "realme", "mi");
        List<String> titles = page.locator(".product-title").allInnerTexts();
        for (String title : titles) {
            String lower = title.toLowerCase();
            for (String brand : bannedBrands) {
                assertFalse(lower.contains(brand), "❌ Brand '" + brand + "' found in: " + title);
            }
        }
    }

    @And("I click on the Apple brand")
    public void clickAppleBrand() {
        Locator appleButton = page.locator("button.brand-name-container:has(a:text('Apple'))");
        appleButton.waitFor(new Locator.WaitForOptions().setTimeout(10000).setState(WaitForSelectorState.VISIBLE));
        popupPage = page.waitForPopup(() -> appleButton.click());
        popupPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
        popupPage.waitForTimeout(3000);
    }

    @And("I select iPhone 16 Pro")
    public void selectiPhone16Pro() {
        Locator iphone16Pro = popupPage.locator("a:has(div.brand-name-main:has-text('iPhone 16 Pro'))").first();
        iphone16Pro.waitFor(new Locator.WaitForOptions().setTimeout(10000).setState(WaitForSelectorState.VISIBLE));
        iphone16Pro.click();
        popupPage.waitForTimeout(3000);
    }

    @And("I apply In Stock filter")
    public void applyInStockFilter() {
        Locator availabilityFilter = popupPage.locator("summary[aria-label^='Availability']");
        availabilityFilter.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        availabilityFilter.click();

        Locator inStockLabel = popupPage.locator("label[for='Filter-filter.v.availability-1']");
        inStockLabel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        inStockLabel.click();

        popupPage.waitForFunction("() => document.querySelectorAll('li.grid__item').length > 0");
        popupPage.waitForTimeout(2000);
    }

    @Then("I scrape products and save to CSV")
    public void scrapeAndSaveProducts() {
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

            Locator nextBtn = popupPage.locator("a[aria-label='Next']");
            if (nextBtn.count() > 0 && nextBtn.isVisible()) {
                nextBtn.click();
                popupPage.waitForFunction("() => document.querySelectorAll('li.grid__item').length > 0");
                popupPage.waitForTimeout(2000);
            } else {
                break;
            }
        }

        products.sort(Comparator.comparingInt(p -> p.discountedPrice));
        try (PrintWriter writer = new PrintWriter("products.csv")) {
            writer.println("Title,Original Price,Discounted Price,Image URL");
            for (Product p : products) {
                writer.printf("\"%s\",\"%s\",%d,\"%s\"%n", p.title, p.originalPrice, p.discountedPrice, p.imageUrl);
            }
            System.out.println("✅ Saved results to products.csv");
        } catch (Exception e) {
            System.out.println("⚠️ Failed to save CSV: " + e.getMessage());
        }

        browser.close();
        playwright.close();
    }

    private String safeText(Locator locator) {
        return locator.count() > 0 ? locator.first().innerText().trim() : "N/A";
    }

    private int extractPrice(String price) {
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
