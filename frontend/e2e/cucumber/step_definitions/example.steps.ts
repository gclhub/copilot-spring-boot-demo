import { Given, When, Then } from '@cucumber/cucumber';

Given('I am on the home page', async function () {
  // Example step - implement navigation to home page
  console.log('Navigate to home page');
});

When('I navigate to the customers page', async function () {
  // Example step - implement navigation to customers page
  console.log('Navigate to customers page');
});

Then('I should see a list of customers', async function () {
  // Example step - implement verification of customers list
  console.log('Verify customers list is visible');
});

When('I navigate to the products page', async function () {
  // Example step - implement navigation to products page
  console.log('Navigate to products page');
});

Then('I should see a list of products', async function () {
  // Example step - implement verification of products list
  console.log('Verify products list is visible');
});
