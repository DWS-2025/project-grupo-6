// Function to add hover and click behavior to cart buttons
function enhanceCartButtons() {
  // Get all add to cart forms
  const addToCartForms = document.querySelectorAll('.add-to-cart-form, [class*="add-to-cart"]');
  
  addToCartForms.forEach(form => {
    const button = form.querySelector('button');
    if (!button) return;
    
    // Save the original button text to restore it later
    const originalText = button.innerHTML;
    const originalBgColor = button.style.backgroundColor || '';
    
    form.addEventListener('submit', function(event) {
      // Add class to change the style
      button.classList.add('added');
      
      // Save the original style if we haven't done it yet
      if (!button.dataset.originalBackground) {
        button.dataset.originalBackground = window.getComputedStyle(button).backgroundColor;
      }
      
      // Set explicitly the green background
      button.style.backgroundColor = '#2e7d32';
      
      // Change text to "Added!"
      const icon = button.querySelector('svg') ? button.querySelector('svg').outerHTML : '';
      button.innerHTML = icon + ' Added!';
      
      // Restore after 1 second
      setTimeout(() => {
        button.classList.remove('added');
        button.innerHTML = originalText;
        button.style.backgroundColor = originalBgColor;
      }, 1000);
    });
    
    // Add hover handlers to improve the experience
    button.addEventListener('mouseover', function() {
      // Only change to green if not in "added" state
      if (!this.classList.contains('added')) {
        this.style.backgroundColor = '#2e7d32';
      }
    });
    
    button.addEventListener('mouseout', function() {
      // Only restore if not in "added" state
      if (!this.classList.contains('added')) {
        this.style.backgroundColor = '';
      }
    });
  });
  
  // Get all product delete buttons
  const deleteForms = document.querySelectorAll('form:not(.add-to-cart-form):not(.remove-form), form[action="/delete-product"]');
  
  deleteForms.forEach(form => {
    const button = form.querySelector('button[type="submit"]');
    if (!button) return;
    
    // Check if it's a delete button based on text or form
    const isDeleteButton = 
      button.textContent.includes('Delete') || 
      form.action.includes('/delete-product') ||
      button.classList.contains('bg-[#567C8D]') ||
      (button.parentElement && button.parentElement.classList.contains('product-buttons'));
    
    if (!isDeleteButton) return;
    
    // Add hover effect using JavaScript for greater precision when in dark mode
    button.addEventListener('mouseover', function() {
      if (document.body.classList.contains('dark-mode')) {
        this.style.backgroundColor = '#c53030';
        this.style.transform = 'translateY(-2px)';
        this.style.boxShadow = '0 4px 8px rgba(197, 48, 48, 0.4)';
      }
    });
    
    button.addEventListener('mouseout', function() {
      if (document.body.classList.contains('dark-mode')) {
        this.style.backgroundColor = '';
        this.style.transform = '';
        this.style.boxShadow = '';
      }
    });
  });
}

// Execute when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
  // Initialize dark mode state
  initDarkMode();
  
  // Enhance cart buttons
  enhanceCartButtons();
  
  // Configure theme change when clicking the switch
  const darkModeToggle = document.getElementById('dark-mode-toggle');
  
  if (darkModeToggle) {
    darkModeToggle.addEventListener('change', function() {
      toggleDarkMode();
      
      // Re-apply button enhancements after changing the theme
      setTimeout(enhanceCartButtons, 100);
    });
  }
  
  // Observe DOM changes for dynamically loaded products
  const observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
      if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
        // Verify if new products have been added
        const hasNewProducts = Array.from(mutation.addedNodes).some(node => 
          node.nodeType === 1 && (
            node.classList?.contains('product-card') || 
            node.querySelector?.('.product-card')
          )
        );
        
        if (hasNewProducts) {
          enhanceCartButtons();
        }
      }
    });
  });
  
  const productsGrid = document.getElementById('productsGrid');
  if (productsGrid) {
    observer.observe(productsGrid, { childList: true, subtree: true });
  }
});

