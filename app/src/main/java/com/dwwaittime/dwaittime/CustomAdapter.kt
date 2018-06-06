package com.dwwaittime.dwaittime

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.dwwaittime.dwaittime.domain.CustomItem
import com.dwwaittime.dwaittime.domain.Section
import com.dwwaittime.dwaittime.databinding.CustomItemBinding
import com.dwwaittime.dwaittime.databinding.SectionRowBinding

class CustomAdapter(var context: Context, var list: ArrayList<Section>) : BaseExpandableListAdapter() {
    val inflater: LayoutInflater

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getGroupCount(): Int {
        return list!!.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return getGroup(groupPosition)!!.rows.size
    }

    override fun getGroup(groupPosition: Int): Section {
        return list!![groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): CustomItem {
        return getGroup(groupPosition)!!.rows[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    /**
     * Section表示のView
     *
     * @param groupPosition int
     * @param isExpanded    boolean 開いているかどうか
     * @param convertView   View
     * @param parent        ViewGroup
     * @return View
     */
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val binding: SectionRowBinding
        if (convertView == null) {
            binding = SectionRowBinding.inflate(LayoutInflater.from(parent.context))
            convertView = binding.root
            convertView.tag = binding
        } else {
            binding = convertView.tag as SectionRowBinding
        }

        binding.model = getGroup(groupPosition)

        return convertView
    }

    /**
     * Sectionの中で表示するView

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
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
     * */

    /**
     * Section表示の中で表示するView
     *
     * @param groupPosition int
     * @param childPosition int
     * @param isLastChild   boolean グループ内の最後の子Viewかどうか
     * @param convertView   View
     * @param parent        ViewGroup
     * @return View
     */
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val binding: CustomItemBinding
        if (convertView == null) {
            binding = CustomItemBinding.inflate(LayoutInflater.from(parent.context))
            convertView = binding.root
            convertView.tag = binding
        } else {
            binding = convertView.tag as CustomItemBinding
        }

        binding.model = getChild(groupPosition, childPosition)

        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        // ここをtrueに設定しないと小ビューのClickが反応しない
        return true
    }

}