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

    private val intentionsSubject: PublishSubject<Intention> = PublishSubject.create()
    private val statesObservable: Observable<State> = compose()

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
            .scan(defaultState, reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }
}
