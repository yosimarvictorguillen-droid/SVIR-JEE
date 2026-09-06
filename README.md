# SVIR-JEE — Sistema de Ventas e Inventario para Repostería

Versión **Java EE clásica** (JSP + Servlets + JDBC, sin frameworks) del
sistema SVIR original (que usa Spring Boot). Mismo dominio de negocio
(pastelería "Dulce Momento"), pensado para cumplir la rúbrica de
Desarrollo Web Integrado: Tomcat como Web Container, JDBC puro con clase
de conexión singleton, JSP con directivas/scriptlets, y MySQL con DER
normalizado.

---

## Stack tecnológico

- **Java 21** + **Jakarta EE 10** (`jakarta.servlet-api` 6.0, JSP 3.1)
- **Apache Tomcat 11**
- **JDBC puro** (MySQL Connector/J) — sin JPA/Hibernate. Todas las
  consultas usan `PreparedStatement`; las transacciones se manejan a mano
  (`Connection.setAutoCommit(false)` + `commit()`/`rollback()`).
- **jBCrypt** para hash de contraseñas.
- **MySQL 8**
- **Maven** (empaqueta un `.war`)
- Frontend: HTML5 + CSS3 + Bootstrap 5 (CDN) + JavaScript vanilla. Vistas
  dinámicas en JSP (scriptlets `<% %>`, expresiones `<%= %>`, directivas
  `<%@ %>` e incluso una declaración `<%! %>`), sin JSTL/EL (para dejar
  explícito el uso de "fundamentos de JSP" que pide la rúbrica).

---

## Módulos implementados

| Módulo | Rutas | Roles |
|---|---|---|
| Login / Logout | `/login`, `/logout` | Público |
| Dashboard | `/app/dashboard` | Todos |
| Productos (CRUD) | `/app/productos` | ADMIN, VENTAS |
| Ingredientes (CRUD + movimientos de stock) | `/app/ingredientes` | ADMIN, COCINA |
| Recetas (ingrediente + cantidad por producto) | `/app/recetas` | ADMIN, COCINA |
| Producción (para stock o por pedido; termina/cancela) | `/app/producciones` | ADMIN, COCINA |
| Clientes (CRUD interno) | `/app/clientes` | ADMIN, VENTAS |
| Pedidos (alta con detalle, cambiar estado, cancelar) | `/app/pedidos` | ADMIN, VENTAS |
| Punto de Venta (carrito + comprobante + ticket) | `/app/pos` | ADMIN, VENTAS |
| Repartidor (pedidos delivery: para recoger / en camino / entregados) | `/app/repartidor` | ADMIN, VENTAS, REPARTIDOR |
| Movimientos (auditoría de stock, solo lectura) | `/app/movimientos` | ADMIN |
| Usuarios (personal interno) | `/app/usuarios` | ADMIN |
| Reportes (ventas por rango de fechas + stock bajo) | `/app/reportes` | ADMIN |
| **Tienda web pública** (sin login de personal): home, catálogo, carrito, checkout (recojo o delivery con GPS), mis pedidos (historial o seguimiento por número), registro/login de clientes | `/home`, `/catalogo`, `/carrito`, `/checkout`, `/mis-pedidos`, `/cliente/login`, `/cliente/registro`, `/cliente/logout` | Público |

Roles de personal: `ADMIN`, `VENTAS`, `COCINA`, `REPARTIDOR` (sesión vía
`HttpSession`, filtro `AuthFilter` protege todo `/app/*` según rol).

---

## Lógica transaccional destacada

- **Crear pedido**: por cada línea, atiende de inmediato lo que el stock
  permita, descuenta `productos.stock` y registra `movimientos_producto`
  (SALIDA/VENTA) — todo en una sola transacción JDBC (`PedidoDAO.crear`).
- **Cancelar pedido**: devuelve el stock atendido y registra el
  movimiento de reversa (`PedidoDAO.cancelar`).
- **Crear producción**: exige receta configurada para cada producto;
  descuenta los ingredientes necesarios (`cantidad_receta × cantidad_a_producir`)
  y falla si no alcanza el stock (`ProduccionDAO.crear`).
- **Terminar producción**: suma el stock producido al producto, registra
  el movimiento (ENTRADA/PRODUCCION) y, si la orden viene de un pedido,
  actualiza `cantidad_atendida` de sus líneas y recalcula el estado del
  pedido (`ProduccionDAO.terminar` + `PedidoDAO.aplicarProduccionAPedido`).
