# Stripe Payment Gateway Integration

## Overview

This marketplace application now integrates with Stripe to handle real payment processing. The payment gateway supports both credit/debit card payments and PayPal payments through Stripe's payment processing system.

## Architecture

### Components

1. **PaymentService** (`com.marketplace.service.PaymentService`)
   - Handles communication with Stripe API
   - Creates payment intents
   - Confirms payments
   - Processes refunds
   - Manages payment lifecycle

2. **OrderService** (Updated)
   - Now integrates PaymentService during order creation
   - Confirms payments after client-side verification
   - Stores Stripe transaction details

3. **PaymentApiController** (`com.marketplace.controller.PaymentApiController`)
   - REST API endpoints for payment operations
   - Handles payment creation, confirmation, status checks
   - Secured with Spring Security authentication

4. **StripeWebhookController** (`com.marketplace.controller.StripeWebhookController`)
   - Handles Stripe webhook events
   - Processes payment_intent.succeeded events
   - Processes payment_intent.payment_failed events
   - Handles charge.refunded events

5. **DTOs**
   - `PaymentRequest`: Request payload for creating payments
   - `PaymentResponse`: Response payload for payment operations

## Configuration

### Environment Variables Required

Set the following environment variables or add them to `application.properties`:

```properties
STRIPE_API_KEY=sk_test_... # Your Stripe test or production API key
STRIPE_WEBHOOK_SECRET=whsec_... # Your Stripe webhook signing secret
```

### Default Configuration (development)

```properties
stripe.api.key=${STRIPE_API_KEY:sk_test_placeholder}
stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET:whsec_test_placeholder}
```

## Database Schema

The Order entity has been updated with new fields:

- `stripePaymentIntentId` (String, 255 chars): Stores Stripe Payment Intent ID
- `stripeTransactionId` (String, 255 chars): Stores Stripe transaction/charge ID

## API Endpoints

### Create Payment Intent
```
POST /api/payments/create-intent
Content-Type: application/json
Authorization: ******

Request Body:
{
  "orderId": 1,
  "amount": 99.99,
  "currency": "usd",
  "paymentMethod": "CARD",
  "cardToken": "tok_visa",
  "description": "Payment for order #1"
}

Response:
{
  "success": true,
  "message": "Payment intent created successfully",
  "paymentIntentId": "pi_...",
  "clientSecret": "pi_..._secret_...",
  "transactionId": "pi_...",
  "paymentStatus": "PENDING",
  "amount": 99.99
}
```

### Confirm Payment
```
POST /api/payments/confirm
Authorization: ******

Query Parameters:
- paymentIntentId: str (the Stripe payment intent ID)
- orderId: long (the order ID)

Response:
{
  "success": true,
  "message": "Payment confirmed successfully",
  "paymentStatus": "VERIFIED",
  "transactionId": "pi_...",
  "paymentIntentId": "pi_..."
}
```

### Get Payment Status
```
GET /api/payments/status/{orderId}
Authorization: ******

Response:
{
  "success": true,
  "message": "Payment status: VERIFIED",
  "paymentStatus": "VERIFIED",
  "transactionId": "pi_...",
  "paymentIntentId": "pi_...",
  "amount": 99.99
}
```

### Refund Payment
```
POST /api/payments/refund
Authorization: ******

Query Parameters:
- orderId: long (the order ID to refund)

Response:
{
  "success": true,
  "message": "Refund initiated",
  "transactionId": "pi_..."
}
```

### Webhook Endpoint
```
POST /api/webhooks/stripe
Stripe-Signature: t=...,v1=...

This endpoint is called by Stripe to notify about payment events.
```

## Integration Flow

### Client-Side Flow (Recommended)

1. User selects payment method and enters details
2. Frontend calls `POST /api/payments/create-intent` to create a Payment Intent
3. Frontend receives `clientSecret` from the response
4. Frontend uses Stripe.js to confirm the payment with `clientSecret`
5. After successful confirmation, frontend calls `POST /api/payments/confirm`
6. Order status is updated to CONFIRMED and payment status to VERIFIED

### Server-Side Webhook Flow

1. Stripe sends webhook event to `/api/webhooks/stripe`
2. Application verifies webhook signature
3. If `payment_intent.succeeded`:
   - Order status is automatically updated to CONFIRMED
   - Payment status is updated to VERIFIED
4. If `payment_intent.payment_failed`:
   - Payment status is updated to FAILED
   - Order remains in PENDING state

## Payment Methods Supported

1. **Credit/Debit Cards**
   - Visa, Mastercard, American Express
   - Stripe handles tokenization securely

2. **PayPal** (through Stripe)
   - Customers can pay via PayPal account
   - Redirected through Stripe's payment flow

## Transaction ID Storage

- **stripePaymentIntentId**: The unique Stripe Payment Intent identifier
  - Begins with `pi_`
  - Used to track and manage the payment
  
- **stripeTransactionId**: The confirmed charge/transaction ID
  - Assigned after successful payment confirmation
  - Used for refunds and reconciliation

## Error Handling

Payment errors are caught and logged:
- Stripe API exceptions
- Authorization failures
- Validation errors
- Network errors

Error messages are returned to the client for display.

## Testing

### Using Stripe Test Cards

In test mode, use these card numbers:

- **Visa**: 4242 4242 4242 4242
- **Mastercard**: 5555 5555 5555 4444
- **Amex**: 3782 822463 10005
- **Declined**: 4000 0000 0000 0002

Use any future expiration date and any 3-digit CVC.

### Getting Webhook Secret for Local Testing

1. Install Stripe CLI: https://stripe.com/docs/stripe-cli
2. Run: `stripe listen --forward-to localhost:8080/api/webhooks/stripe`
3. Use the webhook signing secret from the CLI output

## Security Considerations

1. **API Key Security**
   - Never commit API keys to version control
   - Use environment variables for secrets
   - Store keys in secure configuration management

2. **Webhook Verification**
   - All webhooks are verified using Stripe's signature
   - Invalid signatures are rejected

3. **HTTPS**
   - Always use HTTPS in production
   - Stripe webhooks require HTTPS endpoints

4. **PCI Compliance**
   - Never store full card numbers
   - Use Stripe's tokenization
   - Only the last 4 digits are stored locally

5. **Authentication**
   - All payment endpoints require user authentication
   - Users can only access their own orders
   - Seller access is properly validated

## Logging

Payment operations are logged with:
- INFO: Successful operations
- WARN: Payment failures
- ERROR: API errors and exceptions

Logs include:
- Payment intent creation
- Payment confirmations
- Webhook events
- Error details

## Troubleshooting

### "Stripe API key not configured"
- Check that `STRIPE_API_KEY` environment variable is set
- Verify it's not a placeholder value
- Restart the application after setting the variable

### "Webhook signature verification failed"
- Ensure `STRIPE_WEBHOOK_SECRET` matches your Stripe dashboard
- Check that you're using the correct webhook for your endpoint
- Verify the webhook is configured in Stripe Dashboard

### "Payment intent not found"
- Check that the payment intent ID is correct
- Verify the payment was created on the same Stripe account
- Check the order ID corresponds to the payment intent

## Future Enhancements

1. Implement retry logic for failed payments
2. Add support for installment plans
3. Implement fraud detection integration
4. Add support for international payment methods
5. Create admin dashboard for payment reconciliation
6. Implement transaction reconciliation service
7. Add payment history export functionality
