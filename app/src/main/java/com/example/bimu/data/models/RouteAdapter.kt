package com.example.bimu.data.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bimu.R

class RouteAdapter(val onClick: (Route) -> Unit) : ListAdapter<Route, RouteAdapter.RouteViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(route: Route, onClick: (Route) -> Unit) {
            itemView.findViewById<TextView>(R.id.textViewRouteName).text = route.title
            itemView.findViewById<TextView>(R.id.textViewDifficulty).text = route.difficulty
            itemView.findViewById<TextView>(R.id.textViewDate).text = route.timeStart
            itemView.setOnClickListener { onClick(route) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Route>() {
        override fun areItemsTheSame(old: Route, new: Route) = old._id == new._id
        override fun areContentsTheSame(old: Route, new: Route) = old == new
    }
}