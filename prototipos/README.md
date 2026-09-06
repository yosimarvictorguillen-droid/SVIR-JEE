# Prototipos de interfaz - 3 alternativas de solucion

Estas son 3 alternativas visuales/tecnicas de interfaz, evaluadas antes de
implementar el sistema real (JSP + Servlets + JDBC). Son paginas HTML5 +
CSS3 **estaticas** (sin conexion a base de datos, con datos de ejemplo
fijos), pensadas para la etapa de "Planteamiento de alternativas de
solucion" de la rubrica.

Cada alternativa incluye las 5 pantallas minimas requeridas: Login,
Dashboard, Formulario, Tabla de datos y Reportes.

| Alternativa | Enfoque | Carpeta |
|---|---|---|
| 1 | Panel administrativo clasico (Bootstrap, sidebar fija, paleta ambar) - es la que se llevo a produccion en `src/main/webapp` | `alternativa-1/` |
| 2 | Panel oscuro tipo "SaaS moderno" (CSS propio, navbar superior, tarjetas KPI) | `alternativa-2/` |
| 3 | Minimalista tipo app de consumo (CSS propio, mucho espacio en blanco, un solo color de acento) | `alternativa-3/` |

Abrir cualquier `login.html` directamente en el navegador (no requiere
servidor).
