package it.unibo.jakta.examples.tictactoe

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.examples.tictactoe.databinding.ActivityMainBinding
import it.unibo.jakta.examples.tictactoe.model.ticTacToe

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        //val navController = findNavController(R.id.nav_host_fragment_content_main)
        //appBarConfiguration = AppBarConfiguration(navController.graph)
        //setupActionBarWithNavController(navController, appBarConfiguration)

        val startBtn = findViewById<Button>(R.id.start_btn)
        val tv = findViewById<TextView>(R.id.gridBtn)
        val lv = findViewById<TextView>(R.id.logText)
        val gridSize = findViewById<EditText>(R.id.gridSize)

        startBtn.setOnClickListener {
            when (startBtn.text) {
                "Start" -> {
                    val system = ticTacToe(Integer.parseInt(gridSize.text.toString()), tv, lv)
                    tv.text = ""
                    lv.text = ""
                    gridSize.isEnabled = false
                    tv.text = "Started"
                    startBtn.text = "Stop"
                    system.start()
                }
                "Stop" -> {
                    tv.text = ""
                    lv.text = ""
                    gridSize.isEnabled = true
                    startBtn.text = "Start"
                }
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}