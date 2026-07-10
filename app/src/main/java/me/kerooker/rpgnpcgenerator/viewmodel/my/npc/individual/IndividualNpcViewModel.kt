package me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.image.GeneratePortraitWorker
import me.kerooker.rpgnpcgenerator.ui.util.ImageStore

class IndividualNpcViewModel(
    private val npcId: Long,
    private val npcRepository: NpcRepository,
    private val appContext: Context
) : ViewModel() {

    val npc: StateFlow<Npc?> = npcRepository.get(npcId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    private val _editState = MutableStateFlow(EditState.VIEW)
    val editState: StateFlow<EditState> = _editState.asStateFlow()

    // The portrait shown when the user entered edit mode. Lets saveEdit tell a portrait the user
    // actually picked apart from one a background render produced while they were editing.
    private var editing = false
    private var imageWhenEditingStarted: String? = null

    fun enableEdit() {
        imageWhenEditingStarted = npc.value?.imagePath
        editing = true
        _editState.value = EditState.EDIT
    }

    fun cancelEdit() {
        editing = false
        _editState.value = EditState.VIEW
    }

    fun saveEdit(edited: Npc) {
        // "Changed" only if the user re-picked the portrait during an edit session; a stale draft value
        // that still matches the pre-edit image is NOT a change (a background render may have replaced it).
        val userChangedPortrait = editing && edited.imagePath != imageWhenEditingStarted
        editing = false
        _editState.value = EditState.VIEW
        viewModelScope.launch(Dispatchers.IO) {
            val latest = npcRepository.get(npcId).firstOrNull() ?: return@launch
            // Honor the user's pick if they changed the portrait; otherwise keep whatever the DB has
            // now, so a portrait generated in the background isn't clobbered by the stale draft value.
            val finalImage = if (userChangedPortrait) edited.imagePath else latest.imagePath
            val merged = edited.copy(id = npcId, imagePath = finalImage)

            npcRepository.update(merged)
            // If the previous portrait is no longer referenced, remove the now-orphaned file.
            if (!latest.imagePath.isNullOrBlank() && latest.imagePath != finalImage) {
                ImageStore.deletePortrait(appContext, latest.imagePath)
            }
        }
    }

    fun delete() {
        val image = npc.value?.imagePath
        viewModelScope.launch(Dispatchers.IO) {
            GeneratePortraitWorker.cancel(appContext, npcId)
            npcRepository.delete(npcId)
            if (!image.isNullOrBlank()) ImageStore.deletePortrait(appContext, image)
        }
    }

    /**
     * Fire-and-forget: queues a background portrait render for this NPC. The worker submits to the
     * server queue, notifies on completion, and writes the image back to the NPC — which this
     * screen picks up automatically via [npc]. Safe to leave the screen after calling.
     */
    fun generatePortrait() {
        GeneratePortraitWorker.enqueue(appContext, npcId)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

enum class EditState {
    VIEW, EDIT
}
