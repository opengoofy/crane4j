cd website
npm run build
echo '构建完毕......'
cd ..
git add .
git commit -m 'docs: update doc'
git push origin gh-pages
echo '推送完毕......'
read -n 1