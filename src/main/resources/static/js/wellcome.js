let currentProductPage = 1;
const productsPerPage = 10;
let minPrice = 0;
let maxPrice = 1000;
let minRating = 0; // Variable for minimum rating filter
let currentSortValue = 'default';
let allProductsLoaded = false;
let searchTimeout = null; // For debouncing search queries
let isLoading = false; // Prevent multiple simultaneous requests

// Function to update visual indicators of active filters
function updateFilterIndicators() {
    const searchInput = document.getElementById('productSearch');
    const minPriceInput = document.getElementById('price-min');
    const maxPriceInput = document.getElementById('price-max');
    const sortSelect = document.getElementById('priceSort');
    const minRatingSelect = document.getElementById('min-rating');
    const priceFilterContainer = document.querySelector('.filter-container:has(#price-min)');
    const sortFilterContainer = document.querySelector('.filter-container:has(#priceSort)');
    const ratingFilterContainer = document.querySelector('.filter-container:has(#min-rating)');
    
    // Active search indicator
    const searchTerm = searchInput.value.trim();
    if (searchTerm) {
        searchInput.parentElement.classList.add('search-input-typing');
    } else {
        searchInput.parentElement.classList.remove('search-input-typing', 'search-input-results', 'search-input-no-results');
    }
    
    // Active price filter indicator
    const defaultMinPrice = 0;
    const defaultMaxPrice = 1000;
    const currentMinPrice = parseFloat(minPriceInput.value) || 0;
    const currentMaxPrice = parseFloat(maxPriceInput.value) || 1000;
    
    if (currentMinPrice !== defaultMinPrice || currentMaxPrice !== defaultMaxPrice) {
        priceFilterContainer.classList.add('active');
    } else {
        priceFilterContainer.classList.remove('active');
    }
    
    // Active rating filter indicator
    const defaultMinRating = 0;
    const currentMinRating = parseInt(minRatingSelect.value) || 0;
    
    if (currentMinRating > defaultMinRating) {
        ratingFilterContainer.classList.add('active');
    } else {
        ratingFilterContainer.classList.remove('active');
    }
    
    // Active sorting indicator
    if (sortSelect.value !== 'default') {
        sortFilterContainer.classList.add('active');
    } else {
        sortFilterContainer.classList.remove('active');
    }
}

