package wordbattle.usecase

import io.reactivex.Single
import javax.inject.Inject

class NextTranslationUseCase @Inject constructor() {

    fun execute(word: String, currentTranslation: String = "", tryCount: Int = 0): Single<String> {
        // TODO: implement
        return Single.error(NotImplementedError())
    }
}
