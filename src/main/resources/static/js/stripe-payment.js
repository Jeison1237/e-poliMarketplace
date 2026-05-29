// Stripe Payment Integration
let stripe = null;
let elements = null;
let cardElement = null;
let currentOrder = null;
let paymentProcessing = false;

// Stripe publishable key - should be configured
// For now, we'll fetch it from the backend configuration
const STRIPE_PUBLIC_KEY = 'pk_test_51TcCejIOdyurIGg8OWqpemTRR5mZGaZrW8D7UScB1xNG978fOqAbIiCAlyKWbKTrsdonexC7DjWC88LkiOgfy4To00yd5J2xKM';

/**
 * Initialize Stripe on page load
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeStripe();
    attachCheckoutFormListener();
});

/**
 * Initialize Stripe Elements
 */
function initializeStripe() {
    if (!STRIPE_PUBLIC_KEY || STRIPE_PUBLIC_KEY.includes('pk_test')) {
        try {
            stripe = Stripe(STRIPE_PUBLIC_KEY);
            elements = stripe.elements();
            cardElement = elements.create('card');
            cardElement.mount('#card-element');
            
            // Handle card errors
            cardElement.addEventListener('change', function(event) {
                const displayError = document.getElementById('card-errors');
                if (event.error) {
                    displayError.textContent = event.error.message;
                } else {
                    displayError.textContent = '';
                }
            });
        } catch (e) {
            console.warn('Stripe not fully initialized (test mode):', e);
        }
    }
}

/**
 * Attach listener to checkout form
 */
function attachCheckoutFormListener() {
    const checkoutForm = document.getElementById('checkout-form');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', handleCheckoutFormSubmit);
    }
}

/**
 * Handle checkout form submission
 */
async function handleCheckoutFormSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    const params = new URLSearchParams(formData);
    
    try {
        const response = await fetch('/orders/checkout', {
            method: 'POST',
            body: params,
            headers: {
                'Accept': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            // Store order info for payment
            currentOrder = {
                id: data.orderId,
                paymentIntentId: data.paymentIntentId,
                clientSecret: data.clientSecret,
                amount: data.amount
            };
            
            // Show payment modal
            showPaymentModal();
        } else {
            showAlert('error', 'Error: ' + data.message);
        }
    } catch (error) {
        console.error('Checkout error:', error);
        showAlert('error', 'Error al procesar la solicitud: ' + error.message);
    }
}

/**
 * Show payment modal
 */
function showPaymentModal() {
    const modal = document.getElementById('payment-modal');
    const overlay = document.getElementById('modal-overlay');
    
    if (modal && overlay) {
        modal.style.display = 'block';
        overlay.style.display = 'block';
        
        // Recreate card element if needed
        if (cardElement && !cardElement._complete) {
            setTimeout(() => {
                cardElement.focus();
            }, 300);
        }
    }
}

/**
 * Close payment modal
 */
function closePaymentModal() {
    const modal = document.getElementById('payment-modal');
    const overlay = document.getElementById('modal-overlay');
    
    if (modal && overlay) {
        modal.style.display = 'none';
        overlay.style.display = 'none';
        paymentProcessing = false;
        document.getElementById('payment-processing').style.display = 'none';
        document.getElementById('pay-button').style.display = 'block';
    }
}

/**
 * Confirm payment with Stripe
 */
async function confirmPayment() {
    if (paymentProcessing || !currentOrder) {
        return;
    }
    
    paymentProcessing = true;
    const payButton = document.getElementById('pay-button');
    const processingMsg = document.getElementById('payment-processing');
    
    payButton.style.display = 'none';
    processingMsg.style.display = 'block';
    
    try {
        // Confirm the payment intent with the card
        const result = await stripe.confirmCardPayment(currentOrder.clientSecret, {
            payment_method: {
                card: cardElement,
                billing_details: {
                    // Could add billing details from form if needed
                }
            }
        });
        
        if (result.error) {
            // Show error to customer
            const errorElement = document.getElementById('card-errors');
            errorElement.textContent = result.error.message;
            
            payButton.style.display = 'block';
            processingMsg.style.display = 'none';
            paymentProcessing = false;
        } else if (result.paymentIntent) {
            // Payment successful - now confirm on backend
            const paymentIntent = result.paymentIntent;
            
            if (paymentIntent.status === 'succeeded') {
                // Confirm the payment on the backend
                await completePaymentOnBackend(paymentIntent.id);
            } else {
                showAlert('error', 'El pago no se completó correctamente: ' + paymentIntent.status);
                payButton.style.display = 'block';
                processingMsg.style.display = 'none';
                paymentProcessing = false;
            }
        }
    } catch (error) {
        console.error('Payment error:', error);
        document.getElementById('card-errors').textContent = 'Error procesando el pago: ' + error.message;
        
        payButton.style.display = 'block';
        processingMsg.style.display = 'none';
        paymentProcessing = false;
    }
}

/**
 * Complete payment confirmation on backend
 */
async function completePaymentOnBackend(paymentIntentId) {
    try {
        const orderId = currentOrder.id;
        
        const response = await fetch(`/orders/${orderId}/confirm-payment?paymentIntentId=${paymentIntentId}`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            showAlert('success', '¡Pago completado! Redirigiendo a tus pedidos...');
            
            setTimeout(() => {
                window.location.href = '/orders';
            }, 2000);
        } else {
            showAlert('error', 'Error confirmando pago: ' + data.message);
            
            const payButton = document.getElementById('pay-button');
            const processingMsg = document.getElementById('payment-processing');
            payButton.style.display = 'block';
            processingMsg.style.display = 'none';
            paymentProcessing = false;
        }
    } catch (error) {
        console.error('Backend error:', error);
        showAlert('error', 'Error al confirmar el pago en el servidor: ' + error.message);
        
        const payButton = document.getElementById('pay-button');
        const processingMsg = document.getElementById('payment-processing');
        payButton.style.display = 'block';
        processingMsg.style.display = 'none';
        paymentProcessing = false;
    }
}

/**
 * Show alert message
 */
function showAlert(type, message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-' + (type === 'error' ? 'error' : 'success');
    alertDiv.innerHTML = '<i class="fas fa-' + (type === 'error' ? 'exclamation-circle' : 'check-circle') + '"></i> ' + message;
    
    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);
        
        setTimeout(() => {
            alertDiv.style.opacity = '0';
            alertDiv.style.transform = 'translateY(-10px)';
            alertDiv.style.transition = 'all 0.4s ease';
            setTimeout(() => alertDiv.remove(), 400);
        }, 5000);
    }
}
