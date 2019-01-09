package wordbattle.common.mvi


import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

interface MviIntention
interface MviInitIntention : MviIntention

interface MviAction
interface MviResult
interface MviState

interface MviInteractor<in Action : MviAction, out Result : MviResult> {

    fun actionProcessor(): ObservableTransformer<in Action, out Result>
}

interface MviBaseViewModel<in Intention, State> {

    fun processIntentions(intentions: Observable<out Intention>): Disposable
    fun states(): Observable<State>
}

abstract class MviViewModel<Intention : MviIntention, Action : MviAction, Result : MviResult, State : MviState>(
    private val interactor: MviInteractor<Action, Result>
) : MviBaseViewModel<Intention, State>,
    ViewModel() {

    abstract val defaultState: State
    abstract val reducer: BiFunction<State, Result, State>
    abstract fun actionFromIntention(intent: Intention): Action

    /**
     * Proxy subject used to keep the stream alive even after the UI gets recycled.
     * This is basically used to keep ongoing events and the last cached State alive
     * while the UI disconnects and reconnects on config changes.
     */
    private val intentionsSubject: PublishSubject<Intention> = PublishSubject.create()
    private val statesObservable: Observable<State> = compose()

    /**
     * take only the first ever InitIntention and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter
        get() = ObservableTransformer<Intention, Intention> { shared ->
            Observable.merge(
                listOf(
                    shared.filter { it is MviInitIntention }.take(1),
                    shared.filter { it !is MviInitIntention }
                )
            )
        }

    override fun processIntentions(intentions: Observable<out Intention>): Disposable =
        intentions
            .subscribe(
                { next -> intentionsSubject.onNext(next) },
                { error -> intentionsSubject.onError(error) },
                { intentionsSubject.onComplete() }
            )

    override fun states(): Observable<State> = statesObservable

    private fun compose(): Observable<State> {
        return intentionsSubject
            .compose(intentFilter)
            .map(this::actionFromIntention)
            .compose(interactor.actionProcessor())
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(defaultState, reducer)
            // When a reducer just emits previousState, there's no reason to call render. In fact,
            // redrawing the UI in cases like this can cause jank (e.g. messing up snackbar animations
            // by showing the same snackbar twice in rapid succession).
            .distinctUntilChanged()
            // Emit the last one event of the stream on subscription
            // Useful when a View rebinds to the ViewModel after rotation.
            .replay(1)
            // Create the stream on creation without waiting for anyone to subscribe
            // This allows the stream to stay alive even when the UI disconnects and
            // match the stream's lifecycle to the ViewModel's one.
            .autoConnect(0)
    }
}
