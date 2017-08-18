package com.dlvs.monstereditor.editor;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.EditText;
import com.bumptech.glide.Glide;
import com.dlvs.monstereditor.Network;
import com.dlvs.monstereditor.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc：编辑器管理类
 * author：mgq
 * date：2017-06-06
 */

public class RichTextEditorManager {
    /*编辑器所展示数据的集合*/
    private List<EditorDataEntity> editShowList = new ArrayList<>();
    /*编辑器中多媒体数据集合*/
    private List<EditorDataEntity> editorDataList = new ArrayList<>();
    /*富文本编辑器*/
    private RichTextEditor editor;
    /*上下文*/
    private Context mContext;
    /*标签长度*/
    private int tagLength = -8;
    private boolean richEditorStatus = true;
    public int editorBackground = 0;

    public RichTextEditorManager(Context mContext) {
        this.mContext = mContext;
    }

    public RichTextEditorManager(RichTextEditor editor, Context mContext) {
        this.editor = editor;
        this.mContext = mContext;
    }

    public boolean isRichEditorStatus() {
        return richEditorStatus;
    }

    public void setRichEditorStatus(boolean richEditorStatus) {
        this.richEditorStatus = richEditorStatus;
    }

    /*回显数据*/
    private void showData(boolean status) {
        int k = 0;
        for (int i = 0; i < editShowList.size(); i++) {
            if (editShowList.get(i).getType().equals(RichTextEditor.TYPE_TEXT) || editShowList.get(i).getType().equals(RichTextEditor.TYPE_ALINK)) {
                if (editShowList.get(i).getText().trim().length() <= 0) {
                    continue;
                }
                editor.addEditTextAtIndex(k, editShowList.get(i).getText(), editShowList.get(i).getType());
            } else if (editShowList.get(i).getType().equals(RichTextEditor.TYPE_AUDIO)) {
//                editor.addAudioViewAtIndex(k, editShowList.get(i));
            } else if (editShowList.get(i).getType().equals(RichTextEditor.TYPE_VIDEO)) {
                editor.addImageViewAtIndex(k, editShowList.get(i));
            } else {
                editor.addImageViewAtIndex(k, editShowList.get(i));
            }
            k++;
        }
    }

    /*过滤html数据*/
    public void filterHtml(final String htmlStr) {
        //过滤
        filterData(htmlStr);
        //排序
        sortMediaPosition();
    }

    /**
     * 过滤数据
     *
     * @param htmlStr
     * @return
     */
    public String filterData(String htmlStr) {
        // 替换换行
        htmlStr = htmlStr.replace("<br>", "\n");
        htmlStr = htmlStr.replace("</p>", "\n");
        // 过滤script标签
        htmlStr = getHtmlFilterString(EditorConstants.REGEX_SCRIPT, htmlStr);
        // 过滤style标签
        htmlStr = getHtmlFilterString(EditorConstants.REGEX_STYLE, htmlStr);
        // 过滤html标签
        htmlStr = getHtmlFilterString(EditorConstants.REGEX_HTML, htmlStr);
        //过滤多媒体标签之前拆分数据
        splitTextData(htmlStr);

        //过滤附件a标签
        htmlStr = getMediaFilterString(EditorConstants.REGEX_ATTACHMENT, htmlStr, RichTextEditor.TYPE_ALINK);
        //过滤图片
        htmlStr = getMediaFilterString(EditorConstants.REGEX_IMG, htmlStr, RichTextEditor.TYPE_IMAGE);
        //过滤音频
        htmlStr = getMediaFilterString(EditorConstants.REGEX_AUDIO, htmlStr, RichTextEditor.TYPE_AUDIO);
        //过滤视频
        htmlStr = getMediaFilterString(EditorConstants.REGEX_VEDIO, htmlStr, RichTextEditor.TYPE_VIDEO);

        ensureMediaTagPosition(htmlStr, RichTextEditor.TYPE_ALINK);
        ensureMediaTagPosition(htmlStr, RichTextEditor.TYPE_IMAGE);
        ensureMediaTagPosition(htmlStr, RichTextEditor.TYPE_AUDIO);
        ensureMediaTagPosition(htmlStr, RichTextEditor.TYPE_VIDEO);

        htmlStr = replaceTagByUrl(htmlStr, RichTextEditor.TYPE_ALINK);
        htmlStr = replaceTagByUrl(htmlStr, RichTextEditor.TYPE_IMAGE);
        htmlStr = replaceTagByUrl(htmlStr, RichTextEditor.TYPE_AUDIO);
        htmlStr = replaceTagByUrl(htmlStr, RichTextEditor.TYPE_VIDEO);
        return htmlStr;
    }