// Function to show feedback after applying filters
function showFilterFeedback(filterType, isActive) {
    const notifications = document.getElementById('filterNotifications');
    if (!notifications) {
        const notificationContainer = document.createElement('div');
        notificationContainer.id = 'filterNotifications';
        notificationContainer.className = 'fixed top-20 right-4 z-50 space-y-2';
        document.body.appendChild(notificationContainer);
    }
    
    const notification = document.createElement('div');
    notification.className = `bg-white border-l-4 ${isActive ? 'border-green-500' : 'border-yellow-500'} shadow-lg rounded-lg p-3 transition-all duration-300 transform translate-x-full`;
    notification.innerHTML = `
        <div class="flex items-center">
            <svg class="h-5 w-5 ${isActive ? 'text-green-500' : 'text-yellow-500'} mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="${isActive ? 'M5 13l4 4L19 7' : 'M12 9v2m0 4h.01'}" />
            </svg>
            <span class="text-sm font-medium text-gray-800">
                ${filterType} ${isActive ? 'applied' : 'removed'}
            </span>
        </div>
    `;
    
    document.getElementById('filterNotifications').appendChild(notification);
    
    // Show notification
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Hide and delete notification
    setTimeout(() => {
        notification.style.transform = 'translateX(full)';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 2000);
}

// Function to load products with improved loading indicator
function loadMoreProducts(resetPagination = false) {
    if (isLoading) return;
    
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const productsGrid = document.getElementById('productsGrid');
    const searchInput = document.getElementById('productSearch');
    const searchTerm = searchInput.value.trim();
    
    if (resetPagination) {
        currentProductPage = 1;
        productsGrid.innerHTML = '';
        showLoadingGrid();
        
        // Update filter indicators
        updateFilterIndicators();
    }
    
    isLoading = true;
    
    // Update visual state of the search input
    const searchContainer = searchInput.closest('.search-input-container');
    if (searchTerm) {
        searchContainer.classList.add('search-loading');
    }
    
    loadMoreBtn.disabled = true;
    loadMoreBtn.innerHTML = '<svg class="animate-spin h-4 w-4 mr-2 flex-shrink-0" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" style="vertical-align: middle;"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg><span class="leading-none" style="vertical-align: middle;">Loading...</span>';
    
    // Build the URL with all parameters
    let url = `/api/products?page=${currentProductPage}&size=${productsPerPage}`;
    if (searchTerm) {
        url += `&search=${encodeURIComponent(searchTerm)}`;
    }
    if (minPrice > 0) {
        url += `&minPrice=${minPrice}`;
    }
    if (maxPrice < 1000) {
        url += `&maxPrice=${maxPrice}`;
    }
    if (currentSortValue !== 'default') {
        url += `&sort=${currentSortValue}`;
    }
    if (minRating > 0) {
        url += `&minRating=${minRating}`;
    }
    
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            isLoading = false;
            hideLoadingGrid();
            searchContainer.classList.remove('search-loading');
            
            if (currentProductPage === 1) {
                productsGrid.innerHTML = '';
            }
            
            if (data.content && data.content.length > 0) {
                // Update visual state of the search input according to results
                if (searchTerm) {
                    searchInput.parentElement.classList.remove('search-input-no-results');
                    searchInput.parentElement.classList.add('search-input-results');
                }
                
                data.content.forEach((product, index) => {
                    const productCard = createProductCard(product);
                    productCard.className += ' product-card-enter';
                    productsGrid.appendChild(productCard);
                    
                    setTimeout(() => {
                        productCard.className = productCard.className.replace('product-card-enter', 'product-card-enter-active');
                    }, index * 50);
                });

                const forms = productsGrid.querySelectorAll('.add-to-cart-form');
                attachAddToCartListeners(forms);
                
                currentProductPage++;
                loadMoreBtn.disabled = false;
                loadMoreBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" style="vertical-align: middle;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg><span class="leading-none" style="vertical-align: middle;">Load More</span>';
                loadMoreBtn.style.display = data.last ? 'none' : 'block';
            } else {
                // Update visual state for no results
                if (searchTerm) {
                    searchInput.parentElement.classList.remove('search-input-results');
                    searchInput.parentElement.classList.add('search-input-no-results');
                }
                
                loadMoreBtn.style.display = 'none';
                if (currentProductPage === 1) {
                    showNoResultsMessage(searchTerm);
                }
            }
            
            updateResultsCounter(data.totalElements, searchTerm);
        })
        .catch(error => {
            console.error('Error loading products:', error);
            isLoading = false;
            hideLoadingGrid();
            searchContainer.classList.remove('search-loading');
            loadMoreBtn.disabled = false;
            loadMoreBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" style="vertical-align: middle;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.69-2.942L13.81 4.658c-.122-.195-.312-.309-.51-.309s-.388.114-.51.309L5.99 13.058C5.178 14.333 6.14 16 7.68 16z"/></svg><span class="leading-none" style="vertical-align: middle;">Try Again</span>';
            
            if (currentProductPage === 1) {
                showErrorMessage();
            }
        });
}

// Function to perform dynamic search with debouncing
function performDynamicSearch() {
    clearTimeout(searchTimeout);
    
    searchTimeout = setTimeout(() => {
        loadMoreProducts(true); // Reset pagination for new search
    }, 300); // 300ms delay for debouncing
}

// Function to show loading indicator in the grid
function showLoadingGrid() {
    const productsGrid = document.getElementById('productsGrid');
    const loadingHTML = `
        <div class="col-span-5 flex justify-center items-center py-12">
            <div class="flex flex-col items-center space-y-4">
                <svg class="animate-spin h-8 w-8 text-[#294156]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <p class="text-[#294156] text-sm">Searching products...</p>
            </div>
        </div>
    `;
    
    // Only show loading if the grid is empty
    if (productsGrid.children.length === 0) {
        productsGrid.innerHTML = loadingHTML;
    }
}

