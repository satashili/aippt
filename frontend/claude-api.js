// Claude API 处理函数
const CLAUDE_API_URL = 'http://localhost:3001/api/claude'; // 确保这个端口与服务器端口匹配

// 生成演示文稿
async function generatePresentation(prompt, style, slideCount, colorTheme) {
    try {
        console.log('正在发送请求到:', `${CLAUDE_API_URL}/generate-presentation`);
        console.log('请求数据:', { prompt, style, slideCount, colorTheme });
        
        const response = await fetch(`${CLAUDE_API_URL}/generate-presentation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                prompt,
                style,
                slideCount,
                colorTheme
            }),
            mode: 'cors' // 明确指定 CORS 模式
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('服务器响应错误:', response.status, errorText);
            throw new Error(errorText || `服务器返回错误: ${response.status}`);
        }

        const data = await response.json();
        console.log('收到响应数据:', data);
        return data;
    } catch (error) {
        console.error('生成演示文稿失败:', error);
        throw error;
    }
}

// 将 HTML 演示文稿转换为可下载的 HTML 文件
function createDownloadableHTML(presentationHTML, title) {
    // 直接返回原始HTML内容的URL
    const blob = new Blob([presentationHTML], { type: 'text/html' });
    return URL.createObjectURL(blob);
}

// 导出函数
window.claudeAPI = {
    generatePresentation,
    createDownloadableHTML
}; 