cd website
npm run build
echo '构建完毕......'
cd ..
git add .
git commit -m 'docs: update doc'
#git push origin gh-pages
echo '提交完毕......'
read -n 1