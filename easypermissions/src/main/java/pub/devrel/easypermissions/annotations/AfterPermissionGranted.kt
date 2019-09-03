package pub.devrel.easypermissions.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AfterPermissionGranted(val value: Int)