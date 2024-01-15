package com.warmerhammer.personalnotes.PopupWindow


import android.content.Context
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.warmerhammer.personalnotes.R
import androidx.recyclerview.widget.ListAdapter
import com.warmerhammer.personalnotes.Data.DataClasses.MenuTitle
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class PWindow @Inject constructor(
    @ActivityContext private val context: Context,
    val clickListener: (command: String) -> Unit
) :
    ListAdapter<MenuTitle, PWindow.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<MenuTitle>() {
        override fun areItemsTheSame(oldItem: MenuTitle, newItem: MenuTitle): Boolean {
            return (oldItem.title == newItem.title &&
                    oldItem.showImage == oldItem.showImage)
        }

        override fun areContentsTheSame(oldItem: MenuTitle, newItem: MenuTitle): Boolean {
            return (oldItem.title == newItem.title &&
                    oldItem.showImage == oldItem.showImage)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemTitle: TextView = view.findViewById(R.id.menuItemTitle)
        val photoFrame: CardView = view.findViewById(R.id.iconCircularFrame)
        val photoIcon: ImageView = view.findViewById(R.id.menuImageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PWindow.ViewHolder, pos: Int) {
        val menuItem = currentList[pos]
        holder.itemTitle.text = menuItem.title

        if (menuItem.title == "Sign Out") {
            holder.photoFrame.visibility = View.VISIBLE
            holder.photoIcon.visibility = View.VISIBLE

            Glide
                .with(context)
                .load(menuItem.imageURL)
                .error(R.drawable.ic_baseline_account_circle_24_black)
                .into(holder.photoIcon)

        } else if (menuItem.title == "Sign In") {
            holder.photoFrame.visibility = View.VISIBLE
            holder.photoIcon.visibility = View.VISIBLE

            holder.photoIcon.setImageResource(R.drawable.ic_baseline_account_circle_24_black)
        }

        else {
            holder.photoFrame.visibility = View.GONE
            holder.photoIcon.visibility = View.GONE
        }

        holder.itemTitle.setOnClickListener {
            clickListener(menuItem.title)
        }

    }

    override fun getItemCount() = currentList.size
}