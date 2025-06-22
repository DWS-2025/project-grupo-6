document.addEventListener('DOMContentLoaded', function() {
    function setupCharacterCounter(inputId, counterId, maxLength) {
        const input = document.getElementById(inputId);
        const counter = document.getElementById(counterId);

        if (input && counter) {
            // Set initial value on page load in case of pre-filled forms (e.g., edit page)
            const initialLength = input.value.length;
            counter.textContent = `${initialLength}/${maxLength}`;
            if (initialLength >= maxLength) {
                counter.classList.add('limit-reached');
            }

            // Update on input
            input.addEventListener('input', function() {
                const currentLength = this.value.length;
                counter.textContent = `${currentLength}/${maxLength}`;

                if (currentLength >= maxLength) {
                    counter.classList.add('limit-reached');
                } else {
                    counter.classList.remove('limit-reached');
                }
            });
        }
    }

    setupCharacterCounter('name', 'name-counter', 40);
    setupCharacterCounter('description', 'description-counter', 300);
}); 