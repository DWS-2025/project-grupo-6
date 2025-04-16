// Función para añadir el comportamiento de hover y click a los botones del carrito
function enhanceCartButtons() {
  // Obtener todos los formularios de añadir al carrito
  const addToCartForms = document.querySelectorAll('.add-to-cart-form, [class*="add-to-cart"]');
  
  addToCartForms.forEach(form => {
    const button = form.querySelector('button');
    if (!button) return;
    
    // Guardar el texto original del botón para restaurarlo después
    const originalText = button.innerHTML;
    const originalBgColor = button.style.backgroundColor || '';
    
    form.addEventListener('submit', function(event) {
      // Añadir clase para cambiar el estilo
      button.classList.add('added');
      
      // Guardar el estilo original si no lo hemos hecho
      if (!button.dataset.originalBackground) {
        button.dataset.originalBackground = window.getComputedStyle(button).backgroundColor;
      }
      
      // Establecer explícitamente el fondo verde
      button.style.backgroundColor = '#2e7d32';
      
      // Cambiar texto a "Added!"
      const icon = button.querySelector('svg') ? button.querySelector('svg').outerHTML : '';
      button.innerHTML = icon + ' Added!';
      
      // Restaurar después de 1 segundo
      setTimeout(() => {
        button.classList.remove('added');
        button.innerHTML = originalText;
        button.style.backgroundColor = originalBgColor;
      }, 1000);
    });
    
    // Añadir manejadores de hover para mejorar la experiencia
    button.addEventListener('mouseover', function() {
      // Solo cambiar a verde si no está en estado "added"
      if (!this.classList.contains('added')) {
        this.style.backgroundColor = '#2e7d32';
      }
    });
    
    button.addEventListener('mouseout', function() {
      // Solo restaurar si no está en estado "added"
      if (!this.classList.contains('added')) {
        this.style.backgroundColor = '';
      }
    });
  });
  
  // Obtener todos los botones de eliminar producto
  const deleteForms = document.querySelectorAll('form:not(.add-to-cart-form):not(.remove-form), form[action="/delete-product"]');
  
  deleteForms.forEach(form => {
    const button = form.querySelector('button[type="submit"]');
    if (!button) return;
    
    // Comprobar si es un botón de eliminar basado en el texto o el formulario
    const isDeleteButton = 
      button.textContent.includes('Delete') || 
      form.action.includes('/delete-product') ||
      button.classList.contains('bg-[#567C8D]') ||
      (button.parentElement && button.parentElement.classList.contains('product-buttons'));
    
    if (!isDeleteButton) return;
    
    // Añadir efecto de hover mediante JavaScript para mayor precisión cuando está en modo oscuro
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

// Ejecutar cuando el DOM esté cargado
document.addEventListener('DOMContentLoaded', function() {
  // Inicializar estado del modo oscuro
  initDarkMode();
  
  // Mejorar botones del carrito
  enhanceCartButtons();
  
  // Configurar el cambio de tema al hacer clic en el switch
  const darkModeToggle = document.getElementById('dark-mode-toggle');
  
  if (darkModeToggle) {
    darkModeToggle.addEventListener('change', function() {
      toggleDarkMode();
      
      // Re-aplicar mejoras a los botones después de cambiar el tema
      setTimeout(enhanceCartButtons, 100);
    });
  }
  
  // Observar cambios en el DOM para productos cargados dinámicamente
  const observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
      if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
        // Verificar si se han añadido nuevos productos
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

// Función para inicializar el modo oscuro basado en preferencias guardadas
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

// Función para alternar el modo oscuro
function toggleDarkMode() {
  const darkModeEnabled = document.body.classList.toggle('dark-mode');
  
  if (darkModeEnabled) {
    localStorage.setItem('darkMode', 'enabled');
  } else {
    localStorage.setItem('darkMode', 'disabled');
  }
}

// Comprobar si el modo oscuro está activado en localStorage
let darkModeEnabled = localStorage.getItem('darkMode') === 'enabled';

// Función para activar el modo oscuro
function enableDarkMode() {
  document.body.classList.add('dark-mode');
  localStorage.setItem('darkMode', 'enabled');
  darkModeEnabled = true;
  
  // Aplicar estilos específicos a los botones de contraseña
  const passwordButtons = document.querySelectorAll('.password-container button, .password-toggle-btn');
  passwordButtons.forEach(btn => {
    // Estilos base para modo oscuro
    btn.style.backgroundColor = "#294156";
    btn.style.color = "white";
    btn.style.transition = "all 0.3s ease"; // Asegurar transición suave
    
    // Limpiar listeners previos para evitar duplicados
    const clonedBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(clonedBtn, btn);
    const currentBtn = clonedBtn; // Usar el botón clonado

    // Agregar event listeners para hover en el botón clonado
    currentBtn.addEventListener('mouseover', function() {
      this.style.backgroundColor = "#4080bf"; // Azul claro/turquesa
      this.style.transform = "translateY(-2px)";
      this.style.boxShadow = "0 4px 8px rgba(0, 0, 0, 0.4)"; // Sombra negra más visible
    });
    
    currentBtn.addEventListener('mouseout', function() {
      // Restaurar estilos base modo oscuro
      this.style.backgroundColor = "#294156"; 
      this.style.transform = "none";
      this.style.boxShadow = "none";
    });
    
    // Reasignar el evento onclick si existe (importante para funcionalidad)
    if (btn.onclick) {
        currentBtn.onclick = btn.onclick;
    }
  });
}

// Función para desactivar el modo oscuro
function disableDarkMode() {
  document.body.classList.remove('dark-mode');
  localStorage.setItem('darkMode', 'disabled'); // Usar 'disabled' para claridad
  darkModeEnabled = false;
  
  // Restaurar estilos originales de los botones
  const passwordButtons = document.querySelectorAll('.password-container button, .password-toggle-btn');
  passwordButtons.forEach(btn => {
    // Limpiar estilos inline aplicados por JS
    btn.style.backgroundColor = "";
    btn.style.color = "";
    btn.style.transform = "";
    btn.style.boxShadow = "";
    btn.style.transition = "";
    
    // Limpiar listeners previos (importante)
    const clonedBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(clonedBtn, btn);
    // Reasignar el evento onclick si existe
    if (btn.onclick) {
        clonedBtn.onclick = btn.onclick;
    }
  });
}

// Aplicar el modo oscuro al cargar la página si está activado
if (darkModeEnabled) {
  enableDarkMode();
}

// Agregar evento al interruptor de modo oscuro cuando esté presente en la página
document.addEventListener('DOMContentLoaded', function() {
  const darkModeToggle = document.getElementById('darkModeToggle');
  
  if (darkModeToggle) {
    // Establecer el estado del interruptor según la preferencia guardada
    darkModeToggle.checked = darkModeEnabled;
    
    // Agregar evento al interruptor
    darkModeToggle.addEventListener('change', function() {
      if (darkModeEnabled) {
        disableDarkMode();
      } else {
        enableDarkMode();
      }
      
      // Forzar la actualización de los estilos de los botones de contraseña
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