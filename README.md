# Android Example

## 依赖于Google Firebase

## 测试
1. Android13+时间、时区基于网络，需要测试前后版本在有无网络的情况下，是否会崩溃。

## Git
```text
1. Github 不要选择初始化仓库，即不要去勾选自带README.md
```

```shell
# 生成Github认证公钥（Mac版）
ssh-keygen -t ed25519 -C "xxx@gmail.com"
cat ~/.ssh/id_ed25519.pub

# 1. 初始化项目
git init

# 2. 提交代码
git add .
git commit -m "xxx"

# 3. 确认分支是否正确, 不对就切换分支git checkout -b main/master
git branch 

# 4. 添加远程仓库
git remote add origin git@github.com:xxx/xx.git

# 5. 第一次提交需要变基
git pull origin main --rebase

# 6. 后续该pull就pull push就push
git pull origin main
git push origin main
```

