package wordbattle

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import wordbattle.repository.GameRepository
import wordbattle.repository.Word
import wordbattle.usecase.BuzzUseCase

class BuzzUseCaseTest {

    @Rule
    @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()
    private val disposables = CompositeDisposable()

    private val word = "Word"
    private val translation = "Translation"

    private lateinit var buzzUseCase: BuzzUseCase
    @Mock
    private lateinit var throwable: Throwable
    @Mock
    private lateinit var repository: GameRepository

    @Before
    fun onStart() {
        buzzUseCase = BuzzUseCase(repository)
    }

    @After
    fun tearDown() {
        disposables.clear()
    }

    @Test
    fun `when words repository error then throw error`() {
        whenever(repository.words())
            .thenReturn(Single.error(throwable))

        val testDisposable = buzzUseCase.execute(word, translation)
            .test()
        disposables.add(
            testDisposable
        )

        verify(repository).words()
        verifyNoMoreInteractions(repository)
        testDisposable.assertError(throwable)
    }

    @Test
    fun `when words repository success and right answer then return proper result`() {
        whenever(repository.words())
            .thenReturn(Single.fromCallable {
                listOf(Word(word, translation), Word("testWord", "testTranslation"))
            })

        val testDisposable = buzzUseCase.execute(word, translation)
            .test()
        disposables.add(
            testDisposable
        )

        verify(repository).words()
        verifyNoMoreInteractions(repository)
        testDisposable.run {
            assertValue(true)
        }
    }

    @Test
    fun `when words repository success and wrong answer then return proper result`() {
        whenever(repository.words())
            .thenReturn(Single.fromCallable {
                listOf(Word(word, "testTranslation"), Word("testWord", translation))
            })

        val testDisposable = buzzUseCase.execute(word, translation)
            .test()
        disposables.add(
            testDisposable
        )

        verify(repository).words()
        verifyNoMoreInteractions(repository)
        testDisposable.run {
            assertValue(false)
        }
    }
}
