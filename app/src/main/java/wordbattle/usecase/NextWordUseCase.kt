package wordbattle.usecase

import io.reactivex.Single
import javax.inject.Inject

class NextWordUseCase @Inject constructor() {

    fun execute(currentWord: String = ""): Single<Pair<String, String>> {
        // TODO: implement
        return Single.error(NotImplementedError())
    }
}
