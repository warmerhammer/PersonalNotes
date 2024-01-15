package com.warmerhammer.personalnotes.Utils

import android.content.Context
import com.warmerhammer.personalnotes.MainActivity.MainActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DragListenerModule {
    @Singleton
    @Binds
    abstract fun bindDragListener(mainActivity: MainActivity): DragListener
}

@Module
@InstallIn(FragmentComponent::class)
object DragListenerObject {
    @DragListenerInterface
    @Provides
    fun getDragListener(@ActivityContext context: Context): DragListener = context as DragListener
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DragListenerInterface