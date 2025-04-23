import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AutoPublisher_GMB_Helper(private val accessToken: String, private val accountID: String) {

    private val client = OkHttpClient()

    @Throws(IOException::class)
    fun createPost(locationId: String, title: String, description: String, sourceUrl: String, languageCode: String = "de-DE") {
        val url = "https://mybusiness.googleapis.com/v4/accounts/${accountID}/locations/$locationId/localPosts"


        // TODO Add date / time selection
        val json = """
            {
                "languageCode": "$languageCode",
                "summary": "$description",
                "event": {
                    "title": "$title",
                    "schedule": {
                        "startDate": {
                            "year": 2024,
                            "month": 4,
                            "day": 20,
                        },
                        "startTime": {
                              "hours": 9,
                              "minutes": 0,
                              "seconds": 0,
                              "nanos": 0,
                        },
                        "endDate": {
                            "year": 2024,
                            "month": 4,
                            "day": 20,
                        },
                        "endTime": {
                              "hours": 17,
                              "minutes": 0,
                              "seconds": 0,
                              "nanos": 0,
                        }
                    }
                },
                "callToAction": {
                    "actionType": "LEARN_MORE",
                    "url": "https://z10.info",
                },
                "media": [
                    {
                        "mediaFormat": "PHOTO",
                        "sourceUrl": "$sourceUrl",
                    }
                ],
                "topicType": "EVENT",
                "validateOnly": "True"
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to create post: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("Failed to create post: ${response.code} - ${response.message}")
                } else {
                    println("Post created successfully.")
                }
            }
        })
    }

    fun listAccounts() {
        val url = "https://mybusinessaccountmanagement.googleapis.com/v1/accounts"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to create: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("Failed to create: ${response.code} - ${response.message}")
                } else {
                    println("created successfully.")
                }
            }
        })
    }
}
