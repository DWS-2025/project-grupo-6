// Animate elements entry
document.addEventListener('DOMContentLoaded', function() {
    const animatedElements = document.querySelectorAll('.animate-in');
    
    animatedElements.forEach(element => {
        const delay = element.getAttribute('data-delay') || 0;
        
        setTimeout(() => {
            element.style.animation = `fadeInUp 0.6s ease-out forwards`;
        }, delay * 1000);
    });
    
    // Function to animate cart icon
    function animateCartIcon() {
        const cartIcon = document.querySelector('.cart-icon i');
        cartIcon.classList.add('fa-bounce');
        
        setTimeout(() => {
            cartIcon.classList.remove('fa-bounce');
        }, 1000);
    }
    
    // Animate cart periodically
    setTimeout(() => {
        setInterval(animateCartIcon, 5000);
    }, 2000);
    
    // Hover effect for features
    const features = document.querySelectorAll('.feature-item');
    
    features.forEach(feature => {
        feature.addEventListener('mouseenter', function() {
            const icon = this.querySelector('.feature-icon i');
            icon.classList.add('fa-bounce');
        });
        
        feature.addEventListener('mouseleave', function() {
            const icon = this.querySelector('.feature-icon i');
            icon.classList.remove('fa-bounce');
        });
    });
    
    // Adjust footer for dark mode
    function updateFooterForDarkMode() {
        const footer = document.querySelector('.custom-footer');
        const isDarkMode = document.body.classList.contains('dark-mode');
        
        if (isDarkMode) {
            footer.style.backgroundColor = '#1a202c';
            footer.style.boxShadow = '0 -5px 15px rgba(0,0,0,0.2)';
        } else {
            footer.style.backgroundColor = '#294156';
            footer.style.boxShadow = '0 -5px 15px rgba(0,0,0,0.1)';
        }
    }
    
    // Check dark mode initially
    updateFooterForDarkMode();
    
    // Observe changes in dark mode
    const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.attributeName === 'class') {
                updateFooterForDarkMode();
            }
        });
    });
    
    observer.observe(document.body, { attributes: true });
}); 