package net.claztec.simplegithub.ui.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View

import net.claztec.simplegithub.R
import net.claztec.simplegithub.ui.search.SearchActivity

class MainActivity : AppCompatActivity() {

    internal lateinit var btnSearch: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSearch = findViewById(R.id.btnActivityMainSearch)
        btnSearch.setOnClickListener { startActivity(Intent(this@MainActivity, SearchActivity::class.java)) }
    }

    companion object {

        private val TAG = "MainActivity"
    }
}