    /*将tag标签替换为对应的url*/
    private String replaceTagByUrl(String htmlStr, String tag) {
        for (int i = 0; i < editorDataList.size(); i++) {
            if (tag.equals(editorDataList.get(i).getType())) {
                if (tag.equals(RichTextEditor.TYPE_ALINK)) {
                    htmlStr = htmlStr.replaceFirst(tag, editorDataList.get(i).getText());
                } else {
                    htmlStr = htmlStr.replaceFirst(tag, editorDataList.get(i).getUrl());
                }
            }
        }
        return htmlStr;
    }

    /**
     * 确定多媒体标签的位置
     *
     * @param htmlStr
     */
    public void ensureMediaTagPosition(String htmlStr, String tag) {
        int position = tagLength;
        for (int i = 0; i < editorDataList.size(); i++) {
            position = htmlStr.indexOf(tag, position + 8);
            for (int k = 0; k < editorDataList.size(); k++) {
                if (editorDataList.get(k).getPosition() == 0 && tag.equals(editorDataList.get(k).getType())) {
                    editorDataList.get(k).setPosition(position);
                    break;
                }
            }
        }
    }

    /**
     * 获取过滤多媒体标签后的数据
     */
    public String getMediaFilterString(String reg, String htmlStr, String tag) {
        EditorDataEntity editorDataEntity;
        //过滤标签
        Pattern p_img = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher m_img = p_img.matcher(htmlStr);
        while (m_img.find()) {
            editorDataEntity = new EditorDataEntity();
            if (tag.equals(RichTextEditor.TYPE_ALINK)) {
                editorDataEntity.setText(m_img.group(1));
            } else {
                editorDataEntity.setUrl(m_img.group(1));
            }
            editorDataEntity.setType(tag);
            if (tag.equals(RichTextEditor.TYPE_AUDIO) || tag.equals(RichTextEditor.TYPE_VIDEO)) {
                editorDataEntity.setName(m_img.group(3));
                editorDataEntity.setPath(m_img.group(5));
                editorDataEntity.setAliasname(m_img.group(7));
            }

            if (tag.equals(RichTextEditor.TYPE_VIDEO)) {
                editorDataEntity.setPoster(m_img.group(9));
            }
            editorDataList.add(editorDataEntity);
        }
        htmlStr = m_img.replaceAll(tag);
        return htmlStr;
    }

    /**
     * 过滤普通Html标签
     */
    public String getHtmlFilterString(String reg, String htmlStr) {
        Pattern p_html = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll("");
        return htmlStr;
    }

    /*拆分文本数据*/
    public void splitTextData(String htmlStr) {
        String data = htmlStr;
        data = insertSplitTag(data, EditorConstants.REGEX_ATTACHMENT);
        data = insertSplitTag(data, EditorConstants.REGEX_IMG);
        data = insertSplitTag(data, EditorConstants.REGEX_AUDIO);
        data = insertSplitTag(data, EditorConstants.REGEX_VEDIO);
        addTextToEditList(data);
    }

    /*插入拆分标签*/
    public String insertSplitTag(String data, String reg) {
        Pattern p_img = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher m_img = p_img.matcher(data);
        data = m_img.replaceAll(EditorConstants.SPLITTAG);
        return data;
    }