// Function to initialize dark mode based on saved preferences
function initDarkMode() {
  const darkModeEnabled = localStorage.getItem('darkMode') === 'enabled';
  const darkModeToggle = document.getElementById('dark-mode-toggle');
  
  if (darkModeEnabled) {
    document.body.classList.add('dark-mode');
    if (darkModeToggle) darkModeToggle.checked = true;
  } else {
    document.body.classList.remove('dark-mode');
    if (darkModeToggle) darkModeToggle.checked = false;
  }
}

// Function to toggle dark mode
function toggleDarkMode() {
  const darkModeEnabled = document.body.classList.toggle('dark-mode');
  
  if (darkModeEnabled) {
    localStorage.setItem('darkMode', 'enabled');
  } else {
    localStorage.setItem('darkMode', 'disabled');
  }
}

// Check if dark mode is enabled in localStorage
let darkModeEnabled = localStorage.getItem('darkMode') === 'enabled';

// Function to activate dark mode
function enableDarkMode() {
  document.body.classList.add('dark-mode');
  localStorage.setItem('darkMode', 'enabled');
  darkModeEnabled = true;
  
  // Apply specific styles to password buttons
  const passwordButtons = document.querySelectorAll('.password-container button, .password-toggle-btn');
  passwordButtons.forEach(btn => {
    // Base styles for dark mode
    btn.style.backgroundColor = "#294156";
    btn.style.color = "white";
    btn.style.transition = "all 0.3s ease"; // Ensure smooth transition
    
    // Clean previous listeners to avoid duplicates
    const clonedBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(clonedBtn, btn);
    const currentBtn = clonedBtn; // Use the cloned button

    // Add event listeners for hover on the cloned button
    currentBtn.addEventListener('mouseover', function() {
      this.style.backgroundColor = "#4080bf"; // Light blue/turquoise
      this.style.transform = "translateY(-2px)";
      this.style.boxShadow = "0 4px 8px rgba(0, 0, 0, 0.4)"; // More visible black shadow
    });
    
    currentBtn.addEventListener('mouseout', function() {
      // Restore base dark mode styles
      this.style.backgroundColor = "#294156"; 
      this.style.transform = "none";
      this.style.boxShadow = "none";
    });
    
    // Reassign the onclick event if it exists (important for functionality)
    if (btn.onclick) {
        currentBtn.onclick = btn.onclick;
    }
  });
}

// Function to deactivate dark mode
function disableDarkMode() {
  document.body.classList.remove('dark-mode');
  localStorage.setItem('darkMode', 'disabled'); // Use 'disabled' for clarity
  darkModeEnabled = false;
  
  // Restore original button styles
  const passwordButtons = document.querySelectorAll('.password-container button, .password-toggle-btn');
  passwordButtons.forEach(btn => {
    // Clean inline styles applied by JS
    btn.style.backgroundColor = "";
    btn.style.color = "";
    btn.style.transform = "";
    btn.style.boxShadow = "";
    btn.style.transition = "";
    
    // Clean previous listeners (important)
    const clonedBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(clonedBtn, btn);
    // Reassign the onclick event if it exists
    if (btn.onclick) {
        clonedBtn.onclick = btn.onclick;
    }
  });
}

// Apply dark mode when loading the page if it's enabled
if (darkModeEnabled) {
  enableDarkMode();
}

// Add event to the dark mode switch when present on the page
document.addEventListener('DOMContentLoaded', function() {
  const darkModeToggle = document.getElementById('darkModeToggle');
  
  if (darkModeToggle) {
    // Set the switch state according to the saved preference
    darkModeToggle.checked = darkModeEnabled;
    
    // Add event to the switch
    darkModeToggle.addEventListener('change', function() {
      if (darkModeEnabled) {
        disableDarkMode();
      } else {
        enableDarkMode();
      }
      
      // Force the update of password button styles
      setTimeout(() => {
        const passwordButtons = document.querySelectorAll('.password-container button, .password-toggle-btn');
        if (darkModeEnabled) {
          passwordButtons.forEach(btn => {
            btn.style.backgroundColor = "#294156";
            btn.style.color = "white";
          });
        } else {
          passwordButtons.forEach(btn => {
            btn.style.backgroundColor = "";
            btn.style.color = "";
          });
        }
      }, 50);
    });
  }
}); 