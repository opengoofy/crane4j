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
    },
    {
        text: '源码',
        items: [
            { text: 'GitHub', link: 'https://github.com/opengoofy/crane4j' },
            { text: 'Gitee', link: 'https://gitee.com/CreateSequence/crane4j' }
        ]
    }
];

// 侧边栏
const sidebarConfig = [
    {
        title: '1.基础',
        collapsable: false, // 不折叠
        children: [
            { title: "1.1.用户指南", path: "/basic/1.1.用户指南.md" },
            {
                title: "1.2.快速开始",
                path: "/basic/quickstart/1.2.0.快速开始.html",
                children: [
                    { title: "1.2.1.在非spring项目中使用", path: "/basic/quickstart/1.2.1.在非spring项目中使用.md" },
                    { title: "1.2.2.在spring项目中使用", path: "/basic/quickstart/1.2.2.在spring项目中使用.md" },
                    { title: "1.2.3.在springboot项目中使用", path: "/basic/quickstart/1.2.3.在springboot项目中使用.md" }
                ]
            },
            { title: "1.3.配置文件", path: "/basic/1.3.配置文件.md" },
            { title: "1.4.源码设计", path: "/basic/1.4.源码设计.md" },
            { title: "1.5.更新日志", path: "/basic/1.5.更新日志.md" }
        ]
    },
    {
        title: '2.数据源容器',
        path: "/container/2.0.数据源容器.html",
        collapsable: false, // 不折叠
        children: [
            { title: "2.1.本地缓存", path: "/container/2.1.本地缓存容器.md" },
            { title: "2.2.枚举", path: "/container/2.2.枚举容器.md" },
            { title: "2.3.常量", path: "/container/2.3.常量容器.md" },
            { title: "2.4.lambda表达式", path: "/container/2.4.lambda容器.md" },
            { title: "2.5.可调用方法", path: "/container/2.5.方法容器.md" },
            { title: "2.6.对象内省", path: "/container/2.6.对象内省.md" },
            { title: "2.7.接口/自定义", path: "/container/2.7.自定义容器.md" },
            { title: "2.8.容器提供者", path: "/container/2.8.容器提供者.html" }
        ]
    },
    {
        title: '3.操作配置',
        collapsable: false, // 不折叠
        path: "/operation/3.0.操作配置.html",
        children: [
            { title: "3.1.声明装配操作", path: "/operation/3.1.声明装配操作.md" },
            { title: "3.2.配置属性映射", path: "/operation/3.2.配置属性映射.md" },
            { title: "3.3.指定装配处理器", path: "/operation/3.3.指定装配处理器.md" },
            { title: "3.4.拆卸嵌套对象", path: "/operation/3.4.拆卸嵌套对象.md" },
            { title: "3.5.操作分组", path: "/operation/3.5.操作分组.md" },
            { title: "3.6.操作排序", path: "/operation/3.6.操作排序.md" }
        ]
    },
    {
        title: '4.执行操作',
        collapsable: false, // 不折叠
        children: [
            { title: "4.1.手动填充", path: "/execute/4.1.手动填充.md" },
            { title: "4.2.自动填充", path: "/execute/4.2.自动填充.md" },
            { title: "4.3.操作者接口", path: "/execute/4.3.操作者接口.md" },
            { title: "4.4.操作执行器", path: "/execute/4.4.操作执行器.md" }
        ]
    },
    {
        title: "5.高级特性",
        collapsable: false, // 不折叠
        children: [
            { title: "5.1.缓存", path: "/advance/5.1.缓存.md" },
            { title: "5.2.组合注解", path: "/advance/5.2.组合注解.md" },
            { title: "5.3.容器的生命周期回调", path: "/advance/5.3.容器的生命周期回调.md" },
            { title: "5.4.反射工厂", path: "/advance/5.4.反射工厂.md" },
            { title: "5.5.操作注解解析器", path: "/advance/5.5.操作注解解析器.md" },
            { title: "5.5.类型转换", path: "/advance/5.6.类型转换.md" }
        ],
    },
    {
        title: "6.扩展组件",
        collapsable: false, // 不折叠
        children: [
            { title: "6.1.MybatisPlus扩展.md", path: "/extension/6.1.MybatisPlus扩展.md" },
        ],
    },
    {
        title: "其他",
        collapsable: false, // 不折叠
        children: [
            { title: "联系作者", path: "/other/联系作者.md" },
            { title: "提问的智慧", path: "/other/提问的智慧.md" }
        ],
    }
]

module.exports = {
    title: 'Crane4j',
    description: 'Crane4j, 基于注解的数据关联框架',
    base: '/crane4j/',
    // base: './',
    dest: "./../docs/",
    head: [
        ['link', { rel: 'icon', href: 'CRANE4J_ICON.png'}]
    ],
    themeConfig: {
        // logo: 'CRANE4J_ICON.png',
        // git仓库
        // repo: 'https://github.com/opengoofy/crane4j',
        sidebarDepth: 3,
        // 配置首页导航栏
        nav: navConfig,
        // 侧边栏
        sidebar: sidebarConfig
    }
}