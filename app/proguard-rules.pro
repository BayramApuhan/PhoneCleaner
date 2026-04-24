# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep model classes used by Compose
-keep class com.bayramapuhan.phonecleaner.domain.model.** { *; }