// Function to hide loading indicator
function hideLoadingGrid() {
    const productsGrid = document.getElementById('productsGrid');
    const loadingElement = productsGrid.querySelector('.col-span-5');
    if (loadingElement && loadingElement.textContent.includes('Searching')) {
        loadingElement.remove();
    }
}

// Function to show "no results" message
function showNoResultsMessage(searchTerm) {
    const productsGrid = document.getElementById('productsGrid');
    const message = searchTerm ? 
        `No products found for "${searchTerm}". Try adjusting your search or filters.` :
        'No products found. Try adjusting your filters.';
    
    productsGrid.innerHTML = `
        <div class="col-span-5 text-center py-12">
            <div class="flex flex-col items-center space-y-4">
                <svg class="h-16 w-16 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <h3 class="text-lg font-medium text-gray-900">No products found</h3>
                <p class="text-gray-500 max-w-md">${message}</p>
                <button onclick="clearAllFiltersAndSearch()" class="mt-4 bg-[#294156] text-white px-4 py-2 rounded-lg hover:bg-[#567C8D] transition-colors">
                    Clear all filters
                </button>
            </div>
        </div>
    `;
}

// Function to show error message
function showErrorMessage() {
    const productsGrid = document.getElementById('productsGrid');
    productsGrid.innerHTML = `
        <div class="col-span-5 text-center py-12">
            <div class="flex flex-col items-center space-y-4">
                <svg class="h-16 w-16 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.69-2.942L13.81 4.658c-.122-.195-.312-.309-.51-.309s-.388.114-.51.309L5.99 13.058C5.178 14.333 6.14 16 7.68 16z" />
                </svg>
                <h3 class="text-lg font-medium text-red-600">Error loading products</h3>
                <p class="text-gray-500">There was a problem loading the products. Please try again.</p>
                <button onclick="loadMoreProducts(true)" class="mt-4 bg-[#294156] text-white px-4 py-2 rounded-lg hover:bg-[#567C8D] transition-colors">
                    Try again
                </button>
            </div>
        </div>
    `;
}

// Function to update results counter with better animation
function updateResultsCounter(totalElements, searchTerm) {
    let counterElement = document.getElementById('resultsCounter');
    
    if (!counterElement) {
        const searchContainer = document.querySelector('.mb-4:has(#productSearch)');
        counterElement = document.createElement('div');
        counterElement.id = 'resultsCounter';
        counterElement.className = 'results-counter';
        searchContainer.appendChild(counterElement);
    }
    
    // Apply exit animation
    counterElement.classList.remove('visible');
    
    setTimeout(() => {
        if (totalElements > 0) {
            const searchText = searchTerm ? ` for "${searchTerm}"` : '';
            const filterText = getActiveFiltersText();
            counterElement.textContent = `Found ${totalElements} product${totalElements !== 1 ? 's' : ''}${searchText}${filterText}`;
            counterElement.classList.add('visible');
        } else {
            counterElement.textContent = '';
        }
    }, 150);
}

// Function to get active filters text
function getActiveFiltersText() {
    const activeFilters = [];
    
    if (minPrice > 0 || maxPrice < 1000) {
        activeFilters.push('price filtered');
    }
    
    if (minRating > 0) {
        activeFilters.push(`${minRating}★+ rated`);
    }
    
    if (currentSortValue !== 'default') {
        activeFilters.push('sorted');
    }
    
    return activeFilters.length > 0 ? ` (${activeFilters.join(', ')})` : '';
}

// Function to clear all filters and searches
function clearAllFiltersAndSearch() {
    // Clear search
    document.getElementById('productSearch').value = '';
    
    // Reset price filters
    document.getElementById('price-min').value = 0;
    document.getElementById('price-max').value = 1000;
    minPrice = 0;
    maxPrice = 1000;
    
    // Reset rating filter
    document.getElementById('min-rating').value = 0;
    minRating = 0;
    
    // Reset sorting
    document.getElementById('priceSort').value = 'default';
    currentSortValue = 'default';
    
    // Reload products
    loadMoreProducts(true);
}

