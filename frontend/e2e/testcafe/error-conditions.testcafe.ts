import { Selector } from 'testcafe';

fixture('Error Conditions - Service Unavailability')
  .page('http://localhost:4200');

test('Should display error message when Customer Service is unavailable', async (t) => {
  // Example test - verify error handling when customer service is down
  // Stop the customer service (port 8081) before running this test
  await t
    .click(Selector('button').withText('Customers'))
    .expect(Selector('.error-message').exists).ok('Error message should be displayed')
    .expect(Selector('.error-message').innerText)
    .contains('Failed to load customers', 'Should show appropriate error message');
});

test('Should display error message when Inventory Service is unavailable', async (t) => {
  // Example test - verify error handling when inventory service is down
  // Stop the inventory service (port 8082) before running this test
  await t
    .click(Selector('button').withText('Products'))
    .expect(Selector('.error-message').exists).ok('Error message should be displayed')
    .expect(Selector('.error-message').innerText)
    .contains('Failed to load products', 'Should show appropriate error message');
});

test('Should display error message when Order Service is unavailable', async (t) => {
  // Example test - verify error handling when order service is down
  // Stop the order service (port 8083) before running this test
  await t
    .click(Selector('button').withText('Orders'))
    .expect(Selector('.error-message').exists).ok('Error message should be displayed')
    .expect(Selector('.error-message').innerText)
    .contains('Failed to load orders', 'Should show appropriate error message');
});

test('Should not crash on network timeout', async (t) => {
  // Example test - verify graceful handling of network timeouts
  await t
    .click(Selector('button').withText('Customers'))
    .wait(5000) // Wait for potential timeout
    .expect(Selector('mat-spinner').exists).notOk('Loading spinner should disappear after error')
    .expect(Selector('mat-toolbar').exists).ok('Application should still be functional');
});

test('Should allow retry after service becomes available', async (t) => {
  // Example test - verify user can retry after service recovery
  await t
    .click(Selector('button').withText('Customers'))
    .expect(Selector('.error-message').exists).ok('Error message should be displayed first')
    // In a real test, you would restart the service here
    // Then navigate away and back to retry
    .click(Selector('button').withText('Home'))
    .click(Selector('button').withText('Customers'));
});
