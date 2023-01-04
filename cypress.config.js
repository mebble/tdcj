const { defineConfig } = require("cypress");

module.exports = defineConfig({
  video: false,
  screenshotOnRunFailure: false,
  e2e: {
    baseUrl: 'http://localhost:8280',
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
