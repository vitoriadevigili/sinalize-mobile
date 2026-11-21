package br.com.sinalize.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.sinalize.ui.dicionario.DicionarioScreen
import br.com.sinalize.ui.home.HomeScreen
import br.com.sinalize.ui.quiz.QuizScreen

/** Rotas equivalentes às do App.tsx ("/", "/quiz", "/dictionary"). */
object Rotas {
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val DICIONARIO = "dicionario"
}

@Composable
fun SinalizeNavHost(navController: NavHostController = rememberNavController()) {
    val voltarAoMenu: () -> Unit = { navController.popBackStack(Rotas.HOME, inclusive = false) }

    NavHost(navController = navController, startDestination = Rotas.HOME) {
        composable(Rotas.HOME) {
            HomeScreen(
                onJogar = { navController.navigate(Rotas.QUIZ) { launchSingleTop = true } },
                onDicionario = { navController.navigate(Rotas.DICIONARIO) { launchSingleTop = true } },
            )
        }
        composable(Rotas.QUIZ) { QuizScreen(onVoltarAoMenu = voltarAoMenu) }
        composable(Rotas.DICIONARIO) { DicionarioScreen(onVoltarAoMenu = voltarAoMenu) }
    }
}
