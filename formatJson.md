```
 /**
  * 对json字符串格式化输出
  * @param jsonStr
  * @return
  */
 fun formatJson(jsonStr: String?): String {
     if (null == jsonStr || "" == jsonStr) return ""
     val sb = StringBuilder()
     var last: Char
     var current = '\u0000'
     var indent = 0
     for (i in 0 until jsonStr.length) {
         last = current
         current = jsonStr[i]
         when (current) {
             '{', '[' -> {
                 sb.append(current)
                 sb.append('\n')
                 indent++
                 addIndentBlank(sb, indent)
             }
             '}', ']' -> {
                 sb.append('\n')
                 indent--
                 addIndentBlank(sb, indent)
                 sb.append(current)
             }
             ',' -> {
                 sb.append(current)
                 if (last != '\\') {
                     sb.append('\n')
                     addIndentBlank(sb, indent)
                 }
             }
             else -> sb.append(current)
         }
     }

     return sb.toString()
 }
 
 /**
 * 添加space
 * @param sb
 * @param indent
 */
private fun addIndentBlank(sb: StringBuilder, indent: Int) {
    for (i in 0 until indent) {
        sb.append('\t')
    }
}
```
