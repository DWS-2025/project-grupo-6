document.addEventListener('DOMContentLoaded', function() {
    const addToCartForm = document.getElementById('addToCartForm');
    if (addToCartForm) {
        addToCartForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const button = this.querySelector('button');
            const originalText = button.textContent;
            const productStockElement = document.querySelector('.stock-info .value');
            const currentStock = parseInt(productStockElement.textContent);

            // Check if out of stock
            if (currentStock <= 0) {
                button.textContent = 'Out of Stock!';
                button.style.backgroundColor = '#c53030';
                button.disabled = true;

                // Restore button after 1 second
                setTimeout(() => {
                    button.textContent = originalText;
                    button.style.backgroundColor = '';
                    button.disabled = false;
                }, 1000);

                return;
            }

            button.disabled = true;

            try {
                // Check the quantity in the cart
                const productId = document.querySelector('input[name="productId"]').value;
                const cartResponse = await fetch(`/get-cart-quantity?productId=${productId}`);
                const cartData = await cartResponse.json();
                const cartQuantity = cartData.quantity || 0;

                // Check if adding one more exceeds the stock
                if (cartQuantity + 1 > currentStock) {
                    button.textContent = 'Out of Stock!';
                    button.style.backgroundColor = '#c53030';

                    // Restore button after 1 second
                    setTimeout(() => {
                        button.textContent = originalText;
                        button.style.backgroundColor = '';
                        button.disabled = false;
                    }, 1000);

                    return;
                }

                // If there is stock, send the form
                const response = await fetch('/add-to-order', {
                    method: 'POST',
                    body: new FormData(this)
                });
                
                // Try to process the response as JSON, but don't fail if it's not JSON
                let data = {};
                try {
                    data = await response.json();
                } catch (e) {
                    console.log('Response is not JSON, continuing...');
                }

                if (response.ok) {
                    // Change background to green and text to "Added!"
                    button.textContent = 'Added!';
                    button.style.backgroundColor = '#2e7d32';

                    // Restore after 1 second
                    setTimeout(() => {
                        button.textContent = originalText;
                        button.style.backgroundColor = '';
                        button.disabled = false;
                    }, 1000);

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
                button.textContent = 'Error';
                button.style.backgroundColor = '#c53030';

                // Restore button after 2 seconds
                setTimeout(() => {
                    button.textContent = originalText;
                    button.style.backgroundColor = '';
                    button.disabled = false;
                }, 2000);
            }
        });
    }
}); 