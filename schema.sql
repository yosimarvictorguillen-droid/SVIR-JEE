-- ============================================================
-- SVIR-JEE - Sistema de Ventas e Inventario para Reposteria
-- DER normalizado - MySQL 8
-- ============================================================

CREATE DATABASE IF NOT EXISTS reposteria_jee
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE reposteria_jee;

-- ------------------------------------------------------------
-- usuarios: personal interno del sistema
-- ------------------------------------------------------------
CREATE TABLE usuarios (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    email               VARCHAR(120)  NOT NULL UNIQUE,
    password_hash       VARCHAR(255)  NOT NULL,
    rol                 ENUM('ADMIN','VENTAS','COCINA','REPARTIDOR') NOT NULL,
    activo              TINYINT(1)    NOT NULL DEFAULT 1,
    telefono            VARCHAR(20)   NULL,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- clientes: personas/empresas a las que se les vende
-- ------------------------------------------------------------
CREATE TABLE clientes (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    dni                 VARCHAR(8)    NULL,
    ruc                 VARCHAR(11)   NULL,
    telefono            VARCHAR(20)   NULL,
    direccion           VARCHAR(255)  NULL,
    email               VARCHAR(150)  NULL,
    password_hash       VARCHAR(100)  NULL COMMENT 'NULL si el cliente no tiene cuenta en la tienda web',
    activo              TINYINT(1)    NOT NULL DEFAULT 1,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- productos: catalogo de productos terminados (vendibles)
-- ------------------------------------------------------------
CREATE TABLE productos (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    descripcion         TEXT          NULL,
    precio              DECIMAL(10,2) NOT NULL,
    stock               INT           NOT NULL DEFAULT 0,
    stock_minimo        INT           NOT NULL DEFAULT 0,
    activo              TINYINT(1)    NOT NULL DEFAULT 1,
    imagen_url          VARCHAR(500)  NULL,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- ingredientes: insumos de cocina (materia prima)
-- ------------------------------------------------------------
CREATE TABLE ingredientes (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    unidad_medida       VARCHAR(20)   NOT NULL,
    stock               DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_minimo        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo              TINYINT(1)    NOT NULL DEFAULT 1,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- recetas: cuanto de cada ingrediente lleva un producto
-- ------------------------------------------------------------
CREATE TABLE recetas (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    producto_id         INT NOT NULL,
    ingrediente_id      INT NOT NULL,
    cantidad            DECIMAL(10,2) NOT NULL,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_receta_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_receta_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id),
    CONSTRAINT uq_receta_producto_ingrediente UNIQUE (producto_id, ingrediente_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- pedidos: cabecera de una venta / pedido
-- ------------------------------------------------------------
CREATE TABLE pedidos (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id          INT NULL,
    usuario_id          INT NULL,
    total               DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado              ENUM('PENDIENTE','PARCIAL','PREPARACION','LISTO','EN_CAMINO','ENTREGADO','CANCELADO')
                            NOT NULL DEFAULT 'PENDIENTE',
    tipo_origen         ENUM('PRESENCIAL','TIENDA','WHATSAPP','WEB','DELIVERY') NOT NULL DEFAULT 'PRESENCIAL',
    observacion         TEXT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- detalle_pedido: lineas de un pedido
-- ------------------------------------------------------------
CREATE TABLE detalle_pedido (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id           INT NOT NULL,
    producto_id         INT NOT NULL,
    cantidad            INT NOT NULL,
    cantidad_atendida   INT NOT NULL DEFAULT 0,
    precio_unitario     DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    estado              ENUM('PENDIENTE','PARCIAL','ATENDIDO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- producciones: ordenes de cocina
-- ------------------------------------------------------------
CREATE TABLE producciones (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    tipo                ENUM('PEDIDO','STOCK') NOT NULL,
    pedido_id           INT NULL,
    usuario_id          INT NOT NULL,
    estado              ENUM('PENDIENTE','EN_PROCESO','TERMINADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    observacion         TEXT NULL,
    fecha_inicio        TIMESTAMP NULL,
    fecha_fin           TIMESTAMP NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_produccion_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    CONSTRAINT fk_produccion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

CREATE TABLE produccion_detalle (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    produccion_id         INT NOT NULL,
    producto_id           INT NOT NULL,
    cantidad_planificada  INT NOT NULL,
    cantidad_producida    INT NOT NULL DEFAULT 0,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_proddet_produccion FOREIGN KEY (produccion_id) REFERENCES producciones(id),
    CONSTRAINT fk_proddet_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- movimientos_producto: auditoria de entradas/salidas de productos
-- ------------------------------------------------------------
CREATE TABLE movimientos_producto (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    producto_id         INT NOT NULL,
    tipo                ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    motivo              ENUM('PRODUCCION','VENTA','CANCELACION','MERMA','AJUSTE_MANUAL') NOT NULL,
    cantidad            INT NOT NULL,
    stock_anterior      INT NOT NULL,
    stock_nuevo         INT NOT NULL,
    referencia_tipo     ENUM('PEDIDO','PRODUCCION','MANUAL') NOT NULL,
    referencia_id       INT NULL,
    usuario_id          INT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movprod_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_movprod_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- movimientos_ingrediente: auditoria de entradas/salidas de ingredientes
-- ------------------------------------------------------------
CREATE TABLE movimientos_ingrediente (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    ingrediente_id      INT NOT NULL,
    tipo                ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    motivo              ENUM('COMPRA','PRODUCCION','CANCELACION','MERMA','AJUSTE_MANUAL') NOT NULL,
    cantidad            DECIMAL(10,2) NOT NULL,
    stock_anterior      DECIMAL(10,2) NOT NULL,
    stock_nuevo         DECIMAL(10,2) NOT NULL,
    referencia_tipo     ENUM('PRODUCCION','MANUAL','COMPRA') NOT NULL,
    referencia_id       INT NULL,
    usuario_id          INT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movingr_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id),
    CONSTRAINT fk_movingr_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- ============================================================
-- Datos semilla
-- ============================================================

-- Usuario ADMIN por defecto (password: Admin123!)
-- El hash se genera con jBCrypt al arrancar la app si la tabla usuarios
-- esta vacia (ver com.svir.jee.util.AppStartupListener). No hace falta
-- insertarlo a mano aqui.

INSERT INTO productos (nombre, descripcion, precio, stock, stock_minimo, activo) VALUES
  ('Torta de chocolate', 'Torta de chocolate rellena de manjar', 45.00, 8, 3, 1),
  ('Mousse de maracuya', 'Mousse individual de maracuya', 12.50, 20, 5, 1),
  ('Pancitos de anis', 'Docena de pancitos de anis', 8.00, 15, 5, 1),
  ('Galletas decoradas', 'Caja de 6 galletas decoradas', 18.00, 10, 4, 1);

INSERT INTO ingredientes (nombre, unidad_medida, stock, stock_minimo, activo) VALUES
  ('Harina', 'kg', 25.00, 5.00, 1),
  ('Azucar', 'kg', 18.00, 5.00, 1),
  ('Chocolate bitter', 'kg', 6.00, 2.00, 1),
  ('Mantequilla', 'kg', 9.00, 3.00, 1),
  ('Huevos', 'unidad', 120.00, 30.00, 1);

INSERT INTO clientes (nombre, dni, telefono, direccion, email, activo) VALUES
  ('Cliente Mostrador', NULL, NULL, NULL, NULL, 1),
  ('Maria Fernandez', '45678912', '987654321', 'Av. Los Alamos 123, Lima', 'maria.fernandez@mail.com', 1);

-- Recetas: cuanto de cada ingrediente lleva una unidad de cada producto
-- (id 1=Harina, 2=Azucar, 3=Chocolate bitter, 4=Mantequilla, 5=Huevos)
INSERT INTO recetas (producto_id, ingrediente_id, cantidad) VALUES
  (1, 1, 0.30), (1, 2, 0.20), (1, 3, 0.15), (1, 5, 3),   -- Torta de chocolate
  (2, 2, 0.05), (2, 5, 1),                                -- Mousse de maracuya
  (3, 1, 0.08), (3, 2, 0.02), (3, 5, 0.2),                -- Pancitos de anis
  (4, 1, 0.10), (4, 2, 0.06), (4, 4, 0.05);                -- Galletas decoradas
