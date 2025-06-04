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

document.querySelector('form').addEventListener('submit', function(e) {
    e.preventDefault();
    const firstName = document.querySelector('input[name="firstName"]').value;
    const lastName = document.querySelector('input[name="lastName"]').value;
    const email = document.querySelector('input[name="email"]').value;
    const phone = document.querySelector('input[name="phone"]').value;
    const age = document.querySelector('input[name="age"]').value;
    const address = document.querySelector('input[name="address"]').value;
    
    // Buscar el campo oculto de contraseña que puede haber sido agregado por el modal
    const hiddenPassword = document.getElementById('hiddenPassword');
    const hiddenConfirmPassword = document.getElementById('hiddenConfirmPassword');
    
    let password = '';
    let confirmPassword = '';
    
    if (hiddenPassword && hiddenPassword.value) {
        password = hiddenPassword.value;
    }
    
    if (hiddenConfirmPassword && hiddenConfirmPassword.value) {
        confirmPassword = hiddenConfirmPassword.value;
    }

    // Validate required fields
    if (!firstName || !lastName || !email) {
        showNotification('Name, Surname and Email are required fields.', 'error');
        return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showNotification('Please enter a valid email address.', 'error');
        return;
    }

    // Validate phone number (optional field)
    if (phone) {
        const phoneRegex = /^[0-9+\-\s()]*$/;
        if (!phoneRegex.test(phone)) {
            showNotification('Please enter a valid phone number.', 'error');
            return;
        }
    }

    // Validate age (optional field)
    if (age) {
        const ageNum = parseInt(age);
        if (isNaN(ageNum) || ageNum < 18 || ageNum > 120) {
            showNotification('Please enter a valid age between 18 and 120.', 'error');
            return;
        }
    }

    // Password validation
    if (password) {
        if (password.length < 8 || !/[A-Z]/.test(password) || !/[a-z]/.test(password) || !/\d/.test(password)) {
            showNotification('The password must be at least 8 characters long, contain uppercase, lowercase and numbers.', 'error');
            return;
        }

        if (password !== confirmPassword) {
            showNotification('The new passwords do not match. Please verify that they are the same.', 'error');
            return;
        }
    }

    // Create form data
    const formData = new FormData();
    formData.append('firstName', firstName);
    formData.append('lastName', lastName);
    formData.append('email', email);
    formData.append('phone', phone);
    formData.append('age', age);
    formData.append('address', address);
    
    if (password) {
        formData.append('password', password);
        formData.append('confirmPassword', confirmPassword);
    }

    // Submit form using fetch
    fetch('/update-profile', {
        method: 'POST',
        body: formData,
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || 'Profile update failed');
            });
        }
        return response.text();
    })
    .then(() => {
        showNotification('Profile updated successfully!', 'success');
        setTimeout(() => {
            window.location.reload();
        }, 3000);
    })
    .catch(error => {
        showNotification('Error updating profile: ' + error.message, 'error');
    });
});

// Código para eliminar órdenes
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll('.delete-order-button').forEach(button => {
        button.addEventListener('click', function () {
            const orderCard = this.closest('.order-card');
            const orderId = orderCard.getAttribute('data-order-id');
            
            fetch(`/delete-order/${orderId}`, { 
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    orderCard.remove();
                    // Si no hay más órdenes, mostrar el mensaje de "No orders yet"
                    if (document.querySelectorAll('.order-card').length === 0) {
                        const noOrdersMessage = document.createElement('p');
                        noOrdersMessage.className = 'no-reviews';
                        noOrdersMessage.innerHTML = '<i class="fas fa-info-circle"></i> No orders yet.';
                        document.querySelector('.options-menu').appendChild(noOrdersMessage);
                    }
                } else {
                    showNotification('Error al eliminar la orden. Por favor, inténtalo de nuevo.', 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error al eliminar la orden. Por favor, inténtalo de nuevo.', 'error');
            });
        });
    });
});