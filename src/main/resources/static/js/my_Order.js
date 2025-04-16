function updateQuantity(productId, change) {
    const input = document.getElementById(`counter_${productId}`);
    let value = parseInt(input.value) + change;
    if (value < 1) value = 1;
    input.value = value;

    // Mostrar un indicador visual de actualización
    const form = input.closest('.quantity-form');
    form.classList.add('updating');
    
    // Crear FormData con los datos del formulario
    const formData = new FormData();
    formData.append('productId', productId);
    formData.append('amount', value);
    
    // Send update to server usando FormData en lugar de JSON
    fetch('/update-amount', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            return response.json(); // Convertir respuesta a JSON si es exitosa
        } else {
            throw new Error('Error en la respuesta del servidor');
        }
    })
    .then(data => {
        // Update order summary dynamically
        updateOrderSummary(data);
        
        // Quitar el indicador visual después de una breve pausa
        setTimeout(() => {
            form.classList.remove('updating');
        }, 500);
        
        // Actualizar el valor en el input para reflejar el cambio
        const updatedProduct = data.products.find(p => p.id == productId);
        if (updatedProduct) {
            input.value = updatedProduct.amount;
        }
    })
    .catch(error => {
        console.error("Error:", error);
        // No mostrar alerta, solo quitar el indicador visual
        form.classList.remove('updating');
        // Revertir al valor anterior
        setTimeout(() => {
            window.location.reload(); // Recargar la página si hay un error
        }, 300);
    });
}

function updateOrderSummary(data) {
    const orderSummary = document.querySelector('.bg-white ul');
    const totalElement = document.querySelector('.order-total');
    
    // Comprobar que los elementos existen
    if (!orderSummary || !totalElement || !data.products) {
        console.error("No se encontraron elementos necesarios para actualizar el resumen");
        return;
    }

    try {
        // Clear current summary
        orderSummary.innerHTML = '';

        // Add updated products
        data.products.forEach(product => {
            const li = document.createElement('li');
            li.className = 'flex justify-between items-center pb-2 border-b border-[#CBD3E6]/50';
            li.innerHTML = `
                <span class="text-[#567C8D]">${product.name} <span class="text-[#CBD3E6]">x${product.amount}</span></span>
                <span class="font-medium text-[#294156]">$${product.price}</span>
            `;
            orderSummary.appendChild(li);
        });

        // Update order total
        totalElement.innerHTML = `
            <div class="flex justify-between items-center">
                <span class="text-[#567C8D]"><i class="fas fa-money-bill-wave mr-2"></i>Total</span>
                <span class="font-bold text-xl text-[#294156]">$${data.total}</span>
            </div>
        `;
        
        // Actualizar también las cantidades en los inputs
        data.products.forEach(product => {
            const input = document.getElementById(`counter_${product.id}`);
            if (input) {
                input.value = product.amount;
            }
        });
    } catch (error) {
        console.error("Error al actualizar el resumen:", error);
    }
}

function removeFromCart(productId) {
    const formData = new FormData();
    formData.append('productId', productId);
    
    fetch('/remove-from-order', {
        method: 'POST',
        body: formData
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        }
    });
}

function deleteOrder() {
    fetch('/delete-order', {
        method: 'POST',
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        }
    });
}

// Agregar event listeners a todos los inputs de cantidad para actualizarlos al cambiar
document.addEventListener('DOMContentLoaded', function() {
    const quantityInputs = document.querySelectorAll('.quantity-input');
    
    quantityInputs.forEach(input => {
        // Valor original para restaurar en caso de error
        let originalValue = input.value;
        
        // Evento para cuando el usuario termina de editar el valor
        input.addEventListener('change', function() {
            const productId = this.closest('.quantity-form').querySelector('input[name="productId"]').value;
            const value = parseInt(this.value);
            
            if (isNaN(value) || value < 1) {
                this.value = originalValue;
                return;
            }
            
            // Actualizar la cantidad con el nuevo valor
            updateQuantity(productId, 0);
            originalValue = this.value;
        });
        
        // Prevenir valores negativos
        input.addEventListener('input', function() {
            if (this.value < 1 && this.value !== '') {
                this.value = 1;
            }
        });
    });
});