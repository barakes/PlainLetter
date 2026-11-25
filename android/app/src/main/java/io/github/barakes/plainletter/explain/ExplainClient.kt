package io.github.barakes.plainletter.explain

import android.util.Log
import io.github.barakes.plainletter.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// Sends the redacted text to the server and reads the explanation.
// Uses only what Android already has (HttpURLConnection and org.json), so no new libraries.
object ExplainClient {

    // Set in app/build.gradle.kts.
    private const val BASE_URL = BuildConfig.SERVER_URL

    private const val TAG = "ExplainClient"

    private const val NO_CONNECTION = "אין חיבור לשרת. נסה שוב בעוד רגע."
    private const val BAD_TEXT = "הטקסט ריק או ארוך מדי."
    private const val DAILY_LIMIT = "הגעת למספר ההסברים היומי. אפשר לנסות שוב מחר."
    private const val BUSY = "השירות עמוס כרגע. נסה שוב בעוד כמה דקות."
    private const val SERVER_ERROR = "משהו השתבש בשרת. נסה שוב."
    private const val BAD_ANSWER = "התשובה מהשרת לא הייתה תקינה. נסה שוב."

    suspend fun explain(text: String, deviceId: String): ExplainResult = withContext(Dispatchers.IO) {
        try {
            val connection = URL("$BASE_URL/api/explain").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10_000
                // The free server sleeps when unused and takes up to a minute to wake,
                // and then may try several AI models. So give it time.
                connection.readTimeout = 90_000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("X-Device-Id", deviceId)

                val body = JSONObject().put("text", text).toString()
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                when (connection.responseCode) {
                    200 -> {
                        val json = connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
                        ExplainResult.Success(parse(json))
                    }
                    400 -> ExplainResult.Failure(BAD_TEXT)
                    429 -> ExplainResult.Failure(DAILY_LIMIT, canRetry = false)
                    503 -> ExplainResult.Failure(BUSY)
                    else -> ExplainResult.Failure(SERVER_ERROR)
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: IOException) {
            Log.w(TAG, "Request to server failed", e)
            ExplainResult.Failure(NO_CONNECTION)
        } catch (e: JSONException) {
            Log.w(TAG, "Bad answer from server", e)
            ExplainResult.Failure(BAD_ANSWER)
        }
    }

    private fun parse(json: String): Explanation {
        val root = JSONObject(json)
        val array = root.getJSONArray("points")
        val points = (0 until array.length()).map { index ->
            val point = array.getJSONObject(index)
            Point(
                text = point.getString("text"),
                quote = point.getString("quote"),
                verified = point.optBoolean("verified", true),
            )
        }
        return Explanation(
            summary = root.getString("summary"),
            points = points,
            disclaimer = root.getString("disclaimer"),
        )
    }
}
