package com.dlvs.monstereditor.editor;

/**
 * desc：编辑器相关的常量值
 * author：mgq
 * date：2017-06-06
 */

public class EditorConstants {
    /*定义匹配script的正则表达式*/
    public static final String REGEX_SCRIPT = "<script[^>]*?>[\\s\\S]*?<\\/script>";
    /*定义匹配style的正则表达式*/
    public static final String REGEX_STYLE = "<style[^>]*?>[\\s\\S]*?<\\/style>";
    /*定义匹配HTML标签的正则表达式*/
    public static final String REGEX_HTML = "</?(?!img|video|audio|a|h\\\\d)[^>]+>";
    /*匹配图片标签的正则表达式*/
    public static final String REGEX_IMG = "<img[^<>]*?\\ssrc=['\"]?(.*?)['\"]?(\\s.*?)?>";
    /*匹配音频标签的正则表达式*/
    public static final String REGEX_AUDIO = "<audio[^<>]*?\\ssrc=['\"]?(.*?)['\"]?(\\s.*?)?\\sname=['\"]?(.*?)['\"]?(\\s.*?)?\\saliasname=['\"]?(.*?)['\"]?(\\s.*?)?\\spath=['\"]?(.*?)['\"]?(\\s.*?)?>";
    /*匹配视频标签的表达式*/
    public static final String REGEX_VEDIO = "<video[^<>]*?\\ssrc=['\"]?(.*?)['\"]?(\\s.*?)?\\sname=['\"]?(.*?)['\"]?(\\s.*?)?\\saliasname=['\"]?(.*?)['\"]?(\\s.*?)?\\spath=['\"]?(.*?)['\"]?(\\s.*?)?\\sposter=['\"]?(.*?)['\"]?(\\s.*?)?>";
    /*匹配资源和附件的正则表达式*/
    public static final String REGEX_ATTACHMENT = "<a[^<>]*?\\shref=['\"]?(.*?)['\"]?(\\s.*?)?>";
    /**/
    public static final String REGEX_AA = "</?(?!h\\\\d)[^>]+>";

    /*拆分字符串标签*/
    public static final String SPLITTAG = "splitTag";
}