- **Cancelar producción**: devuelve al stock los ingredientes que se
  habían descontado (`ProduccionDAO.cancelar`).
- **Movimiento manual de ingrediente**: ENTRADA/SALIDA son delta, AJUSTE
  fija el stock al valor exacto indicado (`IngredienteDAO.registrarMovimiento`).

Todas estas operaciones abren su propia `Connection`, hacen
`setAutoCommit(false)`, y en caso de error ejecutan `rollback()` antes de
relanzar la excepción — así que si algo falla a mitad de camino, nada
queda a medias en la base de datos.

---

## Estructura del proyecto

```
SVIR-JEE/
├── pom.xml
├── docs/
│   └── schema.sql                    # DER completo (11 tablas) + datos semilla
├── prototipos/                       # 3 alternativas de UI estáticas (rúbrica cap. 3)
│   ├── alternativa-1/  (Bootstrap clásico, ámbar)
│   ├── alternativa-2/  (panel oscuro estilo SaaS)
│   └── alternativa-3/  (minimalista, un solo acento)
└── src/main/
    ├── resources/
    │   ├── db.properties.example     # plantilla (el real NO se sube a git)
    ├── java/com/svir/jee/
    │   ├── db/ConexionBD.java        # singleton JDBC (DriverManager)
    │   ├── model/                    # POJOs + enums
    │   ├── dao/                      # acceso a datos, JDBC puro
    │   ├── filter/                   # EncodingFilter, AuthFilter
    │   ├── util/                     # PasswordUtil (BCrypt), AppStartupListener
    │   └── servlet/                  # un servlet por recurso (patrón Front Controller simple)
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml
        │   └── views/                 # JSP del panel interno (+ common/header-footer)
        │       └── tienda/            # JSP de la tienda pública (+ su propio header/footer)
        └── assets/css/
```

---

## Instalación y ejecución

### 1. Base de datos

```bash
mysql -u root -p < docs/schema.sql
```

Esto crea la base `reposteria_jee` con las 11 tablas, datos semilla
(4 productos, 5 ingredientes, 2 clientes, 12 líneas de receta) y deja la
tabla `usuarios` vacía a propósito: el usuario **ADMIN** se crea solo, la
primera vez que arranca la aplicación (ver más abajo).

### 2. Configurar la conexión

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Edita `db.properties` con tu usuario/password reales de MySQL. Este
archivo está en `.gitignore` — nunca se sube al repositorio público.

### 3. Compilar y empaquetar

```bash
mvn clean package
```

Genera `target/svir-jee.war`.

### 4. Desplegar en Tomcat

Copia el `.war` a `TOMCAT_HOME/webapps/` (o usa el botón "Run" de
NetBeans/Eclipse si tienes el servidor configurado ahí) y arranca Tomcat:

```bash
TOMCAT_HOME/bin/startup.bat
```

La app queda en `http://localhost:8080/svir-jee/`.

Al desplegar por primera vez (tabla `usuarios` vacía), la aplicación crea
automáticamente:

```
Email:      admin@dulcemomento.com
Contraseña: Admin123!
```

Cámbiala una vez que entres por primera vez (o crea otros usuarios desde
**Usuarios** y desactiva este).

### 5. Tienda web pública

No requiere login: `http://localhost:8080/svir-jee/home`. Desde el panel
interno hay un botón **"Tienda"** en la barra superior que la abre en una
pestaña nueva (igual que en el SVIR original).

---

## Simplificaciones respecto al SVIR original

Para mantener el alcance manejable, esta versión **no** incluye (se
puede agregar después con la misma arquitectura):

- Recuperación de contraseña con pregunta de seguridad (ni para personal
  ni para clientes) — el reseteo de clave de un cliente/usuario solo lo
  puede hacer un ADMIN editándolo.
- Subida de imágenes de producto (el catálogo usa un color + inicial
  como placeholder en vez de foto).
- Búsqueda de cliente por DNI/RUC en vivo dentro del POS (el cajero solo
  registra el documento/razón social manualmente al elegir Boleta con
  DNI o Factura).
- WhatsApp deep-links y "contactar admin" en el flujo de recuperación.

---

## Prototipos estáticos (rúbrica, capítulo 3)

`prototipos/alternativa-1|2|3/` son mockups **estáticos** (HTML/CSS
puro, sin backend), cada uno con las 5 pantallas mínimas exigidas
(Login, Dashboard, Formulario, Tabla, Reportes) en un estilo visual
distinto. La *Alternativa 1* es la que se llevó a producción.
