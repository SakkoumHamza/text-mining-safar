// JavaScript for Document Search Interface

$(document).ready(function() {
    
    // Auto-focus on search input when page loads
    $('#query').focus();
    
    // Search form submission with loading state
    $('form').on('submit', function() {
        var submitBtn = $(this).find('button[type="submit"]');
        var originalText = submitBtn.html();
        
        // Show loading state
        submitBtn.html('<i class="fas fa-spinner fa-spin me-2"></i>جاري البحث...');
        submitBtn.prop('disabled', true);
        
        // Re-enable after 5 seconds as fallback
        setTimeout(function() {
            submitBtn.html(originalText);
            submitBtn.prop('disabled', false);
        }, 5000);
    });
    
    // Example query buttons
    $('.example-query-btn').on('click', function(e) {
        e.preventDefault();
        var query = $(this).data('query');
        $('#query').val(query);
        $('form').submit();
    });
    
    // Real-time search suggestions (basic implementation)
    var suggestions = [
        'بغداد عاصمة الخلافة العباسية',
        'الإمبراطورية العثمانية والفتح',
        'العلوم والفلسفة الإسلامية',
        'دمشق والأمويين',
        'الأندلس والمسجد الكبير',
        'الحروب والجيوش',
        'السياسة والحكم',
        'التجارة والاقتصاد',
        'الثقافة والحضارة'
    ];
    
    // Simple autocomplete
    $('#query').on('input', function() {
        var input = $(this).val().toLowerCase();
        var matches = suggestions.filter(function(suggestion) {
            return suggestion.toLowerCase().includes(input);
        });
        
        // You can implement a dropdown here if needed
        // For now, we'll just show suggestions in console for development
        if (input.length > 2 && matches.length > 0) {
            console.log('Suggestions:', matches.slice(0, 3));
        }
    });
    
    // Keyboard shortcuts
    $(document).on('keydown', function(e) {
        // Ctrl/Cmd + K to focus search
        if ((e.ctrlKey || e.metaKey) && e.keyCode === 75) {
            e.preventDefault();
            $('#query').focus().select();
        }
        
        // Escape to clear search
        if (e.keyCode === 27) {
            $('#query').val('').focus();
        }
    });
    
    // Smooth scroll to results
    if ($('.result-card').length > 0) {
        $('html, body').animate({
            scrollTop: $('.result-card').first().offset().top - 100
        }, 500);
    }
    
    // Copy query functionality
    $('.copy-query-btn').on('click', function() {
        var query = $(this).data('query');
        navigator.clipboard.writeText(query).then(function() {
            // Show toast or feedback
            showToast('تم نسخ الاستعلام');
        });
    });
    
    // Share results functionality
    $('.share-results-btn').on('click', function() {
        var url = window.location.href;
        navigator.clipboard.writeText(url).then(function() {
            showToast('تم نسخ رابط النتائج');
        });
    });
    
    // Toast notification function
    function showToast(message) {
        // Create toast element
        var toast = $('<div class="toast-notification">' + message + '</div>');
        toast.css({
            'position': 'fixed',
            'top': '20px',
            'right': '20px',
            'background': '#28a745',
            'color': 'white',
            'padding': '10px 20px',
            'border-radius': '5px',
            'z-index': 9999,
            'opacity': 0
        });
        
        $('body').append(toast);
        
        // Animate in
        toast.animate({opacity: 1}, 300);
        
        // Remove after 3 seconds
        setTimeout(function() {
            toast.animate({opacity: 0}, 300, function() {
                toast.remove();
            });
        }, 3000);
    }
    
    // Advanced search toggle
    $('#advanced-toggle').on('click', function() {
        $('#advanced-options').slideToggle();
    });
    
    // Result cards hover effects
    $('.result-card').hover(
        function() {
            $(this).addClass('shadow-lg');
        },
        function() {
            $(this).removeClass('shadow-lg');
        }
    );
    
    // Statistics refresh (if needed)
    $('.refresh-stats').on('click', function() {
        location.reload();
    });
});

/**
 * View full document with highlights
 */
async function viewFullDocument(docId, query) {
    try {
        showLoading('Đang tải toàn bộ văn bản...');
        
        const response = await fetch(`/api/document/full?docId=${encodeURIComponent(docId)}&query=${encodeURIComponent(query)}`);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || 'Lỗi khi tải văn bản');
        }
        
        showFullDocumentModal(data, query);
        
    } catch (error) {
        console.error('Error fetching full document:', error);
        showError('Không thể tải toàn bộ văn bản: ' + error.message);
    } finally {
        hideLoading();
    }
}

/**
 * Show full document in a modal
 */
