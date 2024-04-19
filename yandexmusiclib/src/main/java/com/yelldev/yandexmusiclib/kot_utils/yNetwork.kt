package com.yelldev.yandexmusiclib.kot_utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import com.fasterxml.jackson.databind.ObjectMapper
import com.yelldev.yandexmusiclib.yClient
import org.apache.commons.codec.binary.Base64
import org.json.JSONException
import org.json.JSONObject
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.Path
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.CompletableFuture
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection


class yNetwork {

    companion object {
        private val userAgent =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"

        private var X_Yandex_Music_Client = "YandexMusicAndroid/24022571"
        private val SECRET = "p93jhgh689SBReK6ghtw62"

        fun post_2(){
            val values = mapOf("name" to "John Doe", "occupation" to "gardener")

            //val objectMapper = ObjectMapper()
            val requestBody: String = "type=test"
//
//            val client = HttpClient.newBuilder().build();
//            val request = HttpRequest.newBuilder()
//                .uri(URI.create("https://httpbin.org/post"))
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                .build()
//            val response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            println(response.body())
        }





//        @WorkerThread
//        fun post(token: String, url: String, f_body: JSONObject,fParams: JSONObject): JSONObject {}

        @WorkerThread
        fun post(token: String, url: String, f_body: JSONObject = JSONObject(), parseArray: Boolean = false): JSONObject {

            val f_url = URL(url)
            println("\nPOST to, $url; token: ${token}\n")

            with(f_url.openConnection() as HttpsURLConnection) {
                requestMethod = "POST"
                setRequestProperty("Authorization", "OAuth $token")
                var fRavBody = ""
                for (qKey in f_body.keys()){
                    var qData = ""
                    if (parseArray)
                        try{
                            val fArray = f_body.getJSONArray(qKey)
                            for (i in 0 until fArray.length())
                                qData += "${fArray.getString(i)},"
                            qData = qData.removeSuffix(",")

                            }catch (e: Exception)
                            {
                                qData = f_body.getString(qKey)
                            }
                    else
                        qData = f_body.getString(qKey)
                    fRavBody += "$qKey=${qData}&"
                }
                fRavBody = fRavBody.removeSuffix("&")
//                fRavBody = f_body.toString()

                val wr = OutputStreamWriter(outputStream);
                wr.write(fRavBody);
                wr.flush();

                val f_code = responseCode
                val f_resp_msg = responseMessage
                println("\nPOST: $f_url: Code : $responseCode; Msg: $responseMessage")
                if(f_code == 200){
                    var f_res = ""
                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            println(line)
                            f_res += line
                        }
                    }
                    return JSONObject(f_res)
                }else{
                    return JSONObject(mapOf("code" to f_code,
                        "message" to f_resp_msg,"result" to f_resp_msg))
                }
            }

        }

        @WorkerThread
        fun postJSON(token: String, url: String, f_body: JSONObject): JSONObject {

            val f_url = URL(url)
            println("\nPOST to, $url; token: ${token}\n")

            with(f_url.openConnection() as HttpsURLConnection) {
                requestMethod = "POST"
                setRequestProperty("Authorization", "OAuth $token")
                setRequestProperty("Content-Type", "application/json")
                var fRavBody = ""
                fRavBody = f_body.toString()

                val wr = OutputStreamWriter(outputStream);
                wr.write(fRavBody);
                wr.flush();

                val f_code = responseCode
                val f_resp_msg = responseMessage
                println("\nPOST: $f_url: Code : $responseCode; Msg: $responseMessage")
                if(f_code == 200){
                    var f_res = ""
                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            println(line)
                            f_res += line
                        }
                    }
                    return JSONObject(f_res)
                }else{
                    return JSONObject(mapOf("code" to f_code,"message" to f_resp_msg))
                }


            }

        }

        private fun _tryWithHeader(url: String?,token: String?): CompletableFuture<JSONObject>?{
            var fTryes = 0
            while (fTryes < 3){
                try{
                    return _getWithHeader(url,token)
                }catch (e: Exception){
                    e.printStackTrace()
                    Thread.sleep((500 + 1000*fTryes).toLong())
                    fTryes++

                }
            }
            Log.e("yNetwork","error connection after 3 try")
            return null
        }

        @WorkerThread
        private fun _getWithHeader(url: String?,token: String?): CompletableFuture<JSONObject>? {

            val f_url = URL(url)
//            TODO if code = 502 try again
            var f_res = ""
            println("\ngetWithHeaders, $url; token: ${token}\n")
            with(f_url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("accept", "application/json")
                if (token != null) {
                    setRequestProperty("Authorization", "OAuth " + token)
                }

                println("\nSent 'GET' request to URL : $f_url; Response Code : $responseCode")

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        println(line)
                        f_res += line
                    }
                }
            }

            return CompletableFuture.completedFuture(JSONObject(f_res))
        }

        @Throws(
            IOException::class,
            InterruptedException::class,
            JSONException::class
        )
         fun getWithHeaders(url: String?, token: String): CompletableFuture<JSONObject>? {


            return _tryWithHeader(url,token)
        }

        fun getWithHeadersAndToken(url: String?, token: String): CompletableFuture<JSONObject>? {


            return _tryWithHeader(url,token)
        }

        @WorkerThread
        fun getWithHeaders(fClient: yClient,url: String?, authorization: Boolean = true): CompletableFuture<JSONObject>? {


            return _tryWithHeader(url,fClient.m_Token)
        }

        fun get_cover(fCli: yClient,f_adr_part: String,f_size: Int): Bitmap? {
            val f_size_str = "$f_size"+"x$f_size"
            val f_adr_part2 = f_adr_part.replace("%%",f_size_str)
            val f_adr = "https://"+f_adr_part2


            val f_url = URL(f_adr)
            var f_res = ""
            println("\ngetWithHeaders, $f_adr; token: ${fCli.m_Token}\n")
            with(f_url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("accept", "application/json")
                setRequestProperty("Authorization", "OAuth " + fCli.m_Token)


                println("\nSent 'GET' request to URL : $f_url; Response Code : $responseCode")
                val bmp = BitmapFactory.decodeStream(inputStream)
                return bmp
            }
        }

        fun get_stream(fCli: yClient,f_adr_part: String): InputStream? {
//            val f_size_str = "$f_size"+"x$f_size"
//            val f_adr_part2 = f_adr_part.replace("%%",f_size_str)
            val f_adr = "https://"+f_adr_part


            val f_url = URL(f_adr)
            var f_res = ""
            println("\ngetWithHeaders, $f_adr; token: ${fCli.m_Token}\n")
            with(f_url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("accept", "application/json")
                setRequestProperty("Authorization", "OAuth " + fCli.m_Token)


                println("\nSent 'GET' request to URL : $f_url; Response Code : $responseCode")
//                val bmp = BitmapFactory.decodeStream(inputStream)
                return inputStream
            }
        }

        fun get(f_adr: String,fCli: yClient,fParams: JSONObject): JSONObject {
            var fUrl = f_adr
            //val encoded = URLEncoder.encode(fParams.toString())
            var fStrParams = "?"
            for (qKey in fParams.keys()){
                fStrParams += "${qKey}=${fParams.getString(qKey)}&"
            }

            fUrl += fStrParams.removeSuffix("&")
            return get(fUrl,fCli)
        }

        fun get(f_adr: String,fCli: yClient): JSONObject {
            val f_url = URL(f_adr)
            var f_res = ""
            println("\ngetWithHeaders, $f_adr; token: ${fCli.m_Token}\n")
            with(f_url.openConnection() as HttpURLConnection) {
//                requestMethod = "GET"  // optional default is GET
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("accept", "application/json")
                setRequestProperty("Authorization", "OAuth " + fCli.m_Token)
                setRequestProperty("Content-Type", "application/json")
//                setRequestProperty(
//                    "X-Yandex-Music-Client",
//                    X_Yandex_Music_Client
//                )
//                doOutput = true

                println("\nSent 'GET' request to URL : $f_url; Response Code : $responseCode")
                if (responseCode == 200){
                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            kotlin.io.println(line)
                            f_res += line
                        }
                    }
                }else{
                    f_res = "{'code':'$responseCode','message':'$responseMessage'}"
                }


            }

            return JSONObject(f_res)
        }

        @WorkerThread
        fun getXml(fClient: yClient,f_some: String): String {
            val f_url = URL(f_some)
            var f_res = ""
            println("\ngetXML, $f_some; token: ${fClient.m_Token}\n")
            with(f_url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"  // optional default is GET
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("accept", "application/json")
//                if (authorization) {
                setRequestProperty("Authorization", "OAuth " + fClient.m_Token)
//                }

                println("\nSent 'GET' request to URL : $f_url; Response Code : $responseCode")

                if(responseCode!=200){
                    val fRes = "{'code':$responseCode,'msg':'$responseMessage'}"
                    Log.e("yNETWORK",fRes)
                    return fRes
                }

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        println(line)
                        f_res += line
                    }
                }
            }
            return f_res
        }

