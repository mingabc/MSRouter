package com.org.testshopcartmodule;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.org.msrouter_api.MSRouter;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@Route(path = "/shopcart/main")
public class ShopCartActivity extends AppCompatActivity {

    TextView queryAccountInfoTextView;

    TextView showAccountInfoTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopcart_activity_main);
        setTitle("购物车页");
        setupUI();
    }

    void setupUI() {

        queryAccountInfoTextView = findViewById(R.id.queryAccountInfoText);

        showAccountInfoTextView = findViewById(R.id.showAccountInfoTextView);

        queryAccountInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestQueryAccountInfo();
            }
        });

    }

    private void requestQueryAccountInfo() {
        QueryAccountParaModel paramM = new QueryAccountParaModel();
        paramM.token = "327281738127381";
        MSRouter.Companion.request(PathConst.kQueryAccountInfo, paramM).response(QueryAccountResponseModel.class,
                new Function1<QueryAccountResponseModel, Unit>() {
                    @Override
                    public Unit invoke(QueryAccountResponseModel response) {

                        String text = response.msg + "  name: " + response.name;
                        Toast.makeText(ShopCartActivity.this, text, Toast.LENGTH_SHORT).show();
                        if (response.code.equals("0")){
                            //信息请求成功，展示信息
                            displayAccount(response);
                        }
                        return null;
                    }
                }
        );
    }

    private void displayAccount(QueryAccountResponseModel model) {
        String text = "name: " + model.name + "\n address: " + model.address;
        showAccountInfoTextView.setText(text);
    }
}

