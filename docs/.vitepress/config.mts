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
        appId: '1E20LXYQNL',
        apiKey: '995870160d333576ea6b4b8ef45bedeb',
        indexName: 'ai-debug-tools',
        locales: {
          ...search,
        }
      }
    },
  }
})