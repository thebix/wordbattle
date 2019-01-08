package wordbattle.mvi

import wordbattle.common.mvi.*

sealed class MainIntention : MviIntention {

    object Init : MainIntention(), MviInitIntention
    data class NextWord(val currentWord: String = "") : MainIntention()
    data class NextTranslation(
        val word: String,
        val currentTranslation: String = "",
        val tryCount: Int = 0
    ) : MainIntention()

    data class Buzz(
        val player: Player,
        val word: String,
        val playerTranslation: String,
        val tryCount: Int
    ) : MainIntention()
}

sealed class MainAction : MviAction {

    object Init : MainAction()
    data class NextWord(val currentWord: String = "") : MainAction()
    data class NextTranslation(
        val word: String,
        val currentTranslation: String = "",
        val tryCount: Int = 0
    ) : MainAction()

    data class Buzz(
        val player: Player,
        val word: String,
        val playerTranslation: String,
        val tryCount: Int
    ) : MainAction()
}

sealed class MainResult : MviResult {

    object FetchStart : MainResult()
    object FetchFinish : MainResult()
    data class NextWord(
        val word: String,
        val nextTranslation: String
    ) : MainResult()

    data class NextTranslation(val translation: String) : MainResult()
    data class PlayerChange(
        val player: Player,
        val state: PlayerState
    ) : MainResult()

    object Error : MainResult()
}

enum class Player {
    One, Two, Three, Four
}

enum class PlayerState {
    RightAnswer, WrongAnswer
}

data class MainState(
    val word: String = "",
    val translation: OneShot<String> = OneShot.empty(),
    val isLoading: Boolean = false,
    val score: Map<Player, Int> = mapOf(
        Pair(Player.One, 0), Pair(Player.Two, 0), Pair(Player.Three, 0), Pair(Player.Four, 0)
    ),
    val tryCount: Int = 0,
    val message: OneShot<String> = OneShot.empty(),
    val fatalError: OneShot<Boolean> = OneShot.empty()

) : MviState, ViewStateWithId()
