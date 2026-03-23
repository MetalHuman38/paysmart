package net.metalbrain.paysmart

import dagger.Module
import dagger.Provides
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.di.AppModule
import net.metalbrain.paysmart.di.FirebaseModule
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class, FirebaseModule::class]
)
object FirebaseTestModule {

    @Provides
    fun provideFirebaseAppCheck(): FirebaseAppCheck =
        mockk(relaxed = true)

    @Provides
    fun provideFirestore(): FirebaseFirestore =
        mockk(relaxed = true)

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth =
        mockk(relaxed = true)

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage =
        mockk(relaxed = true)

    @Provides
    fun provideFirebaseMessaging(): FirebaseMessaging =
        mockk(relaxed = true)
}
