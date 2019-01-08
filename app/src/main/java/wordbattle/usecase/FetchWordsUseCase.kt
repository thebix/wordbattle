package wordbattle.usecase

import io.reactivex.Completable
import wordbattle.di.MainActivityScope
import wordbattle.repository.GameRepository
import javax.inject.Inject

@MainActivityScope
class FetchWordsUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    fun execute(): Completable =
        gameRepository.fetchWords()
}
