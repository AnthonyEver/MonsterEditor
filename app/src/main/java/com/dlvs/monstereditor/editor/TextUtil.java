package com.dlvs.monstereditor.editor;

import android.text.Html;
import android.text.TextUtils;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc：文字工具类
 * author：haojie
 * date：2017-07-04
 */
public class TextUtil {

    /**
     * 验证手机格式
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
        String telRegex = "[1][3578]\\d{9}";// "[1]"代表第1位为数字1，"[3578]"代表第二位可以为3、5、7、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        return mobiles.matches(telRegex);
    }


    /**
     * 文本框非空验证
     * @param eText
     * @return
     */
    public static boolean editTextJudgeEmpty(EditText... eText) {
        for (int i = 0; i < eText.length; i++) {
            if (TextUtils.isEmpty(eText[i].getText().toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证邮箱格式
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String mailRegex = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";//
        return email.matches(mailRegex);
    }

    /**
     * 验证字符串是否为空
     * @param args
     * @return
     */
    public static boolean isEmpty(String... args) {
        if (args != null) {
            for (String str : args) {
                if (str == null || str.equals("")) {
                    return true;
                }
            }
        } else
            return true;
        return false;
    }

    /**
     * 过滤字符串中的HTML
     * @param string
     */
    public static String deleteHtmlString(String string){
        return Html.fromHtml(string).toString().trim();
    }

    /**
     * 判断一个字符串是否都为数字
     * @param strNum
     * @return
     */
    public static boolean isDigit(String strNum) {
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher((CharSequence) strNum);
        return matcher.matches();
    }

    /**
     * 截取数字
     * @param content
     * @return
     */
    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 截取非数字
     * @param content
     * @return
     */
    public static String splitNotNumber(String content) {
        Pattern pattern = Pattern.compile("\\D+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 判断一个字符串是否含有数字
     * @param content
     * @return
     */
    public static boolean HasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    /**
     * 转换时间
     * @param time
     * @return
     */
    public static Date transTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        try {
            return sdf.parse(time);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据当前时间判断传递的时间间隔
     * @param createDate
     * @return
     */
    public static String getTimeText(String createDate) {
        String r = "";
        if(TextUtil.isEmpty(createDate)) return r;
        long createTime = Long.parseLong(createDate);
        long nowTime = System.currentTimeMillis();
        long result = Math.abs(nowTime - createTime);
        if (result < 60000) {// 一分钟内
            long seconds = result / 1000;
            if (seconds == 0) {
                r = "刚刚";
            } else {
                r = seconds + "秒前";
            }
        } else if (result >= 60000 && result < 3600000) {// 一小时内
            long seconds = result / 60000;
            r = seconds + "分钟前";
        } else if (result >= 3600000 && result < 86400000) {// 一天内
            long seconds = result / 3600000;
            r = seconds + "小时前";
        } else if (result >= 86400000 && result < 259200000) {// 三天内
            long seconds = result / 86400000;
            r = seconds + "天前";
        } else {// 日期格式
            SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");
            r = format.format(Long.parseLong(createDate));
        }
        return r;
    }

}
