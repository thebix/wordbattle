package wordbattle.mvi

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import timber.log.Timber
import wordbattle.common.mvi.MviInteractor
import wordbattle.di.IoScheduler
import wordbattle.usecase.BuzzUseCase
import wordbattle.usecase.FetchWordsUseCase
import wordbattle.usecase.NextTranslationUseCase
import wordbattle.usecase.NextWordUseCase
import java.io.IOException
import javax.inject.Inject

class MainInteractor @Inject constructor(
    private val buzzUseCase: BuzzUseCase,
    private val fetchWordsUseCase: FetchWordsUseCase,
    private val nextWordUseCase: NextWordUseCase,
    private val nextTranslationUseCase: NextTranslationUseCase,
    @IoScheduler private val ioScheduler: Scheduler
) : MviInteractor<MainAction, MainResult> {

    override fun actionProcessor(): ObservableTransformer<in MainAction, out MainResult> {
        return ObservableTransformer { actions ->
            actions
                .publish { shared ->
                    Observable.merge(
                        shared.ofType(MainAction.Init::class.java)
                            .compose(initProcessor),
                        shared.ofType(MainAction.NextWord::class.java)
                            .compose(nextWordProcessor),
                        shared.ofType(MainAction.NextTranslation::class.java)
                            .compose(nextTranslationProcessor),
                        shared.ofType(MainAction.Buzz::class.java)
                            .compose(buzzProcessor)
                    )
                }
        }
    }

    private val initProcessor: ObservableTransformer<in MainAction, MainResult> =
        ObservableTransformer { actions ->
            actions
                .switchMap {
                    fetchWordsUseCase.execute()
                        .subscribeOn(ioScheduler)
                        .toSingleDefault(MainResult.FetchFinish as MainResult)
                        .toObservable()
                        .doOnError { error ->
                            if (error is IOException) {
                                Timber.d(error)
                            } else {
                                Timber.e(error)
                            }
                        }
                        .onErrorReturnItem(MainResult.Error)
                        .startWith(MainResult.FetchStart)
                }
        }

    private val nextWordProcessor: ObservableTransformer<in MainAction.NextWord, MainResult> =
        ObservableTransformer { actions ->
            actions
                .switchMap {
                    nextWordUseCase.execute(it.currentWord)
                        .map { (word, translation) ->
                            MainResult.NextWord(word, translation) as MainResult
                        }
                        .doOnError { error -> Timber.e(error) }
                        .onErrorReturnItem(MainResult.Error)
                        .toObservable()
                }
        }

    private val nextTranslationProcessor: ObservableTransformer<in MainAction.NextTranslation, MainResult> =
        ObservableTransformer { actions ->
            actions
                .switchMap {
                    nextTranslationUseCase.execute(it.word, it.currentTranslation, it.tryCount)
                        .map { nextTranslation ->
                            MainResult.NextTranslation(nextTranslation) as MainResult
                        }
                        .doOnError { error -> Timber.e(error) }
                        .onErrorReturnItem(MainResult.Error)
                        .toObservable()
                }
        }

    private val buzzProcessor: ObservableTransformer<in MainAction.Buzz, MainResult> =
        ObservableTransformer { actions ->
            actions
                .switchMap {
                    buzzUseCase.execute(it.word, it.playerTranslation)
                        .map { isRight ->
                            MainResult.PlayerChange(
                                it.player,
                                if (isRight) PlayerState.RightAnswer else PlayerState.WrongAnswer
                            ) as MainResult
                        }
                        .doOnError { error -> Timber.e(error) }
                        .onErrorReturnItem(MainResult.Error)
                        .toObservable()
                }
        }
}
