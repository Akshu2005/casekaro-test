Feature: CaseKaro Product Search

  Scenario: Search and scrape Apple iPhone 16 Pro products
    Given I launch the browser
    When I navigate to the CaseKaro website
    And I click on Mobile Covers
    And I search for "Apple"
    Then I should not see banned brands
    And I click on the Apple brand
    And I select iPhone 16 Pro
    And I apply In Stock filter
    Then I scrape products and save to CSV
