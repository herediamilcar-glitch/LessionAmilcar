# LESSION — Monitoreo de actividad muscular

Aplicación Android para conectar una banda inteligente EMGv3 por Bluetooth Low Energy (BLE), visualizar mediciones durante el entrenamiento y consultar sesiones guardadas.

## Casos de uso

### CU-01 — Conectar la banda inteligente

- **Actor:** Deportista.
- **Precondiciones:** Bluetooth activado y permisos de Bluetooth concedidos.
- **Flujo principal:**
  1. El deportista abre **Conectar Banda**.
  2. La aplicación busca dispositivos BLE y muestra los encontrados.
  3. El deportista selecciona la banda.
  4. La aplicación establece la conexión y muestra su estado.
- **Flujos alternativos:** Si no se encuentra el dispositivo, el deportista puede volver a escanear. Si la banda se desconecta o se apaga Bluetooth, la aplicación informa la interrupción; si corresponde, intenta reconectarse.
- **Postcondición:** La banda queda conectada y lista para enviar mediciones.

### CU-02 — Consultar datos físicos en tiempo real

- **Actor:** Deportista.
- **Precondiciones:** Banda conectada y enviando mediciones EMGv3.
- **Flujo principal:**
  1. El deportista abre el panel de actividad.
  2. La aplicación muestra la lectura EMG en ADC, el promedio, el máximo, la cantidad de muestras y la frecuencia de muestreo.
  3. Los valores visibles se actualizan cuando llegan nuevas muestras.
- **Postcondición:** El deportista puede consultar las mediciones recibidas y sus unidades.

### CU-03 — Consultar gráficos de rendimiento

- **Actor:** Deportista.
- **Precondiciones:** La aplicación recibió mediciones del sensor.
- **Flujo principal:**
  1. El deportista abre **Gráficos**.
  2. La aplicación representa la señal EMG como un gráfico de líneas.
  3. Al recibir nuevas muestras, actualiza el gráfico y señala el umbral configurado.
- **Postcondición:** El deportista puede observar las variaciones de la señal durante el entrenamiento.

### CU-04 — Recibir alertas sobre el estado físico

- **Actor:** Deportista.
- **Precondiciones:** Sensor conectado y umbral de alerta configurado.
- **Flujo principal:**
  1. La aplicación compara cada lectura con el umbral.
  2. Cuando una lectura alcanza o supera el umbral, muestra una alerta visual con la lectura y el valor de referencia.
  3. El deportista puede consultar el historial de alertas y descartar la alerta activa.
- **Flujos alternativos:** Ante la interrupción de la conexión, la aplicación muestra el estado de conexión perdido o el error correspondiente.
- **Postcondición:** La señal que excede el umbral queda identificada. La alerta es informativa y no constituye un diagnóstico médico.

### CU-05 — Guardar y consultar mediciones

- **Actor:** Deportista.
- **Precondiciones:** Banda conectada y sesión de entrenamiento iniciada.
- **Flujo principal:**
  1. El deportista inicia una sesión de entrenamiento.
  2. La aplicación registra las mediciones y las alertas generadas durante la sesión.
  3. Al detener la sesión, guarda el resumen y las mediciones en la base de datos local.
  4. El deportista abre **Historial** y selecciona una sesión para consultar su resumen, gráfico y muestras.
  5. Si lo desea, exporta la sesión en formato CSV.
- **Postcondición:** La sesión queda disponible en el historial para consulta posterior.

## Casos de prueba

Los siguientes casos describen verificaciones funcionales manuales en un dispositivo Android con Bluetooth LE. Para los casos que requieren el sensor, se necesita una banda EMGv3 compatible y transmitiendo datos.

| ID | Caso relacionado | Pasos | Resultado esperado |
|---|---|---|---|
| CP-01 | CU-01: Conexión exitosa | Activar Bluetooth, conceder los permisos solicitados, abrir **Conectar Banda**, escanear y seleccionar la banda. | La banda aparece en la lista y la aplicación indica conexión establecida. Al llegar muestras, indica que está recibiendo datos. |
| CP-02 | CU-01: Bluetooth desactivado | Desactivar Bluetooth y abrir o iniciar el escaneo de dispositivos. | La aplicación no informa una conexión exitosa y muestra el estado de error o desconexión. |
| CP-03 | CU-01 / CU-04: Pérdida de conexión | Conectar la banda y apagarla o alejarla hasta interrumpir la comunicación. | La aplicación informa que se perdió la conexión y, si la reconexión automática está activa y la banda vuelve a estar disponible, intenta reconectarse. |
| CP-04 | CU-02: Lectura en tiempo real | Conectar el sensor y transmitir varias muestras con valores conocidos. | La lectura EMG aparece en ADC; los valores y estadísticas se actualizan al recibir nuevas muestras. |
| CP-05 | CU-02: Datos identificados | Abrir el panel de actividad con el sensor conectado. | Las mediciones visibles incluyen etiquetas comprensibles y unidades, como ADC y Hz; antes de recibir señal se muestra que está esperando datos. |
| CP-06 | CU-03: Gráfico y actualización | Abrir **Gráficos**, transmitir muestras con lecturas ascendentes y descendentes, y observar la pantalla. | Se muestra una línea que refleja ambas variaciones y el gráfico incorpora nuevas mediciones recibidas. |
| CP-07 | CU-04: Lectura bajo el umbral | Configurar un umbral y transmitir una lectura inferior. | No se genera una nueva alerta de esfuerzo elevado por esa lectura. |
| CP-08 | CU-04: Lectura igual o superior al umbral | Configurar el umbral y transmitir una lectura igual o superior a este. | Se muestra una alerta que indica la lectura y el umbral; el evento aparece en el historial de alertas. |
| CP-09 | CU-05: Guardado de sesión | Conectar el sensor, iniciar una sesión, transmitir varias lecturas y detenerla. | La sesión queda en el historial con su resumen y las mediciones registradas durante esa sesión. |
| CP-10 | CU-05: Consulta y exportación | Abrir una sesión guardada desde el historial y solicitar su exportación CSV. | Se puede consultar el gráfico y las muestras de la sesión; el CSV contiene el resumen y las mediciones con sus marcas de tiempo. |
| CP-11 | CU-05: Persistencia | Guardar una sesión, cerrar y volver a abrir la aplicación, y consultar el historial. | La sesión y sus mediciones continúan disponibles. |
