package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.VaultSecurity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KeyNest", appName)
  }

  @Test
  fun `test provider detection and key entropy`() {
    val openAiSample = "sk-proj-placeholder-test"
    assertEquals("OpenAI", VaultSecurity.detectProviderFromKey(openAiSample))

    val geminiSample = "AIzaSyPlaceholderTest"
    assertEquals("Google Gemini", VaultSecurity.detectProviderFromKey(geminiSample))

    val entropy = VaultSecurity.calculateEntropy("sample-arbitrary-random-key-value-1234567890")
    assertTrue(entropy.entropyBits > 20)
  }
}
