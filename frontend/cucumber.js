module.exports = {
  default: {
    require: ['tests/cucumber/step_definitions/**/*.js'],
    format: ['progress', 'html:tests/cucumber/reports/cucumber-report.html'],
    paths: ['tests/cucumber/features/**/*.feature'],
    publishQuiet: true
  }
};
