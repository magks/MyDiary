package com.example.max.mydiary

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class DiaryPageAdapter(private val myDataset: ArrayList<String>) :
        RecyclerView.Adapter<DiaryPageAdapter.DiaryEntryViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class DiaryEntryViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DiaryPageAdapter.DiaryEntryViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.diary_page_list_item, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        return DiaryEntryViewHolder(textView)
        //return MyViewHolder(TextView())
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: DiaryEntryViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = myDataset[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}