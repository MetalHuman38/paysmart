package net.metalbrain.paysmart

import dagger.Module
import dagger.Provides
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.di.AppModule
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object FirebaseTestModule {

    @Provides
    fun provideFirestore(): FirebaseFirestore =
        mockk(relaxed = true)

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth =
        mockk(relaxed = true)
}
