package com.dwwaittime.dwaittime

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.ListView
import android.widget.Toast
import com.dwwaittime.dwaittime.databinding.ActivityMainBinding
import com.dwwaittime.dwaittime.domain.CustomItem
import com.dwwaittime.dwaittime.domain.Place
import com.dwwaittime.dwaittime.domain.Section
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity(), ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

    // 待ち時間取得API
    val url = "http://182.163.86.16/WaittimeApi.php?target="

    val CONNECTION_TIMEOUT_MILLISECONDS = 60000

    private var binding: ActivityMainBinding? = null

    private var place: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val json = getAssets().open("attraction_sample.json").reader(charset=Charsets.UTF_8).use { it.readText() }
        val moshi = Moshi.Builder().build()
        place = moshi.adapter<Place>(Place::class.java).fromJson(json)

        // with setContentView
        // Bindingインスタン取得
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding!!.listview.setOnGroupClickListener(this)
        binding!!.listview.setOnChildClickListener(this)

        var tabs:TabLayout = findViewById(R.id.tabs) as TabLayout

        val context : Context = this

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let{
                    var urlWithParam = url
                    when(tab.position){
                        0 -> {
                            urlWithParam = urlWithParam + "tdl"
                            Log.i("myTag", "onTabSelebted:" + urlWithParam)
                            GetWaitTimeAsyncTask(context).execute(urlWithParam)
                        }
                        1 -> {
                            urlWithParam = urlWithParam +  "tds"
                            Log.i("myTag", "onTabSelebted:" + urlWithParam)
                            GetWaitTimeAsyncTask(context).execute(urlWithParam)

                        }
                        else -> {
                            urlWithParam = urlWithParam + "tdl"
                            GetWaitTimeAsyncTask(context).execute(urlWithParam)
                        }
                    }
                }
                Log.i("myTag", "onTabSelebted")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.i("myTag", "onTabUnselected")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.i("myTag", "onTabReselected")
            }

        })

        // 引っ張って更新のイベント登録
        swipe_refresh?.setOnRefreshListener {
            var urlWithParam = url
            when(tabs.selectedTabPosition){

                0 -> {
                    urlWithParam = urlWithParam + "tdl"
                    GetWaitTimeAsyncTask(this).execute(urlWithParam)
                }
                1 -> {
                    urlWithParam = urlWithParam + "tds"
                    GetWaitTimeAsyncTask(this).execute(urlWithParam)
                }
            }
        }

        // 初期表示
        var urlWithParam = url
        when(tabs.selectedTabPosition){

            0 -> {
                urlWithParam = urlWithParam + "tdl"
                GetWaitTimeAsyncTask(this).execute(urlWithParam)
            }
            1 -> {
                urlWithParam = urlWithParam + "tds"
                GetWaitTimeAsyncTask(this).execute(urlWithParam)
            }
        }


    }

    inner class GetWaitTimeAsyncTask(val contextSub : Context) : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            // Before doInBackground
        }

        override fun doInBackground(vararg urls: String?): String {
            var con : HttpURLConnection? = null

            var result :String = ""
            try{
                val url = URL(urls[0])

                con = url.openConnection() as HttpURLConnection
                con.connectTimeout = CONNECTION_TIMEOUT_MILLISECONDS
                con.readTimeout = CONNECTION_TIMEOUT_MILLISECONDS

                result = streamToString(con.inputStream)

            }catch(ex : Exception){
                ex.printStackTrace()
            }finally {
                con?.let{
                    it.disconnect()
                }
            }
            return result
        }

        override fun onProgressUpdate(vararg values: String?) {
            //publishProgressで呼ばれることもある

        }

        override fun onPostExecute(result: String?) {
            // Done
            try{
                swipe_refresh?.isRefreshing = false

                if(result == null){
                    Toast.makeText(contextSub, "待ち時間情報を取得できません", Toast.LENGTH_SHORT).show()
                    return
                }

                result?.let{
                    // mapperオブジェクトを作成
                    val mapper = jacksonObjectMapper()

                    // CustomItem形式にマッピング変換
                    val customItem = mapper.readValue<Array<CustomItem>>(result!!)

                    // UIに描画
                    //var lv = findViewById(R.id.listview) as ListView

                    // 表示用リストに格納
                    val adapter = CustomAdapter(contextSub, createItem(customItem))
                    binding!!.listview.setAdapter(adapter)

                    // 初期表示をリスト展開した状態に設定
                    for (i in 0 until binding!!.listview.expandableListAdapter.groupCount) {
                        binding!!.listview.expandGroup(i)
                    }
                    /*
                    TODO
                    var list = arrayListOf<CustomItem>()
                    for(item in customItem){
                        list.add(CustomItem(item.id, item.name, item.operatingStatus, item.time, item.fsStatus, item.updateTime))
                    }

                    val adapter = CustomAdapter(contextSub, list)
                    // adapterをset
                    lv.adapter = adapter
                    */


                }

            }catch (ex: Exception){
                val msg : String = "待ち時間情報を取得できません"
                Toast.makeText(contextSub, msg, Toast.LENGTH_SHORT).show()
                ex.printStackTrace()
            }
        }

    }

    /**
     * Listの表示を行う為のデータ作成処理
     * このメソッドの場合
     * 全部で10セクション
     * 1セクションに5個
     *
     * @return List
     */
    fun createItem(customItems : Array<CustomItem>): ArrayList<Section> {
        val items = arrayListOf<Section>()

        val themes = when(tabs.selectedTabPosition){
            0 -> {
                place!!.tdl
            }
            1 -> {
                place!!.tds
            }
            else -> {
                place!!.tdl
            }
        }

        // Sectionの数ループを回す
        for (theme in themes) {
            // Sectionの中に表示する子データ
            val rows = ArrayList<CustomItem>()
            for(attraction in theme.attractions){

                val row = (CustomItem(attraction.code ,
                        attraction.name,
                        "",
                        "",
                        "",
                        "")
                        )
                //val row = (CustomItem(item.id, item.name, item.operatingStatus, item.time, item.fsStatus, item.updateTime))
                for(customItem in customItems){
                    // jsonのcodeとDBから取得したcodeが一致したら待ち時間情報などを格納
                    if(customItem.id == attraction.code){
                        row.operatingStatus = customItem.operatingStatus
                        row.time = if (customItem.time.equals("-1")) { "" } else {"${customItem.time}分"}
                        row.fsStatus = customItem.fsStatus
                        row.updateTime = "(更新日時:${customItem.updateTime})"

                    }
                }
                rows.add(row)
            }
            items.add(Section(theme.name ,rows))
        }
        return items
    }

    fun streamToString(inputStream: InputStream) : String{

        val br : BufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        var result = ""

        try{
            do{
                line = br.readLine()
                line?.let{
                    result += line
                }
            }while(line != null)
            inputStream.close()

        }catch (ex : Exception){
            ex.printStackTrace()
        }

        return result

    }

    override fun onChildClick(parent: ExpandableListView, v: View, groupPosition: Int, childPosition: Int, id: Long): Boolean {
        //Toast.makeText(this, String.format(Locale.getDefault(), "Section = %d Row = %d", groupPosition, childPosition), Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onGroupClick(parent: ExpandableListView, v: View, groupPosition: Int, id: Long): Boolean {
        //Toast.makeText(this, String.format(Locale.getDefault(), "Section = %d", groupPosition), Toast.LENGTH_SHORT).show()
        // return trueで折りたたまれなくなる、falseで折りたたまれる
        return true
    }
}