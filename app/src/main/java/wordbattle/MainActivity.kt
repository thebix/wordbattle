package wordbattle

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.babbel.wordbattle.R
import dagger.android.AndroidInjection

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }
}
