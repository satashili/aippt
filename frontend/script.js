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
    const generateBtn = document.querySelector('.generate-btn');
    if (generateBtn) {
        generateBtn.addEventListener('click', function() {
            this.classList.add('loading');
            
            // 模拟生成过程
            setTimeout(() => {
                this.classList.remove('loading');
                
                // 这里可以添加生成完成后的操作
                alert('演示文稿生成完成！在实际应用中，这里会显示生成的PPT预览。');
            }, 3000);
        });
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

    // 检查用户登录状态
    fetch(`${window.config.apiBaseUrl}/user`, {
        credentials: 'include'  // 重要：发送cookie
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`登录状态检查失败: ${response.status}`);
        }
        return response.json();
    })
    .then(user => {
        if (user && user.id) {
            // 用户已登录，更新UI显示登录状态
            document.querySelector('.auth-buttons').innerHTML = `
                <div class="user-container">
                    <img class="user-avatar" src="${user.picture || 'images/default-avatar.png'}" alt="头像">
                    <div class="user-details">
                        <span class="user-name">${user.name || '用户'}</span>
                        <span class="user-email">${user.email || ''}</span>
                    </div>
                    <button onclick="logout()" class="login-btn">退出</button>
                </div>
            `;
        } else {
            // 确保用户未登录时显示登录按钮
            document.querySelector('.auth-buttons').innerHTML = `
                <button onclick="window.location.href='login.html'" class="login-btn">登录</button>
                <button onclick="window.location.href='register.html'" class="signup-btn">注册</button>
            `;
        }
    })
    .catch(error => {
        console.error('登录状态检查失败:', error);
        // 确保用户未登录时显示登录按钮
        document.querySelector('.auth-buttons').innerHTML = `
            <button onclick="window.location.href='login.html'" class="login-btn">登录</button>
            <button onclick="window.location.href='register.html'" class="signup-btn">注册</button>
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