let currentProductPage = 1;
const productsPerPage = 5;
let minPrice = 0;
let maxPrice = 1000;
let currentSortValue = 'default';
let allProductsLoaded = false;
let searchTimeout = null; // For debouncing search queries
let isLoading = false; // Prevent multiple simultaneous requests

// Función para actualizar indicadores visuales de filtros activos
function updateFilterIndicators() {
    const searchInput = document.getElementById('productSearch');
    const minPriceInput = document.getElementById('price-min');
    const maxPriceInput = document.getElementById('price-max');
    const sortSelect = document.getElementById('priceSort');
    const priceFilterContainer = document.querySelector('.filter-container:has(#price-min)');
    const sortFilterContainer = document.querySelector('.filter-container:has(#priceSort)');
    
    // Indicador para búsqueda activa
    const searchTerm = searchInput.value.trim();
    if (searchTerm) {
        searchInput.parentElement.classList.add('search-input-typing');
    } else {
        searchInput.parentElement.classList.remove('search-input-typing', 'search-input-results', 'search-input-no-results');
    }
    
    // Indicador para filtro de precio activo
    const defaultMinPrice = 0;
    const defaultMaxPrice = 1000;
    const currentMinPrice = parseFloat(minPriceInput.value) || 0;
    const currentMaxPrice = parseFloat(maxPriceInput.value) || 1000;
    
    if (currentMinPrice !== defaultMinPrice || currentMaxPrice !== defaultMaxPrice) {
        priceFilterContainer.classList.add('active');
    } else {
        priceFilterContainer.classList.remove('active');
    }
    
    // Indicador para ordenamiento activo
    if (sortSelect.value !== 'default') {
        sortFilterContainer.classList.add('active');
    } else {
        sortFilterContainer.classList.remove('active');
    }
}

// Función para mostrar feedback después de aplicar filtros
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
    
    // Mostrar notificación
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Ocultar y eliminar notificación
    setTimeout(() => {
        notification.style.transform = 'translateX(full)';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 2000);
}

// Función para cargar productos con indicador de carga mejorado
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
        
        // Actualizar indicadores de filtros
        updateFilterIndicators();
    }
    
    isLoading = true;
    
    // Actualizar estado visual del input de búsqueda
    const searchContainer = searchInput.closest('.search-input-container');
    if (searchTerm) {
        searchContainer.classList.add('search-loading');
    }
    
    loadMoreBtn.disabled = true;
    loadMoreBtn.innerHTML = '<svg class="animate-spin h-4 w-4 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>Loading...';
    
    // Construir la URL con todos los parámetros
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
                // Actualizar estado visual del input según resultados
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
                loadMoreBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>Load More';
                loadMoreBtn.style.display = data.last ? 'none' : 'block';
            } else {
                // Actualizar estado visual para sin resultados
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
            loadMoreBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.69-2.942L13.81 4.658c-.122-.195-.312-.309-.51-.309s-.388.114-.51.309L5.99 13.058C5.178 14.333 6.14 16 7.68 16z"/></svg>Try Again';
            
            if (currentProductPage === 1) {
                showErrorMessage();
            }
        });
}

// Función para realizar búsqueda dinámica con debouncing
function performDynamicSearch() {
    clearTimeout(searchTimeout);
    
    searchTimeout = setTimeout(() => {
        loadMoreProducts(true); // Reset pagination for new search
    }, 300); // 300ms delay for debouncing
}

// Función para mostrar indicador de carga en el grid
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
    
    // Solo mostrar loading si el grid está vacío
    if (productsGrid.children.length === 0) {
        productsGrid.innerHTML = loadingHTML;
    }
}

// Función para ocultar indicador de carga
function hideLoadingGrid() {
    const productsGrid = document.getElementById('productsGrid');
    const loadingElement = productsGrid.querySelector('.col-span-5');
    if (loadingElement && loadingElement.textContent.includes('Searching')) {
        loadingElement.remove();
    }
}

// Función para mostrar mensaje de "sin resultados"
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

// Función para mostrar mensaje de error
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