    /*将文本数据装载到编辑器集合中*/
    public void addTextToEditList(String data) {
        String filterData = getHtmlFilterString(EditorConstants.REGEX_AA,data);
        String[] textData = filterData.split(EditorConstants.SPLITTAG);
        for (int i = 0; i < textData.length; i++) {
            editShowList.add(new EditorDataEntity(RichTextEditor.TYPE_TEXT, textData[i]));
        }
    }

    /**
     * 对多媒体资源位置进行排序
     */
    private void sortMediaPosition() {
        Collections.sort(editorDataList, new Comparator<EditorDataEntity>() {
            @Override
            public int compare(EditorDataEntity lhs, EditorDataEntity rhs) {
                return lhs.getPosition() - rhs.getPosition();
            }
        });
        setMediaBitmap();
    }

    /*设置bitmap*/
    private void setMediaBitmap() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                try {
                    for (int i = 0; i < editorDataList.size(); i++) {
                        if (editorDataList.get(i).getType().equals(RichTextEditor.TYPE_IMAGE)) {
                            String url = editorDataList.get(i).getUrl();
                            if(!url.contains("http")){
                                url = Network.FILE_SERVER_COMMON_URL + url;
                            }
                            editorDataList.get(i).setBitmapResource(Glide.with(mContext).load(url).asBitmap().into(500, 500).get());
                        } else if (editorDataList.get(i).getType().equals(RichTextEditor.TYPE_VIDEO) || editorDataList.get(i).getType().equals(RichTextEditor.TYPE_AUDIO)) {
                            editorDataList.get(i).setBitmapResource(Glide.with(mContext).load(Network.FILE_SERVER_COMMON_URL + editorDataList.get(i).getPoster()).asBitmap().into(500, 500).get());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                //组合文本数据和多媒体数据
                for (int i = 0; i < editorDataList.size(); i++) {
                    if (editorDataList.get(i).getBitmapResource() == null) {
                        editorDataList.get(i).setBitmapResource(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.default_bg));
                    }
                    EditorDataEntity editData = new EditorDataEntity(editorDataList.get(i).getText(), editorDataList.get(i).getType(), editorDataList.get(i).getUrl(), editorDataList.get(i).getBitmapResource());
                    editData.setMediaPath(editData.getUrl());
                    int number = 2 * i + 1;
                    if (number > editShowList.size()) {
                        editShowList.add(editData);
                    } else {
                        editShowList.add(number, editData);
                    }
                }

                showData(isRichEditorStatus());
            }
        }.execute();
    }

    /**
     * 添加图片到富文本剪辑器
     */
    public void insertBitmap(EditorDataEntity editorDataEntity) {
        editor.insertImage(editorDataEntity);
    }

    /**
     * 获得多媒体个数
     *
     * @param type
     * @return
     */
    public int getMediaCount(String type) {
        int audioCount = 0;
        int videoCount = 0;
        List<EditorDataEntity> editorDataEntityList = editor.buildEditData();
        for (int i = 0; i < editorDataEntityList.size(); i++) {
            if (editorDataEntityList.get(i).getType().equals(RichTextEditor.TYPE_AUDIO)) {
                audioCount++;
                continue;
            }
            if (editorDataEntityList.get(i).getType().equals(RichTextEditor.TYPE_VIDEO)) {
                videoCount++;
            }
        }
        if (type.equals(RichTextEditor.TYPE_AUDIO)) {
            return audioCount;
        } else {
            return videoCount;
        }
    }

    public void setEditorHint(String hint) {
        editor.setEditHint(hint);
    }

    public List<EditorDataEntity> getEditorDataList() {
        return editorDataList;
    }

    public List<EditorDataEntity> getEditShowList() {
        return editShowList;
    }

    public EditText setEditTextFocus(){
        return editor.setEditTextFocus();
    }
}
