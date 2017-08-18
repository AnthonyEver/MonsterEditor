package com.dlvs.monstereditor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dlvs.monstereditor.editor.RichTextEditor;
import com.dlvs.monstereditor.editor.RichTextEditorManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.rte_main)
    RichTextEditor rteMain;
    RichTextEditorManager editorManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        editorManager = new RichTextEditorManager(rteMain, this);
        editorManager.setEditorHint("请输入回复内容");
        editorManager.setEditTextFocus();
    }
}
