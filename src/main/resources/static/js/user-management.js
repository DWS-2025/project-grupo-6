document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const deleteModalOverlay = document.getElementById('confirmDeleteModal');
    const editModalOverlay = document.getElementById('editUserModal');
    const deleteButtons = document.querySelectorAll('.open-modal');
    const editButtons = document.querySelectorAll('.open-edit-modal');
    const cancelDeleteButton = document.getElementById('cancelDelete');
    const cancelEditButton = document.getElementById('cancelEdit');
    const userNameElement = document.getElementById('userName');
    const emailInput = document.getElementById('userEmailInput');
    const originalEmailInput = document.getElementById('originalEmailInput');
    const editFirstNameInput = document.getElementById('editFirstName');
    const editLastNameInput = document.getElementById('editLastName');
    const editEmailInput = document.getElementById('editEmail');
    const editAddressInput = document.getElementById('editAddress');
    const editPhoneInput = document.getElementById('editPhone');
    const editAgeInput = document.getElementById('editAge');
    const editPasswordInput = document.getElementById('editPassword');
    const saveChangesButton = document.getElementById('saveChangesBtn');
    const passwordValidationMessage = document.getElementById('passwordValidationMessage');
    const userCards = document.querySelectorAll('.user-card');
    
    // Password validation variables
    let passwordIsValid = true; // Default true because password is optional

    // Add animation effects to user cards on initial load
    setTimeout(() => {
        animateUserCards();
    }, 100);

    // Add hover effects to info items
    addHoverEffectsToInfoItems();
    
    // Open delete modal when delete button is clicked
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const userName = this.getAttribute('data-name');
            const userEmail = this.getAttribute('data-email');
            
            userNameElement.textContent = userName;
            emailInput.value = userEmail;
            
            openModal(deleteModalOverlay);
        });
    });
    
    // Open edit modal when edit button is clicked
    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            const userEmail = this.getAttribute('data-email');
            const firstName = this.getAttribute('data-firstname');
            const lastName = this.getAttribute('data-lastname');
            const address = this.getAttribute('data-address');
            const phone = this.getAttribute('data-phone');
            const age = this.getAttribute('data-age');
            
            // Populate form fields
            originalEmailInput.value = userEmail;
            editFirstNameInput.value = firstName;
            editLastNameInput.value = lastName;
            editEmailInput.value = userEmail;
            editAddressInput.value = address;
            editPhoneInput.value = phone;
            editAgeInput.value = age;
            editPasswordInput.value = ''; // Reset password field
            
            // Reset validation
            passwordIsValid = true;
            hideValidationMessage();
            
            openModal(editModalOverlay);
        });
    });
    
    // Password validation event
    editPasswordInput.addEventListener('input', validatePassword);
    
    // Validate password function
    function validatePassword() {
        const password = editPasswordInput.value.trim();
        
        // If password is empty, it's valid (not changing password)
        if (password === '') {
            passwordIsValid = true;
            hideValidationMessage();
            return;
        }
        
        const lengthValid = password.length >= 8;
        const hasUppercase = /[A-Z]/.test(password);
        const hasLowercase = /[a-z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        
        passwordIsValid = lengthValid && hasUppercase && hasLowercase && hasNumber;
        
        if (!passwordIsValid) {
            let message = 'La contraseña debe cumplir los siguientes requisitos:';
            if (!lengthValid) message += '<br>- Mínimo 8 caracteres';
            if (!hasUppercase) message += '<br>- Al menos una letra mayúscula';
            if (!hasLowercase) message += '<br>- Al menos una letra minúscula';
            if (!hasNumber) message += '<br>- Al menos un número';
            
            showValidationMessage(message, 'error');
        } else {
            showValidationMessage('La contraseña cumple con todos los requisitos', 'success');
        }

        // Enable or disable submit button
        saveChangesButton.disabled = !passwordIsValid;
        saveChangesButton.style.opacity = passwordIsValid ? '1' : '0.6';
        saveChangesButton.style.cursor = passwordIsValid ? 'pointer' : 'not-allowed';
    }
    
    // Show validation message with animation
    function showValidationMessage(message, type) {
        passwordValidationMessage.innerHTML = message;
        passwordValidationMessage.className = 'validation-message ' + type;
        
        // Use height animation
        if (passwordValidationMessage.style.display === 'none') {
            passwordValidationMessage.style.display = 'block';
            passwordValidationMessage.style.maxHeight = '0';
            passwordValidationMessage.style.opacity = '0';
            
            setTimeout(() => {
                passwordValidationMessage.style.maxHeight = '200px';
                passwordValidationMessage.style.opacity = '1';
            }, 10);
        }
    }
    
    // Hide validation message with animation
    function hideValidationMessage() {
        if (passwordValidationMessage.style.display !== 'none') {
            passwordValidationMessage.style.maxHeight = '0';
            passwordValidationMessage.style.opacity = '0';
            
            setTimeout(() => {
                passwordValidationMessage.style.display = 'none';
            }, 300);
        }
    }
    
    // Form submission validation
    document.getElementById('editUserForm').addEventListener('submit', function(event) {
        // Validate password before submit
        validatePassword();
        
        if (!passwordIsValid) {
            event.preventDefault();
            showValidationMessage('Por favor, corrige los errores de la contraseña antes de guardar', 'error');
            
            // Shake the submit button to indicate error
            saveChangesButton.classList.add('shake-animation');
            setTimeout(() => {
                saveChangesButton.classList.remove('shake-animation');
            }, 820);
        } else {
            // Add loading indicator to button
            saveChangesButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
            saveChangesButton.disabled = true;
        }
    });
    
    // Close delete modal when cancel button is clicked
    cancelDeleteButton.addEventListener('click', function() {
        closeModal(deleteModalOverlay);
    });
    
    // Close edit modal when cancel button is clicked
    cancelEditButton.addEventListener('click', function() {
        closeModal(editModalOverlay);
    });
    
    // Close delete modal when background is clicked
    deleteModalOverlay.addEventListener('click', function(e) {
        if (e.target === deleteModalOverlay) {
            closeModal(deleteModalOverlay);
        }
    });
    
    // Close edit modal when background is clicked
    editModalOverlay.addEventListener('click', function(e) {
        if (e.target === editModalOverlay) {
            closeModal(editModalOverlay);
        }
    });

    // Add escape key listener for modals
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (deleteModalOverlay.classList.contains('active')) {
                closeModal(deleteModalOverlay);
            }
            if (editModalOverlay.classList.contains('active')) {
                closeModal(editModalOverlay);
            }
        }
    });

    // Helper functions
    function openModal(modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Add entrance animation to modal
        const modalContent = modal.querySelector('.confirmation-modal');
        modalContent.classList.add('animate-in');
        
        setTimeout(() => {
            modalContent.classList.remove('animate-in');
        }, 500);
    }
    
    function closeModal(modal) {
        const modalContent = modal.querySelector('.confirmation-modal');
        modalContent.classList.add('animate-out');
        
        setTimeout(() => {
            modal.classList.remove('active');
            document.body.style.overflow = '';
            modalContent.classList.remove('animate-out');
        }, 300);
    }
    
    function animateUserCards() {
        userCards.forEach((card, index) => {
            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, index * 100);
        });
    }
    
    function addHoverEffectsToInfoItems() {
        const infoItems = document.querySelectorAll('.info-item');
        
        infoItems.forEach(item => {
            item.addEventListener('mouseenter', function() {
                const icon = this.querySelector('i');
                icon.classList.add('bounce-effect');
                
                setTimeout(() => {
                    icon.classList.remove('bounce-effect');
                }, 1000);
            });
        });
    }

    // Add CSS for new animations
    const style = document.createElement('style');
    style.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
            20%, 40%, 60%, 80% { transform: translateX(5px); }
        }
        
        .shake-animation {
            animation: shake 0.8s cubic-bezier(.36,.07,.19,.97) both;
        }
        
        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
            40% { transform: translateY(-8px); }
            60% { transform: translateY(-4px); }
        }
        
        .bounce-effect {
            animation: bounce 1s ease;
        }
        
        .animate-in {
            animation: zoomIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }
        
        .animate-out {
            animation: zoomOut 0.3s ease forwards;
        }
        
        @keyframes zoomIn {
            from { transform: scale(0.8); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }
        
        @keyframes zoomOut {
            from { transform: scale(1); opacity: 1; }
            to { transform: scale(0.8); opacity: 0; }
        }
        
        .validation-message {
            transition: max-height 0.3s ease, opacity 0.3s ease;
            overflow: hidden;
        }
    `;
    document.head.appendChild(style);
}); 