# Frontend Integration Guide - Stripe Payments

## Setup

### Install Stripe.js

Add Stripe.js to your HTML:

```html
<script src="https://js.stripe.com/v3/"></script>
```

## Basic Implementation

### Step 1: Initialize Stripe

```javascript
const stripe = Stripe('YOUR_PUBLISHABLE_KEY');
const elements = stripe.elements();
const cardElement = elements.create('card');
cardElement.mount('#card-element');
```

### Step 2: Create a Payment Intent

When user clicks checkout, create a payment intent:

```javascript
async function createPaymentIntent(orderId, amount) {
  const response = await fetch('/api/payments/create-intent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      orderId: orderId,
      amount: amount,
      currency: 'usd',
      paymentMethod: 'CARD',
      description: `Payment for order #${orderId}`
    })
  });

  const data = await response.json();
  if (!data.success) {
    throw new Error(data.message);
  }
  return data;
}
```

### Step 3: Confirm Payment with Stripe

```javascript
async function confirmPayment(orderId, clientSecret) {
  const result = await stripe.confirmCardPayment(clientSecret, {
    payment_method: {
      card: cardElement,
      billing_details: {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value
      }
    }
  });

  if (result.error) {
    console.error('Payment failed:', result.error.message);
    return false;
  }

  return result.paymentIntent;
}
```

### Step 4: Confirm Order on Backend

```javascript
async function confirmOrderPayment(orderId, paymentIntentId) {
  const response = await fetch(`/api/payments/confirm`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams({
      orderId: orderId,
      paymentIntentId: paymentIntentId
    })
  });

  const data = await response.json();
  if (!data.success) {
    throw new Error(data.message);
  }
  return data;
}
```

## Complete Checkout Flow Example

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://js.stripe.com/v3/"></script>
</head>
<body>
  <form id="checkout-form">
    <h2>Payment Details</h2>
    <input type="text" id="name" placeholder="Full Name" required>
    <input type="email" id="email" placeholder="Email" required>
    
    <div id="card-element"></div>
    <div id="card-errors" role="alert"></div>
    
    <button type="submit">Pay Now</button>
  </form>

  <script>
    const stripe = Stripe('YOUR_PUBLISHABLE_KEY');
    const elements = stripe.elements();
    const cardElement = elements.create('card');
    cardElement.mount('#card-element');

    // Handle form submission
    document.getElementById('checkout-form').addEventListener('submit', async (e) => {
      e.preventDefault();

      const orderId = new URLSearchParams(window.location.search).get('orderId');
      const amount = new URLSearchParams(window.location.search).get('amount');

      try {
        // Step 1: Create payment intent
        const intentData = await createPaymentIntent(orderId, amount);

        // Step 2: Confirm payment with Stripe
        const paymentIntent = await confirmPayment(orderId, intentData.clientSecret);

        // Step 3: Confirm on backend
        const result = await confirmOrderPayment(orderId, paymentIntent.id);

        alert('Payment successful!');
        window.location.href = `/orders/${orderId}`;
      } catch (error) {
        document.getElementById('card-errors').textContent = error.message;
      }
    });

    // Handle card errors
    cardElement.addEventListener('change', (e) => {
      if (e.error) {
        document.getElementById('card-errors').textContent = e.error.message;
      } else {
        document.getElementById('card-errors').textContent = '';
      }
    });
  </script>
</body>
</html>
```

## Using Stripe Payment Element (Recommended)

Stripe Payment Element simplifies integration and supports more payment methods:

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://js.stripe.com/v3/"></script>
  <style>
    .payment-element {
      margin-bottom: 15px;
    }
  </style>
</head>
<body>
  <form id="payment-form">
    <div id="payment-element"></div>
    <button id="submit" type="submit">
      <div class="spinner hidden" id="spinner"></div>
      <span id="button-text">Pay now</span>
    </button>
    <div id="payment-message" class="hidden"></div>
  </form>

  <script>
    const stripe = Stripe('YOUR_PUBLISHABLE_KEY');

    let elements;

    initialize();

    async function initialize() {
      const orderId = new URLSearchParams(window.location.search).get('orderId');
      const amount = new URLSearchParams(window.location.search).get('amount');

      // Create payment intent
      const response = await fetch('/api/payments/create-intent', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderId: orderId,
          amount: amount,
          currency: 'usd',
          description: `Payment for order #${orderId}`
        })
      });

      const paymentData = await response.json();

      elements = stripe.elements({
        clientSecret: paymentData.clientSecret
      });

      const paymentElement = elements.create('payment');
      paymentElement.mount('#payment-element');
    }

    document.getElementById('payment-form').addEventListener('submit', handleSubmit);

    async function handleSubmit(e) {
      e.preventDefault();
      setLoading(true);

      const orderId = new URLSearchParams(window.location.search).get('orderId');

      const { error } = await stripe.confirmPayment({
        elements,
        confirmParams: {
          return_url: `http://localhost:8080/orders/${orderId}?confirmed=true`
        }
      });

      if (error) {
        showMessage(error.message);
        setLoading(false);
      }
    }

    function showMessage(messageText) {
      const messageContainer = document.querySelector('#payment-message');
      messageContainer.classList.remove('hidden');
      messageContainer.textContent = messageText;
    }

    function setLoading(isLoading) {
      document.querySelector('#submit').disabled = isLoading;
      document.querySelector('#spinner').classList.toggle('hidden', !isLoading);
      document.querySelector('#button-text').classList.toggle('hidden', isLoading);
    }
  </script>
</body>
</html>
```

## Testing

### Test Cards

Use these in test mode:

- **Success**: 4242 4242 4242 4242
- **Requires Auth**: 4000 0025 0000 3155
- **Declined**: 4000 0000 0000 0002

## API Integration Checklist

- [ ] Publish key configured in frontend
- [ ] Create payment intent on order checkout
- [ ] Handle Stripe payment confirmation
- [ ] Confirm payment on backend
- [ ] Display payment status to user
- [ ] Handle payment errors
- [ ] Redirect to success/failure pages
- [ ] Store payment intent ID for support

## Environment Variables

```env
REACT_APP_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

## Support

For issues:
1. Check browser console for Stripe.js errors
2. Review payment intent status in Stripe Dashboard
3. Check backend logs for payment processing errors
4. Verify API keys are correct

## Documentation Links

- [Stripe.js Documentation](https://stripe.com/docs/js)
- [Payment Intent API](https://stripe.com/docs/payments/payment-intents)
- [Payment Element](https://stripe.com/docs/payments/payment-element)
- [Error Handling](https://stripe.com/docs/payments/handle-payment-errors)
