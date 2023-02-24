// 配置首页导航栏
const navConfig = [
      { text: '首页', link: '/' },
      { text: '文档', link: '/basic/1.用户指南.md' },
      { 
        text: '关于作者', 
        items: [
          { text: 'Github', link: 'https://github.com/Createsequence/' },
          { text: 'Gitee', link: 'https://gitee.com/CreateSequence' },
          { text: 'Blog', link: 'https://blog.xiajibagao.top' }
        ]
      }
    ];

// 侧边栏
const sidebarConfig = [
      {
        title: '入门',
        path: '/basic/',
        collapsable: false, // 不折叠
        children: [
          { title: "1.用户指南", path: "/basic/1.用户指南.md" },
          { title: "2.快速开始", path: "/basic/2.快速开始.md" },
          { title: "3.配置文件", path: "/basic/3.配置文件.md" },
          { title: "4.源码设计", path: "/basic/4.源码设计.md" }
        ]
      },
      {
        title: "使用",
        path: '/advance/',
        collapsable: false, // 不折叠
        children: [
          { title: "1.数据源容器", path: "/advance/1.数据源容器.md" },
          { title: "2.装配操作", path: "/advance/2.装配操作.md" },
          { title: "3.字段映射", path: "/advance/3.字段映射.md" },
          { title: "4.拆卸操作", path: "/advance/4.拆卸操作.md" },
          { title: "5.自动填充", path: "/advance/5.自动填充.md" },
          { title: "6.分组过滤", path: "/advance/6.分组过滤.md" },
          { title: "7.缓存", path: "/advance/7.缓存.md" },
          { title: "8.操作执行器", path: "/advance/8.操作执行器.md" }
        ],
      },
      {
        title: "其他",
        path: '/other/',
        collapsable: false, // 不折叠
        children: [
          { title: "联系作者", path: "/other/联系作者.md" },
          { title: "提问的智慧", path: "/other/提问的智慧.md" }
        ],
      }
    ]


module.exports = {
  title: 'Cranej4',
  description: 'Cranej4, 基于注解的数据关联框架',
  // base: '/crane4j/',
  base: './',
  head: [
    ['link', { rel: 'icon', href: 'CRANE4J_ICON.png'}]
  ],
  themeConfig: {
    // logo: 'CRANE4J_ICON.png',
    // git仓库
    repo: 'https://github.com/Createsequence/crane4j',
    sidebarDepth: 3,
    // 配置首页导航栏
    nav: navConfig,
    // 侧边栏
    sidebar: sidebarConfig
  }
}