document.addEventListener('DOMContentLoaded', function() {
    // Check for errors in the URL to display messages
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        const errorType = urlParams.get('error');
        let errorMessage = 'An error occurred while processing your review.';
        
        if (errorType === 'character_limit') {
            errorMessage = 'The review exceeds the maximum of 400 characters allowed.';
        }
        
        // Show error message
        showToast(errorMessage, true);
    }
    
    // Format the average rating to one decimal place
    const ratingElement = document.querySelector('.rating-info .value');
    const ratingText = ratingElement.textContent;
    const ratingValue = parseFloat(ratingText);
    if (!isNaN(ratingValue)) {
        ratingElement.textContent = ratingValue.toFixed(1) + ' ⭐';
    }
    
    // Initialize Quill editor
    const quill = new Quill('#editor-container', {
        modules: {
            toolbar: [
                [{ 'header': [1, 2, 3, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                [{ 'color': [] }, { 'background': [] }],
                ['link', 'image'],
                ['clean']
            ]
        },
        placeholder: 'Write your review here...',
        theme: 'snow'
    });
    
    // Create an object to control the editor lock state
    const editorState = {
        locked: false,
        editingMode: false,
        enable: function() {
            this.locked = false;
            quill.enable();
            document.querySelector('.ql-toolbar').classList.remove('disabled');
            document.querySelector('.ql-container').classList.remove('disabled');
        },
        disable: function() {
            this.locked = true;
            quill.disable();
            document.querySelector('.ql-toolbar').classList.add('disabled');
            document.querySelector('.ql-container').classList.add('disabled');
            showToast(`You have reached the ${MAX_CHARS} character limit. Delete text to continue.`, true);
        },
        enableEditMode: function() {
            this.editingMode = true;
            // Enable editing temporarily, without considering the limit
            this.enable();
            // Change background color to indicate edit mode
            document.querySelector('.ql-container').classList.add('editing-mode');
            showToast("Edit mode activated. Delete text and then click outside the editor.", false);
        },
        disableEditMode: function() {
            this.editingMode = false;
            document.querySelector('.ql-container').classList.remove('editing-mode');
            
            // Check if we're still under the limit now
            const currentCharCount = countCharacters();
            if (currentCharCount > MAX_CHARS) {
                this.disable();
                unlockButton.classList.add('visible');
            }
        }
    };
    
    // Add button to unlock editing
    const editingControls = document.createElement('div');
    editingControls.className = 'editing-controls';
    const unlockButton = document.createElement('button');
    unlockButton.type = 'button';
    unlockButton.className = 'unlock-btn';
    unlockButton.textContent = 'Edit review (delete text)';
    unlockButton.addEventListener('click', function() {
        editorState.enableEditMode();
        this.classList.remove('visible');
    });
    editingControls.appendChild(unlockButton);
    document.querySelector('.ql-container').after(editingControls);
    
    // Detector for exiting edit mode when clicking outside the editor
    document.addEventListener('click', function(e) {
        if (editorState.editingMode && !quill.root.contains(e.target) && e.target !== unlockButton) {
            editorState.disableEditMode();
        }
    });
    
    // Detector for exiting edit mode when pressing Escape
    document.addEventListener('keydown', function(e) {
        if (editorState.editingMode && e.key === 'Escape') {
            editorState.disableEditMode();
        }
    });
    
    // Character counter functionality
    const MAX_CHARS = 400;
    const characterCounter = document.getElementById('character-counter');
    
    function countCharacters() {
        // Get plain text without HTML tags to count characters
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = quill.root.innerHTML;
        
        // Clean unnecessary whitespace and normalize
        const textContent = tempDiv.textContent
            .replace(/\s+/g, ' ')  // Replace multiple spaces with a single one
            .trim();
        
        const charCount = textContent.length;
        
        // Update visual counter
        characterCounter.textContent = `${charCount}/${MAX_CHARS} characters`;
        
        // Mark visually when approaching or exceeding the limit
        if (charCount > MAX_CHARS) {
            characterCounter.classList.add('limit-reached');
            
            // Block the editor if it's not already blocked and we're not in edit mode
            if (!editorState.locked && !editorState.editingMode) {
                editorState.disable();
                unlockButton.classList.add('visible');
            }
        } else if (charCount >= MAX_CHARS * 0.9) {
            // More than 90% of the limit - visual warning
            characterCounter.classList.add('limit-warning');
            characterCounter.classList.remove('limit-reached');
            
            // If it was blocked and we're not in edit mode, unblock
            if (editorState.locked && !editorState.editingMode) {
                editorState.enable();
                unlockButton.classList.remove('visible');
            }
        } else {
            characterCounter.classList.remove('limit-reached', 'limit-warning');
            
            // If it was blocked and we're not in edit mode, unblock
            if (editorState.locked && !editorState.editingMode) {
                editorState.enable();
                unlockButton.classList.remove('visible');
            }
        }
        
        // For debugging
        console.log(`Character count: ${charCount}/${MAX_CHARS}`);
        
        return charCount;
    }
    
    // Auto-save draft and character count on text change
    let autoSaveTimeout;
    let previousContent = quill.root.innerHTML;
    let isRestoringContent = false;
    
    quill.on('text-change', function(delta, oldDelta, source) {
        // Avoid infinite loops during content restoration
        if (isRestoringContent) return;
        
        // 1. Update character counter
        const charCount = countCharacters();
        
        // 2. If we're in edit mode, allow changes without restrictions
        if (editorState.editingMode) {
            return;
        }
        
        // 3. Handle character limit when not in edit mode
        if (charCount > MAX_CHARS && source === 'user') {
            isRestoringContent = true;
            
            // Get the previous HTML version that we know is within the limit
            const selection = quill.getSelection();
            
            // History: get the previous content (if available)
            const oldContent = localStorage.getItem(`quill_content_backup_${productId}`);
            
            if (oldContent) {
                // Restore previous content
                quill.setText(''); // Clear everything first
                quill.clipboard.dangerouslyPasteHTML(0, oldContent);
                quill.setSelection(selection ? Math.min(selection.index, MAX_CHARS) : 0);
            } else {
                // No backup, try to truncate the current content as best as possible
                const truncatedHTML = truncateHTML(quill.root.innerHTML, MAX_CHARS);
                quill.setText(''); // Clear everything first
                quill.clipboard.dangerouslyPasteHTML(0, truncatedHTML);
            }
            
            // Block editor after restoring content
            setTimeout(() => {
                editorState.disable();
                unlockButton.classList.add('visible');
                isRestoringContent = false;
            }, 10);
        } else if (charCount <= MAX_CHARS) {
            // 4. Save backup copy of valid content
            localStorage.setItem(`quill_content_backup_${productId}`, quill.root.innerHTML);
            
            // 5. Configure auto-save draft
            clearTimeout(autoSaveTimeout);
            autoSaveTimeout = setTimeout(function() {
                const currentContent = quill.root.innerHTML;
                if (currentContent !== previousContent && currentContent !== '<p><br></p>') {
                    previousContent = currentContent;
                    const timestamp = new Date().getTime();
                    localStorage.setItem(draftKey, JSON.stringify({
                        content: currentContent,
                        timestamp: timestamp
                    }));
                    updateDraftInfo(new Date(timestamp));
                }
            }, 30000); // 30 seconds
        }
    });
    
    // Function to truncate HTML trying to preserve structure
    function truncateHTML(html, maxChars) {
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        
        truncateNode(tempDiv, maxChars);
        return tempDiv.innerHTML;
    }
    
    // Function for truncating nodes recursively
    function truncateNode(node, charsLeft) {
        if (charsLeft <= 0) return 0;
        
        if (node.nodeType === Node.TEXT_NODE) {
            if (node.textContent.length > charsLeft) {
                node.textContent = node.textContent.substr(0, charsLeft);
                return 0;
            }
            return charsLeft - node.textContent.length;
        }
        
        if (node.nodeType === Node.ELEMENT_NODE) {
            let remaining = charsLeft;
            const childNodes = Array.from(node.childNodes);
            
            for (let i = 0; i < childNodes.length; i++) {
                remaining = truncateNode(childNodes[i], remaining);
                if (remaining <= 0) {
                    // Remove all remaining nodes
                    for (let j = i + 1; j < childNodes.length; j++) {
                        node.removeChild(childNodes[j]);
                    }
                    break;
                }
            }
            
            return remaining;
        }
        
        return charsLeft;
    }
    
    // Draft functionality
    const productId = document.querySelector('input[name="productId"]').value;
    const draftKey = `review_draft_${productId}`;
    const draftInfoEl = document.getElementById('draftInfo');
    const saveDraftBtn = document.getElementById('saveDraftBtn');
    const clearEditorBtn = document.getElementById('clearEditorBtn');
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    // Load saved draft if exists
    const savedDraft = localStorage.getItem(draftKey);
    if (savedDraft) {
        const draftData = JSON.parse(savedDraft);
        quill.root.innerHTML = draftData.content;
        updateDraftInfo(new Date(draftData.timestamp));
        
        // Check if the loaded content exceeds the limit
        setTimeout(() => {
            const charCount = countCharacters();
            if (charCount > MAX_CHARS) {
                editorState.disable();
                unlockButton.classList.add('visible');
            }
        }, 100);
    } else {
        // Initialize character counter
        countCharacters();
    }
    
    // Save draft
    saveDraftBtn.addEventListener('click', function() {
        const content = quill.root.innerHTML;
        if (content && content !== '<p><br></p>') {
            const timestamp = new Date().getTime();
            localStorage.setItem(draftKey, JSON.stringify({
                content: content,
                timestamp: timestamp
            }));
            updateDraftInfo(new Date(timestamp));
            showToast('Draft saved successfully', false);
        } else {
            showToast('Nothing to save', true);
        }
    });
    
    // Clear editor
    clearEditorBtn.addEventListener('click', function() {
        quill.setText('');
        localStorage.removeItem(draftKey);
        draftInfoEl.querySelector('span').textContent = 'No draft saved';
        showToast('Editor cleared', false);
    });
    
    // Update draft info
    function updateDraftInfo(date) {
        const formattedDate = new Intl.DateTimeFormat('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
        
        draftInfoEl.querySelector('span').textContent = `Draft saved at ${formattedDate}`;
    }
    
    // Show toast notification
    function showToast(message, isError) {
        toastMessage.textContent = message;
        if (isError) {
            toast.classList.add('error');
        } else {
            toast.classList.remove('error');
        }
        
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }
    
    // Form submission handling
    document.getElementById('reviewForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        // If we're in edit mode, exit first
        if (editorState.editingMode) {
            editorState.disableEditMode();
        }
        
        // Get editor content and sanitize it
        const editorContent = quill.root.innerHTML;
        const sanitizedContent = DOMPurify.sanitize(editorContent, {
            ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 's', 'h1', 'h2', 'h3', 'ol', 'ul', 'li', 'a', 'img', 'blockquote', 'span'],
            ALLOWED_ATTR: ['href', 'src', 'alt', 'style', 'target', 'class'],
            ALLOW_DATA_ATTR: false,
            ADD_ATTR: ['target'],
            FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover'],
            FORCE_HTTPS: true,
        });
        
        // Check if the content is empty (only contains whitespace or HTML tags without content)
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = sanitizedContent;
        const textContent = tempDiv.textContent.replace(/\s+/g, ' ').trim();
        
        if (textContent === '') {
            showToast('Please write a review before submitting', true);
            return;
        }
        
        // Check character count properly (using text content, not HTML)
        const charCount = textContent.length;
        if (charCount > MAX_CHARS) {
            showToast(`Review exceeds maximum of ${MAX_CHARS} characters`, true);
            
            // Restore edit mode to correct
            editorState.enableEditMode();
            return;
        }
        
        // Check if rating is selected
        const ratingSelect = document.querySelector('select[name="rating"]');
        if (!ratingSelect.value) {
            showToast('Please select a rating', true);
            return;
        }
        
        // Set the sanitized content to the hidden input
        document.getElementById('hiddenComment').value = sanitizedContent;
        
        // Clear draft after successful submission
        localStorage.removeItem(draftKey);
        localStorage.removeItem(`quill_content_backup_${productId}`);
        
        // Submit the form
        this.submit();
    });
    
    // Handle text pasting to prevent exceeding the limit
    quill.root.addEventListener('paste', function(e) {
        // Check if we're close to the limit before pasting
        const currentCharCount = countCharacters();
        
        if (currentCharCount >= MAX_CHARS) {
            // We're already at the limit, prevent pasting completely
            e.preventDefault();
            e.stopPropagation();
            showToast(`Cannot paste text: you have reached the ${MAX_CHARS} character limit`, true);
            return false;
        }
        
        // If the clipboard contains plain text, we can check its length
        const clipboardData = e.clipboardData || window.clipboardData;
        if (clipboardData && clipboardData.getData) {
            const pastedText = clipboardData.getData('text/plain');
            
            // Calculate if pasting would exceed the limit
            if (pastedText && currentCharCount + pastedText.length > MAX_CHARS) {
                e.preventDefault();
                e.stopPropagation();
                
                // Calculate how many characters we can paste
                const remainingChars = MAX_CHARS - currentCharCount;
                if (remainingChars > 0) {
                    // Paste only what fits within the limit
                    const truncatedPaste = pastedText.substring(0, remainingChars);
                    
                    // Insert the truncated text manually
                    const selection = quill.getSelection();
                    if (selection) {
                        quill.insertText(selection.index, truncatedPaste);
                    }
                    
                    showToast(`The pasted text has been truncated to fit the ${MAX_CHARS} character limit`, true);
                } else {
                    showToast(`Cannot paste: you would exceed the ${MAX_CHARS} character limit`, true);
                }
                
                return false;
            }
        }
    });
    
    // Sanitize existing review content
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