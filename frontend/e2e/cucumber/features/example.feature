Feature: E-Commerce Microservices Demo
  As a user
  I want to view data from microservices
  So that I can verify the system is working

  Scenario: View customers list
    Given I am on the home page
    When I navigate to the customers page
    Then I should see a list of customers

  Scenario: View products list
    Given I am on the home page
    When I navigate to the products page
    Then I should see a list of products
