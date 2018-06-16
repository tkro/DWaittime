package com.dwwaittime.dwaittime

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.dwwaittime.dwaittime.domain.CustomItem
import com.dwwaittime.dwaittime.domain.SectionShow
import com.dwwaittime.dwaittime.databinding.CustomItemBinding
import com.dwwaittime.dwaittime.databinding.CustomItemShowBinding
import com.dwwaittime.dwaittime.databinding.SectionShowRowBinding
import com.dwwaittime.dwaittime.domain.CustomItemShow
import com.dwwaittime.dwaittime.domain.Section

class CustomAdapter2(var context: Context, var list: ArrayList<SectionShow>) : BaseExpandableListAdapter() {
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

    override fun getGroup(groupPosition: Int): SectionShow {
        return list!![groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): CustomItemShow {
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
     * SectionShow表示のView
     *
     * @param groupPosition int
     * @param isExpanded    boolean 開いているかどうか
     * @param convertView   View
     * @param parent        ViewGroup
     * @return View
     */
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val binding: SectionShowRowBinding
        if (convertView == null) {
            binding = SectionShowRowBinding.inflate(LayoutInflater.from(parent.context))
            convertView = binding.root
            convertView.tag = binding
        } else {
            binding = convertView.tag as SectionShowRowBinding
        }

        binding.model = getGroup(groupPosition)

        return convertView
    }

    /**
     * SectionShow表示の中で表示するView
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
        val binding: CustomItemShowBinding
        if (convertView == null) {
            binding = CustomItemShowBinding.inflate(LayoutInflater.from(parent.context))
            convertView = binding.root
            convertView.tag = binding
        } else {
            binding = convertView.tag as CustomItemShowBinding
        }

        binding.model = getChild(groupPosition, childPosition)

        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        // ここをtrueに設定しないと小ビューのClickが反応しない
        return true
    }

}