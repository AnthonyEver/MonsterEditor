package com.dlvs.monstereditor.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.dlvs.monstereditor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * desc：这是一个富文本编辑器，给外部提供insertImage接口，添加的图片跟当前光标所在位置有关
 * author：mgq
 * date：2017-06-06
 */
@SuppressLint({"NewApi", "InflateParams"})
public class RichTextEditor extends LinearLayout {
    private Context context;
    private static final int EDIT_PADDING = 10; // edittext常规padding是10dp
    private static final int EDIT_FIRST_PADDING_TOP = 10; // 第一个EditText的paddingTop值

    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    public LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private OnKeyListener keyListener; // 所有EditText的软键盘监听器
    private OnClickListener btnListener; // 图片右上角红叉按钮监听器
    private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
    public EditText lastFocusEdit; // 最近被聚焦的EditText
    private int disappearingImageIndex = 0;
    public static final String TYPE_TEXT = "textTag";
    public static final String TYPE_VIDEO = "videoTag";
    public static final String TYPE_AUDIO = "audioTag";
    public static final String TYPE_IMAGE = "imageTag";
    public static final String TYPE_ALINK = "alinkTag";

    public static final boolean STATUS_ENABLE = true;
    public static final boolean STATUS_DISABLE = false;
    private boolean editorStatus = true;
    private String editHint = "*讨论内容";
    private Handler cursorHandler = new Handler();
    private FunctionBarListener functionBarListener;

    private int TAG_TEXT = 1;
    private int TAG_IMAGE = 2;

    public void setEditHint(String editHint) {
        this.editHint = editHint;
        lastFocusEdit.setHint(editHint);
    }

    public RichTextEditor(Context context) {
        this(context, null);
    }

