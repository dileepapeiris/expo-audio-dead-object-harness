package androidx.annotation
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
annotation class RequiresPermission(val value: String = "")
