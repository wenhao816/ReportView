package com.self.zsp.reportview;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import util.StatusBarUtils;

/**
 * @decs: 主页
 * @author: 郑少鹏
 * @date: 2018/7/31 20:31
 * @version: v 1.0
 */
public class MainActivity extends BaseActivity {
    @BindView(R.id.btnUrlIntegration)
    Button btnUrlIntegration;
    @BindView(R.id.btnDirectoryIntegration)
    Button btnDirectoryIntegration;

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        StatusBarUtils.setColorNoTranslucent(this, ContextCompat.getColor(this, R.color.background));
        setContentView(R.layout.activity_main);
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

    @OnClick({R.id.btnUrlIntegration, R.id.btnDirectoryIntegration})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            /*
              URL集成
             */
            case R.id.btnUrlIntegration:
                jumpNoBundle(UrlIntegrationActivity.class);
                break;
            /*
              目录集成
             */
            case R.id.btnDirectoryIntegration:
                jumpNoBundle(DirectoryIntegrationActivity.class);
                break;
            default:
                break;
        }
    }
}
