-- Eliminar la tabla vieja si existe para actualizar la estructura
DROP TABLE IF EXISTS alimentos;

-- Crear la tabla con la columna 'estado' para el borrado lógico
CREATE TABLE alimentos (
  codigo varchar(20) NOT NULL,
  nombre varchar(100) NOT NULL,
  stock_actual int(11) NOT NULL,
  stock_minimo int(11) NOT NULL,
  stock_maximo int(11) NOT NULL,
  categoria varchar(50) NOT NULL,
  estado varchar(20) NOT NULL DEFAULT 'Activo',
  PRIMARY KEY (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insertar los datos iniciales
INSERT INTO alimentos (codigo, nombre, stock_actual, stock_minimo, stock_maximo, categoria, estado) VALUES
('010', 'Arroz Premium (kg)', 40, 15, 100, 'Granos y Cereales (kg)', 'Activo'),
('020', 'Frijol Negro (kg)', 25, 10, 80, 'Granos y Cereales (kg)', 'Activo'),
('030', 'Lentejas (kg)', 8, 12, 60, 'Granos y Cereales (kg)', 'Activo'),
('040', 'Aceite Vegetal (L)', 50, 20, 120, 'Abarrotes', 'Activo'),
('050', 'Atún en Lata (und)', 3, 25, 150, 'Proteínas y Conservas (und)', 'Activo');
