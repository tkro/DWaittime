package com.dwwaittime.dwaittime

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.dwwaittime.dwaittime.domain.CustomItem

class CustomAdapter(var context: Context, var items: ArrayList<CustomItem>) : BaseAdapter() {
    val inflater: LayoutInflater

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v = convertView
        var holder: CustomViewHolder? = null

        // 再利用
        v?.let{
            holder = it.tag as CustomViewHolder?
        } ?: run {
            v = inflater.inflate(R.layout.custom_item, null)
            holder = CustomViewHolder(v?.findViewById(R.id.name) as TextView,
                    v?.findViewById(R.id.operatingStatus) as TextView,
                    v?.findViewById(R.id.time) as TextView,
                    v?.findViewById(R.id.fsStatus) as TextView,
                    v?.findViewById(R.id.updateTime) as TextView)
            v?.tag = holder
        }

        holder?.let {
            it.name.text = items.get(position).name
            it.operatingStatus.text = items.get(position).operatingStatus
            if(items.get(position).time == -1){
                it.time.text = ""
            }else{
                it.time.text = items.get(position).time.toString() + "分"
            }

            it.fsStatus.text = items.get(position).fsStatus
            it.updateTime.text = "(更新時間:"+ items.get(position).updateTime + ")"
        }

        return v as View
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long{
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    class CustomViewHolder(var name: TextView,
                           var operatingStatus: TextView,
                           var time: TextView,
                           var fsStatus: TextView,
                           var updateTime: TextView)
}