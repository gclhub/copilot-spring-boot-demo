module.exports = {
  default: {
    require: ['e2e/cucumber/step_definitions/**/*.ts', 'e2e/cucumber/support/**/*.ts'],
    requireModule: ['ts-node/register'],
    format: ['progress', 'html:cucumber-reports/cucumber-report.html'],
    publishQuiet: true
  }
};
