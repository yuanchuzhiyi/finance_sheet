# GitHub Pages 部署指南

## 已完成的配置

✅ **Vite 配置更新**：设置 base 路径为 `/finance_sheet/`
✅ **GitHub Actions 工作流**：自动构建和部署

## 部署步骤

### 1. 启用 GitHub Pages

1. 访问 GitHub 仓库：https://github.com/yuanchuzhiyi/finance_sheet
2. 点击 **Settings** (设置)
3. 在左侧菜单中找到 **Pages**
4. 在 **Source** 下拉菜单中选择：
   - **Source**: GitHub Actions
5. 点击 **Save** (保存)

### 2. 推送代码触发部署

```bash
git add .
git commit -m "Configure GitHub Pages deployment"
git push
```

### 3. 等待部署完成

1. 访问仓库的 **Actions** 标签页
2. 查看 "Deploy to GitHub Pages" 工作流
3. 等待构建和部署完成（通常 2-5 分钟）
4. 部署成功后会显示绿色 ✓

### 4. 访问您的应用

部署完成后，应用将可以通过以下地址访问：

```
https://yuanchuzhiyi.github.io/finance_sheet/
```

## 配置说明

### Vite 配置 (vite.config.ts)

```typescript
export default defineConfig({
  base: '/finance_sheet/',  // GitHub Pages 子路径
  // ...
})
```

### GitHub Actions 工作流 (.github/workflows/deploy.yml)

- **触发条件**：推送到 main 分支或手动触发
- **构建步骤**：安装依赖 → 构建项目 → 上传构建产物
- **部署步骤**：部署到 GitHub Pages

## 优势

✅ **国内访问稳定**：GitHub Pages 在中国大陆访问较为稳定
✅ **自动部署**：推送代码后自动构建和部署
✅ **完全免费**：无需额外费用
✅ **HTTPS 支持**：自动提供 HTTPS 证书

## 注意事项

1. **首次部署**需要在 GitHub Settings 中启用 Pages
2. **域名格式**：`https://用户名.github.io/仓库名/`
3. **数据存储**：数据保存在浏览器 localStorage，不依赖后端
4. **更新方式**：推送代码到 main 分支即可自动更新

## 自定义域名（可选）

如果您有自己的域名，可以配置：

1. 在 GitHub Pages 设置中添加自定义域名
2. 在域名服务商处添加 CNAME 记录指向 `yuanchuzhiyi.github.io`
3. 更新 `vite.config.ts` 中的 `base: '/'`

## 故障排除

### 部署失败

1. 检查 Actions 日志查看具体错误
2. 确保 package.json 中的依赖都是正确的
3. 确保构建命令 `npm run build` 在本地能成功运行

### 页面 404

1. 确认 GitHub Pages 已启用
2. 确认 base 路径设置正确
3. 等待几分钟让 DNS 生效

### 页面空白

1. 按 F12 打开浏览器控制台查看错误
2. 检查资源加载路径是否正确
3. 清除浏览器缓存后重试

## 本地测试

在推送前可以本地测试构建：

```bash
npm run build
npm run preview
```

访问 http://localhost:4173/finance_sheet/ 预览
