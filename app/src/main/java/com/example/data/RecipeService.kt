package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class Recipe(
    @Json(name = "name") val name: String,
    @Json(name = "prepTime") val prepTime: String,
    @Json(name = "whyItFits") val whyItFits: String,
    @Json(name = "ingredients") val ingredients: List<String>,
    @Json(name = "instructions") val instructions: List<String>
)

@JsonClass(generateAdapter = true)
data class RecipeResponse(
    @Json(name = "recipes") val recipes: List<Recipe>
)

object RecipeService {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun suggestRecipes(fruits: List<String>): List<Recipe> = withContext(Dispatchers.IO) {
        if (fruits.isEmpty()) return@withContext emptyList()

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Return some default placeholder mock recipes in case API Key is not configured yet.
            return@withContext getMockRecipes(fruits)
        }

        val prompt = """
            Create 3 delicious, healthy recipe suggestions that can be made using some or all of these fruits currently in the user's shopping cart: ${fruits.joinToString(", ")}.
            You can include other common kitchen ingredients (e.g., milk, yogurt, honey, ice, chia seeds, oats, cinnamon).
            
            Return the response as a single, valid JSON object that exactly matches this schema:
            {
              "recipes": [
                {
                  "name": "Recipe Name",
                  "prepTime": "15 mins",
                  "whyItFits": "A brief explanation of why this recipe was suggested based on the fruits in their cart",
                  "ingredients": [
                    "1 cup sliced mango",
                    "1/2 cup yogurt",
                    "1 tbsp honey"
                  ],
                  "instructions": [
                    "Wash and prep the ingredients.",
                    "Blend everything together in a high-speed blender until smooth.",
                    "Pour into a glass and serve chilled."
                  ]
                }
              ]
            }
            
            Do not include any markdown formatting, code blocks (such as ```json), or extra text outside of the JSON object.
        """.trimIndent()

        // Build the REST request payload using raw Map representation for simplicity and absolute safety
        val requestPayload = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt)
                    )
                )
            ),
            "generationConfig" to mapOf(
                "responseMimeType" to "application/json",
                "temperature" to 0.7
            )
        )

        val jsonAdapter = moshi.adapter(Map::class.java)
        val requestBodyString = jsonAdapter.toJson(requestPayload)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyString.toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("API call failed with status: ${response.code} - ${response.message}")
                }
                val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                
                // Parse Gemini API Response
                val responseMap = moshi.adapter(Map::class.java).fromJson(bodyString)
                val candidates = responseMap?.get("candidates") as? List<*>
                val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
                val content = firstCandidate?.get("content") as? Map<*, *>
                val parts = content?.get("parts") as? List<*>
                val firstPart = parts?.firstOrNull() as? Map<*, *>
                val rawText = firstPart?.get("text") as? String ?: throw Exception("No text in response")

                val cleanText = cleanJsonResponse(rawText)
                val recipeResponseAdapter = moshi.adapter(RecipeResponse::class.java)
                val recipeResponse = recipeResponseAdapter.fromJson(cleanText)
                
                recipeResponse?.recipes ?: getMockRecipes(fruits)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to mock recipes on any networking or parsing failure to ensure exceptional UX
            getMockRecipes(fruits)
        }
    }

    private fun cleanJsonResponse(rawText: String): String {
        var text = rawText.trim()
        if (text.startsWith("```json")) {
            text = text.substringAfter("```json")
        } else if (text.startsWith("```")) {
            text = text.substringAfter("```")
        }
        if (text.endsWith("```")) {
            text = text.substringBeforeLast("```")
        }
        return text.trim()
    }

    private fun getMockRecipes(fruits: List<String>): List<Recipe> {
        val list = mutableListOf<Recipe>()
        val firstFruit = fruits.firstOrNull() ?: "Fruit"
        
        list.add(
            Recipe(
                name = "Fresh $firstFruit Medley Smoothie",
                prepTime = "5 mins",
                whyItFits = "Utilizes your fresh ${fruits.joinToString(", ")} for a refreshing vitamin-packed start to your day.",
                ingredients = fruits.map { "1 cup chopped fresh $it" } + listOf("1/2 cup Greek yogurt or milk", "1 tbsp organic honey", "3-4 ice cubes"),
                instructions = listOf(
                    "Wash all the fruits thoroughly and chop them into medium-sized pieces.",
                    "Add the chopped ${fruits.joinToString(" and ")} into a blender.",
                    "Pour in the yogurt/milk and honey.",
                    "Blend on high speed until completely smooth and creamy.",
                    "Pour into glasses, garnish with a thin fruit slice, and serve immediately!"
                )
            )
        )

        list.add(
            Recipe(
                name = "Healthy ${fruits.joinToString(" & ")} Fruit Salad",
                prepTime = "10 mins",
                whyItFits = "A crisp and colorful bowl highlighting the natural flavors of your selected cart items.",
                ingredients = fruits.map { "1 cup diced $it" } + listOf("1 tbsp fresh lime juice", "A pinch of chat masala or black salt", "Fresh mint leaves for garnish"),
                instructions = listOf(
                    "Dice all fruits into uniform bite-sized cubes.",
                    "In a large bowl, gently toss the diced fruits together.",
                    "Drizzle fresh lime juice over the top to keep them vibrant and add a zesty kick.",
                    "Sprinkle a pinch of chat masala or black salt for a hint of local spice.",
                    "Garnish with mint leaves and chill in the refrigerator for 15 minutes before serving."
                )
            )
        )

        list.add(
            Recipe(
                name = "Comforting Oatmeal Bowl with Caramelized Fruit",
                prepTime = "12 mins",
                whyItFits = "Perfect comforting breakfast pairing warm rolled oats with a healthy fruit topping.",
                ingredients = fruits.map { "1/2 cup finely sliced $it" } + listOf("1 cup rolled oats", "2 cups almond milk or water", "1/2 tsp ground cinnamon", "1 tbsp maple syrup"),
                instructions = listOf(
                    "In a small saucepan, bring the milk or water to a gentle boil.",
                    "Stir in the rolled oats and cinnamon, reduce heat to low, and cook for 5-7 minutes until soft.",
                    "In a separate small pan, cook the sliced fruits with the maple syrup and a splash of water for 3-5 minutes until tender and caramelized.",
                    "Transfer the cooked oatmeal to a bowl.",
                    "Spoon the warm fruit compote over the oatmeal, sprinkle extra cinnamon if desired, and enjoy!"
                )
            )
        )

        return list
    }
}
