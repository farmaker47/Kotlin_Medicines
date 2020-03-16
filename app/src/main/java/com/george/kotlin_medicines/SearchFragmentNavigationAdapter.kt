package com.george.kotlin_medicines

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.george.kotlin_medicines.databinding.SearchFragmentAdapterBinding
import java.util.*

class SearchFragmentNavigationAdapter(
    val mContext: Context,
    private var hitsList: ArrayList<String>?,
    private val mSearchClickItemListener: SearchClickItemListener
) :
    RecyclerView.Adapter<SearchFragmentNavigationAdapter.NavigationAdapterViewHolder>() {

    private lateinit var binding: SearchFragmentAdapterBinding

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

        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.search_fragment_adapter,
            parent,
            false
        )

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
        holder.bind(position)
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
            imageViewHolder = itemView.findViewById(R.id.imageFragmentAdapter)
            itemView.setOnClickListener(this)
        }

        fun bind(
            position: Int
        ) {
            textViewHolder.text = hitsList!![position]
            imageViewHolder.setImageResource(R.drawable.medicine)
        }
    }

    fun setHitsData(list: ArrayList<String>?) {
        hitsList = list
        /*notifyDataSetChanged();*/
    }

}