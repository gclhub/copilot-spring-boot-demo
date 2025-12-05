import { Before, After, Status, setDefaultTimeout } from '@cucumber/cucumber';
import { CustomWorldClass } from './world';

// Set default timeout to 30 seconds
setDefaultTimeout(30 * 1000);

Before(async function (this: CustomWorldClass) {
  await this.init();
});

After(async function (this: CustomWorldClass, { result }) {
  // Take screenshot on failure
  if (result?.status === Status.FAILED && this.page) {
    const screenshot = await this.page.screenshot();
    this.attach(screenshot, 'image/png');
  }
  
  await this.cleanup();
});
