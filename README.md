# marketplace-spring
E-commerce marketplace con Spring Boot

## Configuración de Base de Datos
La aplicación usa PostgreSQL. Configura la conexión con las variables:
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

## Pasarela de Pagos - Stripe Integration

La aplicación incluye integración completa con Stripe para procesar pagos reales.

### Requisitos
- Cuenta en Stripe (https://stripe.com)
- API Key de Stripe (clave secreta)
- Webhook Secret para eventos de Stripe

### Configuración

1. **Variables de entorno**:
   ```
   STRIPE_API_KEY=sk_test_... (o sk_live_... en producción)
   STRIPE_WEBHOOK_SECRET=whsec_...
   ```

2. **Endpoints disponibles**:
   - `POST /api/payments/create-intent` - Crear intención de pago
   - `POST /api/payments/confirm` - Confirmar pago
   - `GET /api/payments/status/{orderId}` - Obtener estado del pago
   - `POST /api/payments/refund` - Reembolsar un pago
   - `POST /api/webhooks/stripe` - Webhook para eventos de Stripe

3. **Métodos de pago soportados**:
   - Tarjetas de crédito/débito (Visa, Mastercard, Amex)
   - PayPal (a través de Stripe)

### Documentación

- [PAYMENT_GATEWAY.md](./PAYMENT_GATEWAY.md) - Documentación técnica completa
- [FRONTEND_INTEGRATION.md](./FRONTEND_INTEGRATION.md) - Guía de integración frontend

### Testing

Para pruebas, usa tarjetas de prueba:
- `4242 4242 4242 4242` - Éxito
- `4000 0000 0000 0002` - Declinar

Ver [PAYMENT_GATEWAY.md](./PAYMENT_GATEWAY.md) para más tarjetas de prueba.

### Seguridad

- Todos los endpoints de pago requieren autenticación
- Las claves API se protegen con variables de entorno
- Los webhooks se verifican con firma de Stripe
- Los números completos de tarjeta nunca se almacenan
- Solo se almacenan los últimos 4 dígitos (si aplica)
