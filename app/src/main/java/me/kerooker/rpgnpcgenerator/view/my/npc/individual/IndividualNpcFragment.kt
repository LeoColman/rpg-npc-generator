package me.kerooker.rpgnpcgenerator.view.my.npc.individual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.api.load
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_age
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_alignment
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_avatar
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_gender
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_languages_list
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_motivation
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_name
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_nickname
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_notes
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_personality_list
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_profession
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_race
import kotlinx.android.synthetic.main.mynpcs_individual_fragment.mynpcs_individual_npc_sexuality
import kotlinx.android.synthetic.main.mynpcs_individual_list.view.add_item_button
import kotlinx.android.synthetic.main.mynpcs_individual_list.view.add_item_text
import kotlinx.android.synthetic.main.mynpcs_individual_list.view.list
import kotlinx.android.synthetic.main.mynpcs_individual_list_element.view.individual_npc_field_text
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.databinding.MynpcsIndividualFragmentBinding
import me.kerooker.rpgnpcgenerator.databinding.MynpcsIndividualListElementBinding
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcEntity
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.EditState
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.EditState.EDIT
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.EditState.VIEW
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.IndividualNpcViewModel
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.File

class IndividualNpcFragment : Fragment() {
    
    private val args: IndividualNpcFragmentArgs by navArgs()
    
    private val individualNpcViewModel by inject<IndividualNpcViewModel> { parametersOf(args.npcId) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return MynpcsIndividualFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@IndividualNpcFragment
            npc = individualNpcViewModel.npc
            editState = individualNpcViewModel.editState
        }.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureViewModelObservers()
        configureListeners()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.individual_npc_edit -> individualNpcViewModel.enableEdit()
            R.id.individual_npc_save -> individualNpcViewModel.saveEdit(buildNpc())
            R.id.individual_npc_cancel -> individualNpcViewModel.cancelEdit()
        }
        return true
    }
    
    private fun buildNpc(): NpcEntity {
        return NpcEntity(
            mynpcs_individual_npc_name.text.toString(),
            mynpcs_individual_npc_nickname.text.toString(),
            mynpcs_individual_npc_gender.editText!!.text.toString(),
            mynpcs_individual_npc_sexuality.editText!!.text.toString(),
            mynpcs_individual_npc_race.editText!!.text.toString(),
            mynpcs_individual_npc_age.editText!!.text.toString(),
            mynpcs_individual_npc_profession.editText!!.text.toString(),
            mynpcs_individual_npc_motivation.editText!!.text.toString(),
            mynpcs_individual_npc_alignment.editText!!.text.toString(),
            mynpcs_individual_npc_personality_list.list.elements(),
            mynpcs_individual_npc_languages_list.list.elements(),
            mynpcs_individual_npc_avatar.getImagePath(),
            mynpcs_individual_npc_notes.editText!!.text.toString()
        )
    }
    
    private fun LinearLayout.elements(): List<String> {
        return children.filterIsInstance<LinearLayout>().map { it.individual_npc_field_text.text.toString() }.toList()
    }
    
    private fun configureViewModelObservers() {
        individualNpcViewModel.editState.observe({ this.lifecycle }, { onEditStateChanged(it) })
        individualNpcViewModel.npc.observe({ this.lifecycle }, { onNpcChanged(it) })
    }
    
    private fun onEditStateChanged(state: EditState) {
        requireActivity().invalidateOptionsMenu()
        
        if(state == EditState.VIEW) {
            view!!.invalidate()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuToInflate = when (individualNpcViewModel.editState.value!!) {
            VIEW -> R.menu.mynpcs_individual_viewmode_item_menu
            EDIT -> R.menu.mynpcs_individual_editmode_item_menu
        }
        inflater.inflate(menuToInflate, menu)
    }
    
    private fun onNpcChanged(newNpc: NpcEntity) {
        // Only updating the lists because other views are updated in XML and Data Binding
        // As the lists are dynamic, this is impossible to do in data binding
        mynpcs_individual_npc_languages_list.list.set(
            newNpc.languages,
            getString(R.string.individual_npc_language_hint)
        )
        mynpcs_individual_npc_personality_list.list.set(
            newNpc.personalityTraits,
            getString(R.string.individual_npc_personality_hint)
        )
        setImage(newNpc.imagePath)
    }
    
    private fun LinearLayout.set(items: List<String>, hint: String) {
        removeAllViews()
        items.forEach { 
            MynpcsIndividualListElementBinding.inflate(layoutInflater, this, true).apply {
                lifecycleOwner = this@IndividualNpcFragment
                editState = individualNpcViewModel.editState
                this.hint = hint
                text = it
                individualNpcFieldMinus.setOnClickListener { removeView(root) }
            }
        }
        
        fun onAddClick() {
            MynpcsIndividualListElementBinding.inflate(layoutInflater, this, true).apply {
                lifecycleOwner = this@IndividualNpcFragment
                editState = individualNpcViewModel.editState
                this.hint = hint
                text = ""
                individualNpcFieldMinus.setOnClickListener { removeView(root) }
            }
        }
        
        (parent as ViewGroup).add_item_button.setOnClickListener { onAddClick() }
        (parent as ViewGroup).add_item_text.setOnClickListener { onAddClick() }
        
    }
    
    private fun setImage(imagePath: String?) {
        if(imagePath == null) return
        mynpcs_individual_npc_avatar.load(File(imagePath))
        mynpcs_individual_npc_avatar.setImagePath(imagePath)
    }
    
    private fun configureListeners() {
        mynpcs_individual_npc_avatar.setOnClickListener { 
            if(individualNpcViewModel.editState.value == EditState.EDIT) {
                pickNewImage()
            }
        }
    }
    
    @Suppress("MagicNumber")
    private fun pickNewImage() {
        ImagePicker.with(this)
            .crop(3f, 4f)
            .compress(1024)
            .start { _, data -> 
                setImage(ImagePicker.getFilePath(data))
            }
    }
}

private const val IMAGE_PATH_TAG = 1
private fun ImageView.getImagePath(): String? = this.getTag() as? String
private fun ImageView.setImagePath(path: String) = this.setTag(path)
