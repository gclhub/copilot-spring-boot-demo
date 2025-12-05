module.exports = {
  default: {
    paths: ['e2e/cucumber/features/**/*.feature'],
    require: [
      'e2e/cucumber/support/**/*.ts',
      'e2e/cucumber/step_definitions/**/*.ts'
    ],
    requireModule: ['ts-node/register', 'source-map-support/register'],
    format: [
      'progress',
      'html:cucumber-reports/cucumber-report.html',
      'json:cucumber-reports/cucumber-report.json'
    ],
    formatOptions: {
      snippetInterface: 'async-await'
    },
    publishQuiet: true,
    parallel: 1,
    timeout: 30000  // 30 seconds timeout for steps
  }
};
