package me.kerooker.rpgnpcgenerator.view.my.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.api.load
import com.lucasurbas.listitemview.ListItemView
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.mynpcs_fragment.my_npcs_recycler
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcEntity
import me.kerooker.rpgnpcgenerator.view.my.npc.MyNpcsAdapter.MyNpcViewHolder
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.MyNpcsViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class MyNpcsFragment : Fragment() {
    
    private val myNpcsViewModel by viewModel<MyNpcsViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.mynpcs_fragment, container, false)
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        configureNpcList()
    }
    
    private fun configureNpcList() {
        my_npcs_recycler.apply {
            adapter = MyNpcsAdapter(emptyList(), { onNpcClick(it) }, { myNpcsViewModel.deleteNpc(it) } )
            itemAnimator = LandingAnimator()
        }
        
        myNpcsViewModel.npcsToDisplay.observe({ this.lifecycle }) {
            val adapter = (my_npcs_recycler.adapter as MyNpcsAdapter)
            adapter.updateNpcList(it)
        }
    }
    
    private fun onNpcClick(npc: NpcEntity) {
        val action = MyNpcsFragmentDirections.actionMyNpcsFragmentToIndividualNpcFragment(npc.id)
        findNavController().navigate(action)
    }
    
}

private class MyNpcsAdapter(
    private var npcsToDisplay: List<NpcEntity>,
    val onNpcClick: (NpcEntity) -> Unit,
    val onNpcDelete: (NpcEntity) -> Unit
) : RecyclerView.Adapter<MyNpcViewHolder>() {
    
    fun updateNpcList(npcList: List<NpcEntity>) {
        val listDiff = calculateDiff(NpcListDiffUtilCallback(npcsToDisplay, npcList))
        npcsToDisplay = npcList
        listDiff.dispatchUpdatesTo(this)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyNpcViewHolder {
        val view = ListItemView(parent.context).apply {
            displayMode = ListItemView.MODE_AVATAR
            avatarView.setImageResource(R.drawable.portrait_placeholder)
            setMultiline(true)
            inflateMenu(R.menu.mynpcs_list_item_menu)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        return MyNpcViewHolder(view)
    }
    
    override fun getItemCount() = npcsToDisplay.size
    
    override fun onBindViewHolder(holder: MyNpcViewHolder, position: Int) {
        holder.setDisplayedNpc(npcsToDisplay[position])
        holder.setOnClickListener { onNpcClick(it) }
        holder.setOnDeleteListener { onNpcDelete(it) }
    }
    
    class MyNpcViewHolder(val view: ListItemView) : ViewHolder(view) {
        
        private lateinit var npc: NpcEntity
        
        @Suppress("MagicNumber")
        fun setDisplayedNpc(entity: NpcEntity) {
            npc = entity
            view.title = "${entity.fullName}, ${entity.nickname}"
            view.subtitle = "${entity.gender} ${entity.race}\n${entity.profession}"
            view.avatarView.scaleType = ScaleType.FIT_CENTER
            view.avatarView.updateLayoutParams { height = (height * 1.33).toInt() }
            if(entity.imagePath != null) {
                view.avatarView.load(File(entity.imagePath))
            }
        }
        
        fun setOnClickListener(block: (NpcEntity) -> Unit) {
            view.setOnClickListener { block(npc) }
        }
        
        fun setOnDeleteListener(block: (NpcEntity) -> Unit) {
            view.setOnMenuItemClickListener { 
                if(it.itemId == R.id.delete) { block(npc) } 
            }
        }
    }
}

private class NpcListDiffUtilCallback(
    private val oldNpcs: List<NpcEntity>,
    private val newNpcs: List<NpcEntity>
) : DiffUtil.Callback() {
    
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldNpcs[oldItemPosition].id == newNpcs[newItemPosition].id
    
    override fun getOldListSize() = oldNpcs.size
    
    override fun getNewListSize() = newNpcs.size
    
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldNpcs[oldItemPosition] == newNpcs[newItemPosition]
}
