package org.rubin55.kaizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import android.util.Log
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.google.common.hash.HashCode
import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import org.rubin55.kaizer.ui.theme.KaizerTheme
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create a request hash.
        val hashFunction: HashFunction = Hashing.sha256()

        @Suppress("UnstableApiUsage")
        val hashCode: HashCode = hashFunction.newHasher()
            .putString("The quick brown fox jumps over the lazy dog", StandardCharsets.UTF_8)
            .hash()

        val requestHash: String = hashCode.toString()

        // Configure cloud project number, initialize manager and token request, create integrity token provider box.
        val cloudProjectId = 12143997365
        val integrityManager = IntegrityManagerFactory.createStandard(applicationContext)
        val integrityTokenProviderRequest =
            PrepareIntegrityTokenRequest.builder().setCloudProjectNumber(cloudProjectId).build()
        var integrityTokenProvider = MutableLiveData<StandardIntegrityTokenProvider>()

        // Execute integrity token provider request, fill integrity token provider box with result.
        integrityManager.prepareIntegrityToken(integrityTokenProviderRequest)
            .addOnSuccessListener { response ->
                Log.i("Kaizer", "I've received an integrityTokenProvider")
                integrityTokenProvider.value = response
            }
            .addOnFailureListener { exception -> Log.e("Kaizer", exception.message ?: "No message here...") }

        // Execute integrity token request using provider.
        integrityTokenProvider.observe(this, Observer { provider ->
            provider?.let {
                val integrityTokenRequest = StandardIntegrityTokenRequest.builder().setRequestHash(requestHash).build()
                val integrityToken: Task<StandardIntegrityToken> = provider.request(integrityTokenRequest)

                integrityToken
                    .addOnSuccessListener { response ->
                        Log.i("Kaizer", "I've received an integrityToken: ${response.token()}")
                    }
                    .addOnFailureListener { exception -> Log.e("Kaizer", exception.message ?: "No message here...") }
            }
        })

        setContent {
            KaizerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Kaizer",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KaizerTheme {
        Greeting("Kaizer")
    }
}