function getCsrfToken() {
    return document.querySelector('meta[name="csrf-token"]').getAttribute('content');
}

function createStarRating(rating) {
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5 ? 1 : 0;
    const emptyStars = 5 - fullStars - halfStar;
    let starsHTML = '<div class="star-rating">';

    for (let i = 0; i < fullStars; i++) {
        starsHTML += '<i class="fas fa-star"></i>';
    }
    if (halfStar) {
        starsHTML += '<i class="fas fa-star-half-alt"></i>';
    }
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '<i class="far fa-star"></i>';
    }
    starsHTML += '</div>';
    return starsHTML;
}

function createProductCard(product) {
    const csrfToken = getCsrfToken();
    const card = document.createElement('div');
    card.className = 'product-card';
    
    card.innerHTML = `
        <a href="/product-details?id=${product.id}" class="block">
            <div class="product-image-container">
                <img src="/image/${product.id}" alt="${product.name}" onerror="this.onerror=null;this.src='/Images/default.jpg';">
            </div>
        </a>
        <div class="product-info p-3">
            <div class="product-info-top">
                <h3 class="product-name">${product.name}</h3>
                ${createStarRating(product.rating)}
                <p class="product-description">${product.description}</p>
            </div>
            
            <div>
                <div class="product-price-stock">
                    <span class="product-price">$${product.price.toFixed(2)}</span>
                    <span class="product-stock">${product.stock > 0 ? `${product.stock} available` : 'Out of stock'}</span>
                </div>
                <div class="product-buttons">
                    <form action="/add-to-order" method="post" class="add-to-cart-form">
                        <input type="hidden" name="productId" value="${product.id}">
                        <input type="hidden" name="_csrf" value="${csrfToken}"/>
                        <button type="submit" class="button">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                            </svg>
                            Add to Cart
                        </button>
                    </form>
                </div>
            </div>
        </div>
    `;
    
    return card;
}

// Function to handle the submission of "add to cart" forms
function attachAddToCartListeners(forms) {
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const button = this.querySelector('button[type="submit"]');
            const originalText = button.innerHTML;
            
            // Change the button to loading state
            button.disabled = true;
            button.innerHTML = '<svg class="animate-spin h-4 w-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>Adding...';
            
            // Submit the form
            fetch(this.action, {
                method: 'POST',
                body: new FormData(this)
            })
            .then(response => {
                if (response.ok) {
                    // Show visual feedback
                    button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>Added!';
                    button.className = button.className.replace('bg-[#294156]', 'bg-green-500');
                    
                    // Restore the button after 2 seconds
                    setTimeout(() => {
                        button.disabled = false;
                        button.innerHTML = originalText;
                        button.className = button.className.replace('bg-green-500', 'bg-[#294156]');
                    }, 2000);
                } else {
                    throw new Error('Failed to add to cart');
                }
            })
            .catch(error => {
                console.error('Error adding to cart:', error);
                button.disabled = false;
                button.innerHTML = originalText;
                
                // Show error briefly
                button.innerHTML = 'Error';
                button.className = button.className.replace('bg-[#294156]', 'bg-red-500');
                setTimeout(() => {
                    button.innerHTML = originalText;
                    button.className = button.className.replace('bg-red-500', 'bg-[#294156]');
                }, 2000);
            });
        });
    });
}

