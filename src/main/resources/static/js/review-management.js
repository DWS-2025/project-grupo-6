/**
 * Review Management JavaScript
 * Handles review display and interactions
 */

document.addEventListener('DOMContentLoaded', function() {
    // Function to toggle review groups
    window.toggleGroup = function(element) {
        const reviewsList = element.nextElementSibling;
        reviewsList.classList.toggle('expanded');
        
        const icon = element.querySelector('.toggle-icon');
        icon.classList.toggle('expanded');
        
        // Aplicar la altura adecuada según el estado - clave para solucionar el problema
        if (reviewsList.classList.contains('expanded')) {
            reviewsList.style.maxHeight = '70vh';
            reviewsList.style.overflowY = 'auto';
        } else {
            reviewsList.style.maxHeight = '0';
            reviewsList.style.overflowY = 'hidden';
        }
        
        // Card animation
        const card = element.closest('.group-card');
        card.style.transform = 'scale(1.02)';
        setTimeout(() => {
            card.style.transform = '';
        }, 300);
    };

    // Referencias al DOM para el modal de eliminación
    const deleteModalOverlay = document.getElementById('confirmDeleteModal');
    const cancelDeleteButton = document.getElementById('cancelDelete');
    const productNameElement = document.getElementById('productName');
    const reviewRatingElement = document.getElementById('reviewRating');
    const productIdInput = document.getElementById('productIdInput');
    const reviewIdInput = document.getElementById('reviewIdInput');
    const deleteReviewForm = document.getElementById('deleteReviewForm');
    
    // Reemplazar los botones de eliminación actuales con la versión modal
    document.querySelectorAll('.delete-form').forEach(form => {
        const productId = form.querySelector('input[name="productId"]').value;
        const reviewId = form.querySelector('input[name="reviewId"]').value;
        
        // Obtener el nombre del producto - pueden estar en diferentes estructuras según la vista
        let productName = '';
        const reviewItem = form.closest('.review-item');
        const productElement = reviewItem.querySelector('.review-product strong');
        
        if (productElement) {
            productName = productElement.textContent;
        } else {
            // Buscar en la estructura de la tarjeta del producto si estamos en vista de productos
            const groupCard = form.closest('.group-card');
            if (groupCard) {
                const titleElement = groupCard.querySelector('.group-title span');
                if (titleElement) {
                    productName = titleElement.textContent.trim();
                }
            }
        }
        
        const rating = reviewItem.querySelector('.review-rating').getAttribute('data-rating');
        
        // Crear un nuevo botón que abre el modal
        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.className = 'open-delete-modal';
        deleteButton.innerHTML = '<i class="fas fa-trash-alt"></i> Eliminar';
        deleteButton.setAttribute('data-product-id', productId);
        deleteButton.setAttribute('data-review-id', reviewId);
        deleteButton.setAttribute('data-product-name', productName);
        deleteButton.setAttribute('data-rating', rating);
        
        // Reemplazar el formulario con el nuevo botón
        form.parentNode.replaceChild(deleteButton, form);
    });
    
    // Agregar listeners a los nuevos botones
    document.querySelectorAll('.open-delete-modal').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            const reviewId = this.getAttribute('data-review-id');
            const productName = this.getAttribute('data-product-name');
            const rating = this.getAttribute('data-rating');
            
            // Mostrar estrellas para el rating
            let starsHTML = '';
            for (let i = 1; i <= 5; i++) {
                if (i <= rating) {
                    starsHTML += '<i class="fas fa-star"></i>';
                } else {
                    starsHTML += '<i class="far fa-star"></i>';
                }
            }
            
            // Actualizar el modal
            productNameElement.textContent = productName;
            reviewRatingElement.innerHTML = starsHTML;
            productIdInput.value = productId;
            reviewIdInput.value = reviewId;
            
            // Abrir el modal
            openModal(deleteModalOverlay);
        });
    });
    
    // Cerrar modal cuando se hace clic en Cancelar
    if (cancelDeleteButton) {
        cancelDeleteButton.addEventListener('click', function() {
            closeModal(deleteModalOverlay);
        });
    }
    
    // Cerrar modal cuando se hace clic en el fondo
    if (deleteModalOverlay) {
        deleteModalOverlay.addEventListener('click', function(e) {
            if (e.target === deleteModalOverlay) {
                closeModal(deleteModalOverlay);
            }
        });
    }
    
    // Agregar listener de tecla Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && deleteModalOverlay && deleteModalOverlay.classList.contains('active')) {
            closeModal(deleteModalOverlay);
        }
    });
    
    // Funciones auxiliares para el modal
    function openModal(modal) {
        if (!modal) return;
        
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Animación de entrada para el modal
        const modalContent = modal.querySelector('.confirmation-modal');
        if (modalContent) {
            modalContent.classList.add('animate-in');
            
            setTimeout(() => {
                modalContent.classList.remove('animate-in');
            }, 500);
        }
    }
    
    function closeModal(modal) {
        if (!modal) return;
        
        const modalContent = modal.querySelector('.confirmation-modal');
        if (modalContent) {
            modalContent.classList.add('animate-out');
            
            setTimeout(() => {
                modal.classList.remove('active');
                document.body.style.overflow = '';
                modalContent.classList.remove('animate-out');
            }, 300);
        } else {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    // Toggle between users and products view
    const viewButtons = document.querySelectorAll('.view-toggle-btn');
    
    viewButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove active class from all buttons
            viewButtons.forEach(b => b.classList.remove('active'));
            
            // Add active class to clicked button
            this.classList.add('active');
            
            // Toggle views with fade effect
            const viewToShow = this.getAttribute('data-view');
            const usersView = document.querySelector('.users-view');
            const productsView = document.querySelector('.products-view');
            
            if (viewToShow === 'users') {
                if (!usersView.classList.contains('active')) {
                    productsView.style.opacity = '0';
                    setTimeout(() => {
                        usersView.classList.add('active');
                        productsView.classList.remove('active');
                        setTimeout(() => {
                            usersView.style.opacity = '1';
                        }, 50);
                    }, 300);
                }
            } else {
                if (!productsView.classList.contains('active')) {
                    usersView.style.opacity = '0';
                    setTimeout(() => {
                        productsView.classList.add('active');
                        usersView.classList.remove('active');
                        setTimeout(() => {
                            productsView.style.opacity = '1';
                        }, 50);
                    }, 300);
                }
            }
        });
    });
    
    // Generate star ratings with star containers for better hover effects
    document.querySelectorAll('.review-rating').forEach(function(element) {
        const rating = parseInt(element.getAttribute('data-rating'));
        let starsHTML = '';
        
        for (let i = 1; i <= 5; i++) {
            starsHTML += `<span class="star-container">`;
            if (i <= rating) {
                starsHTML += `<i class="fas fa-star"></i>`;
            } else {
                starsHTML += `<i class="far fa-star"></i>`;
            }
            starsHTML += `</span>`;
        }
        
        element.innerHTML = starsHTML;
        
        // Add pulse animation on hover
        element.addEventListener('mouseenter', function() {
            const stars = this.querySelectorAll('.star-container');
            stars.forEach((star, index) => {
                setTimeout(() => {
                    star.style.transform = 'scale(1.2)';
                    setTimeout(() => {
                        star.style.transform = 'scale(1)';
                    }, 150);
                }, index * 100);
            });
        });
    });
    
    // Set initials for user avatars
    document.querySelectorAll('[data-initial]').forEach(function(avatar) {
        const initial = avatar.getAttribute('data-initial');
        if (initial && initial.length > 0) {
            avatar.textContent = initial[0].toUpperCase();
        } else {
            avatar.textContent = '?';
        }
    });
    
    // Count reviews for each user/product
    document.querySelectorAll('.review-count').forEach(function(countContainer) {
        const counters = countContainer.querySelectorAll('.review-counter');
        const countDisplay = countContainer.querySelector('.review-count-number');
        if (countDisplay) {
            countDisplay.textContent = counters.length;
        }
    });
    
    // Staggered card loading animations
    document.querySelectorAll('.group-card').forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 100 + (index * 120));
    });
    
    // Animate section header on load
    const sectionHeader = document.querySelector('.section-header');
    if (sectionHeader) {
        sectionHeader.style.opacity = '0';
        sectionHeader.style.transform = 'translateY(-20px)';
        
        setTimeout(() => {
            sectionHeader.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
            sectionHeader.style.opacity = '1';
            sectionHeader.style.transform = 'translateY(0)';
        }, 100);
    }
    
    // Animate toggle button container
    const toggleContainer = document.querySelector('.view-toggle-container');
    if (toggleContainer) {
        toggleContainer.style.opacity = '0';
        toggleContainer.style.transform = 'translateY(-10px)';
        
        setTimeout(() => {
            toggleContainer.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            toggleContainer.style.opacity = '1';
            toggleContainer.style.transform = 'translateY(0)';
        }, 400);
    }
}); 