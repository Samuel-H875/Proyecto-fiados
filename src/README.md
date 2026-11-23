# Integrantes

- Samuel David Henández Narváez
- Jorge Samuel Morelo Martinez

## Proyecto Fiados

Programa de consola en Java para llevar el **fiado de una tienda de barrio**: registrar clientes, anotar compras fiadas y abonos, y decidir automáticamente si a un cliente se le puede seguir fiando según su tipo.

## Requisitos

- **JDK 17 o superior** (se necesita el compilador `javac`, no solo el `java`).
- Terminal **PowerShell** (la de VS Code en Windows sirve).

Para comprobar que Java está instalado:

```powershell
javac -version
java -version
```

## Estructura del proyecto

```text
Proyecto_Fiados/
├── src/
│   ├── fiados/
│   │   ├── Main.java                  ← punto de arranque
│   │   ├── ui/                        ← interfaz de consola
│   │   │   └── ConsolaFiados.java
│   │   ├── servicio/                  ← casos de uso y almacenamiento
│   │   │   ├── ServicioFiados.java
│   │   │   ├── RepositorioClientes.java
│   │   │   └── DatosEjemplo.java
│   │   ├── evaluacion/                ← reglas de cada tipo de cliente
│   │   │   ├── EvaluadorFiado.java            (clase abstracta)
│   │   │   ├── EvaluadorClienteNuevo.java
│   │   │   ├── EvaluadorClienteConocido.java
│   │   │   ├── EvaluadorClienteConfianza.java
│   │   │   ├── EvaluadorClienteQuincenal.java
│   │   │   ├── RegistroEvaluadores.java
│   │   │   └── ResultadoEvaluacion.java
│   │   └── modelo/                    ← datos del negocio
│   │       ├── Cliente.java
│   │       ├── Movimiento.java
│   │       ├── TipoMovimiento.java    (enum)
│   │       ├── TipoCliente.java       (enum)
│   │       └── Dinero.java
│   └── pruebas/
│       └── PruebaFlujo.java
└── out/                               ← se crea al compilar (código compilado)
```

## Cómo compilar (y crear la carpeta `out`)

Abre la terminal **en la carpeta `Proyecto_Fiados`** (la que contiene `src`) y ejecuta:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

Este comando compila todos los archivos `.java` de `src` y deja los `.class` en la carpeta **`out`**. Si `out` no existe, el mismo comando la crea. Si por algún motivo da error por la carpeta, créala a mano y vuelve a ejecutar el comando:

```powershell
mkdir out
```

> Si cambias cualquier archivo `.java`, **vuelve a compilar**. La carpeta `out` guarda la versión anterior y el programa seguirá comportándose como antes hasta que recompiles, tambien puedes borrar la carpeta out y volver a ejecutar.

## Cómo ejecutar el programa

```powershell
chcp 65001
java -cp out fiados.Main
```

- `chcp 65001` pone la terminal en UTF-8 para que se vean bien las tildes, la ñ y los símbolos (por ejemplo `n.º`, `límite`). Solo hace falta una vez por cada terminal abierta.
- `java -cp out fiados.Main` ejecuta el programa usando las clases compiladas de `out`.

## Cómo se usa

Al iniciar aparece este menú. Se elige escribiendo el número y pulsando Enter:

| Opción | Qué hace |
| --- | --- |
| 1 | Registrar un cliente nuevo (nombre y tipo) |
| 2 | Registrar una compra fiada |
| 3 | Registrar un abono |
| 4 | Ver el estado de cuenta de un cliente |
| 5 | Listar todos los clientes |
| 6 | Listar los clientes con saldo pendiente |
| 7 | Cargar datos de ejemplo |
| 8 | Cambiar el tipo de un cliente |
| 0 | Salir |

Consejo: para probar rápido, usa primero la opción **7** (datos de ejemplo) y luego pide una compra fiada a cada cliente.

Los datos se guardan **solo en memoria**: al cerrar el programa se pierden.

## Tipos de cliente

El tipo se elige con un número al registrar o cambiar un cliente.

| N.º | Tipo | Regla |
| --- | --- | --- |
| 1 | nuevo | Límite de $50.000; máximo 2 compras fiadas seguidas sin abonar |
| 2 | conocido | Límite de $150.000; se bloquea si pasan más de 30 días sin abonar |
| 3 | confianza | Límite de $300.000 + 10 % de lo abonado, con tope de $600.000 |
| 4 | quincenal | Límite de $200.000; se bloquea si pasan más de 15 días sin abonar |

**Cambio de tipo:** no se permite si la deuda actual del cliente supera el límite del tipo al que se quiere pasar. El programa indica cuánto debe abonar antes de poder hacer el cambio.

## Diseño

- **Template Method:** `EvaluadorFiado.evaluar()` define los pasos de la evaluación y cada tipo de cliente solo implementa su propia regla (`calcularLimite()` y, opcionalmente, `verificarCondiciones()`).
- **Enums:** `TipoMovimiento` (compra fiada / abono, con etiqueta y signo sobre el saldo) y `TipoCliente` (opciones del menú de tipos).
- **Capas:** `ui` → `servicio` → `evaluacion` y `modelo`. La consola solo habla con `ServicioFiados`.

## Pruebas

Con el proyecto ya compilado:

```powershell
java -cp out pruebas.PruebaFlujo
```

## Problemas frecuentes

| Problema | Solución |
| --- | --- |
| `javac` no se reconoce como comando | Instala un JDK (no solo el JRE) y reinicia la terminal. |
| Se ven símbolos raros en lugar de tildes | Ejecuta `chcp 65001` antes de `java`. |
| El programa no muestra los cambios que hice | Vuelve a compilar con el comando `javac`. |
| Error de que no encuentra la clase `fiados.Main` | Asegúrate de estar en la carpeta `Proyecto_Fiados` y de haber compilado antes. |
