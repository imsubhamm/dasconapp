package com.dascon.machinerylog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dascon.machinerylog.data.api.MachineDto
import com.dascon.machinerylog.data.local.Session
import com.dascon.machinerylog.ui.home.HomeScreen
import com.dascon.machinerylog.ui.login.LoginScreen
import com.dascon.machinerylog.ui.logform.LogFormScreen
import com.dascon.machinerylog.ui.theme.DasconTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DasconLogApp).container

        setContent {
            DasconTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var session by remember { mutableStateOf<Session?>(null) }
                    LaunchedEffect(Unit) {
                        container.sessionStore.session.collect { session = it }
                    }

                    val current = session
                    if (current == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        return@Surface
                    }

                    val nav = rememberNavController()
                    var selectedMachine by remember { mutableStateOf<MachineDto?>(null) }
                    val start = if (current.isLoggedIn) "home" else "login"

                    NavHost(navController = nav, startDestination = start) {
                        composable("login") {
                            LoginScreen(
                                repository = container.repository,
                                onLoggedIn = {
                                    nav.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                sessionStore = container.sessionStore,
                                repository = container.repository,
                                onLogout = {
                                    nav.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNewLog = { projectId, _, machine, logDate ->
                                    selectedMachine = machine
                                    nav.navigate("log/$projectId/$logDate")
                                },
                            )
                        }
                        composable(
                            route = "log/{projectId}/{logDate}",
                            arguments = listOf(
                                navArgument("projectId") { type = NavType.LongType },
                                navArgument("logDate") { type = NavType.StringType },
                            ),
                        ) { entry ->
                            val projectId = entry.arguments!!.getLong("projectId")
                            val logDate = entry.arguments!!.getString("logDate")!!
                            val machine = selectedMachine
                            if (machine == null) {
                                nav.popBackStack()
                            } else {
                                LogFormScreen(
                                    repository = container.repository,
                                    sessionStore = container.sessionStore,
                                    projectId = projectId,
                                    machine = machine,
                                    logDate = logDate,
                                    onDone = { msg ->
                                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                        nav.popBackStack()
                                    },
                                    onBack = { nav.popBackStack() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
