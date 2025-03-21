document.addEventListener('DOMContentLoaded', function() {
    // 尝试从URL获取数据
    const urlParams = new URLSearchParams(window.location.search);
    const dataParam = urlParams.get('data');
    
    let presentationData = null;
    
    if (dataParam) {
        try {
            presentationData = JSON.parse(decodeURIComponent(dataParam));
        } catch (error) {
            console.error('无法从URL解析数据:', error);
        }
    }
    
    // 如果URL中没有数据，尝试从sessionStorage获取
    if (!presentationData) {
        try {
            const storedData = sessionStorage.getItem('presentationData');
            if (storedData) {
                presentationData = JSON.parse(storedData);
                // 使用后清除存储
                sessionStorage.removeItem('presentationData');
            }
        } catch (error) {
            console.error('无法从sessionStorage获取数据:', error);
        }
    }
    
    if (presentationData) {
        displayPresentation(presentationData);
        
        // 设置标题
        if (presentationData.title) {
            document.getElementById('presentation-title').textContent = presentationData.title;
            document.title = presentationData.title + ' - AI Slides';
        }
        
        // 设置下载按钮事件
        document.getElementById('download-btn').addEventListener('click', function() {
            downloadHTML(presentationData.html, presentationData.title || '演示文稿');
        });
        
        // 设置打印按钮事件
        document.getElementById('print-btn').addEventListener('click', function() {
            printPresentation();
        });
    } else {
        showError('未提供演示文稿数据');
    }
});

// 显示演示文稿
function displayPresentation(data) {
    if (!data || !data.html) {
        showError('演示文稿内容为空');
        return;
    }

    const container = document.getElementById('presentation-container');
    container.innerHTML = data.html;
}

// 下载HTML文件
function downloadHTML(html, filename) {
    // 直接使用API返回的HTML内容创建Blob
    const blob = new Blob([html], {type: 'text/html'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename.replace(/\s+/g, '_') + '.html';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// 打印演示文稿
function printPresentation() {
    const iframe = document.querySelector('#presentation-container iframe');
    if (iframe) {
        iframe.contentWindow.print();
    } else {
        alert('无法打印：演示文稿未正确加载');
    }
}

// 显示错误消息
function showError(message) {
    const container = document.getElementById('presentation-container');
    container.innerHTML = `
        <div class="error-message">
            <i class="fas fa-exclamation-circle"></i>
            <h2>出错了</h2>
            <p>${message}</p>
            <button onclick="window.location.href='index.html'" class="action-btn">
                <i class="fas fa-home"></i> 返回首页
            </button>
        </div>
    `;
}

// 调整iframe高度以适应内容 - 改进版本
function adjustIframeHeight() {
    const iframe = document.getElementById('presentation-iframe');
    if (iframe) {
        try {
            const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
            const newHeight = iframeDoc.documentElement.scrollHeight || iframeDoc.body.scrollHeight;
            
            // 确保高度至少有最小值
            iframe.style.height = Math.max(newHeight, 800) + 'px';
            
            // 如果内容高度变化很大，可能需要再次调整
            setTimeout(adjustIframeHeight, 1000); // 再次检查高度
        } catch (e) {
            console.error('调整iframe高度时出错:', e);
        }
    }
} 