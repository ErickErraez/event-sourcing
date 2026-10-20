# Event Sourcing Workshop

API didactica de pedidos construida con Java, Spring Boot y Event Sourcing. Esta guia explica como preparar el entorno, iniciar la aplicacion y probar sus operaciones.

## Requisitos

- JDK 21.
- Maven 3.9 o superior.
- Git, si vas a clonar el repositorio.

Comprueba que Java y Maven estan instalados:

```powershell
java -version
mvn -version
```

La salida de `java -version` debe mostrar la version 21. Maven debe estar configurado para usar ese JDK.

## 1. Obtener el proyecto

Si aun no tienes una copia local, clona el repositorio y entra en la carpeta:

```powershell
git clone https://github.com/ErickErraez/event-sourcing.git
cd event-sourcing
```

Si ya tienes el proyecto, abre una terminal en su carpeta raiz, donde se encuentra `pom.xml`.

## 2. Ejecutar las pruebas

Desde la raiz del proyecto, ejecuta:

```powershell
mvn test
```

Maven descargara las dependencias necesarias la primera vez y ejecutara las pruebas. Si terminan correctamente, ya puedes iniciar la API.

## 3. Iniciar la aplicacion

En una terminal ubicada en la raiz del proyecto, ejecuta:

```powershell
mvn spring-boot:run
```

Espera el mensaje de inicio de Spring Boot. La API quedara disponible en `http://localhost:8080`. Mantén abierta esta terminal mientras la usas; para detener el servidor, pulsa `Ctrl+C`.

## 4. Probar la API

Abre una segunda terminal PowerShell. Los siguientes ejemplos usan `curl.exe`, incluido en Windows.

### Crear un pedido

```powershell
curl.exe -i -X POST http://localhost:8080/api/orders `
  -H "Content-Type: application/json" `
  -d '{"orderId":"order-100","customerId":"customer-7","totalAmount":125.50}'
```

La respuesta debe ser `201 Created` y mostrar el pedido con estado `PLACED`.

### Consultar el pedido y sus eventos

```powershell
curl.exe -i http://localhost:8080/api/orders/order-100
curl.exe -i http://localhost:8080/api/orders/order-100/events
```

La primera peticion devuelve el estado actual reconstruido. La segunda muestra el historial, que inicialmente contiene `OrderPlaced`.

### Pagar el pedido

```powershell
curl.exe -i -X POST http://localhost:8080/api/orders/order-100/payments `
  -H "Content-Type: application/json" `
  -d '{"paymentMethod":"CARD"}'
```

La respuesta debe ser `200 OK` y el estado debe cambiar a `PAID`. Consulta de nuevo el pedido y los eventos para ver el resultado:

```powershell
curl.exe -i http://localhost:8080/api/orders/order-100
curl.exe -i http://localhost:8080/api/orders/order-100/events
```

El historial conserva tanto `OrderPlaced` como `OrderPaid`; el estado se obtiene reproduciendo ambos eventos.

## Endpoints disponibles

| Metodo | Ruta | Descripcion |
|---|---|---|
| `POST` | `/api/orders` | Crea un pedido. Cuerpo: `orderId`, `customerId`, `totalAmount`. |
| `POST` | `/api/orders/{orderId}/payments` | Paga un pedido. Cuerpo: `paymentMethod`. |
| `GET` | `/api/orders/{orderId}` | Consulta el estado actual reconstruido. |
| `GET` | `/api/orders/{orderId}/events` | Consulta el historial de eventos. |

## Notas

- No hace falta instalar ni configurar una base de datos.
- El Event Store actual guarda los eventos en memoria: al detener o reiniciar la aplicacion, los pedidos y su historial se pierden.
- Los identificadores de pedido deben ser unicos durante una ejecucion. Para repetir los ejemplos, reinicia la aplicacion o cambia `orderId`.

