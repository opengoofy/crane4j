cd website

# 切换到本地的 dev 分支
git checkout dev
# 删除本地和gitee远程的dev-doc分支
git branch -D dev-doc
git push origin --delete dev-doc
# 从本地的 dev 分支创建新的 dev-doc 分支
git checkout -b dev-doc
echo '重新建立dev-doc分支......'
# 切换到 dev-doc 分支
git checkout dev-doc

# 修改config.js文件中的base路径
sed -i "s/base: '\/crane4j\//base: '\/crane4j-doc\//" ./docs/.vuepress/config.js
echo '修改base路径为 "/crane-doc/"......'

# 重新部署
cd ..
./deploy.sh

#npm run build
#echo '构建完毕......'
#cd ..
#git add .
#git commit -m 'docs: update doc'
##git push origin gh-pages
#echo '提交完毕......'
#read -n 1