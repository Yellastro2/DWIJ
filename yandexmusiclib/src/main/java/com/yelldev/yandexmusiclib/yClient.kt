package com.yelldev.yandexmusiclib

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.WorkerThread
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yelldev.yandexmusiclib.kot_utils.yNetwork
import com.yelldev.yandexmusiclib.kot_utils.yPlayList
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class yClient(val m_Token: String,val mUserID: String,val mLogin: String = "") {

	val mapper = jacksonObjectMapper()

	companion object {
		val BASE_URL = "https://api.music.yandex.net"
		val BASE_URL_2 = "https://api.music.yandex.ru"

		val TYPE_PLAYLIST = "playlist"
		val TYPE_ARTIST = "artist"
		val TYPE_TRACK = "track"
		val TYPE_ALBUM = "album"
		val TAG = "yClient"
	}

	fun create_playlist(fName: String,fPublic: Boolean = true){
		val url = "$BASE_URL/users/$mUserID/playlists/create"
		val f_args = JSONObject()
		f_args.put("title", fName)
		var fP = "public"
		if(!fPublic) fP = "private"
		f_args.put("visibility", fP)

		yNetwork.post(m_Token,url,f_args)
	}

	class FeedbackType{
		companion object{
			val RADIO_STARTED = "radioStarted"
			val TRACK_STARTED = "trackStarted"
			val TRACK_FINISHED = "trackFinished"
			val SKIP = "skip"
		}
	}

	fun likeAction(
					objectType: String, //`track` - трек, `artist` - исполнитель, `playlist` - плейлист, `album` - альбом.
					ids: String,
					remove: Boolean = false,
					userID: String = mUserID
		): Boolean {
//		Действия с отметкой "Мне нравится".
//
//        Note:
//            Типы объектов: `track` - трек, `artist` - исполнитель, `playlist` - плейлист, `album` - альбом.
//
//            Идентификатор плейлиста указывается в формате `owner_id:playlist_id`. Где `playlist_id` - идентификатор
//            плейлиста, `owner_id` - уникальный идентификатор владельца плейлиста.
//
//        Args:
//            object_type (:obj:`str`): Тип объекта.
//            ids (:obj:`str` | :obj:`int` | :obj:`list` из :obj:`str` | :obj:`list` из :obj:`int`): Уникальный
//                идентификатор объекта или объектов.
//            remove (:obj:`bool`, optional): Если :obj:`True` то снимает отметку, иначе ставит.
//            userID (:obj:`str` | :obj:`int`, optional): Уникальный идентификатор пользователя. Если не указан
//                используется ID текущего пользователя.
//            *args: Произвольные аргументы (будут переданы в запрос).
//            **kwargs: Произвольные именованные аргументы (будут переданы в запрос).
//
//        Returns:
//            :obj:`bool`: :obj:`True` при успешном выполнении запроса, иначе :obj:`False`.
//
//        Raises:
//            :class:`yandex_music.exceptions.YandexMusicError`: Базовое исключение библиотеки.

//		if userID is None and self.me is not None:
//		userID = self.me.account.uid

		val action = if (remove) "remove" else "add-multiple"
		val url = "${BASE_URL}/users/${userID}/likes/${objectType}s/${action}"

		val fParams = JSONObject("{'${objectType}-ids': '$ids' }")
		val result = yNetwork.post(m_Token,url,fParams)
//			self._request.post(url, { f'{object_type}-ids': ids }, *args, ** kwargs)

		if (objectType == "track")
			return (result.has("revision"))

		return (result.has("ok") )
	}

	fun getWaveNextTrack(fStationId: String,
						 fPrevTrack: String,
						 fPrevSecond: Int,
						 fNextTrack: String): JSONObject {
//		Запуск потока по сущности сервиса осуществляется через станцию `<type>:<id>`.
//		Например, станцией для запуска потока по треку будет `track:1234`.

//		settings2 (:obj:`bool`, optional): Использовать ли второй набор настроек.
//		queue (:obj:`str` | :obj:`int` , optional): Уникальной идентификатор трека, который только что был.
//
//		Для продолжения цепочки треков необходимо:
//
//		1. Передавать `ID` трека, что был до этого (первый в цепочки).
//		2. Отправить фидбек о конце или скипе трека, что был передан в `queue`.
//		3. Отправить фидбек о начале следующего трека (второй в цепочки).
//		4. Выполнить запрос получения треков. В ответе придёт новые треки или произойдёт сдвиг цепочки на 1 элемент.

//		Для работы станции надо:
//
//создать сессию через /rotor/session/new и получить radio_session_id и sequence(настраивается с помощью флага)
//оповестить о начале работы станции через /rotor/session/{radio_session_id}/feedback
//оповестить об окончании трека через /rotor/session/{radio_session_id}/feedback
//получить новые треки через /rotor/session/{radio_session_id}/tracks
//Реализация класса Station с использованием новых методов API: https://pastebin.com/dJaHQmTp
//
//		Проход по цепочке до конца не изучен. Часто встречаются дубликаты.
//
//		Все официальные клиенты выполняют запросы с `settings2 = True`.

		val fFbFinish = rotorStationFBTrackFinished(fStationId,
			fTrack = fPrevTrack)

		val fFbStart = rotorStationFBTrackStarted(fStationId, fTrack = fNextTrack)

		val fUrl = "$BASE_URL/rotor/session/${fStationId}/tracks"
		val fParams = JSONObject("{'settings2': 'True'}")
		val fQue = JSONArray()
		fQue.put(fPrevTrack)
		fParams.put("queue",fQue)
		return yNetwork.postJSON(m_Token,fUrl,fParams)
	}

	fun rotorStationFBRadioStarted(fStationId: String,
								   fFrom: String,
								   fBatch: String = "",): JSONObject{
		return feedback(fStationId,FeedbackType.RADIO_STARTED,fFrom=fFrom, fBatch = fBatch)
	}

	fun rotorStationFBTrackStarted(fStationId: String,
								   fTrack: String,
								   fBatch: String = "",): JSONObject{
		return feedback(fStationId,FeedbackType.TRACK_STARTED,fTrack=fTrack, fBatch = fBatch)
	}

	fun rotorStationFBTrackFinished(fStationId: String,
								   	fTrack: String,
								   	fBatch: String = "",): JSONObject{

		val fSeconds: Float = 0.1f
		return feedback(fStationId,FeedbackType.TRACK_FINISHED,fTrack=fTrack,fSeconds=fSeconds,  fBatch = fBatch)
	}

	fun rotorStationFBSkip(fStationId: String,
							fFrom: String,
							fSeconds: Float,
							fBatch: String = "",): JSONObject{
		return feedback(fStationId,FeedbackType.SKIP,fFrom=fFrom,fSeconds=fSeconds,  fBatch = fBatch)
	}

	fun feedback(fStationId: String,
				 fType: String,
				 fTrack: String = "",
				 fFrom: String = "",
				 fSeconds: Float = 0f,
				 fBatch: String = ""): JSONObject {
//		Пример `station`: `user:onyourwave`, `genre:allrock`.
//		Пример `from_`: `mobile-radio-user-123456789`.
//
//		Args:
//		station (:obj:`str`): Станция.
//		type_ (:obj:`str`): Тип отправляемого отзыва.
//		timestamp (:obj:`str` | :obj:`float` | :obj:`int`, optional): Текущее время и дата.
//		from_ (:obj:`str`, optional): Откуда начато воспроизведение радио.
//		batch_id (:obj:`str`, optional): Уникальный идентификатор партии треков. Возвращается при получении треков.
//		total_played_seconds (:obj:`int` |:obj:`float`, optional): Сколько было проиграно секунд трека
//		перед действием.
//		track_id (:obj:`int` | :obj:`str`, optional): Уникальной идентификатор трека.
//		val fTime = (java.time.Instant.now().toEpochMilli()/1000).toInt()
		val fUrl = "$BASE_URL/rotor/session/${fStationId}/feedback"
//		Пример: "2023-05-01T10:35:14.604531+07:00"

		val anotherTIME = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ")
			.withZone(ZoneId.systemDefault())
			.format(Instant.now())
		val fParams = JSONObject()
		fParams.put("type",fType)
		fParams.put("timestamp",anotherTIME)

		if (!fTrack.isNullOrEmpty())
			fParams.put("trackId",fTrack)
		if (fSeconds!=0f)
			fParams.put("totalPlayedSeconds",fSeconds.toString())
		if (fFrom != "")
			fParams.put("from",fFrom)

		val fJson = JSONObject()
		fJson.put("event",fParams)

		val fData = JSONObject()
		if (!fBatch.isNullOrEmpty())
			fData.put("batch-id",fBatch)

		return yNetwork.postJSON(m_Token,fUrl,fJson)

	}

	fun getPlaylistObj(f_id: String, user_id: String? = null): yPlayList {

		val f_json = getPlaylistJSON(f_id,user_id)

		return mapper.readValue(f_json.toString(), yPlayList::class.java)
	}

	@WorkerThread
	fun getPlaylistJSON(f_id: String, user_id: String? = null): JSONObject {
		var f_user_id = ""
		if (user_id == null && mUserID != null) {
			f_user_id = mUserID
		} else if (user_id != null) {
			f_user_id = user_id
		}

		val url = "$BASE_URL/users/$f_user_id/playlists/$f_id"


		val result = yNetwork.get(url, this)
		Log.i("TAG_YAM", result.toString())

		result.getJSONObject("result").getJSONArray("tracks")
		val f_json = result.getJSONObject("result")

		return f_json
	}

	fun getCover(f_adr_part: String,f_size: Int): Bitmap? {
		return yNetwork.get_cover(this@yClient,f_adr_part,f_size)
	}

	fun getStream(f_adr_part: String,f_size: Int): InputStream? {
		return yNetwork.get_stream(this@yClient, f_adr_part, f_size)
	}

	fun getCover(f_cover_data: JSONObject,f_size: Int): Bitmap? {
//		try {
		if(f_cover_data.has("error"))
			return null
		if (f_cover_data.getString("type") == "mosaic") {
			val f_adr_part = f_cover_data
				.getJSONArray("itemsUri")
				.getString(0)
			return yNetwork.get_cover(this@yClient, f_adr_part, f_size)
		}

		return null
	}

	fun getCoverPazle(fCoverData: JSONObject, fSize: Int): Array<Bitmap?>? {
		if(fCoverData.has("error"))
			return null
		val fList = Array(fCoverData.getJSONArray("itemsUri").length()) {
			val qUrl = fCoverData
				.getJSONArray("itemsUri")
				.getString(it)
			return@Array yNetwork.get_cover(this@yClient, qUrl, fSize)
		}
		return fList
	}

	fun likePlaylist(fPlayList: yPlayList){
//		TODO
	}

	fun changePlaylist(fPlList: String, fDif: JSONArray,fRev: Int): JSONObject {
		val url = "$BASE_URL/users/$mUserID/playlists/${fPlList}/change"
		val fData =  JSONObject("{'kind': ${fPlList}, 'revision': $fRev, 'diff': $fDif}")
		return yNetwork.post(m_Token,url,fData)
	}

	fun getRotorList(){
		val fPath = "$BASE_URL/rotor/stations/list"
		val fParam = JSONObject()
		fParam.put("language","ru")

		val result = yNetwork.get(fPath,this,fParam)
		return
	}
	
	fun getLiked(fType: String,fUserP: String = ""): JSONObject {
//		Получение объектов с отметкой "Мне нравится".
//
//        Args:
//            object_type (:obj:`str`): Тип объекта.
//            user_id (:obj:`str` | :obj:`int`, optional): Уникальный идентификатор пользователя. Если не указан
//                используется ID текущего пользователя.
//            params (:obj:`dict`, optional): Параметры, которые будут переданы в запрос.
//            **kwargs (:obj:`dict`, optional): Произвольные аргументы (будут переданы в запрос).
		var fUser = fUserP
		if (fUserP == "")
			fUser = mUserID

        val fUrl = "${BASE_URL}/users/${fUser}/likes/${fType}s"

		val fRes = yNetwork.get(fUrl,this)


//        if object_type == 'track':
//            return TracksList.de_json(result.get('library'), self)
//
//        return Like.de_list(result, self, object_type)
		return fRes
	}

	fun wave(fTag: String): JSONObject {
		val fSess = "$BASE_URL/rotor/session/new"

//		Пример seed: 'track:{track_id}', 'user:onyourwave'
//		'seeds': self.seeds,
//            'includeTracksInResponse': True

		val fJson = JSONObject("{'seeds': ['${fTag}']," +
				"'includeTracksInResponse': 'true'}")

		try{
			val result = yNetwork.postJSON(m_Token,fSess, fJson)
//			{
//    "invocationInfo": {
//        "req-id": "1693419935683119-10174574382569902833",
//        "hostname": "music-stable-back-sas-45.sas.yp-c.yandex.net",
//        "exec-duration-millis": 588
//    },
//    "result": {
//        "radioSessionId": "nnSv-rjiDyHBde6ROLZ-U6XN",
//        "sequence": [{
//            "track": {
//                "id": "74163305",
//                "realId": "74163305",
//                "title": "Thicky",
//                "major": {
//                    "id": 87,
//                    "name": "BELIEVE_DIGITAL"
//                },
//                "available": true,
			val sdfsd =0
			return result
		}catch (e: Exception){
			e.printStackTrace()
		}
		return JSONObject("")
//		url = f'{self.base_url}/rotor/station/{station}/tracks'
//		url = f'{self.base_url}/rotor/stations/list'
	}

	fun users_playlist(
		kind: String,
		user_id: String? = null,
		f_args: Map<String, String>
	): JSONObject {
//		Получение плейлиста или списка плейлистов по уникальным идентификаторам.
//
//        Note:
//            Если передан один `kind`, то вернётся не список плейлистов, а один плейлист.
//
//        Args:
//            kind (:obj:`str` | :obj:`int` | :obj:`list` из :obj:`str` | :obj:`int`): Уникальный идентификатор плейлиста
//                или их список.
//            user_id (:obj:`str` | :obj:`int`, optional): Уникальный идентификатор пользователя владеющим плейлистом.
//            **kwargs (:obj:`dict`, optional): Произвольные аргументы (будут переданы в запрос).
//
//        Returns:
//            :obj:`list` из :obj:`yandex_music.Playlist` | :obj:`yandex_music.Playlist` | :obj:`None`:
//            Список плейлистов или плейлист, иначе :obj:`None`.
//
//        Raises:
//            :class:`yandex_music.exceptions.YandexMusicError`: Базовое исключение библиотеки.
//
		val url = "$BASE_URL/users/$user_id/playlists/$kind"
		val result = yNetwork.get(url,this)
//
		return result
	}

//	if user_id is None and self.me is not None:
//	user_id = self.me.account.uid
//
//	if isinstance(kind, list):
//	url = f'{self.base_url}/users/{user_id}/playlists'
//
//	data = {'kinds': kind}
//
//	result = self._request.post(url, data, *args, **kwargs)
//
//	return Playlist.de_list(result, self)
//	else:
//
//
//	fun users_playlists(kind: Union[List[Union[str, int]], str, int],
//	user_id: Union[str, int] = None,
//	*args,
//	**kwargs,
//	) -> Union[Playlist, List[Playlist]]:
//	"""Получение плейлиста или списка плейлистов по уникальным идентификаторам.
//
//        Note:
//            Если передан один `kind`, то вернётся не список плейлистов, а один плейлист.
//
//        Args:
//            kind (:obj:`str` | :obj:`int` | :obj:`list` из :obj:`str` | :obj:`int`): Уникальный идентификатор плейлиста
//                или их список.
//            user_id (:obj:`str` | :obj:`int`, optional): Уникальный идентификатор пользователя владеющим плейлистом.
//            **kwargs (:obj:`dict`, optional): Произвольные аргументы (будут переданы в запрос).
//
//        Returns:
//            :obj:`list` из :obj:`yandex_music.Playlist` | :obj:`yandex_music.Playlist` | :obj:`None`:
//            Список плейлистов или плейлист, иначе :obj:`None`.
//
//        Raises:
//            :class:`yandex_music.exceptions.YandexMusicError`: Базовое исключение библиотеки.
//        """
//
//	if user_id is None and self.me is not None:
//	user_id = self.me.account.uid
//
//	if isinstance(kind, list):
//	url = f'{self.base_url}/users/{user_id}/playlists'
//
//	data = {'kinds': kind}
//
//	result = self._request.post(url, data, *args, **kwargs)
//
//	return Playlist.de_list(result, self)
//	else:
//	url = f'{self.base_url}/users/{user_id}/playlists/{kind}'
//	result = self._request.get(url, *args, **kwargs)
//
//	return Playlist.de_json(result, self)
//
//
//	@log



	fun getObjList(fType: String,fIds: JSONArray): JSONObject {
//		"""Получение объекта/объектов.
//
//        Args:
//            object_type (:obj:`str`): Тип объекта. ["playlist","artist","track","album"]
//            ids (:obj:`str` | :obj:`int` | :obj:`list` из :obj:`str` | :obj:`list` из :obj:`int`): Уникальный
//                идентификатор объекта или объектов.
//            params (:obj:`dict`, optional): Параметры, которые будут переданы в запрос.
//            **kwargs (:obj:`dict`, optional): Произвольные аргументы (будут переданы в запрос).

//		Note:
//            Идентификатор плейлиста указывается в формате `owner_id:playlist_id`. Где `playlist_id` - идентификатор
//            плейлиста, `owner_id` - уникальный идентификатор владельца плейлиста.
//
//            Данный метод возвращает сокращенную модель плейлиста для отображения больших список.
//
//        Warning:
//            Данный метод не возвращает список треков у плейлиста! Для получения объекта :obj:`yandex_music.Playlist` c
//            заполненным полем `tracks` используйте метод :func:`yandex_music.Client.users_playlists` или
//            метод :func:`yandex_music.Playlist.fetch_tracks`.

//		Для треков ??
//		with_positions (:obj:`bool`, optional): С позициями TODO.

		val fParams = JSONObject()
		fParams.put("${fType}-ids",fIds)
		if(fType== TYPE_TRACK)
			fParams.put("with-positions","True")
//
		val fUrl = "$BASE_URL/${fType}s${if(fType==TYPE_PLAYLIST) "/list" else ""}"
		return yNetwork.post(m_Token,fUrl,fParams,true)
//		 result = self._request.post(url, params, *args, **kwargs)
	}

	fun getUserListPllistsJSON(user_id: String? = null,
						   args: Map<String,String>? = null): JSONArray {
		var f_user_id = ""
		if( user_id == null && mUserID != null) {
			f_user_id = mUserID
		} else if (user_id != null){
			f_user_id = user_id
		}

		val url = "$BASE_URL/users/$f_user_id/playlists/list"

		val result = yNetwork.get(url,this)
		Log.i("TAG_YAM",result.toString())

		return result.getJSONArray("result")
	}


	fun removePlaylist(fKindId: String): Boolean {
		val fUrl = "$BASE_URL/users/$mUserID/playlists/$fKindId/delete"
		val fRes = yNetwork.post(m_Token,fUrl)
		return responseWrapper(fRes)
	}

	fun responseWrapper(fRes: JSONObject): Boolean {
		try {
			val fMsg = fRes.getString("result")
			return fMsg == "ok"
		}catch (e: Exception){
			e.printStackTrace()
			return false
		}

	}




}