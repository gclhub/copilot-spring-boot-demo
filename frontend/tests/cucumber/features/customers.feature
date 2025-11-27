Feature: Customer Management
  As a user
  I want to manage customers
  So that I can view and interact with customer data

  Scenario: View customer list
    Given I am on the customer page
    When the page loads
    Then I should see a list of customers

  Scenario: View customer details
    Given I am on the customer page
    When I click on a customer
    Then I should see the customer details
