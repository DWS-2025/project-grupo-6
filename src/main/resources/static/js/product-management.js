document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const modalOverlay = document.getElementById('confirmDeleteModal');
    const cancelButton = document.getElementById('cancelDelete');
    const productNameElement = document.getElementById('productName');
    const productIdInput = document.getElementById('productIdInput');
    const deleteForm = document.getElementById('deleteProductForm');
    const productsGrid = document.getElementById('productsGrid');
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    
    // Pagination variables
    let currentPage = 1;
    let hasMore = true;
    const pageSize = 20;
    
    // Función para mostrar notificaciones elegantes
    function showNotification(message, type = 'success') {
        // Crear elemento de notificación
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        
        // Añadir icono según el tipo
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
        
        // Añadir al DOM
        document.body.appendChild(notification);
        
        // Mostrar con animación
        setTimeout(() => {
            notification.classList.add('show');
        }, 10);
        
        // Configurar botón para cerrar
        notification.querySelector('.close-notification').addEventListener('click', () => {
            notification.classList.remove('show');
            setTimeout(() => {
                notification.remove();
            }, 300);
        });
        
        // Auto-cerrar después de 4 segundos
        setTimeout(() => {
            if (document.body.contains(notification)) {
                notification.classList.remove('show');
                setTimeout(() => {
                    if (document.body.contains(notification)) {
                        notification.remove();
                    }
                }, 300);
            }
        }, 4000);
    }
    
    // Configurar los enlaces de productos
    function setupProductLinks() {
        const productLinks = document.querySelectorAll('.product-link, .product-name-link');
        
        productLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                // Solo para efectos visuales - añadir una pequeña animación al hacer clic
                e.currentTarget.classList.add('clicked');
                
                // Extraer el nombre del producto desde el atributo title
                const title = e.currentTarget.getAttribute('title');
                const productName = title ? title.replace('View ', '') : 'product';
                
                // Mostrar notificación (opcional, podemos quitar esto si no lo deseas)
                setTimeout(() => {
                    showNotification(`Viewing details of "${productName}"`, 'info');
                }, 300);
            });
        });
    }
    
    // Función para cargar productos con animaciones mejoradas
    function loadProducts(page) {
        // Mostrar un efecto de carga en el botón
        loadMoreBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Loading Products';
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
                // Actualizar variables de paginación
                currentPage = data.currentPage;
                hasMore = data.hasMore;
                
                // Actualizar el botón de cargar más
                loadMoreBtn.disabled = false;
                if (!hasMore) {
                    loadMoreBtn.style.display = 'none';
                } else {
                    loadMoreBtn.innerHTML = '<i class="fas fa-plus-circle mr-2"></i> Load More Products';
                    loadMoreBtn.classList.remove('loading');
                }
                
                // Si es la primera página y no hay productos, mostrar estado vacío
                if (page === 1 && data.products.length === 0) {
                    productsGrid.innerHTML = `
                        <div class="empty-state">
                            <i class="fas fa-box-open"></i>
                            <p>No products available</p>
                            <a href="/create-product" class="add-product-btn mt-6">
                                <i class="fas fa-plus"></i> Add First Product
                            </a>
                        </div>
                    `;
                    return;
                }
                
                // Crear elementos para cada producto con clases para animaciones
                const productElements = data.products.map(product => {
                    return `
                        <div class="product-item">  
                            <div class="product-image">
                                <a href="/product-details?id=${product.id}" class="product-link" title="View ${product.name}">
                                    <img src="/image/${product.id}" alt="${product.name}" class="product-thumbnail">
                                </a>
                            </div>
                            <div class="product-header">
                                <a href="/product-details?id=${product.id}" class="product-name-link" title="View ${product.name}">
                                    <h3 class="product-name">${product.name}</h3>
                                </a>
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
                } else {
                    // Para páginas adicionales, añadir al final
                    // Primero, eliminamos las animaciones de los productos existentes
                    const existingProducts = document.querySelectorAll('.product-item');
                    existingProducts.forEach(product => {
                        product.style.opacity = '1';
                        product.style.transform = 'translateY(0)';
                    });
                    
                    // Crear un fragmento para añadir los nuevos productos
                    const fragment = document.createDocumentFragment();
                    const tempContainer = document.createElement('div');
                    tempContainer.innerHTML = productElements;
                    
                    // Añadir cada nuevo elemento al fragmento
                    while (tempContainer.firstChild) {
                        fragment.appendChild(tempContainer.firstChild);
                    }
                    
                    // Añadir todos los nuevos productos al grid
                    productsGrid.appendChild(fragment);
                }
                
                // Configurar animaciones para todos los productos
                const allProducts = document.querySelectorAll('.product-item');
                const startIndex = page === 1 ? 0 : allProducts.length - data.products.length;
                
                allProducts.forEach((product, index) => {
                    // Solo aplicar animación a los productos recién cargados
                    if (index >= startIndex) {
                        // Calcular el delay basado en la posición dentro del grupo actual
                        const positionInCurrentBatch = index - startIndex;
                        const delay = Math.min(0.05 * positionInCurrentBatch, 0.95);
                        product.style.animationDelay = `${delay}s`;
                    }
                });
                
                // Adjuntar event listeners a los botones de eliminar
                setupDeleteButtons();
                
                // Configurar enlaces de productos
                setupProductLinks();
                
                // Animación suave al scroll a nuevos elementos si se están cargando más
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
                loadMoreBtn.innerHTML = '<i class="fas fa-exclamation-circle mr-2"></i> Try Again';
                loadMoreBtn.classList.remove('loading');
                loadMoreBtn.disabled = false;
                
                showNotification('Error loading products. Please try again.', 'error');
            });
    }
    
    // Configurar botones de eliminar
    function setupDeleteButtons() {
        const deleteButtons = document.querySelectorAll('.delete-action');
        
        deleteButtons.forEach(button => {
            // Eliminar listeners anteriores para evitar duplicados
            const newButton = button.cloneNode(true);
            button.parentNode.replaceChild(newButton, button);
            
            // Añadir nuevo event listener
            newButton.addEventListener('click', function(e) {
                e.preventDefault();
                const productName = this.getAttribute('data-name');
                const productId = this.getAttribute('data-id');
                
                productNameElement.textContent = productName;
                productIdInput.value = productId;
                
                // Abrir modal con animación
                modalOverlay.classList.add('active');
                document.body.style.overflow = 'hidden'; // Prevent scrolling
            });
        });
    }
    
    // Cargar productos iniciales
    loadProducts(currentPage);
    
    // Event listener para el botón "Load More"
    loadMoreBtn.addEventListener('click', function() {
        if (hasMore && !this.disabled) {
            // Mostrar efecto de carga
            this.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Loading Products';
            this.disabled = true;
            
            // Añadir pequeña pausa para mejor experiencia visual
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
        e.preventDefault(); // Prevenir el envío normal del formulario
        
        const productId = productIdInput.value;
        const productName = productNameElement.textContent;
        
        if (!productId) {
            showNotification('Error: No product ID found', 'error');
            return;
        }
        
        // Actualizar el botón de eliminar para mostrar el estado de carga
        const submitButton = this.querySelector('button[type="submit"]');
        const originalButtonText = submitButton.innerHTML;
        submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        submitButton.disabled = true;
        
        //Delete product request
        fetch(`/delete-product`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
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
            // Cerrar el modal
            modalOverlay.classList.remove('active');
            document.body.style.overflow = '';
            
            // Mostrar notificación de éxito
            showNotification(`Product "${productName}" deleted successfully`, 'success');
            
            // Añadir una pequeña pausa para mejor UX
            setTimeout(() => {
                // Recargar los productos (primera página)
                currentPage = 1;
                loadProducts(currentPage);
            }, 300);
        })
        .catch(error => {
            console.error('Error deleting product:', error);
            showNotification('Error deleting product. Please try again.', 'error');
            
            // Restaurar el botón
            submitButton.innerHTML = originalButtonText;
            submitButton.disabled = false;
        })
        .finally(() => {
            // Asegurarse de que el modal se cierre
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
    
    // Agregar estilos para las notificaciones
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