package wordbattle.repository

import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import wordbattle.api.WordApi
import wordbattle.di.MainActivityScope
import javax.inject.Inject

@MainActivityScope
class GameRepository @Inject constructor(
    private val wordApi: WordApi
) {

    private var items: List<Word> = listOf()

    fun words(): Single<List<Word>> =
        Single.fromCallable { items }

    fun fetchWords(): Completable =
        wordApi.words()
            .doOnSuccess { words ->
                items = words
            }
            .ignoreElement()
}

data class Word(

    @SerializedName("text_eng")
    val textEng: String,
    @SerializedName("text_spa")
    val textSpa: String

)
