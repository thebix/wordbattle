package wordbattle.usecase

import io.reactivex.Single
import wordbattle.mvi.Player
import wordbattle.repository.GameRepository
import javax.inject.Inject

class BuzzUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    // TODO: check method params
    fun execute(player: Player, word: String, playerTranslation: String, tryCount: Int = 0): Single<Boolean> =
        gameRepository.words()
            .map { words ->
                words.first { it.textEng == word }.textSpa == playerTranslation
            }
}
