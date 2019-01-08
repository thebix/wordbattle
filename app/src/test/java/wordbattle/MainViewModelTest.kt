package wordbattle

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import wordbattle.common.mvi.OneShot
import wordbattle.mvi.*

class MainViewModelTest {

    @Rule
    @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val disposables: CompositeDisposable = CompositeDisposable()

    val word = "Word"
    val translation = "Translation"

    @Before
    fun onStart() {
        // no-op
    }

    @After
    fun tearDown() {
        disposables.clear()
    }

    @Test
    fun `when Init intention and Error result then produce state with Error`() {
        val (result, state) = getStateReducerPair(MainResult.Error, MainState())
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable { MainIntention.Init })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    @Test
    fun `when Init intention and FetchStart result then produce state isLoading true`() {
        val (result, state) = getStateReducerPair(MainResult.FetchStart, MainState())
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable { MainIntention.Init })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    @Test
    fun `when Init intention and FetchFinish result then produce state isLoading false`() {
        val (result, state) = getStateReducerPair(MainResult.FetchFinish, MainState(isLoading = true))
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable { MainIntention.Init })
        )

        testDisposable.run {
            assertNoErrors()
            assertValue(state)
        }
    }

    @Test
    fun `when NextWord intention and Error result then produce state with Error`() {
        val (result, state) = getStateReducerPair(MainResult.Error, MainState())
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable { MainIntention.NextWord(word) })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    @Test
    fun `when NextWord intention and NextWord result then produce state with word, translation and tryCount 0`() {
        val (result, state) = getStateReducerPair(MainResult.NextWord(word, translation), MainState())
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable { MainIntention.NextWord(word) })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    @Test
    fun `when NextTranslation intention and Error result then produce state with Error`() {
        val (result, state) = getStateReducerPair(MainResult.Error, MainState())
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable { MainIntention.NextTranslation(word) })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    @Test
    fun `when NextTranslation intention and NextWord result then produce state with translation and increased tryCount`() {
        val (result, state) = getStateReducerPair(MainResult.NextTranslation(translation), MainState())
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable { MainIntention.NextTranslation(word) })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    @Test
    fun `when Buzz intention and Error result then produce state with Error`() {
        val (result, state) = getStateReducerPair(MainResult.Error, MainState())
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable {
                MainIntention.Buzz(
                    Player.One,
                    word,
                    translation,
                    0
                )
            })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    @Test
    fun `when Buzz intention and PlayerChange result then produce state with score and message`() {
        val (result, state) = getStateReducerPair(
            MainResult.PlayerChange(Player.One, PlayerState.RightAnswer),
            MainState()
        )
        val viewModel = createViewModel { result }

        val testDisposable = viewModel.states().test()
        disposables.addAll(
            testDisposable,
            viewModel.processIntentions(Observable.fromCallable {
                MainIntention.Buzz(
                    Player.One,
                    word,
                    translation,
                    0
                )
            })
        )

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, state)
        }
    }

    // region Helpers

    private fun createViewModel(
        actionsMapper: (MainAction) -> MainResult
    ): MainViewModel {
        val interactor = mock<MainInteractor> {
            on { actionProcessor() } doReturn ObservableTransformer { action ->
                action.map { item -> actionsMapper(item) }
            }
        }

        return MainViewModel(interactor)
    }

    private fun getStateReducerPair(
        result: MainResult,
        prevState: MainState
    ): Pair<MainResult, MainState> {
        val state: MainState =
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
                else -> throw IllegalArgumentException("MainResult isn't implemented: $result")
            }
        return result to state
    }

    // endregion
}
