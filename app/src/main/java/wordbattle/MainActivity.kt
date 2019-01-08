package wordbattle

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v7.app.AppCompatActivity
import com.babbel.wordbattle.R
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import wordbattle.mvi.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val intentionSubject = PublishSubject.create<MainIntention>()
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val viewModel: MainViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, ViewModelFactory(interactor))
            .get(MainViewModel::class.java)
    }

    @Inject
    lateinit var interactor: MainInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

    override fun onStart() {
        super.onStart()
        disposables.addAll(
            viewModel.states()
                .subscribe(::render),
            viewModel.processIntentions(intentions())
        )
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }


    @UiThread
    private fun render(state: MainState) {
        Timber.d("State: $state")
        with(state) {
            // TODO: implement
        }
    }

    private fun intentions() = Observable.merge(
        Observable.fromCallable { MainIntention.Init },
        intentionSubject
    )
}
