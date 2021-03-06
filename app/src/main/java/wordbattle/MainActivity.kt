package wordbattle

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.babbel.wordbattle.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import wordbattle.mvi.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * Intentions, Actions, Result and State are declared in MainIARS.kt
 *
 * MainActivity sends user Intention (ex, button tap) to the viewModel.processIntentions() method (in ViewModel's base class ).
 * viewModel.processIntentions() sends Intention to the compose() method of base class.
 * compose() method connects all logic of data flow.
 *
 * compose() method filters initial intention, since this intention should pass the stream only once.
 * Then compose() method do mapping from Intention to Action in actionFromIntention() method of MainViewModel class.
 * This action is sent to the interactor.actionProcessor() method of MainInteractor class. MainInteractor class is the class where
 * all side effects are happened for MainActivity.
 *
 * After applying business logic in Interactor it results with MainResult. This result is sent back to MainViewModel's reducer.
 *
 * Reducer creates new view state based on previous state and incoming result and sends this state to the MainActivity.
 *
 * View (MainActivity) has two points of data interaction:  intentions() - outgoing, render() - incoming
 * ViewModel (MainViewModel) has two points of data interaction: actionFromIntention() - outgoing, reducer - incoming
 */

class MainActivity : AppCompatActivity() {

    private val intentionSubject = PublishSubject.create<MainIntention>()
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val viewModel: MainViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, ViewModelFactory(interactor))
            .get(MainViewModel::class.java)
    }

    @Inject
    lateinit var interactor: MainInteractor
    private var isGameRunning = false
    private var isBuzzed = false
    private var currentWord: String = ""
    private var currentTranslation: String = ""
    private var currentTryCount: Int = 0
    private lateinit var buzz1: TextView
    private lateinit var buzz2: TextView
    private lateinit var buzz3: TextView
    private lateinit var buzz4: TextView
    private lateinit var word1: TextView
    private lateinit var word2: TextView
    private lateinit var translation1: TextView
    private lateinit var translation2: TextView
    private lateinit var score1: TextView
    private lateinit var score2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        buzz1 = findViewById(R.id.main_player1)
        buzz2 = findViewById(R.id.main_player2)
        buzz3 = findViewById(R.id.main_player3)
        buzz4 = findViewById(R.id.main_player4)
        word1 = findViewById(R.id.main_word1)
        word2 = findViewById(R.id.main_word2)
        translation1 = findViewById(R.id.main_translation1)
        translation2 = findViewById(R.id.main_translation2)
        score1 = findViewById(R.id.main_score1)
        score2 = findViewById(R.id.main_score2)
    }

    override fun onStart() {
        super.onStart()
        disposables.addAll(
            viewModel.states()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::render),
            viewModel.processIntentions(intentions())
        )
    }

    override fun onStop() {
        translation1.clearAnimation()
        translation2.clearAnimation()
        word1.clearAnimation()
        word2.clearAnimation()
        disposables.clear()
        super.onStop()
    }

    /**
     * This is a single point for getting state of View
     */
    @UiThread
    private fun render(state: MainState) {
        Timber.d("State: $state")
        with(state) {
            fatalError.get(this)?.let {
                Toast.makeText(applicationContext, R.string.main_fatal_error, Toast.LENGTH_LONG)
                    .show()
            }

            currentWord = word
            if (word.isNotBlank()) {
                word1.visibility = VISIBLE
                word2.visibility = VISIBLE
                word1.text = word
                word2.text = word
            } else {
                word1.visibility = GONE
                word2.visibility = GONE
            }

            translation.get(this)?.let {
                isBuzzed = false
                showTranslation(it)
                currentTranslation = it
            }

            currentTryCount = tryCount

            // TODO: show loader
            score1.text = score.values.toString()
            score2.text = score.values.toString()

            message.get(this)?.let {
                showMessage(it)
            }
        }
    }

    private fun intentions() = Observable.merge(
        listOf(
            Observable.fromCallable { MainIntention.Init },
            intentionSubject,
            RxView.clicks(buzz1)
                .filter { isBuzzed.not() }
                .map {
                    buzzIntention(Player.One)
                },
            RxView.clicks(buzz2)
                .filter { isBuzzed.not() }
                .map {
                    buzzIntention(Player.Two)
                },
            RxView.clicks(buzz3)
                .filter { isBuzzed.not() }
                .map {
                    buzzIntention(Player.Three)
                },
            RxView.clicks(buzz4)
                .filter { isBuzzed.not() }
                .map {
                    buzzIntention(Player.Four)
                }
        )
    )

    private fun buzzIntention(player: Player) = if (isGameRunning) {
        isBuzzed = true
        translation1.clearAnimation()
        translation2.clearAnimation()
        MainIntention.Buzz(
            player,
            currentWord,
            currentTranslation,
            currentTryCount
        )
    } else {
        isGameRunning = true
        buzz1.text = getString(R.string.main_buzz)
        buzz2.text = getString(R.string.main_buzz)
        buzz3.text = getString(R.string.main_buzz)
        buzz4.text = getString(R.string.main_buzz)
        MainIntention.NextWord()
    }

    private fun showTranslation(translation: String) {
        translation1.clearAnimation()
        translation2.clearAnimation()
        translation1.text = translation
        translation2.text = translation
        val move1 = AnimationUtils.loadAnimation(applicationContext, R.anim.move1)
        val move2 = AnimationUtils.loadAnimation(applicationContext, R.anim.move2)
        val fadeIn = AlphaAnimation(0f, 1f)
        val fadeOut = AlphaAnimation(1f, 0f)
        // TODO: move to companion object const
        fadeIn.duration = 500
        fadeOut.duration = 500
        val randomDuration = Random.nextLong(1000, 3000)
        move1.duration = randomDuration
        move2.duration = randomDuration
        fadeOut.startOffset = randomDuration - 500

        val animationSet1 = AnimationSet(true)
        animationSet1.addAnimation(move1)
        animationSet1.addAnimation(fadeIn)
        animationSet1.addAnimation(fadeOut)
        val animationSet2 = AnimationSet(true)
        animationSet2.addAnimation(move2)
        animationSet2.addAnimation(fadeIn)
        animationSet2.addAnimation(fadeOut)

        translation1.animation = animationSet1
        translation2.animation = animationSet2

        animationSet1.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    // no-op
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (isBuzzed.not()) {
                        intentionSubject.onNext(
                            MainIntention.NextTranslation(
                                currentWord,
                                currentTranslation,
                                currentTryCount
                            )
                        )
                    }
                }

                override fun onAnimationStart(animation: Animation?) {
                    // no-op
                }
            }
        )
    }

    private fun showMessage(message: String) {
        word1.text = message
        word2.text = message
        word1.clearAnimation()
        word2.clearAnimation()
        val scale = AnimationUtils.loadAnimation(applicationContext, R.anim.scale)
        // TODO: to const
        scale.duration = 600

        scale.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    // no-op
                }

                override fun onAnimationEnd(animation: Animation?) {
                    intentionSubject.onNext(MainIntention.NextWord(currentWord))
                }

                override fun onAnimationStart(animation: Animation?) {
                    // no-op
                }
            }
        )
        word1.animation = scale
        word2.animation = scale
    }
}
