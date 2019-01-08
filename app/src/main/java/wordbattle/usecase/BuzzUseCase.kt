package wordbattle.usecase

import io.reactivex.Single
import wordbattle.di.MainActivityScope
import wordbattle.repository.GameRepository
import javax.inject.Inject

@MainActivityScope
class BuzzUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    fun execute(word: String, playerTranslation: String): Single<Boolean> =
        gameRepository.words()
            .map { words ->
                words.first { it.textEng == word }.textSpa == playerTranslation
            }
}
