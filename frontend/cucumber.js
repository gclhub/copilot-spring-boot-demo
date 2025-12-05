module.exports = {
  default: {
    paths: ['e2e/cucumber/features/**/*.feature'],
    require: ['e2e/cucumber/step_definitions/**/*.ts', 'e2e/cucumber/support/**/*.ts'],
    requireModule: ['ts-node/register', 'source-map-support/register'],
    format: [
      'progress',
      'html:cucumber-reports/cucumber-report.html',
      'json:cucumber-reports/cucumber-report.json'
    ],
    publishQuiet: true,
    parallel: 1
  }
};
