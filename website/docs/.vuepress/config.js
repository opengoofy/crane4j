// 配置首页导航栏
const navConfig = [
    { text: '首页', link: '/' },
    {
        text: '源码',
        items: [
            { text: 'GitHub', link: 'https://github.com/opengoofy/crane4j' },
            { text: 'Gitee', link: 'https://gitee.com/CreateSequence/crane4j' }
        ]
    },
    {
        text: '关于作者',
        items: [
            { text: 'Github', link: 'https://github.com/Createsequence/' },
            { text: 'Gitee', link: 'https://gitee.com/CreateSequence' },
            { text: 'Blog', link: 'https://blog.xiajibagao.top' }
        ]
    },
    { text: '关于我们', link: 'https://github.com/opengoofy' }
];

// 侧边栏
const sidebarConfig = [
    {
        title: '指南',
        collapsable: false,
        children: [
            { title: "简介", path: "/user_guide/what_is_crane4j.md" },
            { title: "基本概念", path: "/user_guide/basic_concept.md" },
            { title: "原理", path: "/user_guide/operational_principle.md" },
            {
                title: '快速开始',
                path: "/user_guide/getting_started/getting_started_abstract.html",
                children: [
                    { title: "在springboot中使用", path: "/user_guide/getting_started/getting_started_with_springboot.md" },
                    { title: "在spring中使用 ", path: "/user_guide/getting_started/getting_started_with_spring.md" },
                    { title: "在非spring环境使用", path: "/user_guide/getting_started/getting_started_without_spring.md" }
                ]
            },
            {
                title: '场景示例',
                children: [
                    { title: "字典值自动转换", path: "/use_case/example_auto_fill_controller.md" },
                    { title: "填充一个复杂对象", path: "/use_case/example_multi_datasource.md" },
                ]
            },
            { title: "常见问题", path: "/user_guide/faq.md" },
        ]
    },
    {
        title: '基础',
        collapsable: false,
        children: [
            { title: "声明装配操作", path: "/basic/declare_assemble_operation.md" },
            { title: "配置属性映射", path: "/basic/multi_assemble_operation.md" },
            {
                title: '数据源容器',
                path: "/basic/container/container_abstract.html",
                children: [
                    { title: "Map集合", path: "/basic/container/map_container.md" },
                    { title: "枚举类", path: "/basic/container/enum_container.md" },
                    { title: "常量类", path: "/basic/container/constant_container.md" },
                    { title: "lambda表达式", path: "/basic/container/lambda_container.md" },
                    { title: "方法", path: "/basic/container/method_container.md" },
                    { title: "对象", path: "/basic/container/object_container.md" },
                    { title: "内省", path: "/basic/container/introspection_container.md" },
                    { title: "自定义", path: "/basic/container/custom_container.md" },
                    { title: "提供者", path: "/basic/container/container_provider.md" },
                ]
            },
            { title: "触发填充操作", path: "/basic/trigger_operation.md" },
            { title: "填充嵌套对象", path: "/basic/declare_disassemble_operation.md" },
            { title: "一对一&多对多", path: "/basic/multi_assemble_operation.md" },
            { title: "分组填充", path: "/basic/operation_group.md" },
            { title: "顺序填充", path: "/basic/operation_sort.md" }
        ]
    },
    {
        title: '进阶',
        collapsable: false,
        children: [
            { title: '缓存', path: "/advanced/cache.md"},
            { title: '组合注解', path: "/advanced/combination_annotation.md"},
            { title: '容器的生命周期', path: "/advanced/container_lifecycle.md"},
            { title: '注解处理器', path: "/advanced/operation_annotation_handler.md"},
            { title: '使用抽象方法填充', path: "/advanced/operator_interface.md"},
            { title: '反射工厂', path: "/advanced/reflection_factory.md"},
            { title: '类型转换', path: "/advanced/type_converter.md"}
        ]
    },
    {
        title: '扩展插件',
        collapsable: false,
        children: [
            { title: 'MybatisPlus', path: "/extension/mybatis_plus_extension.md"},
            { title: 'Jackson', path: "/extension/jackson_extension.md"},
        ]
    },
    {
        title: '其他',
        collapsable: false,
        children: [
            { title: '配置文件', path: "/other/configuration_properties.md"},
            { title: '更新日志', path: "/other/changelog.md"},
            { title: '联系作者', path: "/other/community.md"},
            { title: '提问的智慧', path: "/other/How-To-Ask-Questions-The-Smart-Way.md"},
        ]
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