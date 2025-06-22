/**
 * Review Management JavaScript
 * Modern and clean functionality for review management
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize page
    initializeReviewManagement();
    
    function initializeReviewManagement() {
        initializeViewToggle();
        initializeGroupToggle();
        initializeStars();
        updateTotalReviews();
        generateUserAvatars();
        initializeDeleteModal();
    }
    
    // View toggle functionality
    function initializeViewToggle() {
        const toggleBtns = document.querySelectorAll('.toggle-btn');
        const viewContainers = document.querySelectorAll('.view-container');
        
        toggleBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const targetView = this.getAttribute('data-view');
                
                // Update active button
                toggleBtns.forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                
                // Update active view
                viewContainers.forEach(container => {
                    container.classList.remove('active');
                    if (container.classList.contains(`${targetView}-view`)) {
                        container.classList.add('active');
                    }
                });
            });
        });
    }
    
    // Group toggle functionality
    function initializeGroupToggle() {
        window.toggleGroup = function(headerElement) {
            const reviewsContainer = headerElement.nextElementSibling;
            const toggleIndicator = headerElement.querySelector('.toggle-indicator');
            
            if (reviewsContainer.classList.contains('expanded')) {
                reviewsContainer.classList.remove('expanded');
                toggleIndicator.classList.remove('expanded');
            } else {
                reviewsContainer.classList.add('expanded');
                toggleIndicator.classList.add('expanded');
            }
        };
    }
    
    // Initialize star ratings
    function initializeStars() {
        const ratingElements = document.querySelectorAll('.review-rating');
        
        ratingElements.forEach(element => {
            const rating = parseFloat(element.getAttribute('data-rating')) || 0;
            generateStars(element, rating);
        });
    }
    
    // Generate stars for rating (with half-star support)
    function generateStars(container, rating) {
        container.innerHTML = '';
        const fullStars = Math.floor(rating);
        const halfStar = rating % 1 >= 0.5 ? 1 : 0;
        const emptyStars = 5 - fullStars - halfStar;

        for (let i = 0; i < fullStars; i++) {
            const star = document.createElement('i');
            star.className = 'fas fa-star';
            container.appendChild(star);
        }
        if (halfStar) {
            const star = document.createElement('i');
            star.className = 'fas fa-star-half-alt';
            container.appendChild(star);
        }
        for (let i = 0; i < emptyStars; i++) {
            const star = document.createElement('i');
            star.className = 'far fa-star';
            container.appendChild(star);
        }
    }
    
    // Update total reviews count
    function updateTotalReviews() {
        const totalReviewsElement = document.getElementById('totalReviews');
        if (!totalReviewsElement) return;
        
        const allReviewItems = document.querySelectorAll('.review-item');
        const totalCount = allReviewItems.length;
        
        totalReviewsElement.textContent = totalCount/2;
        
        // Update individual group counts
        const groupCards = document.querySelectorAll('.review-group-card');
        groupCards.forEach(card => {
            const reviewItems = card.querySelectorAll('.review-item');
            const countElement = card.querySelector('.review-count-number');
            if (countElement) {
                countElement.textContent = reviewItems.length;
            }
        });
    }
    
    // Generate user avatars with initials
    function generateUserAvatars() {
        const avatars = document.querySelectorAll('.group-avatar[data-initial], .user-avatar[data-initial]');
        
        avatars.forEach(avatar => {
            const initial = avatar.getAttribute('data-initial');
            if (initial) {
                avatar.textContent = initial.toUpperCase();
            }
        });
    }
    
    // Initialize Delete Modal System
    function initializeDeleteModal() {
        // Create modal HTML if it doesn't exist
        if (!document.getElementById('deleteReviewModal')) {
            createDeleteModal();
        }
        
        // Attach delete button handlers
        const deleteButtons = document.querySelectorAll('.delete-btn');
        deleteButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                showDeleteModal(this);
            });
        });
    }
    
    // Create the delete modal HTML
    function createDeleteModal() {
        const modalHTML = `
            <div class="delete-modal-backdrop" id="deleteReviewModal">
                <div class="delete-modal-container">
                    <div class="delete-modal-header">
                        <div class="delete-modal-icon">
                            <i class="fas fa-exclamation-triangle"></i>
                        </div>
                        <h3 class="delete-modal-title">Eliminar Reseña</h3>
                        <p class="delete-modal-subtitle">Esta acción no se puede deshacer</p>
                    </div>
                    
                    <div class="delete-modal-body">
                        <div class="review-preview">
                            <div class="preview-title">Reseña a eliminar:</div>
                            <div class="preview-text" id="reviewPreviewText"></div>
                            <div class="preview-meta">
                                <span id="reviewPreviewAuthor"></span>
                                <span>•</span>
                                <span id="reviewPreviewProduct"></span>
                                <span>•</span>
                                <div class="review-rating" id="reviewPreviewRating" data-rating="0"></div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="delete-modal-footer">
                        <button type="button" class="modal-btn cancel-delete-btn" id="cancelDeleteReview">
                            <i class="fas fa-times"></i>
                            <span>Cancelar</span>
                        </button>
                        <button type="button" class="modal-btn confirm-delete-btn" id="confirmDeleteReview">
                            <i class="fas fa-trash-alt"></i>
                            <span>Eliminar Reseña</span>
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        
        // Add event listeners for modal buttons
        document.getElementById('cancelDeleteReview').addEventListener('click', hideDeleteModal);
        document.getElementById('confirmDeleteReview').addEventListener('click', executeDelete);
        
        // Close modal when clicking backdrop
        document.getElementById('deleteReviewModal').addEventListener('click', function(e) {
            if (e.target === this) {
                hideDeleteModal();
            }
        });
        
        // Close modal with Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                hideDeleteModal();
            }
        });
    }
    
    // Show delete modal with review data
    function showDeleteModal(deleteButton) {
        const form = deleteButton.closest('form');
        const reviewItem = deleteButton.closest('.review-item');
        const productId = form.querySelector('input[name="productId"]').value;
        const reviewId = form.querySelector('input[name="reviewId"]').value;
        
        // Store form data for later use
        window.currentDeleteForm = form;
        
        // Extract review information
        const reviewText = reviewItem.querySelector('.review-text')?.textContent.trim() || 'Sin comentario';
        const rating = reviewItem.querySelector('.review-rating')?.getAttribute('data-rating') || '0';
        
        // Find author and product info
        let authorName = 'Usuario desconocido';
        let productName = 'Producto desconocido';
        
        // Check if we're in user view or product view
        const groupCard = reviewItem.closest('.review-group-card');
        if (groupCard) {
            const groupName = groupCard.querySelector('.group-name')?.textContent.trim();
            
            // If the group has a product image or product avatar, we're in product view
            if (groupCard.querySelector('.group-product-image') || groupCard.querySelector('.product-avatar')) {
                productName = groupName || 'Producto desconocido';
                const userNameElement = reviewItem.querySelector('.user-name');
                authorName = userNameElement ? userNameElement.textContent.trim() : 'Usuario desconocido';
            } else {
                // We're in user view
                authorName = groupName || 'Usuario desconocido';
                const productInfo = reviewItem.querySelector('.review-product-info strong');
                productName = productInfo ? productInfo.textContent.trim() : 'Producto desconocido';
            }
        }
        
        // Populate modal with review data
        document.getElementById('reviewPreviewText').textContent = reviewText;
        document.getElementById('reviewPreviewAuthor').textContent = `Por: ${authorName}`;
        document.getElementById('reviewPreviewProduct').textContent = `Producto: ${productName}`;
        
        // Update rating stars
        const ratingElement = document.getElementById('reviewPreviewRating');
        ratingElement.setAttribute('data-rating', rating);
        generateStars(ratingElement, parseFloat(rating));
        
        // Show modal
        const modal = document.getElementById('deleteReviewModal');
        modal.classList.add('active');
        
        // Focus on cancel button for accessibility
        setTimeout(() => {
            document.getElementById('cancelDeleteReview').focus();
        }, 100);
    }
    
    // Hide delete modal
    function hideDeleteModal() {
        const modal = document.getElementById('deleteReviewModal');
        if (modal) {
            modal.classList.remove('active');
            window.currentDeleteForm = null;
        }
    }
    
    // Execute the delete action
    function executeDelete() {
        if (window.currentDeleteForm) {
            // Add loading state to confirm button
            const confirmBtn = document.getElementById('confirmDeleteReview');
            const originalContent = confirmBtn.innerHTML;
            confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Eliminando...</span>';
            confirmBtn.disabled = true;
            
            // Submit the form
            window.currentDeleteForm.submit();
        }
    }
    
    // Add smooth scrolling to page
    document.documentElement.style.scrollBehavior = 'smooth';
    
    // Add hover effects for cards
    const groupCards = document.querySelectorAll('.review-group-card');
    groupCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    // Initialize tooltips if needed
    function initializeTooltips() {
        const tooltipElements = document.querySelectorAll('[data-tooltip]');
        
        tooltipElements.forEach(element => {
            element.addEventListener('mouseenter', function() {
                const tooltipText = this.getAttribute('data-tooltip');
                if (tooltipText) {
                    showTooltip(this, tooltipText);
                }
            });
            
            element.addEventListener('mouseleave', function() {
                hideTooltip();
            });
        });
    }
    
    function showTooltip(element, text) {
        const tooltip = document.createElement('div');
        tooltip.className = 'tooltip-popup';
        tooltip.textContent = text;
        tooltip.style.cssText = `
            position: absolute;
            background: #374151;
            color: white;
            padding: 0.5rem 0.75rem;
            border-radius: 0.375rem;
            font-size: 0.875rem;
            z-index: 1000;
            pointer-events: none;
            transform: translateY(-100%);
            margin-top: -0.5rem;
            box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
        `;
        
        document.body.appendChild(tooltip);
        
        const rect = element.getBoundingClientRect();
        tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
        tooltip.style.top = rect.top + 'px';
        
        tooltip.style.opacity = '0';
        setTimeout(() => {
            tooltip.style.opacity = '1';
            tooltip.style.transition = 'opacity 0.2s ease';
        }, 50);
    }
    
    function hideTooltip() {
        const tooltip = document.querySelector('.tooltip-popup');
        if (tooltip) {
            tooltip.style.opacity = '0';
            setTimeout(() => {
                if (tooltip.parentNode) {
                    tooltip.parentNode.removeChild(tooltip);
                }
            }, 200);
        }
    }
    
    // Initialize tooltips
    initializeTooltips();
    
    // Add keyboard navigation
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            // Close any open modals or dropdowns
            hideDeleteModal();
            
            const expandedContainers = document.querySelectorAll('.reviews-container.expanded');
            expandedContainers.forEach(container => {
                const header = container.previousElementSibling;
                if (header) {
                    toggleGroup(header);
                }
            });
        }
    });
    
    console.log('Review Management initialized successfully');
}); 