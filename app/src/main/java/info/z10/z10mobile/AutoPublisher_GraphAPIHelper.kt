package info.z10.z10mobile

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.util.InternalAPI
import io.ktor.util.toByteArray

class AutoPublisher_GraphAPIHelper() {
    companion object {
        @OptIn(InternalAPI::class)
        suspend fun publish(caption: String) {


            val client = HttpClient()
            val urlString = "https://graph.facebook.com/${Credentials.igAccountID}/media?access_token=${Credentials.userAccessToken}&image_url=${Credentials.contentURL}&caption=$caption"

            val response: HttpResponse = client.get(urlString) {
                method = HttpMethod.Get
            }

            Log.e("AAAAAAAAAAAAAA", response.content.toByteArray().decodeToString())

            client.close()
        }
    }
}