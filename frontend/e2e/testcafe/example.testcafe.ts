import { Selector } from 'testcafe';

fixture('E-Commerce Microservices Demo')
  .page('http://localhost:4200');

test('Home page should load', async (t) => {
  // Example test - verify the home page loads
  await t
    .expect(Selector('mat-toolbar').exists).ok('Toolbar should be visible')
    .expect(Selector('mat-card-title').innerText).contains('E-Commerce');
});

test('Navigate to customers page', async (t) => {
  // Example test - navigate to customers page
  await t
    .click(Selector('button').withText('Customers'))
    .expect(Selector('mat-card-title').innerText).contains('Customers');
});
