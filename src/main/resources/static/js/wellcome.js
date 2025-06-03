let currentProductPage = 1;
const productsPerPage = 5;
let minPrice = 0;
let maxPrice = 1000;
let currentSortValue = 'default';
let allProductsLoaded = false;

// Función para cargar productos
function loadMoreProducts() {
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const productsGrid = document.getElementById('productsGrid');
    const searchInput = document.getElementById('productSearch');
    const searchTerm = searchInput.value.trim();
    
    // Deshabilitar el botón mientras se cargan los productos
    loadMoreBtn.disabled = true;
    loadMoreBtn.textContent = 'Loading...';
    
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
    
    // Hacer la petición AJAX al servidor
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (currentProductPage === 1) {
                productsGrid.innerHTML = '';
            }
            
            // Añadir productos al grid
            data.content.forEach(product => {
                const productCard = createProductCard(product);
                productsGrid.appendChild(productCard);
            });

            // Adjuntar listeners para los formularios de añadir al carrito
            const forms = productsGrid.querySelectorAll('.add-to-cart-form');
            attachAddToCartListeners(forms);
            
            // Actualizar el botón de "Load More"
            if (data.content.length > 0) {
                currentProductPage++;
                loadMoreBtn.disabled = false;
                loadMoreBtn.textContent = 'Load More';
                loadMoreBtn.style.display = data.last ? 'none' : 'block';
            } else {
                loadMoreBtn.style.display = 'none';
                if (currentProductPage === 1) {
                    productsGrid.innerHTML = '<div class="col-span-5 text-center text-gray-500">No products found</div>';
                }
            }
        })
        .catch(error => {
            console.error('Error loading products:', error);
            loadMoreBtn.disabled = false;
            loadMoreBtn.textContent = 'Load More';
            productsGrid.innerHTML = '<div class="col-span-5 text-center text-red-500">Error loading products</div>';
        });
}

// Función para crear una tarjeta de producto
function createProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card';
    
    card.innerHTML = `
        <div class="product-image-container" onclick="window.location.href='/product-details?id=${product.id}'">
            <img src="/image/${product.id}" alt="${product.name}">
        </div>
        <div class="product-info">
            <h3 class="product-name" onclick="window.location.href='/product-details?id=${product.id}'">${product.name}</h3>
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

// Función para adjuntar listeners a los formularios de añadir al carrito
function attachAddToCartListeners(forms) {
    forms.forEach(form => {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            const button = this.querySelector('button');
            const originalText = button.innerHTML;
            const originalColor = button.style.backgroundColor;
            const stockText = this.closest('.product-card').querySelector('.product-info p:nth-child(3)').textContent;
            const stockNumber = parseInt(stockText.replace('Stock: ', ''), 10);
            
            // Guardar transición original y aplicar nueva para efecto suave
            const originalTransition = button.style.transition;
            button.style.transition = 'background-color 0.3s ease, color 0.3s ease';

            // Verificar si el producto está agotado
            if (stockNumber <= 0) {
                showButtonMessage(button, 'Out of Stock!', '#ff0000', originalText, originalColor, originalTransition);
                return;
            }

            button.disabled = true;
            
            try {
                // Verificar la cantidad en el carrito
                const productId = this.querySelector('input[name="productId"]').value;
                const cartResponse = await fetch(`/get-cart-quantity?productId=${productId}`);
                const cartData = await cartResponse.json();
                const cartQuantity = cartData.quantity || 0;
                
                // Verificar si al añadir uno más se supera el stock
                if (cartQuantity + 1 > stockNumber) {
                    showButtonMessage(button, 'Out of Stock!', '#ff0000', originalText, originalColor, originalTransition);
                    return;
                }
                
                const response = await fetch(this.action, {
                    method: 'POST',
                    body: new FormData(this)
                });
                
                // Intentar procesar la respuesta como JSON
                let data = {};
                try {
                    data = await response.json();
                } catch (e) {
                    console.log('La respuesta no es JSON, continuando...');
                }
                
                if (response.ok) {
                    showButtonMessage(button, 'Added!', '#2e7d32', originalText, originalColor, originalTransition);
                    
                    // Actualizar el contador del carrito en el header
                    const cartCounter = document.querySelector('.cart-counter');
                    if (cartCounter && data && data.cartSize) {
                        cartCounter.textContent = data.cartSize;
                    }
                } else {
                    throw new Error('Error al añadir al carrito');
                }
            } catch (error) {
                console.error('Error:', error);
                showButtonMessage(button, 'Error', '#ff0000', originalText, originalColor, originalTransition, 2000);
            }
        });
    });
}

// Función para mostrar mensajes en el botón con animación
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

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    // Cargar productos iniciales
    loadMoreProducts();
    
    // Configurar el botón de búsqueda
    const searchButton = document.getElementById('searchButton');
    const searchInput = document.getElementById('productSearch');
    
    searchButton.addEventListener('click', () => {
        currentProductPage = 1;
        loadMoreProducts();
    });

    // Permitir búsqueda al presionar Enter
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            currentProductPage = 1;
            loadMoreProducts();
        }
    });
    
    // Configurar el filtro de precio
    const applyPriceFilter = document.getElementById('apply-price-filter');
    const resetPriceFilter = document.getElementById('reset-price-filter');
    
    applyPriceFilter.addEventListener('click', () => {
        minPrice = parseFloat(document.getElementById('price-min').value) || 0;
        maxPrice = parseFloat(document.getElementById('price-max').value) || 1000;
        currentProductPage = 1;
        loadMoreProducts();
    });
    
    resetPriceFilter.addEventListener('click', () => {
        document.getElementById('price-min').value = 0;
        document.getElementById('price-max').value = 1000;
        minPrice = 0;
        maxPrice = 1000;
        currentProductPage = 1;
        loadMoreProducts();
    });
    
    // Configurar el ordenamiento
    const priceSort = document.getElementById('priceSort');
    priceSort.addEventListener('change', () => {
        currentSortValue = priceSort.value;
        currentProductPage = 1;
        loadMoreProducts();
    });
});