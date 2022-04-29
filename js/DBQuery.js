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

/**
 * 删除指定表的一条数据
 * @param {*} tblname 
 * @param {*} data_map 
 */
function deleteFromTable(tblname, data_map) {
    if (data_map.size != ColumnInfo.get(tblname).length) {
        alert("Error1 at deleteFromTable!")
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
    xmlhttp.open("POST", "/DBManager/delete", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname + "&" + postString)

    var data = xmlhttp.responseText
    alert(data)
}

/**
 * 更新一条数据
 * 发送格式：
 * tblname=xx
 * data_map_json:
 * {
 *  "old":{
 *         "col1":value1,
 *         "col2":value2...
 *        }
 *  "new":{同old}
 * }
 * @param {*} tblname 
 * @param {Map} old_data_map 旧值
 * @param {Map} new_data_map 新值
 * @returns 
 */
function updateFromTable(tblname, old_data_map, new_data_map) {
    var old_jsonstr = JSON.stringify(Map2Obj(old_data_map))
    var new_jsonstr = JSON.stringify(Map2Obj(new_data_map))
    var data_map_json = {
        "old": old_jsonstr,
        "new": new_jsonstr
    }
    var data_map_jsonstr = JSON.stringify(data_map_json)

    console.log(data_map_jsonstr)

    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("POST", "/DBManager/update", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname + "&data_map_json=" + data_map_jsonstr)

    var data = xmlhttp.responseText
    alert(data)
}
function Map2Obj(map) {
    let obj = Object.create(null)
    for (let [k, v] of map) {
        obj[k] = v
    }
    return obj
}

/**
 * 生成指定大小的表格
 * @param {*} row 
 * @param {*} col 
 * @param {*} data_arr row大小的数组，每个元素为col大小的数组
 */
function generateTable2D(row, col, data_arr) {
    table = document.createElement("table")
    tBody = document.createElement("tBody")
    for (var i = 0; i < row; i++) {
        tr = tBody.insertRow(i)
        var arr = data_arr[i]
        for (var j = 0; j < col; j++) {
            td = tr.insertCell(j)
            td.innerHTML = arr[j]
        }
    }
    table.appendChild(tBody)
    document.body.appendChild(table)
}
/**
 * 生成一维纵向或横向表
 * @param {*} num 
 * @param {*} data_arr 
 * @param {*} type 0 for 纵向  1 for 横向
 */
function generateTable1D(num, data_arr, type) {
    table = document.createElement("table")
    tBody = document.createElement("tBody")
    if (type == 0) {
        for (var i = 0; i < num; i++) {
            tr = tBody.insertRow(i)
            td = tr.insertCell(0)
            td.innerHTML = data_arr[i]
        }
    }
    else if (type == 1) {
        tr = tBody.insertRow(0)
        for (var i = 0; i < num; i++) {
            td = tr.insertCell(i)
            td.innerHTML = data_arr[i]
        }
    }
    else console.log("Error in generateTable1D")
    table.appendChild(tBody)
    document.body.appendChild(table)
}
//var data_map = new Map()
//data_map.set("id", 1)
//data_map.set("name", "aa")
//data_map.set("funding", "200")
//var str = JSON.stringify(Map2Obj(data_map))
//console.log(str)
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

