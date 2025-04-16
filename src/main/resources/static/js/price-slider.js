// Function to configure price filter inputs
function initPriceFilter() {
    console.log("Initializing price filter");
    
    // Get references to DOM elements
    const minValueInput = document.getElementById('price-min');
    const maxValueInput = document.getElementById('price-max');
    const applyFilterBtn = document.getElementById('apply-price-filter');
    const resetFilterBtn = document.getElementById('reset-price-filter');
    
    // Check if elements exist
    if (!minValueInput || !maxValueInput) {
        console.error("Error: Price filter elements not found");
        return;
    }

    // Default global values and initial configuration
    let minPrice = 0;
    let maxPrice = 1000;
    let dynamicMaxPrice = 1000;
    
    // Try to access global variables
    try {
        minPrice = typeof window.minPrice !== 'undefined' ? window.minPrice : 0;
        dynamicMaxPrice = typeof window.dynamicMaxPrice !== 'undefined' ? window.dynamicMaxPrice : 1000;
        maxPrice = typeof window.maxPrice !== 'undefined' ? window.maxPrice : dynamicMaxPrice;
        
        console.log("Initial values:", {minPrice, maxPrice, dynamicMaxPrice});
    } catch (e) {
        console.error("Error getting global variables:", e);
    }
    
    const minGap = 10; // Minimum difference between min and max
    
    // Only set values if they aren't already set or are at default values
    if (!minValueInput.value || parseInt(minValueInput.value) < minPrice) {
        minValueInput.value = minPrice;
    }
    
    if (!maxValueInput.value || parseInt(maxValueInput.value) > dynamicMaxPrice) {
        maxValueInput.value = dynamicMaxPrice;
    }
    
    // Update labels
    const minPriceLabel = document.querySelector('label[for="price-min"]');
    const maxPriceLabel = document.querySelector('label[for="price-max"]');
    
    if (minPriceLabel) minPriceLabel.textContent = `Min Price ($${minPrice})`;
    if (maxPriceLabel) maxPriceLabel.textContent = `Max Price ($${dynamicMaxPrice})`;
    
    // Validate numeric values
    function validateNumberInput(input, isMin) {
        let value = parseInt(input.value);
        const otherValue = isMin ? parseInt(maxValueInput.value) : parseInt(minValueInput.value);
        
        // Validate if it's a number
        if (isNaN(value)) {
            value = isMin ? minPrice : dynamicMaxPrice;
        }
        
        // Limit to valid range
        if (isMin) {
            if (value < minPrice) value = minPrice;
            if (value > otherValue - minGap) value = otherValue - minGap;
        } else {
            if (value > dynamicMaxPrice) value = dynamicMaxPrice;
            if (value < otherValue + minGap) value = otherValue + minGap;
        }
        
        input.value = value;
    }
    
    // Event for minimum numeric input
    minValueInput.addEventListener('change', function() {
        validateNumberInput(this, true);
    });
    
    // Event for maximum numeric input
    maxValueInput.addEventListener('change', function() {
        validateNumberInput(this, false);
    });
    
    // Event for apply filter button
    if (applyFilterBtn) {
        applyFilterBtn.addEventListener('click', function() {
            try {
                // Update global variables
                window.minPrice = parseInt(minValueInput.value);
                window.maxPrice = parseInt(maxValueInput.value);
                
                // Visual effect
                this.classList.add('filter-applied');
                setTimeout(() => {
                    this.classList.remove('filter-applied');
                }, 1000);
                
                // Reload products
                reloadProducts();
            } catch (e) {
                console.error("Error applying filter:", e);
            }
        });
    }
    
    // Event for reset filter button
    if (resetFilterBtn) {
        resetFilterBtn.addEventListener('click', function() {
            // Reset controls
            minValueInput.value = minPrice;
            maxValueInput.value = dynamicMaxPrice;
            
            // Reset global variables
            window.minPrice = minPrice;
            window.maxPrice = dynamicMaxPrice;
            
            // Reload products
            reloadProducts();
        });
    }
    
    // Helper function to reload products
    function reloadProducts() {
        if (typeof window.currentProductPage !== 'undefined') {
            window.currentProductPage = 1;
            
            const productsGrid = document.getElementById('productsGrid');
            if (productsGrid) {
                productsGrid.innerHTML = '<div class="col-span-full flex justify-center items-center py-8"><div class="loading-spinner mr-3"></div><span>Filtering products...</span></div>';
            }
            
            if (typeof window.loadMoreProducts === 'function') {
                window.loadMoreProducts();
            }
        }
    }
    
    console.log("Price filter initialized correctly");
}

// Initialize when the page is fully loaded
window.addEventListener('load', function() {
    // Initialize with a small delay to ensure all global variables are available
    setTimeout(initPriceFilter, 300);
}); 