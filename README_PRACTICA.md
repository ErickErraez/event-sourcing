# Ejercicio practico: cancelar un pedido

## Objetivo

Agrega al proyecto la posibilidad de cancelar un pedido que todavia no se haya pagado. La cancelacion debe registrarse como un evento nuevo, sin borrar ni modificar los eventos anteriores.

Al terminar, el flujo sera:

```text
POST /api/orders
  -> OrderPlaced
POST /api/orders/{orderId}/cancellations
  -> OrderCancelled
GET /api/orders/{orderId}
  -> estado reconstruido CANCELLED
GET /api/orders/{orderId}/events
  -> historial completo y ordenado
```

Tiempo sugerido: 45-60 minutos.

## Antes de empezar

1. Sigue las instrucciones de [README.md](README.md) para instalar requisitos, iniciar la aplicacion y probar el flujo actual.
2. Desde la raiz del proyecto, ejecuta `mvn test` y confirma que las pruebas existentes pasan.
3. Revisa como se conectan `Order`, `OrderEvent`, `OrderApplicationService`, `EventStore` y `OrderController`.

No necesitas instalar una base de datos: el Event Store actual guarda los streams en memoria.

## Requerimientos

1. Agrega un evento de dominio `OrderCancelled` con `orderId`, `reason` y `occurredAt`. Su tipo debe ser `OrderCancelled`.
2. Agrega el estado `CANCELLED` a `OrderStatus` y conserva el motivo de cancelacion en el agregado para que pueda reconstruirse.
3. Solo se puede cancelar un pedido en estado `PLACED`.
   - Si el pedido ya esta `PAID` o `CANCELLED`, rechaza la operacion con `InvalidOrderOperationException`.
   - Rechaza motivos nulos, vacios o que contengan solo espacios.
4. Al cancelar, el agregado debe emitir y aplicar `OrderCancelled`, siguiendo el patron existente de `pay` y `OrderPaid`.
5. Al reconstruir el pedido, reproducir `OrderPlaced` seguido de `OrderCancelled` debe producir estado `CANCELLED` y conservar el motivo.
6. Agrega la operacion a `OrderApplicationService`: carga el historial, reconstruye el agregado, realiza la cancelacion y guarda los eventos nuevos usando la version esperada del historial.
7. Expone `POST /api/orders/{orderId}/cancellations`, con un cuerpo como:

   ```json
   {
     "reason": "El cliente ya no necesita el pedido"
   }
   ```

   Debe responder `200 OK` con el pedido actualizado. Si es necesario, adapta la respuesta del pedido para que el motivo pueda consultarse despues de reconstruirlo.
8. Incluye `OrderCancelled` en la respuesta de `GET /api/orders/{orderId}/events`, con el motivo dentro de `data`.
9. Conserva los errores HTTP: `404 Not Found` si el pedido no existe y `400 Bad Request` para operaciones invalidas.

## Pasos sugeridos

### 1. Implementa el dominio

Agrega el evento, el estado y la transicion de cancelacion en `Order`. Escribe pruebas unitarias para las reglas de negocio y la reconstruccion antes de conectar la API.

### 2. Conecta el caso de uso

Sigue el patron de `payOrder`: carga la historia, valida que el pedido exista, reconstruye el agregado, ejecuta la cancelacion y agrega los eventos nuevos con la version actual del stream.

### 3. Expone la operacion en la API

Agrega el request y el endpoint de cancelacion en `OrderController`. Actualiza la conversion de eventos para que el historial exponga el motivo.

### 4. Prueba el flujo completo

Inicia la aplicacion desde la raiz:

```powershell
mvn spring-boot:run
```

En otra terminal PowerShell, crea un pedido:

```powershell
curl.exe -i -X POST http://localhost:8080/api/orders `
  -H "Content-Type: application/json" `
  -d '{"orderId":"order-200","customerId":"customer-9","totalAmount":80.00}'
```

Cancela el pedido:

```powershell
curl.exe -i -X POST http://localhost:8080/api/orders/order-200/cancellations `
  -H "Content-Type: application/json" `
  -d '{"reason":"El cliente ya no necesita el pedido"}'
```

Consulta el estado y el historial:

```powershell
curl.exe -i http://localhost:8080/api/orders/order-200
curl.exe -i http://localhost:8080/api/orders/order-200/events
```

Comprueba que el estado sea `CANCELLED`, que el motivo se conserve y que el historial contenga `OrderPlaced` y `OrderCancelled`, en ese orden.

## Pruebas minimas

Agrega pruebas que verifiquen que:

- Un pedido `PLACED` puede cancelarse y genera exactamente un evento `OrderCancelled`.
- Rehidratar los eventos de creacion y cancelacion produce estado `CANCELLED` y conserva el motivo.
- No se puede cancelar un pedido `PAID`.
- No se puede cancelar dos veces un pedido.
- Se rechaza un motivo nulo, vacio o compuesto solo por espacios.
- La API permite cancelar un pedido existente y consultar el nuevo evento.
- La API devuelve `404` para un pedido inexistente y `400` para una cancelacion invalida.

Ejecuta todas las pruebas al terminar:

```powershell
mvn test
```

## Criterios de aceptacion

- [ ] La cancelacion agrega un evento nuevo; no borra ni edita el historial.
- [ ] El estado actual se obtiene reproduciendo el historial.
- [ ] Solo se puede cancelar un pedido en estado `PLACED`.
- [ ] El motivo se valida y esta disponible despues de reconstruir el pedido.
- [ ] El caso de uso agrega eventos con la version actual del stream.
- [ ] Las pruebas existentes y las nuevas pasan con `mvn test`.

## Para conversar al finalizar

1. Por que `OrderCancelled` es un evento y no una actualizacion del pedido?
2. Que deberia ocurrir si se solicita cancelar despues del pago? Se podria modelar una devolucion con otro caso de uso y otro evento?
3. Si el motivo de cancelacion tuviera que corregirse, por que no convendria editar el evento guardado?

## Reto opcional

Agrega `OrderRefunded` como un flujo separado. Define en que estado se permite el reembolso, registra el evento correspondiente y verifica que el estado se reconstruya reproduciendo toda la historia. No reutilices ni modifiques `OrderPaid` para representar el reembolso.
