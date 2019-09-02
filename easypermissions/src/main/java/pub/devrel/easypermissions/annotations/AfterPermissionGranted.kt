package pub.devrel.easypermissions.annotations

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Target
annotation class AfterPermissionGranted(val value: Int)