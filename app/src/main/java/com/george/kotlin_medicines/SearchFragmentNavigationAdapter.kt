package com.george.kotlin_medicines

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class SearchFragmentNavigationAdapter(
    private val mContext: Context,
    private var hitsList: ArrayList<String>?,
    private val mSearchClickItemListener: SearchClickItemListener
) :
    RecyclerView.Adapter<SearchFragmentNavigationAdapter.NavigationAdapterViewHolder>() {

    interface SearchClickItemListener {
        fun onListItemClick(
            itemIndex: Int,
            sharedImage: ImageView?,
            type: String?
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        i: Int
    ): NavigationAdapterViewHolder {
        return NavigationAdapterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.search_fragment_adapter, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: NavigationAdapterViewHolder,
        position: Int
    ) {

        //setting the name at the textView
        holder.textViewHolder.text = hitsList!![position]
        holder.imageViewHolder.setImageDrawable(
            mContext.resources.getDrawable(R.drawable.medicine)
        )
    }

    override fun getItemCount(): Int {
        return if (hitsList != null && hitsList!!.size > 0) {
            hitsList!!.size
        } else {
            0
        }
    }

    inner class NavigationAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textViewHolder: TextView
        val imageViewHolder: ImageView
        override fun onClick(view: View) {
            val clickedPosition = adapterPosition
            mSearchClickItemListener.onListItemClick(
                clickedPosition,
                imageViewHolder,
                hitsList!![clickedPosition]
            )
        }

        init {
            textViewHolder = itemView.findViewById(R.id.textViewFragmentAdapter)
            imageViewHolder =
                itemView.findViewById(R.id.imageFragmentAdapter)
            itemView.setOnClickListener(this)
        }
    }

    fun setHitsData(list: ArrayList<String>?) {
        hitsList = list
        /*notifyDataSetChanged();*/
    }

}