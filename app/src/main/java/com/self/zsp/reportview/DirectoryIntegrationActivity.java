package com.self.zsp.reportview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.fr.android.activity.LoadAppFromWelcomeActivity;

import base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * @decs: 目录集成
 * @author: 郑少鹏
 * @date: 2018/8/1 15:44
 * @version: v 1.0
 */
public class DirectoryIntegrationActivity extends BaseActivity {
    @BindView(R.id.etDirectoryIntegrationServerAddress)
    EditText etDirectoryIntegrationServerAddress;
    @BindView(R.id.etDirectoryIntegrationServerName)
    EditText etDirectoryIntegrationServerName;
    @BindView(R.id.etDirectoryIntegrationUserName)
    EditText etDirectoryIntegrationUserName;
    @BindView(R.id.etDirectoryIntegrationPassWord)
    EditText etDirectoryIntegrationPassWord;
    @BindView(R.id.cbDirectoryIntegrationWithExtraParametersOrNot)
    CheckBox cbDirectoryIntegrationWithExtraParametersOrNot;
    @BindView(R.id.etDirectoryIntegrationParameterKey)
    EditText etDirectoryIntegrationParameterKey;
    @BindView(R.id.etDirectoryIntegrationParameterValue)
    EditText etDirectoryIntegrationParameterValue;
    @BindView(R.id.btnDirectoryIntegrationStart)
    Button btnDirectoryIntegrationStart;
    /**
     * 选
     */
    private boolean check;

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_directory_integration);
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

    @OnClick(R.id.btnDirectoryIntegrationStart)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnDirectoryIntegrationStart:
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
        String serverAddress = etDirectoryIntegrationServerAddress.getText().toString();
        String serverName = etDirectoryIntegrationServerName.getText().toString();
        String userName = etDirectoryIntegrationUserName.getText().toString();
        String passWord = etDirectoryIntegrationPassWord.getText().toString();
        String parameterKey = etDirectoryIntegrationParameterKey.getText().toString();
        String parameterValue = etDirectoryIntegrationParameterValue.getText().toString();
        if ("".equals(serverAddress)) {
            toastShort(getString(R.string.serverAddressNull));
        } else if ("".equals(serverName)) {
            toastShort(getString(R.string.serverNameNull));
        } else if ("".equals(userName)) {
            toastShort(getString(R.string.userNameNull));
        } else if ("".equals(passWord)) {
            toastShort(getString(R.string.passWordNull));
        } else {
            Intent welcomeIntent = new Intent(this, LoadAppFromWelcomeActivity.class);
            // 地址（数据决策系统）
            welcomeIntent.putExtra("serverIp", serverAddress);
            // 名称（数据决策系统）
            welcomeIntent.putExtra("serverName", serverName);
            // 用户名（数据决策系统）
            welcomeIntent.putExtra("username", userName);
            // 密码（数据决策系统）
            welcomeIntent.putExtra("password", passWord);
            if (check) {
                if (!"".equals(parameterKey) && !"".equals(parameterValue)) {
                    welcomeIntent.putExtra(parameterKey, parameterValue);
                }
                welcomeIntent.putExtra("fromurl", "com.self.zsp.reportview.DirectoryIntegrationActivity");
            }
            startActivity(welcomeIntent);
        }
    }

    /**
     * 点键盘外区域隐键盘
     *
     * @return 控件数组
     */
    @Override
    public int[] hideSoftByEditViewIds() {
        return new int[]{R.id.etDirectoryIntegrationServerAddress,
                R.id.etDirectoryIntegrationServerName,
                R.id.etDirectoryIntegrationUserName,
                R.id.etDirectoryIntegrationPassWord,
                R.id.etDirectoryIntegrationParameterKey,
                R.id.etDirectoryIntegrationParameterValue,};
    }
}
