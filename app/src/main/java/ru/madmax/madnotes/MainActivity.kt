package ru.madmax.madnotes

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.madmax.madnotes.databinding.ActivityMainBinding


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 200L
                slideUp.doOnEnd { splashScreenView.remove() }
                slideUp.start()
            }
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.bottom_notes,
                R.id.bottom_categories,
                R.id.bottom_reminders,
                R.id.bottom_settings
            ),
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        binding.mainToolbar.setupWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.createNoteFragment
                || destination.id == R.id.createCategoryFragment
                || destination.id == R.id.createReminderFragment
                || destination.id == R.id.searchFragment
            ) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
            val isTopLevelDestination =
                appBarConfiguration.topLevelDestinations.contains(destination.id)
            if (!isTopLevelDestination) {
                binding.mainToolbar.setNavigationIcon(
                    R.drawable.ic_back_arrow
                )
                binding.mainToolbar.setNavigationIconTint(
                    ContextCompat.getColor(
                        this,
                        R.color.back_arrow_tint
                    )
                )
            }
        }

        binding.bottomNavigationView.setupWithNavController(navController)
    }

}