    public RichTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public RichTextEditor(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        if (context instanceof FunctionBarListener) {
            functionBarListener = (FunctionBarListener) context;
        }
        inflater = LayoutInflater.from(context);
        this.setOrientation(LinearLayout.VERTICAL);

        // 1. 初始化allLayout
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        allLayout.setBackgroundColor(Color.WHITE);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        addView(allLayout, layoutParams);

        // 2. 初始化键盘退格监听
        // 主要用来处理点击回删按钮时，view的一些列合并操作
        keyListener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    EditText edit = (EditText) v;
                    onBackspacePress(edit);
                }
                return false;
            }
        };

        // 3. 图片叉掉处理
        btnListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout parentView = (RelativeLayout) v.getParent();
                onImageCloseClick(parentView);
            }
        };

        focusListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (functionBarListener != null) {
                    functionBarListener.setPublishStatus();
                    if (hasFocus) {
                        lastFocusEdit = (EditText) v;
                        functionBarListener.setFunctionBar();
                    } else {
                        functionBarListener.hideFunctionBar();
                    }
                }
            }
        };

        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        EditText firstEdit = createEditText(editHint);
        firstEditParam.setMargins(0, 0, 0, 30);
        allLayout.addView(firstEdit, firstEditParam);
        lastFocusEdit = firstEdit;
    }

    /*功能栏监听*/
    public interface FunctionBarListener {
        void setPublishStatus();

        void setFunctionBar();

        void hideFunctionBar();
    }

    /**
     * 处理软键盘backSpace回退事件
     *
     * @param editTxt 光标所在的文本输入框
     */
    private void onBackspacePress(EditText editTxt) {
        int startSelection = editTxt.getSelectionStart();
        // 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
        if (startSelection == 0) {
            int editIndex = allLayout.indexOfChild(editTxt);
            View preView = allLayout.getChildAt(editIndex - 1); // 如果editIndex-1<0,
            // 则返回的是null
            if (null != preView) {
                if (preView instanceof RelativeLayout || preView instanceof FrameLayout) {
                    // 光标EditText的上一个view对应的是图片
                    onImageCloseClick(preView);
                } else if (preView instanceof EditText) {
                    // 光标EditText的上一个view对应的还是文本框EditText
                    String str1 = editTxt.getText().toString();
                    EditText preEdit = (EditText) preView;
                    String str2 = preEdit.getText().toString();

                    // 合并文本view时，不需要transition动画
                    allLayout.setLayoutTransition(null);
                    allLayout.removeView(editTxt);

                    // 文本合并
                    preEdit.setText(str2 + str1);
                    preEdit.requestFocus();
                    preEdit.setCursorVisible(true);
                    preEdit.setSelection(str2.length(), str2.length());
                    lastFocusEdit = preEdit;
                }
            }
        }
    }

    /**
     * 处理图片叉掉的点击事件
     *
     * @param view 整个image对应的relativeLayout view
     * @type 删除类型 0代表backspace删除 1代表按红叉按钮删除
     */
    private void onImageCloseClick(View view) {
        disappearingImageIndex = allLayout.indexOfChild(view);
        allLayout.removeView(view);
    }

    /**
     * 生成文本输入框
     */
    private EditText createEditText(String hint) {
        EditText editText = new EditText(context);
        editText.setOnKeyListener(keyListener);
        editText.setTag(viewTagIndex++);
        editText.setHint(hint);
        editText.setTextSize(15);
        editText.setOnFocusChangeListener(focusListener);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.addTextChangedListener(editTextWatcher);
        editText.setGravity(Gravity.TOP);
        editText.setHorizontallyScrolling(false);
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (functionBarListener != null) {
                    functionBarListener.setFunctionBar();
                }
            }
        });
        return editText;
    }


    /**
     * 生成图片布局
     */
    private RelativeLayout createImageLayout(EditorDataEntity editorDataEntity) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.edit_imageview, null);
        View playView = null;
        TextView tvDuration;
        View closeView = layout.findViewById(R.id.image_close);
        layout.setTag(TAG_IMAGE, editorDataEntity);
        closeView.setOnClickListener(btnListener);
        return layout;
    }

    /**
     * 添加图片
     */
    public void insertImage(EditorDataEntity editorDataEntity) {
        Bitmap bmp = getScaledBitmap(editorDataEntity.getPoster(), getWidth());
        editorDataEntity.setBitmapResource(bmp);
        insertDataInfo(editorDataEntity);
    }

    /**
     * 插入数据信息
     */
    public void insertDataInfo(EditorDataEntity editorDataEntity) {
        String lastEditStr = lastFocusEdit.getText().toString();
        int cursorIndex = lastFocusEdit.getSelectionStart();
        String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
        int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);

        if (lastEditStr.length() == 0 || editStr1.length() == 0) {
            // 如果EditText为空，或者光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
            if (editorDataEntity.getType().equals(TYPE_AUDIO)) {
            } else {
                addImageViewAtIndex(lastEditIndex, editorDataEntity);
            }

        } else {
            // 如果EditText非空且光标不在最顶端，则需要添加新的imageView和EditText
            lastFocusEdit.setText(editStr1);
            String editStr2 = lastEditStr.substring(cursorIndex).trim();
            if (editorDataEntity.getType().equals(TYPE_AUDIO)) {
            } else {
                addImageViewAtIndex(lastEditIndex + 1, editorDataEntity);
            }
            addEditTextAtIndex(lastEditIndex + 2, editStr2, editorDataEntity.getType());
        }
        hideKeyBoard();
    }

    /**
     * 隐藏小键盘
     */
    public void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index   位置
     * @param editStr EditText显示的文字
     */
    public void addEditTextAtIndex(final int index, String editStr, String tag) {
        EditText etText = createEditText("");
        etText.setText(editStr == null ? "" : editStr);
        etText.setTag(tag);
        allLayout.addView(etText, index);
        allLayout.addView(etText, index);
    }

    /**
     * 在特定位置添加ImageView
     */
    public void addImageViewAtIndex(final int index, final EditorDataEntity editorDataEntity) {
        final RelativeLayout imageLayout = createImageLayout(editorDataEntity);
        DataImageView imageView = (DataImageView) imageLayout
                .findViewById(R.id.edit_imageView);
        imageView.setImageBitmap(editorDataEntity.getBitmapResource());
        imageView.setBitmap(editorDataEntity.getBitmapResource());
        imageView.setAbsolutePath(editorDataEntity.getPoster());
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        // 调整imageView的高度
        int imageHeight = getWidth() * editorDataEntity.getBitmapResource().getHeight() / editorDataEntity.getBitmapResource().getWidth();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, imageHeight);
        imageView.setLayoutParams(lp);
        lp.setMargins(26, 0, 26, 30);
        allLayout.addView(imageLayout, index);
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width view的宽度
     */
    private Bitmap getScaledBitmap(String filePath, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int sampleSize = options.outWidth > width ? options.outWidth / width
                + 1 : 1;
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * dp和pixel转换
     *
     * @param dipValue dp值
     * @return 像素值
     */
    public int dip2px(float dipValue) {
        float m = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    /**
     * 构建编辑器数据(对外提供的接口, 生成编辑数据上传)
     */
    public List<EditorDataEntity> buildEditData() {
        List<EditorDataEntity> dataList = new ArrayList<EditorDataEntity>();
        int num = allLayout.getChildCount();
        for (int index = 0; index < num; index++) {
            View itemView = allLayout.getChildAt(index);
            EditorDataEntity itemData = null;
            if (itemView instanceof EditText) {
                itemData = new EditorDataEntity();
                EditText item = (EditText) itemView;
                if (TextUtil.isEmpty(item.getText().toString().trim())) {
                    continue;
                }
                itemData.setText(item.getText().toString());
                itemData.setType(RichTextEditor.TYPE_TEXT);
            }
            dataList.add(itemData);
            LogUtils.d(dataList.toString());
        }
        return dataList;
    }

    TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (functionBarListener != null) {
                functionBarListener.setPublishStatus();
            }
        }
    };

    public EditText setEditTextFocus() {
        lastFocusEdit.requestFocus();
        lastFocusEdit.setSelection(0);
        return lastFocusEdit;
    }
}
