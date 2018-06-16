package com.dwwaittime.dwaittime.domain

/**
 * APIからの取得結果とマッピングするためのクラス（ショー用）
 * */
public class CustomItemShowDbMap(
                        var id: String,
                        var name: String,
                        var operatingStatus: String,
                        var time: String,
                        var fsStatus: String,
                        var updateTime: String,
                        var areaJName: String
)