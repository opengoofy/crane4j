echo "commit message: "
read commit_message

# 构建 github 文档
echo '开始构建github文档......'
cd website
npm run build
echo '文档构建完毕......'
cd ..
git add .
git commit -m "$commit_message"
echo 'github文档提交完毕(dev)......'

echo '开始构建gitee文档......'
cd website
git checkout devs
# 删除本地和gitee远程的dev-doc分支
git branch -D dev-doc
git push gitee-doc --delete dev-doc
# 从本地的 dev 分支创建新的 dev-doc 分支
git checkout -b dev-doc
echo '重新建立dev-doc分支......'
git checkout dev-doc
# 修改config.js文件中的base路径
sed -i "s/base: '\/crane4j\//base: '\/crane4j-doc\//" ./docs/.vuepress/config.js
echo '修改base路径为 "/crane-doc/"......'
npm run build
echo '文档构建完毕......'
cd ..
git add .
git commit --amend
echo 'gitee文档提交完毕(dev-doc)......'