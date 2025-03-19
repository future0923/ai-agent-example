import { defineConfig } from 'vitepress'

import { search , zh } from "./locales/zh"

// https://vitepress.dev/reference/site-config
export default defineConfig({
  sitemap: {
    hostname: 'https://ai.debugtools.cc',
  },
  lastUpdated: true,
  locales: {
    root: {
      label: '简体中文',
      ...zh
    },
  },
  themeConfig: {
    search: {
      provider: 'algolia',
      options: {
        appId: 'Q3PQ9B9Z5C',
        apiKey: 'c1efd92fbc0ff7c6829d99c17a7f9926',
        indexName: 'debug-tools',
        locales: {
          ...search,
        }
      }
    },
  }
})