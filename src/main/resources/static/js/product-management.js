document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const modalOverlay = document.getElementById('confirmDeleteModal');
    const cancelButton = document.getElementById('cancelDelete');
    const productNameElement = document.getElementById('productName');
    const productIdInput = document.getElementById('productIdInput');
    const deleteForm = document.getElementById('deleteProductForm');
    const productsGrid = document.getElementById('productsGrid');
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    
    // Variables para paginación
    let currentPage = 1;
    let hasMore = true;
    const pageSize = 20;
    
    // Debug info
    console.log('Product management page initialized');
    
    // Función para cargar productos
    function loadProducts(page) {
        // Mostrar un spinner en el botón de cargar más
        loadMoreBtn.innerHTML = '<span class="loading-spinner"></span> Loading...';
        loadMoreBtn.classList.add('loading');
        
        fetch(`/api/products/management?page=${page}&size=${pageSize}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar variables de paginación
                currentPage = data.currentPage;
                hasMore = data.hasMore;
                
                // Si no hay más productos, ocultar el botón
                if (!hasMore) {
                    loadMoreBtn.style.display = 'none';
                } else {
                    loadMoreBtn.innerHTML = '<i class="fas fa-plus-circle mr-2"></i> Load More Products';
                    loadMoreBtn.classList.remove('loading');
                }
                
                // Si es la primera página y no hay productos, mostrar mensaje vacío
                if (page === 1 && data.products.length === 0) {
                    productsGrid.innerHTML = `
                        <div class="empty-state">
                            <i class="fas fa-box-open"></i>
                            <p>No products available</p>
                        </div>
                    `;
                    return;
                }
                
                // Crear elementos para cada producto
                const productElements = data.products.map(product => {
                    return `
                        <div class="product-item new-product">  
                            <div class="product-image">
                                <img src="/image/${product.id}" alt="${product.name}" class="product-thumbnail">
                            </div>
                            <div class="product-header">
                                <h3 class="product-name">${product.name}</h3>
                            </div>
                            
                            <div class="product-actions">
                                <a href="/edit-product/${product.id}" class="edit-action" title="Edit Product">
                                    <i class="fas fa-edit"></i>
                                </a>
                                <button type="button" class="delete-action" 
                                        data-id="${product.id}" 
                                        data-name="${product.name}" title="Delete Product">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </div>
                        </div>
                    `;
                }).join('');
                
                // Si es la primera página, reemplazar el contenido
                if (page === 1) {
                    productsGrid.innerHTML = productElements;
                    
                    // Configurar animaciones para todos los productos (primera carga)
                    const allProducts = document.querySelectorAll('.product-item');
                    allProducts.forEach((product, index) => {
                        // Quitar la clase 'new-product' para mantener consistencia
                        product.classList.remove('new-product');
                        
                        // Calcular el delay basado en el índice (0.05s incremento)
                        const delay = Math.min(0.05 * (index + 1), 1.5); // máximo 1.5s de retraso
                        product.style.animationDelay = `${delay}s`;
                    });
                } else {
                    // Si no es la primera página, añadir al final sin alterar los productos existentes
                    
                    // Primero, eliminamos la animación de todos los productos existentes
                    // para que no vuelvan a animarse
                    const existingProducts = document.querySelectorAll('.product-item');
                    existingProducts.forEach(product => {
                        product.style.opacity = '1';
                        product.style.animation = 'none';
                    });
                    
                    // Ahora añadimos los nuevos productos
                    const tempContainer = document.createElement('div');
                    tempContainer.innerHTML = productElements;
                    
                    // Obtenemos todos los nuevos elementos para añadirlos al DOM
                    const newProductElements = Array.from(tempContainer.children);
                    newProductElements.forEach(product => {
                        productsGrid.appendChild(product);
                    });
                    
                    // Seleccionamos solo los productos recién añadidos usando la clase 'new-product'
                    const newProducts = document.querySelectorAll('.new-product');
                    
                    // Dar tiempo al DOM para renderizar los elementos antes de aplicar animaciones
                    setTimeout(() => {
                        newProducts.forEach((product, index) => {
                            // Quitar la clase 'new-product' para no volver a seleccionarlos
                            product.classList.remove('new-product');
                            
                            // Calcular el delay basado en el índice (0.05s incremento)
                            const delay = Math.min(0.05 * (index + 1), 1.0); // máximo 1.0s de retraso
                            product.style.animationDelay = `${delay}s`;
                        });
                    }, 10);
                }
                
                // Adjuntar event listeners a los nuevos botones de eliminar
                setupDeleteButtons();
            })
            .catch(error => {
                console.error('Error loading products:', error);
                loadMoreBtn.innerHTML = '<i class="fas fa-exclamation-circle mr-2"></i> Error loading products';
                loadMoreBtn.classList.remove('loading');
            });
    }
    
    // Configurar botones de eliminar
    function setupDeleteButtons() {
        const deleteButtons = document.querySelectorAll('.delete-action');
        console.log('Delete buttons found:', deleteButtons.length);
        
        deleteButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const productName = this.getAttribute('data-name');
                const productId = this.getAttribute('data-id');
                
                console.log('Delete clicked for product:', productName, 'ID:', productId);
                
                productNameElement.textContent = productName;
                productIdInput.value = productId;
                
                modalOverlay.classList.add('active');
                document.body.style.overflow = 'hidden'; // Prevent scrolling
            });
        });
    }
    
    // Cargar productos iniciales
    loadProducts(currentPage);
    
    // Event listener para el botón "Load More"
    loadMoreBtn.addEventListener('click', function() {
        if (hasMore) {
            currentPage++;
            loadProducts(currentPage);
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
        e.preventDefault(); // Prevenir el envío normal del formulario
        
        const productId = productIdInput.value;
        if (!productId) {
            console.error('No product ID found in form');
            alert('Error: No product ID found');
            return;
        }
        
        console.log('Submitting delete form for product ID:', productId);
        
        // Usar la API en lugar del formulario normal
        fetch(`/api/products/${productId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Cerrar el modal
            modalOverlay.classList.remove('active');
            document.body.style.overflow = '';
            
            // Recargar los productos (primera página)
            currentPage = 1;
            loadProducts(currentPage);
            
            // Mostrar mensaje de éxito
            const successMessage = document.createElement('div');
            successMessage.classList.add('success-message');
            successMessage.innerHTML = '<i class="fas fa-check-circle"></i> Product deleted successfully';
            document.querySelector('main').prepend(successMessage);
            
            // Eliminar mensaje después de 3 segundos
            setTimeout(() => {
                successMessage.remove();
            }, 3000);
        })
        .catch(error => {
            console.error('Error deleting product:', error);
            alert('Error deleting product. Please try again.');
            
            // Cerrar el modal
            modalOverlay.classList.remove('active');
            document.body.style.overflow = '';
        });
    });
}); 