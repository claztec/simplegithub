package net.claztec.simplegithub.ui.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import net.claztec.simplegithub.R
import net.claztec.simplegithub.ui.search.SearchActivity
import org.jetbrains.anko.intentFor

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnActivityMainSearch.setOnClickListener {
            startActivity(intentFor<SearchActivity>())
        }
    }

    companion object {

        private val TAG = "MainActivity"
    }
}
