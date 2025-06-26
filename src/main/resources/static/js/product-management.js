document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const modalOverlay = document.getElementById('confirmDeleteModal');
    const cancelButton = document.getElementById('cancelDelete');
    const productNameElement = document.getElementById('productName');
    const productIdInput = document.getElementById('productIdInput');
    const deleteForm = document.getElementById('deleteProductForm');
    const productsGrid = document.getElementById('productsGrid');
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const productCountElement = document.getElementById('productCount');
    
    // Pagination variables
    let currentPage = 1;
    let hasMore = true;
    const pageSize = 20;
    
    // Function to show delete confirmation modal
    window.showDeleteConfirmation = function(id, name) {
        productNameElement.textContent = name;
        productIdInput.value = id;
        modalOverlay.classList.add('active');
        document.body.style.overflow = 'hidden'; // Prevent scrolling
    }
    
    // Function to show elegant notifications
    function showNotification(message, type = 'success') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        
        // Add icon according to the type
        let icon = 'check-circle';
        if (type === 'error') icon = 'exclamation-circle';
        if (type === 'warning') icon = 'exclamation-triangle';
        if (type === 'info') icon = 'info-circle';
        
        notification.innerHTML = `
            <i class="fas fa-${icon}"></i>
            <p>${message}</p>
            <button class="close-notification">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        // Add to DOM
        document.body.appendChild(notification);
        
        // Show with animation
        setTimeout(() => {
            notification.classList.add('show');
        }, 10);
        
        // Configure close button
        notification.querySelector('.close-notification').addEventListener('click', () => {
            notification.classList.remove('show');
            setTimeout(() => {
                notification.remove();
            }, 300);
        });
        
        // Auto close after 5 seconds
        setTimeout(() => {
            if (notification.parentElement) {
                notification.classList.remove('show');
                setTimeout(() => {
                    notification.remove();
                }, 300);
            }
        }, 5000);
    }
    
    // Function to update product counter
    function updateProductCount(count) {
        if (productCountElement) {
            productCountElement.textContent = count > 0 ? `${count} products` : '';
        }
    }
    
    // Function to configure product links (no longer needed with the new design)
    function setupProductLinks() {
        // This function is no longer needed with the new design
        // The links are handled directly in the template
    }
    
    // Function to load products with improved animations
    function loadProducts(page) {
        // Show a loading effect on the button
        loadMoreBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Loading...</span>';
        loadMoreBtn.classList.add('loading');
        loadMoreBtn.disabled = true;
        
        fetch(`/api/products/management?page=${page}&size=${pageSize}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Update pagination variables
                currentPage = data.currentPage;
                hasMore = data.hasMore;
                
                // Update the product counter
                updateProductCount(data.totalElements);
                
                // Update the "Load More" button
                loadMoreBtn.disabled = false;
                if (!hasMore) {
                    loadMoreBtn.style.display = 'none';
                } else {
                    loadMoreBtn.innerHTML = '<i class="fas fa-chevron-down"></i><span>Load More</span>';
                    loadMoreBtn.classList.remove('loading');
                }
                
                // If it is the first page and there are no products, show empty state
                if (page === 1 && data.products.length === 0) {
                    productsGrid.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-icon">
                                <i class="fas fa-box-open"></i>
                            </div>
                            <h3 class="empty-title">No products yet</h3>
                            <p class="empty-description">Start building your catalog by adding your first product</p>
                            <a href="/create-product" class="primary-btn">
                                <i class="fas fa-plus"></i>
                                <span>Add First Product</span>
                            </a>
                        </div>
                    `;
                    return;
                }
                
                // If it is the first page, clear the grid
                if (page === 1) {
                    productsGrid.innerHTML = '';
                }
                
                // Add products to the grid
                const startIndex = productsGrid.children.length;
                data.products.forEach((product, index) => {
                    const productCard = document.createElement('div');
                    productCard.className = 'product-card';
                    productCard.innerHTML = `
                        <div class="product-image-wrapper">
                            <img src="/image/${product.id}" alt="${product.name}" class="product-image" loading="lazy">
                            <div class="product-overlay">
                                <div class="product-actions">
                                    <button class="action-btn edit-btn" onclick="window.location.href='/edit-product/${product.id}'" title="Edit Product">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="action-btn delete-btn" 
                                            data-id="${product.id}" 
                                            data-name="${product.name}" 
                                            title="Delete Product"
                                            onclick="showDeleteConfirmation(${product.id}, '${product.name}')">
                                        <i class="fas fa-trash-alt"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                        
                        <div class="product-info">
                            <h3 class="product-name">${product.name}</h3>
                            <div class="product-meta">
                                <span class="product-id">ID: ${product.id}</span>
                            </div>
                        </div>
                    `;
                    
                    // Apply animation delay
                    const animationDelay = Math.min(0.05 * (startIndex + index), 0.5);
                    productCard.style.animationDelay = `${animationDelay}s`;
                    
                    productsGrid.appendChild(productCard);
                });
                
                // Update references to all products
                const allProducts = Array.from(productsGrid.children);
                
                // Apply staggered animation delays only to new products
                allProducts.forEach((product, index) => {
                    // Only apply animation to newly loaded products
                    if (index >= startIndex) {
                        // Calculate the delay based on the position within the current batch
                        const positionInCurrentBatch = index - startIndex;
                        const delay = Math.min(0.05 * positionInCurrentBatch, 0.95);
                        product.style.animationDelay = `${delay}s`;
                    }
                });
                
                // Attach event listeners to delete buttons
                setupDeleteButtons();
                
                // Configure product links (no longer needed)
                // setupProductLinks();
                
                // Smooth scroll to new elements if more products are being loaded
                if (page > 1) {
                    const newProducts = Array.from(productsGrid.children).slice(-data.products.length);
                    if (newProducts.length > 0) {
                        setTimeout(() => {
                            newProducts[0].scrollIntoView({ behavior: 'smooth', block: 'start' });
                        }, 100);
                    }
                }
            })
            .catch(error => {
                console.error('Error loading products:', error);
                loadMoreBtn.innerHTML = '<i class="fas fa-exclamation-circle"></i><span>Try Again</span>';
                loadMoreBtn.classList.remove('loading');
                loadMoreBtn.disabled = false;
                
                showNotification('Error loading products. Please try again.', 'error');
            });
    }
    
    // Configure delete buttons
    function setupDeleteButtons() {
        const deleteButtons = document.querySelectorAll('.delete-btn');
        
        deleteButtons.forEach(button => {
            // Remove previous listeners to avoid duplicates
            const newButton = button.cloneNode(true);
            button.parentNode.replaceChild(newButton, button);
            
            // Add new event listener
            newButton.addEventListener('click', function(e) {
                e.preventDefault();
                const productName = this.getAttribute('data-name');
                const productId = this.getAttribute('data-id');
                
                productNameElement.textContent = productName;
                productIdInput.value = productId;
                
                // Open modal with animation
                modalOverlay.classList.add('active');
                document.body.style.overflow = 'hidden'; // Prevent scrolling
            });
        });
    }
    
    // Load initial products
    loadProducts(currentPage);
    
    // Event listener for the "Load More" button
    loadMoreBtn.addEventListener('click', function() {
        if (hasMore && !this.disabled) {
            // Show loading effect
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Loading...</span>';
            this.disabled = true;
            
            // Add a small pause for better visual experience
            setTimeout(() => {
                currentPage++;
                loadProducts(currentPage);
            }, 300);
        }
    });
    
    // Close modal when cancel button is clicked
    cancelButton.addEventListener('click', function() {
        modalOverlay.classList.remove('active');
        document.body.style.overflow = ''; // Restore scrolling
    });
    
    // Close modal when background is clicked
    modalOverlay.addEventListener('click', function(e) {
        if (e.target === modalOverlay) {
            modalOverlay.classList.remove('active');
            document.body.style.overflow = ''; // Restore scrolling
        }
    });
    
    // Ensure form submits correctly
    deleteForm.addEventListener('submit', function(e) {
        e.preventDefault(); // Prevent the form from being submitted
        
        const productId = productIdInput.value;
        const productName = productNameElement.textContent;
        
        if (!productId) {
            showNotification('Error: No product ID found', 'error');
            return;
        }
        
        // Update the delete button to show the loading state
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        const submitButton = this.querySelector('button[type="submit"]');
        const originalButtonText = submitButton.innerHTML;
        submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        submitButton.disabled = true;
        
        //Delete product request
        fetch('/delete-product', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: `productId=${productId}`
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Close the modal
            modalOverlay.classList.remove('active');
            document.body.style.overflow = '';
            
            // Show success notification
            showNotification(`Product "${productName}" deleted successfully`, 'success');
            
            // Add a small pause for better UX
            setTimeout(() => {
                // Reload products (first page)
                currentPage = 1;
                loadProducts(currentPage);
            }, 300);
        })
        .catch(error => {
            console.error('Error deleting product:', error);
            showNotification('Error deleting product. Please try again.', 'error');
            
            // Restore the button
            submitButton.innerHTML = originalButtonText;
            submitButton.disabled = false;
        })
        .finally(() => {
            // Make sure the modal closes
            modalOverlay.classList.remove('active');
            document.body.style.overflow = '';
        });
    });
    
    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Escape key closes modal
        if (e.key === 'Escape' && modalOverlay.classList.contains('active')) {
            modalOverlay.classList.remove('active');
            document.body.style.overflow = '';
        }
    });
    
    // Add styles for notifications
    const style = document.createElement('style');
    style.textContent = `
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            padding: 16px 20px;
            display: flex;
            align-items: center;
            max-width: 400px;
            transform: translateX(120%);
            transition: transform 0.3s ease;
            z-index: 9999;
            border-left: 4px solid #567C8D;
        }
        
        .notification.show {
            transform: translateX(0);
        }
        
        .notification i {
            font-size: 1.2rem;
            margin-right: 12px;
        }
        
        .notification p {
            flex: 1;
            margin: 0;
            font-size: 0.95rem;
        }
        
        .notification-success {
            border-left-color: #28a745;
        }
        
        .notification-success i {
            color: #28a745;
        }
        
        .notification-error {
            border-left-color: #ff5858;
        }
        
        .notification-error i {
            color: #ff5858;
        }
        
        .notification-warning {
            border-left-color: #ffc107;
        }
        
        .notification-warning i {
            color: #ffc107;
        }
        
        .notification-info {
            border-left-color: #567C8D;
        }
        
        .notification-info i {
            color: #567C8D;
        }
        
        .close-notification {
            background: transparent;
            border: none;
            cursor: pointer;
            margin-left: 10px;
            color: #aaa;
            padding: 0;
            font-size: 0.8rem;
            transition: color 0.2s;
        }
        
        .close-notification:hover {
            color: #333;
        }
    `;
    document.head.appendChild(style);
}); 