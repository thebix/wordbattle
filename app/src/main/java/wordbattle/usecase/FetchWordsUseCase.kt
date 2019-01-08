package wordbattle.usecase

import io.reactivex.Completable
import wordbattle.repository.GameRepository
import javax.inject.Inject

class FetchWordsUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    fun execute(): Completable =
        gameRepository.fetchWords()
}
