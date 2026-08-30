# Plan de Limpieza y Ajuste para Repartidor

Este plan detalla los cambios necesarios para transformar la aplicación en una versión exclusiva para repartidores, ajustando el Splash Screen, eliminando componentes innecesarios y corrigiendo problemas de paquetes.

## Cambios Propuestos

### Componente de UI (composeApp)

#### [MODIFY] [SplashScreen.kt](file:///Users/tayler/Desktop/PizzzaDrive/composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/splash/SplashScreen.kt)
- Eliminar la lógica de sincronización de productos (`viewModel.syncProducts`).
- Establecer un retraso fijo de 3 segundos utilizando `delay(3000)`.
- Asegurar que al terminar llame a `onFinished()`.

#### [MODIFY] [AppNavigation.kt](file:///Users/tayler/Desktop/PizzzaDrive/composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/component/AppNavigation.kt)
- Cambiar el destino final del Splash Screen para que navegue directamente a `DriverHome` en lugar de `RoleSelection`.
- Eliminar todas las rutas y composables que no sean `Splash` y `DriverHome`.

#### [MODIFY] [MainActivity.kt](file:///Users/tayler/Desktop/PizzzaDrive/composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/MainActivity.kt)
- Eliminar la inyección y uso de `CartViewModel`, `StoreViewModel` y `AuthViewModel` si no son requeridos por la vista del repartidor.
- Mantener `AppViewModel` ya que se solicita conservar la lógica relacionada a los servicios.

#### [DELETE] Carpetas de UI no relacionadas
- `composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/client/`
- `composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/menu/`
- `composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/branches/`
- `composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/products/`
- `composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/roles/`
- `composeApp/src/androidMain/kotlin/com/pizzza/pizzzaapp/ui/orders/` (si no es usada por el repartidor).

### Componente de Datos y Servicios (shared)

#### [MODIFY] [KmmService.kt](file:///Users/tayler/Desktop/PizzzaDrive/shared/src/commonMain/kotlin/com/pizzza/pizzzaapp/repository/network/KmmService.kt)
- Actualizar el paquete a `com.pizzza.pizzzaDrive.repository.network`.
- Corregir el import de `LoginResponse` a `com.pizzza.pizzzaDrive.repository.network.model.LoginResponse`.

#### [MODIFY] Otros archivos en `shared`
- Revisar y actualizar paquetes de `DataUseCase`, `IDataNetwork`, `DataNetwork` y modelos para que coincidan con `com.pizzza.pizzzaDrive`.

## Plan de Verificación

### Verificación Manual
- Ejecutar la aplicación y verificar que el Splash Screen dure 3 segundos.
- Confirmar que después del Splash se muestre la pantalla "Bienvenido, Repartidor".
- Verificar que no haya errores de compilación debido a los cambios de paquete.
