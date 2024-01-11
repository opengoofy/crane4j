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