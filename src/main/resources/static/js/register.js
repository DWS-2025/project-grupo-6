/**
 * Registration form validation
 * Controls the validation of form fields in real-time and on submit
 */

document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const addressInput = document.getElementById('address');
    const phoneInput = document.getElementById('phoneNumber');
    const ageInput = document.getElementById('age');
    
    // Add input event listeners for real-time validation
    const formInputs = [firstNameInput, lastNameInput, emailInput, 
                        passwordInput, addressInput, phoneInput, ageInput];
    
    formInputs.forEach(input => {
        if (input) {
            input.addEventListener('input', function() {
                validateInput(input);
            });
            
            input.addEventListener('blur', function() {
                validateInput(input);
            });
        }
    });
    
    // Age field validation specifically for numeric input
    if (ageInput) {
        ageInput.addEventListener('input', function() {
            // Allow only numbers
            this.value = this.value.replace(/[^0-9]/g, '');
            validateInput(this);
        });
    }
    
    // Form submission handler
    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            let isValid = true;
            
            // Validate all inputs before submission
            formInputs.forEach(input => {
                if (input && !validateInput(input)) {
                    isValid = false;
                }
            });
            
            if (!isValid) {
                event.preventDefault();
                
                // Find the first invalid input and scroll to it
                const firstInvalidInput = document.querySelector('.is-invalid');
                if (firstInvalidInput) {
                    firstInvalidInput.focus();
                    firstInvalidInput.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    });
                }
            }
        });
    }
    
    // Input validation function
    function validateInput(input) {
        if (!input) return true;
        
        const value = input.value.trim();
        const inputId = input.id;
        let isValid = true;
        let errorMessage = '';
        
        // Reset validation state
        input.classList.remove('is-invalid');
        const feedbackElement = input.nextElementSibling.nextElementSibling;
        if (feedbackElement && feedbackElement.classList.contains('invalid-feedback')) {
            feedbackElement.textContent = '';
        }
        
        // Validation rules for each field
        switch (inputId) {
            case 'firstName':
                if (value === '') {
                    isValid = false;
                    errorMessage = 'First name is required';
                } else if (value.length < 2) {
                    isValid = false;
                    errorMessage = 'First name must be at least 2 characters';
                }
                break;
                
            case 'lastName':
                if (value === '') {
                    isValid = false;
                    errorMessage = 'Last name is required';
                } else if (value.length < 2) {
                    isValid = false;
                    errorMessage = 'Last name must be at least 2 characters';
                }
                break;
                
            case 'email':
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (value === '') {
                    isValid = false;
                    errorMessage = 'Email is required';
                } else if (!emailRegex.test(value)) {
                    isValid = false;
                    errorMessage = 'Please enter a valid email address';
                }
                break;
                
            case 'password':
                if (value === '') {
                    isValid = false;
                    errorMessage = 'Password is required';
                } else if (value.length < 8) {
                    isValid = false;
                    errorMessage = 'Password must be at least 8 characters';
                } else if (!/[A-Z]/.test(value)) {
                    isValid = false;
                    errorMessage = 'Password must contain at least one uppercase letter';
                } else if (!/[a-z]/.test(value)) {
                    isValid = false;
                    errorMessage = 'Password must contain at least one lowercase letter';
                } else if (!/[0-9]/.test(value)) {
                    isValid = false;
                    errorMessage = 'Password must contain at least one number';
                }
                break;
                
            case 'address':
                if (value === '') {
                    isValid = false;
                    errorMessage = 'Address is required';
                } else if (value.length < 5) {
                    isValid = false;
                    errorMessage = 'Please enter a valid address';
                }
                break;
                
            case 'phoneNumber':
                const phoneRegex = /^\d{6,9}$/;
                if (value === '') {
                    isValid = false;
                    errorMessage = 'Phone number is required';
                } else if (!phoneRegex.test(value)) {
                    isValid = false;
                    errorMessage = 'Please enter a valid phone number (6-9 digits)';
                }
                break;
                
            case 'age':
                if (value === '') {
                    isValid = false;
                    errorMessage = 'Age is required';
                } else {
                    const age = parseInt(value);
                    if (isNaN(age) || age < 18 || age > 120) {
                        isValid = false;
                        errorMessage = 'You must be between 18 and 120 years old';
                    }
                }
                break;
        }
        
        // Show validation feedback
        if (!isValid) {
            input.classList.add('is-invalid');
            if (feedbackElement) {
                feedbackElement.textContent = errorMessage;
            }
        }
        
        return isValid;
    }
    
    // Configure overlay click to redirect to login
    const overlay = document.querySelector('.overlay');
    if (overlay) {
        overlay.addEventListener('click', function() {
            window.location.href = '/login';
        });
    }
}); 