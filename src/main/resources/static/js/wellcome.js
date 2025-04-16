 // Global variables for price filtering
 window.currentProductPage = 1;
 window.productsPerPage = 5;
 window.allProductsLoaded = false;
 window.minPrice = 0;
 window.maxPrice = 1000;
 window.dynamicMaxPrice = 1000; // Will be updated from server
 window.currentSortValue = "default";

 // Function to get maximum and minimum price from server
 async function fetchInitialMaxPrice() {
     try {
         const response = await fetch(`/api/products?page=1&size=1`);
         const data = await response.json();
         
         if (data.maxPriceForFilter) {
             window.dynamicMaxPrice = Math.ceil(data.maxPriceForFilter);
             window.maxPrice = window.dynamicMaxPrice;
             
             // Also look for minimum price of products
             fetch(`/api/products?page=1&size=${window.productsPerPage}&sort=price_asc`)
                 .then(response => response.json())
                 .then(data => {
                     if (data.products && data.products.length > 0) {
                         // Get the minimum price found
                         const lowestPrice = Math.floor(data.products[0].price);
                         
                         // Update minimum price if greater than zero
                         if (lowestPrice > 0) {
                             window.minPrice = lowestPrice;
                             
                             // Update labels with new values
                             document.querySelector('label[for="price-min"]').textContent = `Min Price ($${window.minPrice})`;
                             document.querySelector('label[for="price-max"]').textContent = `Max Price ($${window.dynamicMaxPrice})`;
                             
                             console.log(`Prices updated: min=${window.minPrice}, max=${window.dynamicMaxPrice}`);
                         }
                     }
                 })
                 .catch(error => {
                     console.error("Error fetching min price:", error);
                 });
         }
     } catch (error) {
         console.error("Error fetching max price:", error);
     }
 }
 
 // Call this on page load
 window.onload = function() {
     fetchInitialMaxPrice().then(() => {
         loadMoreProducts();
     });
 };
 
 // Function to generate product card HTML
 function generateProductCard(product) {
     return `
         <div class="product-card rounded-lg overflow-hidden product-item" data-id="${product.id}" data-name="${product.name}" data-price="${product.price}">
             <div class="product-image-container">
                 <a href="/product-details?id=${product.id}">
                     <img src="/image/${product.id}" alt="${product.name}" class="cursor-pointer hover:scale-105 transition-transform duration-500">
                 </a>
             </div>
             <div class="product-info p-3 space-y-1">
                 <h3 class="product-name text-[#294156] text-lg font-medium leading-tight truncate" title="${product.name}">${product.name}</h3>
                 <p class="text-[#567C8D] font-light">$${product.price}</p>
                 <p class="text-[#567C8D]/70 text-xs font-light">Stock: ${product.stock}</p>
             </div>
             <div class="product-buttons p-3 pt-0">
                 <form action="/add-to-order" method="post" class="block add-to-cart-form">
                     <input type="hidden" name="productId" value="${product.id}">
                     <button type="submit" class="w-full bg-[#294156] text-white px-4 py-2 rounded hover:bg-[green] transition-colors text-sm">
                         <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 inline-block mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                         </svg>
                         Add to Cart
                     </button>
                 </form>
                 ${product.isAdmin ? `
                 <form action="/delete-product" method="post" class="block mt-2">
                     <input type="hidden" name="productId" value="${product.id}">
                     <button type="submit" class="w-full bg-[#567C8D] text-white px-4 py-2 rounded hover:bg-[red] transition-colors text-sm">Delete</button>
                 </form>
                 ` : ''}
             </div>
         </div>
     `;
 }
 
 // Function to add products to grid
 function addProductsToGrid(products, productsGrid) {
     if (products && products.length > 0) {
         products.forEach(product => {
             const template = generateProductCard(product);
             const tempDiv = document.createElement('div');
             tempDiv.innerHTML = template.trim();
             productsGrid.appendChild(tempDiv.firstChild);
         });
         
         // Attach event listeners to the newly added forms
         attachAddToCartListeners(document.querySelectorAll('.add-to-cart-form'));
         
         // Dispatch event for dark mode
         document.dispatchEvent(new CustomEvent('productsLoaded'));
         
         return true;
     }
     
     return false;
 }
 
 // Function to update load more button state
 function updateLoadMoreButton(hasMore, loadMoreBtn, loadMoreFunction = null) {
     loadMoreBtn.disabled = false;
     loadMoreBtn.textContent = 'Load More Products';
     
     if (hasMore) {
         loadMoreBtn.style.display = '';
         if (loadMoreFunction) {
             loadMoreBtn.onclick = loadMoreFunction;
         }
     } else {
         loadMoreBtn.style.display = 'none';
         window.allProductsLoaded = true;
     }
 }
 
 // Function to show loading state
 function showLoading(productsGrid) {
     if (window.currentProductPage === 1) {
         productsGrid.innerHTML = '<div class="col-span-full flex justify-center items-center py-8"><div class="loading-spinner mr-3"></div><span>Loading products...</span></div>';
     }
 }
 
 // Function to show no products message
 function showNoProductsMessage(productsGrid, message = "No products found") {
     productsGrid.innerHTML = `
         <div class="col-span-full text-center py-8">
             <p class="text-[#294156] opacity-70">${message}</p>
             <button id="reset-filters" class="mt-4 bg-[#567C8D] text-white px-4 py-2 rounded hover:bg-[#294156] transition-colors">
                 Reset Filters
             </button>
         </div>
     `;
     
     document.getElementById('reset-filters').addEventListener('click', function() {
         // Reset price filter inputs
         const minValueInput = document.getElementById('price-min');
         const maxValueInput = document.getElementById('price-max');
         minValueInput.value = window.minPrice;
         maxValueInput.value = window.dynamicMaxPrice;
         
         // Reset global variables
         window.minPrice = minValueInput.value;
         window.maxPrice = window.dynamicMaxPrice;
         
         // Reload products
         window.currentProductPage = 1;
         loadMoreProducts();
     });
 }
 
 // Function to show error message
 function showErrorMessage(productsGrid, message = "Error loading products. Please try again.") {
     if (window.currentProductPage === 1) {
         productsGrid.innerHTML = `
             <div class="col-span-full text-center py-8">
                 <p class="text-red-500">${message}</p>
             </div>
         `;
     }
 }
 
 // Function to load more products
 function loadMoreProducts() {
     const loadMoreBtn = document.getElementById('loadMoreBtn');
     const productsGrid = document.getElementById('productsGrid');
     
     // Disable the button while loading products
     loadMoreBtn.disabled = true;
     loadMoreBtn.textContent = 'Loading...';
     
     console.log(`Loading products with filters: min=${window.minPrice}, max=${window.maxPrice}, sort=${window.currentSortValue}`);
     
     // Make AJAX request to the server to get the next page of products
     fetch(`/api/products?page=${window.currentProductPage}&size=${window.productsPerPage}&minPrice=${window.minPrice}&maxPrice=${window.maxPrice}&sort=${window.currentSortValue}`)
         .then(response => response.json())
         .then(data => {
             if (window.currentProductPage === 1) {
                 productsGrid.innerHTML = '';
             }
             
             const productsAdded = addProductsToGrid(data.products, productsGrid);
             
             if (productsAdded) {
                 // Increment the page counter
                 window.currentProductPage++;
                 updateLoadMoreButton(data.hasMore, loadMoreBtn);
             } else if (window.currentProductPage === 1) {
                 // No products found with current filters
                 showNoProductsMessage(productsGrid);
                 loadMoreBtn.style.display = 'none';
                 window.allProductsLoaded = true;
             }
         })
         .catch(error => {
             console.error('Error loading products:', error);
             showErrorMessage(productsGrid);
             loadMoreBtn.disabled = false;
             loadMoreBtn.textContent = 'Load More Products';
         });
 }

 // Function to attach event listeners to add-to-cart forms
 function attachAddToCartListeners(forms) {
     forms.forEach(form => {
         form.addEventListener('submit', async function(e) {
             e.preventDefault();
             const button = this.querySelector('button');
             const originalText = button.innerHTML;
             const originalColor = button.style.backgroundColor;
             const stockText = this.closest('.product-card').querySelector('.product-info p:nth-child(3)').textContent;
             const stockNumber = parseInt(stockText.replace('Stock: ', ''), 10);
             
             // Save original transition and apply new one for smooth effect
             const originalTransition = button.style.transition;
             button.style.transition = 'background-color 0.3s ease, color 0.3s ease';

             // Check if product is out of stock
             if (stockNumber <= 0) {
                 showButtonMessage(button, 'Out of Stock!', '#ff0000', originalText, originalColor, originalTransition);
                 return;
             }

             button.disabled = true;
             
             try {
                 // Check the quantity in the cart
                 const productId = this.querySelector('input[name="productId"]').value;
                 const cartResponse = await fetch(`/get-cart-quantity?productId=${productId}`);
                 const cartData = await cartResponse.json();
                 const cartQuantity = cartData.quantity || 0;
                 
                 // Check if adding one more exceeds the stock
                 if (cartQuantity + 1 > stockNumber) {
                     showButtonMessage(button, 'Out of Stock!', '#ff0000', originalText, originalColor, originalTransition);
                     return;
                 }
                 
                 const response = await fetch(this.action, {
                     method: 'POST',
                     body: new FormData(this)
                 });
                 
                 // Try to process the response as JSON
                 let data = {};
                 try {
                     data = await response.json();
                 } catch (e) {
                     console.log('Response is not JSON, continuing...');
                 }
                 
                 if (response.ok) {
                     showButtonMessage(button, 'Added!', '#2e7d32', originalText, originalColor, originalTransition);
                     
                     // Update the cart counter in the header
                     const cartCounter = document.querySelector('.cart-counter');
                     if (cartCounter && data && data.cartSize) {
                         cartCounter.textContent = data.cartSize;
                     }
                 } else {
                     throw new Error('Failed to add to cart');
                 }
             } catch (error) {
                 console.error('Error:', error);
                 showButtonMessage(button, 'Error', '#ff0000', originalText, originalColor, originalTransition, 2000);
             }
         });
     });
 }
 
 // Helper function to show button messages with animation
 function showButtonMessage(button, text, bgColor, originalText, originalColor, originalTransition, duration = 1000) {
     button.textContent = text;
     button.style.backgroundColor = bgColor;
     button.style.color = 'white';
     button.style.transform = 'scale(1.03)';
     button.style.boxShadow = `0 4px 8px rgba(${bgColor === '#2e7d32' ? '46, 125, 50' : '255, 0, 0'}, 0.5)`;
     button.disabled = true;

     setTimeout(() => {
         button.innerHTML = originalText;
         button.style.backgroundColor = originalColor;
         button.style.color = '';
         button.style.transform = '';
         button.style.boxShadow = '';
         button.style.transition = originalTransition;
         button.disabled = false;
     }, duration);
 }

 // Initialize event listeners on document load
 document.addEventListener('DOMContentLoaded', function() {
     // Product search functionality
     const searchInput = document.getElementById('productSearch');
     const searchButton = document.getElementById('searchButton');
     const priceSort = document.getElementById('priceSort');

     // Price sorting functionality
     priceSort.addEventListener('change', function() {
         const sortValue = this.value;
         window.currentSortValue = sortValue;
         
         // Reset pagination and reload products
         window.currentProductPage = 1;
         const productsGrid = document.getElementById('productsGrid');
         showLoading(productsGrid);
         loadMoreProducts();
     });
     
     // Database search function
     function performSearch(searchTerm) {
         // If search term is empty, load all products
         if (!searchTerm) {
             window.location.href = '/products';
             return;
         }
         
         const productsGrid = document.getElementById('productsGrid');
         const loadMoreBtn = document.getElementById('loadMoreBtn');
         
         // Reset pagination
         window.currentProductPage = 1;
         
         // Show loading spinner
         showLoading(productsGrid);
         
         // Get current price filter values
         const currentMinPrice = parseInt(document.getElementById('price-min').value) || 0;
         const currentMaxPrice = parseInt(document.getElementById('price-max').value) || window.dynamicMaxPrice;
         
         // Make search request with price filters
         fetch(`/search-products-json?term=${encodeURIComponent(searchTerm)}&page=1&size=${window.productsPerPage}&minPrice=${currentMinPrice}&maxPrice=${currentMaxPrice}`)
             .then(response => response.json())
             .then(data => {
                 productsGrid.innerHTML = '';
                 
                 const productsAdded = addProductsToGrid(data.products, productsGrid);
                 
                 if (productsAdded) {
                     // Increment page
                     window.currentProductPage++;
                     
                     // Update load more button to use search function
                     updateLoadMoreButton(data.hasMore, loadMoreBtn, function() {
                         loadMoreWithSearch(searchTerm);
                     });
                 } else {
                     // No products found
                     showNoProductsMessage(productsGrid, `No products found for "${searchTerm}"`);
                     loadMoreBtn.style.display = 'none';
                 }
             })
             .catch(error => {
                 console.error('Search error:', error);
                 showErrorMessage(productsGrid, 'Error searching for products. Please try again.');
                 loadMoreBtn.style.display = 'none';
             });
     }
     
     // Function to load more search results
     function loadMoreWithSearch(searchTerm) {
         const loadMoreBtn = document.getElementById('loadMoreBtn');
         const productsGrid = document.getElementById('productsGrid');
         
         loadMoreBtn.disabled = true;
         loadMoreBtn.textContent = 'Loading...';
         
         // Get current price filter values
         const currentMinPrice = parseInt(document.getElementById('price-min').value) || 0;
         const currentMaxPrice = parseInt(document.getElementById('price-max').value) || window.dynamicMaxPrice;
         
         fetch(`/search-products-json?term=${encodeURIComponent(searchTerm)}&page=${window.currentProductPage}&size=${window.productsPerPage}&minPrice=${currentMinPrice}&maxPrice=${currentMaxPrice}`)
             .then(response => response.json())
             .then(data => {
                 const productsAdded = addProductsToGrid(data.products, productsGrid);
                 
                 if (productsAdded) {
                     // Increment page count
                     window.currentProductPage++;
                     
                     // Update button with the same search function
                     updateLoadMoreButton(data.hasMore, loadMoreBtn, function() {
                         loadMoreWithSearch(searchTerm);
                     });
                 } else {
                     // No more products
                     loadMoreBtn.style.display = 'none';
                 }
             })
             .catch(error => {
                 console.error('Error loading more search results:', error);
                 loadMoreBtn.disabled = false;
                 loadMoreBtn.textContent = 'Load More Products';
             });
     }
     
     // Event listeners for search
     searchButton.addEventListener('click', function() {
         const searchTerm = searchInput.value.trim();
         performSearch(searchTerm);
     });
     
     // Allow search on Enter key
     searchInput.addEventListener('keydown', function(event) {
         if (event.key === 'Enter') {
             event.preventDefault();
             const searchTerm = this.value.trim();
             performSearch(searchTerm);
         }
     });
 });