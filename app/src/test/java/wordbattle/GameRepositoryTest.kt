package wordbattle

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
import wordbattle.api.WordApi
import wordbattle.repository.GameRepository
import wordbattle.repository.Word

class GameRepositoryTest {

    @Rule
    @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val word = "Word"
    private val translation = "Translation"

    private val disposables = CompositeDisposable()

    private lateinit var repository: GameRepository
    @Mock
    private lateinit var throwable: Throwable
    @Mock
    private lateinit var wordApi: WordApi

    @Before
    fun onStart() {
        repository = GameRepository(wordApi)
    }

    @After
    fun tearDown() {
        disposables.clear()
    }

    @Test
    fun `when words then emit words list`() {
        val wordsList = listOf(Word(word, translation), Word("testWord", "testTranslation"))
        whenever(wordApi.words())
            .thenReturn(Single.fromCallable { wordsList })

        val testSubscription = repository.fetchWords()
            .andThen(repository.words())
            .test()
        disposables.add(
            testSubscription
        )

        testSubscription.assertValue(wordsList)
    }

    @Test
    fun `when fetchWords success then complete`() {
        val wordsList = listOf(Word(word, translation), Word("testWord", "testTranslation"))
        whenever(wordApi.words())
            .thenReturn(Single.fromCallable { wordsList })

        val testSubscription = repository.fetchWords()
            .test()
        disposables.add(
            testSubscription
        )

        testSubscription.assertComplete()
    }

    @Test
    fun `when fetchWords error then error`() {
        whenever(wordApi.words())
            .thenReturn(Single.error(throwable))

        val testSubscription = repository.fetchWords()
            .test()
        disposables.add(
            testSubscription
        )

        testSubscription.assertError(throwable)
    }

}