//        @Throws(
//            IOException::class,
//            NoSuchAlgorithmException::class,
//            InvalidKeyException::class,
//            JSONException::class
//        )
//        fun getDownloadInfoRequest(trackId: Int): CompletableFuture<JSONObject>? {
//            val TIMESTAMP = (System.currentTimeMillis() / 1000).toString()
//
//            // Generate sign
//            val data = trackId.toString() + TIMESTAMP
//            val sha256_HMAC = Mac.getInstance("HmacSHA256")
//            val secret_key = SecretKeySpec(
//                SECRET.toByteArray(StandardCharsets.UTF_8),
//                "HmacSHA256"
//            )
//            sha256_HMAC.init(secret_key)
//            val sign =
//                Base64.encodeBase64String(sha256_HMAC.doFinal(data.toByteArray(StandardCharsets.UTF_8)))
//            val URL =
//                "https://api.music.yandex.net/tracks/$trackId/download-info?can_use_streaming=true&ts=$TIMESTAMP&sign=$sign"
//            val obj = URL(URL)
//            with(obj.openConnection() as HttpURLConnection){
//                requestMethod = "GET"
//                setRequestProperty(
//                    "X-Yandex-Music-Client",
//                    X_Yandex_Music_Client
//                )
//                setRequestProperty("accept", "application/json")
//                setRequestProperty("Authorization", "OAuth " + Token.getToken())
//                doOutput = true
//                val `in` = BufferedReader(InputStreamReader(inputStream))
//                var inputLine: String?
//                val response = StringBuffer()
//                while (`in`.readLine().also { inputLine = it } != null) {
//                    response.append(inputLine)
//                }
//                `in`.close()
//                return CompletableFuture.completedFuture(JSONObject(response.toString()))
//            }


//        }

        fun postDataAndHeaders(s: String, s1: String, b: Boolean): CompletableFuture<JSONObject?> {
            //TODO
            Log.i("YA_TAG","postDataAndHeaders TODO ")
            return CompletableFuture.completedFuture(JSONObject(""))

        }
    }


}