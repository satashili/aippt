const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const path = require('path');
const bodyParser = require('body-parser');
const Anthropic = require('@anthropic-ai/sdk');

// 加载环境变量
dotenv.config({ path: path.join(__dirname, '.env') });

const app = express();
const PORT = process.env.PORT || 3001;

// 中间件
app.use(cors({
    origin: '*', // 允许所有来源
    methods: ['GET', 'POST'], // 允许的 HTTP 方法
    allowedHeaders: ['Content-Type', 'Accept'] // 允许的请求头
}));
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, '..')));

// Claude API 密钥
const CLAUDE_API_KEY = process.env.CLAUDE_API_KEY;
if (!CLAUDE_API_KEY) {
    console.error('错误: 缺少 CLAUDE_API_KEY 环境变量');
    process.exit(1);
}

// 初始化 Anthropic 客户端
const anthropic = new Anthropic({
    apiKey: CLAUDE_API_KEY
});

// 添加一个简单的测试端点
app.get('/api/claude/test', (req, res) => {
    res.json({ message: '代理服务器正常工作' });
});

// 代理 Claude API 的端点
app.post('/api/claude/generate-presentation', async (req, res) => {
    console.log('收到生成演示文稿请求:', req.body);
    
    try {
        const { prompt, style, slideCount, colorTheme } = req.body;

        if (!prompt) {
            console.error('缺少必要参数: prompt');
            return res.status(400).json({ message: '缺少必要参数: prompt' });
        }

        // 构建发送给 Claude 的系统提示和用户提示
        const systemPrompt = `你是一个专业的网页展示创建助手。你需要根据用户的描述创建一个HTML格式的网页展示。
遵循以下规则:
1. 根据用户给的文字信息，排版创建一个精美的网页，在一个html文件里实现
2. 设计需要精彩绝伦，有动画效果，要输出必要的css代码，可以自己设计图形，但是不要引用图片的链接
3. 网页设计要求：避免使用overflow:hidden，改用overflow-y:auto允许垂直滚动，容器设置overflow:visible，确保内容完整显示且响应式。
3. 风格: ${style || '请使用现代大气风格'}
4. 颜色主题: ${colorTheme || '请使用蓝色系精美配色'}
5. 如果客户输入是中文，请使用中文输出，如果客户输入是英文，请使用英文输出
6. 请保证网页在移动端的适配
7. 直接输出HTML代码，不要包含任何解释或其他文本`;

        const userPrompt = prompt;

        console.log('发送到 Claude 的提示:');
        console.log('系统提示:', systemPrompt);
        console.log('用户提示:', userPrompt);

        // 调用 Claude API
        const response = await anthropic.messages.create({
            model: "claude-3-7-sonnet-20250219",
            max_tokens: 40000,
            system: systemPrompt,
            messages: [{ role: "user", content: userPrompt }]
        });

        console.log('Claude API 响应:', response);

        // 提取 Claude 的响应内容
        const claudeResponse = response.content[0].text;
        console.log('Claude 响应内容:', claudeResponse);

        // 提取 HTML 内容
        let htmlContent = claudeResponse;
        
        // 如果响应包含代码块，提取代码块内容
        const htmlMatch = claudeResponse.match(/```html\n([\s\S]*?)\n```/) || 
                         claudeResponse.match(/```\n([\s\S]*?)\n```/);
        
        if (htmlMatch) {
            htmlContent = htmlMatch[1];
        }
        
        // 创建响应对象
        const presentationData = {
            title: prompt.length > 30 ? prompt.substring(0, 30) + '...' : prompt,
            html: htmlContent
        };

        console.log('成功生成演示文稿，返回数据');
        res.json(presentationData);
    } catch (error) {
        console.error('调用 Claude API 失败:', error.message);
        if (error.response) {
            console.error('Claude API 错误详情:', error.response.data);
        }
        res.status(500).json({
            message: '生成演示文稿失败',
            error: error.message
        });
    }
});

// 添加测试页面路由
app.get('/test', (req, res) => {
    res.sendFile(path.join(__dirname, 'test.html'));
});

// 启动服务器
app.listen(PORT, () => {
    console.log(`服务器运行在 http://localhost:${PORT}`);
    console.log(`API 测试页面: http://localhost:${PORT}/test`);
    console.log(`前端页面: http://localhost:${PORT}/index.html`);
}); 