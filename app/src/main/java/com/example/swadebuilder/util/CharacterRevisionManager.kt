package com.example.swadebuilder.util

import com.example.swadebuilder.model.PersonagemSnapshot
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CharacterRevision(
    val revisionId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val stageName: String,
    val reason: String,
    val snapshot: PersonagemSnapshot
)

@Serializable
data class CharacterRevisionHistory(
    val characterId: String,
    val revisions: List<CharacterRevision> = emptyList()
)

class CharacterRevisionManager(
    private val maxRevisions: Int = 20
) {
    fun createRevision(
        snapshot: PersonagemSnapshot,
        reason: String,
        stageName: String = "Progresso"
    ): CharacterRevision {
        return CharacterRevision(
            revisionId = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            stageName = stageName,
            reason = reason,
            snapshot = snapshot
        )
    }

    fun addRevision(
        history: CharacterRevisionHistory,
        revision: CharacterRevision
    ): CharacterRevisionHistory {
        val updatedList = (history.revisions + revision).takeLast(maxRevisions)
        return history.copy(revisions = updatedList)
    }

    fun canUndo(history: CharacterRevisionHistory): Boolean {
        return history.revisions.isNotEmpty()
    }

    fun popLastRevision(history: CharacterRevisionHistory): Pair<CharacterRevisionHistory, CharacterRevision?> {
        if (history.revisions.isEmpty()) {
            return history to null
        }
        val lastRevision = history.revisions.last()
        val remaining = history.revisions.dropLast(1)
        return history.copy(revisions = remaining) to lastRevision
    }
}
