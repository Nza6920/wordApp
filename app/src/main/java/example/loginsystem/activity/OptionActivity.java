package example.loginsystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import example.loginsystem.R;

import static example.loginsystem.my.config.Config.RELANDING;

public class OptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
    }

    public void backToHome(View view) {
        finish();
    }

    // 更新密码
    public void updatePassword(View view) {
        Intent intent = new Intent(this,UpdatePasswordActivity.class);
        startActivityForResult(intent, RELANDING);
    }

    // 关于我们
    public void aboutUs(View view) {
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
    }

    // 设置间隔数
    public void updateInterval(View view) {
        Intent intent = new Intent(this, SetIntervalActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case RELANDING:
                    if (resultCode == RESULT_OK) {
                        Intent intent = new Intent(this, MainActivity.class);
                        String result = data.getStringExtra("result");
                        intent.putExtra("result", result);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;
              default:
                    break;
        }
    }
}
