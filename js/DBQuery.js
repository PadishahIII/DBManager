//获取所有表格的名字及字段名
/**
 * 返回内容：
 * {
 *  "num":数量
 *  "tblnames":"tbl1;tbl2;..."
 *  "colnames":"col1_1,col1_2..;col2_1,col2_2..."
 * }
 */
var TableNames = new Array()// {'num' => 12 str1 str2 }
var ColumnInfo = new Map()//tblname => Array(col1,col2)
function getTables() {
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("GET", "/DBManager/getTables", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send()
    var data = xmlhttp.responseText
    //alert(data)
    console.log(data)
    var json = JSON.parse(data)
    TableNames['num'] = json['num']
    var tblnames = json['tblnames'].split(';')
    var cols = json['colnames'].split(';')
    for (var i in tblnames) {
        if (tblnames[i] != '') {
            TableNames.push(tblnames[i])
        }
    }
    for (var i in cols) {
        if (cols[i] != '') {
            var tblname = TableNames[i]
            var col = new Array()
            var cols_i = cols[i].split(',')
            for (var j in cols_i) {
                if (cols_i[j] != '') {
                    col.push(cols_i[j])
                }
            }//for j
            ColumnInfo.set(tblname, col)
        }//if
    }//for i
}

/**
 * 查询指定表的所有元组
 * 返回结果：
 * {
 *  "num":9   //元组个数
 *  "0":"aa;bb;cc;"
 *  "1":"dd;ee;ff"
 *  ...
 * }
 */
var TableData = new Map()// tblname => data_array
function getTableData(tblname) {
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("GET", "/DBManager/queryAllData?tblname=" + tblname, false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send()
    var data = xmlhttp.responseText

    //alert(data)
    var json = JSON.parse(data)

    var num = json['num']
    var col_datas = new Array()
    var i = 0
    while (i < num) {
        var col_data_str = json[String(i)]
        var col_data_strs = col_data_str.split(";")
        var col_data_arr = new Array()
        for (var j in col_data_strs) {
            if (col_data_strs[j] != '') {
                col_data_arr.push(col_data_strs[j])
            }//if
        }//for j
        col_datas.push(col_data_arr)
        i++
    }//while i
    TableData.set(tblname, col_datas)

}

/**
 * 向指定表插入一个元组
 * data_map:<列名，属性值>
 * @param {*} tblname 
 * @param {*} data_map 
 */
function insertIntoTable(tblname, data_map) {
    if (data_map.size != ColumnInfo.get(tblname).length) {
        alert("Error1 at insertIntoTable!")
        return false
    }
    var postString = new String()
    for (var i of data_map.keys()) {
        postString += i
        postString += '=' + data_map.get(i)
        postString += "&"
    }
    postString = postString.slice(0, postString.length - 1)
    console.log("poststr:")
    console.log("tblname=" + tblname + "&" + postString)
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("POST", "/DBManager/insert", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname + "&" + postString)

    var data = xmlhttp.responseText
    alert(data)

}
//var data_map = new Map()
//data_map.set("id", 1)
//data_map.set("name", "aa")
//data_map.set("funding", "200")
//for (var i of data_map) {
//    console.log(i)
//    console.log(data_map.get(i))
//}
//var s = new String("abc")
//console.log(s.length)
//console.log(s.substring(0, s.length - 1))
//console.log(s.slice(0, s.length - 1))
//var arr = new Array()
//arr.push(1)
//console.log(arr.length)
//
//var map = new Map()
//map.set("a", 1)
//map.set("b", 2)
//console.log(map.keys())
//for (var i of map.keys()) {
//    console.log(i)
//    console.log(map.get(i))
//}
//console.log(map.size)
/**
 * 获取
 */

