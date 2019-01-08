package wordbattle.repository

import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class GameRepository @Inject constructor() {

    fun words(): Single<List<Word>> {
        // TODO: implement
        return Single.error(NotImplementedError())
    }

    fun fetchWords(): Completable {
        // TODO: load words from remote to repo
        return Completable.error(NotImplementedError())
    }
}

data class Word(

    @SerializedName("text_eng")
    val textEng: String,
    @SerializedName("text_spa")
    val textSpa: String

)
