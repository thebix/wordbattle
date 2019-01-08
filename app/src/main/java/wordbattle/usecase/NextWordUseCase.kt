package wordbattle.usecase

import io.reactivex.Single
import wordbattle.di.MainActivityScope
import wordbattle.repository.GameRepository
import javax.inject.Inject

@MainActivityScope
class NextWordUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    fun execute(currentWord: String = ""): Single<Pair<String, String>> =
        gameRepository.words()
            .map { words ->
                val newWord = words
                    .filter { it.textEng != currentWord }
                    .random().textEng
                val newTranslation = words
                    .random().textSpa
                newWord to newTranslation
            }
}
