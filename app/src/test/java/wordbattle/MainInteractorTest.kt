package wordbattle

import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import wordbattle.mvi.MainAction
import wordbattle.mvi.MainInteractor
import wordbattle.mvi.MainResult
import wordbattle.usecase.BuzzUseCase
import wordbattle.usecase.FetchWordsUseCase
import wordbattle.usecase.NextTranslationUseCase
import wordbattle.usecase.NextWordUseCase

class MainInteractorTest {

    @Rule
    @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val word = "Word"
    private val newWord = "NewWord"
    private val translation = "Translation"

    private val scheduler = Schedulers.trampoline()
    private val disposables = CompositeDisposable()
    private lateinit var interactor: MainInteractor

    @Mock
    private lateinit var throwable: Throwable
    @Mock
    private lateinit var buzzUseCase: BuzzUseCase
    @Mock
    private lateinit var fetchWordsUseCase: FetchWordsUseCase
    @Mock
    private lateinit var nextWordUseCase: NextWordUseCase
    @Mock
    private lateinit var nextTranslationUseCase: NextTranslationUseCase

    @Before
    fun onStart() {
        interactor = MainInteractor(
            buzzUseCase,
            fetchWordsUseCase,
            nextWordUseCase,
            nextTranslationUseCase,
            scheduler
        )
    }

    @After
    fun tearDown() {
        disposables.clear()
    }

    @Test
    fun `when Init action and fetchWordsUseCase Error then Error result`() {
        whenever(fetchWordsUseCase.execute())
            .thenReturn(Completable.error(throwable))

        val testDisposable = getInteractorActionProcessor(MainAction.Init).test()
        disposables.add(testDisposable)

        testDisposable.run {
            assertNoErrors()
            assertValueAt(1, MainResult.Error)
        }
        verifyZeroInteractions(
            buzzUseCase,
            nextWordUseCase,
            nextTranslationUseCase
        )
    }

    @Test
    fun `when Init action and fetchWordsUseCase success then start and finish fetching results`() {
        whenever(fetchWordsUseCase.execute())
            .thenReturn(Completable.complete())

        val testDisposable = getInteractorActionProcessor(MainAction.Init).test()
        disposables.add(testDisposable)

        testDisposable.run {
            assertNoErrors()
            assertValues(
                MainResult.FetchStart,
                MainResult.FetchFinish
            )
        }
        verifyZeroInteractions(
            buzzUseCase,
            nextWordUseCase,
            nextTranslationUseCase
        )
    }

    @Test
    fun `when NextWord action and nextWordUseCase Error then Error result`() {
        whenever(nextWordUseCase.execute(word))
            .thenReturn(Single.error(throwable))

        val testDisposable = getInteractorActionProcessor(MainAction.NextWord(word)).test()
        disposables.add(testDisposable)

        testDisposable.run {
            assertNoErrors()
            assertValue(MainResult.Error)
        }
        verifyZeroInteractions(
            buzzUseCase,
            fetchWordsUseCase,
            nextTranslationUseCase
        )
    }

    @Test
    fun `when NextWord action and nextWordUseCase success then NextWord result`() {
        whenever(nextWordUseCase.execute(word))
            .thenReturn(Single.fromCallable { Pair(newWord, translation) })

        val testDisposable = getInteractorActionProcessor(MainAction.NextWord(word)).test()
        disposables.add(testDisposable)

        testDisposable.run {
            assertNoErrors()
            assertValues(
                MainResult.NextWord(newWord, translation)
            )
        }
        verifyZeroInteractions(
            buzzUseCase,
            fetchWordsUseCase,
            nextTranslationUseCase
        )
    }

    // TODO: the same couples for MainAction.NextTranslation and MainAction.Buzz

    // region Helpers

    private fun getInteractorActionProcessor(action: MainAction): Observable<MainResult> {
        return Observable.fromCallable { action }
            .compose(interactor.actionProcessor())
    }

    // endregion
}
