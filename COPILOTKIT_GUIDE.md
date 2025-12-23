# CopilotKit AI 助手集成指南

本项目已成功集成 CopilotKit AI 助手，为家庭财务报表应用添加了智能对话功能。

## 功能特性

### 1. AI 财务助手
- **智能对话**：点击右下角的 AI 助手图标，即可打开聊天侧边栏
- **财务数据分析**：AI 可以读取并分析当前的财务数据
- **自动添加科目**：通过对话让 AI 帮你添加新的收入、支出、资产或负债科目
- **财务建议**：AI 可以根据你的财务数据提供分析和建议

### 2. AI 可执行的操作

#### 添加财务科目
AI 可以帮你添加新的财务科目。例如：
- "帮我添加一个工资收入科目，金额5000元"
- "添加一个餐饮支出科目"
- "记录一笔房贷负债，金额100万"

#### 分析财务状况
AI 可以分析你当前的财务状况：
- "分析一下我的财务状况"
- "我的收支情况怎么样？"
- "净资产是多少？"

## 配置步骤

### 1. 获取 API Key

你需要从 CopilotKit 获取 API Key：

1. 访问 [CopilotKit Cloud](https://cloud.copilotkit.ai/)
2. 注册并登录账号
3. 创建新项目
4. 复制 API Key

### 2. 配置环境变量

编辑项目根目录下的 `.env` 文件：

```bash
# CopilotKit API Configuration
VITE_COPILOT_CLOUD_API_KEY=your_copilotkit_api_key_here
```

将 `your_copilotkit_api_key_here` 替换为你的实际 API Key。

### 3. 启动应用

```bash
npm run dev
```

应用启动后，你会在右下角看到 AI 助手图标。

## 使用示例

### 示例 1: 添加收入科目
```
你: 帮我添加一个工资收入，金额8000元
AI: 已成功添加工资科目，金额为¥8,000
```

### 示例 2: 分析财务
```
你: 分析一下我这个月的财务状况
AI: 财务分析摘要：
- 收入总额：¥15,000
- 支出总额：¥8,500
- 现金流：¥6,500 (盈余)
- 资产总额：¥500,000
- 负债总额：¥200,000
- 净资产：¥300,000 (正值)
```

### 示例 3: 添加支出科目
```
你: 记录一笔房租支出3000元
AI: 已成功添加房租科目，金额为¥3,000
```

## 技术实现

### AI 可读数据 (useCopilotReadable)
AI 助手可以读取以下数据：
- 当前查看模式（年/月/日）
- 当前时期数据
- 财务摘要（收入、支出、资产、负债、现金流、净资产）
- 所有科目分组和项目

### AI 操作 (useCopilotAction)

#### 1. addFinancialItem
添加新的财务科目
- 参数：
  - `groupType`: 科目类型（income/expense/asset/liability）
  - `itemName`: 科目名称
  - `amount`: 金额（可选）

#### 2. analyzeFinances
分析当前财务状况
- 返回详细的财务分析报告

## 注意事项

1. **API Key 安全**：
   - 不要将 `.env` 文件提交到代码仓库
   - `.env` 文件已在 `.gitignore` 中

2. **API 配额**：
   - CopilotKit 有使用配额限制
   - 建议合理使用，避免频繁调用

3. **数据隐私**：
   - AI 助手会读取你的财务数据进行分析
   - 数据仅用于生成回复，不会被存储

## 自定义扩展

如果你想添加更多 AI 功能，可以在 `src/App.tsx` 中添加更多的 `useCopilotAction`：

```typescript
useCopilotAction({
  name: 'yourCustomAction',
  description: '你的自定义操作描述',
  parameters: [
    {
      name: 'param1',
      type: 'string',
      description: '参数描述',
      required: true,
    },
  ],
  handler: async ({ param1 }) => {
    // 你的处理逻辑
    return '操作结果';
  },
});
```

## 常见问题

### Q: AI 助手图标不显示？
A: 检查以下几点：
1. 确认已配置正确的 API Key
2. 确认应用已正确启动
3. 查看浏览器控制台是否有错误信息

### Q: AI 无法添加科目？
A: 确保当前已有数据加载，且科目类型正确（income/expense/asset/liability）

### Q: 如何关闭 AI 助手？
A: 点击侧边栏顶部的关闭按钮，或再次点击 AI 助手图标

## 更多资源

- [CopilotKit 官方文档](https://docs.copilotkit.ai/)
- [CopilotKit GitHub](https://github.com/CopilotKit/CopilotKit)
- [API 参考](https://docs.copilotkit.ai/reference)

---

祝你使用愉快！如有问题，欢迎反馈。
