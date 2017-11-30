package edu.vanderbilt.crawler.ui.screens.maindrawer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import edu.vanderbilt.crawler.R

class MainDrawerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)

        //val toggle = ActionBarDrawerToggle(
        //        this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        //drawer_layout.addDrawerListener(toggle)
        //toggle.syncState()

        //nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        //if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
        //    drawer_layout.closeDrawer(GravityCompat.START)
        //} else {
            super.onBackPressed()
        //}
    }
}
