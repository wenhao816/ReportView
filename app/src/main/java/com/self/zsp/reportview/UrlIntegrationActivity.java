package com.self.zsp.reportview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.fr.android.activity.LoadAppFromURLActivity;

import base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * @decs: URL集成
 * 直接使用URL访问服务器，原生预览模板
 * @author: 郑少鹏
 * @date: 2018/8/1 11:27
 * @version: v 1.0
 */
public class UrlIntegrationActivity extends BaseActivity {
    @BindView(R.id.etUrlIntegrationServerAddress)
    EditText etUrlIntegrationServerAddress;
    @BindView(R.id.etUrlIntegrationTemplateName)
    EditText etUrlIntegrationTemplateName;
    @BindView(R.id.etUrlIntegrationTitle)
    EditText etUrlIntegrationTitle;
    @BindView(R.id.cbUrlIntegrationFillingMode)
    CheckBox cbUrlIntegrationFillingMode;
    @BindView(R.id.btnUrlIntegrationStart)
    Button btnUrlIntegrationStart;
    /**
     * 选
     */
    private boolean check;

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_url_integration);
        ButterKnife.bind(this);
    }

    @Override
    protected void stepUI() {

    }

    @Override
    protected void initConfiguration() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void startLogic() {

    }

    @Override
    protected void setListener() {

    }

    @OnClick(R.id.btnUrlIntegrationStart)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnUrlIntegrationStart:
                start();
                break;
            default:
                break;
        }
    }

    /**
     * "@Optional" 选择性注入，当前对象不存则抛异常。可于变量或方法上加一注解，变注入为选择性（目标View存则注入，不存则什么事都不做）
     *
     * @param compoundButton 按钮
     * @param isChecked      选状
     */
    @Optional
    @OnCheckedChanged(R.id.cbUrlIntegrationFillingMode)
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        check = isChecked;
    }

    /**
     * 开始
     */
    private void start() {
        String serverAddress = etUrlIntegrationServerAddress.getText().toString();
        String templateName = etUrlIntegrationTemplateName.getText().toString();
        String title = etUrlIntegrationTitle.getText().toString();
        if ("".equals(serverAddress)) {
            toastShort(getString(R.string.serverAddressNull));
        } else if ("".equals(templateName)) {
            toastShort(getString(R.string.templateNameNull));
        } else {
            Intent intent = new Intent(this, LoadAppFromURLActivity.class);
            intent.putExtra("url", check ? serverAddress + "?reportlet=" + templateName + "&op=write" : serverAddress + "?reportlet=" + templateName);
            intent.putExtra("title", title);
            startActivity(intent);
        }
    }

    /**
     * 点键盘外区域隐键盘
     *
     * @return 控件数组
     */
    @Override
    public int[] hideSoftByEditViewIds() {
        return new int[]{R.id.etUrlIntegrationServerAddress, R.id.etUrlIntegrationTemplateName};
    }
}
