package wordbattle.usecase

import io.reactivex.Single
import wordbattle.mvi.Player
import javax.inject.Inject

class BuzzUseCase @Inject constructor() {

    fun execute(player: Player, word: String, playerTranslation: String, tryCount: Int = 0): Single<Boolean> {
        // TODO: implement
        return Single.error(NotImplementedError())
    }
}
