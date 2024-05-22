package com.yelldev.yandexmusiclib;

import com.yelldev.yandexmusiclib.exeptions.NoTokenFoundException;
import com.yelldev.yandexmusiclib.kot_utils.yNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Account {
    private static final String BaseUrl = "https://api.music.yandex.net:443";

//    @Async
//    public static CompletableFuture<JSONObject> expiriments() throws IOException, InterruptedException, ExecutionException, NoTokenFoundException, JSONException {
//        if (Objects.equals(Token.getToken(), "")) throw new NoTokenFoundException();
//        String urlToRequest = "/account/experiments";
//        JSONObject result = NetworkManager.getWithHeaders(BaseUrl + urlToRequest, true).get();
//        return CompletableFuture.completedFuture(result);
//    }

//    @Async
//    public static CompletableFuture<JSONObject> promocode(String promocode, String language) throws IOException, ExecutionException, InterruptedException, NoTokenFoundException, JSONException {
//        if (Objects.equals(Token.getToken(), "")) throw new NoTokenFoundException();
//        String urlToRequest = "/account/consume-promo-code";
//        JSONObject result = NetworkManager.postDataAndHeaders(BaseUrl + urlToRequest, "code="+promocode+"&language="+language, true).get();
//        return CompletableFuture.completedFuture(result);
//    }

//    @Async
//    public static CompletableFuture<JSONObject> showSettings() throws IOException, InterruptedException, ExecutionException, NoTokenFoundException, JSONException {
//        if (Objects.equals(Token.getToken(), "")) throw new NoTokenFoundException();
//        String urlToRequest = "/account/settings";
//        JSONObject result = NetworkManager.getWithHeaders(BaseUrl + urlToRequest, true).get();
//        return CompletableFuture.completedFuture(result);
//    }

//    @Async
//    public static CompletableFuture<JSONObject> settingsChange(String data) throws IOException, ExecutionException, InterruptedException, NoTokenFoundException, JSONException {
//        if (Objects.equals(Token.getToken(), "")) throw new NoTokenFoundException();
//        String urlToRequest = "/account/settings";
//        JSONObject result = NetworkManager.postDataAndHeaders(BaseUrl + urlToRequest, data, true).get();
//        return CompletableFuture.completedFuture(result);
//    }



    //{
    //    "invocationInfo": {
    //        "hostname": "music-stable-back-vla-38.vla.yp-c.yandex.net",
    //        "req-id": "1685574752944318-2935723734957964950",
    //        "exec-duration-millis": "59"
    //    },
    //    "result": {
    //        "account": {
    //            "now": "2023-05-31T23:12:33+00:00",
    //            "uid": 1729972566,
    //            "login": "Yellastro2",
    //            "region": 225,
    //            "fullName": "Надточиев Сема",
    //            "secondName": "Надточиев",
    //            "firstName": "Сема",
    //            "displayName": "Yellastro2",
    //            "serviceAvailable": true,
    //            "hostedUser": false,
    //            "passport-phones": [{
    //                "phone": "+79106252959"
    //            }],
    //            "registeredAt": "2023-05-30T22:38:05+00:00",
    //            "child": false,
    //            "nonOwnerFamilyMember": false
    //        },
    //        "permissions": {
    //            "until": "2023-06-01T23:12:33+00:00",
    //            "values": ["landing-play", "feed-play", "radio-play", "mix-play", "play-radio-full-tracks"],
    //            "default": ["landing-play", "feed-play", "radio-play", "mix-play", "play-radio-full-tracks"]
    //        },
    //        "subscription": {
    //            "hadAnySubscription": false,
    //            "canStartTrial": false,
    //            "mcdonalds": false
    //        },
    //        "subeditor": false,
    //        "subeditorLevel": 0,
    //        "pretrialActive": false,
    //        "masterhub": {
    //            "activeSubscriptions": [],
    //            "availableSubscriptions": []
    //        },
    //        "plus": {
    //            "hasPlus": false,
    //            "isTutorialCompleted": false
    //        },
    //        "hasOptions": [],
    //        "defaultEmail": "Yellastro2@yandex.ru",
    //        "userhash": "ddec1ae02e82a9c8fe8cf415235fc08e8aeac84b953d07f0aa880b07f887b2c5"
    //    }
    //}
    public static CompletableFuture<JSONObject> showInformAccount(String fToken) throws IOException, InterruptedException, ExecutionException, NoTokenFoundException, JSONException {
        if (Objects.equals(fToken, "")) throw new NoTokenFoundException();
        String urlToRequest = "/account/status";
        //String f_testUrl = "https://api.music.yandex.net/";

        JSONObject result = yNetwork.Companion.getWithHeadersAndToken(BaseUrl + urlToRequest, fToken).get();
        return CompletableFuture.completedFuture(result);
    }

    public static CompletableFuture<JSONObject> showInformAccountFromToken(
            String token
    ) throws IOException, InterruptedException, ExecutionException, NoTokenFoundException, JSONException {
//        if (Objects.equals(Token.getToken(), "")) throw new NoTokenFoundException();
        String urlToRequest = "/account/status";
        //String f_testUrl = "https://api.music.yandex.net/";

        JSONObject result = yNetwork
                .Companion.getWithHeadersAndToken(BaseUrl + urlToRequest, token).get();
        return CompletableFuture.completedFuture(result);
    }

//    public static CompletableFuture<JSONObject> getLikesTrack(String userId) throws IOException, InterruptedException, ExecutionException, JSONException {
//        String urlToRequest = "/users/" + userId + "/likes/tracks";
//
//        JSONObject result = NetworkManager.getWithHeaders(BaseUrl + urlToRequest, false).get();
//        return CompletableFuture.completedFuture(result);
//    }
//
//    public static CompletableFuture<JSONObject> getDislikesTracks(String userId) throws IOException, InterruptedException, ExecutionException, NoTokenFoundException, JSONException {
//        if (Objects.equals(Token.getToken(), "")) throw new NoTokenFoundException();
//        String urlToRequest = "/users/" + userId + "/dislikes/tracks";
//        JSONObject result = NetworkManager.getWithHeaders(BaseUrl + urlToRequest, true).get();
//        return CompletableFuture.completedFuture(result);
//
//    }
}
