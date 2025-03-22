document.addEventListener('DOMContentLoaded', function() {
    if (typeof particlesJS !== 'undefined') {
        particlesJS('particles-js', {
            particles: {
                number: {
                    value: 80,
                    density: {
                        enable: true,
                        value_area: 800
                    }
                },
                color: {
                    value: '#3a86ff'
                },
                shape: {
                    type: 'circle',
                    stroke: {
                        width: 0,
                        color: '#000000'
                    },
                    polygon: {
                        nb_sides: 5
                    }
                },
                opacity: {
                    value: 0.5,
                    random: true,
                    anim: {
                        enable: true,
                        speed: 1,
                        opacity_min: 0.1,
                        sync: false
                    }
                },
                size: {
                    value: 3,
                    random: true,
                    anim: {
                        enable: true,
                        speed: 2,
                        size_min: 0.1,
                        sync: false
                    }
                },
                line_linked: {
                    enable: true,
                    distance: 150,
                    color: '#3a86ff',
                    opacity: 0.4,
                    width: 1
                },
                move: {
                    enable: true,
                    speed: 1,
                    direction: 'none',
                    random: true,
                    straight: false,
                    out_mode: 'out',
                    bounce: false,
                    attract: {
                        enable: false,
                        rotateX: 600,
                        rotateY: 1200
                    }
                }
            },
            interactivity: {
                detect_on: 'canvas',
                events: {
                    onhover: {
                        enable: true,
                        mode: 'grab'
                    },
                    onclick: {
                        enable: true,
                        mode: 'push'
                    },
                    resize: true
                },
                modes: {
                    grab: {
                        distance: 140,
                        line_linked: {
                            opacity: 1
                        }
                    },
                    bubble: {
                        distance: 400,
                        size: 40,
                        duration: 2,
                        opacity: 8,
                        speed: 3
                    },
                    repulse: {
                        distance: 200,
                        duration: 0.4
                    },
                    push: {
                        particles_nb: 4
                    },
                    remove: {
                        particles_nb: 2
                    }
                }
            },
            retina_detect: true
        });
    }

    // 滚动动画
    const animateOnScroll = () => {
        const elements = document.querySelectorAll('.feature-card, .template-card, .generator-box');
        
        elements.forEach(element => {
            const elementPosition = element.getBoundingClientRect().top;
            const screenPosition = window.innerHeight / 1.3;
            
            if (elementPosition < screenPosition) {
                element.classList.add('fade-in');
            }
        });
    };

    window.addEventListener('scroll', animateOnScroll);
    animateOnScroll(); // 初始检查

    // 导航栏滚动效果
    const header = document.querySelector('header');
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            header.style.background = 'rgba(8, 13, 23, 0.95)';
            header.style.boxShadow = '0 5px 20px rgba(0, 0, 0, 0.3)';
        } else {
            header.style.background = 'rgba(15, 23, 41, 0.8)';
            header.style.boxShadow = 'none';
        }
    });

    // 生成按钮效果
    const generateBtn = document.querySelector('#generate-btn');
    if (generateBtn) {
        generateBtn.addEventListener('click', async function() {
            // 获取输入内容
            const promptText = document.getElementById('presentation-prompt').value.trim();
            if (!promptText) {
                alert('Please enter a presentation description');
                return;
            }
            
            // 获取选项
            const styleInput = document.getElementById('style-input');
            const slideCountSelect = document.getElementById('slide-count-select');
            const colorThemeInput = document.getElementById('color-theme-input');
            
            const style = styleInput ? styleInput.value.trim() : '';
            const slideCount = slideCountSelect ? slideCountSelect.value : '10-15 slides';
            const colorTheme = colorThemeInput ? colorThemeInput.value.trim() : '';
            
            // 显示加载状态
            this.classList.add('loading');
            
            // 创建加载消息数组
            const loadingMessages = [
                "AI is creating...",
                "Good things take time...",
                "AISlides is working..."
            ];
            
            let messageIndex = 0;
            const loadingTextElement = this.querySelector('.loading-text');
            const originalText = loadingTextElement.innerHTML;
            
            // 设置定时器，每2秒更换一次消息
            const messageInterval = setInterval(() => {
                messageIndex = (messageIndex + 1) % loadingMessages.length;
                loadingTextElement.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${loadingMessages[messageIndex]}`;
            }, 2000);
            
            try {
                console.log('Calling Claude API to generate presentation...');
                console.log('Parameters:', { promptText, style, slideCount, colorTheme });
                
                // 调用 Claude API 生成演示文稿
                const presentation = await window.claudeAPI.generatePresentation(
                    promptText, 
                    style, 
                    slideCount, 
                    colorTheme
                );
                
                console.log('Received presentation data:', presentation);
                
                // 将演示文稿数据编码为 URL 参数
                const presentationData = encodeURIComponent(JSON.stringify(presentation));
                
                console.log('Preparing to redirect to presentation viewer page');
                
                // 使用 sessionStorage 存储数据
                sessionStorage.setItem('presentationData', JSON.stringify(presentation));
                window.location.href = 'presentation-viewer.html';
            } catch (error) {
                console.error('Failed to generate presentation:', error);
                alert(`Failed to generate presentation: ${error.message || 'Unknown error'}`);
                this.classList.remove('loading');
                
                // 清除消息定时器
                clearInterval(messageInterval);
                
                // 恢复原始加载文本
                loadingTextElement.innerHTML = originalText;
            }
        });
    }

    // 增强选项组件交互
    const styleInput = document.getElementById('style-input');
    if (styleInput) {
        // 风格选项预设
        const stylePresets = ['Simple business', 'Creative design', 'Technology', 'Cyberpunk', 'Natural scenery', 'Retro style', 'Minimalism'];
        
        // 创建风格预设标签
        const stylePresetContainer = document.createElement('div');
        stylePresetContainer.className = 'preset-tags';
        
        stylePresets.forEach(preset => {
            const tag = document.createElement('span');
            tag.className = 'preset-tag';
            tag.textContent = preset;
            tag.addEventListener('click', () => {
                styleInput.value = preset;
                // 移除其他标签的激活状态
                document.querySelectorAll('.preset-tag').forEach(t => t.classList.remove('active'));
                // 添加当前标签的激活状态
                tag.classList.add('active');
            });
            stylePresetContainer.appendChild(tag);
        });
        
        // 将预设标签添加到风格输入框后面
        styleInput.parentNode.appendChild(stylePresetContainer);
    }
    
    // 颜色主题预设
    const colorThemeInput = document.getElementById('color-theme-input');
    if (colorThemeInput) {
        // 创建颜色预设选择器
        const colorPresetContainer = document.createElement('div');
        colorPresetContainer.className = 'color-presets';
        
        // 预设颜色方案
        const colorPresets = [
            { name: '深蓝', color: '#1a73e8' },
            { name: '翠绿', color: '#0f9d58' },
            { name: '珊瑚红', color: '#f44336' },
            { name: '紫罗兰', color: '#673ab7' },
            { name: '琥珀黄', color: '#ffc107' },
            { name: '青色', color: '#00bcd4' }
        ];
        
        colorPresets.forEach(preset => {
            const colorOption = document.createElement('div');
            colorOption.className = 'color-preset-option';
            colorOption.style.backgroundColor = preset.color;
            colorOption.setAttribute('data-color-name', preset.name);
            
            // 添加提示文本
            const tooltip = document.createElement('span');
            tooltip.className = 'color-tooltip';
            tooltip.textContent = preset.name;
            colorOption.appendChild(tooltip);
            
            colorOption.addEventListener('click', () => {
                colorThemeInput.value = preset.name;
                // 移除其他选项的激活状态
                document.querySelectorAll('.color-preset-option').forEach(opt => opt.classList.remove('active'));
                // 添加当前选项的激活状态
                colorOption.classList.add('active');
            });
            
            colorPresetContainer.appendChild(colorOption);
        });
        
        // 添加自定义颜色选项
        const customColorOption = document.createElement('div');
        customColorOption.className = 'color-preset-option custom-color';
        customColorOption.innerHTML = '<i class="fas fa-plus"></i>';
        customColorOption.setAttribute('data-color-name', '自定义');
        
        const customTooltip = document.createElement('span');
        customTooltip.className = 'color-tooltip';
        customTooltip.textContent = '自定义';
        customColorOption.appendChild(customTooltip);
        
        customColorOption.addEventListener('click', () => {
            const colorPicker = document.createElement('input');
            colorPicker.type = 'color';
            colorPicker.style.opacity = '0';
            colorPicker.style.position = 'absolute';
            colorPicker.style.pointerEvents = 'none';
            
            document.body.appendChild(colorPicker);
            colorPicker.click();
            
            colorPicker.addEventListener('input', function() {
                customColorOption.style.backgroundColor = this.value;
                colorThemeInput.value = this.value;
            });
            
            colorPicker.addEventListener('change', function() {
                document.body.removeChild(this);
                // 移除其他选项的激活状态
                document.querySelectorAll('.color-preset-option').forEach(opt => opt.classList.remove('active'));
                // 添加当前选项的激活状态
                customColorOption.classList.add('active');
            });
        });
        
        colorPresetContainer.appendChild(customColorOption);
        colorThemeInput.parentNode.appendChild(colorPresetContainer);
    }
    
    // 增强AI Power选择器
    const slideCountSelect = document.getElementById('slide-count-select');
    if (slideCountSelect) {
        // 创建自定义选择器
        const powerLevelContainer = document.createElement('div');
        powerLevelContainer.className = 'power-level-slider';
        
        // 创建滑块
        const slider = document.createElement('input');
        slider.type = 'range';
        slider.min = '1';
        slider.max = '4';
        slider.value = '2'; // 默认值
        slider.className = 'power-slider';
        
        // 创建滑块标签
        const sliderLabels = document.createElement('div');
        sliderLabels.className = 'slider-labels';
        
        const labels = ['5-10 Power', '10-15 Power', '15-20 Power', '20+ Power'];
        labels.forEach((label, index) => {
            const labelElement = document.createElement('span');
            labelElement.textContent = label;
            labelElement.className = 'slider-label';
            labelElement.setAttribute('data-value', index + 1);
            sliderLabels.appendChild(labelElement);
        });
        
        // 更新选择器值
        slider.addEventListener('input', () => {
            const value = parseInt(slider.value);
            slideCountSelect.value = labels[value - 1];
            
            // 更新标签激活状态
            document.querySelectorAll('.slider-label').forEach(label => {
                if (parseInt(label.getAttribute('data-value')) === value) {
                    label.classList.add('active');
                } else {
                    label.classList.remove('active');
                }
            });
        });
        
        // 点击标签更新滑块
        sliderLabels.querySelectorAll('.slider-label').forEach(label => {
            label.addEventListener('click', () => {
                const value = label.getAttribute('data-value');
                slider.value = value;
                slideCountSelect.value = labels[value - 1];
                
                // 更新标签激活状态
                document.querySelectorAll('.slider-label').forEach(l => {
                    if (l === label) {
                        l.classList.add('active');
                    } else {
                        l.classList.remove('active');
                    }
                });
            });
        });
        
        // 初始化激活状态
        sliderLabels.querySelector('[data-value="2"]').classList.add('active');
        
        powerLevelContainer.appendChild(slider);
        powerLevelContainer.appendChild(sliderLabels);
        
        // 隐藏原始选择器并添加自定义选择器
        slideCountSelect.style.display = 'none';
        slideCountSelect.parentNode.appendChild(powerLevelContainer);
    }

    // 颜色选择器
    const colorOptions = document.querySelectorAll('.color-option');
    if (colorOptions.length > 0) {
        colorOptions.forEach(option => {
            option.addEventListener('click', function() {
                // 移除其他选项的active类
                colorOptions.forEach(opt => opt.classList.remove('active'));
                
                // 为当前选项添加active类
                this.classList.add('active');
                
                // 如果是自定义颜色选项
                if (this.classList.contains('custom-color')) {
                    const colorPicker = document.createElement('input');
                    colorPicker.type = 'color';
                    colorPicker.style.opacity = '0';
                    colorPicker.style.position = 'absolute';
                    colorPicker.style.pointerEvents = 'none';
                    
                    document.body.appendChild(colorPicker);
                    colorPicker.click();
                    
                    colorPicker.addEventListener('input', function() {
                        option.style.backgroundColor = this.value;
                    });
                    
                    colorPicker.addEventListener('change', function() {
                        document.body.removeChild(this);
                    });
                }
            });
        });
    }

    // 平滑滚动
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 80,
                    behavior: 'smooth'
                });
            }
        });
    });

    // 添加 CTA 按钮点击事件
    const getStartedBtn = document.querySelector('.cta-button');
    const watchDemoBtn = document.querySelector('.secondary-button');
    
    // 添加 Get Started 按钮点击事件
    if (getStartedBtn) {
        getStartedBtn.addEventListener('click', function() {
            // 滚动到 generator 部分
            const generatorSection = document.querySelector('#generator');
            if (generatorSection) {
                window.scrollTo({
                    top: generatorSection.offsetTop - 80,
                    behavior: 'smooth'
                });
            }
        });
    }
    
    // 添加 Watch Demo 按钮点击事件
    if (watchDemoBtn) {
        watchDemoBtn.addEventListener('click', function() {
            // 跳转到 images/2.html
            window.location.href = 'images/2.html';
        });
    }

    // 添加打字机效果
    const textElement = document.querySelector('.hero-content p');
    if (textElement) {
        const text = textElement.textContent;
        textElement.textContent = '';
        
        let i = 0;
        const typeWriter = () => {
            if (i < text.length) {
                textElement.textContent += text.charAt(i);
                i++;
                setTimeout(typeWriter, 50);
            }
        };
        
        // 延迟启动打字效果
        setTimeout(typeWriter, 1000);
    }

    // 模板卡片悬停效果增强
    const templateCards = document.querySelectorAll('.template-card');
    templateCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = '';
        });
    });

    // 添加模板按钮点击事件
    const templateButtons = document.querySelectorAll('.use-template-btn');
    templateButtons.forEach((button, index) => {
        // 修改按钮文本为"Show This Template"
        button.textContent = 'Show This Template';
        
        // 添加点击事件处理
        button.addEventListener('click', function() {
            // 根据索引确定要打开的HTML文件
            const templateFile = `images/${index + 1}.html`;
            
            // 打开对应的HTML文件
            window.open(templateFile, '_blank');
        });
    });

    // FAQ interactions
    const faqItems = document.querySelectorAll('.faq-item');
    faqItems.forEach(item => {
        const question = item.querySelector('.faq-question');
        question.addEventListener('click', () => {
            // Close other open FAQs
            faqItems.forEach(otherItem => {
                if (otherItem !== item && otherItem.classList.contains('active')) {
                    otherItem.classList.remove('active');
                }
            });
            
            // Toggle current FAQ state
            item.classList.toggle('active');
        });
    });
    
    // Pricing card hover effects
    const pricingCards = document.querySelectorAll('.pricing-card');
    pricingCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            if (!this.classList.contains('popular')) {
                this.style.transform = 'translateY(-10px)';
            } else {
                this.style.transform = 'scale(1.05) translateY(-10px)';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            if (!this.classList.contains('popular')) {
                this.style.transform = '';
            } else {
                this.style.transform = 'scale(1.05)';
            }
        });
    });
    
    // Subscribe button click events
    const pricingBtns = document.querySelectorAll('.pricing-btn');
    pricingBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // Logic to open payment modal would go here
            alert('In a real application, this would open the payment page.');
        });
    });

    // 立即检查登录状态
    fetch(`${window.config.apiBaseUrl}/user`, {
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`登录失败: ${response.status}`);
        }
        return response.json();
    })
    .then(user => {
        if (!user || !user.id) {
            throw new Error('无效的用户数据');
        }
        // 用户已登录，显示用户信息
        document.querySelector('.auth-buttons').innerHTML = `
            <div class="user-container">
                <img class="user-avatar" src="${user.picture || 'default-avatar.png'}" alt="User Avatar">
                <div class="user-details">
                    <span class="user-name">${user.name || 'Unknown User'}</span>
                    <span class="user-email">${user.email || ''}</span>
                </div>
                <button onclick="logout()" class="login-btn">Sign out</button>
            </div>
        `;
    })
    .catch(error => {
        console.error('登录状态检查失败:', error);
        // 确保用户未登录时显示登录按钮
        document.querySelector('.auth-buttons').innerHTML = `
            <button onclick="window.location.href=window.config.googleAuthUrl" class="login-btn">Log in</button>
            <button onclick="window.location.href='register.html'" class="signup-btn">Sign up</button>
        `;
    });
});

// 退出登录函数
function logout() {
    console.log('退出函数被调用');
    
    fetch(`${window.config.apiBaseUrl}/logout`, {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => {
        console.log('登出响应状态:', response.status);
        window.location.href = 'http://localhost:3000';
    })
    .catch(error => {
        console.error('登出请求失败:', error);
        window.location.href = 'http://localhost:3000';
    });
}

// 添加3D倾斜效果
document.addEventListener('mousemove', function(e) {
    const cards = document.querySelectorAll('.feature-card');
    
    cards.forEach(card => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        
        if (x > 0 && x < rect.width && y > 0 && y < rect.height) {
            const xRotation = ((y - rect.height / 2) / rect.height) * 10;
            const yRotation = ((rect.width / 2 - x) / rect.width) * 10;
            
            card.style.transform = `perspective(1000px) rotateX(${xRotation}deg) rotateY(${yRotation}deg) scale(1.05)`;
            card.style.transition = 'transform 0.1s';
        } else {
            card.style.transform = '';
            card.style.transition = 'transform 0.5s';
        }
    });
});

// 添加滚动进度指示器
window.onscroll = function() {
    const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
    const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    const scrolled = (winScroll / height) * 100;
    
    // 创建或获取进度条元素
    let progressBar = document.getElementById('scroll-progress');
    if (!progressBar) {
        progressBar = document.createElement('div');
        progressBar.id = 'scroll-progress';
        progressBar.style.position = 'fixed';
        progressBar.style.top = '0';
        progressBar.style.left = '0';
        progressBar.style.height = '3px';
        progressBar.style.background = 'linear-gradient(to right, #3a86ff, #8338ec)';
        progressBar.style.zIndex = '1001';
        progressBar.style.transition = 'width 0.1s';
        document.body.appendChild(progressBar);
    }
    
    progressBar.style.width = scrolled + '%';
}; 