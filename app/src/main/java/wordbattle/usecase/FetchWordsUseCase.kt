package wordbattle.usecase

import io.reactivex.Completable
import javax.inject.Inject

class FetchWordsUseCase @Inject constructor() {

    fun execute(): Completable {
        // TODO: implement
        return Completable.error(NotImplementedError())
    }
}
