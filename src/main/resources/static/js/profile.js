function togglePasswordSection() {
    const passwordSection = document.getElementById("passwordSection");
    passwordSection.style.display = passwordSection.style.display === "none" ? "block" : "none";
}

function togglePasswordVisibility() {
    const passwordInput = document.getElementById("passwordInput");
    const toggleButton = passwordInput.nextElementSibling;

    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        toggleButton.textContent = "Hide";
    } else {
        passwordInput.type = "password";
        toggleButton.textContent = "Show";
    }
}

function toggleConfirmPasswordVisibility() {
    const confirmPasswordInput = document.getElementById("confirmPasswordInput");
    const toggleButton = confirmPasswordInput.nextElementSibling;

    if (confirmPasswordInput.type === "password") {
        confirmPasswordInput.type = "text";
        toggleButton.textContent = "Hide";
    } else {
        confirmPasswordInput.type = "password";
        toggleButton.textContent = "Show";
    }
}

function toggleCurrentPasswordVisibility() {
    const currentPasswordInput = document.getElementById("currentPasswordInput");
    const toggleButton = currentPasswordInput.nextElementSibling;

    if (currentPasswordInput.type === "password") {
        currentPasswordInput.type = "text";
        toggleButton.textContent = "Hide";
    } else {
        currentPasswordInput.type = "password";
        toggleButton.textContent = "Show";
    }
}

// Function to show custom notifications
function showNotification(message, type) {
    const notificationContainer = document.getElementById('notification-container');
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    // Add close button
    const closeBtn = document.createElement('span');
    closeBtn.className = 'notification-close';
    closeBtn.innerHTML = '&times;';
    closeBtn.onclick = function() {
        notification.remove();
    };
    
    notification.appendChild(closeBtn);
    notificationContainer.appendChild(notification);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        notification.classList.add('fade-out');
        setTimeout(() => {
            notification.remove();
        }, 500);
    }, 5000);
}

// Sanitize rich text content
document.addEventListener('DOMContentLoaded', function() {
    // Sanitize review content
    document.querySelectorAll('.rich-text-content').forEach(container => {
        const content = container.innerHTML;
        container.innerHTML = DOMPurify.sanitize(content, {
            ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 's', 'h1', 'h2', 'h3', 'ol', 'ul', 'li', 'a', 'img', 'blockquote', 'span'],
            ALLOWED_ATTR: ['href', 'src', 'alt', 'style', 'target', 'class'],
            ALLOW_DATA_ATTR: false,
            ADD_ATTR: ['target'],
            FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover'],
            FORCE_HTTPS: true,
        });
    });
    
    
    
});

// Get the token and header name from the meta
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// Code to delete orders
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll('.delete-order-button').forEach(button => {
        button.addEventListener('click', function () {
            const orderCard = this.closest('.order-card');
            const orderId = orderCard.getAttribute('data-order-id');
            
            fetch(`/delete-order/${orderId}`, { 
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    orderCard.remove();
                    // If there are no more orders, show the message "No orders yet"
                    if (document.querySelectorAll('.order-card').length === 0) {
                        const noOrdersMessage = document.createElement('p');
                        noOrdersMessage.className = 'no-reviews';
                        noOrdersMessage.innerHTML = '<i class="fas fa-info-circle"></i> No orders yet.';
                        document.querySelector('.options-menu').appendChild(noOrdersMessage);
                    }
                } else {
                    showNotification('Error deleting the order. Please try again.', 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error deleting the order. Please try again.', 'error');
            });
        });
    });
});