# API Gateway

## Objetivo
Centralizar el acceso a los microservicios `auth-service`, `catalog-service` y `orders-service` desde un solo punto de entrada.

## Stack
- Java 21
- Spring Boot 3.5.11
- Spring Cloud Gateway
- Maven Wrapper (`mvnw.cmd`)

## Estructura
- `src/main/java/com/reto/api_gateway/ApiGatewayApplication.java`: clase principal.
- `src/main/resources/application.yml`: configuraci?n de puertos, rutas y actuator.

## Puerto del gateway
- Gateway: `8080`

## Rutas configuradas
1. `auth-service`
- Entrada: `/auth/**`
- Destino: `http://localhost:8081`

2. `catalog-service`
- Entrada: `/catalog/**`
- Destino: `http://localhost:8083`
- Filtro: `RewritePath=/catalog/(?<segment>.*), /${segment}`
- Ejemplo: `GET /catalog/products` se reenv?a como `GET /products`.

3. `orders-service`
- Entrada: `/orders/**`
- Destino: `http://localhost:8082`

## Actuator
Endpoints expuestos:
- `/actuator/health`
- `/actuator/gateway`

## Ejecutar localmente
Desde la raiz del repo (`C:\Users\wgacol\IdeaProjects\auth-service`):

```powershell
.\mvnw.cmd -f api-gateway\pom.xml spring-boot:run
```

## Validaci?n r?pida
Con los 3 servicios arriba (`8081`, `8082`, `8083`), probar:

```powershell
curl http://localhost:8080/auth/<tu-endpoint>
curl http://localhost:8080/catalog/products
curl http://localhost:8080/orders/<tu-endpoint>
```

## Criterios de aceptaci?n (HU)
- Ruta a `Auth` funciona.
- Ruta a `Catalog` funciona.
- Ruta a `Orders` funciona.
- Se demuestra verificaci?n de conexi?n entre servicios mediante llamadas por el gateway.
