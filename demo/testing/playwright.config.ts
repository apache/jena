import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./tests",
  timeout: 30_000,
  retries: 0,
  workers: 1, // sequential — shared metadata state
  use: {
    baseURL: process.env.APP_URL || "http://localhost:8000/",
    viewport: { width: 1440, height: 900 },
    screenshot: "off", // we take screenshots manually
  },
  projects: [{ name: "chromium", use: { browserName: "chromium" } }],
});