// Improved event listeners
document.addEventListener('DOMContentLoaded', () => {
    loadMoreProducts();
    
    const searchInput = document.getElementById('productSearch');
    const searchButton = document.getElementById('searchButton');
    const applyRatingFilterBtn = document.getElementById('apply-rating-filter');
    const resetRatingFilterBtn = document.getElementById('reset-rating-filter');
    const minRatingSelect = document.getElementById('min-rating');
    
    // Improved dynamic search
    searchInput.addEventListener('input', () => {
        const searchIcon = searchButton.querySelector('svg');
        
        if (searchInput.value.trim()) {
            searchIcon.innerHTML = '<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>';
            searchIcon.classList.add('animate-spin');
            
            // Add active search class
            document.body.classList.add('dynamic-search-active');
        } else {
            searchIcon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />';
            searchIcon.classList.remove('animate-spin');
            
            // Remove active search class
            document.body.classList.remove('dynamic-search-active');
            
            // Clear visual states of the input
            searchInput.parentElement.classList.remove('search-input-typing', 'search-input-results', 'search-input-no-results');
        }
        
        performDynamicSearch();
    });
    
    searchButton.addEventListener('click', () => {
        clearTimeout(searchTimeout);
        loadMoreProducts(true);
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            clearTimeout(searchTimeout);
            loadMoreProducts(true);
        }
    });
    
    // Event listener for Apply Rating Filter button
    applyRatingFilterBtn.addEventListener('click', () => {
        const selectedRating = parseInt(minRatingSelect.value) || 0;
        if (minRating !== selectedRating) {
            minRating = selectedRating;
            showFilterFeedback('Rating filter', selectedRating > 0);
            loadMoreProducts(true);
        }
    });
    
    // Event listener for Reset Rating Filter button
    resetRatingFilterBtn.addEventListener('click', () => {
        if (minRating !== 0) {
            minRating = 0;
            minRatingSelect.value = 0;
            showFilterFeedback('Rating filter', false);
            loadMoreProducts(true);
        }
    });
    
    // Price filters with visual feedback
    const applyPriceFilter = document.getElementById('apply-price-filter');
    const resetPriceFilter = document.getElementById('reset-price-filter');
    const minPriceInput = document.getElementById('price-min');
    const maxPriceInput = document.getElementById('price-max');
    
    let priceFilterTimeout = null;
    
    function applyPriceFilterDynamic() {
        clearTimeout(priceFilterTimeout);
        priceFilterTimeout = setTimeout(() => {
            const newMinPrice = parseFloat(minPriceInput.value) || 0;
            const newMaxPrice = parseFloat(maxPriceInput.value) || 1000;
            
            const wasActive = minPrice > 0 || maxPrice < 1000;
            const isNowActive = newMinPrice > 0 || newMaxPrice < 1000;
            
            minPrice = newMinPrice;
            maxPrice = newMaxPrice;
            
            if (wasActive !== isNowActive) {
                showFilterFeedback('Price filter', isNowActive);
            }
            
            loadMoreProducts(true);
        }, 500);
    }
    
    minPriceInput.addEventListener('input', applyPriceFilterDynamic);
    maxPriceInput.addEventListener('input', applyPriceFilterDynamic);
    
    applyPriceFilter.addEventListener('click', () => {
        clearTimeout(priceFilterTimeout);
        
        const newMinPrice = parseFloat(minPriceInput.value) || 0;
        const newMaxPrice = parseFloat(maxPriceInput.value) || 1000;
        
        minPrice = newMinPrice;
        maxPrice = newMaxPrice;
        
        // Button animation
        applyPriceFilter.classList.add('filter-applied');
        setTimeout(() => {
            applyPriceFilter.classList.remove('filter-applied');
        }, 1000);
        
        loadMoreProducts(true);
    });
    
    resetPriceFilter.addEventListener('click', () => {
        minPriceInput.value = 0;
        maxPriceInput.value = 1000;
        minPrice = 0;
        maxPrice = 1000;
        
        showFilterFeedback('Price filter', false);
        loadMoreProducts(true);
    });
    
    // Sorting with feedback
    const priceSort = document.getElementById('priceSort');
    priceSort.addEventListener('change', () => {
        const wasActive = currentSortValue !== 'default';
        currentSortValue = priceSort.value;
        const isNowActive = currentSortValue !== 'default';
        
        if (wasActive !== isNowActive) {
            showFilterFeedback('Sort option', isNowActive);
        }
        
        loadMoreProducts(true);
    });
    
    // Initialize indicators
    updateFilterIndicators();
});