function showFullDocumentModal(docData, query) {
    // Remove existing modal if any
    const existingModal = document.getElementById('fullDocumentModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Create modal HTML
    const modalHtml = `
        <div class="modal fade" id="fullDocumentModal" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="fas fa-file-alt me-2"></i>
                            ${escapeHtml(docData.documentName)}
                        </h5>
                        <div class="ms-auto d-flex align-items-center">
                            <span class="badge bg-success me-2">
                                Độ tương đồng: ${docData.scorePercentage}
                            </span>
                            <span class="text-muted me-3">
                                ${docData.wordCount} từ
                            </span>
                            <div class="btn-group me-2" role="group">
                                <button type="button" class="btn btn-outline-secondary btn-sm" 
                                        onclick="copyFullDocumentText()">
                                    <i class="fas fa-copy"></i> Sao chép
                                </button>
                                <button type="button" class="btn btn-outline-secondary btn-sm" 
                                        onclick="printFullDocument()">
                                    <i class="fas fa-print"></i> In
                                </button>
                                <button type="button" class="btn btn-outline-secondary btn-sm" 
                                        onclick="toggleFullDocumentHighlights()">
                                    <i class="fas fa-highlighter"></i> 
                                    <span id="highlightToggleText">Ẩn highlights</span>
                                </button>
                            </div>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                    </div>
                    <div class="modal-body">
                        <div class="search-info mb-3">
                            <small class="text-muted">
                                <i class="fas fa-search me-1"></i>
                                Tìm kiếm: <strong>${escapeHtml(query)}</strong>
                            </small>
                        </div>
                        <div id="fullDocumentContent" class="document-content">
                            ${docData.fullContent}
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                            Đóng
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Add modal to DOM
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Store document data for other functions
    window.currentFullDocument = {
        data: docData,
        query: query,
        highlightsVisible: true
    };
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('fullDocumentModal'));
    modal.show();
    
    // Clean up when modal is hidden
    document.getElementById('fullDocumentModal').addEventListener('hidden.bs.modal', function() {
        this.remove();
        delete window.currentFullDocument;
    });
}

/**
 * Copy full document text to clipboard
 */
function copyFullDocumentText() {
    if (!window.currentFullDocument) return;
    
    const content = document.getElementById('fullDocumentContent');
    const text = content.textContent || content.innerText;
    
    navigator.clipboard.writeText(text).then(() => {
        showSuccess('Đã sao chép văn bản vào clipboard');
    }).catch(err => {
        console.error('Error copying text:', err);
        showError('Không thể sao chép văn bản');
    });
}

/**
 * Print full document
 */
function printFullDocument() {
    if (!window.currentFullDocument) return;
    
    const content = document.getElementById('fullDocumentContent').innerHTML;
    const docData = window.currentFullDocument.data;
    
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <html>
        <head>
            <title>${escapeHtml(docData.documentName)}</title>
            <style>
                body { 
                    font-family: Arial, sans-serif; 
                    direction: rtl; 
                    text-align: right;
                    line-height: 1.6;
                    margin: 20px;
                }
                .search-highlight { 
                    background-color: yellow; 
                    padding: 2px 4px;
                    border-radius: 3px;
                }
                .header {
                    border-bottom: 2px solid #333;
                    padding-bottom: 10px;
                    margin-bottom: 20px;
                }
                .document-info {
                    color: #666;
                    font-size: 14px;
                    margin-bottom: 10px;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>${escapeHtml(docData.documentName)}</h1>
                <div class="document-info">
                    Độ tương đồng: ${docData.scorePercentage} | 
                    Số từ: ${docData.wordCount} | 
                    Tìm kiếm: "${escapeHtml(window.currentFullDocument.query)}"
                </div>
            </div>
            <div class="content">
                ${content}
            </div>
        </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.print();
}

/**
 * Toggle highlights in full document view
 */
function toggleFullDocumentHighlights() {
    if (!window.currentFullDocument) return;
    
    const content = document.getElementById('fullDocumentContent');
    const toggleText = document.getElementById('highlightToggleText');
    
    if (window.currentFullDocument.highlightsVisible) {
        // Hide highlights
        const highlighted = content.innerHTML;
        const plainText = highlighted.replace(/<mark[^>]*>(.*?)<\/mark>/g, '$1');
        content.innerHTML = plainText;
        toggleText.textContent = 'Hiện highlights';
        window.currentFullDocument.highlightsVisible = false;
    } else {
        // Show highlights
        content.innerHTML = window.currentFullDocument.data.fullContent;
        toggleText.textContent = 'Ẩn highlights';
        window.currentFullDocument.highlightsVisible = true;
    }
}

// API helper functions
window.DocumentSearchAPI = {
    // Quick search via AJAX
    quickSearch: function(query, callback) {
        $.ajax({
            url: '/api/search',
            method: 'GET',
            data: { query: query },
            success: callback,
            error: function(xhr, status, error) {
                console.error('Search API error:', error);
                if (callback) callback(null, error);
            }
        });
    },
    
    // Get search statistics
    getStatistics: function(callback) {
        $.ajax({
            url: '/api/search/statistics',
            method: 'GET',
            success: callback,
            error: function(xhr, status, error) {
                console.error('Statistics API error:', error);
            }
        });
    },
    
    // Check if query has relevant documents
    checkRelevance: function(query, callback) {
        $.ajax({
            url: '/api/search/check-relevance',
            method: 'GET',
            data: { query: query },
            success: callback,
            error: function(xhr, status, error) {
                console.error('Relevance check API error:', error);
            }
        });
    }
};
