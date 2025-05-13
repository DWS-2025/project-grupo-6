document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const modalOverlay = document.getElementById('confirmDeleteModal');
    const deleteButtons = document.querySelectorAll('.open-modal');
    const cancelButton = document.getElementById('cancelDelete');
    const userNameElement = document.getElementById('userName');
    const emailInput = document.getElementById('userEmailInput');
    
    // Open modal when delete button is clicked
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const userName = this.getAttribute('data-name');
            const userEmail = this.getAttribute('data-email');
            
            userNameElement.textContent = userName;
            emailInput.value = userEmail;
            
            modalOverlay.classList.add('active');
            document.body.style.overflow = 'hidden'; // Prevent scrolling
        });
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
}); 