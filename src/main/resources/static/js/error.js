document.addEventListener('DOMContentLoaded', function() {
    let seconds = 10;
    const countdownEl = document.querySelector('.redirect-countdown');
    
    const countdown = setInterval(function() {
        seconds--;
        countdownEl.textContent = seconds;
        
        if (seconds <= 0) {
            clearInterval(countdown);
            window.location.href = '/products';
        }
    }, 1000);
    
    // Stop the counter if the user interacts with the buttons
    document.querySelectorAll('.button-container a').forEach(button => {
        button.addEventListener('click', function() {
            clearInterval(countdown);
        });
    });
});