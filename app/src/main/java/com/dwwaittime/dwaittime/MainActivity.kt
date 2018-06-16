package com.dwwaittime.dwaittime

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.TabLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import android.widget.Toast
import com.dwwaittime.dwaittime.R.id.*
import com.dwwaittime.dwaittime.databinding.ActivityMainBinding
import com.dwwaittime.dwaittime.domain.*
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
    val url = "http://182.163.86.16/test/WaittimeApi_test.php?target="
    //val url = "http://localhost/TDRApp_CrawlerWaitTime/WaittimeApi.php?target="

    val CONNECTION_TIMEOUT_MILLISECONDS = 60000

    private var binding: ActivityMainBinding? = null

    private var place: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val json = getAssets().open("facility_define.json").reader(charset=Charsets.UTF_8).use { it.readText() }
        val moshi = Moshi.Builder().build()
        place = moshi.adapter<Place>(Place::class.java).fromJson(json)

        var swipeRefresh = findViewById(R.id.swipe_refresh) as SwipeRefreshLayout
        swipeRefresh.layoutParams.height = swipeRefresh.layoutParams.height - 56


        // with setContentView
        // Bindingインスタン取得
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding!!.listview.setOnGroupClickListener(this)
        binding!!.listview.setOnChildClickListener(this)

        //直接呼べるので、変数宣言はいらない？
        //var tabs:TabLayout = findViewById(R.id.tabs) as TabLayout

        val context : Context = this

        // タブクリック時のイベント登録
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let{
                    var urlWithParam = decideApiUrl(tab.position,bottom_nav.selectedItemId)
                    GetWaitTimeAsyncTask(context).execute(urlWithParam)
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

        //BottomNavigationViewへのイベント登録
        bottom_nav.setOnNavigationItemSelectedListener {
            it?.let{
                var urlWithParam = decideApiUrl(tabs.selectedTabPosition, it.itemId)
                GetWaitTimeAsyncTask(context).execute(urlWithParam)
                true
            }
        }

        // 引っ張って更新のイベント登録
        swipe_refresh?.setOnRefreshListener {
            var urlWithParam = decideApiUrl(tabs.selectedTabPosition, bottom_nav.selectedItemId)
            GetWaitTimeAsyncTask(this).execute(urlWithParam)
        }

        // 初期表示
        var urlWithParam = decideApiUrl(tabs.selectedTabPosition, bottom_nav.selectedItemId)
        GetWaitTimeAsyncTask(this).execute(urlWithParam)

    }

    // 待ち時間取得（サーバのAPIに対して非同期でリクエストする）
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

                    when(bottom_nav.selectedItemId) {
                        R.id.show -> {
                            // ショーの場合はレイアウトの内容を変更する
                            // CustomItemShow形式にマッピング変換
                            val customItem :Array<CustomItemShowDbMap> = mapper.readValue<Array<CustomItemShowDbMap>>(result)

                            // 表示用リストに格納
                            val adapter = CustomAdapter2(contextSub, createItem(customItem))
                            binding!!.listview.setAdapter(adapter)

                            // 初期表示をリスト展開した状態に設定
                            for (i in 0 until binding!!.listview.expandableListAdapter.groupCount) {
                                binding!!.listview.expandGroup(i)
                            }
                        }
                        else -> {
                            // CustomItem形式にマッピング変換
                            val customItem = mapper.readValue<Array<CustomItem>>(result)

                            // UIに描画
                            //var lv = findViewById(R.id.listview) as ListView

                            // 表示用リストに格納
                            val adapter = CustomAdapter(contextSub, createItem(customItem))
                            binding!!.listview.setAdapter(adapter)

                            // 初期表示をリスト展開した状態に設定
                            for (i in 0 until binding!!.listview.expandableListAdapter.groupCount) {
                                binding!!.listview.expandGroup(i)
                            }
                        }
                    }

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
        val themes = if(tabs.selectedTabPosition == 0 && bottom_nav.selectedItemId == R.id.attraction){
            // TDLタブ、アトラクションタブ選択
            place!!.tdl_A
        }else if(tabs.selectedTabPosition == 1 && bottom_nav.selectedItemId == R.id.attraction) {
            place!!.tds_A
        }else if(tabs.selectedTabPosition == 0 && bottom_nav.selectedItemId == R.id.restaurant){
            place!!.tdl_R
        }else if(tabs.selectedTabPosition == 1 && bottom_nav.selectedItemId == R.id.restaurant){
            place!!.tds_R
        }else{
            place!!.tdl_A
        }

        // Sectionの数ループを回す
        for (theme in themes) {
            // Sectionの中に表示する子データ
            val rows = ArrayList<CustomItem>()
            for(facility in theme.facilities){
                //codeとアトラクション名のみ設定した1行分のデータを生成
                val row = (CustomItem(facility.code ,
                        facility.name,
                        "",
                        "",
                        "",
                        "")
                        )
                //val row = (CustomItem(item.id, item.name, item.operatingStatus, item.time, item.fsStatus, item.updateTime))
                for(customItem in customItems){
                    // jsonのcodeとDBから取得したcodeが一致したら待ち時間情報などを1行データに格納
                    if(customItem.id == facility.code){
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

    /**
     * Listの表示を行う為のデータ作成処理（ショーレイアウト用）
     * このメソッドの場合
     * 全部で10セクション
     * 1セクションに5個
     *
     * @return List
     */
    fun createItem(customItems : Array<CustomItemShowDbMap>): ArrayList<SectionShow> {

        //テーマの定義
        val themes = arrayOf("ワールドバザール",
                "アドベンチャーランド",
                "ウエスタンランド",
                "クリッターカントリー",
                "ファンタージランド",
                "トゥーンタウン",
                "トゥモローランド",
                "メディテレーニアンハーバー",
                "アメリカンウォーターフロント",
                "ミステリアスアイランド",
                "ポートディスカバリー",
                "ロストリバーデルタ",
                "マーメイドラグーン",
                "アラビアンコースト",
                "パークワイド"
                )

        // Section生成
        val items = arrayListOf<SectionShow>()

        // テーマごとに繰り返し
        //
        for(theme in themes){

            var sectionText = ""

            // Sectionの中に表示する子データ
            val rows = ArrayList<CustomItemShow>()
            for(customItem in customItems){

                if(sectionText.isEmpty() && customItem.areaJName.indexOf(theme) >= 0) {
                    // セクション名未決定で、エリア名内にテーマ名称を含む場合
                    // セクション名に採用
                    sectionText = theme
                }else if(customItem.areaJName.indexOf(sectionText) < 0){
                    // セクション名が決まった状態で、エリア名内にセクション名（テーマ名称）を含まない場合
                    // スキップ
                    continue
                }else if(sectionText.isEmpty()){
                    // セクション名未決定の場合
                    continue
                }

                //codeとアトラクション名のみ設定した1行分のデータを生成

                val times = customItem.time.split("/")
                val fsStaties = customItem.fsStatus.split("/")
                var timeText = ""
                for ( i in times.indices){
                    timeText += "${if(fsStaties.size != 0) fsStaties[i] else ""} ${times[i]}\n"
                }

                val row = (CustomItemShow(
                        customItem.id ,
                        customItem.name,
                        customItem.operatingStatus,
                        timeText,
                        "",
                        "(更新日時:${customItem.updateTime})"
                ))

                rows.add(row)
            }
            if(!sectionText.isEmpty()) {
                items.add(SectionShow(sectionText, rows))
            }
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

    // Tab選択状態、BottomNavigationView選択状態
    fun decideApiUrl(tabPos : Int, bottomNavPos : Any) : String{
        var returnStr = ""
        // Tab選択状態
        if (tabPos == 0) {
            when (bottomNavPos) {
            // BottomNavigationView選択状態
                R.id.attraction -> {
                    returnStr = url + "tdl_a"
                }
                R.id.restaurant -> {
                    returnStr = url + "tdl_r"
                }
                R.id.show -> {
                    returnStr = url + "tdl_s"
                }
                else -> {
                    returnStr = url + "tdl_a"
                }
            }
        } else {
            when (bottomNavPos) {
                R.id.attraction -> {
                    returnStr = url + "tds_a"
                }
                R.id.restaurant -> {
                    returnStr = url + "tds_r"
                }
                R.id.show -> {
                    returnStr = url + "tds_s"
                }
                else -> {
                    returnStr = url + "tds_a"
                }
            }

        }
        return returnStr
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