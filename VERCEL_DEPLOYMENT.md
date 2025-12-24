# Vercel 部署指南

## 问题诊断与修复

已完成的修复：

1. ✅ **缺少 vercel.json 配置** - 已创建 SPA 路由配置
2. ✅ **后端 API 依赖导致卡住** - 已添加超时和环境变量检查
3. ✅ **路由配置问题** - 已通过 rewrites 修复

## 核心问题说明

原项目设计为前后端分离架构，包含：
- 前端：React + Vite (存储在浏览器 localStorage)
- 后端：Python FastAPI (可选的远程同步服务)

在 Vercel 上仅部署前端时，应用会尝试连接后端 API（`http://localhost:8000`），导致页面加载卡住。

**解决方案：**
- 添加了环境变量检查，未配置 `VITE_API_URL` 时自动禁用远程存储
- 添加了 3 秒超时机制，防止 API 请求阻塞应用
- 应用可以完全独立运行，数据保存在浏览器本地

## 部署步骤

### 1. 提交新的配置文件

```bash
git add vercel.json
git commit -m "Add Vercel configuration"
git push
```

### 2. 在 Vercel 控制台设置环境变量

访问你的 Vercel 项目设置：
- 进入 Vercel Dashboard
- 选择你的项目
- 点击 "Settings" → "Environment Variables"
- 添加以下环境变量：

**可选的环境变量：**

应用可以不设置任何环境变量直接运行（数据仅保存在浏览器本地）。

如果需要启用远程同步功能，可配置：
```
VITE_API_URL=你的后端API地址
```

> 注意：CopilotKit 功能当前未激活，`VITE_COPILOT_CLOUD_API_KEY` 暂时不需要配置

### 3. 重新部署

设置环境变量后，有两种方式触发重新部署：

**方式 1：自动重新部署**
- Vercel 会自动检测到 git push 并重新部署

**方式 2：手动触发**
- 在 Vercel Dashboard 中点击 "Deployments"
- 找到最新的部署
- 点击右侧的 "..." 菜单
- 选择 "Redeploy"

### 4. 验证部署

部署完成后：
1. 访问 Vercel 提供的 URL（通常是 `https://your-project.vercel.app`）
2. 检查页面是否正常加载
3. 打开浏览器控制台（F12）查看是否有错误

## vercel.json 配置说明

已创建的配置包含：

```json
{
  "buildCommand": "npm run build",        // 构建命令
  "outputDirectory": "dist",              // 输出目录
  "devCommand": "npm run dev",            // 开发命令
  "installCommand": "npm install",        // 安装依赖
  "framework": "vite",                    // 框架类型
  "rewrites": [
    {
      "source": "/(.*)",                  // 所有路由
      "destination": "/index.html"         // 重定向到 index.html（SPA 必需）
    }
  ]
}
```

## 常见问题

### Q: 页面显示 404
A: 确保 `rewrites` 配置正确，这对 SPA 应用至关重要

### Q: 页面空白但没有错误
A: 检查环境变量是否正确设置，特别是 `VITE_COPILOT_CLOUD_API_KEY`

### Q: 构建失败
A: 检查构建日志，可能是依赖安装问题，确保 package.json 中的依赖都是正确的

### Q: 本地运行正常但部署后不工作
A: 检查环境变量，Vite 的环境变量必须以 `VITE_` 开头才能在客户端使用

## 测试本地构建

在推送到 Vercel 前，可以先本地测试构建：

```bash
# 构建项目
npm run build

# 预览构建结果
npm run preview
```

访问 http://localhost:4173 查看构建后的应用

## 注意事项

1. **环境变量前缀**：Vite 要求客户端可访问的环境变量必须以 `VITE_` 开头
2. **不要提交 .env 文件**：已在 .gitignore 中排除，敏感信息不要提交到 Git
3. **生产环境 API Key**：确保在 Vercel 中使用生产环境的 API Key
