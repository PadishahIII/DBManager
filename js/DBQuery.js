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
    //console.log(data)
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
 *  "col1":"aa;bb;cc;"
 *  "col2":"dd;ee;"
 *  ...
 * }
 */
var TableData = new Map()// tblname => data_array
function getTableData(tblname) {
    xmlhttp = new XMLHttpRequest()
    xmlhttp.open("GET", "/DBManager/queryAllData", false)
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
    xmlhttp.send("tblname=" + tblname)
    var data = xmlhttp.responseText
    var json = JSON.parse(data)

    var num = json['num']
    //该表的所有字段名
    var columns = ColumnInfo.get(tblname)
    var col_datas = new Array()
    for (var i in columns) {
        var col_data_str = json[columns[i]]
        var col_data_strs = col_data_str.split(";")
        var col_data_arr = new Array()
        for (var j in col_data_strs) {
            if (col_data_strs[j] != '') {
                col_data_arr.push(col_data_strs[j])
            }//if
        }//for j
        col_datas.push(col_data_arr)
    }//for i
    TableData.set(tblname, col_datas)

}
var arr = new Array(4, 5)
for (var i in arr) {
    console.log(i)
}

/**
 * 获取
 */

