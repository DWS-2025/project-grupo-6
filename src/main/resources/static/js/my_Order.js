function updateQuantity(productId, change) {
    const input = document.getElementById(`counter_${productId}`);
    let value = parseInt(input.value) + change;
    if (value < 1) value = 1;
    input.value = value;

    // Show an update indicator
    const form = input.closest('.quantity-form');
    form.classList.add('updating');
    
    // Create FormData with the form data
    const formData = new FormData();
    formData.append('productId', productId);
    formData.append('amount', value);
    
    // Send update to server using FormData instead of JSON
    fetch('/update-amount', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            return response.json(); // Convert response to JSON if successful
        } else {
            throw new Error('Error in server response');
        }
    })
    .then(data => {
        // Update order summary dynamically
        updateOrderSummary(data);
        
        // Remove the visual indicator after a brief pause
        setTimeout(() => {
            form.classList.remove('updating');
        }, 500);
        
        // Update the input value to reflect the change
        const updatedProduct = data.products.find(p => p.id == productId);
        if (updatedProduct) {
            input.value = updatedProduct.amount;
        }
    })
    .catch(error => {
        console.error("Error:", error);
        // No show alert, only remove the visual indicator
        form.classList.remove('updating');
        // Revert to the previous value
        setTimeout(() => {
            window.location.reload(); // Reload the page if there's an error
        }, 300);
    });
}

function updateOrderSummary(data) {
    const orderSummary = document.querySelector('.bg-white ul');
    const totalElement = document.querySelector('.order-total');
    
    // Check that the elements exist
    if (!orderSummary || !totalElement || !data.products) {
        console.error("No elements found to update the summary");
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
        
        // Also update the quantities in the inputs
        data.products.forEach(product => {
            const input = document.getElementById(`counter_${product.id}`);
            if (input) {
                input.value = product.amount;
            }
        });
    } catch (error) {
        console.error("Error updating the summary:", error);
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

// Add event listeners to all quantity inputs to update them when changing
document.addEventListener('DOMContentLoaded', function() {
    const quantityInputs = document.querySelectorAll('.quantity-input');
    
    quantityInputs.forEach(input => {
        // Original value to restore in case of error
        let originalValue = input.value;
        
        // Event for when the user finishes editing the value
        input.addEventListener('change', function() {
            const productId = this.closest('.quantity-form').querySelector('input[name="productId"]').value;
            const value = parseInt(this.value);
            
            if (isNaN(value) || value < 1) {
                this.value = originalValue;
                return;
            }
            
            // Update the quantity with the new value
            updateQuantity(productId, 0);
            originalValue = this.value;
        });
        
        // Prevent negative values
        input.addEventListener('input', function() {
            if (this.value < 1 && this.value !== '') {
                this.value = 1;
            }
        });
    });
});