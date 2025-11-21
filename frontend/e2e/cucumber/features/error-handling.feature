Feature: Error Handling and Exception Cases
  As a user
  I want to see appropriate error messages
  So that I understand when services are unavailable

  Scenario: Customer Service is unavailable
    Given I am on the home page
    And the Customer Service is not running
    When I navigate to the customers page
    Then I should see an error message about customer service unavailability
    And the application should remain stable

  Scenario: Inventory Service is unavailable
    Given I am on the home page
    And the Inventory Service is not running
    When I navigate to the products page
    Then I should see an error message about inventory service unavailability
    And the application should remain stable

  Scenario: Order Service is unavailable
    Given I am on the home page
    And the Order Service is not running
    When I navigate to the orders page
    Then I should see an error message about order service unavailability
    And the application should remain stable

  Scenario: Network timeout handling
    Given I am on the home page
    When I navigate to a page with a slow service
    Then I should see a loading indicator
    And eventually see an error message or data
    And the application should not crash

  Scenario: Retry after service recovery
    Given I am on the customers page
    And I see an error due to service unavailability
    When the service becomes available
    And I refresh or navigate back to the page
    Then I should see the customer data
    And no error messages should be displayed

  Scenario: Multiple service failures
    Given I am on the home page
    And all microservices are unavailable
    When I try to access customers, products, and orders
    Then I should see appropriate error messages for each service
    And the application should remain responsive
