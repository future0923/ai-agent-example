import { createRequire } from 'module'
import { DefaultTheme, defineConfig } from 'vitepress'

const require = createRequire(import.meta.url)
const pkg = require('../../package.json')

export const zh = defineConfig({
  lang: 'zh-Hans',
  head: [
    ['meta', {name: 'theme-color', content: '#389BFF'}],
    [
      'script',
      {},
      `var _hmt = _hmt || [];
            (function() {
              var hm = document.createElement("script");
              hm.src = "https://hm.baidu.com/hm.js?8037de16a5792e203ce7aed2fe892e69";
              var s = document.getElementsByTagName("script")[0]; 
              s.parentNode.insertBefore(hm, s);
            })();`
    ],
  ],
  title: 'Ai Agent',
  titleTemplate: '智能体(ai agent)开发示例',
  description: "智能体（ai agent）开发的相关知识与代码示例",
  themeConfig: {
    siteTitle: 'Ai Agent',
    logo: '/pluginIcon.svg',
    // https://vitepress.dev/reference/default-theme-config
    nav: nav(),
    outline: {
      level: 'deep',
      label: '页面导航'
    },
    editLink: {
      pattern: 'https://github.com/future0923/ai-agent-example/edit/main/docs/:path',
      text: '在 GitHub 上编辑此页面'
    },
    docFooter: {
      prev: '上一页',
      next: '下一页'
    },
    returnToTopLabel: '回到顶部',
    darkModeSwitchLabel: '主题',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式',
    lastUpdated: {
      text: '最后更新于',
    },
    sidebar: {
      '/guide': {
        base: '/guide/',
        items: sidebarGuide()
      },
      '/spring': {
        base: '/spring/',
        items: sidebarSpring()
      },
    },
    socialLinks: [
      {icon: 'github', link: 'https://github.com/future0923/ai-agent-example'}
    ],
    footer: {
      message: `基于 Apache 许可发布 | 版权所有 © 2024-${new Date().getFullYear()} <a href="https://github.com/future0923/" target="_blank">Future0923</a>`,
      copyright: '<a href="https://beian.miit.gov.cn/" target="_blank">吉ICP备2024021764号-1</a> | <img src="/icon/beian.png" alt="" style="display: inline-block; width: 18px; height: 18px; vertical-align: middle;" /> <a href="https://beian.mps.gov.cn/#/query/webSearch?code=22010302000528" rel="noreferrer" target="_blank">吉公网安备22010302000528</a>'
    },
  }
})

function nav(): DefaultTheme.NavItem[] {
  return [
    {
      text: '概念',
      link: '/guide/concepts',
      activeMatch: '/guide/'
    },
    {
      text: 'SpringAI',
      link: '/spring/concepts',
      activeMatch: '/spring/'
    },
    {
      text: 'DebugTools',
      link: 'https://debug-tools.cc/zh',
    },
  ]
}

function sidebarGuide(): DefaultTheme.SidebarItem[] {
  return [
    {
      text: '概览',
      collapsed: false,
      items: [
        {text: 'AI核心概念', link: 'concepts'},
      ]
    },
  ]
}

function sidebarSpring(): DefaultTheme.SidebarItem[] {
  return [
    {
      text: '概览',
      collapsed: false,
      items: [
        {text: 'Spring AI', link: 'concepts'},
      ]
    },
    {
      text: '教程',
      collapsed: false,
      items: [
        {text: '聊天模型(Chat Model)', link: 'chat-model'},
        {text: '嵌入模型(Embedding Model)', link: 'embedding-model'},
        {text: 'Chat Client', link: 'chat-client'},
        {text: '工具(Tool)/功能调用(Function Calling)', link: 'function-calling'},
        {text: '结构化输出(Structured Output)', link: 'structured-output'},
        {text: '文档检索(Document Retriever)', link: 'document-retriever'},
        {text: 'ETL管道(ETL Pipeline)', link: 'etl-pipeline'},
        {text: '向量存储(Vector Store)', link: 'vector-store'},
        {text: '索引增强生成(RAG)', link: 'rag'},
        {text: '聊天记忆(Chat Memory)', link: 'chat-memory'},
        {text: '模型上下文协议(MCP)', link: 'mcp'}
      ]
    },
  ]
}

export const search: DefaultTheme.AlgoliaSearchOptions['locales'] = {
  zh: {
    placeholder: '搜索文档',
    translations: {
      button: {
        buttonText: '搜索文档',
        buttonAriaLabel: '搜索文档'
      },
      modal: {
        searchBox: {
          resetButtonTitle: '清除查询条件',
          resetButtonAriaLabel: '清除查询条件',
          cancelButtonText: '取消',
          cancelButtonAriaLabel: '取消'
        },
        startScreen: {
          recentSearchesTitle: '搜索历史',
          noRecentSearchesText: '没有搜索历史',
          saveRecentSearchButtonTitle: '保存至搜索历史',
          removeRecentSearchButtonTitle: '从搜索历史中移除',
          favoriteSearchesTitle: '收藏',
          removeFavoriteSearchButtonTitle: '从收藏中移除'
        },
        errorScreen: {
          titleText: '无法获取结果',
          helpText: '你可能需要检查你的网络连接'
        },
        footer: {
          selectText: '选择',
          navigateText: '切换',
          closeText: '关闭',
          searchByText: '搜索提供者'
        },
        noResultsScreen: {
          noResultsText: '无法找到相关结果',
          suggestedQueryText: '你可以尝试查询',
          reportMissingResultsText: '你认为该查询应该有结果？',
          reportMissingResultsLinkText: '点击反馈'
        }
      }
    }
  }
}