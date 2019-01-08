package wordbattle.usecase

import io.reactivex.Single
import wordbattle.repository.GameRepository
import javax.inject.Inject
import kotlin.random.Random

class NextTranslationUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    fun execute(word: String, currentTranslation: String = "", tryCount: Int = 0): Single<String> {
        // TODO: move const to companion object
        val isRightAnswerRequired = tryCount > Random.nextInt(3, 6)
        return gameRepository.words()
            .map { words ->
                if (isRightAnswerRequired) {
                    val translation = words.first { it.textEng == word }.textSpa
                    if (translation != currentTranslation) {
                        return@map translation
                    }
                }
                words
                    .filter { it.textSpa != currentTranslation }
                    .random().textSpa
            }
    }
}
