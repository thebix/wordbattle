package wordbattle.mvi

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import io.reactivex.functions.BiFunction
import wordbattle.common.mvi.MviViewModel
import wordbattle.common.mvi.OneShot

class MainViewModel(
    interactor: MainInteractor
) : MviViewModel<MainIntention, MainAction, MainResult, MainState>(interactor) {

    override val defaultState: MainState
        get() = MainState()

    override fun actionFromIntention(intent: MainIntention): MainAction {
        return when (intent) {
            MainIntention.Init -> MainAction.Init
            is MainIntention.NextWord -> MainAction.NextWord(intent.currentWord)
            is MainIntention.NextTranslation -> MainAction.NextTranslation(
                intent.word,
                intent.currentTranslation,
                intent.tryCount
            )
            is MainIntention.Buzz -> MainAction.Buzz(
                intent.player,
                intent.word,
                intent.playerTranslation,
                intent.tryCount
            )
        }
    }

    override val reducer: BiFunction<MainState, MainResult, MainState>
        get() = BiFunction { prevState, result ->
            when (result) {
                MainResult.FetchStart -> prevState.copy(
                    isLoading = true
                )
                MainResult.FetchFinish -> prevState.copy(
                    isLoading = false
                )
                MainResult.Error -> prevState.copy(
                    fatalError = OneShot(true)
                )
                is MainResult.PlayerChange -> {
                    val score: MutableMap<Player, Int> = prevState.score.toMutableMap()
                    val message =
                        if (result.state == PlayerState.RightAnswer) {
                            score[result.player] = (score[result.player] ?: 0) + 1
                            // TODO: should be enum with mapping to proper text in view class OR should be part of BE response
                            "Right"
                        } else {
                            score[result.player] = (score[result.player] ?: 0) - 1
                            "Wrong"
                        }
                    prevState.copy(
                        score = score,
                        message = OneShot(message)
                    )
                }
                is MainResult.NextWord -> prevState.copy(
                    word = result.word,
                    translation = OneShot(result.nextTranslation),
                    tryCount = 0
                )
                is MainResult.NextTranslation -> prevState.copy(
                    translation = OneShot(result.translation),
                    tryCount = prevState.tryCount + 1
                )
            }
        }
}

class ViewModelFactory constructor(
    private val interactor: MainInteractor

) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == MainViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(interactor) as T
        }
        throw IllegalArgumentException("unknown ViewModel class $modelClass")
    }
}
