/**
 * Carousel functionality for login page
 * Controls the image carousel animation and transitions
 */

// Global variables for carousel
// Carousel functionality
const carouselImages = document.querySelectorAll('.carousel img');
let currentImageIndex = 0;
let nextImageIndex = 1;
let prevImageIndex = carouselImages.length - 1;

// Initialize carousel
function initCarousel() {
    // Position all images initially
    carouselImages.forEach((img, index) => {
        if (index === currentImageIndex) {
            img.classList.add('active');
            img.style.transform = 'translateX(0) scale(1)';
            img.style.opacity = '1';
            img.style.zIndex = '3';
            img.style.filter = 'blur(0)';
        } else if (index === nextImageIndex) {
            img.style.transform = 'translateX(50%) scale(0.8)';
            img.style.opacity = '0.7';
            img.style.zIndex = '2';
            img.style.filter = 'blur(1px)';
        } else if (index === prevImageIndex) {
            img.style.transform = 'translateX(-50%) scale(0.8)';
            img.style.opacity = '0.7';
            img.style.zIndex = '1';
            img.style.filter = 'blur(1px)';
        } else {
            img.style.transform = 'translateX(0) scale(0.6)';
            img.style.opacity = '0';
            img.style.zIndex = '0';
        }
    });
    
    // Start automatic rotation
    setInterval(nextSlide, 2400);
}

// Function to move to the next slide
function nextSlide() {
    // Remove active class from current image
    carouselImages[currentImageIndex].classList.remove('active');
    
    // Update indices
    prevImageIndex = currentImageIndex;
    currentImageIndex = nextImageIndex;
    nextImageIndex = (nextImageIndex + 1) % carouselImages.length;
    
    // Update styles for all images
    carouselImages.forEach((img, index) => {
        if (index === currentImageIndex) {
            img.classList.add('active');
            img.style.transform = 'translateX(0) scale(1)';
            img.style.opacity = '1';
            img.style.zIndex = '3';
            img.style.filter = 'blur(0)';
        } else if (index === nextImageIndex) {
            img.style.transform = 'translateX(50%) scale(0.8)';
            img.style.opacity = '0.7';
            img.style.zIndex = '2';
            img.style.filter = 'blur(1px)';
        } else if (index === prevImageIndex) {
            img.style.transform = 'translateX(-50%) scale(0.8)';
            img.style.opacity = '0.7';
            img.style.zIndex = '1';
            img.style.filter = 'blur(1px)';
        } else {
            img.style.transform = 'translateX(0) scale(0.6)';
            img.style.opacity = '0';
            img.style.zIndex = '0';
        }
    });
}

// Initialize carousel when document is ready
document.addEventListener('DOMContentLoaded', initCarousel);