// Función para actualizar contador de resultados con mejor animación
function updateResultsCounter(totalElements, searchTerm) {
    let counterElement = document.getElementById('resultsCounter');
    
    if (!counterElement) {
        const searchContainer = document.querySelector('.mb-4:has(#productSearch)');
        counterElement = document.createElement('div');
        counterElement.id = 'resultsCounter';
        counterElement.className = 'results-counter';
        searchContainer.appendChild(counterElement);
    }
    
    // Aplicar animación de salida
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

// Función para obtener texto de filtros activos
function getActiveFiltersText() {
    const activeFilters = [];
    
    if (minPrice > 0 || maxPrice < 1000) {
        activeFilters.push('price filtered');
    }
    
    if (currentSortValue !== 'default') {
        activeFilters.push('sorted');
    }
    
    return activeFilters.length > 0 ? ` (${activeFilters.join(', ')})` : '';
}

// Función para limpiar todos los filtros y búsquedas
function clearAllFiltersAndSearch() {
    // Limpiar búsqueda
    document.getElementById('productSearch').value = '';
    
    // Resetear filtros de precio
    document.getElementById('price-min').value = 0;
    document.getElementById('price-max').value = 1000;
    minPrice = 0;
    maxPrice = 1000;
    
    // Resetear ordenamiento
    document.getElementById('priceSort').value = 'default';
    currentSortValue = 'default';
    
    // Recargar productos
    loadMoreProducts(true);
}

// Función para crear una tarjeta de producto
function createProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card';
    
    card.innerHTML = `
        <div class="product-image-container" onclick="window.location.href='/product-details?id=${product.id}'">
            <img src="/image/${product.id}" alt="${product.name}" loading="lazy">
        </div>
        <div class="product-info">
            <h3 class="product-name cursor-pointer hover:text-[#567C8D] transition-colors" onclick="window.location.href='/product-details?id=${product.id}'">${product.name}</h3>
            <p class="product-description">${product.description}</p>
            <p class="text-[#567C8D]/70 text-xs font-light">Stock: ${product.stock}</p>
            <div class="product-buttons">
                <span class="text-lg font-semibold text-[#294156]">$${product.price}</span>
                <form action="/add-to-order" method="post" class="add-to-cart-form">
                    <input type="hidden" name="productId" value="${product.id}">
                    <button type="submit" class="button w-full">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 inline-block mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                        </svg>
                        Add to Cart
                    </button>
                </form>
            </div>
        </div>
    `;
    
    return card;
}

// Función para manejar el envío de formularios de "añadir al carrito"
function attachAddToCartListeners(forms) {
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const button = this.querySelector('button[type="submit"]');
            const originalText = button.innerHTML;
            
            // Cambiar el botón a estado de carga
            button.disabled = true;
            button.innerHTML = '<svg class="animate-spin h-4 w-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>Adding...';
            
            // Enviar el formulario
            fetch(this.action, {
                method: 'POST',
                body: new FormData(this)
            })
            .then(response => {
                if (response.ok) {
                    // Mostrar feedback visual
                    button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>Added!';
                    button.className = button.className.replace('bg-[#294156]', 'bg-green-500');
                    
                    // Restaurar el botón después de 2 segundos
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
                
                // Mostrar error brevemente
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

// Event Listeners mejorados
document.addEventListener('DOMContentLoaded', () => {
    loadMoreProducts();
    
    const searchInput = document.getElementById('productSearch');
    const searchButton = document.getElementById('searchButton');
    
    // Búsqueda dinámica mejorada
    searchInput.addEventListener('input', () => {
        const searchIcon = searchButton.querySelector('svg');
        
        if (searchInput.value.trim()) {
            searchIcon.innerHTML = '<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>';
            searchIcon.classList.add('animate-spin');
            
            // Agregar clase de búsqueda activa
            document.body.classList.add('dynamic-search-active');
        } else {
            searchIcon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />';
            searchIcon.classList.remove('animate-spin');
            
            // Remover clase de búsqueda activa
            document.body.classList.remove('dynamic-search-active');
            
            // Limpiar estados visuales del input
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
    
    // Filtros de precio con feedback visual
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
        
        // Animación del botón
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
    
    // Ordenamiento con feedback
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
    
    // Inicializar indicadores
    updateFilterIndicators();
});