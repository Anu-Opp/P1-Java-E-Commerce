package com.cyat.ecommerce;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {
    
    @GetMapping("/")
    public String home() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>A-class Closet</title>
            <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                :root {
                    --primary-beige: #D4C4A8;
                    --warm-beige: #E8DCC0;
                    --deep-beige: #B8A082;
                    --charcoal: #2C2C2C;
                    --soft-white: #FAFAF8;
                    --accent-gold: #C9A96E;
                    --text-dark: #3A3A3A;
                    --shadow: rgba(180, 160, 130, 0.15);
                }
                
                body {
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                    background: linear-gradient(135deg, var(--soft-white) 0%, var(--warm-beige) 100%);
                    color: var(--text-dark);
                    line-height: 1.6;
                    overflow-x: hidden;
                }
                
                .header {
                    background: linear-gradient(135deg, #3A3A3A 0%, #2A2A2A 100%);
                    color: var(--soft-white);
                    padding: 1.5rem 2rem;
                    position: relative;
                    overflow: hidden;
                }
                
                .header::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="%23D4C4A8" opacity="0.15"/><circle cx="75" cy="75" r="1" fill="%23D4C4A8" opacity="0.15"/><circle cx="50" cy="10" r="0.5" fill="%23D4C4A8" opacity="0.08"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
                    pointer-events: none;
                }
                
                .header-content {
                    position: relative;
                    z-index: 2;
                    max-width: 1200px;
                    margin: 0 auto;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                }
                
                .header-left {
                    display: flex;
                    align-items: center;
                    gap: 2rem;
                }
                
                .header-center {
                    flex: 1;
                    text-align: center;
                }
                
                .logo {
                    width: 75px;
                    height: 75px;
                    background: linear-gradient(135deg, var(--accent-gold), var(--primary-beige));
                    border-radius: 18px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 6px 20px rgba(201, 169, 110, 0.3);
                    position: relative;
                    overflow: hidden;
                    flex-shrink: 0;
                }
                
                .logo::before {
                    content: '';
                    position: absolute;
                    top: -50%;
                    left: -50%;
                    width: 200%;
                    height: 200%;
                    background: linear-gradient(45deg, transparent, rgba(255,255,255,0.1), transparent);
                    transform: rotate(45deg);
                    animation: logoShine 3s ease-in-out infinite;
                }
                
                .logo-text {
                    font-size: 1.6rem;
                    font-weight: 800;
                    color: var(--charcoal);
                    letter-spacing: -0.05em;
                    text-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                
                .contact-bar {
                    position: absolute;
                    bottom: 1rem;
                    right: 2rem;
                    display: flex;
                    align-items: center;
                    gap: 1rem;
                }
                
                .contact-item {
                    display: flex;
                    align-items: center;
                    gap: 0.4rem;
                    color: var(--primary-beige);
                    text-decoration: none;
                    transition: all 0.3s ease;
                    padding: 0.4rem 0.8rem;
                    border-radius: 20px;
                    background: rgba(212, 196, 168, 0.1);
                    backdrop-filter: blur(10px);
                    border: 1px solid rgba(212, 196, 168, 0.2);
                }
                
                .contact-item:hover {
                    background: rgba(212, 196, 168, 0.2);
                    transform: translateY(-2px);
                    color: var(--accent-gold);
                }
                
                .contact-icon {
                    font-size: 1rem;
                }
                
                .contact-text {
                    font-size: 0.8rem;
                    font-weight: 500;
                }
                
                .header h1 {
                    font-size: clamp(1.8rem, 3.5vw, 2.5rem);
                    font-weight: 700;
                    background: linear-gradient(135deg, var(--soft-white), var(--primary-beige));
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    background-clip: text;
                    text-shadow: 0 2px 20px rgba(212, 196, 168, 0.3);
                    margin-bottom: 0.3rem;
                    letter-spacing: -0.02em;
                }
                
                .header h2 {
                    font-size: clamp(0.9rem, 2vw, 1.1rem);
                    font-weight: 300;
                    color: var(--primary-beige);
                    margin-bottom: 0;
                    letter-spacing: 0.3px;
                }
                
                .slideshow-container {
                    max-width: 1200px;
                    margin: 3rem auto;
                    padding: 0 2rem;
                    position: relative;
                }
                
                .slide {
                    display: none;
                    width: 100%;
                    border-radius: 20px;
                    overflow: hidden;
                    box-shadow: 
                        0 20px 60px var(--shadow),
                        0 8px 25px rgba(0,0,0,0.1);
                    transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
                    position: relative;
                }
                
                .slide::after {
                    content: '';
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    height: 200px;
                    background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
                    pointer-events: none;
                }
                
                .slide.active {
                    display: block;
                    animation: slideIn 0.8s cubic-bezier(0.4, 0, 0.2, 1);
                }
                
                .slide img {
                    width: 100%;
                    height: 500px;
                    object-fit: cover;
                    filter: brightness(0.85) contrast(1.15);
                }
                
                .slide-overlay {
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    background: linear-gradient(transparent, rgba(44, 44, 44, 0.9));
                    padding: 2.5rem;
                    color: white;
                }
                
                .slide-overlay h3 {
                    font-size: 1.5rem;
                    font-weight: 600;
                    margin-bottom: 0.5rem;
                    text-shadow: 0 2px 8px rgba(0,0,0,0.5);
                }
                
                .slide-overlay p {
                    font-size: 1rem;
                    opacity: 0.95;
                    text-shadow: 0 1px 4px rgba(0,0,0,0.5);
                }
                
                .slide-nav {
                    display: flex;
                    justify-content: center;
                    gap: 1rem;
                    margin-top: 2rem;
                }
                
                .nav-dot {
                    width: 12px;
                    height: 12px;
                    border-radius: 50%;
                    background: var(--deep-beige);
                    opacity: 0.4;
                    cursor: pointer;
                    transition: all 0.3s ease;
                }
                
                .nav-dot.active {
                    opacity: 1;
                    background: var(--accent-gold);
                    transform: scale(1.2);
                }
                
                .features {
                    padding: 5rem 2rem;
                    background: var(--soft-white);
                    margin: 3rem 0;
                }
                
                .gallery-section {
                    padding: 4rem 2rem;
                    background: linear-gradient(135deg, var(--warm-beige) 0%, var(--soft-white) 100%);
                    margin: 2rem 0;
                }
                
                .gallery-header {
                    text-align: center;
                    max-width: 800px;
                    margin: 0 auto 3rem;
                }
                
                .gallery-header h2 {
                    font-size: clamp(1.8rem, 4vw, 2.5rem);
                    color: var(--charcoal);
                    font-weight: 600;
                    margin-bottom: 1rem;
                    letter-spacing: -0.02em;
                }
                
                .gallery-header p {
                    font-size: 1.1rem;
                    color: var(--text-dark);
                    opacity: 0.8;
                }
                
                .gallery-grid {
                    display: grid;
                    grid-template-columns: 1fr;
                    gap: 3rem;
                    max-width: 800px;
                    margin: 0 auto 3rem;
                }
                
                .gallery-item {
                    position: relative;
                    border-radius: 20px;
                    overflow: hidden;
                    box-shadow: 
                        0 15px 40px rgba(180, 160, 130, 0.2),
                        0 5px 15px rgba(0,0,0,0.1);
                    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                    background: white;
                }
                
                .gallery-item:hover {
                    transform: translateY(-10px) scale(1.03);
                    box-shadow: 
                        0 25px 60px rgba(180, 160, 130, 0.3),
                        0 10px 25px rgba(0,0,0,0.15);
                }
                
                .gallery-image {
                    position: relative;
                    width: 100%;
                    height: 400px;
                    overflow: hidden;
                }
                
                .gallery-image img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                    transition: transform 0.6s ease;
                    filter: brightness(0.95) contrast(1.05);
                }
                
                .gallery-item:hover .gallery-image img {
                    transform: scale(1.08);
                }
                
                .gallery-overlay {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: linear-gradient(135deg, rgba(44, 44, 44, 0.85), rgba(201, 169, 110, 0.7));
                    color: white;
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                    align-items: center;
                    text-align: center;
                    padding: 3rem;
                    opacity: 0;
                    transition: all 0.4s ease;
                    backdrop-filter: blur(2px);
                }
                
                .gallery-item:hover .gallery-overlay {
                    opacity: 1;
                }
                
                .gallery-overlay h3 {
                    font-size: 1.6rem;
                    font-weight: 600;
                    margin-bottom: 1.2rem;
                    text-shadow: 0 3px 15px rgba(0,0,0,0.6);
                    letter-spacing: -0.02em;
                }
                
                .gallery-overlay p {
                    font-size: 1.05rem;
                    line-height: 1.6;
                    margin-bottom: 2rem;
                    opacity: 0.95;
                    text-shadow: 0 2px 8px rgba(0,0,0,0.4);
                    max-width: 400px;
                }
                
                .gallery-icon {
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    background: rgba(255, 255, 255, 0.15);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    backdrop-filter: blur(15px);
                    border: 2px solid rgba(255, 255, 255, 0.25);
                    transition: all 0.3s ease;
                }
                
                .gallery-item:hover .gallery-icon {
                    background: rgba(201, 169, 110, 0.3);
                    border-color: rgba(201, 169, 110, 0.5);
                    transform: scale(1.1);
                }
                
                .gallery-icon i {
                    font-size: 1.4rem;
                    color: white;
                }
                
                .special-link {
                    color: var(--accent-gold);
                    text-decoration: none;
                    font-weight: 500;
                    transition: all 0.3s ease;
                    border-bottom: 2px solid transparent;
                }
                
                .special-link:hover {
                    color: var(--deep-beige);
                    border-bottom-color: var(--deep-beige);
                    transform: translateY(-1px);
                }
                
                .special-card {
                    text-align: center;
                    padding: 3rem 2rem;
                    background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(232, 220, 192, 0.3));
                    border-radius: 20px;
                    box-shadow: 0 12px 35px rgba(180, 160, 130, 0.15);
                    backdrop-filter: blur(10px);
                    border: 1px solid rgba(212, 196, 168, 0.3);
                    transition: all 0.3s ease;
                }
                
                .special-card:hover {
                    transform: translateY(-5px);
                    box-shadow: 0 20px 50px rgba(180, 160, 130, 0.25);
                }
                
                .special-card h3 {
                    color: var(--charcoal);
                    margin-bottom: 1rem;
                    font-size: 1.4rem;
                    font-weight: 600;
                }
                
                .special-card p {
                    color: var(--text-dark);
                    margin-bottom: 2rem;
                    opacity: 0.8;
                    font-size: 1.1rem;
                }
                
                .features-header {
                    text-align: center;
                    max-width: 800px;
                    margin: 0 auto 4rem;
                }
                
                .features-header h2 {
                    font-size: clamp(1.5rem, 3vw, 2.2rem);
                    color: var(--charcoal);
                    font-weight: 400;
                    line-height: 1.4;
                    letter-spacing: -0.02em;
                }
                
                .features-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                    gap: 2rem;
                    max-width: 1200px;
                    margin: 0 auto;
                }
                
                .feature-card {
                    background: white;
                    padding: 2.5rem;
                    border-radius: 16px;
                    box-shadow: 0 8px 30px var(--shadow);
                    text-align: center;
                    transition: transform 0.3s ease, box-shadow 0.3s ease;
                    border: 1px solid rgba(212, 196, 168, 0.2);
                }
                
                .feature-card:hover {
                    transform: translateY(-5px);
                    box-shadow: 0 15px 40px var(--shadow);
                }
                
                .feature-icon {
                    font-size: 2.5rem;
                    color: var(--accent-gold);
                    margin-bottom: 1.5rem;
                }
                
                .feature-card h3 {
                    font-size: 1.3rem;
                    color: var(--charcoal);
                    margin-bottom: 1rem;
                }
                
                .feature-card p {
                    color: var(--text-dark);
                    opacity: 0.8;
                }
                
                .footer {
                    background: var(--charcoal);
                    color: var(--primary-beige);
                    padding: 3rem 2rem 2rem;
                    text-align: center;
                    margin-top: 4rem;
                }
                
                .footer-content {
                    max-width: 1200px;
                    margin: 0 auto;
                }
                
                .footer p {
                    opacity: 0.8;
                    font-size: 0.95rem;
                }
                
                @keyframes slideIn {
                    from {
                        opacity: 0;
                        transform: translateX(30px);
                    }
                    to {
                        opacity: 1;
                        transform: translateX(0);
                    }
                }
                
                @keyframes logoShine {
                    0%, 100% {
                        transform: translateX(-100%) translateY(-100%) rotate(45deg);
                    }
                    50% {
                        transform: translateX(100%) translateY(100%) rotate(45deg);
                    }
                }
                
                @media (max-width: 768px) {
                    .header {
                        padding: 1rem;
                    }
                    
                    .header-content {
                        flex-direction: column;
                        gap: 1rem;
                    }
                    
                    .header-left {
                        gap: 1rem;
                    }
                    
                    .contact-bar {
                        position: static;
                        justify-content: center;
                        gap: 0.8rem;
                    }
                    
                    .contact-item {
                        padding: 0.3rem 0.6rem;
                    }
                    
                    .contact-text {
                        font-size: 0.7rem;
                    }
                    
                    .logo {
                        width: 65px;
                        height: 65px;
                    }
                    
                    .logo-text {
                        font-size: 1.4rem;
                    }
                    
                    .slideshow-container {
                        margin: 2rem auto;
                        padding: 0 1rem;
                    }
                    
                    .slide img {
                        height: 300px;
                    }
                    
                    .gallery-section {
                        padding: 3rem 1rem;
                    }
                    
                    .gallery-grid {
                        gap: 2rem;
                        max-width: 100%;
                        margin: 0 auto 2rem;
                    }
                    
                    .gallery-image {
                        height: 280px;
                    }
                    
                    .gallery-overlay {
                        padding: 2rem;
                    }
                    
                    .gallery-overlay h3 {
                        font-size: 1.3rem;
                    }
                    
                    .gallery-overlay p {
                        font-size: 0.95rem;
                    }
                    
                    .special-card {
                        padding: 2.5rem 1.5rem;
                    }
                    
                    .features {
                        padding: 3rem 1rem;
                    }
                    
                    .feature-card {
                        padding: 2rem;
                    }
                }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="header-content">
                    <div class="header-left">
                        <div class="logo">
                            <div class="logo-text">AC2</div>
                        </div>
                    </div>
                    <div class="header-center">
                        <h1>A-Class Closet</h1>
                        <h2>Premium Digital Solutions for Modern Businesses</h2>
                    </div>
                </div>
                <div class="contact-bar">
                    <a href="tel:+1234567890" class="contact-item">
                        <i class="fas fa-phone contact-icon"></i>
                        <span class="contact-text">+1 (234) 567-8900</span>
                    </a>
                    <a href="mailto:hello@ac2.com" class="contact-item">
                        <i class="fas fa-envelope contact-icon"></i>
                        <span class="contact-text">hello@ac2.com</span>
                    </a>
                </div>
            </div>
            
            <div class="slideshow-container">
                <div class="slide active" data-slide="0">
                    <img src="/images/ecommerce-website-designs-1.webp" alt="Modern E-commerce Design">
                    <div class="slide-overlay">
                        <h3>Sophisticated Design Architecture</h3>
                        <p>Clean, modern interfaces that convert visitors into customers</p>
                    </div>
                </div>
                <div class="slide" data-slide="1">
                    <img src="/images/ecommerce-website-designs-2.webp" alt="Responsive Commerce Platform">
                    <div class="slide-overlay">
                        <h3>Responsive Commerce Platform</h3>
                        <p>Seamless experiences across all devices and platforms</p>
                    </div>
                </div>
                <div class="slide" data-slide="2">
                    <img src="/images/ecommerce-website-designs-3.webp" alt="Advanced Analytics Dashboard">
                    <div class="slide-overlay">
                        <h3>Data-Driven Insights</h3>
                        <p>Advanced analytics to optimize your business performance</p>
                    </div>
                </div>
                <div class="slide-nav">
                    <div class="nav-dot active" onclick="currentSlide(1)"></div>
                    <div class="nav-dot" onclick="currentSlide(2)"></div>
                    <div class="nav-dot" onclick="currentSlide(3)"></div>
                </div>
            </div>
            
            <div class="gallery-section">
                <div class="gallery-header">
                    <h2>Our Design Portfolio</h2>
                    <p>Explore our collection of premium e-commerce solutions</p>
                </div>
                <div class="gallery-grid">
                    <div class="gallery-item">
                        <div class="gallery-image">
                            <img src="/images/ecommerce-website-designs-4.webp" alt="Premium Shopping Experience">
                            <div class="gallery-overlay">
                                <h3>Premium Shopping Experience</h3>
                                <p>Luxury e-commerce designs that elevate your brand presence</p>
                                <div class="gallery-icon">
                                    <i class="fas fa-search-plus"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="gallery-grid">
                    <div class="special-card">
                        <h3><i class="fas fa-mobile-alt" style="color: var(--accent-gold); margin-right: 0.5rem;"></i>Mobile-First Commerce</h3>
                        <p>Discover our specialized mobile-first e-commerce solution designed for modern consumers</p>
                        <a href="/mobile-commerce" class="special-link" style="font-size: 1.1rem; display: inline-flex; align-items: center; gap: 0.5rem;">
                            View Mobile Commerce Design 
                            <i class="fas fa-arrow-right" style="font-size: 0.9rem;"></i>
                        </a>
                    </div>
                </div>
                
                <div class="gallery-grid">
                    <div class="gallery-item">
                        <div class="gallery-image">
                            <img src="/images/ecommerce-website-designs-6.webp" alt="Enterprise Solutions">
                            <div class="gallery-overlay">
                                <h3>Enterprise Solutions</h3>
                                <p>Scalable platforms designed for high-volume businesses</p>
                                <div class="gallery-icon">
                                    <i class="fas fa-search-plus"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="gallery-grid">
                    <div class="gallery-item">
                        <div class="gallery-image">
                            <img src="/images/ecommerce-website-designs-7.webp" alt="Next-Gen Commerce">
                            <div class="gallery-overlay">
                                <h3>Next-Gen Commerce</h3>
                                <p>Future-ready solutions with cutting-edge technology</p>
                                <div class="gallery-icon">
                                    <i class="fas fa-search-plus"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="features">
                <div class="features-header">
                    <h2>Elevating your brand with sophisticated e-commerce experiences that drive growth and inspire confidence</h2>
                </div>
                <div class="features-grid">
                    <div class="feature-card">
                        <div class="feature-icon">
                            <i class="fas fa-store"></i>
                        </div>
                        <h3>Premium Storefronts</h3>
                        <p>Craft distinctive digital experiences that reflect your brand's sophistication and drive meaningful engagement with your customers.</p>
                    </div>
                    <div class="feature-card">
                        <div class="feature-icon">
                            <i class="fas fa-chart-line"></i>
                        </div>
                        <h3>Growth Analytics</h3>
                        <p>Harness powerful insights and data visualization tools to make informed decisions that accelerate your business growth.</p>
                    </div>
                    <div class="feature-card">
                        <div class="feature-icon">
                            <i class="fas fa-shield-alt"></i>
                        </div>
                        <h3>Enterprise Security</h3>
                        <p>Rest assured with bank-level security protocols and compliance standards that protect your business and customers.</p>
                    </div>
                </div>
            </div>
            
            <div class="footer">
                <div class="footer-content">
                    <p>&copy; 2025 A-class closet. Crafted with precision for modern enterprises.</p>
                </div>
            </div>
            
            <script>
                let currentIndex = 0;
                const slides = document.querySelectorAll('.slide');
                const dots = document.querySelectorAll('.nav-dot');
                
                function showSlide(index) {
                    slides.forEach((slide, i) => {
                        slide.classList.toggle('active', i === index);
                    });
                    
                    dots.forEach((dot, i) => {
                        dot.classList.toggle('active', i === index);
                    });
                }
                
                function nextSlide() {
                    currentIndex = (currentIndex + 1) % slides.length;
                    showSlide(currentIndex);
                }
                
                function currentSlide(n) {
                    currentIndex = n - 1;
                    showSlide(currentIndex);
                }
                
                // Auto-advance slides
                setInterval(nextSlide, 4000);
                
                // Initialize
                showSlide(0);
            </script>
        </body>
        </html>
        """;
    }
    
    @GetMapping("/mobile-commerce")
    public String mobileCommerce() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Mobile-First Commerce - A-class closet</title>
            <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                :root {
                    --primary-beige: #D4C4A8;
                    --warm-beige: #E8DCC0;
                    --deep-beige: #B8A082;
                    --charcoal: #2C2C2C;
                    --soft-white: #FAFAF8;
                    --accent-gold: #C9A96E;
                    --text-dark: #3A3A3A;
                    --shadow: rgba(180, 160, 130, 0.15);
                }
                
                body {
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                    background: linear-gradient(135deg, var(--soft-white) 0%, var(--warm-beige) 100%);
                    color: var(--text-dark);
                    line-height: 1.6;
                    overflow-x: hidden;
                }
                
                .header {
                    background: linear-gradient(135deg, #3A3A3A 0%, #2A2A2A 100%);
                    color: var(--soft-white);
                    padding: 1.5rem 2rem;
                    position: relative;
                    overflow: hidden;
                }
                
                .header::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="%23D4C4A8" opacity="0.15"/><circle cx="75" cy="75" r="1" fill="%23D4C4A8" opacity="0.15"/><circle cx="50" cy="10" r="0.5" fill="%23D4C4A8" opacity="0.08"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
                    pointer-events: none;
                }
                
                .header-content {
                    position: relative;
                    z-index: 2;
                    max-width: 1200px;
                    margin: 0 auto;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                }
                
                .header-left {
                    display: flex;
                    align-items: center;
                    gap: 2rem;
                }
                
                .header-center {
                    flex: 1;
                    text-align: center;
                }
                
                .logo {
                    width: 75px;
                    height: 75px;
                    background: linear-gradient(135deg, var(--accent-gold), var(--primary-beige));
                    border-radius: 18px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 6px 20px rgba(201, 169, 110, 0.3);
                    position: relative;
                    overflow: hidden;
                    flex-shrink: 0;
                }
                
                .logo::before {
                    content: '';
                    position: absolute;
                    top: -50%;
                    left: -50%;
                    width: 200%;
                    height: 200%;
                    background: linear-gradient(45deg, transparent, rgba(255,255,255,0.1), transparent);
                    transform: rotate(45deg);
                    animation: logoShine 3s ease-in-out infinite;
                }
                
                .logo-text {
                    font-size: 1.6rem;
                    font-weight: 800;
                    color: var(--charcoal);
                    letter-spacing: -0.05em;
                    text-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                
                .back-link {
                    color: var(--primary-beige);
                    text-decoration: none;
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    transition: all 0.3s ease;
                    padding: 0.5rem 1rem;
                    border-radius: 20px;
                    background: rgba(212, 196, 168, 0.1);
                    backdrop-filter: blur(10px);
                    border: 1px solid rgba(212, 196, 168, 0.2);
                }
                
                .back-link:hover {
                    background: rgba(212, 196, 168, 0.2);
                    color: var(--accent-gold);
                    transform: translateY(-2px);
                }
                
                .header h1 {
                    font-size: clamp(1.5rem, 3vw, 2rem);
                    font-weight: 700;
                    background: linear-gradient(135deg, var(--soft-white), var(--primary-beige));
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    background-clip: text;
                    text-shadow: 0 2px 20px rgba(212, 196, 168, 0.3);
                    margin-bottom: 0.3rem;
                    letter-spacing: -0.02em;
                }
                
                .header h2 {
                    font-size: clamp(0.8rem, 1.8vw, 1rem);
                    font-weight: 300;
                    color: var(--primary-beige);
                    margin-bottom: 0;
                    letter-spacing: 0.3px;
                }
                
                .hero-section {
                    max-width: 1000px;
                    margin: 4rem auto;
                    padding: 0 2rem;
                    text-align: center;
                }
                
                .hero-section h1 {
                    font-size: clamp(2.5rem, 5vw, 4rem);
                    color: var(--charcoal);
                    font-weight: 700;
                    margin-bottom: 1.5rem;
                    letter-spacing: -0.03em;
                }
                
                .hero-section p {
                    font-size: 1.2rem;
                    color: var(--text-dark);
                    opacity: 0.8;
                    max-width: 600px;
                    margin: 0 auto 3rem;
                }
                
                .image-showcase {
                    max-width: 900px;
                    margin: 0 auto 4rem;
                    border-radius: 24px;
                    overflow: hidden;
                    box-shadow: 
                        0 25px 70px rgba(180, 160, 130, 0.3),
                        0 10px 30px rgba(0,0,0,0.1);
                    transition: all 0.4s ease;
                }
                
                .image-showcase:hover {
                    transform: translateY(-10px);
                    box-shadow: 
                        0 35px 90px rgba(180, 160, 130, 0.4),
                        0 15px 40px rgba(0,0,0,0.15);
                }
                
                .image-showcase img {
                    width: 100%;
                    height: 600px;
                    object-fit: cover;
                    filter: brightness(0.95) contrast(1.05);
                }
                
                .features-section {
                    background: var(--soft-white);
                    padding: 5rem 2rem;
                    margin: 4rem 0;
                }
                
                .features-container {
                    max-width: 1200px;
                    margin: 0 auto;
                }
                
                .features-header {
                    text-align: center;
                    margin-bottom: 4rem;
                }
                
                .features-header h2 {
                    font-size: clamp(2rem, 4vw, 3rem);
                    color: var(--charcoal);
                    font-weight: 600;
                    margin-bottom: 1rem;
                    letter-spacing: -0.02em;
                }
                
                .features-header p {
                    font-size: 1.1rem;
                    color: var(--text-dark);
                    opacity: 0.8;
                    max-width: 600px;
                    margin: 0 auto;
                }
                
                .features-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
                    gap: 3rem;
                }
                
                .feature-card {
                    background: white;
                    padding: 3rem;
                    border-radius: 20px;
                    box-shadow: 0 10px 40px rgba(180, 160, 130, 0.15);
                    text-align: center;
                    transition: all 0.3s ease;
                    border: 1px solid rgba(212, 196, 168, 0.2);
                }
                
                .feature-card:hover {
                    transform: translateY(-8px);
                    box-shadow: 0 20px 60px rgba(180, 160, 130, 0.25);
                }
                
                .feature-icon {
                    width: 80px;
                    height: 80px;
                    background: linear-gradient(135deg, var(--accent-gold), var(--primary-beige));
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin: 0 auto 2rem;
                    box-shadow: 0 8px 25px rgba(201, 169, 110, 0.3);
                }
                
                .feature-icon i {
                    font-size: 2rem;
                    color: var(--charcoal);
                }
                
                .feature-card h3 {
                    font-size: 1.5rem;
                    color: var(--charcoal);
                    font-weight: 600;
                    margin-bottom: 1rem;
                }
                
                .feature-card p {
                    color: var(--text-dark);
                    opacity: 0.8;
                    line-height: 1.7;
                }
                
                .footer {
                    background: var(--charcoal);
                    color: var(--primary-beige);
                    padding: 3rem 2rem 2rem;
                    text-align: center;
                    margin-top: 4rem;
                }
                
                .footer-content {
                    max-width: 1200px;
                    margin: 0 auto;
                }
                
                .footer p {
                    opacity: 0.8;
                    font-size: 0.95rem;
                }
                
                @keyframes logoShine {
                    0%, 100% {
                        transform: translateX(-100%) translateY(-100%) rotate(45deg);
                    }
                    50% {
                        transform: translateX(100%) translateY(100%) rotate(45deg);
                    }
                }
                
                @media (max-width: 768px) {
                    .header {
                        padding: 1rem;
                    }
                    
                    .header-content {
                        flex-direction: column;
                        gap: 1rem;
                    }
                    
                    .header-left {
                        gap: 1rem;
                    }
                    
                    .logo {
                        width: 65px;
                        height: 65px;
                    }
                    
                    .logo-text {
                        font-size: 1.4rem;
                    }
                    
                    .hero-section {
                        margin: 3rem auto;
                        padding: 0 1rem;
                    }
                    
                    .image-showcase {
                        margin: 0 auto 3rem;
                    }
                    
                    .image-showcase img {
                        height: 400px;
                    }
                    
                    .features-section {
                        padding: 4rem 1rem;
                    }
                    
                    .features-grid {
                        grid-template-columns: 1fr;
                        gap: 2rem;
                    }
                    
                    .feature-card {
                        padding: 2.5rem;
                    }
                }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="header-content">
                    <div class="header-left">
                        <div class="logo">
                            <div class="logo-text">AC2</div>
                        </div>
                        <a href="/" class="back-link">
                            <i class="fas fa-arrow-left"></i>
                            <span>Back to Portfolio</span>
                        </a>
                    </div>
                    <div class="header-center">
                        <h1>Mobile-First Commerce</h1>
                        <h2>Optimized for Modern Mobile Shopping</h2>
                    </div>
                </div>
            </div>
            
            <div class="hero-section">
                <h1>Mobile-First E-Commerce</h1>
                <p>Experience the future of mobile commerce with our cutting-edge design that prioritizes speed, usability, and conversion optimization for smartphone users.</p>
            </div>
            
            <div class="image-showcase">
                <img src="/images/ecommerce-website-designs-5.webp" alt="Mobile-First Commerce Design - Optimized mobile shopping experience">
            </div>
            
            <div class="features-section">
                <div class="features-container">
                    <div class="features-header">
                        <h2>Mobile Commerce Excellence</h2>
                        <p>Every element is crafted to deliver exceptional mobile shopping experiences that drive conversions and customer satisfaction.</p>
                    </div>
                    
                    <div class="features-grid">
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="fas fa-mobile-alt"></i>
                            </div>
                            <h3>Touch-Optimized Interface</h3>
                            <p>Intuitive touch gestures and finger-friendly navigation designed specifically for mobile devices, ensuring effortless browsing and purchasing.</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="fas fa-bolt"></i>
                            </div>
                            <h3>Lightning Fast Performance</h3>
                            <p>Optimized loading speeds and responsive design that delivers instant page loads, reducing bounce rates and improving user engagement.</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="fas fa-credit-card"></i>
                            </div>
                            <h3>One-Tap Checkout</h3>
                            <p>Streamlined checkout process with mobile payment integration, allowing customers to complete purchases with minimal friction.</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="fas fa-search"></i>
                            </div>
                            <h3>Smart Search & Discovery</h3>
                            <p>AI-powered search functionality with visual filters and predictive suggestions that help customers find products instantly.</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="fas fa-heart"></i>
                            </div>
                            <h3>Personalized Experience</h3>
                            <p>Dynamic content personalization based on user behavior, preferences, and purchase history to increase engagement and sales.</p>
                        </div>
                        
                        <div class="feature-card">
                            <div class="feature-icon">
                                <i class="fas fa-shield-alt"></i>
                            </div>
                            <h3>Secure Mobile Payments</h3>
                            <p>Bank-level security with biometric authentication and encrypted transactions, ensuring customer data protection and trust.</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="footer">
                <div class="footer-content">
                    <p>&copy; 2025 A-class Closet. Pioneering mobile commerce innovation.</p>
                </div>
            </div>
        </body>
        </html>
        """;
    }
}